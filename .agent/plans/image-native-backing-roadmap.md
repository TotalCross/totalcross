<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda
SPDX-License-Identifier: LGPL-2.1-only
-->

# Native Image Backing and Skia Execution Roadmap

This document defines the fixed architecture shared by the sequential ExecPlans.
They follow `AGENTS.md`, `.agent/PLANS.md`, and the repository
`logical-commits` skill. Do not reopen these design decisions during execution.

## Purpose / Big Picture

Replace `totalcross.ui.image.Image`'s mandatory full-size Java `int[]` raster
storage on deployed Skia builds with an explicit backing model. Normal decoded
and generated images must live in native Skia-owned storage, while Java SE and
non-Skia compatibility paths retain a raster backing behind an abstraction.

Move deferred image operations away from Java/native loops over `Image.pixels`.
Execute geometry and compatible color operations directly through Skia at draw
time when possible. When an API requires concrete output, materialize the same
pipeline once into a native `SkSurface`/`SkImage`, then read or encode from that
backing. A transformation must remain part of the semantic state of the `Image`;
it is never merely a visual effect attached to a control paint.

The completed design must support, without a persistent Java pixel array on the
native Skia path:

    Image image = new Image("input.png");
    Image rotated = image.getRotatedScaledInstance(100, 90, 0);
    rotated.applyColor(...);
    rotated.createPng(output);

The saved PNG must contain the transformed result. The same deferred operations
must also work when the image is drawn directly to the screen or into another
image.

## Execution Sequence

Execute these plans in order. Do not start a later plan while an earlier plan has
unresolved acceptance failures.
After finishing a plan, do not initiate the next plan immediately. The execution 
will be later resumed after the plan's implementation is validated.

1. `.agent/exec-plan-image-native-backing-01-foundation.md`
   Introduce backing contracts and native Skia ownership without removing the
   legacy raster path.
2. `.agent/exec-plan-image-native-backing-02-materialization.md`
   Make decoded and generated images use native backing on deployed Skia builds.
3. `.agent/exec-plan-image-native-backing-03-geometry.md`
   Execute frame, crop, scale, smooth-scale, and rotate/scale through Skia.
4. `.agent/exec-plan-image-native-backing-04-color.md`
   Execute or materialize color/alpha/touch-up mutations without `Image.pixels`.
5. `.agent/exec-plan-image-native-backing-05-retirement.md`
   Move readback/encoding/legacy contracts to the backing API and retire the
   pixel-array fields from `Image`.

### Execution status

The five-plan image-native-backing sequence is complete. Plan 5's historical
implementation close was `b7337e93e`; the current corrective FRAME_LAYOUT
revision is `ee0fbe23a`, whose macOS arm64 SDK/native gate, focused frame-state
coverage, geometry/materialization smokes, and deployed final smoke passed.
The structural split is committed at `4d9ea4dd7`; its macOS arm64 native
build and required deployed smokes also passed. The completion report is in
`.agent/reports/image-native-backing-05-editorial.md` and compact evidence is
indexed in `.agent/evidence/image-native-backing-05.jsonl`.

At the start of each plan, record the actual `HEAD`. Do not automatically switch
branches. The expected starting code contains `ImagePipeline`, `ImageSource`,
`EncodedImageSource`, `RasterImageSource`, `ImageDrawingBridge`, the Skia image
surface code under `TotalCrossVM/src/nm/ui/skia/`, and the existing generated
image smoke test. If any of these contracts are absent or materially different,
stop and report the mismatch instead of reconstructing the feature from history.

## Fixed Architecture

### Image state

`Image` owns presentation metadata and exactly one of two content states:

    Deferred state:
        pipeline != null
        materialized backing may be absent

    Materialized state:
        pipeline == null
        backing != null

Do not use `pixels == null` or `textureId < 0` as the semantic state test.

