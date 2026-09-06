<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Phase 3: Compact raster backing formats

Follow `AGENTS.md`, `.agent/PLANS.md`, logical-commits, and the image benchmark protocol.

## Goal

Extend `NativeImageBacking` from fixed RGBA8888 to internal compact source
formats without changing public `Image` semantics:

- RGB565 for opaque non-grayscale encoded sources;
- GRAY8 for structurally grayscale opaque encoded sources;
- ARGB4444 for opt-in alpha-bearing encoded sources;
- automatic transactional promotion to RGBA8888 at mutable/full-precision
  barriers.

All formats are internal, opt-in, `DEFAULT=DISABLED`; mutable targets stay RGBA8888. GPU-only backing, KTX2/compressed textures, cache/pressure
policy, mmap/VirtualAlloc, and Phase-4 lifecycle work are out of scope.

## Branch and working set

Branch:

    perf/image-opt-phase3-formats

Create it from the final accepted HEAD of `perf/image-opt-phase2-raster`.
Current accepted Phase-2 HEAD:

    86bfeafe388ce866236c3ae58eecb144664895e2

Record the actual parent SHA at bootstrap. Do not branch from master or rewrite
history. Phase 4 starts from the final Phase-3 HEAD; record its SHA in Phase-4 state.

Plan/support:

    .agent/plans/exec-plan-image-opt-phase3-formats.md
    .agent/state/image-opt-phase3-formats.md
    .agent/evidence/image-opt-phase3-formats.jsonl
    .agent/archive/image-opt-phase3-formats-history.md
    .agent/reports/image-opt-phase3-formats-editorial.md
    .agent/benchmarks/image-opt-phase3-formats/


Resume from state first; keep enough state to avoid rereading long logs.

## Execution rules

- Benchmarks: local macOS only, Release software Skia:

      -DCMAKE_BUILD_TYPE=Release
      -DTC_GRAPHICS_SOFTWARE=ON
      -DTC_RENDERER_SKIA=ON
      -DTC_WINDOWING_SDL=ON

- Build only SDK/macOS at milestone boundaries; run related native smokes there.

- Commit direct evidence, not builds/binaries/logs. New files <=20 KiB/~600
  lines; deterministically gzip oversized raw 200-sample CSVs.
- Follow `logical-commits`; never amend. Preserve unrelated changes.

## Experimental contract — freeze before implementation

Phase-1/2 lessons are mandatory. Before runtime changes:

1. create every Phase-3 workload, fixture, correctness oracle, counter field,
   deployment task, and both final combined tests;
2. dry-run every workload (3-5 samples) and fix batch sizes so every timed
   sample is >=30 ms;
3. keep full input/output hashing and quality calculations outside timing;
4. record full-content hashes for every encoded input before scenario settings
   can affect anything;
5. commit a deterministic Phase-2 true-base adapter under
   `.agent/benchmarks/image-opt-phase3-formats/true-base-harness/` that replays
   the final harness on the exact Phase-2 base using only benchmark shims for
   unavailable Phase-3 APIs/counters; Phase-2 settings must remain functional;
6. record an adapter digest over all adapted harness files;
7. commit harness/fixtures/adapter before Phase-3 runtime code.

After an S1 exists, do not silently alter fixture bytes, timed operations,
batching, hash semantics, or correctness logic. If one must change, commit the
benchmark correction first, reproduce the affected pre-implementation S1 with
the adapter/checkpoint, then continue. Never call a final-runtime default/off run
S1. Never change acceptance thresholds after seeing results without explicit
user direction.

### Scenario and measurement rules

For isolated features:

- S1 = exact runtime immediately before target implementation;
- S2 = post-implementation runtime, target explicitly disabled, all non-target
  optimizations disabled;
- S3 = same post runtime, only target enabled.

Use identical fixture bytes/build/machine/warmups/batch/operations. Initial run:
3 warmups + 60 samples, external RSS every 50 ms.

If elapsed CV >5% or S2 is near/beyond +5% median elapsed or peak RSS, rerun the
comparison with 200 samples. A 200-sample median regression >5% blocks closure.
A 200-sample peak-RSS delta >5% requires matched S1/S2 `vmmap -summary` + `ps`
RSS after the same checkpoints (normally samples 100 and 150):

- if live RSS, physical footprint, private writable/allocator memory, or growth
  confirms the regression, fix it;
- if matched captures do not reproduce it and show no progressive private-memory
  growth, preserve both results and mark the peak signal not confirmed.

