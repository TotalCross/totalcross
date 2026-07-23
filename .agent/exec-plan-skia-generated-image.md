<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda
SPDX-License-Identifier: LGPL-2.1-only
-->

# Make SkBitmap authoritative for generated images

This ExecPlan follows `AGENTS.md` and the policy in
`TotalCross/totalcross-depot-tools/.agent/PLANS.md`. It is a living document:
keep `Progress`, `Surprises & Discoveries`, `Decision Log`, and
`Outcomes & Retrospective` current. Store raw logs and artifacts in the
supporting evidence files.

## Purpose / Big Picture

Fix TotalCross issue #417, “Image created via code turns out blank,” so an image
created in Java, rendered through `Image.getGraphics()`, and later exported or
consumed is not blank on Android.

Use the exact issue example as the source of truth:

- Issue: `https://github.com/TotalCross/totalcross/issues/417`
- Example: `https://github.com/user-attachments/files/30019877/Tcsort.zip`

Download and inspect the attachment before implementation. Record its SHA-256,
identify the minimal failing flow, and derive the permanent smoke test from that
exact source. Do not approximate the example from the issue description.

For Skia builds, each generated image will have an offscreen `SkBitmap` and an
associated `SkCanvas`. Once created, the bitmap is authoritative. Drawing,
pixel access, image-to-image drawing, and output encoding must use it directly.
The Java pixel array is used only to initialize a missing bitmap. There is no
ongoing synchronization in either direction.

Use surface ID `-1` for the screen and keep image surfaces zero-based, so the
first image remains surface `0`.

Also perform limited source organization:

- move Skia from `TotalCrossVM/src/nm/ui/android/` to
  `TotalCrossVM/src/nm/ui/skia/`;
- split Skia and `GraphicsPrimitives_c.h` into a few logical files;
- avoid a new renderer abstraction because MBD is expected to replace this
  stack.

Completion requires the issue-derived smoke test to pass from the same revision
on Java SE, macOS, and Android. Java SE covers the Java byte-array path; macOS
and Android cover native Skia build, deployment, and runtime behavior.

## Working Set and Resume Protocol

Read `AGENTS.md`, local `.agent/PLANS.md` when present, this plan, and then
`.agent/state/skia-generated-image.md`. Inspect only files relevant to the
active milestone.

Maintain:

- `.agent/state/skia-generated-image.md`: rewrite after each logical commit with
  active milestone, paths, validation, blockers, deferrals, and next action.
- `.agent/evidence/skia-generated-image.jsonl`: append revision, target,
  command, result, assertion count, artifact paths, and limitations.
- `.agent/archive/skia-generated-image-history.md`: completed detail and
  rejected alternatives when needed.
- `.agent/reports/skia-generated-image-editorial.md`: final factual handoff.

Resume with:

    git status --short
    sed -n '1,220p' .agent/state/skia-generated-image.md

## Progress

- [x] (2026-07-22) Confirmed the affected image-rendering flow.
- [x] (2026-07-22) Chose `SkBitmap` as authoritative after creation.
- [x] (2026-07-22) Rejected synchronization with `Image.pixels`.
- [x] (2026-07-22) Reserved surface `-1` for the screen and kept image IDs
  zero-based.
- [x] (2026-07-22) Limited refactoring to source relocation and logical cuts.
- [x] (2026-07-23T02:44:00-03:00) Inspect `Tcsort.zip` and record its hash,
  entry point, failing flow, and expected output. The APK baseline reproduced
  the blank PNG on the connected Android emulator; the supporting evidence is
  in `.agent/evidence/skia-generated-image.jsonl`.
- [x] (2026-07-23T03:03:18-03:00) Move and split sources without intended
  behavior change. Skia now lives under `nm/ui/skia/`; its screen/font glue,
  surface code, and primitive code are separate translation units, and the
  graphics primitive header includes mechanical Skia, text, shape, and screen
  fragments at their original compilation positions. Native and SDK builds
  passed; no CTest tests are registered in the structural build directory.
- [x] (2026-07-23T03:20:00-03:00) Add image-owned bitmap/canvas surfaces and
  route Skia drawing correctly. Focused native build and surface invariant
  probe passed.
