<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Image/rendering final reconstruction evidence index

Append one compact record per milestone validation or benchmark. Keep raw logs and samples at their artifact paths; do not duplicate full result tables here.

## Setup

- 2026-10-03: `git fetch origin`; remote `origin/master` was `5a44f503bf6fa1bec350f1218f4d501a70fc4812`, and the requested SHA is its ancestor. Created a clean worktree/branch at that exact SHA. The original checkout has unrelated local modifications and was left untouched.

## Milestone 1 — copyRect routing

- `TotalCrossSDK/./gradlew-agent test --tests totalcross.ui.image.ImageDestinationScaleTest --tests totalcross.ui.gfx.GraphicsDeferredImageTest --warning-mode=none --console=plain`: passed; two focused test classes, 10 s; summary `TotalCrossSDK/agent-logs/20261003-213922-test-agent.log`.
- `cmake -S TotalCrossVM -B build/image-rendering-final-macos -DCMAKE_BUILD_TYPE=Release -G Ninja -DTC_BUILD_NATIVE_STARTUP_TEST=ON -DTC_ENABLE_RUNTIME_DIAGNOSTICS=OFF -DTCVM_DEPOT_TOOLS_DIR=<existing local depot-tools>`; `ninja -C build/image-rendering-final-macos skia_surface_test`; `build/image-rendering-final-macos/skia_surface_test`: passed, including physical-copy hit/miss behavior and pixel checks; logs `/tmp/image-final-copyrect-cmake.log`, `/tmp/image-final-copyrect-ninja.log`, `/tmp/image-final-copyrect-skia-test.log`.
- `python3 scripts/validate-copyright-headers.sh --files <six changed production/test files>` and `git diff --check`: passed; 6 files validated, 0 header changes.

## Milestone 2 — typed opaque writePixels

- Commit `f45b0a1f9` (`fix(image): restore typed opaque raster writes`) reconnects the typed policy through deferred geometry and ordinary native image draws. It requires a proven-opaque compatible software raster, exact integral device 1:1 mapping, safe rectangular clipping, readable non-overlapping pixels, and a successful write; failed or ineligible writes use the existing Skia draw fallback. Unknown opacity proofs are keyed to backing generation. Diagnostics add separate attempt/hit/fallback metrics.
- `cd TotalCrossSDK && ./gradlew-agent test --tests totalcross.ui.image.ImageDestinationScaleTest --tests totalcross.sys.RuntimeDiagnosticsTest --warning-mode=none --console=plain`: passed in 5 s; 28 executed, 18 skipped across the two suites. Summary: `TotalCrossSDK/agent-logs/20261003-220636-test-agent.log`; full output: `TotalCrossSDK/agent-logs/20261003-220636-test-full.log`.
- `ninja -C build/image-rendering-final-macos tcvm skia_surface_test`: passed with runtime diagnostics OFF; log `/tmp/image-final-m2-ninja.log`. `build/image-rendering-final-macos/skia_surface_test`: passed, including opaque/clipped hits, deferred-plan hit, all ineligible/failure cases, and pixel parity; log `/tmp/image-final-m2-skia-test.log`.
- `python3 scripts/validate-copyright-headers.sh --files <19 changed source/test files>` and `git diff --check`: passed; 17 source files validated, 0 header changes after correcting the touched legacy test header.

## Milestone 3 — final-raster admission modes

- Commit `9896ec9ad` (`fix(image): separate final raster admission modes`) adds the internal typed `SECOND_OBSERVATION`/`IMMEDIATE` choice. Generic resolution retains second-use admission; immediate mode replaces the same single exact-scale slot.
- `cd TotalCrossSDK && ./gradlew-agent test --tests totalcross.ui.image.ImageRasterAdmissionModeTest --warning-mode=none --console=plain`: passed, 2 tests in 4 s. Summary: `TotalCrossSDK/agent-logs/20261003-223008-test-agent.log`; full log: `TotalCrossSDK/agent-logs/20261003-223008-test-full.log`.
- Focused copyright validation (3 files) and `git diff --cached --check`: passed.

## Milestone 4 — persistent UI ownership

