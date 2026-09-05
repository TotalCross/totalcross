<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Phase 2: Optimize lossless raster image decode, opacity, draw, and readback

This ExecPlan follows `AGENTS.md`, `.agent/PLANS.md`,
`.agents/skills/logical-commits/SKILL.md`, and the benchmark protocol committed
by phase 1.

## Purpose / Big Picture

Implement the highest-return raster optimizations that do not intentionally
reduce image quality:

1. decode directly into the final native backing buffer;
2. make opacity a cached structural property;
3. restore the opaque 1:1 `writePixels()` fast path for `NativeImageBacking`;
4. remove avoidable full-image scratch buffers from readback and color
   materialization.

Every item remains independently switchable through
`ImageOptimizationSettings`. Every item receives the required three-scenario
benchmark before/after comparison.

This phase does not introduce RGB565, GRAY8, ARGB4444, GPU-only backing, KTX2,
compressed GPU textures, cache-budget policy, memory-pressure eviction, or mmap.

## Branch Contract

Use:

    perf/image-opt-phase2-raster

Create it from the final HEAD of:

    perf/image-opt-phase1-controls

Do not create it from `master`. Record the exact parent SHA in state.

Do not push, merge, rebase, amend, or rewrite history unless explicitly asked.

## Working Set and Resume Protocol

Plan:

    .agent/plans/exec-plan-image-opt-phase2-raster.md

Supporting files:

    .agent/state/image-opt-phase2-raster.md
    .agent/evidence/image-opt-phase2-raster.jsonl
    .agent/archive/image-opt-phase2-raster-history.md
    .agent/reports/image-opt-phase2-raster-editorial.md
    .agent/benchmarks/image-opt-phase2-raster/

On resume, read state first. Read the benchmark protocol only when preparing or
validating a scenario:

    .agent/design/image-optimization-benchmark-protocol.md

Primary source paths:

    TotalCrossVM/third_party/jpeg/JpegLoader.c
    TotalCrossVM/src/nm/ui/image_Image.c
    TotalCrossVM/src/nm/ui/skia/skia_image_backing.cpp
    TotalCrossVM/src/nm/ui/skia/skia_image_backing_internal.h
    TotalCrossVM/src/nm/ui/skia/skia_image_color.cpp
    TotalCrossVM/src/nm/ui/skia/skia_surface.cpp
    TotalCrossSDK/src/main/java/totalcross/ui/image/Image.java
    TotalCrossSDK/src/main/java/totalcross/ui/image/NativeImageBacking.java
    TotalCrossSDK/build.gradle
    TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/

Locate the current PNG decoder by symbol `pngLoad` and inspect only its
implementation and helper paths before editing it.

## Execution, Build, Artifact, and Commit Rules

All user constraints from phase 1 remain in force:

- benchmarks run locally on macOS;
- only SDK and macOS builds are allowed;
- builds only at related milestone boundaries;
- native smokes only at related milestone boundaries and final completion;
- new files <=20 KiB/~600 lines;
- do not refactor existing large files only for size;
- commit all direct plan/benchmark/report/evidence artifacts;
- do not commit builds, binaries, deploy outputs, or verbose logs;
- invoke the logical-commits skill for every commit;
- preserve unrelated local changes.

Use the phase-1 benchmark protocol exactly. Every S1/S2/S3 run must explicitly
disable every non-target optimization from this series.

Use macOS software Skia for every benchmark in this phase:

    -DTC_GRAPHICS_SOFTWARE=ON
    -DTC_RENDERER_SKIA=ON
    -DTC_WINDOWING_SDL=ON
    -DCMAKE_BUILD_TYPE=Release

## Current Architecture and Required Compatibility

The current JPEG Skia path allocates a complete native `Pixel*` raster, fills it
row by row, then calls `skia_image_backing_create_from_argb_pixels()`. That
function allocates another complete RGBA raster before
`SkImage::MakeRasterData()` assumes ownership. The enabled zero-copy path must
remove that second whole-image representation.

`NativeImageBackingRecord` currently stores an `SkImage` or raster `SkSurface`,
width/height, generation, and APPLY_COLOR2 analysis. It has no persistent
opacity classification.

`skia_surface.cpp` already contains a conservative legacy
`skia_canWritePixels()` path for legacy `SkBitmap` surfaces. Preserve its safety
conditions and general draw fallback.

`skia_image_backing.cpp` currently reads native images through full RGBA
vectors. The enabled row-readback path must bound scratch space by width rather
than image area.

