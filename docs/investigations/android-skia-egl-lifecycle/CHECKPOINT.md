<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Investigation checkpoint

## Current milestone

Milestone 2 complete — mandatory pause before Milestone 3.

## Completed sub-milestones

- Read repository instructions, ExecPlan conventions, and the logical-commits
  and validate-headers skills.
- Inspected branches, worktrees, and local changes. The main checkout has
  unrelated untracked work; the isolated target worktree is clean.
- Refreshed origin/master and confirmed the requested local and remote branch
  already points at that exact revision.
- Reviewed issue 563, the existing I03 report, reproducer PR 7, and historical
  workflow run 43113450337.
- Inspected current Android EGL and Skia source against the historical 7.3.0
  revision.
- Booted the installed API 30 ARM64 AVD with host GPU acceleration and
  confirmed API 30, ARM64, and the Apple M1 Pro OpenGL ES renderer.
- Built the current-source SDK and Android ARM64 release bundle. The first
  harness attempt stopped at missing Android UI resources before any
  permission or filesystem scenario.
- Confirmed the Android resource directory is missing floatingFrg.png and
  outlined_edit.png, both available by the same names in the Material folder.
  Added these only to a disposable TCUI archive for testing; the repository
  source assets were removed immediately after generation.
- Rebuilt the exact customer and marker-only APKs from baseline SDK/runtime
  artifacts and ran all six permission phases on the API 30 ARM64 AVD.
- Reproduced SIGSEGV on return from the in-place permission screen in both
  customer and marker-only APK logcats. The trace variant confirmed successful
  directory create/delete before the surface callback crash.
- Preserved compact crash evidence, scenario assertions, artifact hashes, and
  the post-crash emulator screenshot in evidence/M1-api30-reproduction.md.
- Added default-off Java/native EGL and Skia lifecycle diagnostics and built a
  diagnostic Android bundle. See evidence/M2-api30-lifecycle-diagnostics.md.
- Reproduced the API 30 trace grant-in-place crash with ordered identity logs:
  old EGL context teardown precedes old Skia surface reset, which flushes via
  GrDirectContext destruction and crashes in GL2Encoder. The pregranted trace
  control created/deleted the directory and remained alive.
- The full harness invocation timed out in ADB cleanup after its exact-app
  phases; focused trace grant-in-place and pregranted phases were run directly.

## Baseline revision

52a947b5295321d7350350bd618c55fb26304c00

This is the initial source baseline and was equal to origin/master and
origin/fix/android-skia-egl-surface-lifecycle after fetch on 2026-11-27. On
resume, origin/master advanced to 6beef491f28c63aa364d3661dd5c37ccff01fb0d;
the intervening commits did not modify the Android EGL/Skia code or SDK
resource/build paths relevant to this result. The investigation branch remains
on its recorded baseline.

## Last completed commit

M1 baseline reproduction commit: c59a6ac2fee0018d2a75402e43aa46397a0d9d38.
M2 diagnostics and sequence are included in this milestone's checkpoint.

## Current hypothesis

During the reproduced replacement, the old EGL context is destroyed before
the old Skia surface is reset. The new EGL context is current while the Skia
surface still tracks the old context; surface destruction flushes queued
GrDirectContext work and SIGSEGVs in the GL encoder. This strongly supports
the lifetime hypothesis; a controlled ordering comparison is still required.

## Confirmed findings

- Historical official 7.3.0 evidence reported four native crashes on each of
  APIs 30, 35, and 36, and no corresponding native crash on API 29.
- Current master preserves the historical Android teardown ordering in the
  inspected GLES and Skia files.
- An API 30 ARM64 emulator is installed and can run with host GPU acceleration.
- The current-source APK opens unhandled FileNotFoundExceptions for two
  missing Android UI resources before entering the customer test. A disposable
  overlay with the existing Material floatingFrg.png and outlined_edit.png
  allowed startup without changing customer Java or production source.
- The exact customer APK and marker-only APK show SIGSEGV after the in-place
  permission grant and return to the app. The trace variant recorded successful
  directory creation and deletion before the crash.
- The native backtrace includes GL2Encoder::s_glDrawRangeElements,
  FillRectOp::onExecute, GrDirectContext::~GrDirectContext,
  SkGpuDevice::~SkGpuDevice, initSkia, initGLES, and
  Java_totalcross_Launcher4A_nativeSurfaceChanged.
- M2 logs correlate the old and replacement EGL contexts with the Skia surface
  and context identities. At `skia.surface.reset.begin`, the replacement EGL
  context was current but the tracked Skia context was the old one. The
  tombstone runs through `GrDrawingManager::flush`,
  `GrDirectContext::~GrDirectContext`, `SkSurface_Gpu::~SkSurface_Gpu`, and
  `destroySkiaScreen`.
- A focused trace pregranted control completed the same file operation and
  retained its process; its Skia surface reset began and ended with the
  associated EGL context current.

## Rejected hypotheses

None.

## Pending experiments

Manual rotation and standalone background/foreground controls were not run.
The next step is Milestone 3: isolate teardown ordering with a controlled
comparison. Do not alter production behavior or start M3 until the user
explicitly authorizes it.

## Test environment

- Host: Apple Silicon Mac, arm64.
- AVD: Codex_TC730_API30_ARM64.
- System image: android-30/google_apis/arm64-v8a.
- Device fingerprint: google/sdk_gphone_arm64/emulator_arm64:11/RSR1.240422.006/12134477:userdebug/dev-keys.
- Renderer: Google (Apple), Android Emulator OpenGL ES Translator (Apple M1 Pro),
  OpenGL ES 3.0 (4.1 Metal - 91.7).
- Emulator is launched read-only with -gpu host and -accel on.
- Runtime result: current-baseline API 30 reproduction; see
  evidence/M1-api30-reproduction.md.
- M2 diagnostic bundle, lifecycle sequence, and focused controls:
  evidence/M2-api30-lifecycle-diagnostics.md and
  evidence/M2-api30-lifecycle-sequence.log.

## Available build artifacts

The current SDK jar, AAB, arm64-v8a libtcvm.so, exact APK, and trace APK were
built from the recorded baseline. The test-only package overlays two UI image
assets; hashes, six phase assertions, sanitized native stack, and screenshot are
in evidence/M1-api30-reproduction.md. Generated APKs and full logs remain
outside Git.

## Remaining technical question

The current Android resource tree is missing two assets eagerly loaded by
Android UI startup. The disposable overlay is sufficient for this reproduced
flow; no production resource fix is in scope for the graphics correction.
M2 supports the context-mismatch hypothesis; M3 must isolate whether teardown
ordering alone prevents this failure. No production correction has been made.

## Exact next concrete action

Stop at the Milestone 2 review pause. After explicit authorization, begin
Milestone 3 with a controlled teardown-order comparison using the recorded
API 30 reproducer and diagnostics.

## Resume protocol

Resume in the isolated worktree
/Users/flsobral/.codex/worktrees/fix-android-skia-egl-surface-lifecycle.
Read this file first. Do not begin Milestone 3 without explicit authorization.