- [x] (2026-07-23T03:32:44-03:00) Enforce bitmap authority across reads,
  writes, image drawing, and output. Initial promotion is guarded by the
  surface ID; focused native build and authority probes passed.
- [x] (2026-07-23T03:43:31-03:00) Create the issue-derived smoke test. The
  standalone TotalCross source, result JSON, and dependency-free PNG checker
  compile and pass focused static checks; runtime deployment remains part of
  the final target matrix.
- [x] (2026-07-23T03:45:00-03:00) Reconcile the implementation, plan, state,
  evidence, and editorial report. The report records the completed native
  work, the smoke artifact, measured source sizes, and the final matrix as
  unverified/deferred.
- [ ] Execute the final Java SE, macOS, and Android matrix from one revision.

## Current Architecture and Scope

The exact path must be confirmed from the ZIP, but the affected contract is:

    Image or MonoImage
        -> getGraphics()
        -> Graphics drawing
        -> export, printing, or another consumer
        -> getPixelRow()

Many Skia calls already receive a surface identifier, but the implementation
uses the global screen canvas. Drawing through a `Graphics` targeting an
`Image` can therefore miss the representation later read by output code.
`getPixelRow()` currently reads the Java image array, which cannot observe
drawing performed only on a Skia canvas.

Keep `Image.textureId` and the Java object layout unchanged:

    SKIA_SCREEN_SURFACE_ID == -1
    first image surface    == 0
    second image surface   == 1

The same numeric value has separate meanings:

- `Image.textureId == -1`: no image bitmap exists;
- draw-call surface `-1`: target the screen.

Authority rule:

    Image.textureId < 0
        Image.pixels may initialize the first SkBitmap.

    Image.textureId >= 0
        SkBitmap is authoritative.
        Never read or write Image.pixels.
        Never upload Image.pixels again.
        Never copy bitmap changes back to Image.pixels.

Java SE keeps its existing byte-array implementation.

Move Skia to:

    TotalCrossVM/src/nm/ui/skia/skia.h
    TotalCrossVM/src/nm/ui/skia/skia_internal.h
    TotalCrossVM/src/nm/ui/skia/skia.cpp
    TotalCrossVM/src/nm/ui/skia/skia_surface.cpp
    TotalCrossVM/src/nm/ui/skia/skia_primitives.cpp

Suggested responsibilities:

- `skia.cpp`: screen context, initialization, flush, fonts, platform glue;
- `skia_surface.cpp`: image surfaces, lifecycle, pixel access, image drawing;
- `skia_primitives.cpp`: clipping, text drawing, shapes, paths, gradients;
- `skia_internal.h`: C++-only shared declarations.

Split `GraphicsPrimitives_c.h` through included headers so the current
`gfx_Graphics.c` compilation model remains:

    GraphicsPrimitives_c.h
    GraphicsPrimitivesSkia_c.h
    GraphicsPrimitivesText_c.h
    GraphicsPrimitivesShapes_c.h
    GraphicsPrimitivesScreen_c.h

Aim for roughly 20 KB or 600 lines where practical, but prefer cohesion over
creating trivial files.

Expected changed paths include `GraphicsPrimitives*.h`,
`GraphicsPrimitives.h`, `ImagePrimitives_c.h`, `image_Image.c`,
`gfx_Graphics.c`, `android/gfx_Graphics_c.h`, the new `skia/` directory,
`TotalCrossVM/CMakeLists.txt`, and the smoke-test source.

Out of scope:

- generic renderer interfaces or runtime backend selection;
- public Java graphics API redesign;
- Java layout changes made only to rename `textureId`;
- dirty-state synchronization;
- MBD migration;
- unrelated algorithm rewrites, formatting, or cleanup;
- committing the attached APK or generated output.

## Plan of Work

### Milestone 0: Capture the exact issue example

Download `Tcsort.zip`, record its SHA-256 and archive listing, and extract it
outside tracked source directories. Identify the source entry point, image type,
drawing calls, output path, dimensions, colors, expected pixels, and deployment
settings. Run the unmodified example on available baseline targets and preserve
logs, screenshots, and generated images. Minimize only after reproducing the
original behavior.

