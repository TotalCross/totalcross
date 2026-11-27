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

## Milestone 2 lifecycle diagnostics

The diagnostic bundle was built from the same baseline with the default-off
`TC_ANDROID_EGL_LIFECYCLE_DIAGNOSTICS` option enabled through Gradle. Java and
native logs correlated on API 30 by surface generation, timestamp, and event
thread. The Java `Surface` identity remained constant across the permission
return while `ANativeWindow_fromSurface` returned a different native window.

In the focused trace grant-in-place phase, `/sdcard/asd` was created and
deleted successfully. On return from Settings, generation 5 replaced the
native window. Native sequence 52 unbound/destroyed the previous EGL context
`0xb4000071a832ce50`; sequence 63 made replacement context
`0xb4000071a834bf90` current. At sequence 68, `skia.surface.reset.begin`
reported that replacement context as current while the old Skia context
`0xb4000071b8313ef0` and old surface `0xb40000717830f190` were still tracked.
There was no reset-end event. The process received SIGSEGV 82 ms after reset
began.

The tombstone stack reaches `GL2Encoder::s_glDrawRangeElements`,
`GrDrawingManager::flush`, `GrDirectContext::~GrDirectContext`,
`SkSurface_Gpu::~SkSurface_Gpu`, `destroySkiaScreen`, `initSkia`, and
`initGLES`. It shows the old Skia surface's pending draw work being flushed as
that surface and its context are destroyed. This strongly implicates the
context mismatch; it does not replace the controlled M3 ordering comparison.

The paired trace pregranted control created/deleted `/sdcard/asd`, had no
permission prompt, completed its Skia reset with its EGL context current, and
kept the same process alive. The initial all-phases harness invocation timed
out sending Back through ADB after the exact-app phases. Focused trace phases
were run directly afterward; manual rotation and standalone background/
foreground remain untested.

Build/runtime artifact hashes, commands, environment, and compact identity and
stack records are in evidence/M2-api30-lifecycle-diagnostics.md and
evidence/M2-api30-lifecycle-sequence.log. Full logs and generated APKs remain
outside Git in the M2 results directory.

## Milestone 3 causal comparison

The isolated API 30 candidate called `destroySkiaScreen()` on the existing
graphics/event thread before destroying the still-current old EGL context.
It preserved surface generations, native window ownership, and replacement
context initialization. Candidate-only build switches and source edits were
removed after the experiment; the replayable patch is
`diagnostic-patches/M3-pre-egl-skia-cleanup-candidate.patch.gz`.

The M2 grant-in-place trace baseline crashed when old Skia cleanup started
after EGL replacement. With only the cleanup order changed, the candidate
passed the initial grant-in-place run plus five repetitions, denied-return,
pregranted, and one rotation/background/foreground control. Old-context Skia
reset ended before EGL teardown; replacement Skia initialization completed.
Grant and pregranted filesystem create/delete operations succeeded, denial
returned the expected `Operation not permitted`, the app stayed responsive,
and no original native crash appeared. The rendered customer surface is
preserved as `evidence/M3-api30-candidate-rendered.png`; the ordered events
and repetition summaries are in `evidence/M3-api30-candidate-sequence.log`.

This controlled API 30 ARM64 comparison confirms the EGL/Skia lifetime cause
for the exercised surface-recreation paths. It does not constitute production
implementation or cross-API validation. The missing `floatingFrg.png` and
`outlined_edit.png` resources remain an independent test-input issue handled
only by the disposable TCUI overlay. The M2 harness ADB Back timeout remains
an independent automation issue; M3 used focused runs and did not correct it.

The pinned Skia revision is `158dc9d7` (`skia-158dc9d7-r8`). Its valid-context
API `releaseResourcesAndAbandonContext()` releases resources before native
context destruction; its lost-context API `abandonContext()` prevents backend
3D calls from resource destructors. The production contract is to clean up the
old screen surface on its owning graphics thread while its EGL context is
current, before EGL/window teardown. If that context is already unavailable,
abandon its direct context before dropping GPU wrappers; do not issue cleanup
GL calls through the replacement context. No broader context-loss redesign
was tested or implemented.

## Milestone 4 initial validation

The Android production path queries EGL context/surface validity and checks
the current display, context, draw surface, and read surface. A valid
non-current context is rebound on the existing graphics thread before Skia
cleanup. The valid path calls `releaseResourcesAndAbandonContext()`; invalid
or unavailable state calls `abandonContext()` before surface wrapper release.
The same ordering is used for surface replacement and final graphics teardown.
Failed replacement initialization can retry on a later callback for the same
window.

The release build and five-case cleanup policy test passed. One API 30 surface
replacement logged cleanup completion before EGL destruction and replacement
Skia initialization, with no fatal signal. Full permission marker assertions,
test-injected loss/init failure, repeated EGL replacement, and distinctive
pixel validation remain incomplete. The screenshot was blank white. The exact
denied harness stopped at the existing ADB Back timeout. See
`evidence/M4-api30-initial-validation.md`.
