<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Milestone 2 — API 30 lifecycle diagnostics

## Scope and build

Added a default-off native diagnostic option,
`TC_ANDROID_EGL_LIFECYCLE_DIAGNOSTICS`, and the Gradle property
`tcAndroidEglLifecycleDiagnostics=true` to enable it for a diagnostic build.
Native events record a monotonic sequence, Linux thread ID, surface generation,
native-window/EGL identities, and current EGL state. Skia events record the
screen context, surface, and canvas identities. Java callback events use the
same log tag and record the Java thread ID, generation, and `Surface` identity;
they are enabled on the emulator with
`setprop log.tag.TC_EGL_LIFECYCLE INFO`.

The test package reused the M1 disposable TCUI asset overlay. No production
graphics behavior was changed. Android bundle SHA-256:
`92e8e3dbc2cc9083b4e7ac8c579300d3fdeb9b6e504cc707f76bba7278969f8d`.
The disposable SDK package SHA-256 is
`5069e89aa56f8f38afc8edda1021e4a62091d1d871cbceeea35b21b4ebac4fd4`.

## API 30 results

The focused trace APK grant-in-place phase reached the original storage
operation and recorded successful create/delete markers for `/sdcard/asd`.
On return from Settings, the same native event thread unbound and destroyed
the old EGL context and surface, released the prior native window, made a new
EGL context current, then entered `initSkia` and reset the old global Skia
surface. At `skia.surface.reset.begin`, `currentContext` was the replacement
EGL context while `skiaContext` still identified the old Skia context. The
reset-end event was absent. The process then received SIGSEGV. The tombstone
shows `GL2Encoder::s_glDrawRangeElements`, `GrDrawingManager::flush`,
`GrDirectContext::~GrDirectContext`, `SkSurface_Gpu::~SkSurface_Gpu`,
`destroySkiaScreen`, `initSkia`, and `initGLES`.

The paired trace pregranted control created and deleted `/sdcard/asd`, had no
permission prompt, and retained the same process without a crash. Its Skia
surface reset began and ended while the associated EGL context remained
current. This supports a context-mismatch explanation for the failing path;
Milestone 3 must still isolate teardown ordering before production changes.

An initial full harness run timed out on `adb shell input keyevent KEYCODE_BACK`
after the three exact-app phases, so its trace phases did not run. The focused
trace grant-in-place and pregranted phases were run directly afterward and
both completed their harness phase. The failing phase is an expected defect
reproduction, not a passing runtime test.

## Compact event evidence

`M2-api30-lifecycle-sequence.log` retains Java/native events for the failing
replacement, the changing native-window and EGL-context identities, the
old-Skia/new-EGL context mismatch, the fatal signal, and the tombstone stack.
The full diagnostic logcats, APKs, and build outputs remain outside Git under
`/var/folders/k8/02b7wfkd7fn32vtm3t5mwxwr0000gn/T/codex-android-skia-egl-m1-9h0ksa2a/results-m2-diagnostics`.

## Commands and validation

Passed:

- `cd TotalCrossVM/android && ANDROID_HOME=... ANDROID_SDK_ROOT=... ./gradlew --no-daemon -PtcAndroidEglLifecycleDiagnostics=true :app:bundleStandardRelease`
  — build successful in 1m09s, 89 actionable tasks (25 executed).
- `python3 tools/android_client_main_build.py --package <M2 diagnostic SDK package> --out <results-m2-diagnostics>`
  — exact and trace APKs built.
- Focused `run_phase(..., variant="trace", phase="grant-in-place", api_level=30, seconds=45)`
  — create/delete succeeded, followed by the reproduced SIGSEGV.
- Focused `run_phase(..., variant="trace", phase="granted-before-launch", api_level=30, seconds=45)`
  — create/delete succeeded and the process remained alive.

The initial all-phases harness invocation exited 1 because ADB timed out while
sending Back; this limitation is recorded above. No API 29/35/36, manual
rotation, standalone lifecycle, physical-device, CI, or performance tests were
run in this milestone.
