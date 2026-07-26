<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Resume state

- Plan: `.agent/exec-plan-419-android-rotation-rendering.md`
- Branch: `fix/419-sluggish-interface-rendering-when-rotating-screen`
- Active milestone: Milestone 6 follow-up blocked; base M6 validation is complete
- Last logical slice: regression of the two-Edit lower-screen keyboard shift across plan checkpoints
- Last logical commits: `5a773cebc` (`perf(graphics,android): remove duplicate rotation repaint`), `551be613f` (`test(graphics,android): count rotation repaint calls`), and the final M6 documentation checkpoint
- Changed paths: `TotalCrossVM/android/app/src/main/java/totalcross/Launcher4A.java`, `TotalCrossVM/src/nm/ui/GraphicsPrimitives_c.h`, `TotalCrossVM/src/nm/ui/android/gfx_Graphics_c.h`, `TotalCrossVM/android/app/src/test/java/totalcross/RotationRequestCoordinatorTest.java`, `.agent/exec-plan-419-android-rotation-rendering.md`, `.agent/state/exec-plan-419-android-rotation-rendering.md`
- Focused validation: `:app:testStandardDebugUnitTest` passed with 11 tests; checkpoint AAB builds for base, M4 SIP, and M4 repaint passed; copyright-header validation and staged `git diff --check` passed
- Build/deploy validation: base `b7c25d776`, M4 SIP `c72ca44a9`, and M4 repaint `5a773cebc` Standard Release AABs were generated with `:app:bundleStandardRelease`, substituted into `TotalCrossSDK/dist/vm/android/TotalCross.aab`, deployed with `tc.Deploy`, and installed on the emulator
- Device validation: emulator `emulator-5554`, model `sdk_gphone64_arm64`, Android 14; old `totalcross.android` package was uninstalled with explicit authorization, then the generated test APK was installed and launched
- Baseline evidence: M1 idle `/tmp/tc-rotation-m1-baseline-idle-summary.log` and `.mp4`; M1 load `/tmp/tc-rotation-m1-baseline-load-summary.log` and `.mp4`
- Milestone 2 evidence: `/tmp/tc-rotation-m2-idle-final.log` and `/tmp/tc-rotation-m2-load-final.log`
- Milestone 3 evidence: rapid `/tmp/tc-rotation-m3-rapid.log`; idle `/tmp/tc-rotation-m3-idle.log`; load `/tmp/tc-rotation-m3-load.log`
- Milestone 4 evidence: baseline closed `/tmp/tc-rotation-m4-idle-closed.log` and `/tmp/tc-rotation-m4-load-closed.log`; keyboard `/tmp/tc-rotation-m4-sip-keyboard-open.log` and `/tmp/tc-rotation-m4-sip-keyboard-after-back.log`; final idle `/tmp/tc-rotation-m4-final-idle.log` and load `/tmp/tc-rotation-m4-final-load.log`; A/B video `/tmp/tc-rotation-m4-ab-load.mp4`
- Milestone 6 evidence: final idle `/tmp/tc-rotation-m6-final-idle-summary.json`; fixed-load `/tmp/tc-rotation-m6-final-load-fixed-summary.json` and `/tmp/tc-rotation-m6-final-load-fixed.mp4`; keyboard `/tmp/tc-rotation-m6-keyboard-before.log` and `/tmp/tc-rotation-m6-keyboard-after.log`; lifecycle `/tmp/tc-rotation-m6-lifecycle-explicit.log`; three-button `/tmp/tc-rotation-m6-three-button.log`; two-Edit shift `/tmp/tc-rotation-m6-two-edits-shift-ime.mp4` and `/tmp/tc-rotation-m6-two-edits-shift-ime-frame.png`; regression frames `/tmp/tc-reg-baseline-loaded-ime.png`, `/tmp/tc-reg-m4-sip-ime2.png`, and `/tmp/tc-reg-m4-repaint-ime.png`
- Regression result: the pre-plan base and both M4 checkpoints all computed `shiftY=1804` with the IME visible while leaving the lower Edit covered; the failure is pre-existing relative to this ExecPlan, not attributable to a milestone transition
- Next concrete action: investigate native OpenGL application of `Window.shiftY` only after explicit authorization; do not implement it as part of this continuation
- Deferred validation: the two-Edit shift fix, additional physical/Android-version/Samsung devices, iOS, Windows, and full distribution validation
- Out of scope: no later milestone exists; runtime changes are blocked pending authorization