This rule is fixed before execution; do not invent a post-result exception.

## Fixed format architecture

Internal enum only:

    RGBA8888
    RGB565
    GRAY8
    ARGB4444

`NativeImageBackingRecord` stores format, actual `rowBytes`, actual backing
bytes=`rowBytes*height`, dimensions, opacity/generation, and existing analysis
state. Remove format-sensitive `width*height*4` assumptions from native backing
accounting/readback/promotion.

Skia mapping:

- RGBA8888 -> `kRGBA_8888_SkColorType`;
- RGB565 -> `kRGB_565_SkColorType`, opaque alpha type;
- GRAY8 -> `kGray_8_SkColorType`, opaque alpha type;
- ARGB4444 -> pinned `kARGB_4444_SkColorType` with its supported premultiplied
  alpha representation.

Prefer Skia conversion/packing helpers for channel layout, endianness,
premultiplication, and rounding. Row scratch is allowed; a second full RGBA
raster is not. If pinned Skia cannot safely construct/sample/read a required
format, stop that format milestone; do not substitute another format.

### Selection precedence

Selection is structural and deterministic; no full-pixel grayscale scan:

1. structurally grayscale non-alpha + `STORAGE_GRAY8=ENABLED` -> GRAY8;
2. otherwise encoded source with no alpha/tRNS + `STORAGE_RGB565=ENABLED` -> RGB565;
3. alpha-bearing/tRNS + `STORAGE_ARGB4444=ENABLED` -> ARGB4444;
4. otherwise -> RGBA8888.

JPEG grayscale/color comes from decoder/header components; JPEG is opaque. PNG
color/grayscale and alpha/tRNS capability come from PNG structure. An RGB/RGBA
file whose values happen to be gray is not reclassified by scan. An alpha-bearing
PNG never selects RGB565 in Phase 3 even when decoded alpha happens to be 255;
selection must not depend on optional Phase-2 opacity metadata.

GRAY8 > RGB565 > ARGB4444 > RGBA8888 is fixed.

### Compact source and promotion rules

Compact formats are canonical only for immutable/source backings.

- Skia may draw/sample them directly.
- `getPixels()` expands to public ARGB32 without changing the source backing.
- Encoding/readback observers may convert row-wise without promotion.
- New mutable/materialized canonical destinations are RGBA8888 unless explicitly
  stated otherwise.
- `getGraphics()`/make-mutable and full-precision canonical barriers promote.
- No dithering feature in this phase.

Do not use `getGraphics()` merely to force compact benchmark materialization.
Add/reuse a package-private test-only non-mutating materialization/introspection
hook; no public API change.

Promotion must be generic and transactional from the first compact format:

1. allocate/populate replacement RGBA8888 completely;
2. expand the compact representation; do not re-decode and create another
   quantization path;
3. preserve/derive valid opacity/generation state;
4. replace canonical backing only after success;
5. release old backing exactly once after commit;
6. failure preserves the compact backing and allows retry.

Add failure injection after promotion allocation and before commit for all three
compact formats.

### Decode ownership

Each compact decode owns one final compact buffer. Decoder/backing-creation
failure or `setjmp`/`longjmp` after allocation must release it exactly once and
leave no half-installed backing. Use explicit heap/state ownership as learned in
Phase 2; do not depend on unsafe automatic pointers across longjmp.

Inject failure after final compact-buffer allocation for every applicable
JPEG/PNG path and prove failure, balanced live backing/bytes, and successful
retry.

## Counters/test introspection

Add internal/test-only evidence for:

- backing creations and live/total bytes by RGBA8888/RGB565/GRAY8/ARGB4444;
- compact direct-decode count/bytes by format;
- full temporary RGBA decode bytes (must be zero in enabled direct compact path);
- compact readback/encode count + peak row scratch;
- compact->RGBA promotion attempts/successes/failures/bytes;
- current backing-format accessor.

Counter reset/clear follows Phase 1: reset initializes/enables test accounting;
clear clears counters without changing the gate unexpectedly.

## Fixtures and quality oracles

Create before implementation:

- large opaque non-grayscale JPEG/PNG for RGB565;
- deterministic structurally grayscale JPEG (and PNG if useful) for GRAY8;
- deterministic alpha PNG with opaque/translucent/transparent pixels and RGB/A
  gradients for ARGB4444;
- fixed inputs for promotion and combined workloads.

Generated inputs must be deterministic, created before scenario configuration,
and hash-identical across scenarios.


