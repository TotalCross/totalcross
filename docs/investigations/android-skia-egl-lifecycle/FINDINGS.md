<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Findings

## Historical evidence

The completed I03 investigation tested the official TotalCross 7.3.0 Android
ARM64 runtime on local ARM64 emulators. It reported native SIGSEGV crashes on
API 30, 35, and 36 across denied and grant-in-place permission flows. API 29
did not show that native crash, but its legacy write-permission behavior
prevented the customer file operation from succeeding after an interactive
grant. Pregranted controls completed the file operation.

The shared native stack included GL2Encoder::s_glDrawRangeElements,
FillRectOp::onExecute, GrDirectContext::~GrDirectContext,
SkGpuDevice::~SkGpuDevice, initSkia, initGLES, and
Java_totalcross_Launcher4A_nativeSurfaceChanged. The exact cause was left
open because EGL and Skia identities and destruction-time context state were
not recorded.

References:

- Issue 563: https://github.com/TotalCross/totalcross/issues/563
- I03 report:
  https://github.com/TotalCross/totalcross-performance-lab/blob/investigation/totalcross-7.3.0-functional-validation/investigations/7.3.0-functional/reports/I03-android-egl-lifecycle.md
- Customer reproducer PR 7:
  https://github.com/TotalCross/totalcross-performance-lab/pull/7
- Historical reproduction run:
  https://github.com/TotalCross/totalcross-performance-lab/actions/runs/43113450337
- Historical TotalCross source revision: 5464c21e3107e3f1ce9c287b724829e2bb870c1d.

These are historical results from 7.3.0 and do not establish current-master
behavior.

## Current source inspection

The inspected baseline is 52a947b5295321d7350350bd618c55fb26304c00. The
current SDK build declares version 7.2.2.

Compared with the historical 5464c21e revision, skia.cpp is unchanged. The
Android EGL code in gfx_Graphics_c.h is also unchanged; the only file diff is
an iOS-only surface-change query helper.

The current source sequence on native surface replacement is:

1. androidApplySurfaceChange obtains the replacement ANativeWindow.
2. If graphics were initialized, androidDestroyEGL unbinds and destroys the
   old EGL context, surface, and display.
3. The old ANativeWindow is released and the replacement is stored.
4. initGLES creates and makes the replacement EGL context current.
5. initGLES calls initSkia.
6. initSkia first calls destroySkiaScreen, which resets the global Skia
   surface and canvas references.

This sequence matches the historical hypothesis but does not show whether the
old context was valid or current at Skia resource destruction.

## Current-baseline runtime result

The current-source SDK and Android ARM64 runtime were built from baseline
52a947b5295321d7350350bd618c55fb26304c00. The PR 7 exact customer source and
marker-only variant were deployed to the installed API 30 ARM64 emulator.

The first attempt stopped before permission handling because the Android UI
resource path lacks floatingFrg.png and outlined_edit.png. Both files exist in
the same source tree's Material resource folder. For the reproduction only,
those two assets were added under the expected Android names while generating
a disposable TCUI.tcz; the temporary source aliases were removed immediately.
The customer Java source and production graphics code were unchanged. This
overlay is test provenance, not a production resource correction.

All six bounded phases then reached permission handling. In the trace
grant-in-place phase, MANAGE_EXTERNAL_STORAGE changed from ignore to allow,
the customer operation recorded successful create and delete markers, and the
process then received SIGSEGV while returning through
Java_totalcross_Launcher4A_nativeSurfaceChanged. The unmodified exact APK's
grant-in-place logcat also contains Fatal signal 11 immediately after
`initSkia`. The marker-only tombstone contains
GL2Encoder::s_glDrawRangeElements and the Skia cleanup frames seen historically.
The pregranted trace control created/deleted the directory and stayed alive;
the denied trace reported the expected Operation not permitted error.

The automation returned failure because the grant-in-place trace process did
not remain alive. That is the reproduced defect. The exact variant's summary
sampled the original PID as still present, but its logcat contains a fatal
signal for that PID; process liveness alone is not a valid pass criterion.
This confirms that the defect remains on the recorded current-source baseline
and that the Settings return reaches the failing surface callback. It does not
prove why Skia cleanup issues graphics work in the observed state.

The full assertions, provenance, native stack excerpt, screenshot, and command
details are in evidence/M1-api30-reproduction.md. The initial startup-blocked
attempt remains recorded in evidence/M1-api30-run.md and
evidence/M1-api30-startup-error.png.
