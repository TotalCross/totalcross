<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Plan 2 of 2 — enable device-space OPAQUE_WRITE_PIXELS and benchmark it on macOS

This ExecPlan follows `AGENTS.md`, the ExecPlan rules in
`TotalCross/totalcross-depot-tools:.agent/PLANS.md`, and
`.agents/skills/logical-commits/SKILL.md`.

Execute only on branch `perf/image-decode-distributed-benchmark` after
`.agent/plans/image-write-pixels-policy-01-benchmark.md` is complete.

## Purpose / Big Picture

Turn the physical 1:1 opportunities already observed by the benchmark into safe
`RASTER_OPAQUE_WRITE_PIXELS` hits.

The implementation must operate in device pixels, manually enforce clipping,
preserve opacity requirements, and fall back for unsupported geometry.

Then execute the focused 24-process macOS profile from Plan 1 and record whether
active cold/warm work improves.

Do not modify `PHYSICAL_VARIANT_CACHE`, JPEG decode/tier selection, resampling,
storage formats, or prefetch policy.

## Working Set and Resume Protocol

Create and commit:

- `.agent/plans/image-write-pixels-policy-02-device-copy.md`
- `.agent/state/image-write-pixels-policy-02-device-copy.md`
- `.agent/evidence/image-write-pixels-policy-02-device-copy.md`
- `.agent/reports/image-write-pixels-policy-02-device-copy-editorial.md`
- `.agent/benchmarks/image-write-pixels-device-policy/summary.md` at final
  benchmark completion.

On resume, read this state first. Read Plan 1 state/report once to verify its
measurement/profile contract, then only active source paths.

Primary paths:

- `TotalCrossVM/src/nm/ui/skia/skia_image_backing.cpp`
- `TotalCrossVM/src/nm/ui/skia/skia_image_backing_internal.h`
- `TotalCrossVM/src/nm/ui/skia/skia_surface_test.cpp`
- benchmark/runner paths changed by Plan 1 only when validation requires them.

## Execution Constraints

- Operate economically with tokens/output.
- Preserve unrelated local changes and stage only task paths.
- Build only SDK and macOS, and only at milestone boundaries.
- Native smoke tests may run only at milestone boundaries/final validation.
- Do not build between logical commits inside one milestone.
- Commit deliberate repository artifacts; do not commit normal build outputs,
  verbose logs, raw result directories, or benchmark ZIPs.
- Every new tracked file must remain <=20 KiB and approximately <=600 lines.
- Do not refactor existing large files merely to reduce size.
- Do not push.

Before editing:

    test "$(git branch --show-current)" = "perf/image-decode-distributed-benchmark"
    git rev-parse HEAD

Require Plan 1 state to report:

- active work metrics implemented;
- full profile = 126 processes;
- focused profile = 24 processes;
- no native optimization policy changed.

## Progress

- [x] Bootstrap Plan 2 state/evidence.
- [x] Milestone 1: implement safe device-space regular writePixels.
- [x] Milestone 2: prove clipping/transform correctness and smoke the real path.
- [x] Milestone 3: run the focused macOS matrix and close the plan.

## Current Architecture and Scope

Completed diagnostics showed:

- current regular writePixels attempts have zero hits;
- roughly 90% of cold attempts and effectively all warm attempts are physical
  1:1 candidates on macOS;
- current policy rejects them because it reasons in logical space and requires
  identity matrix plus `saveCount == 1`;
- macOS content scale 2.0 commonly maps a 179x179 logical destination to a
  358x358 physical copy;
- `SkCanvas::writePixels()` ignores current matrix and clip.

Therefore the new policy must compute the exact physical source subset and
destination after applying matrix and clip. It must never simply remove the
legacy guards.

## Fixed Architectural Decisions

### A. One device-copy planner for diagnostics and execution

In `skia_image_backing.cpp`, replace the diagnostic-only candidate logic with
one internal planner used by both candidate accounting and actual execution.

Define a small local POD struct equivalent to:

    WritePixelsDeviceCopyPlan {
        SkIRect sourcePixels;
        SkIRect destinationPixels;
        bool clipped;
    }

Do not expose this struct publicly.

The planner succeeds only when all conditions below hold:

1. target canvas/source dimensions are valid and `alphaMask == 255`;
2. source rectangle coordinates are finite, integral, non-empty, and inside the
   physical source;
3. total matrix is finite positive scale+translation only:
   no perspective, skew, rotation, zero scale, or negative scale;
4. logical destination edges map to finite integral device edges;
5. mapped physical destination width/height exactly equal source rectangle
   width/height; no scaling is permitted;
6. obtain `getDeviceClipBounds()` and intersect the mapped destination with it;
7. obtain writable target bounds through `targetCanvas->peekPixels()` and
   intersect with those bounds too;
8. reject empty visibility rather than performing an untracked no-op;
9. derive the visible source subset from physical destination offsets;
10. require the derived source subset to remain integral, non-empty, in source
    bounds, and exactly the same dimensions as visible destination.