All disabled paths must preserve the phase-1/master behavior for A/B testing.
Do not delete the old implementation while this experimental series depends on
S2.

## Benchmark Workloads

Before implementing the first item, create all four item benchmark workloads.
This satisfies “benchmark created before implementation” without forcing a new
build before every item.

Use the existing committed image fixtures when suitable. Reuse the 600x600 PNG
and the existing 512x512/1960x1960 JPEG fixtures used by the native-backing
smokes rather than adding duplicate binaries.

Create small benchmark apps/support rather than one >600-line app.

### Decode workload

Force a fresh encoded-source materialization per measured iteration; do not
allow decoded-backing reuse across samples. Measure PNG and JPEG separately.
Include the large JPEG so peak-memory differences are visible. Do not include
`getPixels()` in the timed decode section.

### Opacity workload

Cover:

- JPEG known opaque;
- PNG without alpha known opaque;
- PNG with alpha but all alpha=255;
- PNG containing actual translucent/transparent pixels.

Measure decode/classification time and any scan count/pixels. The benchmark
must prove that JPEG and alpha-free PNG need no post-decode full raster scan.

### Opaque draw workload

Draw a large opaque JPEG and opaque PNG repeatedly 1:1 at integer coordinates,
no alpha mask, no scaling, no rotation, identity matrix, and no active clip.
Also include one ineligible draw for each guard category to verify fallback
counting without timing those guards as the primary performance result.

### Readback/materialization workload

Cover:

- `getPixels()` on a large native-backed image;
- PNG/JPEG encoding or an equivalent full readback consumer already present;
- a native color operation that requires canonical materialization/readback,
  including APPLY_COLOR2 where the current capability still requires it.

Measure elapsed time, peak RSS, full scratch bytes, row scratch bytes, and output
parity.

## Plan of Work

### Milestone 0 — Bootstrap and commit the plan

Create `perf/image-opt-phase2-raster` from the exact final phase-1 branch HEAD.
Create state/evidence/archive/editorial skeletons and commit this plan.

Suggested commit:

    docs(image): add raster optimization execplan

No build in this milestone.

### Milestone 1 — Commit all phase-2 benchmark workloads and run zero-copy S1

Create the four workloads above before touching implementation code for any
phase-2 optimization. Add Gradle deployment tasks by extending the existing
Image macOS smoke registration pattern; do not introduce another deploy system.

Commit benchmark sources first:

    test(image): add raster optimization benchmarks

At milestone end, build SDK and macOS software Skia once. Run only the zero-copy
S1 scenario with:

    DECODE_ZERO_COPY = DISABLED

and every other series toggle disabled. Commit the exact S1 samples/report
metadata before implementing zero-copy.

Suggested evidence commit:

    test(image): record zero copy decode baseline

### Milestone 2 — Implement zero-copy PNG/JPEG decode

Feature:

    DECODE_ZERO_COPY

When disabled, execute the current decoder-to-Pixel-to-RGBA path unchanged.

When enabled on deployed Skia:

- allocate one final RGBA8888 output buffer;
- fill that buffer directly during JPEG/PNG decoding;
- transfer its ownership to `SkImage::MakeRasterData()` without copying;
- use one allocator/release contract consistently; do not pair `xmalloc` with
  `delete[]` or another mismatched release function;
- keep the decoder's one-row internal buffer; “zero-copy” here means no second
  full decoded raster, not literally zero temporary bytes;
- preserve hidden RGB in PNG alpha-zero pixels when supplied by the decoder;
- preserve adaptive JPEG denominator semantics and transactional promotion;
- on decode or backing creation failure, release the final buffer exactly once
  and leave no half-installed backing.

Implement a generic owned-pixel creation helper in the Skia backing layer so
phase 3 can extend it to other formats without another ownership redesign. In
this phase it supports only RGBA8888.

Add counters:

    zeroCopyDecodeCount
    copiedDecodeCount
    decodeCopiedBytes
    decodeFinalBufferBytes

Do not make zero-copy default-enabled.

Suggested implementation commit:

    perf(image): decode directly into native backing

At milestone end, build SDK and macOS software Skia, run focused Image tests and
decode/native-materialization smokes, then run zero-copy S2 and S3.

Acceptance:

- S2 has no confirmed >5% regression versus S1;
- S3 creates one full decoded raster instead of two full decoded rasters;
- decoded visible pixels match S2 exactly; preserve documented hidden-RGB
  semantics;
