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

## Current-master runtime result

The current-source SDK and ARM64 release bundle built successfully. The
PR 7 exact and marker-only APKs also compiled. The six bounded API 30 phases
did not reach the customer permission or filesystem operations: the app showed
an unhandled FileNotFoundException for
/data/user/0/totalcross.appzmtw/totalcross/res/android/floatingFrg.png in
Resources.uiStyleChanged / MainWindow.setUIStyle, followed by “Aborting
program.” MANAGE_EXTERNAL_STORAGE remained ignore in denied and grant-in-place
phases and was allow in pregranted phases. No permission prompt, customer
operation marker, or historical native crash signature was observed.

This is a blocked reproduction, not evidence that the graphics defect is
absent. The application never entered the Activity transition under test.
The current Android EGL and Skia source still matches the historical source in
the relevant paths. Build and emulator provenance are recorded in
evidence/M1-api30-run.md; the visible startup error is
evidence/M1-api30-startup-error.png.
