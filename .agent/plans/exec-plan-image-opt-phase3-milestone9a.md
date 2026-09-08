<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Phase 3 Milestone 9A — Correctness and final-stack harness

This ExecPlan follows `AGENTS.md`, `.agents/skills/logical-commits/SKILL.md`,
and the ExecPlan contract from `TotalCross/totalcross-depot-tools/.agent/PLANS.md`.
It is the first of two sequential Milestone-9 plans. Execute 9A completely
before `exec-plan-image-opt-phase3-milestone9b.md`.

## Purpose / Big Picture

Prepare the rebased Image Optimization Phase 3 for final measurement. Correct
two remaining compact-format defects, make the benchmark configuration reflect
the complete frozen Phase 2 feature set, and add the cross-feature/adaptive-JPEG
correctness coverage needed before any new authoritative S1 is captured.

No new optimization is allowed. At the end of 9A, runtime correctness and the
measurement harness are frozen; 9B performs the authoritative benchmarks,
GitHub/Android validation, and final Phase-3 freeze.

## Working Set and Resume Protocol

Branch:

    perf/image-opt-phase3-formats

Frozen Phase 2:

    6d1c95f77fcb9c74d19b4e9393dba7c82cd37aee

Expected pre-Milestone-9 Phase-3 tip:

    604d7bb3bdba05e70699b648c59e093fb1c330e3

Commit both Milestone-9 plans under:

    .agent/plans/exec-plan-image-opt-phase3-milestone9a.md
    .agent/plans/exec-plan-image-opt-phase3-milestone9b.md

Update existing support files; do not create parallel state/history systems:

    .agent/state/image-opt-phase3-formats.md
    .agent/evidence/image-opt-phase3-formats.jsonl
    .agent/archive/image-opt-phase3-formats-history.md
    .agent/reports/image-opt-phase3-formats-editorial.md
    .agent/design/image-optimization-benchmark-protocol.md
    .agent/benchmarks/image-opt-phase3-formats/

Resume economically:

1. read `.agent/state/image-opt-phase3-formats.md` first;
2. read only the active section of this plan;
3. inspect only active paths named by state;
4. search the evidence JSONL only for the active slice when provenance is
   needed;
5. do not reread the full archive, editorial report, or historical benchmark
   tree during normal resume.

Before first edit, read `AGENTS.md`, logical-commits, and the benchmark protocol
once. Keep active decisions in state afterward.

Every new file must stay <=20 KiB or approximately 600 lines. Split before the
limit. Do not refactor an existing file merely to make it smaller. Direct plan
artifacts must be committed. Ordinary build output, dylibs, executables, APKs,
full build logs, and raw logcat are not plan artifacts and must not be committed.

## Progress

- [x] Verify branch/base/tip and clean-tree safety.
- [x] Commit both Milestone-9 plans and activate 9A in state.
- [x] Fix compact final-buffer accounting and ARGB4444 opacity semantics.
- [x] Freeze explicit final Phase-2/Phase-3 feature sets and exact-base adapter.
- [x] Add final-stack, invalidation, observer, writePixels, and adaptive-JPEG
      correctness coverage.
- [x] Complete focused build/smoke validation and hand off to Plan 9B.

## Current Architecture and Scope

Use explicit phase feature sets, never contiguous ranges.

Final Phase 2 is exactly:

    0  DECODE_ZERO_COPY
    1  RASTER_OPACITY_METADATA
    2  RASTER_OPAQUE_WRITE_PIXELS
    3  RASTER_ROW_READBACK
    4  RASTER_DIRECT_COLOR_MATERIALIZATION
    13 RASTER_TARGET_COLORTYPE_CONVERSION
    14 RASTER_PHYSICAL_VARIANT_CACHE
    15 RASTER_PHYSICAL_IDENTITY_FOLDING

Phase 3 storage is exactly:

    5 STORAGE_RGB565
    6 STORAGE_GRAY8
    7 STORAGE_ARGB4444

IDs 8-12 remain disabled in Milestone 9. `DIAGNOSTIC_ACCOUNTING` stays disabled
as an optimization feature. Test accounting may be enabled through existing
test hooks, identically across scenarios and outside timed work.

Before configuring a benchmark/smoke scenario, reset settings, explicitly
disable every ID `0..FEATURE_COUNT-1`, then enable only the requested explicit
set. Never rely on `DEFAULT`.

Compact backing contract remains:

    RGBA8888  4 B/px
    RGB565    2 B/px, opaque
    GRAY8     1 B/px, opaque
    ARGB4444  2 B/px, premultiplied alpha