- large-image peak memory or measured temporary bytes improve;
- no decode retry/promotion regression.

Commit benchmark report:

    test(image): record zero copy decode results

Using the same post-zero-copy build with `DECODE_ZERO_COPY=DISABLED`, run and
commit the opacity S1 before editing opacity code.

### Milestone 3 — Implement structural opacity metadata

Feature:

    RASTER_OPACITY_METADATA

Add an internal opacity enum to `NativeImageBackingRecord`:

    UNKNOWN
    OPAQUE
    TRANSLUCENT

The only fast-path proof is `OPAQUE`. Never treat `UNKNOWN` as opaque.

When enabled:

- JPEG decode sets `OPAQUE` without scanning pixels.
- PNG with no alpha/tRNS sets `OPAQUE`.
- PNG with alpha determines `OPAQUE` versus `TRANSLUCENT` while decoding; do
  not perform a second full-image pass.
- backing snapshot preserves opacity.
- operations may preserve `OPAQUE` only when their implementation cannot
  introduce alpha. If there is doubt, set `UNKNOWN`; do not guess.
- crop/scale of an `OPAQUE` source preserve `OPAQUE`.
- rotation preserves `OPAQUE` only when source is opaque and the fill is known
  opaque for all newly exposed pixels; transparent fill yields `UNKNOWN`.
- alpha/fade/transparency-key operations that can introduce transparency yield
  `UNKNOWN` unless their exact parameters prove opacity.
- mutable surface writes invalidate a previously proven opacity unless the
  write operation itself proves the new state.

For an `UNKNOWN` immutable raster encountered by a future opaque fast path,
allow one cached fallback opacity scan per backing generation. Do not scan on
every draw.

Add counters:

    opacityKnownFromSource
    opacityDeterminedDuringDecode
    opacityFallbackScans
    opacityFallbackPixels

When the feature is disabled, keep the current alpha-type/opaque behavior.

Suggested implementation commit:

    perf(image): track native backing opacity

At milestone end build and run focused semantic smokes, then opacity S2/S3.
S3 acceptance requires no separate full scan for JPEG or alpha-free PNG and no
visible pixel change.

Commit report, then run and commit `RASTER_OPAQUE_WRITE_PIXELS` S1 from this
pre-writePixels commit.

### Milestone 4 — Restore runtime opaque `writePixels()` for native backing

Feature:

    RASTER_OPAQUE_WRITE_PIXELS

Implement a shared conservative raster-copy helper used by
`NativeImageBacking`/draw-plan execution. Do not duplicate divergent safety
logic.

The enabled helper may call `SkCanvas::writePixels()` only when all conditions
hold:

- `TC_GRAPHICS_SOFTWARE`;
- source backing is proven `OPAQUE`;
- effective alpha mask is exactly 255;
- source and destination dimensions are identical;
- no sampling/scaling/rotation/color filter or draw-plan color stage remains;
- destination coordinates are integer;
- canvas total matrix is identity;
- canvas save count indicates no active clip/save state, matching the existing
  legacy safety rule;
- full destination bounds lie inside the target;
- source pixels are CPU-accessible through `peekPixels()`/equivalent.

For this phase, support full-image 1:1 copies first. Then, within the same
milestone only after full-image validation, support crop/frame subsets by
creating a pixmap subset without copying. If the pinned Skia API cannot expose a
safe subset without allocation, leave subset draws on the normal path and
record that limitation; do not copy pixels just to reach the fast path.

Any failed eligibility check falls through to the existing Skia draw path.
Never change output merely to increase hit rate.

Add:

    writePixelsAttempts
    writePixelsHits
    writePixelsFallbacks
    writePixelsCopiedBytes

Suggested implementation commit:

    perf(image): restore opaque raster copy fast path

At milestone end build and run the opaque-draw smoke plus semantic image smokes,
then S2/S3.

Acceptance:

- S2 remains baseline-equivalent;
- S3 pixel output exactly matches S2;
- eligible workload records writePixels hits;
- ineligible guard cases record fallbacks;
- report the measured macOS software-renderer speedup without carrying forward
  the historical “10x” claim unless measured again.

Commit report, then run and commit row-readback S1 before touching readback code.

### Milestone 5 — Replace full-image scratch with row/block processing

Features:

    RASTER_ROW_READBACK
    RASTER_DIRECT_COLOR_MATERIALIZATION

Treat these as one implementation family but report their counters separately.