- Commit `c7bf28d09` (`fix(image): retain final rasters for attached controls`) tracks distinct images owned by attached `ImageControl`s across replacement, fit-image changes, reparenting, removal, and ScrollContainer bag removal. Shared controls keep the pipeline cache alive; the last owner releases the final-raster slot. A weak owner registry handles controls collected with an unreachable parent tree without adding fields to `Image`, whose native field offsets are fixed.
- Focused SDK validation passed 74 tests across `ImageRasterAdmissionModeTest`, `ImagePersistentRasterAdmissionTest`, `ImageDestinationScaleTest`, `ImageAsyncPreparationTest`, `ImageDeferredColorMutationTest`, `ImageDeferredFrameStateTest`, and `ScrollContainerDisplayPreparationTest`. Summary: `TotalCrossSDK/agent-logs/20261003-223518-test-agent.log`; full log: `TotalCrossSDK/agent-logs/20261003-223518-test-full.log`.
- `python3 scripts/validate-copyright-headers.sh --files <9 changed source/test files>` and `git diff --check`: passed; 9 files validated, 0 header changes. After the weak-owner registry follow-up, the three subsequently edited Java files were revalidated, also with 0 changes. Native `TARGET_COLOR`/`PHYSICAL` implementation files were untouched; their regression remains in M5.

## Milestone 5 — integration

- Commit `2f81accfb` (`fix(image): keep owner registry deploy compatible`) removes the unsupported `WeakReference(Object, ReferenceQueue)` constructor and sweeps collected weak owners during registry access. It also keeps the converter-sensitive registry paths in small methods. `tc.Deploy` then accepted the full SDK UI artifact.
- The focused integration selection passed 317 tests across 42 suites (20 skipped): `cd TotalCrossSDK && ./gradlew-agent test --tests 'totalcross.ui.image.*Test' --tests totalcross.ui.gfx.GraphicsDeferredImageTest --tests 'totalcross.ui.ScrollContainer*Test' --tests 'totalcross.ui.ScrollRasterReuse*Test' --tests totalcross.sys.RuntimeDiagnosticsTest --tests 'totalcross.sys.runtime.ImageRuntimeConfiguration*Test' --tests 'totalcross.sys.runtime.RuntimeConfiguration*Test' --tests tc.tools.ArtifactBoundariesTest --tests tc.tools.converter.ImageFieldAbiTest --tests 'tc.tools.converter.NativeImage*Test' --tests tc.tools.converter.GraphicsRasterWriteConverterTest --tests tc.tools.converter.EncodedImageSourceConverterTest --tests tc.tools.converter.RuntimeDiagnosticsConverterTest --tests tc.simulator.RuntimeConfigurationSimulatorTest --tests totalcross.LauncherPreviewSurfaceTest --warning-mode=none --console=plain`. Summary/full logs: `TotalCrossSDK/agent-logs/20261003-224413-test-agent.log` and `...-test-full.log`.
- `cd TotalCrossSDK && ./gradlew-agent dist -x test --warning-mode=none --console=plain`: passed in 16 s; summary/full logs `TotalCrossSDK/agent-logs/20261003-224340-dist-agent.log` and `...-dist-full.log`.
- Skia Release `tcvm`/Launcher targets and `skia_surface_test` passed with `TC_ENABLE_RUNTIME_DIAGNOSTICS=OFF`; surface output confirmed physical identity and surface copy assertions. Logs: `/tmp/image-final-m5-native-build.log` and `/tmp/image-final-m5-skia-surface.log`.
- The native scroll smoke requires the legacy software renderer. The first Skia-backed invocation reported `NATIVE_MOVE_UNAVAILABLE` with `openGl=true`, matching the documented conservative Skia fallback. A separate diagnostics-OFF legacy Release configuration then built `tcvm` and Launcher (117 Ninja steps), and `runScrollRasterReuseSmokeMacOS` passed with `productionScrollReused=true`, both directions, fallback recovery, and `nativePrimitive=true`. Build logs: `/tmp/image-final-m5-native-legacy-configure.log`, `/tmp/image-final-m5-native-legacy-build.log`; smoke summary/full logs: `TotalCrossSDK/agent-logs/20261003-224659-runScrollRasterReuseSmokeMacOS-agent.log` and `...-full.log`.
- Eight deployed Skia image smokes passed: scroll copy fast path, warm path, raster core/opaque write, draw presentation state, lazy JPEG, physical variants, async preparation, and PNG async preparation. Each emitted `overallPass=true` or its required pass marker. Summary/full logs: `TotalCrossSDK/agent-logs/20261003-224829-runImageScrollRasterFastPathSmokeMacOS-agent.log` and `...-full.log`.
- `python3 scripts/validate-copyright-headers.sh --files TotalCrossSDK/src/main/java/totalcross/ui/image/ImageDrawingBridge.java` and `git diff --check`: passed. Other platform builds and P12 probes remain deferred as instructed.
