<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda
SPDX-License-Identifier: LGPL-2.1-only
-->

# Native Image Backing Architecture Plan

## Purpose

Make deployed Skia `Image` instances native-backed from construction through
drawing and materialization. Encoded sources remain authoritative, decoded and
generated pixels live in an explicit backing abstraction, and deferred
operations remain semantic pipeline state until a draw or API barrier executes
them.

The observable result is that encoded PNG/JPEG images, generated images, and
mutable surfaces do not require a persistent Java `int[]` proportional to the
raster on deployed Skia. Geometry and exact compatible color operations can be
executed by Skia at draw time. Pixel observers and encoders materialize the
same semantic pipeline into a native backing, so sequences such as decode,
rotate, color mutation, and PNG save preserve their final pixels.

## Scope

The implementation is confined to the SDK Image model and its native VM
bridge:

* `TotalCrossSDK/src/main/java/totalcross/ui/image/` contains backing
  contracts, source ownership, semantic pipelines, draw plans, and
  compatibility logic.
* `TotalCrossVM/src/nm/ui/` and `TotalCrossVM/src/nm/ui/skia/` contain opaque
  backing ownership, decoding, geometry, color, readback, and native draw code.
* `TotalCrossSDK/build.gradle` wires SDK tests and macOS smoke fixtures.
* SDK tests, converter tests, native tests, smoke apps, and accounting code are
  permanent regressions for the behavior described here.

## Architecture

`Image` has presentation metadata, an immutable semantic `ImagePipeline`, and
one content representation. `ImageBacking` abstracts that representation:
`RasterImageBacking` owns Java raster arrays for JavaSE and non-Skia fallback;
`NativeImageBacking` owns one opaque native handle for deployed Skia. The Java
API never exposes Skia types.

`EncodedImageSource` eagerly captures and structurally validates caller input,
but defers pixel decode. Its bytes and native backing cache are independently
owned. A decoded native image is immutable; a generated or graphics target is
a mutable native surface. Snapshots are immutable and release is idempotent.

The native object supports empty mutable surfaces, decoder-owned images,
snapshots, source/subset drawing, row or region readback, explicit full-raster
compatibility snapshots, and safe release. It uses explicit TotalCross ARGB
and Skia color conversion rather than host-endian pixel reinterpretation.

The semantic pipeline remains authoritative. `ImageDrawPlan` is an immutable
execution optimization carrying ordered operations, source/frame metadata,
content and destination scales, alpha, and fill information. It never becomes
canonical `Image` state. A draw may reuse a prefix or destination-scale result,
but it cannot reorder operations or change their meaning.

## Compatibility and invariants

The following contracts are fixed for the implementation:

* Public `Image` signatures and the native field/bridge ABI remain compatible.
  The object-field prefix keeps backing in the former image-pixel slot,
  reserves the next legacy slot, and retains the established later indexes.
* JavaSE and non-Skia paths remain raster-backed and retain the legacy eager
  algorithms. Deployed `getPixels()` returns a detached snapshot and never
  becomes canonical storage.
* `getGraphics()` is the mutability barrier. Deferred or immutable content is
  resolved once into a mutable native surface, while `lockChanges()` retains
  its documented null behavior.
* `freeTexture()` may release transient GPU resources but never the canonical
  native backing. Native ownership is attached to the backing wrapper and is
  safe across snapshots, aliases, cache eviction, and failed retries.
* Logical dimensions are independent of physical dimensions. Exact fractional
  content scales use checked ceiling arithmetic; frame layout preserves
  integer truncation, residual strips, and zero-width compatibility.
* Frame selection is source-rectangle presentation state, not a copied visible
  array and not a draw-plan cache key. Current frame, alpha, transparent color,
  and hardware scale are synchronized whenever a cached result is reused.
* `SCALE` uses nearest-neighbor sampling, `SMOOTH_SCALE` retains the existing
  cubic quality, and rotation retains output bounds and fill-color behavior.
* Color formulas, alpha-zero hidden RGB, exact color-key matching, frame scope,
  call-time fade ordering, and eager materialized-image aliasing remain intact.
* Canonical barriers resolve at scale one and adopt a native result. A direct
  draw may resolve at destination scale without mutating the source `Image`.
* Deterministic source failures may remain cached. Allocation, resource, and
  promotion failures remain retryable and never destroy a valid old backing.

## Plan of work

### 1. Native image backing model

Introduce `ImageBacking`, `NativeImageBacking`, `RasterImageBacking`, and
`BackingImageSource`. Define ownership, immutable snapshots, mutable surfaces,
release behavior, dimensions, and the unchanged `Image`/native field layout.
Register the opaque native handle while preserving existing slots and the
JavaSE/non-Skia fallback.

### 2. Native materialization

Route PNG/JPEG decoding and generated image allocation to native backing on
deployed Skia. Make `getGraphics()` target native surfaces and make deferred
barriers, snapshots, and lifecycle operations backing-aware. Decode directly
from captured encoded bytes without a Java full-raster allocation.

### 3. Native deferred geometry

