<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda
SPDX-License-Identifier: LGPL-2.1-only
-->

# Move Image materialization to native Skia backing

This ExecPlan follows `AGENTS.md`, `.agent/PLANS.md`, the repository
`logical-commits` skill, and
`.agent/image-native-backing-roadmap.md`. Execute it only after plan 1 passes.

## Purpose / Big Picture

Make native Skia backing authoritative for normal deployed decoded and generated
Images. PNG/JPEG decode and `new Image(width,height)` must stop allocating a full
Java `int[]` raster on the native Skia path. Java SE remains raster-backed.

At the end of this plan, drawing, `getGraphics()`, lazy decode, targeted JPEG
decode, and backing snapshots operate on native storage. Image transforms may
still resolve through old eager algorithms until plans 3 and 4.

## Working Set and Resume Protocol

Maintain `.agent/state/image-native-backing-02.md` and compact evidence
`.agent/evidence/image-native-backing-02.jsonl`. Read state first. Read plan 1
only if state names an unresolved compatibility helper; otherwise trust its
completed outcomes and the roadmap.

Active paths normally include:

- `TotalCrossSDK/src/main/java/totalcross/ui/image/{Image,ImageBacking,NativeImageBacking,RasterImageBacking,BackingImageSource,EncodedImageSource,ImagePipeline}.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/gfx/Graphics.java`
- `TotalCrossVM/src/nm/ui/image_Image.c`
- PNG/JPEG loader paths reached from `pngLoad`/`jpegLoad`
- `TotalCrossVM/src/nm/ui/GraphicsPrimitivesSkia_c.h`
- `TotalCrossVM/src/nm/ui/skia/`
- `TotalCrossVM/src/nm/instancefields.h`
- focused SDK/native smoke tests.

All new files remain below 20 KB/~600 lines.

## Progress

- [ ] Milestone 1: make generated Images native-surface-backed on deployed Skia.
- [ ] Milestone 2: decode PNG/JPEG directly into native backing.
- [ ] Milestone 3: make draw/materialization/snapshot barriers backing-aware.
- [ ] Close plan 2 and prepare plan 3 state.

## Current Architecture and Scope

Plan 1 added explicit backing contracts and an opaque native Skia owner. The
legacy Image arrays still exist as transitional storage. `ImagePipeline` already
contains lazy encoded sources and destination-scale resolution.

This plan changes authority on the deployed Skia path. The invariant at its end
is:

    pipeline != null  -> semantic deferred state
    pipeline == null  -> backing is authoritative

The old `pixels` fields may still be populated on Java SE and temporary fallback
paths, but native Skia correctness must no longer depend on them.

Do not yet replace geometry/color algorithms; the goal is storage/materialization.

## Plan of Work

### Milestone 1: Allocate generated images as native surfaces

Change `Image(int logicalWidth, int logicalHeight, double contentScale, ...)` so:

- Java SE/non-Skia creates `RasterImageBacking` exactly as today;
- deployed Skia calls `NativeImageBacking.createSurface(pixelWidth,pixelHeight)`;
- no Java array proportional to image area is allocated on deployed Skia;
- logical width/height/contentScale metadata is unchanged;
- transient allocation failures preserve current ImageException behavior.

Make `Graphics` targeting an Image resolve its canvas from `NativeImageBacking`,
not by promoting `Image_pixels` through `skia_makeBitmap`. Keep the old promotion
path only for a `RasterImageBacking` fallback.

`getGraphics()` on a native mutable backing returns Graphics directly. Preserve
font initialization and `gfx.refresh` behavior. Do not use `pixels != null` as
the availability test.

Preserve `lockChanges()` compatibility by introducing/using explicit locked state
if needed; do not finalize that API cleanup until plan 5.

Add focused tests/smoke source for generated native image drawing and row readback.
Reuse `tests/smoke/issue-417-generated-image` rather than duplicating its full
scenario when it already proves the path.

Commit checkpoint A for generated Image allocation. Checkpoint B for Graphics
surface routing if independently reviewable.

Milestone gate:

    cmake -S TotalCrossVM -B build-image-backing-02-m1 \
      -DCMAKE_BUILD_TYPE=Release \
      -DCMAKE_OSX_ARCHITECTURES=arm64 -G Ninja
    cmake --build build-image-backing-02-m1 --target tcvm Launcher --parallel

    cd TotalCrossSDK
    ./gradlew-agent <focused generated-image tests>
    ./gradlew-agent dist -x test

Run the generated-image native smoke only after these builds. Assert written
pixels, draw-to-screen/image behavior, and nonblank output/readback.

Acceptance:

- generated native Images have native mutable backing and no full Java raster;
- `getGraphics()` writes to canonical backing;
- issue-417 style generated image behavior passes on macOS smoke;
- Java SE generated-image tests remain unchanged.

### Milestone 2: Decode PNG/JPEG directly to native backing

Refactor deployed decode destinations so `pngLoad` and `jpegLoad` no longer
allocate/store `Image_pixels` for Skia builds. Preserve encoded bag ownership,
format inspection, comments, logical dimensions, frame metadata, deterministic
corruption caching, and transient failure classification.

Required decode destination rule:

- decoder allocates or receives a native pixel buffer;
- decode/color conversion writes once into that native buffer;
- native backing takes ownership without a second full-size copy when safe;
- if the decoder/API requires a separate temporary native buffer, release it
  immediately after constructing the backing; never route it through Java.

Use explicit ARGB/channel conversion. Validate integer overflow before allocating
`width * height * sizeof(Pixel)`.

PNG:

- preserve alpha, transparent-color/comment metadata, and current supported PNG
  restrictions;