Because writePixels ignores matrix and clip, actual writes use only
`sourcePixels` and `destinationPixels` from this plan.

`saveCount` is no longer an eligibility rule.

### B. Actual copy behavior

Preserve existing opacity behavior.

For `tryWritePixelsImage()`:

- require existing `sourceOpaque`;
- `peekPixels`;
- `extractSubset(plan.sourcePixels)`;
- `writePixels` at `plan.destinationPixels.left/top`.

For native backing:

- RGBA8888: keep existing metadata/proof behavior, obtain pixels/snapshot,
  extract the visible subset, then write it;
- ARGB4444: preserve current opaque requirement;
- existing compact opaque formats: read/convert only visible source rows and
  write only corresponding visible destination rows.

Any planner, opacity, source-pixel, allocation, or write failure uses the
existing generic rendering fallback.

Do not change `tryDirectImageCopy()` or `tryDirectPhysicalCopy()` behavior.

### C. Regular-path accounting

Global writePixels counters remain unchanged for compatibility.

Add counters incremented only by `tryWritePixelsImage()` and
`tryWritePixels()`:

- `writePixelsRegularAttempts`
- `writePixelsRegularHits`
- `writePixelsRegularFallbacks`
- `writePixelsRegularCopiedBytes`
- `writePixelsRegularClippedHits`

Require:

    regularAttempts == regularHits + regularFallbacks

Use the same device planner for candidate counting and execution.

Update detailed rejection semantics:

- `writePixelsRejectMatrix`: unsupported/non-finite transform;
- `writePixelsRejectSaveCount`: compatibility field, no longer a rule, expected
  zero for the regular policy;
- `writePixelsRejectSourceRect`: invalid/non-integral/out-of-bounds source;
- `writePixelsRejectSizeMismatch`: physical destination is not 1:1;
- `writePixelsRejectFractionalDestination`: mapped device edges not integral;
- `writePixelsRejectDestinationBounds`: no valid writable visible target;
- opacity/source-pixel/write-failure retain current meanings.

`writePixelsDeviceOneToOneCandidates` counts successful physical plans before
opacity/pixel availability.

Expose the new regular counters through existing Java/native benchmark
accounting using the repository's private native-name length constraint. Do not
introduce bridge names that exceed the VM resolver-safe length.

### D. Native correctness proof

Extend `skia_surface_test.cpp`.

Use `skia_image_backing_internal::tryWritePixelsImage()` with
`skia_image_backing_canvas_for_surface_id()` so the policy can be exercised
directly without a new public API.

Cover:

- identity 1:1 opaque copy;
- content scale 2.0 mapping logical destination to physical 1:1;
- saved partial clip, verifying pixels inside and outside the clip;
- translated destination with partial device clip;
- unsupported skew/rotation or equivalent non-scale-translate matrix fallback;
- fractional mapped physical boundary fallback;
- alpha mask below 255 fallback;
- non-opaque image fallback.

The partial-clip assertions are mandatory because this policy bypasses Skia's
normal clip application during the actual `writePixels()` call.

## Plan of Work

### Bootstrap checkpoint

Create Plan 2 state/evidence files, verify Plan 1 completion, validate
size/header/whitespace, and commit.

Suggested commit:

    docs(benchmark): plan device write pixels policy

No build or native smoke is allowed here.

### Milestone 1 — implement the device-space policy

Modify `skia_image_backing.cpp` according to Contracts A/B/C.

Do not change physical variant, target-color conversion, JPEG, storage, or
prefetch policy.

Update benchmark accounting/schema for the new regular-path counters.

Suggested commit:

    perf(skia): enable device space write pixels

Before commit use only focused static/header/whitespace validation.

No native build yet.

### Milestone 2 — correctness tests and real-path smoke

Add the native cases from Contract D.

Suggested commit:

    test(skia): cover clipped device write pixels

Do not build between the policy and test commits.

Milestone 2 gate:

1. Build only macOS ARM64:
   - `tcvm`;
   - `Launcher`;
   - the native Skia test target containing `skia_surface_test.cpp`.
2. Run the native Skia test.
3. Build SDK only if the policy/accounting changed Java/native bridge contracts.
4. Package one macOS-only benchmark bundle.
5. Run benchmark self-test.
6. Run focused real-path smokes:
   - `4/off`, `4/on`;
   - `6/off`, `6/on`;
   - `32795/off`, `32799/off`;
   - `32795/on`, `32799/on`.

Require:

- all native pixel/clip assertions pass;
- no image draws outside the explicit clip;
- regular attempts = hits + fallbacks;
- regular hits > 0 for masks with bit 2 enabled;
- `writePixelsRejectSaveCount == 0` on the new regular path;
- work-time/JPEG frame invariants from Plan 1 still pass.

If `regularClippedHits` is zero in real scroll, record that fact; the native
partial-clip test remains the correctness proof.