Correctness uses full output outside timing; never sparse samples or encoded
length as checksum. RGB565 must expand exactly to an independent 565 reference,
alpha=255, with per-channel max error/RMSE reported versus RGBA8888. GRAY8 must
expand to R=G=B/A=255 and exact/reference decoder values; color sources must not
select it. ARGB4444 uses an independent packing/premultiplication reference,
reports alpha and black/white composite max error/RMSE, ignores hidden RGB at
alpha=0, and must not accumulate quantization across repeated observers/draws.
Halos, channel swaps, alpha inversion, or loss beyond the 4-bit model are bugs.

## Milestones

### 0 — Bootstrap and freeze contract

Create branch from exact Phase-2 final HEAD. Commit this plan and
state/evidence/archive/editorial skeletons. Record parent SHA and the acceptance
rules above before results exist.

    docs(image): add compact backing execplan

No build.

### 1 — Freeze complete benchmark harness

Before runtime code, create all isolated workloads, promotion tests,
RGB565+writePixels interaction, both final combined workloads, fixtures, quality
oracles, format/counter assertions, deployment tasks, and true-base adapter.

Dry-run all workloads and prove fixed input hashes, stable full-output hashes,
>=30 ms samples, analysis outside timing, and counters sufficient to prove
selection/bytes/no promotion/direct decode. The compact materialization helper
must not call `getGraphics()` on the source.

Commit:

    test(image): add compact backing benchmarks

Build SDK + macOS software Skia once. Capture and commit RGB565 S1 on exact
pre-RGB565 runtime:

    test(image): record rgb565 backing baseline

### 2 — Generic infrastructure + RGB565

Feature: `STORAGE_RGB565`.

Implement generic format metadata, owned compact backing creation,
rowBytes/accounting, compact observer conversion, counters, and generic
transactional promotion before making RGB565 selectable.

Eligible RGB565 paths:
- one final 565 buffer;
- JPEG/PNG rows write/convert directly to final storage;
- no full RGBA staging raster;
- adaptive JPEG tiers preserved;
- direct Skia draw/sample;
- observers expand without promotion;
- `getGraphics()` promotes exactly once.

Smokes: format/accounting, alpha-bearing rejection, reference quality,
observer non-promotion, mutation promotion, promotion failure/retry, decode
post-allocation failure/retry.


At milestone end build/test/smoke, then S2/S3 plus RGB565+writePixels. Require
no confirmed S2 regression, ~2 bytes/pixel, zero full-RGBA staging, quality and
ownership/promotion passes, and exact-output writePixels or conservative fallback.
Commit `perf(image): add rgb565 native backing` and
`test(image): record rgb565 backing results`. Capture GRAY8 S1 before edits.

### 3 — GRAY8

Feature: `STORAGE_GRAY8`.

Select only structurally grayscale non-alpha sources; GRAY8 precedes RGB565.
Decode directly to final one-byte storage where supported; no full RGBA/565
staging. Reuse generic observer/ownership/promotion infrastructure.

Smokes: GRAY8-vs-RGB565 precedence, color-source rejection, exact/reference
ARGB expansion, observer non-promotion, promotion/decode failure + retry.

Build/test/smoke then S2/S3. Acceptance: no confirmed disabled regression,
~1 byte/pixel actual storage, correct structural selection, quality pass, no
full-size conversion staging, same promotion/ownership guarantees.

Commit report, then capture/commit ARGB4444 S1 before edits.

### 4 — ARGB4444

Feature: `STORAGE_ARGB4444`.

Select only alpha-bearing/tRNS sources when enabled and higher-precedence
formats are structurally ineligible. Use pinned Skia-supported premultiplied
4444 semantics. Convert decoder rows directly to final 16-bit storage; no full
RGBA staging raster.

Smokes: representative alpha extremes/translucency, channel order, black/white
compositing, hidden RGB alpha=0 treatment, no repeated quantization, observer
non-promotion, mutation promotion, decode/promotion failure + retry, and full
selector matrix with all format toggles enabled.

Build/test/smoke then S2/S3. Acceptance: no confirmed disabled regression,
~2 bytes/pixel actual storage, quality/compositing oracle passes, no halo/channel
corruption, exact precedence, observer stays compact.

Commit implementation/report logically.

### 5 — Cross-format promotion/observer matrix

No new toggle. Generic promotion already exists; fix only real gaps found here.
Capture/commit promotion S1 before hardening edits.