- preserve multi-frame/comment-derived behavior where applicable;
- do not change eager encoded-structure validation.

JPEG:

- preserve full decode behavior;
- preserve targeted/reduced decode dimensions and retry semantics;
- targeted decode remains legal only under the existing first-smooth-scale
  eligibility rule; do not move/reorder pipeline nodes.

Change decode result validation from `decoded.pixels != null` to explicit backing
validity plus positive dimensions/metadata.

Keep Java SE ImageIO decode and `RasterImageBacking` unchanged as the differential
reference.

Add test hooks that can prove native decoded backing existence without exposing
Skia publicly. Replace decoded-raster allocation failure tests on deployed Skia
with equivalent native-backing allocation failure tests; retain Java SE raster
failure coverage.

Commit checkpoint C for PNG/native destination. Checkpoint D for JPEG/targeted
decode if the loaders are independently reviewable. Checkpoint E for Java
materialization/test adaptation.

Milestone gate:

    cmake -S TotalCrossVM -B build-image-backing-02-m2 \
      -DCMAKE_BUILD_TYPE=Release \
      -DCMAKE_OSX_ARCHITECTURES=arm64 -G Ninja
    cmake --build build-image-backing-02-m2 --target tcvm Launcher --parallel

    cd TotalCrossSDK
    ./gradlew-agent <focused lazy-decode and targeted-decode tests>
    ./gradlew-agent dist -x test

Run a native smoke after the gate with deterministic PNG and JPEG resources.
Prove dimensions, selected pixels, alpha for PNG, targeted JPEG result dimensions,
and successful repeated draw without a Java full-raster dependency.

Acceptance:

- native PNG/JPEG materialization creates `NativeImageBacking`;
- ordinary native decode does not create a Java area-sized raster;
- targeted JPEG decode still avoids full intrinsic decode when eligible;
- deterministic/retryable failures retain their prior classification;
- SDK and macOS gates pass.

### Milestone 3: Make barriers and snapshots native-aware

Update `materializeCanonicalChecked`, `resolveForDrawing`, source snapshots,
copy/adoption helpers, and Graphics draw bridge so a native-resolved Image stays
native. Do not convert native backing to raster merely because a legacy helper
expects `pixels`.

For a materialized native source used by a result-producing transform, create an
immutable native snapshot and store it in `BackingImageSource`. A later
`getGraphics()` write to the source must not affect the derived pipeline.

For a deferred image whose canonical materialization is requested, resolve to a
native backing and adopt that backing. Clear the pipeline/cache exactly once.
Preserve metadata (`logicalWidth`, `logicalHeight`, `contentScale`, frame state,
alphaMask, transparentColor, hw scale).

For direct drawing of a materialized native image, `Graphics.drawImage`,
`copyRect`, and `copyImageRect` must pass native backing through without Java
readback.

Keep the two-entry destination-scale cache and make cache entries native-backed.
Eviction drops references; do not call `freeTexture()` on canonical backing.

Add a focused regression smoke for the previously observed indirect ImageControl
path: an encoded 500x500 image scaled to about 89x89 during repeated reposition
must draw correctly and must not corrupt unrelated layout state. Keep the smoke
small; do not copy an entire Showcase application.

Commit checkpoint F for snapshot/materialization. Checkpoint G for Graphics/cache
routing. Checkpoint H for smoke/test fixture if useful.

Milestone gate:

    cmake -S TotalCrossVM -B build-image-backing-02-m3 \
      -DCMAKE_BUILD_TYPE=Release \
      -DCMAKE_OSX_ARCHITECTURES=arm64 -G Ninja
    cmake --build build-image-backing-02-m3 --target tcvm Launcher --parallel

    cd TotalCrossSDK
    ./gradlew-agent <focused image materialization/draw tests>
    ./gradlew-agent dist -x test

Run the related native smoke only after the gate.

Acceptance:

- native backing survives all ordinary draw/materialization paths;
- no draw barrier requires `Image.pixels` for native images;
- materialized-source derived images have call-time snapshot semantics;
- ImageControl 500x500->~89x89 repeated resize path is stable;
- cache behavior is preserved.

## Surprises & Discoveries

Record only facts that change remaining implementation. Do not reopen the fixed
choice of native backing versus Java raster.

## Decision Log

- Decision: generated images use native mutable surfaces on deployed Skia.
- Decision: PNG/JPEG decoders write to native-owned storage, never Java area-sized
  arrays on deployed Skia.
- Decision: materialization adopts a backing, not a pixel array.
- Decision: result-producing transforms snapshot mutable native sources at call
  time.

## Validation and Acceptance

Build only SDK and macOS, only at milestone gates. Native smoke only at the end of
each related milestone. Between commits use static/header/diff checks.

Use `logical-commits` for every checkpoint. State explicitly that expensive build
validation is deferred to the milestone gate when committing earlier slices.

## Risks and Open Questions

No design decisions are delegated. Stop and report if a decoder cannot be adapted
to native-owned output without changing its externally observable format support;
do not silently re-enable Java full-raster storage as the normal Skia path.

If zero-copy ownership is impossible for one decoder, one additional native copy
is allowed as a bounded fallback. A Java full-image copy is not allowed.

## Idempotence and Recovery

Decoder allocation failure must release partial native buffers/backings. Handle
creation/adoption must be exception-safe. Repeated smoke runs must overwrite only
their own temporary output. Preserve unrelated worktree and caches.

## Outcomes & Retrospective

At completion record which constructors/decoders are native-backed, gate results,
smoke results, and any legacy raster compatibility path intentionally retained for
plans 3-5.

## Revision Note

This is the authority-migration phase. Do not add geometry/color algorithm work
unless strictly required to keep the migrated storage path functional.
