<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image native backing Plan 5 completion

Plan 5 and the complete five-plan image-native-backing sequence are complete.
The functional retirement commit is `b7337e93e`; the final gate ran against
that code revision before this documentation update.

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

The macOS arm64 final gate passed:

- CMake configure with `CMAKE_OSX_ARCHITECTURES=arm64` and build of `tcvm` and
  `Launcher` — passed; logs are `/tmp/image-native-backing-05-final-arm64-cmake.log`
  and `/tmp/image-native-backing-05-final-arm64-build.log`.
- Focused Image/ABI SDK tests — passed; log is
  `/tmp/image-native-backing-05-final-focused-tests.log`.
- `./gradlew-agent dist` — passed; compact/full logs are
  `TotalCrossSDK/agent-logs/20260902-223220-dist-agent.log` and
  `TotalCrossSDK/agent-logs/20260902-223220-dist-full.log`.
- Deployed `ImageLazyMaterializationSmokeApp` against the final dylib —
  passed with every required field true, `backingReadbackCount=2`, and
  `overallPass=true`; log is `/tmp/image-native-backing-05-final-smoke-arm64.log`.

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