Selection remains structural: GRAY8 for structural grayscale non-alpha when
enabled; otherwise RGB565 for non-alpha; ARGB4444 for alpha/tRNS; otherwise
RGBA8888. Compact formats are canonical only for immutable/source backings.
Readback/encoding do not promote. Mutable/full-precision barriers promote
transactionally to RGBA8888. Failed promotion preserves the compact backing and
retryability.

Preserve final Phase-2 derived-state rules: identity folding wins before cache;
one persistent derived slot per source/generation; first identical variant
occurrence observes, second materializes, later hits reuse; target-color and
physical-variant paths share the slot; mutation/promotion invalidates; derived
state never becomes canonical public state; GPU builds never execute software
raster fast paths.

Out of scope: SIMD packing, dithering, cache budgets, mmap, GPU-only backing,
compressed textures, hosted benchmark workflow creation, and Phase-4 lifecycle
work.

## Plan of Work

### Milestone 9.0 — Bootstrap and freeze the contract

Run:

    git status --short
    git branch --show-current
    git rev-parse HEAD
    git rev-parse origin/perf/image-opt-phase2-raster
    git merge-base HEAD 6d1c95f77fcb9c74d19b4e9393dba7c82cd37aee
    git diff --check

Require branch `perf/image-opt-phase3-formats`, frozen Phase-2 remote SHA and
merge-base both exactly `6d1c95f77...`, and no unrelated changes that would be
overwritten. Expected tip is `604d7bb...`; if later unreviewed runtime changes
exist, stop and report instead of absorbing them.

Add both 9A and 9B plans and rewrite the existing state so Milestone 9A is
active. Move completed Milestone 1-8 operational detail to existing archive if
needed; do not duplicate it in active state.

Commit:

    docs(image): add phase 3 final integration closeout

No build for this docs-only checkpoint.

### Milestone 9.1 — Correct compact accounting and ARGB4444 opacity

Active native paths:

    TotalCrossVM/third_party/jpeg/JpegLoader.c
    TotalCrossVM/third_party/png/PngLoader.c
    TotalCrossVM/src/nm/ui/skia/skia_image_backing.cpp

Fix `decodeFinalBufferBytesForTest` so each successful decode records exactly
one final-buffer amount:

    RGBA8888 -> width * height * 4
    RGB565   -> width * height * 2
    GRAY8    -> width * height
    ARGB4444 -> width * height * 2

Compact decode must not additionally add an RGBA-sized final buffer.
`temporaryRgbaDecodeBytesForTest` remains zero in direct compact paths.

Fix ARGB4444 opacity during PNG compact packing. Derive the proof from the same
decoded alpha used to pack each ARGB4444 pixel, regardless of
`RASTER_OPACITY_METADATA`. Start as opaque and clear on the first alpha != 255.
Do not add a second image scan.

If ARGB4444 is selected but transformed libpng rows do not expose usable alpha,
keep backing opacity `UNKNOWN`; never infer opaque. Do not create RGBA staging or
change format selection to solve that case.

Add regression coverage proving:

- translucent ARGB4444 + `STORAGE_ARGB4444=ON` +
  `RASTER_OPAQUE_WRITE_PIXELS=ON` + `RASTER_OPACITY_METADATA=OFF` remains
  ARGB4444, falls back from writePixels, and matches compact reference output;
- structurally alpha-bearing but pixel-opaque ARGB4444 under the same settings
  can be proven opaque independently of opacity metadata;
- successful compact JPEG/PNG decode reports only actual final compact bytes;
- injected failure after compact final-buffer allocation leaves accounting/live
  backing balanced and retry succeeds.

Run focused image tests and existing compact smoke only. Do not benchmark.

Commit runtime fix and direct regression tests together:

    fix(image): correct compact decode accounting and opacity

Record this SHA as the last runtime correction in the next state-bearing commit;
do not create a recursive docs commit only to record its own SHA.

### Milestone 9.2 — Freeze the final-stack harness

Modify existing benchmark support to expose explicit immutable sets equivalent
to:

    PHASE2_FINAL = {0,1,2,3,4,13,14,15}
    PHASE3_STORAGE = {5,6,7}

Add one scenario helper that resets settings, explicitly disables all feature
IDs, then enables an explicit requested set. Remove final-stack configuration
that uses a range ending at `RASTER_DIRECT_COLOR_MATERIALIZATION`.

Create two final workload names:

    milestone9-isolated
    milestone9-full-stack

