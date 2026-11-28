<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Investigation checkpoint

## Current milestone

Milestone 4 production correction is committed; focused API 30 lifecycle
validation is complete. Corrected permission scenarios and default-off pixel
validation remain outstanding. Stop before Milestone 5 pending review.

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
- Reused retained M2 diagnostic APKs/logs and tested one isolated candidate
  that released the screen Skia surface on the event thread before old EGL
  teardown. See evidence/M3-api30-causal-candidate.md.
- Candidate grant-in-place, denied-return, and pregranted scenarios passed.
  Five additional grant-in-place repetitions also passed with cleanup-end
  before EGL teardown and replacement Skia initialization recorded each time.
- One rotation/background/foreground control observed rotation, pause/resume,
  surface callbacks, continued process responsiveness, and no native crash.
- Confirmed the pinned Skia API provides `abandonContext()` for an already
  lost context and `releaseResourcesAndAbandonContext()` while the native
  context is still valid. No production code change was retained.

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
M2 diagnostics commit: 7b86b52b00a1933453581db575af76f774a42c05.
M3 candidate and evidence are included in this milestone's checkpoint.

## Current hypothesis

During the reproduced replacement, the old EGL context is destroyed before
the old Skia surface is reset. The new EGL context is current while the Skia
surface still tracks the old context; surface destruction flushes queued
GrDirectContext work and SIGSEGVs in the GL encoder. This strongly supports
the lifetime hypothesis. Releasing the screen surface on the same thread while
the old context remains current, before EGL teardown, prevented the reproduced
API 30 crashes in the grant-in-place and denied-return candidate flows.

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
- M3 candidate logs show old-context Skia surface reset completion before
  `egl.destroy.begin`, followed by replacement `egl.create.madeCurrent`,
  `skia.init.end`, and Java surface acceptance. Six grant-in-place runs
  created/deleted `/sdcard/asd`, retained their processes, and had no SIGSEGV.
- Denied-return reported the expected permission failure, completed old-context
  cleanup before EGL teardown, and returned to the live application. The
  pregranted control passed. Rotation plus Home/foreground also completed with
  callbacks, rendering, and no native fatal signal.
- The dependency pin is Skia `158dc9d7` (`skia-158dc9d7-r8`). Its documented
  lost-context path is `GrDirectContext::abandonContext()` before releasing
  surface wrappers; the valid-context destroy path supports releasing Skia
  resources before invalidating EGL.

## Rejected hypotheses

None.

## Pending experiments

M4 production correction is implemented and focused API 30 runtime validation
is complete for Skia/EGL teardown ordering, real context loss, injected
initialization failures and retry, repeated cleanup, and five rendered surface
replacements. The corrected original customer permission scenarios and full
default-off recreation pixel check remain unrun. See
evidence/M4-api30-resumed-validation.md and TEST-MATRIX.md. Milestone 5 has not
started and requires explicit authorization.

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
- M3 causal candidate results, Skia context-loss API contract, screenshot, and
  ordered sequences: evidence/M3-api30-causal-candidate.md,
  evidence/M3-api30-candidate-rendered.png, and
  evidence/M3-api30-candidate-sequence.log.

## Available build artifacts

The current SDK jar, AAB, arm64-v8a libtcvm.so, exact APK, and trace APK were
built from the recorded baseline. The test-only package overlays two UI image
assets; hashes, six phase assertions, sanitized native stack, and screenshot are
in evidence/M1-api30-reproduction.md. The M2 baseline and M3 candidate bundles,
APKs, and full logs remain outside Git; the candidate patch and compact
ordered evidence are committed separately.

## Remaining technical question

No unresolved M4 cleanup policy question remains for the exercised Android
paths. API 29/30/35/36 matrix validation and the corrected customer permission
flows remain for a later authorized milestone. M1's missing TCUI resources and
M2's all-phase ADB Back timeout remain independent findings and were not fixed.

## Exact next concrete action

Pause for review of completed M4 work. Do not begin Milestone 5 without
explicit authorization.

## Milestone 4 checkpoint update

Production correction commits: 4c128ad88 and d71b21bc7. Policy/test-probe
commit: b9415e205; original cleanup-policy test commit: c5e7b7103. The resumed
cleanup-policy C test also passed all five cases. Diagnostic and default-off
ARM64 Release builds passed. API 30 testing
verified actual context destruction followed by Skia abandon cleanup; a real
failed replacement initialization was first found to call `skia_shiftScreen`
without a canvas. The Android update path now skips drawing while the screen
surface is not ready, and a cleared failure followed by another surface
callback restored EGL, Skia, visible content, and the same running process.
Injected Skia surface-creation failure/retry and repeated empty cleanup passed.
Five consecutive forced EGL replacements during rotations each showed cleanup
end before EGL destroy, replacement EGL current before Skia init end, and
preserved exact colored pixels for retained/shared/new images. The corrected
customer grant/denied/pregranted scenarios were not rerun in this interval.
Evidence is in `evidence/M4-api30-resumed-validation.md`; initial M4 results
remain in `evidence/M4-api30-initial-validation.md`. Milestone 5 has not begun.

## Resume protocol

Resume in the isolated worktree
/Users/flsobral/.codex/worktrees/fix-android-skia-egl-surface-lifecycle.
Read this file first. M4 is complete for this authorized interval; wait for
explicit authorization before Milestone 5.
