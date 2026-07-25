<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Resume state

- Plan: `.agent/exec-plan-419-android-rotation-rendering.md`
- Branch: `fix/419-sluggish-interface-rendering-when-rotating-screen`
- Active milestone: Milestone 1, baseline capture after instrumentation
- Last logical slice: disabled-by-default structured rotation tracing and trace summarizer
- Changed paths: `TotalCrossVM/android/app/src/main/java/totalcross/Launcher4A.java`, `TotalCrossVM/src/event/android/event_c.h`, `TotalCrossVM/src/nm/ui/GraphicsPrimitives_c.h`, `TotalCrossVM/src/nm/ui/android/gfx_Graphics_c.h`, `TotalCrossVM/src/nm/ui/android/rotation_trace.h`, `TotalCrossVM/src/nm/ui/skia/skia.cpp`, `scripts/diagnostics/summarize-rotation-trace.py`
- Focused validation: synthetic summarizer input passed; `:app:assembleStandardDebug` passed; log `/tmp/tc-rotation-m1-build.log`
- Device validation: blocked before launch because `adb install -r` returned `INSTALL_FAILED_UID_CHANGED` for the existing `totalcross.android` package; no uninstall or data deletion was performed
- Next concrete action: choose a safe install target or obtain authorization to replace only the stale emulator package, then enable `log.tag.TotalCrossRotation=DEBUG` and capture the idle and deterministic event-thread-load baseline
- Deferred validation: twenty rotations per workload, screen recording, and baseline evidence; deferred because the instrumented APK could not be installed
- Out of scope: duplicate dropping, stale-task coalescing, SIP/repaint changes, EGL/Skia ownership changes, and all later milestone validation
