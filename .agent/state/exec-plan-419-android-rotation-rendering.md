<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Resume state

- Plan: `.agent/exec-plan-419-android-rotation-rendering.md`
- Branch: `fix/419-sluggish-interface-rendering-when-rotating-screen`
- Active milestone: Milestone 5 completed at its evidence gate; Milestone 6 is pending
- Last logical slice: EGL/Skia ownership gate reviewed; no refactor justified by the trace evidence
- Last logical commits: `5a773cebc` (`perf(graphics,android): remove duplicate rotation repaint`) and `551be613f` (`test(graphics,android): count rotation repaint calls`); prior SIP slice `c72ca44a9`
- Changed paths: `TotalCrossVM/android/app/src/main/java/totalcross/Launcher4A.java`, `TotalCrossVM/src/nm/ui/GraphicsPrimitives_c.h`, `TotalCrossVM/src/nm/ui/android/gfx_Graphics_c.h`, `TotalCrossVM/android/app/src/test/java/totalcross/RotationRequestCoordinatorTest.java`, `.agent/exec-plan-419-android-rotation-rendering.md`, `.agent/state/exec-plan-419-android-rotation-rendering.md`
- Focused validation: `:app:testStandardDebugUnitTest` passed with 11 tests; copyright-header validation and staged `git diff --check` passed
- Build/deploy validation: final Standard Release AAB `:app:bundleStandardRelease` passed; `tc.Deploy` and APK installation passed using `TotalCrossSDK/dist/vm/android/TotalCross.aab`
- Device validation: emulator `emulator-5554`, model `sdk_gphone64_arm64`, Android 14; old `totalcross.android` package was uninstalled with explicit authorization, then the generated test APK was installed and launched
- Baseline evidence: M1 idle `/tmp/tc-rotation-m1-baseline-idle-summary.log` and `.mp4`; M1 load `/tmp/tc-rotation-m1-baseline-load-summary.log` and `.mp4`
- Milestone 2 evidence: `/tmp/tc-rotation-m2-idle-final.log` and `/tmp/tc-rotation-m2-load-final.log`
- Milestone 3 evidence: rapid `/tmp/tc-rotation-m3-rapid.log`; idle `/tmp/tc-rotation-m3-idle.log`; load `/tmp/tc-rotation-m3-load.log`
- Milestone 4 evidence: baseline closed `/tmp/tc-rotation-m4-idle-closed.log` and `/tmp/tc-rotation-m4-load-closed.log`; keyboard `/tmp/tc-rotation-m4-sip-keyboard-open.log` and `/tmp/tc-rotation-m4-sip-keyboard-after-back.log`; final idle `/tmp/tc-rotation-m4-final-idle.log` and load `/tmp/tc-rotation-m4-final-load.log`; A/B video `/tmp/tc-rotation-m4-ab-load.mp4`
- Next concrete action: begin Milestone 6 final lifecycle/resource-recovery validation; do not execute it as part of this continuation
- Deferred validation: all Milestone 6+ tests, broader device/lifecycle matrix, and native ownership changes remain deferred by instruction
- Out of scope for the next continuation: all Milestone 6+ validation and final evidence collection