Fix correctness/accounting defects before proceeding.

## Milestone 3 — focused macOS benchmark

Resolve the corpus deterministically:

1. use `$TC_IMAGE_CORPUS` when set and valid;
2. otherwise use `~/Downloads/win32/win32` only when valid;
3. otherwise stop with an explicit corpus-path error.

Use the exact passing implementation package unless source changed afterward.

Run:

    python3 <bundle>/run-benchmark.py \
      --bundle <bundle> \
      --phase self-test \
      --profile write-pixels-policy

Run required focused smokes, then:

    python3 <bundle>/run-benchmark.py \
      --bundle <bundle> \
      --phase matrix \
      --profile write-pixels-policy

Do not run `--phase full`.
Do not run the standalone decode matrix.

Require exactly 24 PASS rows and no missing/duplicate `(mask,prefetch,run)` keys.

Use active `work_time_ns`, not paced `frame_time_ns`, as the primary metric.

Create:

- `.agent/benchmarks/image-write-pixels-device-policy/summary.md`

Include:

- implementation HEAD;
- SDK/native hashes;
- corpus count/hash;
- raw results directory;
- result ZIP path and SHA-256;
- 24/24 process status;
- both rounds for each controlled pair and prefetch profile;
- median work P50/P95;
- median paint P50/P95;
- enabled-minus-control ns/percent deltas;
- regular attempts/hits/fallbacks and hit rate;
- candidates and candidate-to-hit conversion;
- clipped hits;
- JPEG ns for context;
- any pair marked `INCONCLUSIVE_VARIANCE`.

Controlled comparisons are:

- `0 -> 4`;
- `2 -> 6`;
- `32795 -> 32799`.

If the two rounds disagree in direction for `work_time_p50`, label that pair
`INCONCLUSIVE_VARIANCE`; do not invent a winner and do not run a third full
matrix automatically.

Performance is a measured result, not a correctness gate. If the policy
regresses, preserve the experimental implementation on this performance branch
and report the regression. Do not silently change another optimization.

Update state/evidence/editorial report and close the plan.

Suggested final commit:

    docs(benchmark): record device write pixels results

## Surprises & Discoveries

Record only observations that affect remaining work, such as:

- candidate succeeds structurally but fails opacity/pixel availability;
- warm paced frame time stays ~16 ms while active work changes;
- cold improves without physical-variant changes;
- a two-round pair is inconclusive.

Do not expand scope in response.

## Decision Log

- Manually apply device matrix and clip because writePixels ignores both.
- Reuse one planner for diagnostic eligibility and execution.
- Keep direct copy paths unchanged.
- Add regular-path counters so composite masks are interpretable.
- Evaluate with six masks × two prefetch modes × two rounds.
- Keep physical-variant and JPEG policies out of scope.

## Validation and Acceptance

Before every logical commit:

    python3 scripts/validate-copyright-headers.sh --files <changed tracked files>
    git diff --check --cached

Run the logical-commits checker before the commit and verify the created commit
afterward.

Every new tracked file must remain <=20480 bytes and approximately <=600 lines.

Functional acceptance:

- device copy never writes outside effective clip/target;
- unsupported transforms and non-1:1 mappings fall back;
- regular writePixels hits become nonzero in real macOS scroll;
- Plan 1 active-work metrics remain valid;
- focused matrix completes 24/24;
- no physical-variant, JPEG, storage, or prefetch policy changes.

## Risks and Open Questions

- `peekPixels()` may fail; fallback must remain correct.
- Mask 4 may spend more on opacity proof than mask 6; both are intentionally
  measured.
- Two rounds are economical rather than publication-grade statistics.
- Existing environment metadata issues remain out of scope.
- RSS/memory collection remains out of scope unless already available.

## Idempotence and Recovery

Use task-specific build/package/result directories.

Never delete unrelated caches, prior results, or local changes.

On benchmark failure preserve partial output. Retry once only for a clearly
environmental/transient failure and use a fresh output directory.

Do not amend or rewrite commits.

## Logical Commit Policy

Follow `.agents/skills/logical-commits/SKILL.md`.

Expected commits:

- `docs(benchmark): plan device write pixels policy`
- `perf(skia): enable device space write pixels`
- `test(skia): cover clipped device write pixels`
- `docs(benchmark): record device write pixels results`

Create focused fix commits only for validation defects.

## Outcomes & Retrospective

At completion record:

- policy contract delivered;
- native clipping/transform proof;
- build/smoke results;
- 24-process benchmark result;
- cold/warm work-time pairwise deltas;
- candidate-to-hit conversion;
- output paths/hashes;
- inconclusive pairs/deviations;
- explicit statement that physical-variant and JPEG policies were unchanged.

The editorial report must use the section structure required by `.agent/PLANS.md`.

## Revision Note

Initial Plan 2: implement safe device-space `OPAQUE_WRITE_PIXELS` and evaluate it
with the focused two-round macOS profile prepared by Plan 1.