Reuse the existing deterministic RGB565, GRAY8, and ARGB4444 fixtures and
existing full-input/full-output hashes and quality oracles. Keep correctness,
quality, hashing, and counter interpretation outside the timed region. During
harness freeze only, adjust batch count so every timed sample is >=30 ms.

If a new benchmark app is cleaner than extending the existing one, create it
under `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/` and keep it
<=20 KiB/~600 lines. Reuse current support/native hooks; do not duplicate the
fixture/oracle implementation.

Create exact-base adapter files under:

    .agent/benchmarks/image-opt-phase3-formats/milestone9/true-base-harness/

The adapter must execute the final Milestone-9 harness against exact Phase 2
`6d1c95f77...`. It may shim only Phase-3 storage IDs/APIs, compact-format
introspection, and Phase-3 counters unavailable on that base. It must preserve
real Phase-2 behavior for IDs `0,1,2,3,4,13,14,15`; never stub or emulate them.

Record a deterministic SHA-256 digest over every adapted harness file.

Before authoritative S1 exists, dry-run both workloads for 3-5 samples and
prove fixture hashes are identical, timed batches >=30 ms, S1 can enable the
real final Phase-2 stack, and no quality/hash analysis is timed.

Commit the frozen harness/adapter before any authoritative capture:

    test(image): add phase 3 final-stack validation

### Milestone 9.3 — Cross-feature and adaptive-JPEG correctness

Use a dedicated smoke app or extend the compact smoke while keeping any new file
<=20 KiB/~600 lines. Reuse, do not reinvent, the Phase-2 geometry recipes in:

    ImageRasterPhysicalIdentityBenchmarkApp
    ImageRasterVariantBenchmarkApp
    ImageRasterTargetColorBenchmarkApp
    ImageRasterPhysicalVariantSmokeApp

Read these once while implementing, then record the chosen recipes in state.

With `PHASE2_FINAL + PHASE3_STORAGE` enabled, prove:

1. **Physical identity.** A compact opaque source with the established
   canceling physical transform increments identity-hit/resample-avoided
   counters, produces no physical-variant materialization for that draw, and
   remains compact.

2. **Physical variant.** Repeated non-identity eligible geometry executes at
   least three identical occurrences: first observes/misses, second
   materializes, later hit reuses. Exactly one persistent variant is admitted
   for the key; source remains compact; promotion count stays zero.

3. **Target color + physical variant.** Using the existing software-raster
   target-color test surface, an opaque compact source requiring geometry and
   target conversion yields one final-size target-color physical variant. Do
   not create both a persistent RGBA geometry variant and another persistent
   target-color representation. Counters must show one physical materialization
   plus reuse and no duplicate target-color materialization.

4. **Invalidation.** Create/reuse a compact-derived variant, then cross
   `getGraphics()` and mutate. Promotion occurs once to RGBA8888. Old derived
   state cannot hit afterward. Repeated post-mutation draws may create only a
   new generation-correct variant.

5. **Observers.** Repeated draw, `getPixels()`, and PNG encoding preserve the
   compact source and use row-wise readback without a full RGBA staging raster.

6. **writePixels.** RGB565 and GRAY8 opaque draws preserve exact output parity.
   Translucent ARGB4444 falls back conservatively. Source formats remain
   unchanged.

Add compact adaptive-JPEG correctness by reusing the nearest existing adaptive
JPEG smoke/test invocation pattern; read that file once and copy its API usage,
not its prose/history.

Exercise RGB565 color JPEG and GRAY8 JPEG at decoder denominators 1, 2, 4, and 8
through the existing full/best-fit/targeted APIs as appropriate. Assert for each
case:

- physical decoded dimensions match the existing tier rule;
- logical encoded-source dimensions remain unchanged;
- `contentScale`/presentation metadata matches the existing adaptive contract;
- expected compact backing format is selected;
- final-buffer bytes equal physical width * height * compact BPP;
- temporary full RGBA decode bytes are zero;
- targeted decode alone does not promote;
- injected compact-final-buffer failure cleans up and retry succeeds.

Retain established full-resolution barriers such as nearest/rotate. Do not
change adaptive decode semantics to favor compact storage.

Run before handing off to 9B:

- focused `totalcross.ui.image.*` tests;
- SDK `dist -x test`;
- Release macOS software-Skia CMake/Ninja build;
- compact Milestone-9 integration smoke;
- `git diff --check`.

If only tests/harness changed after the 9.1 runtime fix, commit:

    test(image): cover compact final-stack integration

