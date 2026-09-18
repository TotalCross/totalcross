<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Plan 1 of 2 — instrument image-scroll writePixels and JPEG diagnostics

This ExecPlan follows `AGENTS.md`, the ExecPlan rules in
`TotalCross/totalcross-depot-tools:.agent/PLANS.md`, and
`.agents/skills/logical-commits/SKILL.md`.

Execute only on branch `perf/image-decode-distributed-benchmark`. Do not create,
switch, rebase, reset, amend, or rewrite branches or commits.

## Purpose / Big Picture

Make the existing real 663-image scroll benchmark explain two unknowns without
changing optimization policy:

1. why `RASTER_OPAQUE_WRITE_PIXELS` has attempts but no hits; and
2. how much native JPEG decode time occurs during scroll, including the actual
   JPEG tier used (`1/1`, `1/2`, `1/4`, `1/8`).

This plan ends when the diagnostics compile, are emitted by the benchmark, and
pass focused macOS native smokes. It must not change the writePixels policy,
physical-variant cache promotion, JPEG tier selection, resampling behavior, or
rendered result. Plan 2 runs the full macOS benchmark.

## Working Set and Resume Protocol

Save this plan at:

- `.agent/plans/image-scroll-diagnostics-macos-01-instrumentation.md`

Create and commit:

- `.agent/state/image-scroll-diagnostics-macos-01-instrumentation.md`
- `.agent/evidence/image-scroll-diagnostics-macos-01-instrumentation.md`
- `.agent/reports/image-scroll-diagnostics-macos-01-instrumentation-editorial.md`

On resume, read the state file first, then the active milestone below and only
the source paths named by state. Read evidence only for a specific prior result.
Do not routinely reread completed image benchmark plans.

Primary paths:

