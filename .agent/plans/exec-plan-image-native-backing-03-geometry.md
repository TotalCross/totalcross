<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda
SPDX-License-Identifier: LGPL-2.1-only
-->

# Execute geometric Image pipeline operations through Skia

This ExecPlan follows `AGENTS.md`, `.agent/PLANS.md`, the repository
`logical-commits` skill, and
`.agent/image-native-backing-roadmap.md`. Execute it only after plan 2 passes.

## Purpose / Big Picture

Stop materializing intermediate rasters for geometric Image operations on native
Skia. Frame selection/layout, crop, scale, smooth-scale, and rotate/scale remain
semantic pipeline nodes and are compiled into source rectangles, transforms, and
sampling at draw time. APIs that require concrete output render the composed
pipeline once into a native backing.

## Working Set and Resume Protocol

Use `.agent/state/image-native-backing-03.md` first and compact evidence
`.agent/evidence/image-native-backing-03.jsonl`. Inspect only the active Image
pipeline, native Skia backing/executor, Graphics bridge, and focused tests.

New files stay below 20 KB/~600 lines. If a native executor needs more than one
file, split by geometry compilation versus materialization; do not create a
single oversized dispatcher.

## Progress

- [x] Milestone 1: add a native/draw execution description for geometry.
- [x] Milestone 2: migrate frame/crop/scale operations.
- [ ] Milestone 3: migrate rotate/scale and materialization barriers.
- [ ] Close plan 3 and prepare plan 4 state.

## Current Architecture and Scope

Plan 2 made native backing authoritative for decoded/generated Images on deployed
Skia. `ImagePipeline` still records operations and destination-scale behavior.
Current resolution may still call eager Java/native algorithms that allocate
intermediate Image rasters.

This plan changes only geometric operations:

    FRAME_SELECT
    FRAME_LAYOUT
    CROP
    SCALE
    SMOOTH_SCALE
    ROTATE_SCALE

Color nodes remain on the old resolver until plan 4.

## Plan of Work

### Milestone 1: Define and bridge the geometric draw plan

Add a compact package-private/native execution description that carries exactly
the information needed to draw a pipeline result:

- root backing/encoded source identity;
- source/frame rectangle;
- output logical and physical dimensions;
- destination content scale;
- transform matrix parameters;
- sampling mode;
- rotation fill color/output bounds;
- frame count/current frame where needed.

Do not expose Skia matrices or sampling classes to Java. Java may pass normalized
operation parameters to a native executor; C++ constructs Skia objects.

Keep pipeline order authoritative. Collapse adjacent geometric nodes only when
mathematically equivalent and order-preserving. Never reorder crop relative to
scale/rotation merely to optimize.

Preserve destination-scale resolution: a pipeline containing geometric nodes is
resolved for the destination scale, while non-geometric metadata keeps canonical
content scale behavior.

For encoded JPEG with an eligible first `SMOOTH_SCALE`, allow the existing
targeted decoder to produce a target-sized native root variant. Treat the first
smooth-scale as satisfied only when decoded physical dimensions match that node's
required dimensions; subsequent nodes remain in order.

Add focused differential tests for metadata and transform composition but defer
build execution to the milestone gate.

Commit checkpoint A for the execution-description/native bridge. Checkpoint B for
pipeline compiler if separate.

Milestone gate:

    cmake -S TotalCrossVM -B build-image-backing-03-m1 \
      -DCMAKE_BUILD_TYPE=Release \
      -DCMAKE_OSX_ARCHITECTURES=arm64 -G Ninja
    cmake --build build-image-backing-03-m1 --target tcvm Launcher --parallel

    cd TotalCrossSDK
    ./gradlew-agent <focused pipeline metadata tests>
    ./gradlew-agent dist -x test

No native smoke is required unless this milestone already changes actual draw
routing; if it does, run only the smallest geometry smoke after the gate.

Acceptance:

- geometry can be described without a destination raster;
- operation order/contentScale metadata remain exact;
- targeted JPEG eligibility remains unchanged;
- SDK/macOS gate passes.

### Milestone 2: Execute frame, crop, scale, and smooth-scale directly

Implement these nodes in the native draw executor.

`FRAME_SELECT`:

- select the frame by source-rectangle x offset;
- do not copy a frame into a separate visible-frame buffer;
- changing current frame is metadata-only for native backing.

`FRAME_LAYOUT`:

- preserve strip backing and frame metadata;
- preserve historical integer truncation and zero-width behavior;
- do not physically split the backing.

`CROP`:

- represent crop as a source subset/rect;
- preserve current logical-to-physical edge rounding and fractional scale rules;
- never promote crop to destination-aware geometry by itself when current
  semantics say it is not.

`SCALE`:

- use nearest-neighbor sampling;
- preserve exact output dimensions/metadata;
- no full-size scaled backing is created for ordinary screen draw.

`SMOOTH_SCALE`:

- use Catmull-Rom-equivalent cubic sampling supported by pinned Skia;
- do not substitute bilinear/`kLow` filtering as the intended algorithm;
- preserve targeted JPEG shortcut where eligible.

For a pipeline composed solely of the above operations, direct screen draw must
consume the root native backing plus source/destination parameters in one Skia
draw when possible.

When a barrier requires materialization, allocate exactly one final-sized native
surface and draw the composed result into it. The resulting `Image` adopts a
native immutable snapshot.

Add differential cases for 1.0, 1.1, 1.25, 1.75 content scales and ceil-sensitive
widths, crop edges, multi-frame selection, nearest versus smooth sampling, and
repeated destination-scale draws/cache reuse.