For RGB565/GRAY8/ARGB4444 prove:
- repeated draw/getPixels/encoding keep format and promotion count zero;
- `getGraphics()` promotes once before mutation;
- promoted pixels equal expansion of compact representation;
- later mutations stay RGBA8888 with no re-promotion;
- full-precision canonical barriers promote;
- injected failure preserves compact backing and retry succeeds.

Benchmark promotion cost/transient memory separately; speedup is not expected.
Build/smoke then S2/S3.

    fix(image): preserve compact backing promotion semantics
    test(image): record compact promotion results

### 6 — Combined RGB565 + GRAY8 + ARGB4444, Phase-2 options disabled

Use one three-source workload in all scenarios:
- opaque non-grayscale -> RGB565 in S3;
- structural grayscale non-alpha -> GRAY8 in S3;
- alpha-bearing/translucent -> ARGB4444 in S3.

Do not mutate/call `getGraphics()` on compact sources. Draw to separate RGBA8888
targets; observer readback/encoding is allowed. Assert source formats stay compact
and promotion count stays zero.

Matrix:
- S1: exact Phase-2 base via adapter; all five Phase-2 raster options disabled;
- S2: final Phase-3 runtime; all Phase-2 options disabled; all compact formats
  disabled;
- S3: same final runtime; all Phase-2 options disabled;
  `STORAGE_RGB565=ENABLED`, `STORAGE_GRAY8=ENABLED`,
  `STORAGE_ARGB4444=ENABLED`.

Require identical input hashes and exact S1/S2 RGBA parity. S3 must pass each
format quality oracle and prove `GRAY8 > RGB565 > ARGB4444` selection for the
three fixtures. Record format/bytes, timing, RSS, full hashes/quality metrics,
direct-decode counters, and zero promotions. Apply the predefined 60→200/`vmmap`
gate when triggered.

    test(image): benchmark combined compact formats

### 7 — Combined compact formats with all Phase-2 options enabled

Use the same three-source compact workload and still prohibit promotion. Enable:

    DECODE_ZERO_COPY
    RASTER_OPACITY_METADATA
    RASTER_OPAQUE_WRITE_PIXELS
    RASTER_ROW_READBACK
    RASTER_DIRECT_COLOR_MATERIALIZATION
    STORAGE_RGB565
    STORAGE_GRAY8
    STORAGE_ARGB4444

Matrix:
- S1: exact Phase-2 base via adapter, all five Phase-2 options enabled;
- S2: final Phase-3 runtime, all five Phase-2 options enabled, all Phase-3
  compact formats disabled;
- S3: same final runtime, all five Phase-2 options enabled, RGB565+GRAY8+
  ARGB4444 enabled.

Adapter shims may cover only Phase-3 APIs/counters; Phase-2 toggles stay real.
Record applicable counters and explain zero hits without distorting the workload.

Require RGB565/GRAY8/ARGB4444 selection for the three fixtures, zero promotions,
no full RGBA staging, bounded row readback/encoding, and unchanged structural
precedence under Phase-2 opacity/zero-copy plumbing. RGB565 writePixels must be
exact or conservatively fall back; all three format quality/reference gates pass.

Use the same 60-sample and predefined 200/`vmmap` gates.

    test(image): benchmark full raster optimization stack

### 8 — Final validation and handoff

Run:
- focused `totalcross.ui.image.*` tests;
- SDK `dist -x test`;
- final Release macOS software-Skia build if source changed;
- relevant native smokes for decode/ownership/retry, alpha/color, geometry,
  presentation/frame, readback/encoding, modifier memory, compact selection,
  promotion, and both final three-format combined workloads.

Do not build Android/iOS/Windows/Linux/GPU.


Final summary records backing bytes, timing/RSS, quality, promotion cost, all
60/200/`vmmap` gates, both combined matrices, limitations, and disabled defaults.
Preserve historical evidence. The corrective RSS evidence and the synchronized
state/evidence/archive/editorial/Outcomes records are committed separately from
the benchmark artifacts. Phase 4 starts from the final HEAD of this branch.

## Validation and completion gate

At relevant milestone boundaries:

    cd TotalCrossSDK
    ./gradlew-agent test --tests 'totalcross.ui.image.*' --no-daemon --console=plain
    ./gradlew-agent dist -x test --no-daemon --console=plain

Use only the exact Release software-Skia `libtcvm.dylib` for native validation.

Phase 3 is complete only when:
- each isolated format and promotion has true S1/S2/S3 evidence;
- no harness correction invalidates a baseline;
- every S2 regression signal passes the frozen 60/200/`vmmap` gate;
- disabled paths preserve Phase-2 behavior;
- compact direct decode has no hidden full-RGBA staging raster;
- decode/promotion failure injection proves balanced ownership and retry;
- observers never promote; mutable/precision barriers always promote
  transactionally;
