<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Resume state

- Plan: `.agent/exec-plan-419-android-rotation-rendering.md`
- Branch: `fix/419-sluggish-interface-rendering-when-rotating-screen`
- Active milestone: complete; M6 follow-up closed with documented AVD limitation
- Last logical slice: final ExecPlan closure after recording the AVD limitation
- Last logical commits: `db8539b9d` (`docs(plan): record current avd shift test`), `7d002ee25` (`docs(plan): record current branch shift test`), and `bcc048c6c` (`docs(plan): record physical sdk shift test`)
- Changed paths: `TotalCrossVM/android/app/src/main/java/totalcross/Launcher4A.java`, `TotalCrossVM/src/nm/ui/GraphicsPrimitives_c.h`, `TotalCrossVM/src/nm/ui/android/gfx_Graphics_c.h`, `TotalCrossVM/android/app/src/test/java/totalcross/RotationRequestCoordinatorTest.java`, `.agent/exec-plan-419-android-rotation-rendering.md`, `.agent/state/exec-plan-419-android-rotation-rendering.md`
- Focused validation: `:app:testStandardDebugUnitTest` passed with 11 tests; checkpoint AAB builds for base, M4 SIP, and M4 repaint passed; copyright-header validation and staged `git diff --check` passed
- Build/deploy validation: base `b7c25d776`, M4 SIP `c72ca44a9`, and M4 repaint `5a773cebc` Standard Release AABs were generated with `:app:bundleStandardRelease`, substituted into `TotalCrossSDK/dist/vm/android/TotalCross.aab`, deployed with `tc.Deploy`, and installed on the emulator; the cached `/Users/flsobral/.gradle/caches/totalcross/sdk/7.2.2` AAB and `tc.Deploy` were also used successfully; the current-branch `:app:bundleStandardRelease` AAB was substituted, deployed with `tc.Deploy`, and installed on the physical device, with the temporary app compiled using `javac --release 8`
- Device validation: the newly started emulator `emulator-5554`, model `sdk_gphone64_arm64`, Android 17 at 1080x2424, reproduced the covered lower Edit with the current-branch AAB; connected physical `192.168.1.111:39653`, model `2312DRA50G`, Android 13, showed the lower Edit above the keyboard with both SDK 7.2.2 and current-branch APKs. Old `totalcross.android` package was uninstalled with explicit authorization.
- Baseline evidence: M1 idle `/tmp/tc-rotation-m1-baseline-idle-summary.log` and `.mp4`; M1 load `/tmp/tc-rotation-m1-baseline-load-summary.log` and `.mp4`
- Milestone 2 evidence: `/tmp/tc-rotation-m2-idle-final.log` and `/tmp/tc-rotation-m2-load-final.log`
- Milestone 3 evidence: rapid `/tmp/tc-rotation-m3-rapid.log`; idle `/tmp/tc-rotation-m3-idle.log`; load `/tmp/tc-rotation-m3-load.log`
- Milestone 4 evidence: baseline closed `/tmp/tc-rotation-m4-idle-closed.log` and `/tmp/tc-rotation-m4-load-closed.log`; keyboard `/tmp/tc-rotation-m4-sip-keyboard-open.log` and `/tmp/tc-rotation-m4-sip-keyboard-after-back.log`; final idle `/tmp/tc-rotation-m4-final-idle.log` and load `/tmp/tc-rotation-m4-final-load.log`; A/B video `/tmp/tc-rotation-m4-ab-load.mp4`
- Milestone 6 evidence: final idle `/tmp/tc-rotation-m6-final-idle-summary.json`; fixed-load `/tmp/tc-rotation-m6-final-load-fixed-summary.json` and `/tmp/tc-rotation-m6-final-load-fixed.mp4`; keyboard `/tmp/tc-rotation-m6-keyboard-before.log` and `/tmp/tc-rotation-m6-keyboard-after.log`; lifecycle `/tmp/tc-rotation-m6-lifecycle-explicit.log`; three-button `/tmp/tc-rotation-m6-three-button.log`; two-Edit shift `/tmp/tc-rotation-m6-two-edits-shift-ime.mp4` and `/tmp/tc-rotation-m6-two-edits-shift-ime-frame.png`; regression frames `/tmp/tc-reg-baseline-loaded-ime.png`, `/tmp/tc-reg-m4-sip-ime2.png`, and `/tmp/tc-reg-m4-repaint-ime.png`
- Regression result: the pre-plan base, both M4 checkpoints, cached SDK 7.2.2 AAB, and current-branch AAB failed on the Android 14/17 emulator sessions; the cached SDK 7.2.2 APK and current-branch AAB both passed on the physical Android 13 device with `shiftY=1800`/`shiftH=200` and the lower Edit visible above the IME. This is device/rendering-path dependent, not a uniform milestone regression.
- SDK 7.2.2 evidence: `/tmp/tc-sdk722-deploy.log`, `/tmp/tc-sdk722-shift-ime.mp4`, `/tmp/tc-sdk722-shift-ime-frame.png`, `/tmp/tc-sdk722-ime.txt`; physical device `/tmp/tc-sdk722-physical-before-ime.png`, `/tmp/tc-sdk722-physical-shift-ime.png`, `/tmp/tc-sdk722-physical-ime.txt`, and `/tmp/tc-sdk722-physical-screenrecord.log`
- Current-branch evidence: `/tmp/tc-current-branch-bundle.log`, `/tmp/tc-current-branch-deploy.log`, `/tmp/tc-current-branch-before-ime.png`, `/tmp/tc-current-branch-shift-ime-retry.png`, and `/tmp/tc-current-branch-ime.txt`
- Current-branch AVD evidence: `/tmp/tc-current-branch-avd-before-ime.png`, `/tmp/tc-current-branch-avd-shift-ime.png`, `/tmp/tc-current-branch-avd-shift-ime-frame.png`, `/tmp/tc-current-branch-avd-shift-ime.mp4`, and `/tmp/tc-current-branch-avd-ime.txt`
- Next concrete action: none; ExecPlan finalized with the AVD limitation recorded
- Deferred validation: native `Window.shiftY` fix/investigation, additional physical/Android-version/Samsung devices, iOS, Windows, and full distribution validation
- Out of scope: no later milestone exists; runtime changes were not included in the finalized plan
