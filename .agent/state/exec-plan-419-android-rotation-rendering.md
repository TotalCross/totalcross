<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Resume state

- Plan: `.agent/exec-plan-419-android-rotation-rendering.md`
- Branch: `fix/419-sluggish-interface-rendering-when-rotating-screen`
- Active milestone: Milestone 1 completed; Milestone 2 is pending
- Last logical slice: corrected rotation-generation association and completed dual-workload baseline capture
- Last logical commit: `0942a6d8d` (`test(graphics,android): stabilize rotation trace generations`)
- Changed paths: `TotalCrossVM/android/app/src/main/java/totalcross/Launcher4A.java`, `TotalCrossVM/src/nm/ui/GraphicsPrimitives_c.h`, `TotalCrossVM/src/nm/ui/android/gfx_Graphics_c.h`, `TotalCrossVM/src/nm/ui/android/rotation_trace.h`, `scripts/diagnostics/summarize-rotation-trace.py`, `.agent/exec-plan-419-android-rotation-rendering.md`, `.agent/state/exec-plan-419-android-rotation-rendering.md`
- Focused validation: synthetic summarizer input passed; `:app:assembleStandardDebug` passed; Standard Release AAB `:app:bundleStandardRelease` passed after trace changes; deploy and APK install passed
- Device validation: emulator `emulator-5554`, model `sdk_gphone64_arm64`, Android 14; old `totalcross.android` package was uninstalled with explicit authorization, then the generated test APK was installed and launched
- Baseline evidence: idle `/tmp/tc-rotation-m1-baseline-idle-summary.log` and `/tmp/tc-rotation-m1-baseline-idle-summary.mp4`; event-thread load `/tmp/tc-rotation-m1-baseline-load-summary.log` and `/tmp/tc-rotation-m1-baseline-load-summary.mp4`; 24 alternating rotations per workload, 24/24 accepted and completed, no stale tasks
- Next concrete action: begin Milestone 2 duplicate-request coordinator work; do not execute it as part of this continuation
- Deferred validation: all later milestone tests, broader device/lifecycle matrix, and optimization validation remain deferred by instruction
- Out of scope: duplicate dropping, stale-task coalescing, SIP/repaint changes, EGL/Skia ownership changes, and all later milestone validation