Execute `FRAME_SELECT`, `FRAME_LAYOUT`, `CROP`, `SCALE`, `SMOOTH_SCALE`, and
`ROTATE_SCALE` through the native backing and draw bridge. Preserve frame-strip
metadata, residual widths, crop composition, fill, fractional scales, and
materialization geometry. Keep materialized `Image` instances eager and
preserve PNG round-trip behavior.

### 4. Native color operations

Execute fade, alpha, touch-up, color mapping, exact color-key passes, and
their barriers against native storage. Preserve operation ordering across
geometry and sampling, multi-frame scope, alpha and hidden RGB, and exact
JavaSE/native semantics. Use native lookup/filter facilities only when they
are exact; otherwise materialize natively.

### 5. Retire deployed pixel-array backing

Route readback, frame access, PNG/JPEG/PDB encoding, equality, hashing, and
native consumers through `ImageBacking`. Remove production dependence on
`Image_pixels` and `Image_pixelsOfAllFrames` while preserving ABI positions,
legacy behavior, and detached deployed `getPixels()` snapshots. Keep native
readback accounting to prove ordinary creation and decode do not read back a
full raster.

### 6. Draw-plan execution

Represent deferred execution with `ImageDrawPlan`. Compile semantic pipelines
into ordered native-executable stages, reuse exact cached prefixes, key draw
plans by decode generation and destination scale, and synchronize mutable
presentation state on every reused result. Treat the plan strictly as an
optimization layer.

### 7. Exact draw-time color fusion

Fuse `TOUCH_UP`, `FADE`, `ALPHA`, `APPLY_FADE`, and exact `APPLY_COLOR` stages
when differential evidence proves parity. Keep fill ordering exact through
rotation and sampling, preserve frame fade scope and hidden RGB, and split at
unsupported or non-exact boundaries. `APPLY_COLOR2` remains an exact barrier
when pinned Skia cannot execute it exactly, with cached analysis as allowed.

### 8. Source backing reuse and adaptive JPEG decode

Cache decoded native backing at `EncodedImageSource` and let independent
pipeline leaves reuse it. For JPEG, use conservative denominator tiers
`{8,4,2,1}`, choose the smallest sufficient tier, promote monotonically and
transactionally, key draw plans by generation, and never upscale a reduced
tier when more detail is required. Zoom-out does not demote. Keep explicit
eviction as a narrow package-private hook unless an existing memory-pressure
callback can safely own it.

## Decisions and alternatives

Native backing is the canonical deployed representation because persistent Java
rasters scale with image area and create avoidable readbacks. JavaSE is not
routed through Skia because its raster implementation is the compatibility
reference. Source capture stays eager while decode stays lazy so paths,
streams, and caller buffers cannot outlive their ownership boundary.

Immutable semantic nodes and detached snapshots are preferred over mutable
commands. Draw plans and caches cannot replace semantic pipeline state. Native
execution is preferred over Java fallback for both geometry and exact color;
approximate filters, operation reordering, global decoded-raster caches,
native traversal of Java nodes, GPU command buffering, and broad reduced
resolution decoding are rejected because they change semantics or lifetime.

`APPLY_COLOR2` remains a barrier when pinned Skia lacks an exact facility.
Explicit eviction remains unwired when no repository-wide memory-pressure
callback exists. These are bounded compatibility decisions, not new public
APIs or scheduler work.

## Risks and open questions

The main risks are ABI slot drift, native handle lifetime errors, stale cached
presentation state, incorrect physical frame geometry, premultiplied-color
rounding, hidden RGB loss, and JPEG underdecode. Each is covered by an ABI,
backing-contract, geometry, color, draw-plan, or adaptive-decode regression.

Do not broaden the public API, modify `tc-sample`, introduce synchronized
correctness paths, change unrelated renderers, or claim unsupported platforms.
Do not destroy a backing on a failed promotion or snapshot. If exact native
execution cannot be established, retain the semantic barrier and document the
limitation.

## Validation and acceptance

Acceptance requires:

* the plan and implementation preserve the public API, native field layout,
  JavaSE fallback, and documented image semantics;
* focused `totalcross.ui.image.*` tests, converter/native-backing tests, SDK
  distribution, and a macOS arm64 build of `tcvm` and `Launcher` pass;
* the macOS smoke matrix covers ImageModifier, JPEG pinch, frame
  state/layout, native geometry, native materialization, lazy materialization,
  presentation state, deferred frame fade, encoded-source decoding, and PNG
  semantic parity;
* native field scans show no production `Image_pixels` or
  `Image_pixelsOfAllFrames` dependency, and readback accounting proves the
  detached compatibility path only;
* copyright/header validation, `git diff --check`, message-format checks, and
  new-file size checks pass;
* generated logs, profiling helpers, and other execution-only material remain
  outside the source-controlled implementation;
* Android, iOS, Linux, and Windows validation is explicitly tracked as a
  separate platform effort when the required toolchains are available.

Validation should be performed at implementation milestones, with verbose
output in task logs and concise results in the technical report. Avoid
repeating an expensive build when an unchanged target already provides the
necessary evidence.
