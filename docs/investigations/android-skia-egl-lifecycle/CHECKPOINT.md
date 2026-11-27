<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Investigation checkpoint

## Current milestone

Milestone 1 — preparation and current-master baseline reproduction.

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
- Built the current-source SDK and Android ARM64 release bundle. The six-phase
  customer harness attempt stopped at an unrelated SDK resource initialization
  error before reaching any permission or filesystem scenario.

## Baseline revision

52a947b5295321d7350350bd618c55fb26304c00

This is the initial source baseline and was equal to origin/master and
origin/fix/android-skia-egl-surface-lifecycle after fetch on 2026-11-27.

## Last completed commit

e298f864fb3894126ae0f51ea506f0ec635b6c4f — initial investigation
documentation. The current M1 result is in the pending checkpoint commit.

## Current hypothesis

The old Skia GPU surface may be destroyed after its EGL context has already
been destroyed during native surface replacement. This is not established.

## Confirmed findings

- Historical official 7.3.0 evidence reported four native crashes on each of
  APIs 30, 35, and 36, and no corresponding native crash on API 29.
- Current master preserves the historical Android teardown ordering in the
  inspected GLES and Skia files.
- An API 30 ARM64 emulator is installed and can run with host GPU acceleration.
- The current-source APK opens an unhandled FileNotFoundException for
  totalcross/res/android/floatingFrg.png before entering the customer test.
  No permission prompt or customer File operation marker was observed.
- The current-source run is inconclusive for the graphics defect; the app
  never reached the Activity transition that historically reproduced it.

## Rejected hypotheses

None.

## Pending experiments

- Build an SDK and Android ARM64 VM from the baseline source.
- Run the existing customer reproducer and diagnostic variant on API 30.
- Assert permission state, app markers/completion, rendering, and crash
  signature.
- Exercise background/foreground and surface recreation with the available
  diagnostic application.

## Test environment

- Host: Apple Silicon Mac, arm64.
- AVD: Codex_TC730_API30_ARM64.
- System image: android-30/google_apis/arm64-v8a.
- Device fingerprint: google/sdk_gphone_arm64/emulator_arm64:11/RSR1.240422.006/12134477:userdebug/dev-keys.
- Renderer: Google (Apple), Android Emulator OpenGL ES Translator (Apple M1 Pro),
  OpenGL ES 3.0 (4.1 Metal - 91.7).
- Emulator is launched read-only with -gpu host and -accel on.
- Runtime test not yet performed.

## Available build artifacts

The current SDK jar, AAB, arm64-v8a libtcvm.so, exact APK, and trace APK were
built from the recorded baseline. Their hashes and the bounded run summary are
in evidence/M1-api30-run.md. Generated APKs and full logs remain outside Git.

## Remaining blockers

The current-source deployment is missing the Android UI resource
floatingFrg.png. Determine whether this comes from SDK packaging or the source
baseline before retrying the reproducer. The SDK manager's stale AVD warning is
not blocking; the emulator booted successfully.

## Exact next concrete action

Compare the source tree and canonical SDK packaging path for the missing
floatingFrg.png runtime asset. Identify a test-input correction that preserves
the customer Java operations, then rerun the API 30 baseline only after
explicit authorization to continue.

## Resume protocol

Resume in the isolated worktree
/Users/flsobral/.codex/worktrees/fix-android-skia-egl-surface-lifecycle.
Read this file first. Do not begin Milestone 2 without explicit authorization.
