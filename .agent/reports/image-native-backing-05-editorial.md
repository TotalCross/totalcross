<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image native backing Plan 5 completion

Plan 5 and the complete five-plan image-native-backing sequence are complete.
The historical functional retirement commit is `b7337e93e`; the corrective
FRAME_LAYOUT commit is `ee0fbe23a`. The structural split commit is
`4d9ea4dd7`, and the final gate below ran against that current code revision.

## Architecture delivered

`Image` now has one canonical `ImageBacking`: `NativeImageBacking` owns the
opaque Skia handle on deployed builds, while `RasterImageBacking` preserves
the live Java raster contract on Java SE and non-Skia paths. The final object
layout reuses slot 0 for `backing`, keeps slot 1 reserved for the legacy
multi-frame object position, and preserves slots 2 through 8.

Native readback, RGBA rows, PNG/JPG/PDB encoding input, equality, hashing,
frame selection, and lifecycle paths observe the backing directly. Deployed
`getPixels()` produces a detached compatibility snapshot; Java SE retains live
raster behavior. Equality ignores alpha as before, and large-image hashing
continues to use the bounded scaled reduction.

The corrective FRAME_LAYOUT path preserves integer-truncated canonical storage
while applying destination-scale dimensions to visible frames. Pure layouts
reuse their complete native strip, and transformed non-divisible strips retain
residual storage pixels through readback and PNG round-trip. Zero-width frame
metadata remains valid without allocating a zero-sized surface.

Geometry compilation and direct drawing remain in `skia_image_geometry.cpp`.
Materialization-specific sizing, surface allocation, prefix rendering, and
backing registration are now in `skia_image_geometry_materialize.cpp`; the
private sharing surface is `skia_image_geometry_internal.h`. Their sizes are
16,314/365, 5,964/125, and 1,040/33 bytes/lines respectively.

All native consumers use explicit `RasterImageBacking_*` or
`NativeImageBacking_*` accessors. The old `Image_pixels` and
`Image_pixelsOfAllFrames` macros/usages are retired. Native destruction and
invalid-handle checks remain idempotent and do not depend on Java monitor
locking.

## Operation routing

Geometry and composable drawing continue through draw-time Skia plans where
possible. Exact color/alpha/key operations and readback/encoding barriers use
the native backing pass; Java raster loops remain intentionally only in the
Java SE/non-Skia compatibility implementation. Native frame metadata keeps
the visible-row reads bounded to the selected frame.

## Final proof

The macOS arm64 final gate passed at `4d9ea4dd7`:

- CMake configure with `CMAKE_OSX_ARCHITECTURES=arm64` and build of `tcvm` and
  `Launcher` — passed; logs are `/tmp/image-native-backing-geometry-split-final-arm64-cmake.log`
  and `/tmp/image-native-backing-geometry-split-final-arm64-build.log`.
- Deployed `ImageDeferredFrameStateSmokeApp` — passed with
  `frameLayoutScaledResidual=true`, `frameLayoutResidualRoundTrip=true`, and
  `overallPass=true`; log is
  `/tmp/image-native-backing-geometry-split-final-frame-state-smoke.log`.
- Deployed `ImageNativeGeometrySmokeApp` and
  `ImageNativeMaterializationSmokeApp` — both passed; logs are
  `/tmp/image-native-backing-geometry-split-final-geometry-run.log` and
  `/tmp/image-native-backing-geometry-split-final-materialization-run.log`.
- Deployed `ImageLazyMaterializationSmokeApp` against the final dylib — passed
  with every required field true, `backingReadbackCount=2`, and
  `overallPass=true`; log is
  `/tmp/image-native-backing-geometry-split-final-lazy-smoke.log`.

The structural-only commit reused the already-built unchanged SDK/Java smoke
artifacts and injected the newly built arm64 dylib into each deployed smoke.

The smoke covers decoded PNG alpha, targeted JPEG dimensions, generated
draw/save, repeated 500x500 ImageControl resize, crop/smooth/rotate drawing,
decode/rotate/applyColor/save, current-frame fade, exact color-key save/readback,
two-entry destination-scale cache reuse, and detached `getPixels()` behavior.
The test-only allocation hook observed zero backing readbacks during normal
creation/decode and two readbacks for the two explicit detached snapshots; it
does not introduce a profiler or a production allocation counter.

Android, iOS, Linux, Windows, and the full platform matrix were not run under
the fixed Plan 5 validation policy. Unrelated local edits and generated or
downloaded artifacts were not staged or committed.