Commit checkpoint C for frame/crop. Checkpoint D for scaling/sampling. Checkpoint
E for barrier adoption/cache if needed.

Milestone gate:

    cmake -S TotalCrossVM -B build-image-backing-03-m2 \
      -DCMAKE_BUILD_TYPE=Release \
      -DCMAKE_OSX_ARCHITECTURES=arm64 -G Ninja
    cmake --build build-image-backing-03-m2 --target tcvm Launcher --parallel

    cd TotalCrossSDK
    ./gradlew-agent <focused scale/crop/frame tests>
    ./gradlew-agent dist -x test

Run native smoke only after the gate. Include the 500x500 -> ~89x89 ImageControl
smooth-scale/reposition regression and a crop/frame case.

Acceptance:

- frame/crop/scale/smooth-scale no longer require intermediate Java/native Image
  rasters for direct draw;
- one final native raster is used when a barrier requires a concrete result;
- sampling and fractional-scale differential tests pass;
- repeated resize regression is stable.

### Milestone 3: Execute rotate/scale directly and preserve save semantics

Implement `ROTATE_SCALE` with Skia transforms while preserving existing
`rotatedDimensions`, percentage scaling, angle normalization, square-image
special behavior, fill color, alpha, frame handling, and output metadata.

For direct draw:

- establish the pipeline output bounds;
- apply required fill color inside those bounds;
- map source coordinates through the composed scale/rotation matrix;
- apply clipping at the destination;
- draw the source frame/subset with the required sampling.

For multiple frames, execute each selected frame using its source offset; do not
copy visible pixels.

For materialization:

- allocate one final output-sized `SkSurface`;
- clear/fill it according to rotation fill semantics;
- render the fully composed geometry pipeline once;
- snapshot/adopt as `NativeImageBacking`.

Prove the required persistence scenario with a smoke/test fixture:

    open PNG -> rotate -> save PNG -> inspect dimensions and deterministic pixels

Do not change encoder format in this milestone; saving must observe the native
materialized result through existing row/output barriers.

Commit checkpoint F for rotate executor. Checkpoint G for rotate->save regression
fixture/barrier fixes.

Milestone gate:

    cmake -S TotalCrossVM -B build-image-backing-03-m3 \
      -DCMAKE_BUILD_TYPE=Release \
      -DCMAKE_OSX_ARCHITECTURES=arm64 -G Ninja
    cmake --build build-image-backing-03-m3 --target tcvm Launcher --parallel

    cd TotalCrossSDK
    ./gradlew-agent <focused rotate/materialization tests>
    ./gradlew-agent dist -x test

Run rotate->save native smoke only after the gate.

Acceptance:

- rotate/scale direct draw does not create an intermediate raster;
- rotation barriers create only the final native result;
- PNG saved after rotation contains the rotated result;
- fill color, dimensions, alpha, frames, and fractional scale remain compatible;
- SDK/macOS gate and smoke pass.

## Surprises & Discoveries

Record only execution facts that affect plan 4. Do not turn sampling differences
into an unplanned algorithm redesign; compatibility failures must be fixed within
the semantics above.

## Decision Log

- Decision: geometry remains semantic pipeline state and is compiled at draw.
- Decision: nearest sampling is required for SCALE; Catmull-Rom-equivalent cubic
  is required for SMOOTH_SCALE.
- Decision: frame selection is a source offset, never a native/Java frame copy.
- Decision: barriers rasterize the composed pipeline once at final dimensions.

## Validation and Acceptance

Only SDK and macOS builds at milestone gates. Native smoke only after related
milestone gates. Use logical commits with header validation and staged diff checks
for all slices.

## Risks and Open Questions

No design alternatives are open. If pinned Skia API spelling differs, adapt to
the equivalent API while keeping required sampling semantics. If the pinned build
cannot express Catmull-Rom directly, implement the equivalent native Skia sampling
path; do not silently downgrade quality or return to Java arrays.

## Idempotence and Recovery

Materialization failure must leave the original deferred pipeline/backing valid
and retryable for resource failures. Do not clear pipeline/cache until the final
native backing is successfully created. Reuse milestone build directories unless
stale configuration is proven.

## Outcomes & Retrospective

Milestones 1 and 2 are complete at implementation commit `04a7bfa0a`.
`ImageGeometryPlan` now preserves source-to-result operation order and carries
the native root backing, frame metadata, content scales, alpha masks, and
hardware scales. The Skia executor handles frame selection/layout, crop,
nearest scale, Catmull-Rom smooth scale, and the currently exercised rotation
path for direct draw and one final native materialization.

The milestone-2 crop/frame smoke passes encoded multi-frame extraction,
current-frame capture, fractional content scales, alpha masks, non-unit
hardware scales, targeted JPEG behavior, and the existing 500x500 to 89x89
ImageControl materialization/reposition regression. The dedicated geometry
smoke passes nearest, crop, smooth, chained scale/crop, rotation, and frame
selection. Exact commands and logs are recorded in
`.agent/evidence/image-native-backing-03.jsonl`.

The broad legacy lazy/ABI smokes remain deferred: they still assert mutable or
identity-preserving `getPixels()` arrays and exercise color-native mutation
paths that belong to plan 4. Android, iOS, Linux, Windows, and the full
platform matrix remain deferred by roadmap scope. Plan 3 stays open at
milestone 3 for a later execution turn; this checkpoint intentionally stops
before that milestone.

## Revision Note

This plan covers geometry only. Keep color/mutation work in plan 4.