Acceptance:

- exact attachment inspected and hashed;
- source and expected output documented;
- Android baseline reproduced when the environment permits;
- smoke assertions based on actual geometry and pixels, not only “not blank”;
- blockers recorded instead of guessed around.

### Milestone 1: Move and split sources

Move Skia to `nm/ui/skia/`, add `skia_internal.h`, extract surface and primitive
code, update includes and CMake, and split the Skia, text, shape, and screen
blocks from `GraphicsPrimitives_c.h`. Preserve function bodies except for
required declaration, include, and linkage changes. Remove old Android-directory
files only after all references are updated.

Focused validation:

    cmake -S TotalCrossVM -B build-skia-structure \
      -DCMAKE_BUILD_TYPE=Release -G Ninja
    ninja -C build-skia-structure

    cd TotalCrossSDK
    ./gradlew-agent clean dist

Acceptance:

- native and SDK builds succeed;
- focused graphics tests pass;
- no old Skia source remains under `nm/ui/android/`;
- the split is reviewable as a mostly mechanical change.

### Milestone 2: Add image-owned Skia surfaces

Replace the bitmap vector with stable heap-owned entries:

    struct SkiaImageSurface {
        SkBitmap bitmap;
        std::unique_ptr<SkCanvas> canvas;
    };

    std::vector<std::unique_ptr<SkiaImageSurface>> imageSurfaces;

Define screen and invalid IDs and implement checked `skiaGetCanvas()` and
`skiaGetBitmap()` helpers. Return the global screen canvas only for `-1`.
Preserve `imageSurfaces.size()` as the next image ID. Create and destroy bitmap
and canvas together. Make clipping and every primitive resolve the selected
canvas. Add a `GraphicsPrimitivesSkia_c.h` helper that returns the screen ID or
obtains/creates the image surface.

Focused proof:

- screen resolves to `-1`;
- first and second images resolve to `0` and `1`;
- image drawing does not affect the screen canvas;
- clipping affects the selected canvas;
- deleted and invalid IDs fail safely.

Acceptance:

- screen rendering remains unchanged;
- images are valid rendering targets;
- IDs remain zero-based;
- bitmap destruction leaves no accessible stale canvas.

### Milestone 3: Make SkBitmap authoritative

Initialize from `Image.pixels` only when `Image.textureId < 0`. Prevent
`applyChanges()` and `Image.changed` from uploading over an existing bitmap.
Change Skia `drawSurface()` to promote source and destination as needed and draw
bitmap-to-canvas, removing the Skia CPU-copy path between Java arrays.

Route relevant operations to the bitmap when present: pixel and RGB access,
region reads and writes, issue-relevant image operations, output encoding, and
row access. Implement `skia_getPixelRow(surfaceId, output, y)` and make native
`getPixelRow()` prefer it when a bitmap exists, retaining the array fallback
otherwise. Use explicit Skia color conversion; do not assume `SkBitmap` memory
matches `PixelConv`. Preserve multi-frame dimensions and offsets.

Audit direct native uses of `Image_pixels`. Adapt uses reachable after promotion
and document intentionally unreachable cases. Do not rewrite unrelated image
algorithms.

Focused validation:

- draw fixed content into image surface `0`;
- verify `getPixelRow()`, `getPixel()`, and `getRGB()`;
- draw the image to the screen and into another image;
- encode and inspect output;
- delete and recreate a surface without stale content.

Acceptance:

- tested operations never read stale array data after promotion;
- no synchronization loop exists;
- `getPixelRow()` reads the authoritative bitmap;
- Java SE behavior is unchanged.

### Milestone 4: Create the smoke test

Start from the extracted issue source. Preserve the image type,
`getGraphics()` flow, triggering drawing calls, and output path. Remove only
unrelated UI, customer content, credentials, and resources.

Use existing cross-platform smoke infrastructure. If none exists, create:

    tests/smoke/issue-417-generated-image/