If any new runtime correctness defect is discovered, fix it in its own logical
`fix(image): ...` commit with a regression test before proceeding. No
authoritative S1 may be captured until runtime and harness are frozen.

At the end of 9A, rewrite state with:

- frozen Phase-2 SHA;
- last runtime correction SHA;
- final harness commit SHA and adapter digest;
- exact active test/source paths;
- focused validation results;
- next action: execute Plan 9B Milestone 9.4, no further runtime edits expected.

Do not add a separate docs-only commit solely for this state update if it can be
included in the final logical test/harness checkpoint.

## Surprises & Discoveries

Known entering 9A:

- historical Phase-3 "full stack" enabled only Phase-2 IDs 0-4 and remains
  historical evidence, not final-stack evidence;
- compact JPEG/PNG currently over-report `decodeFinalBufferBytesForTest` by also
  adding an RGBA-sized amount;
- ARGB4444 opacity must be independent of `RASTER_OPACITY_METADATA` to avoid an
  unsafe opaque-fast-path classification.

Record only new discoveries that change remaining work. Move resolved detail to
existing archive at milestone consolidation.

## Decision Log

- Milestone 9 is closeout/correctness/measurement work only; no new optimization.
- Final Phase 2 is exactly `{0,1,2,3,4,13,14,15}`; Phase 3 storage is `{5,6,7}`.
- Every scenario explicitly disables all IDs before enabling a set.
- Historical per-format Phase-3 samples remain immutable and are not relabeled
  after the rebase.
- ARGB4444 unprovable opacity resolves to `UNKNOWN`, never optimistic opaque.
- Authoritative performance does not start until Plan 9A freezes runtime and
  harness.

## Validation and Acceptance

Plan 9A is complete only when:

- both known runtime defects are fixed and covered;
- exact Phase-2 adapter uses `6d1c95f77...` and preserves real IDs 0-4,13-15;
- explicit feature-set configuration replaces phase ranges in final-stack work;
- identity, variant, target-color combination, invalidation, observers,
  writePixels, and adaptive-JPEG compact correctness all pass;
- focused image tests, SDK distribution, Release macOS software-Skia build,
  compact integration smoke, and `git diff --check` pass;
- no authoritative benchmark samples have been captured before harness freeze;
- all direct 9A artifacts are committed and state points directly to Plan 9B.

Any output/hash/quality mismatch, stale/duplicate persistent variant, compact
promotion during ordinary draw/readback, unsafe translucent writePixels use, or
adaptive-JPEG semantic regression blocks Plan 9B.

## Risks and Open Questions

No architectural questions are delegated to the executor.

Fixed responses:

- missing usable ARGB4444 alpha proof -> opacity `UNKNOWN`, conservative fallback;
- runtime change after harness freeze but before S1 -> update state, revalidate,
  and only then start 9B;
- new file would exceed size limit -> split before commit, do not refactor old
  files just to satisfy the new-file rule;
- unrelated local/untracked paths -> preserve untouched and record in state.

## Idempotence and Recovery

Use temporary worktrees/directories for the frozen Phase-2 adapter/runtime;
never checkout/reset over the active Phase-3 tree. Do not overwrite historical
benchmark evidence. Failed dry-run artifacts may be removed only if they were
created by the uncommitted current attempt.

Follow logical-commits. Commit functional slices with their direct tests. Do not
amend/squash prior Phase history and do not create recursive commits merely to
record their own SHA.

No push, merge, tag, release, or branch-history rewrite is required in Plan 9A.

## Outcomes & Retrospective

Milestone 9A delivered the compact decode accounting and ARGB4444 opacity
corrections, explicit `{0,1,2,3,4,13,14,15}` and `{5,6,7}` configuration,
the final-stack benchmark workloads, and the exact-base adapter. The final
harness revision is `a6e23f73011c5457b8d6e9cbc69be9bcaed1428b`; its adapter
digest is `1fb32deccbb0c1d79c041e8f25af6d7bd1a3e6da4536fa3c4ddfb521aa08a158`.
Focused image tests, SDK distribution, Release macOS software-Skia build,
compact smoke, final-stack correctness, and both 3-sample dry runs passed.
No authoritative Milestone-9 sample was captured before the freeze. GO for
Plan 9B Milestone 9.4; no further runtime edits are expected in 9A.

## Revision Note

2026-09-08: Split Milestone 9 into two sequential ExecPlans to keep each new
plan below the repository 20 KiB/~600-line artifact limit. Plan 9A freezes
correctness and measurement inputs; Plan 9B performs measurement/platform
closeout and final handoff.
