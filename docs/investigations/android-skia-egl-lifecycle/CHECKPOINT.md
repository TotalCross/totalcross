<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Investigation checkpoint

## Current milestone

Milestone 1 complete — mandatory pause before Milestone 2.

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

## Baseline revision

52a947b5295321d7350350bd618c55fb26304c00

This is the initial source baseline and was equal to origin/master and
origin/fix/android-skia-egl-surface-lifecycle after fetch on 2026-11-27. On
resume, origin/master advanced to 6beef491f28c63aa364d3661dd5c37ccff01fb0d;
the intervening commits did not modify the Android EGL/Skia code or SDK
resource/build paths relevant to this result. The investigation branch remains
on its recorded baseline.

## Last completed commit

This checkpoint's reproduction evidence follows 0bb06b0959a3e00425de793d75f22bb0c4e53f2b,
the prior startup-blocker evidence commit.

## Current hypothesis

The old Skia GPU surface may be destroyed after its EGL context has already
been destroyed during native surface replacement. This is not established.

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
- This confirms reproduction on the recorded source baseline. It does not
  establish whether Skia resources were released with a valid/current old EGL
  context.

## Rejected hypotheses

None.

## Pending experiments

No further Milestone 1 experiments are pending. Manual rotation and standalone
background/foreground controls were not run; the permission Settings return
already reproduced the surface callback crash. Milestone 2 instrumentation is
the next action only after explicit authorization.

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

## Available build artifacts

The current SDK jar, AAB, arm64-v8a libtcvm.so, exact APK, and trace APK were
built from the recorded baseline. The test-only package overlays two UI image
assets; hashes, six phase assertions, sanitized native stack, and screenshot are
in evidence/M1-api30-reproduction.md. Generated APKs and full logs remain
outside Git.

## Remaining blockers

The current Android resource tree is missing two assets eagerly loaded by
Android UI startup. The disposable overlay is sufficient for this reproduced
flow; no production resource fix is in scope for the graphics correction. The
remaining technical uncertainty is the actual Skia/EGL resource ownership and
context state at teardown. No lifecycle instrumentation or production change
has been made.

## Exact next concrete action

Stop at the Milestone 1 review pause. After explicit authorization, begin
Milestone 2 by inspecting lifecycle ownership and preparing minimal ordered
Android EGL/Skia diagnostics in an isolated patch.

## Resume protocol

Resume in the isolated worktree
/Users/flsobral/.codex/worktrees/fix-android-skia-egl-surface-lifecycle.
Read this file first. Do not begin Milestone 2 without explicit authorization.