Use fixed dimensions, colors, coordinates, and deterministic output. Avoid
network, user input, and timing-dependent assertions. Emit
`issue-417-result.json` containing platform, implementation path, dimensions,
selected expected and background pixels, encoded size, stable hash when
possible, and pass/fail. Preserve the generated image on failure and final
validation. Assert several interior, border, and background pixels and return a
non-zero result on failure.

Acceptance:

- test is clearly derived from the attached source;
- affected Android baseline fails;
- fixed implementation passes;
- both drawing and row/output paths are exercised;
- test runs without the original ZIP after commit.

## Surprises & Discoveries

- The attachment must be inspected before final smoke assertions are chosen.
- Existing surface parameters reduce the need for API changes.
- Moving the screen to `-1` preserves image surface `0`.
- Bitmap authority affects every reachable native array read after promotion.
- The attachment's `MonoImage` flow produces a 576x576 fully transparent PNG on
  the current Android baseline: the extracted RGBA artifact contained zero
  non-zero bytes after `createPng()`, even though the source fills white and
  draws a black border.
- The state file named in the resume protocol was absent at resume time; this
  milestone created `.agent/state/skia-generated-image.md` as the canonical
  state file named by this plan.
- The checked-in source build uses `TotalCrossVM/CMakeLists.txt`; the local
  `TotalCrossVM/xcode/tcvm.xcodeproj` is ignored/generated and was not included
  in the relocation commit. CMake regeneration is required before an Xcode
  build observes the new path.
- Splitting `GraphicsPrimitives_c.h` required preserving an outer
  `#ifndef SKIA_H` across the CPU-only line-drawing block; moving only the
  visible function text initially left an unmatched `#else`. The final split
  keeps the conditional boundary in the main header and the compiler accepted
  both C and C++ translation units.
- Reading `getPixelRow()` directly from the bitmap was stale after drawing via
  its canvas. The final implementation reads the selected canvas with
  `readPixels()` into an explicit RGBA transfer row before converting to
  `PixelConv`.
- No cross-platform smoke harness exists in the repository. The test therefore
  owns its small TotalCross app and a dependency-free host checker, while the
  platform deployment command remains explicit in its README.

Add only discoveries that materially change remaining work.

## Decision Log

- Decision: Derive the smoke test from `Tcsort.zip`.
  Rationale: Validate the reported behavior, not an approximation.
  Date: 2026-07-22.
- Decision: Make `SkBitmap` authoritative after creation.
  Rationale: Keep one mutable image representation.
  Date: 2026-07-22.
- Decision: Use `Image.pixels` only for initial promotion.
  Rationale: Preserve initialization without stale reuploads.
  Date: 2026-07-22.
- Decision: Read `getPixelRow()` from the bitmap when present.
  Rationale: Directly fix the row/output path.
  Date: 2026-07-22.
- Decision: Use screen surface `-1` and zero-based image IDs.
  Rationale: Avoid collision without shifting indices.
  Date: 2026-07-22.
- Decision: Move Skia to `nm/ui/skia/`.
  Rationale: Skia is shared infrastructure, not Android-only code.
  Date: 2026-07-22.
- Decision: Limit refactoring to logical cuts.
  Rationale: Avoid over-investing before MBD.
  Date: 2026-07-22.
- Decision: Require Java SE, macOS, and Android final validation.
  Rationale: Cover the Java array path and native Skia deployments.
  Date: 2026-07-22.
- Decision: Treat Milestone 0 as active when resuming without the state file.
  Rationale: The plan's first unchecked item was the exact attachment
  inspection, and starting source changes without that evidence would violate
  the plan's source-of-truth requirement.
  Date: 2026-07-23.
- Decision: Keep the primitive fragments included at their original positions
  in `GraphicsPrimitives_c.h`.
  Rationale: The header is compiled as part of the C implementation model and
  its static helpers depend on declaration order and platform conditionals;
  preserving positions makes the source organization mechanical and avoids a
  renderer abstraction or behavior change.
  Date: 2026-07-23.
- The earlier mechanical extraction left several new fragments commented out;
  restore the original ranges before routing Milestone 2 calls.
  Rationale: the active C build must compile the real graphics functions while
  preserving their original declaration order.
  Date: 2026-07-23.