Introduce a package-private backing abstraction in
`TotalCrossSDK/src/main/java/totalcross/ui/image/`:

    ImageBacking
      |- NativeImageBacking
      `- RasterImageBacking

`RasterImageBacking` owns Java raster arrays. It is the normal Java SE and
non-Skia fallback representation. `NativeImageBacking` owns one opaque native
handle and contains no Skia type in the Java API.

The native handle refers to a C++ backing object owned by the Skia layer. The
native object holds one canonical representation:

- immutable `SkImage` for decoded/snapshotted/final transformed content; or
- mutable `SkSurface` for an image currently used as a Graphics destination.

A native backing may convert to a mutable surface for `getGraphics()`. A snapshot
of a mutable surface is an immutable `SkImage`, using Skia copy-on-write where
supported.

Do not expose `SkImage`, `SkSurface`, or Skia-specific enums in public or
package-level Java contracts other than the opaque native backing bridge.

### Source and snapshot semantics

Keep `EncodedImageSource` as the source for not-yet-decoded PNG/JPEG data.
Replace raster-only pipeline snapshots with a backing-aware source. A deferred
result created from a materialized source must observe the source contents at the
moment the result-producing method is called.

Therefore:

- snapshot a `NativeImageBacking` to an immutable native image before using it as
  the root of a result-producing deferred pipeline;
- clone a `RasterImageBacking` when a detached raster snapshot is required;
- never let a later `Graphics` write or in-place mutation of the source change an
  already-created derived image.

The private `Image(Image src)` sharing behavior remains an aliasing mechanism:
when it is used for legacy shared-image semantics, share the same backing wrapper
and metadata-sharing objects exactly as the old code shared raster/texture state.
Do not silently turn that constructor into a snapshot operation.

### Native ownership

Use a Java `NativeImageBacking` wrapper as the sole owner of its opaque native
handle. Multiple `Image`/source objects may share the same wrapper object; do not
invent a second Java-side reference count. Its native release operation must be
idempotent and its finalizer must release the handle once.

Do not depend on Java `synchronized` for device correctness. TotalCross deploy
ignores synchronized-method locking. The Image API remains externally
unsynchronized/single-thread-affine as today. Native destruction and invalid
handle checks must fail safely without relying on a Java monitor.

`freeTexture()` may release transient GPU/cache resources, but it must never
release the only canonical native image backing. Backing lifetime belongs to the
backing wrapper, not to the legacy texture-cache API.

### ABI and field layout

`Image` field order is consumed by `TotalCrossVM/src/nm/instancefields.h`.
During the migration, add new object fields only after the existing object-field
sequence so old indexes do not shift unexpectedly.

At final retirement, preserve the established object-slot positions used by
native code. The intended final object-field prefix is:

    slot 0: ImageBacking backing
    slot 1: Object reservedLegacyPixelsOfAllFrames  // always null
    slot 2: String comment
    slot 3: Graphics gfx
    slot 4: boolean[] changed
    slot 5: int[] instanceCount
    slot 6: Image[] master
    slot 7: String path
    slot 8: ImagePipeline pipeline

Reuse the old `pixels` slot for `backing` and keep slot 1 reserved so later
object indexes stay stable. Do not compact these slots. Keep all existing int
positions, including legacy/non-authoritative `textureId`.

Update `instancefields.h` only in the milestone that atomically updates all
native consumers of the changed slot meaning.

### Native Skia backing implementation

Create cohesive native files under `TotalCrossVM/src/nm/ui/skia/`; new files must
remain below 20 KB and approximately 600 lines each. Prefer responsibilities such
as:

- backing ownership, snapshot, surface creation, and metadata;
- drawing/materialization of image pipelines;
- native readback/exact pixel transforms if a second file is needed.

Do not refactor an existing large file merely to reduce its size. Extract code
from an existing file only when the extraction is required by this feature and
the new file remains within the new-file size limit.

The native backing API must provide, at minimum:

- create empty mutable backing;
- create immutable backing from decoder-owned native pixels;
- validate dimensions and handle;
- get mutable canvas/surface;
- make immutable snapshot;
- draw source backing or source subset to a target canvas;
- read one row/region in TotalCross ARGB semantics;
- create a full raster readback only for explicit compatibility APIs;
- release backing;
- expose no raw pointer to Java except as the opaque handle.

Use explicit TotalCross ARGB <-> Skia color conversion. Do not reinterpret
host-endian bytes as `Pixel`; prior Skia work already showed channel-order bugs
from that assumption.

### Decode behavior

On deployed Skia builds, PNG and JPEG decode must never allocate a Java array
proportional to decoded pixel count. Decode into native-owned storage and install
or wrap it into the native backing. Avoid a second full-size native copy when
Skia can safely take ownership of the decoder buffer.

Keep encoded-byte capture/validation eager as already designed. Keep pixel decode
lazy. Preserve deterministic-corruption caching and retryable resource/allocation
failure semantics.

Target-aware JPEG decode remains valid only when the first effective root
operation is smooth scaling and the existing eligibility checks permit it. Do not
reorder pipeline nodes to obtain a targeted decode.

### Generated/mutable images

On deployed Skia builds, `new Image(width, height)` and logical-image creation
allocate a mutable native `SkSurface` backing directly. `Image.getGraphics()`
returns a `Graphics` whose native target canvas is that surface.

If `getGraphics()` is called on an immutable native backing or deferred image:

1. resolve semantic deferred operations;
2. create a mutable surface of the final pixel dimensions;
3. draw/copy current image content once;
4. replace the Image's canonical backing with that mutable backing;
5. return Graphics targeting it.

If `lockChanges()` has been called, preserve the documented behavior that
`getGraphics()` returns null. Implement this with explicit state, not with a
missing pixel array.

### Pipeline execution

Operations remain semantic `ImagePipeline` nodes. A draw is an execution target,
not the owner of transformation state.

Classify operations as follows:

Draw-fusible geometry:

- `FRAME_SELECT`
- `FRAME_LAYOUT` metadata/source selection
- `CROP`
- `SCALE`
- `SMOOTH_SCALE`
- `ROTATE_SCALE`

Color/filter candidates:

- `FADE`
- `ALPHA`
- `APPLY_COLOR`
- `APPLY_FADE`
- `TOUCH_UP`

Exact/native-transform operations:

- `APPLY_COLOR2` because it requires source-wide brightness analysis;
- `CHANGE_COLORS` because it requires exact ARGB equality;
- `SET_TRANSPARENT_COLOR` because it requires exact color-key behavior.

A color/filter candidate may use a Skia built-in filter only if differential
tests prove exact required TotalCross output for representative boundary values.
If a built-in filter differs, use this fixed fallback order:

1. use a pinned-Skia runtime/filter mechanism that implements the exact formula;
2. if that mechanism is unavailable in the pinned headers, perform the exact
   transform natively against Skia/native pixel storage and produce a new native
   backing.

Never fall back to a Java full-image raster solely to implement one of these
operations.

### Geometric semantics

Preserve current metadata and rounding rules. In particular:

- logical dimensions remain independent of physical backing dimensions;
- `contentScale` is exact and is never reconstructed from rounded dimensions;
- crop uses the current `scaledCropEdge`/ceil-sensitive rules;
- fractional scales such as 1.1, 1.25, and 1.75 remain covered;
- frame layout preserves integer truncation and zero-width compatibility;
- multi-frame storage remains one strip backing plus frame metadata;
- frame selection is a source-rectangle offset, not a copy into a visible-frame
  array.

Use nearest-neighbor sampling for `SCALE`. Use Catmull-Rom-equivalent cubic
sampling for `SMOOTH_SCALE`; do not silently downgrade it to bilinear/low-quality
filtering. Rotation uses existing output-dimension and fill-color semantics.

For direct draw of a rotated image, draw the fill color over the transformed
image's output bounds before drawing the transformed source when the historical
operation requires that fill. Respect alpha and clipping.

### Color semantics

Preserve the existing formulas, frame scope, alpha handling, and integer/clamp
behavior. In particular:

- `APPLY_FADE` affects only the frame selected at call time for multi-frame
  images;
- `APPLY_COLOR`, `APPLY_COLOR2`, and `CHANGE_COLORS` retain their historical
  multi-frame behavior;
- alpha-zero pixels that the old implementation leaves untouched must remain
  byte-identical when later observed through readback/encoding;
- `CHANGE_COLORS` compares exact ARGB values;
- `SET_TRANSPARENT_COLOR` preserves exact legacy color-key semantics;
- do not use premultiplied-color approximations when the old public result is
  observable as unpremultiplied TotalCross ARGB.

Prefer lookup tables for operations that naturally map 8-bit channels, but only
when they preserve exact behavior. `APPLY_COLOR2` must perform its brightness
reduction against the correct full frame strip before applying its transform.

### Materialization barriers

A pipeline may remain deferred through ordinary screen drawing. These operations
require a concrete semantic result and therefore materialize to a native backing
on deployed Skia:

- `getGraphics()`;
- drawing into another mutable `Image` when the destination operation cannot
  consume the pipeline directly;
- `createPng`, `createJpg`, PDB image output, or any encoder path;
- `getPixelRow`, `getRGB`, pixel access, equality, and hashing when the operation
  needs concrete pixels;
- APIs whose compatibility contract explicitly returns a raster.

A materialization barrier renders the whole pending pipeline once into a native
surface/backing, adopts that result when the API is mutating the Image, and does
not create a full Java pixel array unless an explicit supported raster API
requires one.

`createPng`/`createJpg` should continue using the existing output semantics and
metadata format. Prefer backing row/region readback into bounded temporary
buffers over replacing the encoder during this refactor. This avoids changing
PNG comments/frame metadata while still removing the persistent full-size Java
raster.

### `getPixels()` compatibility

`getPixels()` is documented for desktop use. Preserve Java SE behavior by
returning the live array owned by `RasterImageBacking`.

On deployed Skia, keep the method callable for binary/source compatibility but
return a materialized snapshot array from native readback. Do not make that array
the canonical backing and do not keep it attached to the Image. No internal
native path may depend on mutations of this deployed snapshot. This does not
change the documented desktop contract.

### Cache behavior

Keep the two-entry per-leaf destination-scale cache policy. Cache native resolved
backings/images or resolved Image variants without introducing a Java raster.
Cache eviction drops references; native backing ownership is released by the
backing wrapper lifecycle. Do not free a shared canonical backing through
`freeTexture()`.

### Java SE and non-Skia behavior

Java SE remains raster-backed and is the differential reference for legacy image
algorithms. Do not force Java SE through native Skia.

Non-Skia deployed code must continue compiling against `RasterImageBacking`.
Fallback native helpers must access `RasterImageBacking` through explicit macros,
not `Image_pixels`. Do not run non-SDK/non-macOS builds in this series.

## Validation and Build Budget

The user explicitly limits build operations for this series.

Allowed builds:

- SDK only;
- macOS arm64 native VM/Launcher only.

Forbidden builds during this series:

- Android;
- iOS;
- Linux;
- Windows;
- any other platform or packaging matrix.

Run an allowed build only at the end of a milestone that changes the relevant
SDK or native contract. Do not build after each commit.

Use the repository macOS command shape:

    cmake -S TotalCrossVM -B <milestone-build-dir> \
      -DCMAKE_BUILD_TYPE=Release \
      -DCMAKE_OSX_ARCHITECTURES=arm64 \
      -G Ninja
    cmake --build <milestone-build-dir> --target tcvm Launcher --parallel

Use the SDK wrapper, normally without `clean` unless stale artifacts require it:

    cd TotalCrossSDK
    ./gradlew-agent dist -x test

When the milestone's acceptance explicitly requires SDK tests, run the focused
SDK test task at that same milestone gate before or with `dist`. Redirect verbose
output to logs and record only summaries.

Native smoke tests may run only at the end of related milestones and once at the
end of the complete sequence. Do not deploy/run a native smoke between logical
commits inside a milestone.

Between milestone gates, validation is limited to non-build checks such as
focused source inspection, test-source/static validation, copyright validation,
`git diff --check`, and generated/native-method table consistency checks that do
not compile targets.

## Commit Policy

Commits are required and must be frequent, focused, and logical. For every commit:

1. read the active state file;
2. inspect only scoped changes with `git status --short -- <paths>` and scoped
   diffs;
3. run the `validate-headers` skill on changed first-party files;
4. stage only intended paths;
5. run `git diff --check --cached`;
6. commit using the `logical-commits` skill and repository message format;
7. validate the commit message using the skill's local Python check;
8. rewrite the active state file with the commit hash, focused validation, and
   next action.

Do not amend or rewrite history. Do not push unless explicitly requested.
Builds deferred by the milestone budget must be named in the commit body/state as
`deferred to milestone gate by plan policy`, not silently omitted.

## New-File Size Rule

Every new source, test, plan-support, state, evidence, archive, or report file
created by this work must remain below 20 KB and approximately 600 lines. Split a
new file by cohesive responsibility before either limit is crossed.

Existing files are exempt from this size threshold. Do not refactor or split an
existing file merely because it already exceeds 20 KB or 600 lines.

## Token-Efficient Resume Protocol

Each ExecPlan owns a state file and append-only compact evidence file. On resume:

1. read the current plan's state file first;
2. read only the active milestone/slice in the current plan;
3. inspect only paths named by state;
4. consult this roadmap only when a fixed architectural invariant is needed;
5. do not reread completed plans, broad diffs, raw logs, or append-only evidence
   unless the state names a specific unresolved item.

At a logical commit, update state only. At a milestone boundary, update Progress,
evidence summary, and the editorial report once. Keep full logs outside tracked
plan files and record only their paths/status.

## Final Acceptance for the Series

The full sequence is complete only when all of the following are true on the same
final revision:

- `Image` has no canonical `int[] pixels` or `pixelsOfAllFrames` field;
- normal deployed Skia decoded/generated images do not allocate Java storage
  proportional to pixel count;
- native decoded content is owned directly by a `NativeImageBacking`;
- generated images draw through a native mutable surface;
- frame/crop/scale/smooth-scale/rotate operations execute through Skia without
  full Java rasters and avoid intermediate native rasters when composable;
- color operations use exact Skia/native backing execution with no Java
  full-image fallback;
- materialization barriers produce a native final result and encoding/readback
  observes all deferred operations;
- decode -> rotate -> color -> save PNG produces the transformed PNG;
- Java SE differential tests retain the supported legacy behavior;
- generated-image smoke behavior remains correct;
- the ImageControl/SideMenu repeated-resize regression no longer occurs when an
  encoded 500x500 header image is scaled to roughly 89x89 during reposition;
- the two-entry destination-scale cache and fractional content-scale behavior are
  preserved;
- no production correctness path relies on synchronized-method locking;
- final SDK build, macOS arm64 build, and final macOS native smoke pass;
- no forbidden platform build was run.
