<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Define deferred Image operations

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`.

## Purpose / Big Picture

Extend the existing lazy-image pipeline so that common Image mutations and
frame operations remain declarative until a pixel barrier is required. Image
construction continues to snapshot and validate encoded sources eagerly, while
pixel decode and deferred transformations happen only when pixels are needed.

The observable result is that an Image loaded from an encoded source can be
color-adjusted, cropped, reduced to a frame, laid out as a multi-frame strip,
or drawn at a destination scale without allocating an eager intermediate for
each call. Existing eager behavior remains the compatibility reference for
already-materialized Images and for operations whose public semantics depend
on immediate pixels.

The scope has three functional families:

1. Color mutations: `applyColor`, `applyColor2`, `changeColors`, `applyFade`,
   and `setTransparentColor`.
2. Crop and explicit frame extraction: `getClippedInstance` and
   `getFrameInstance`, including call-time source snapshots and presentation
   scale behavior.
3. Frame state and layout: `setCurrentFrame`, `nextFrame`, `prevFrame`,
   `setFrameCount`, cached variant synchronization, and `FRAME_LAYOUT`.

## Progress

- [ ] Define the immutable deferred operation model and scale invariants.
- [ ] Define color mutation compatibility, including multi-frame behavior.
- [ ] Define crop, clipping, explicit frame extraction, and snapshot rules.
- [ ] Define frame selection, frame layout, cache synchronization, and barriers.
- [ ] Define focused tests, SDK distribution validation, and macOS smoke gates.

## Starting Architecture and Scope

The existing pipeline is a linked immutable chain rooted in either an encoded
source snapshot or a detached raster snapshot. A derived Image holds public
metadata and a private pipeline reference. Appending an operation creates a new
node; it never mutates an existing node or the shared source. The native drawing
layer receives only an ordinary materialized Image, so native Graphics code
does not need to understand pipeline nodes.

Encoded source I/O is eager. Paths, streams, and caller-owned byte ranges are
consumed or copied during construction. JavaSE retains an owned byte array;
deployed targets retain an independently owned native encoded bag. No path,
stream, VM-array address, or borrowed native buffer remains authoritative after
construction. Structural validation does not decode compressed pixels.

Canonical barriers resolve at content scale 1, adopt the resulting raster into
the receiver, release cached draw textures, drop cached variants, and clear the
pipeline. A drawing boundary may instead resolve a deferred pipeline for the
destination `Graphics.contentScale` without adopting that variant into the
original Image. Ordinary materialized Images are never regenerated merely
because a destination is high density.

## Deferred Node Contract

Operation nodes use stable integer kinds and primitive parameters. Their
metadata records logical dimensions, physical dimensions, frame count, full
strip width, and the source content scale needed to preserve legacy behavior.
Nodes are scale-neutral unless the operation explicitly carries physical
geometry derived from the preceding raster. They must not retain mutable Image
objects, streams, paths, global caches, or borrowed pixel buffers.

Geometric nodes resolve every geometric step at the requested destination
scale. Physical bounds use checked arithmetic and `ceil(logical * scale)`;
public logical bounds are never reconstructed from rounded physical bounds.
Color-only nodes preserve the scale of their input and execute in exact chain
order. Hardware presentation fields `hwScaleW` and `hwScaleH` affect display
metadata, not deferred raster dimensions. `alphaMask` is synchronized onto a
resolved variant at the public Image boundary.

Each pipeline leaf may retain at most two exact destination-scale variants in
an LRU cache. A cache hit reuses the materialized variant. A miss resolves from
the immutable source, and an eviction releases only the evicted variant's
texture before dropping its Java reference. Failed resolutions are not cached;
deterministic source decode failures may be cached according to the existing
source failure contract. No source-level or global decoded-raster cache is
introduced.

## Color Mutations

When `pipeline != null`, the five mutations append immutable nodes and return
without crossing a pixel barrier. The leaf cache is invalidated so no variant
created before the mutation can be returned afterward. The resolver invokes the
existing eager implementation on a detached materialized copy, preserving the
established native and JavaSE kernels, alpha behavior, and exception timing.

When `pipeline == null`, each public method keeps the existing eager path and
the existing pixel-array alias behavior. This distinction is part of the
compatibility contract: deferral must not alter the behavior of an Image whose
pixels are already materialized.

The operation order is observable and must remain the linked-node order. Fade
and transparency do not create a density and therefore preserve their input
content scale. A fade node captures the current presentation frame at the time
the call is made, so later frame changes cannot reorder or retarget an earlier
fade. Multiple fade nodes retain their individual call-time frame semantics.

For deferred multi-frame images, `applyColor`, `applyColor2`, and
`changeColors` retain the historical reset-to-frame-zero behavior. Fade and
transparent-color operations retain their established current-frame contract.
The final resolved image must still expose the expected frame metadata and
selected visible pixels, including after canonical materialization.

## Crop and Explicit Frame Extraction

`getClippedInstance` appends a crop node rather than drawing immediately into a
new eager image. It validates positive dimensions and checked allocation
bounds. If the source is materialized, it first creates an immutable raster
snapshot containing the complete current frame state and presentation metadata.
If the source is already deferred, the existing immutable pipeline is extended.

`getFrameInstance(frame)` appends an explicit frame-selection node with the
selected frame index and the dimensions captured at call time. Both operations
are isolated from later changes to the source Image: source frame selection,
frame count, pixels, color mutations, and other presentation state cannot alter
the derived operation's snapshot.

Crop coordinates and physical bounds must preserve the eager crop baseline at
content scale 1, 1.5, 2, and other finite positive fractional scales. A
destination-aware crop following a geometric transform uses the transformed
physical raster; a natural-scale crop preserves the preceding source content
scale. Legacy `alphaMask` and `hwScaleW/H` behavior is retained, using the
legacy-compatible path whenever those fields make direct deferred cropping
non-equivalent.

The crop resolver must preserve the distinction between the physical full-strip
width and the logical visible frame width. This is required for crops that are
followed by frame layout or frame switching, and it prevents a fractional scale
from changing the selected pixels through integer truncation.

## Frame State and Layout

For a deferred Image, `setCurrentFrame`, `nextFrame`, and `prevFrame` update
presentation state without appending a node or changing a cache key. The
selected frame is applied to each resolved full-strip variant immediately
before it is used. Canonical barriers adopt the selected visible frame while
retaining the full-strip data and metadata required by the eager model.

`setFrameCount` on a deferred Image appends an immutable, scale-neutral
`FRAME_LAYOUT` node. It preserves legacy integer truncation, `FC=n` metadata,
logical dimensions, physical frame width, and the full-strip width. Allocation
failures are retryable and must not poison the shared encoded source. The
zero-width case where the requested frame count exceeds the full-strip width
remains representable with an empty visible buffer and intact full-strip data.

For `CROP -> FRAME_LAYOUT`, the layout node uses the preceding canonical raster
physical full-strip width. At 2x, 1.5x, and exact fractional scales, frame
selection, visible pixels, frame switching, metadata, and later canonical
materialization must agree with the eager barrier baseline. Cached variants
must synchronize the visible frame without copying stale frame pixels over the
full strip.

## Compatibility and Invariants

The following invariants are mandatory:

- Public method signatures, exceptions, Image field ordering, and native Image
  ABI remain unchanged.
- Materialized Images remain eager for mutations, crop, frame extraction, frame
  state, and layout.
- Deferred receivers use only immutable nodes and detached snapshots.
- Source snapshots are stable against path, stream, byte-array, and source-Image
  changes after the operation call.
- Canonical pixel, export, and native barriers resolve exactly once to a
  scale-one ordinary Image, subject to the existing retry/failure rules.
- Destination-scale drawing does not materialize or mutate the original
  deferred Image.
- Logical dimensions remain stable while physical dimensions follow the exact
  requested content scale with checked ceiling arithmetic.
- Color and fade operations do not move across geometric operations.
- Current-frame presentation state is not a pipeline operation or cache key.
- Cached variants and their textures have bounded lifetime and no global
  ownership.
- Retryable allocation, resource, and resolution failures are not cached, while
  deterministic content failures may follow the existing source failure
  contract. Texture release and variant clearing are idempotent and never
  release the shared encoded source.

## Plan of Work

### Milestone 1 — Defer color mutations

Add node kinds and deferred dispatch for all five color mutations. Extract or
reuse eager helpers so materialized behavior is unchanged. Invalidate affected
leaf variants, preserve operation order, and cover multi-frame reset/current
frame quirks. Add focused Java tests and the dedicated macOS smoke fixture,
including Gradle compile, deploy, and pass/fail checks.

Acceptance requires construction to remain lazy, each mutation to remain
deferred, equivalent eager/deferred pixels to match within the established
contract, cached variants to be invalidated, and conservative JPEG eligibility
to remain unchanged by color nodes.

### Milestone 2 — Defer crop and frame extraction

Add crop and explicit frame-selection nodes. Capture source pixels and frame
metadata at the operation boundary, preserve current-frame semantics, and
resolve physical crop bounds for natural, HiDPI, and fractional scales. Add
focused tests for snapshot isolation, metadata, deterministic pixels, frame
selection, legacy presentation fields, and source changes after the call. Add
and register the crop/frame macOS smoke.

Acceptance requires lazy crop and extraction, source isolation, eager-baseline
equivalence at tested scales, correct destination-scale behavior, and no
regression of the materialized path.

### Milestone 3 — Defer frame state and layout

Add deferred presentation updates, full-strip synchronization, canonical
selected-frame adoption, and the scale-neutral `FRAME_LAYOUT` node. Cover fade
ordering, cached variant switching, allocation retry, zero-width compatibility,
crop-to-layout composition, and exact fractional scale cases whose physical
bounds require ceiling.

Acceptance requires stable metadata and visible pixels before and after
canonical barriers, correct full-strip reuse, no stale cache synchronization,
and identical eager/deferred frame behavior within the documented tolerance.

## Validation and Acceptance

Use the smallest validation that proves each slice, then run the final Image
family and distribution checks. The focused SDK selectors must cover the three
deferred test classes plus the existing Image deferred suites. The final SDK
validation includes the relevant `totalcross.ui.image.*Image*` tests and
`./gradlew-agent dist -x test` from `TotalCrossSDK`.

Build a fresh macOS Release `tcvm` with CMake/Ninja from the final functional
tree. Run the color, crop/frame, frame-state, encoded-source, lazy-materialize,
and Image ABI smokes against that exact dylib. Each smoke must emit its
machine-readable required fields and `overallPass=true`; the ABI smoke must
continue to prove field/bridge ordering.

Run `python3 scripts/validate-copyright-headers.sh --files` for changed
first-party files and `git diff --check`. Do not claim Android, iOS, Linux,
Windows, packaging, or a full platform matrix without running those builds.
Those validations are deferred when the local environment or milestone scope
does not include them.

## Risks and Open Questions

Do not broaden public API, change native descriptors, teach native Graphics to
walk pipeline nodes, add asynchronous or global caching, or reorder operations
to improve optimization. Do not apply reduced-resolution decode to a path whose
operation ordering changes semantics. If a platform exposes a conflict between
legacy presentation fields and direct deferred geometry, preserve the eager
compatibility path and record the limitation rather than changing the public
contract.

The accepted reduced-JPEG similarity rule, if exercised by the existing lazy
pipeline, is tolerance-based rather than byte equality. Sampling policy and
other unrelated rendering changes remain outside this feature.
