<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Resume state

- Plan: `.agent/exec-plan-419-android-rotation-rendering.md`
- Branch: `fix/419-sluggish-interface-rendering-when-rotating-screen`
- Active milestone: Milestone 4 completed; Milestone 5 is pending
- Last logical slice: measured SIP and repaint duplication removed in independent Android commits
- Last logical commits: `c72ca44a9` (`fix(runtime,android): avoid redundant rotation sip events`) and `5a773cebc` (`perf(graphics,android): remove duplicate rotation repaint`)
- Changed paths: `TotalCrossVM/android/app/src/main/java/totalcross/Launcher4A.java`, `TotalCrossVM/src/nm/ui/GraphicsPrimitives_c.h`, `TotalCrossVM/src/nm/ui/android/gfx_Graphics_c.h`, `TotalCrossVM/android/app/src/test/java/totalcross/RotationRequestCoordinatorTest.java`, `.agent/exec-plan-419-android-rotation-rendering.md`, `.agent/state/exec-plan-419-android-rotation-rendering.md`
- Focused validation: `:app:testStandardDebugUnitTest` passed with 11 tests; copyright-header validation and staged `git diff --check` passed
- Build/deploy validation: final Standard Release AAB `:app:bundleStandardRelease` passed; `tc.Deploy` and APK installation passed using `TotalCrossSDK/dist/vm/android/TotalCross.aab`
- Device validation: emulator `emulator-5554`, model `sdk_gphone64_arm64`, Android 14; old `totalcross.android` package was uninstalled with explicit authorization, then the generated test APK was installed and launched
- Baseline evidence: M1 idle `/tmp/tc-rotation-m1-baseline-idle-summary.log` and `.mp4`; M1 load `/tmp/tc-rotation-m1-baseline-load-summary.log` and `.mp4`
- Milestone 2 evidence: `/tmp/tc-rotation-m2-idle-final.log` and `/tmp/tc-rotation-m2-load-final.log`
- Milestone 3 evidence: rapid `/tmp/tc-rotation-m3-rapid.log`; idle `/tmp/tc-rotation-m3-idle.log`; load `/tmp/tc-rotation-m3-load.log`
- Milestone 4 evidence: baseline closed `/tmp/tc-rotation-m4-idle-closed.log` and `/tmp/tc-rotation-m4-load-closed.log`; keyboard `/tmp/tc-rotation-m4-sip-keyboard-open.log` and `/tmp/tc-rotation-m4-sip-keyboard-after-back.log`; final idle `/tmp/tc-rotation-m4-final-idle.log` and load `/tmp/tc-rotation-m4-final-load.log`; A/B video `/tmp/tc-rotation-m4-ab-load.mp4`
- Next concrete action: begin Milestone 5 EGL/Skia ownership analysis only if the recorded duplicate initialization evidence justifies it; do not execute it as part of this continuation
- Deferred validation: all Milestone 5+ tests, broader device/lifecycle matrix, and native ownership changes remain deferred by instruction
- Out of scope for the next continuation: duplicate dropping, stale-task coalescing, SIP/repaint changes, and all later-milestone validation; Milestone 5 is limited to EGL/Skia ownership analysis if justified by the recorded evidence