- Decision: represent each image surface as a heap-owned bitmap/canvas pair,
  retain deleted vector slots, and resolve every target through checked helpers.
  Rationale: stable zero-based IDs and safe deletion are prerequisites for
  bitmap authority without stale canvas references.
  Date: 2026-07-23.
- Decision: after promotion, ignore `Image.changed` and the Java pixel array;
  route image-to-image drawing through Skia canvases and retain only
  intentionally array-only image algorithms outside the affected path.
  Rationale: a single authoritative bitmap prevents stale reuploads and CPU
  copies while avoiding unrelated algorithm rewrites.
  Date: 2026-07-23.
- Decision: convert between `PixelConv` and Skia colors explicitly at native
  boundaries.
  Rationale: SkBitmap memory layout and PixelConv channel order are not an
  implicit ABI contract.
  Date: 2026-07-23.
- Decision: keep `nome.png` as the generated image name and store it as a
  relative path in `issue-417-result.json`.
  Rationale: this preserves the attachment's output path while making result
  files portable when pulled from different platform application directories.
  Date: 2026-07-23.
- Decision: implement the host-side checker with Python's standard library
  only.
  Rationale: smoke validation should not require Pillow or another optional
  dependency on CI or a developer workstation.
  Date: 2026-07-23.

## Validation and Acceptance

Use focused validation during slices. Run the full matrix at final closure or
when shared changes justify it.

Source checks:

    rg -n 'android/skia\.h|nm/ui/android/skia' TotalCrossVM

    find TotalCrossVM/src/nm/ui -type f \
      \( -name '*.c' -o -name '*.h' -o -name '*.cpp' \) \
      -size +20k -print

SDK build:

    cd TotalCrossSDK
    ./gradlew-agent clean dist

Native build:

    cmake -S TotalCrossVM -B build \
      -DCMAKE_BUILD_TYPE=Release -G Ninja
    ninja -C build

### Java SE

Run the issue-derived smoke app through the current Java SE launcher or test
harness using the SDK from the same revision. Confirm the result identifies the
Java byte-array path, expected pixels match, output is readable and non-blank,
and existing Java SE image tests pass. Save result JSON, image, exact command,
JDK version, and logs.

### macOS

Use the repository-supported macOS deployment flow resolved from current
scripts or documentation. Build SDK and native runtime from the same revision,
deploy and launch the native app, and confirm the result identifies Skia.
Expected pixels must match, image surface `0` must work, screen and image
canvases must remain distinct, and moving Skia must not introduce missing
sources, symbols, bundles, or runtime dependencies. Save environment, commands,
logs, result JSON, and image.

### Android

Build with:

    cd TotalCrossVM/android
    ./gradlew :tcvm:fetchNativeDependencies
    ./gradlew :tcvm:externalNativeBuildCleanRelease
    ./gradlew :app:assembleStandardRelease

Deploy the smoke app through the current Android flow and run on a real device.
An emulator may supplement but not replace the final device run. Require an
explicit completion signal, collect result JSON, image, and logcat, and confirm:
expected pixels match, output is not blank, `getPixelRow()` reads the bitmap,
no stale array upload occurs, image surface `0` works, and no Android build path
references `nm/ui/android/skia.*`.

Final matrix from one revision:

    Java SE  -> Java byte-array implementation -> pass
    macOS    -> native Skia implementation     -> pass
    Android  -> native Skia implementation     -> pass

For each row record revision, dependency revision, build/deployment/runtime
commands, environment, assertion count, result path, image path, log path, and
status. Visual inspection alone is insufficient; pixel assertions must fail
programmatically.

## Risks and Open Questions

- Exact attachment details remain unknown until inspection.
- Skia color type, alpha handling, row bytes, and channel order may differ from
  `PixelConv`.
- Animated images may require all-frame width and current-frame offsets.
- Deleted vector slots must remain safely detectable because IDs are indices.
- The current macOS deployment command must be resolved from repository scripts.
- The smoke app needs a reliable cross-platform completion and artifact path.
- Java SE and deployed targets may use different `Image` implementations;
  result metadata must prove both paths ran.
