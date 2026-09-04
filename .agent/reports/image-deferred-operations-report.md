<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Deferred Image operations report

## Result

The deferred Image operation families are complete. Encoded sources continue
to be captured and structurally validated eagerly, while deferred receivers
represent color mutations, cropping, explicit frame extraction, frame state,
and frame layout as immutable pipeline operations until a pixel barrier or
destination draw requires materialization.

The public Image API, field layout, native bridge ABI, eager behavior of
materialized Images, and established exception and alias contracts remain
compatible. The implementation is limited to the existing lazy-image
pipeline and does not add a public deferred-operation API.

## Delivered architecture

`ImagePipeline` is an immutable linked chain rooted in an encoded or detached
raster source. Nodes contain only operation kinds, primitive parameters, and
metadata for logical dimensions, physical dimensions, content scale, frame
count, and full-strip width. Derived Images share immutable source objects and
never retain a path, stream, caller array, mutable Image, or borrowed native
buffer.

Canonical barriers resolve at scale one and adopt an ordinary materialized
Image. Destination drawing may resolve a separate variant at the destination
content scale without mutating the original deferred Image. Geometric nodes
use checked ceiling arithmetic for physical bounds; color-only nodes preserve
their input scale and exact operation order. Hardware presentation scale and
alpha state remain presentation metadata rather than raster density.

Resolved variants are bounded to two exact-scale entries per pipeline leaf,
with LRU replacement and texture-only release on eviction. Failed resolutions
are not cached, and the shared encoded source remains usable after cache
eviction or retryable allocation/resource failures.

## Functional behavior

Deferred `applyColor`, `applyColor2`, `changeColors`, `applyFade`, and
`setTransparentColor` append scale-neutral nodes. Resolution reuses the
existing eager JavaSE or native kernels on detached materialized variants and
invalidates stale cached variants. Materialized receivers still execute their
historical eager paths.

Deferred multi-frame color operations preserve the established quirks:
`applyColor`, `applyColor2`, and `changeColors` reset to frame zero, while fade
and transparent-color operations preserve their current-frame contract. Fade
nodes capture call-time frame ordering, including chains with multiple fade
operations. Color and fade nodes never move across geometric nodes.

`getClippedInstance` and `getFrameInstance` append immutable crop and explicit
frame-selection nodes. Materialized sources are detached at the operation
boundary, and deferred sources retain the existing immutable chain. Later
source pixel changes, frame changes, mutations, and metadata changes cannot
alter the derived result. Crop geometry preserves eager-baseline behavior at
natural, HiDPI, and fractional content scales, including compatibility paths
for alpha and hardware presentation fields.

Deferred `setCurrentFrame`, `nextFrame`, and `prevFrame` update presentation
state without adding nodes or cache keys. Resolved full-strip variants update
the visible frame at use time, and canonical barriers adopt the selected frame.
Deferred `setFrameCount` appends a scale-neutral `FRAME_LAYOUT` node that
preserves legacy metadata, integer truncation, logical dimensions, physical
frame widths, full-strip data, and retryable allocation behavior. The
zero-width frame-count case remains representable. `CROP` followed by
`FRAME_LAYOUT` retains physical full-strip width and correct frame pixels at
HiDPI and fractional scales.

## Problems addressed

- Eager mutation and crop paths allocated intermediate rasters for deferred
  receivers; they now use immutable nodes while preserving eager behavior for
  materialized receivers.
- Cached full-strip variants could expose stale visible-frame pixels after a
  frame change; presentation state is now synchronized when a variant is used.
- Frame layout could derive physical widths from rounded values and lose
  logical or full-strip metadata; layout retains both representations and uses
  checked ceiling bounds.
- Crop followed by frame layout could lose the preceding physical full-strip
  width; the composition now carries that width through resolution.
- Fade resolution could observe a later frame instead of the frame visible at
  the call site; each fade node preserves call-time frame ordering.
- Fractional destination scales exposed ceil-sensitive errors; the coverage
  includes exact 1.1, 1.25, and 1.75 cases for frame and cropped paths.

## Decisions and alternatives

The design keeps source capture eager and pixel decode lazy, uses immutable
nodes rather than mutable command objects, and keeps caches local and bounded.
Current-frame state is presentation state rather than a pipeline node or cache
key. Canonical barriers remain scale-one adoption points, and native rendering
continues to receive only ordinary materialized Images.

The following alternatives were rejected because they would change semantics
or lifetime guarantees: eager drawing for every crop or mutation, mutable or
globally shared pipeline nodes, a global decoded-raster cache, native traversal
of Java pipeline nodes, frame state embedded in cache keys, operation
reordering/fusion, and broad reduced-resolution decoding. Sampling-policy
changes, regional JPEG crop decode, selective GIF decode, GPU command
buffering, and arbitrary Graphics deferral remain separate work.

## Validation

The consolidated tree passed the following gates:

- focused Java tests for deferred color mutation, crop, explicit frame
  extraction, and frame state/layout;
- final `totalcross.ui.image.*Image*` tests;
- SDK `dist -x test` distribution build;
- fresh macOS Release CMake/Ninja `tcvm` build;
- encoded-source, lazy-materialization, color-mutation, crop/frame,
  frame-state, and Image ABI macOS smokes against the exact native build;
- copyright-header validation for changed first-party files;
- `git diff --check`;
- exact fractional-scale coverage for frame selection and crop-to-layout,
  including ceil-sensitive physical bounds.

All required smoke fields passed with `overallPass=true`. The functional SDK,
tests, smoke applications, and build wiring are preserved; only the planning
and handoff material is consolidated into the final plan and this report.

## Limitations

Android, iOS, Linux, Windows, packaging, and a complete platform matrix were
not rerun as part of this local acceptance gate. The supported behavior is the
existing JavaSE and macOS-validated lazy pipeline contract; cross-platform
release claims require the corresponding platform builds and packaging jobs.
