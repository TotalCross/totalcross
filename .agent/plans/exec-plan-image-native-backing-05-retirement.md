<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda
SPDX-License-Identifier: LGPL-2.1-only
-->

# Retire Image pixel-array storage and finish compatibility barriers

This ExecPlan follows `AGENTS.md`, `.agent/PLANS.md`, the repository
`logical-commits` skill, and
`.agent/image-native-backing-roadmap.md`. Execute it only after plans 1-4 pass.

## Purpose / Big Picture

Remove `Image`'s canonical `int[] pixels` and `pixelsOfAllFrames` storage. Route
readback, encoding, equality, hashing, frame state, lock/free lifecycle, and
non-Skia compatibility through explicit backings. Finish the ABI slot transition
and prove the complete native Skia flow with final SDK/macOS validation.

## Working Set and Resume Protocol

Use `.agent/state/image-native-backing-05.md` first and compact evidence
`.agent/evidence/image-native-backing-05.jsonl`. Consult earlier plans only for a
specific unresolved item named by state. The roadmap is the architecture source
of truth.

New files remain below 20 KB/~600 lines. Existing oversized files must not be
split merely to satisfy this rule.

## Progress

- [ ] Milestone 1: migrate readback, encoding, equality/hash, and frame access.
- [ ] Milestone 2: retire Image pixel fields and legacy native pixel macros.
- [ ] Milestone 3: run final compatibility/smoke gate and reconcile documentation.
- [ ] Complete the full five-plan sequence.

## Current Architecture and Scope

Plans 1-4 provide native backing for normal deployed Skia content and execute all
pipeline operations without relying on Image pixel arrays. Remaining arrays are
legacy/transitional storage and compatibility assumptions.

The final design keeps `RasterImageBacking` for Java SE/non-Skia paths and uses
`NativeImageBacking` on deployed Skia. The `Image` object itself no longer owns
full-size pixel arrays.

## Plan of Work

### Milestone 1: Route pixel-observing APIs through backing

Migrate each API deliberately; do not perform a blind search/replace.

`getPixelRow` and Graphics RGB/pixel reads:

- native backing reads directly from the backing canvas/image with explicit ARGB
  conversion;
- use bounded row/region temporary buffers;
- Java SE reads `RasterImageBacking`;
- no API allocates a full Java raster unless its documented return type requires
  one.

`createPng`, `createJpg`, PDB output, and related encoders:

- materialize the semantic pipeline to native backing first on deployed Skia;
- preserve existing encoding format, PNG comment/frame metadata, alpha, and
  output API behavior;
- feed existing encoders from backing row/region readback rather than replacing
  encoding format in this refactor;
- a rotate/color/crop pipeline saved as PNG must encode the transformed result.

`equals`:

- preserve dimension checks and alpha-ignored comparison;
- compare bounded native rows/tiles on deployed Skia;
- do not create two full Java arrays.

`hashCode`:

- preserve cached hash behavior and `Arrays.hashCode`-equivalent result for the
  legacy pixel sequence;
- preserve the existing >4096-pixel 64x64 scaling optimization;
- implement the hash over native row/temporary data or a bounded 64x64 native
  result, not a full Java raster.

`getPixels`:

- Java SE returns the live `RasterImageBacking.pixels` array exactly as the
  documented desktop behavior;
- deployed Skia materializes and returns a detached snapshot array from native
  readback; do not attach it as canonical backing and do not make native
  correctness depend on later mutations of that unsupported device snapshot.

`setCurrentFrame`/next/prev:

- native backing only updates normalized frame metadata/source selection;
- remove visible-frame copying from native Skia path;
- raster fallback may retain its copy behavior inside `RasterImageBacking` if
  required by Java SE compatibility.

Commit checkpoint A for readback/encoding. Checkpoint B for equals/hash/getPixels.
Checkpoint C for frame cleanup if separate.

Milestone gate:

    cmake -S TotalCrossVM -B build-image-backing-05-m1 \
      -DCMAKE_BUILD_TYPE=Release \
      -DCMAKE_OSX_ARCHITECTURES=arm64 -G Ninja
    cmake --build build-image-backing-05-m1 --target tcvm Launcher --parallel

    cd TotalCrossSDK
    ./gradlew-agent <focused image readback/encoding/equality tests>
    ./gradlew-agent dist -x test