- Supported direct mutation of `Image.pixels` after promotion would conflict
  with bitmap authority. Stop and record that compatibility issue before
  weakening the rule.

## Idempotence and Recovery

Use Git-aware moves. Do not delete old files until all new files and references
exist. Bitmap creation returns an existing surface when available, creates one
surface otherwise, and leaves the image unpromoted on failure. Deleting invalid
or empty surfaces is safe.

Do not compact the image-surface vector because IDs are indices. Slot reuse is
out of scope unless already safe. `getPixelRow()` is read-only and repeatable.

Do not commit generated dependencies, builds, logs, the original APK, or the
unminimized archive. Before each logical commit:

    git status --short
    git diff --check
    git diff --stat

Keep unrelated changes untouched. If one final target fails, preserve successful
evidence, fix the focused issue, and rerun that target plus any shared
validation justified by the change.

## Outcomes & Retrospective

At each milestone record delivered behavior, changed paths, focused validation,
remaining work, and evidence location.

Milestone 0 completed on 2026-07-23: the attachment was downloaded to a
temporary directory, hashed, listed, and extracted outside tracked source.
`tcsort.java` is the entry point source and uses `MonoImage(576, 576)`,
`getGraphics()`, a white fill, a black inset border, `createPng("nome.png")`,
and `Vm.exec("viewer", ...)`. On the Android emulator, the unmodified APK
created a 576x576 PNG whose decoded RGBA stream had zero non-zero bytes. The
expected smoke geometry is therefore a white 576x576 image with a black
border at the 10-pixel inset and an opaque interior; the current baseline
instead is blank/transparent. Evidence and command artifacts are recorded in
`.agent/evidence/skia-generated-image.jsonl`.

Milestone 1 completed on 2026-07-23: `skia.h` and `skia.cpp` moved from
`TotalCrossVM/src/nm/ui/android/` to `TotalCrossVM/src/nm/ui/skia/`; the CMake
source list and native includes were updated; `skia_internal.h` centralizes
C++-only Skia declarations; `skia_surface.cpp` owns the existing bitmap and
pixel functions; and `skia_primitives.cpp` owns the existing drawing
primitives. `GraphicsPrimitives_c.h` now includes four mechanically extracted
fragments for Skia, text, shapes, and screen code. The structural CMake/Ninja
build and SDK `clean dist` passed. CTest found no registered tests, so no
runtime graphics assertion was claimed in this milestone; the source split
itself was not a runtime graphics validation.

Milestone 2 completed on 2026-07-23: image surfaces now use stable
`std::vector<std::unique_ptr<SkiaImageSurface>>` entries containing a bitmap
and canvas owned together. Screen target `-1`, image IDs `0` and `1`, clipping,
pixel access, image drawing, and all Skia primitives resolve checked target
canvases; deleted slots remain inaccessible. The C helper promotes an image
only to establish its initial target surface, while bitmap authority remains
Milestone 3 work. `ninja -C build-skia-structure` passed, and a temporary
headless probe passed ID, isolation, clipping, deletion, and invalid-ID
assertions. Evidence is appended to
`.agent/evidence/skia-generated-image.jsonl`; SDK, Android, Java SE, and
issue-derived smoke validation were intentionally deferred to later
milestones.

Milestone 3 completed on 2026-07-23: `Image.pixels` now initializes a Skia
surface only while its texture ID is negative; `applyChanges()` and source
promotion do not reupload an existing bitmap. Pixel, RGB, and row reads use
the selected Skia canvas/bitmap with explicit channel conversion, and the
active image-to-image path draws bitmap-to-canvas without copying Java arrays.
The authority probe covered row reads, `getPixel()`/`getRGB()` read and write,
image-to-image drawing, mutation of the original Java array after promotion,
and deletion/recreation without stale content. The focused native build,
authority probe, and the earlier surface probe passed. Array-only transforms
such as scale, rotate, and color transforms remain intentionally outside this
issue path. SDK, Java SE, Android/macOS deployment, and the issue-derived
smoke test remain deferred to later milestones.

