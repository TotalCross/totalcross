<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Resume state

- Plan: `.agent/exec-plan-419-android-rotation-rendering.md`
- Branch: `fix/419-sluggish-interface-rendering-when-rotating-screen`
- Active milestone: Milestone 3 completed; Milestone 4 is pending
- Last logical slice: immutable resize snapshots and stale-generation cancellation before JNI
- Last logical commit: `6e8cbb406` (`fix(graphics,android): coalesce stale rotation work`)
- Changed paths: `TotalCrossVM/android/app/src/main/java/totalcross/Launcher4A.java`, `TotalCrossVM/android/app/src/main/java/totalcross/RotationRequestCoordinator.java`, `TotalCrossVM/android/app/src/test/java/totalcross/RotationRequestCoordinatorTest.java`, `.agent/exec-plan-419-android-rotation-rendering.md`, `.agent/state/exec-plan-419-android-rotation-rendering.md`
- Focused validation: `:app:testStandardDebugUnitTest` passed with 11 tests; copyright-header validation and staged `git diff --check` passed
- Build/deploy validation: Standard Release AAB `:app:bundleStandardRelease` passed; `tc.Deploy` and APK installation passed using `TotalCrossSDK/dist/vm/android/TotalCross.aab`
- Device validation: emulator `emulator-5554`, model `sdk_gphone64_arm64`, Android 14; old `totalcross.android` package was uninstalled with explicit authorization, then the generated test APK was installed and launched
- Baseline evidence: M1 idle `/tmp/tc-rotation-m1-baseline-idle-summary.log` and `.mp4`; M1 load `/tmp/tc-rotation-m1-baseline-load-summary.log` and `.mp4`
- Milestone 2 evidence: `/tmp/tc-rotation-m2-idle-final.log` and `/tmp/tc-rotation-m2-load-final.log`
- Milestone 3 evidence: rapid `/tmp/tc-rotation-m3-rapid.log` (3 stale tasks before JNI); idle `/tmp/tc-rotation-m3-idle.log` (10 accepted, 6 completed, 7 stale); load `/tmp/tc-rotation-m3-load.log` (10 accepted, 8 completed, 5 stale); final stable dimensions `1080x2220`
- Next concrete action: begin Milestone 4 measured SIP/repaint slices; do not execute it as part of this continuation
- Deferred validation: all Milestone 4+ tests, broader device/lifecycle matrix, and optimization validation remain deferred by instruction
- Out of scope: duplicate dropping, stale-task coalescing, SIP/repaint changes, EGL/Skia ownership changes, and all later milestone validation
