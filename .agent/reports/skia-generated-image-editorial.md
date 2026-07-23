<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Editorial Report: Skia Generated Image

Issue 417 was reproduced from the attached `Tcsort.zip`: the Android
baseline exported the 576x576 `MonoImage` as a fully transparent PNG despite
the white fill and black inset border. The checked-in smoke test preserves
that exact `MonoImage`/`getGraphics()`/drawing/`createPng()` flow.

The fix gives generated images an owned `SkBitmap`/`SkCanvas` pair, promotes
the Java array only once, and routes drawing, pixel, RGB, row, image-to-image,
and PNG output through the authoritative Skia surface. Array-only transforms
such as scale, rotate, and color transforms remain outside this issue path.
Pixel conversion is explicit scalar ARGB, independent of host byte order.

## Final matrix

The corrected smoke passed on all three requested targets from one revision:

- Java SE: `java-byte-array`, checker passed, process exit 0.
- macOS arm64: `native-skia`, checker passed, process exit 0.
- Android emulator `emulator-5554`: `native-skia`, APK deployment and checker
  passed. The app was force-stopped after artifact collection to avoid the
  asynchronous Android loader teardown race documented in the smoke README.

The macOS headless Ninja configuration reports `Settings.platform=Linux` in
the JSON, although the executable and native library are macOS arm64. Java SE
also emitted a non-fatal optional telemetry `NoClassDefFoundError` for
`net.harawata.appdirs.AppDirsFactory`.

Final source sizes: `skia.cpp` 351 lines, `skia_surface.cpp` 233,
`skia_primitives.cpp` 279, `GraphicsPrimitives_c.h` 1,507,
`GraphicsPrimitivesSkia_c.h` 418, `Tcsort.java` 171, and `check_result.py`
65.

The Android blank baseline and all final artifact paths/hashes are recorded in
`.agent/evidence/skia-generated-image.jsonl`. No later milestone validations
were run.