Milestone 4 completed on 2026-07-23: `tests/smoke/issue-417-generated-image/`
now contains a minimized `Tcsort.java` that preserves the issue's
`MonoImage(576, 576)`, `getGraphics()`, white fill, black inset border, and
`createPng()` to `nome.png`. It asserts four selected pixels and one interior
RGBA row, emits `issue-417-result.json` with platform/path/dimensions/pixel/
encoding metadata, and records a CRC32 for the PNG. `check_result.py` verifies
the result and PNG structure using only the Python standard library. The
source compiled against the current SDK jars, the checker passed
`py_compile`, and `git diff --check` passed. The Android blank baseline is
already recorded in Milestone 0 evidence; runtime execution of this new test
on Java SE, macOS, and Android remains deferred to the final matrix.

At completion state whether the issue was reproduced, which attachment code
became the smoke test, whether the Android baseline failed, whether all three
targets passed, which operations use bitmap authority, any retained array-only
paths, final source sizes, limitations, and deferred work. Create the editorial
report required by `.agent/PLANS.md`.

## Editorial Report

### Editorial Summary

Issue 417 showed a generated `MonoImage` exporting as a fully transparent
576x576 PNG on the Android baseline, even though the application filled the
image white and drew a black inset border. The underlying problem was that
Skia drawing and later image reads could use different representations: a
canvas held the drawing while Java pixel arrays remained blank or stale.

The implementation now gives each generated image an owned `SkBitmap` and
`SkCanvas`, promotes the Java array only once, and routes the affected drawing,
pixel, RGB, row, and output paths through the Skia representation. A small
regression app derived directly from the attachment is checked in and emits
machine-readable pixel and PNG metadata. The Android baseline failure is
verified; the corrected app has compile-level validation, while final runtime
passes on Java SE, macOS, and Android remain unverified and deferred.

### Original Plan versus Actual Outcome

The plan intended a limited Skia source reorganization, image-owned surfaces,
bitmap authority, and an issue-derived smoke test followed by a three-target
runtime matrix. The source relocation, surface ownership, bitmap authority,
and smoke artifact were implemented as planned. The implementation retained
array-only transformations such as scale, rotate, and color transforms because
they are outside the reachable issue path and rewriting them would expand the
change unnecessarily.

The final runtime matrix was not run in this continuation because it requires
platform deployment and is explicitly deferred in the current state. As a
result, the plan does not claim that all three final targets passed. The
baseline Android failure is an observed Milestone 0 result, not a replacement
for corrected-target validation.

### What Changed

`TotalCrossVM/src/nm/ui/skia/` now contains the relocated and split Skia
implementation. `SkiaImageSurface` owns a bitmap/canvas pair with screen ID
`-1`, zero-based image IDs, and checked access after deletion.
`TotalCrossVM/src/nm/ui/image_Image.c`, `ImagePrimitives_c.h`,
`GraphicsPrimitives_c.h`, and `GraphicsPrimitivesSkia_c.h` implement guarded
promotion and route affected image operations through those surfaces. Native
color conversion is explicit between `PixelConv` and Skia RGBA colors.

`tests/smoke/issue-417-generated-image/Tcsort.java` preserves the source
attachment's 576x576 drawing flow and adds four pixel assertions, one RGBA row
assertion, and deterministic PNG/result output. `check_result.py` validates
the JSON and PNG structure using only Python's standard library.

Measured source sizes at reconciliation were 351 lines for `skia.cpp`, 233
for `skia_surface.cpp`, 279 for `skia_primitives.cpp`, 1,507 for the main
graphics header, 418 for its Skia fragment, 161 for the smoke app, and 61 for
the checker.

### Decisions and Trade-offs

The bitmap is authoritative after promotion, so later `Image.changed` signals
cannot trigger stale Java-array uploads. This removes synchronization loops and
CPU array copies in the affected path, at the cost of making direct Java-array
mutation after promotion unsupported for Skia images.

Surface IDs remain stable by retaining deleted vector slots. This avoids stale
references and ID shifts, at the cost of not reusing deleted slots. The smoke
checker uses relative `nome.png` output paths so pulled artifacts work across
platform application directories. It deliberately avoids optional Python
packages so the test is portable.