Run native smoke only after the gate. It must include decode -> rotate ->
applyColor -> save PNG and compare dimensions/selected pixels, plus generated
image draw -> save.

Acceptance:

- all pixel-observing APIs work from backing;
- encoded output contains pending semantic operations;
- no full Java raster is used except explicit deployed `getPixels()` snapshot;
- SDK/macOS gate and smoke pass.

### Milestone 2: Remove Image pixel fields and atomically update ABI macros

Before editing field slots, run a focused inventory once:

    rg -n 'Image_pixels|Image_pixelsOfAllFrames|\.pixels\b|pixelsOfAllFrames' \
      TotalCrossSDK/src/main/java/totalcross/ui/image \
      TotalCrossVM/src/nm/ui TotalCrossVM/src/nm/instancefields.h

Classify remaining hits as:

- Image legacy field/direct use to remove;
- RasterImageBacking legitimate use;
- local temporary buffer names that do not imply Image storage;
- Java SE loader/algorithm use that must be redirected to RasterImageBacking.

Do not repeatedly rerun broad searches after every edit; record the inventory in
state and re-run once at milestone end.

Perform the final object-slot transition atomically:

    old slot 0 pixels            -> ImageBacking backing
    old slot 1 pixelsOfAllFrames -> reservedLegacyPixelsOfAllFrames
    slots 2..8                   -> retain existing order/meaning

Remove the transitional backing field that plan 1 appended after the existing
object fields. Keep slot 1 as a null reserved Object. Do not compact it.

Update `instancefields.h` in the same commit so:

- `Image_backing(o)` addresses object slot 0;
- no `Image_pixels` or `Image_pixelsOfAllFrames` macros remain;
- `RasterImageBacking` has explicit native macros for fallback pixel arrays;
- `NativeImageBacking` has its handle macro;
- all existing Image int/double indexes remain unchanged.

Update native fallback algorithms that are still required outside Skia to accept
or fetch `RasterImageBacking` rather than reading arrays from Image directly.
Do not run those forbidden platform builds; source compatibility/static
inspection is the only validation allowed for those branches in this series.

Remove `materializeCanonicalChecked`/related correctness dependence on
`synchronized`. The method may remain unsynchronized; Image is not being made
thread-safe in this work. Test hooks may retain Java SE synchronization only if
clearly test-only and not used for device correctness.

Finish lifecycle semantics:

- `getGraphics()` checks explicit lock/backing state, never pixels;
- `lockChanges()` materializes native state and marks graphics locked; it does not
  discard canonical backing;
- `applyChanges()` updates/flushes native mutable backing as needed;
- `freeTexture()` releases only transient cache/GPU state;
- backing finalization owns canonical native destruction;
- `Image.finalize()` must not double-free shared backing.

Remove transitional dual-storage synchronization helpers from plan 1.

Commit checkpoint D for Java field/backing transition plus `instancefields.h` and
all native slot consumers; this is one ABI-sensitive atomic commit. Do not split
the slot meaning change across commits. Commit checkpoint E for lifecycle cleanup
if it can follow after D without leaving D uncompilable.

Milestone gate — required ABI gate:

    cmake -S TotalCrossVM -B build-image-backing-05-m2 \
      -DCMAKE_BUILD_TYPE=Release \
      -DCMAKE_OSX_ARCHITECTURES=arm64 -G Ninja
    cmake --build build-image-backing-05-m2 --target tcvm Launcher --parallel

    cd TotalCrossSDK
    ./gradlew-agent <focused full Image suite>
    ./gradlew-agent dist -x test

Run native smoke only after the gate, covering decode, generated surface,
Graphics drawing, scale/rotate, color mutation, readback, and save.

At the milestone end rerun the focused inventory. Acceptance requires:

- no `int[] pixels` or `pixelsOfAllFrames` field in `Image`;
- no native `Image_pixels`/`Image_pixelsOfAllFrames` macro or direct use;
- direct raster arrays exist only inside `RasterImageBacking`/Java SE-local
  compatibility code;
- Image object slots 2..8 and int/double indexes remain stable;
- ABI macOS build/smoke and SDK gate pass.

### Milestone 3: Final regression and resource-behavior gate

Create or update one compact native smoke suite rather than many duplicate apps.
Keep every new test/support file under the size limit. Reuse the existing
issue-417 smoke where practical.

The final native smoke must prove, on the same revision:

1. decoded PNG draws with correct alpha and selected pixels;
2. targeted JPEG smooth-scale produces expected target dimensions;
3. generated `Image(width,height)` accepts Graphics drawing and saves nonblank
   output;
4. 500x500 encoded ImageControl scaled to about 89x89 during repeated reposition
   remains stable and does not corrupt accordion/layout state;
5. crop + smooth-scale + rotate direct draw works;
6. decode -> rotate -> applyColor -> save PNG persists both operations;
7. APPLY_FADE current-frame behavior works for a multi-frame strip;
8. changeColors/setTransparentColor exact pixels survive save/readback;
9. repeated destination-scale draws reuse the two-entry cache without stale
   presentation state;
10. explicit deployed `getPixels()` returns correct snapshot without changing
    the Image's native backing authority.

Add a low-cost allocation/accounting assertion or test hook showing that normal
native PNG/JPEG decode and generated-image creation do not create a Java array
proportional to pixel count. Do not build a profiler framework; a focused backing
kind/allocation counter is sufficient if it remains test-only.

Run the final allowed gates only now:

    cmake -S TotalCrossVM -B build-image-backing-final \
      -DCMAKE_BUILD_TYPE=Release \
      -DCMAKE_OSX_ARCHITECTURES=arm64 -G Ninja
    cmake --build build-image-backing-final --target tcvm Launcher --parallel

    cd TotalCrossSDK
    ./gradlew-agent <focused/full Image tests required by this series>
    ./gradlew-agent dist -x test

Then run the final macOS native smoke suite. Do not run Android, iOS, Linux, or
Windows builds/smokes.

Use static final checks from repository root:

    rg -n 'Image_pixels|Image_pixelsOfAllFrames' TotalCrossVM/src/nm || true
    rg -n 'int\[\]\s+pixels\s*;|pixelsOfAllFrames' \
      TotalCrossSDK/src/main/java/totalcross/ui/image/Image.java || true
    python3 scripts/validate-copyright-headers.sh --files <changed first-party files>
    git diff --check

Commit checkpoint F for final smoke/test changes if not already committed.
Commit checkpoint G only for fixes discovered by the final gate. Do not create a
cosmetic final commit solely to alter plan state.

Acceptance:

- all roadmap final acceptance criteria pass;
- final SDK/macOS builds and native smoke pass on one revision;
- no forbidden platform build was run;
- all new files satisfy size limits;
- no unrelated local changes were staged/committed.

## Surprises & Discoveries

Record only final limitations or compatibility facts that remain relevant.
Resolved migration history belongs in archive/evidence, not this active plan.

## Decision Log

- Decision: Java SE keeps live RasterImageBacking pixels; deployed Skia
  `getPixels()` is a detached compatibility snapshot.
- Decision: preserve Image object slot positions with one reserved legacy slot.
- Decision: preserve existing encoder format/metadata and feed it from backing
  readback rather than replacing encoders here.
- Decision: remove synchronized-method correctness assumptions; do not attempt a
  new Image thread-safety model.

## Validation and Acceptance

This plan contains the final ABI gate, so Level-3/4-style validation is narrowed
by explicit user policy to SDK plus macOS arm64 only. Native smoke is allowed only
at milestone ends and final completion.

Before every commit, execute the logical-commits workflow with focused header and
staged diff validation. Record milestone builds as deferred until their gate.

## Risks and Open Questions

There are no open design questions. If a remaining native consumer truly requires
a persistent Java pixel array on the Skia path, treat that as an incomplete
migration: adapt it to backing/readback or stop and report the exact unsupported
contract. Do not keep a hidden parallel full raster.

## Idempotence and Recovery

The ABI slot change is atomic; never leave Java field meaning and native macros at
different commits. If that commit fails validation, fix forward in a new logical
commit unless the user explicitly requests history rewriting. Native backing
release remains idempotent and partial materialization leaves prior state valid.

## Outcomes & Retrospective

At completion update the editorial report with:

- final architecture delivered;
- operations using draw-time Skia versus exact native pass;
- persistent Java raster cases that remain intentionally (Java SE/non-Skia);
- final commit SHA;
- SDK/macOS commands and outcomes;
- final smoke scenarios/outcomes;
- explicit statement that other platform builds were not run by user policy;
- measurable managed-memory change if the focused allocation hook produced one.

## Revision Note

This is the final retirement phase. Any remaining feature beyond the fixed
roadmap is follow-up work, not scope for extending this plan indefinitely.