When disabled, preserve existing full-vector paths.

When enabled:

- `getPixels()`/ARGB readback uses at most one RGBA row scratch buffer and writes
  converted ARGB values directly to the destination Java/native output;
- raw RGBA readback writes directly to caller output when the Skia API can do
  so without another full temporary;
- encoding consumes rows/blocks rather than first constructing a second full
  image;
- color materialization allocates the required destination backing once and
  processes source rows/blocks directly into it;
- no operation allocates a whole-image scratch buffer unless the algorithm
  fundamentally requires one and the report names that exception;
- output destination memory is not counted as scratch.

Add:

    rowReadbackCount
    fullReadbackCount
    rowScratchPeakBytes
    fullScratchBytes
    directColorMaterializationCount

Preserve `getPixels()` public ARGB semantics and documented PNG hidden-RGB
behavior.

Suggested implementation commit:

    perf(image): bound native image scratch memory

At milestone end build, run focused readback/color/encoding smokes, then S2/S3.

Acceptance:

- S2 matches S1;
- S3 output parity passes;
- large-image scratch is O(width), not O(width*height), for covered paths;
- peak memory/report shows the measured effect;
- no ordinary direct-draw workload starts readback.

Commit report.

### Milestone 6 — Final phase validation and handoff

Run the relevant Image unit tests, SDK dist, macOS software Skia build if HEAD
changed after the last build, and the native Image smoke family covering decode,
modifier, native color, native geometry, presentation state, frame/fade, and
memory behavior.

Do not build other platforms.

Create a concise phase-level benchmark summary that references the four item
reports and states which toggles remain experimental/off by default.

Update state/evidence/archive/editorial and commit:

    docs(image): complete raster optimization phase

Record the exact HEAD to use as the phase-3 base.

## Validation and Acceptance

For milestone-close SDK validation:

    cd TotalCrossSDK
    ./gradlew-agent test --tests 'totalcross.ui.image.*' \
      --no-daemon --console=plain
    ./gradlew-agent dist -x test --no-daemon --console=plain

For macOS native validation use only Release software Skia and the exact
built `libtcvm.dylib`.

Before every commit run header validation and `git diff --check --cached`.

The phase is complete only when all four item reports contain S1/S2/S3 data and
no confirmed disabled-path regression remains.

## Risks and Stop Conditions

Stop the affected slice rather than redesigning semantics if:

- zero-copy ownership cannot guarantee exactly-once release;
- PNG hidden RGB would be canonicalized or lost unexpectedly;
- writePixels cannot respect matrix/clip/bounds semantics;
- row processing changes ARGB or alpha behavior;
- a change requires a public API contract.

Do not substitute lossy formats for a failed lossless optimization.

## Idempotence and Recovery

Benchmark result filenames are tied to item/scenario and commit SHA recorded
inside each file/report. Do not rerun completed scenarios unless source, build
revision, or measurement regime changed.

On interruption, state must name:

- active item;
- whether its S1 is already committed;
- implementation commit if any;
- last completed build;
- S2/S3 status;
- next exact command.

## Progress

- [x] Bootstrap phase-2 branch and plan.
- [x] Commit all benchmark workloads; capture zero-copy S1.
- [x] Implement/report zero-copy; capture opacity S1.
- [x] Implement/report opacity metadata; capture opacity S2/S3.
- [x] Capture writePixels S1.
- [x] Implement/report writePixels; capture writePixels S2/S3.
- [ ] Capture row-readback S1.
- [ ] Implement/report row/block readback and color materialization.
- [ ] Final validation and phase-3 handoff.

## Decision Log

- Decision: preserve old paths behind `DISABLED` for controlled A/B.
  Rationale: the user requires post-implementation disabled benchmarks.
  Date: 2026-09-05.

- Decision: opacity is proof metadata, not a heuristic.
  Rationale: writePixels must never be selected from uncertain alpha state.
  Date: 2026-09-05.

- Decision: writePixels is software-renderer-only in this phase.
  Rationale: GPU optimization is a later lifecycle concern.
  Date: 2026-09-05.

- Decision: “zero-copy” removes the extra full decoded raster; row decoder
  scratch remains allowed.
  Rationale: avoid misleading terminology and preserve decoder design.
  Date: 2026-09-05.

## Outcomes & Retrospective

Update only at milestone boundaries with measured facts and committed evidence.

## Revision Note

Initial post-native-backing raster optimization plan. Lossy/compact formats are
deliberately deferred to phase 3.