- `TotalCrossVM/src/nm/ui/skia/skia_image_backing.cpp`
- the existing Skia backing accounting declaration header used by that file
- `TotalCrossVM/src/nm/ui/ImageTestAccounting_c.h`
- `TotalCrossVM/third_party/jpeg/JpegLoader.c`
- `TotalCrossSDK/src/main/java/totalcross/ui/image/Image.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/image/NativeImageBacking.java`
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageScrollRealWorkloadBenchmarkApp.java`
- `scripts/run-image-scroll-distributed-benchmark.py`
- `scripts/README-image-benchmarks.md` only for schema documentation

Read only when needed:

- `TotalCrossVM/src/nm/ui/GraphicsPrimitivesSkia_c.h`: deployed `drawSurface()`.
- `TotalCrossVM/src/nm/ui/skia/skia_surface.cpp`: Skia clip save/restore.
- `TotalCrossVM/src/tests/ir/tcir_jit_benchmark.c`: existing cross-platform
  monotonic nanosecond timer pattern.
- `scripts/package-image-scroll-benchmark.sh`: macOS bundle packaging.

## Global Constraints

Operate economically with tokens and tool output.

- Prefer `rg`, narrow line ranges, scoped `git status`, and scoped `git diff`.
- Preserve unrelated local changes; stage only task paths.
- Do not build Android, iOS, Windows, Linux, or unrelated targets.
- Build operations are allowed only for the SDK and macOS native runtime, and
  only at the end of a related milestone.
- Native smoke tests are allowed only at milestone gates and final validation.
- Between logical implementation commits inside a milestone, use only static
  checks; explicitly defer SDK/native build validation to the milestone gate.
- Redirect verbose build/smoke output outside tracked source and print concise
  summaries.
- Every repository artifact deliberately created by this plan must be committed.
  Build outputs and logs are operational outputs and are not committed.
- Every new tracked file must remain <= 20 KiB and approximately <= 600 lines.
  Existing large files may be edited in place and must not be refactored only
  to reduce size.
- If another new tracked file would exceed those limits, stop and create a
  follow-on ExecPlan.
- Do not push.

Bootstrap check:

    test "$(git branch --show-current)" = "perf/image-decode-distributed-benchmark"
    git rev-parse HEAD

Record starting HEAD and any deliberate pre-existing task-path changes in state.

## Progress

- [x] Bootstrap plan/state/evidence and commit the execution contract.
- [x] Milestone 1: add native writePixels and JPEG diagnostic accounting.
- [x] Milestone 2: expose phase/frame diagnostics through the scroll benchmark.
- [x] Close Plan 1 and leave Plan 2 as the only next action.

## Current Architecture and Fixed Decisions

### writePixels

`RASTER_OPAQUE_WRITE_PIXELS` is bit 2 (`mask 4`). Current
`tryWritePixelsImage()` / `tryWritePixels()` require, among other conditions:

- valid target/source and `alphaMask == 255`;
- identity total matrix;
- `targetCanvas->getSaveCount() == 1`;
- full source rectangle;
- equal source/destination dimensions;
- integral destination origin;
- destination inside target;
- opaque/readable source;
- successful `writePixels()`.

The deployed `drawSurface()` path applies `skia_setClip()` before the native
backing draw. The clip saves the canvas, so `saveCount == 1` is expected to
reject the clipped `ImageControl` path. On macOS, content scale can also make
the canvas matrix non-identity even for a physically one-to-one draw.

Measure these facts. Do not relax them in this plan.

### JPEG decode

`JpegLoader.c` already knows the requested mode and actual
`cinfo.scale_num/scale_denom` after header parsing and scale selection. Measure
there rather than inferring the tier from requested dimensions.

The existing benchmark resets image accounting immediately before the measured
cold pass. With prefetch on, it captures prefetch accounting before that reset.
Preserve this separation.

Do not change the standalone 90-process decode matrix in this plan.

## Diagnostic Contract A — writePixels rejection and opportunity counters

Keep existing attempts/hits/fallbacks/copied-byte counters.

Add these structural rejection counters. Evaluate them independently when the
state is cheap to inspect:

- `writePixelsRejectInvalidTargetOrSource`
- `writePixelsRejectAlphaMask`
- `writePixelsRejectMatrix`
- `writePixelsRejectSaveCount`
- `writePixelsRejectSourceRect`
- `writePixelsRejectSizeMismatch`
- `writePixelsRejectFractionalDestination`
- `writePixelsRejectDestinationBounds`

Add these downstream reasons only when the existing execution actually reaches
the corresponding gate. Do not perform extra opacity scans/readbacks merely for
diagnostics:

- `writePixelsRejectOpacity`
- `writePixelsRejectSourcePixels`
- `writePixelsRejectWriteFailure`

Structural rejection counters are intentionally non-exclusive. Their sum must
not be compared with fallback count.

Always preserve:

    writePixelsAttempts == writePixelsHits + writePixelsFallbacks

Add two observational candidate counters:

- `writePixelsDeviceOneToOneCandidates`
- `writePixelsDeviceOneToOneKnownOpaqueCandidates`

A device-one-to-one candidate requires:

1. valid target/source and `alphaMask == 255`;
2. total matrix is positive scale+translate only: no skew, rotation, perspective;
3. mapped destination is an axis-aligned device rectangle with integral bounds;
4. intersection with device clip is non-empty;
5. the corresponding source subset maps exactly 1:1 in physical pixels with
   integral source bounds and no resampling.

The first candidate ignores unresolved opacity. The known-opaque counter also
requires opacity already known without scanning.

Do not call `proveOpaqueForWritePixels()` only for the candidate diagnostic.
Do not call `writePixels()` from the candidate probe. Do not change fallback
control flow.

Gate all new accounting with the existing backing/test accounting enablement.

## Diagnostic Contract B — native JPEG timing and actual tier

Instrument successful JPEG decode in `JpegLoader.c`, where the effective scale
is known.

Use the repository's existing nanosecond timing strategy from
`TotalCrossVM/src/tests/ir/tcir_jit_benchmark.c`:

- Windows: `QueryPerformanceCounter`;
- Apple: `mach_continuous_time` + `mach_timebase_info`;
- other POSIX: `clock_gettime(CLOCK_MONOTONIC_RAW, ...)`.

Do not use millisecond `getTimeStamp()`.

Define `jpegNativeDecodeNs` as:

- start: after `jpeg_calc_output_dimensions()` and immediately before output
  allocation/materialization for the successful decode;
- end: immediately before successful loader return after decoded output/backing
  has been produced.

This intentionally includes allocation, libjpeg decompression, pixel/storage
conversion, and native backing creation/adoption inside that successful loader
path. It excludes encoded-byte acquisition outside the loader. Do not describe
it as pure IDCT time.

Record:

- successful JPEG decode total count and total ns;
- successful count and ns by actual denominator:
  - `1`: full;
  - `2`: half;
  - `4`: quarter;
  - `8`: eighth;
  - `other`: defensive bucket;
- successful count by requested mode:
  - full;
  - target;
  - explicit-ratio;
  - best-fit;
- JPEG decode failure count.

Use the actual `cinfo.scale_denom`; do not infer it from dimensions.

Extend `ImageTestAccounting_c.h` with `long` support using
`getStaticFieldLong`, without changing existing integer accounting behavior.
Add/reset corresponding Java fields in `Image.java`.

When diagnostic accounting is disabled, normal applications must not call the
timer or update these counters. Do not log per decode and do not allocate a
diagnostic object per decode.

## Diagnostic Contract C — scroll benchmark output

Extend existing `Counters`/snapshot logic; do not create a second accounting
framework.

For prefetch-on, capture JPEG diagnostics at prefetch completion before the
existing accounting reset. Store them separately from measured scroll.

For every measured frame, snapshot JPEG counters before/after the frame and
record deltas using primitive fields.

Append these columns to `frames.csv`:

- `jpeg_decode_count`, `jpeg_decode_ns`
- `jpeg_full_count`, `jpeg_full_ns`
- `jpeg_half_count`, `jpeg_half_ns`
- `jpeg_quarter_count`, `jpeg_quarter_ns`
- `jpeg_eighth_count`, `jpeg_eighth_ns`
- `jpeg_other_count`, `jpeg_other_ns`

Do not add per-frame writePixels rejection columns.

Extend `counters.json` with:

- every new writePixels rejection/candidate counter;
- `jpegDecode.prefetch`;
- `jpegDecode.scroll`;
- denominator count/ns buckets and mode counts.

Keep existing top-level counters for compatibility.

Extend aggregated `summary.csv` only with:

- `jpeg_decode_count`
- `jpeg_decode_ns`
- `jpeg_full_count`
- `jpeg_half_count`
- `jpeg_quarter_count`
- `jpeg_eighth_count`
- `write_pixels_attempts`
- `write_pixels_hits`
- `write_pixels_fallbacks`
- `write_pixels_device_1to1_candidates`
- `write_pixels_device_1to1_known_opaque_candidates`
- `write_pixels_reject_matrix`
- `write_pixels_reject_save_count`
- `write_pixels_reject_size_mismatch`

Detailed rejection reasons remain in `counters.json`.

Correct feature status for the four attempt-based features:

- `DISABLED`: bit off;
- `NOT_REACHED`: enabled and activity/attempt count zero;
- `ATTEMPTED_NO_HIT`: activity/attempt count nonzero and hits zero;
- `EXERCISED`: hits nonzero.

Map explicit activity counters for:

- `RASTER_OPAQUE_WRITE_PIXELS`;
- `RASTER_TARGET_COLORTYPE_CONVERSION`;
- `RASTER_PHYSICAL_VARIANT_CACHE`;
- `RASTER_PHYSICAL_IDENTITY_FOLDING`.

Do not redesign status semantics for unrelated features in this plan.

## Diagnostic Contract D — invariants

Benchmark output must reject internally inconsistent diagnostics:

- attempts = hits + fallbacks for writePixels;
- candidate counters <= attempts;
- JPEG total count = full+half+quarter+eighth+other counts;
- JPEG total ns = sum of denominator ns buckets;
- frame count/ns values are non-negative;
- completed measured-frame JPEG deltas sum to measured-scroll JPEG aggregate;
- prefetch counters are excluded from post-reset scroll counters;
- requested mask = effective mask.

Do not make half dominance a hard assertion. Report actual tiers.

## Plan of Work

### Bootstrap checkpoint

Create plan/state/evidence files, record starting HEAD and scoped local changes,
validate new-file size limits, and commit only those artifacts.

Suggested commit:

    docs(benchmark): plan macos image diagnostics

No build or native smoke is allowed here.

### Milestone 1 — native diagnostic accounting

Slice 1A: implement Contract A in the existing Skia backing accounting path.
Use small local helpers only if they materially reduce duplicated condition
classification. Do not broadly refactor the backing manager.

Expose/reset getters through the existing `NativeImageBacking` accounting bridge.

Suggested commit:

    perf(vm): diagnose opaque write pixel fallbacks

Before commit: copyright/header validation and `git diff --check --cached`.
Defer SDK/native build to Milestone 1 gate.

Slice 1B: implement Contract B in `ImageTestAccounting_c.h`, `Image.java`, and
`JpegLoader.c`.

Suggested commit:

    perf(vm): measure jpeg decode tiers

Again, before commit use only static/header/whitespace checks.

Milestone 1 gate — builds are now allowed:

1. Run the smallest focused SDK image tests needed to compile the changed Java
   accounting contract.
2. Build SDK with `TotalCrossSDK/gradlew-agent`; save verbose logs.
3. Build only macOS ARM64 `tcvm` and `Launcher`:

       cmake -S TotalCrossVM -B build/image-scroll-diagnostics-macos \
         -DCMAKE_BUILD_TYPE=Release \
         -DCMAKE_OSX_ARCHITECTURES=arm64 \
         -G Ninja
       cmake --build build/image-scroll-diagnostics-macos \
         --target tcvm Launcher --parallel

4. Build no other platform.
5. Validate changed-file headers, whitespace, and all new-file size limits.
6. Record concise gate results/log paths in evidence/state and commit those
   plan artifacts if changed.

If the gate fails because of this milestone, fix only the accounting contract
in a focused follow-up commit and rerun the gate.

### Milestone 2 — benchmark phase/frame integration

Slice 2A: extend `ImageScrollRealWorkloadBenchmarkApp` per Contracts C/D:

- prefetch JPEG snapshot before reset;
- scroll aggregate snapshot;
- per-frame JPEG deltas;
- detailed run-level writePixels counters;
- four-feature status correction;
- consistency checks.

Suggested commit:

    perf(benchmark): record scroll decode diagnostics

Slice 2B: extend `scripts/run-image-scroll-distributed-benchmark.py` to carry
the compact fields into `summary.csv`. Preserve the 126-process plan, seed,
rounds, baseline semantics, process isolation, and fail-fast behavior.

Update benchmark README only for new fields/status names. If a parser fixture is
needed, keep it small and deterministic.

Suggested commit:

    test(benchmark): validate diagnostic result schema

No SDK/native build between Slice 2A and 2B.

Milestone 2 gate — builds and macOS smokes are now allowed:

1. Static tooling checks:

       bash -n scripts/package-image-scroll-benchmark.sh
       PYTHONDONTWRITEBYTECODE=1 python3 -m py_compile \
         scripts/run-image-scroll-distributed-benchmark.py

2. Run smallest focused SDK tests.
3. Build SDK.
4. Rebuild only macOS ARM64 `tcvm` and `Launcher`.
5. Assemble a local macOS-only SDK package using the same layout expected by
   `.github/workflows/package.yml`.
6. Package one macOS benchmark bundle with
   `scripts/package-image-scroll-benchmark.sh`.
7. Run benchmark self-test and existing four scroll smokes:
   `0/off`, `0/on`, `32799/off`, `32799/on`.
8. Run one additional `mask=4/off` and one `mask=4/on` process if the existing
   smoke phase does not already execute mask 4.
9. Require:
   - mask 4 has nonzero writePixels attempts;
   - new reject/candidate counters parse;
   - attempts = hits + fallbacks;
   - attempts > 0 and hits = 0 yields `ATTEMPTED_NO_HIT`;
   - JPEG denominator count/ns invariants pass;
   - per-frame decode sums match scroll totals;
   - prefetch and scroll decode sections remain distinct.

Zero or nonzero mask-4 hits are both acceptable before policy changes. If hits
appear, record the discovery and continue when invariants pass.

Commit milestone evidence/state.

## Validation and Acceptance

Before each logical commit:

    python3 scripts/validate-copyright-headers.sh --files <changed files>
    git diff --check --cached

For every new tracked file:

    wc -c <file>
    wc -l <file>

Require <= 20480 bytes and approximately <= 600 lines.

Plan 1 completes only when:

- runtime diagnostic accounting is gated and policy-neutral;
- SDK build passes;
- macOS ARM64 `tcvm` and `Launcher` build pass;
- benchmark schema parses;
- macOS native scroll smokes pass;
- mask 4 exposes exact rejection/candidate counters;
- actual JPEG denominator and native decode ns are present per phase/frame;
- no other platform was built;
- all plan artifacts are committed.

## Risks and Discoveries Policy

Known facts to preserve:

- structural writePixels reject counters overlap;
- device-one-to-one candidates are policy opportunities, not proof that direct
  write is safe without explicit device clipping;
- the JPEG timer includes allocation/storage/backing work by contract;
- half decode is expected to dominate macOS from prior geometry but must be
  measured, not assumed.

Do not optimize based on smoke results. Record surprises in state/evidence and
leave policy changes for a later plan.

## Idempotence and Recovery

Use task-specific build/package directories. Do not delete unrelated caches,
logs, benchmark outputs, or local changes.

New counters must reset to zero through the existing benchmark reset path.
Rerunning a smoke must produce fresh output under the existing runner contract.

State is the first resume read. Evidence is append-only and compact. Do not
amend commits. Use a focused follow-up commit for validation fixes.

## Logical Commits

Follow `.agents/skills/logical-commits/SKILL.md`. Expected commit family:

- `docs(benchmark): plan macos image diagnostics`
- `perf(vm): diagnose opaque write pixel fallbacks`
- `perf(vm): measure jpeg decode tiers`
- `perf(benchmark): record scroll decode diagnostics`
- `test(benchmark): validate diagnostic result schema`

Non-trivial commit bodies must state why, behavior/compatibility impact,
focused validation, and milestone builds still deferred when applicable.

## Outcomes & Retrospective

Completed factual outcomes:

- Delivered gated writePixels rejection/candidate accounting, native JPEG
  denominator timing/tier accounting, prefetch-versus-scroll separation,
  per-frame JPEG deltas, compact distributed summary fields, and explicit
  attempt-feature statuses.
- Milestone 1 passed focused SDK tests, SDK distribution packaging, and the
  macOS ARM64 `tcvm`/`Launcher` build. Milestone 2 passed the static checks,
  rebuilt SDK/native/package gate, bundle self-test, four prescribed smokes,
  and additional mask-4 `off`/`on` smokes. Mask 4 reported nonzero attempts,
  zero hits, `ATTEMPTED_NO_HIT`, and valid candidate/rejection counters.
- Exact task commits: `73519be8b`, `621cf45d4`, `12490a1ab`, `7e1914b29`,
  `0563e08b8`, `b45df2945`, `49cbc7bdc`, `4d781d5a2`, `72fe224f2`,
  `a6a5255db`, and `f803d4a4d`.
- The first Milestone 2 smoke exposed the VM's 32-character native resolver
  limit and a candidate-name truncation collision. A focused bridge-name fix
  was committed and the complete macOS gate was rerun successfully. A first
  package attempt also ran out of disk space; after task-output cleanup and
  user-provided disk space, the macOS package and smokes passed.
- No optimization policy, JPEG tier selection, rendering policy, or cache
  behavior changed.

Then set the next action to Plan 2:
`.agent/plans/image-scroll-diagnostics-macos-02-benchmark.md`.

Plan 1 is closed. The next action is Plan 2:
`.agent/plans/image-scroll-diagnostics-macos-02-benchmark.md`.

## Revision Note

Initial Plan 1: implement and smoke-validate diagnostic-only writePixels and JPEG
instrumentation on `perf/image-decode-distributed-benchmark`.