- selection/precedence and quality gates pass;
- both combined tests pass with zero compact-source promotions;
- final validation is green.

## Stop conditions

Stop the affected milestone instead of inventing an alternative if:
- pinned Skia lacks a required compact color type/safe raster API;
- compact sampling/readback cannot preserve required semantics;
- public ARGB32 readback cannot be correct;
- direct compact decode cannot guarantee exactly-once release;
- promotion cannot preserve the original backing on failure;
- ARGB4444 falls outside the accepted 4-bit premultiplied quality model;
- a fix requires public pixel-format API;
- a confirmed disabled regression remains after the predefined diagnostic.

Do not substitute another lossy/GPU format or begin Phase-4 work here.

## Recovery state

State always records active milestone, exact pre-item S1 runtime SHA/harness
digest, implementation SHA, S2/S3 status, last build/smoke, any 200-sample or
`vmmap` gate, and next exact command. Never implement a format before its S1 is
committed. If a measurement regime changes, preserve old artifacts and create a
clearly named corrective set.

## Progress

- [x] Bootstrap/freeze contract.
- [x] Freeze all workloads, fixtures, oracles, counters, and true-base adapter.
- [x] RGB565 S1 -> implementation -> S2/S3.
- [x] GRAY8 S1 -> implementation -> S2/S3.
- [x] ARGB4444 S1 -> implementation -> S2/S3.
- [x] Promotion S1 -> cross-format promotion/observer validation -> S2/S3.
- [x] Combined compact-format matrix with Phase-2 options disabled.
- [x] Combined compact-format matrix with all Phase-2 options enabled.
- [x] Corrective compact correctness matrix, row-allocation optimization, and 200-sample RSS gates.
- [x] Final validation/documentation/Phase-4 handoff.

## Fixed decisions

- Compact formats are immutable/source-first; mutable targets stay RGBA8888.
- Precedence is GRAY8 > RGB565 > ARGB4444 > RGBA8888; alpha-bearing/tRNS
  sources do not select RGB565.
- ARGB4444 is opt-in lossy premultiplied storage.
- Ownership/promotion and the benchmark/adapter/RSS rules above are fixed before
  runtime work; both final matrices enforce the exact
  `RGB565|RGB565|GRAY8|GRAY8|ARGB4444` order and prohibit compact-source
  promotion.

## Outcomes & Retrospective

The compact backing milestone delivered opt-in RGB565, GRAY8, and ARGB4444
source storage with deterministic structural precedence, exact observer parity,
transactional promotion, and direct decoder ownership. All disabled S2 controls
matched their final-harness S1 controls within the frozen regression gate. The
first GRAY8/ARGB4444 S1s used a shorter workload; those exact-base artifacts
remain preserved and matched final-harness corrective S1s are used for timing
comparisons after the workload was doubled to meet the 30 ms floor.

The implementation was largely delivered in one runtime slice before the
per-format S1 captures; the exact-base and matched-control artifacts are both
preserved rather than conflated. The final row-conversion correction writes
directly into the supplied buffer and keeps compact writePixels scratch bounded
to one width*4 row. The all-format smoke matrix now checks full pixel equality
through promotion, allocation-failure preservation/retry, exact RGB565/GRAY8
draw parity, and ARGB4444 translucent fallback.

The required 200-sample matched RSS gate passed for promotion (+0.3%),
combined-disabled (+2.4%), and full-stack combined-enabled (-3.2%). ARGB4444
reported a raw +9.7% peak-RSS signal; sample-100/150 vmmap and ps evidence is
preserved. Its S2 peak physical footprint was lower than S1 (81.3M vs 84.4M),
with the sampled RSS difference concentrated in allocator residency, so the
signal is recorded as unconfirmed rather than a confirmed live-footprint
regression. No Phase-4 work starts here.

The authoritative three-pair ARGB4444 recheck used a corrected final workload
against the exact true-base dylib in alternating order S1->S2, S2->S1, S1->S2.
S2 peak-RSS deltas were -0.7%, +7.1%, and -4.3%; matched S2 peak physical
footprint deltas were -4.9%, -2.6%, and +2.0%. Current physical/private
writable residency changed with run order and allocator/page state, so the
frozen >5% regression rule rejects a reproducible disabled-path regression.
Runtime source was not changed.