### Unexpected Problems and Discoveries

The issue archive's exact source was necessary to identify `MonoImage`, the
576x576 dimensions, the 10-pixel border, and the `nome.png` output. The
unmodified Android APK produced a PNG with zero non-zero decoded RGBA bytes.

During native work, a direct bitmap read in `getPixelRow()` was stale after a
canvas draw; reading the selected canvas with `readPixels()` fixed that case.
The structural split also exposed preprocessor-order constraints, so the
graphics fragments remain included at their original positions. These details
are recorded in `Surprises & Discoveries` and the JSONL evidence file.

### Validation and Measurable Results

Observed results include the Android baseline reproduction in Milestone 0,
successful native structural builds and headless authority probes in
Milestones 1–3, and successful compilation of the smoke app with the current
SDK jars. The current smoke artifact passed:

    javac -Xlint:none -cp TotalCrossSDK/build/libs/totalcross-sdk-7.2.2.jar:TotalCrossSDK/build/libs/tcui-7.2.2.jar -d <temporary-output> tests/smoke/issue-417-generated-image/Tcsort.java
    python3 -m py_compile tests/smoke/issue-417-generated-image/check_result.py
    git diff --check

No final runtime smoke result, PNG artifact, deployment log, performance
measurement, or size comparison across targets was produced in this stage.

### Useful Evidence and Examples

The exact attachment hash, source summary, and blank Android PNG are recorded
in `.agent/evidence/skia-generated-image.jsonl`. The implementation commits
are `9bf9faeb3`, `627dcf6f7`, `6c840cd41`, `14082b5d1`, and `5973bd890`; the
smoke and documentation commits are `3c20258f9` and `f06182cd8`.
The reproducible smoke instructions are in
`tests/smoke/issue-417-generated-image/README.md`, and the temporary native
probes used during development were `/tmp/skia-authority-probe` and
`/tmp/skia-surface-probe`.

### Limitations, Remaining Work, and Open Questions

The Java SE, macOS native deployment, and Android corrected-target runtime
passes remain to be executed from one revision. Android final acceptance also
requires a real device; the earlier emulator baseline only establishes the
reported failure. The current smoke checker validates PNG structure and the
application's pixel metadata, but does not independently decompress PNG IDAT
pixels. Direct Java-array mutation after Skia promotion remains intentionally
unsupported, and array-only image transforms were not rewritten.

### Possible Article Angles

- For runtime engineers: “When a canvas and a pixel array disagree” — use the
  issue's blank PNG to explain authoritative image representations and stale
  uploads.
- For cross-platform SDK maintainers: “Stable surface IDs across native image
  lifecycles” — explain screen ID `-1`, heap-owned bitmap/canvas pairs, and
  safe deletion.
- For test authors: “Turning a customer archive into a deterministic graphics
  regression test” — show how fixed geometry, row assertions, and portable
  result metadata make a rendering bug reproducible.

### Suggested Narrative

Begin with the 576x576 white-and-black image that exported blank on Android.
Show how drawing landed on a Skia canvas while output read stale Java arrays,
then describe the constraints: preserve Java SE behavior, keep image IDs
zero-based, and avoid a renderer rewrite. Follow the implementation from
source relocation to owned surfaces and one-time promotion, including the
canvas `readPixels()` discovery and explicit channel conversion. End with the
checked-in smoke app, the observed baseline failure, the focused native
probes, and the still-pending three-target runtime matrix.

### Claims Requiring Human Review

The issue number, attachment provenance, and baseline failure are supported by
the recorded archive and emulator evidence, but any public statement about
customer impact should be reviewed against the original issue. The claim that
all final targets pass must not be made until the deferred matrix is executed.
No performance, binary-size, or real-device Android claims are currently
substantiated.

## Revision Note

2026-07-22: Initial plan. It requires the direct `Tcsort.zip` attachment as the
smoke-test source, makes `SkBitmap` authoritative, reserves surface `-1` for the
screen, keeps image IDs zero-based, moves Skia to `nm/ui/skia/`, limits the
refactor to logical cuts, and requires final Java SE, macOS, and Android
validation.
