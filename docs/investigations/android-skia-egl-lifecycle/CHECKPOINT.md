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

## Baseline revision

52a947b5295321d7350350bd618c55fb26304c00

This is the initial source baseline and was equal to origin/master and
origin/fix/android-skia-egl-surface-lifecycle after fetch on 2026-11-27.

## Last completed commit

None for this investigation yet. The baseline commit is listed above.

## Current hypothesis

The old Skia GPU surface may be destroyed after its EGL context has already
been destroyed during native surface replacement. This is not established.

## Confirmed findings

- Historical official 7.3.0 evidence reported four native crashes on each of
  APIs 30, 35, and 36, and no corresponding native crash on API 29.
- Current master preserves the historical Android teardown ordering in the
  inspected GLES and Skia files.
- An API 30 ARM64 emulator is installed and can run with host GPU acceleration.

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

No current-source APK or libtcvm.so artifacts are present in this worktree.
The historical 7.3.0 artifacts and logs are summarized and linked in
FINDINGS.md; they are not substitutes for current-source validation.

## Remaining blockers

None yet. The Android SDK manager reports old AVD image metadata as missing,
but the emulator itself booted successfully from the installed image.

## Exact next concrete action

Build the SDK and ARM64 runtime from this baseline using the canonical Gradle
paths. Reuse the existing customer Java sources and harness, adapting only the
package/build input as necessary for the current-source SDK. Then run API 30
baseline cases and preserve a compact result, hashes, logs, and crash evidence.

## Resume protocol

Resume in the isolated worktree
/Users/flsobral/.codex/worktrees/fix-android-skia-egl-surface-lifecycle.
Read this file first. Do not begin Milestone 2 without explicit authorization.
