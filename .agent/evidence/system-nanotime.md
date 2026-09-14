<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# System nanoTime evidence

Append-only milestone validation index.

## 2026-09-14

- Milestone 1, commit `276b5c6c6`: focused Gradle test passed with exit code 0;
  7 tasks, 5 actionable, 3 executed, 2 up-to-date; logs:
  `TotalCrossSDK/agent-logs/20260914-185612-test-full.log` and
  `TotalCrossSDK/agent-logs/20260914-185612-test-agent.log`.
- Milestone 1: `git diff --check` and the focused copyright validator passed.
- Milestone 2 implementation is staged in the working tree; macOS native build
  remains deferred until the slice is complete.
- Milestone 2 macOS native build passed after adding `mach/mach_time.h`; command
  logs are in `/tmp/system-nanotime-macos-build.log`, and the produced runtime
  is `build/system-nanotime-macos/libtcvm.dylib`.
- The first macOS native configure/build attempt failed because the Darwin
  helper omitted `mach/mach_time.h`; the include was added and the rerun passed.
- Milestone 3 SDK distribution build passed (`/tmp/system-nanotime-sdk-dist.log`)
  and the focused converter/API/structural test passed
  (`/tmp/system-nanotime-sdk-test.log`).
- Milestone 3 macOS native rebuild passed (`/tmp/system-nanotime-macos-final-build.log`).
- The first smoke deployment failed because `tc.Deploy` requires a JAR rather
  than a standalone class; after adding the JAR task, the smoke passed with
  `deltaNanos=21110084`, `overallPass=true`.
