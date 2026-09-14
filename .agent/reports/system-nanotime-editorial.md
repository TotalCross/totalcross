<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# System nanoTime handoff

Implemented on branch `feat/system-nanotime` in three logical commits:

- `276b5c6c6 feat(system): add nanoTime device API`
- `ab165a6d8 feat(vm): implement monotonic nanoTime clocks`
- `test(system): add nanoTime macOS smoke` (final milestone)

The implementation keeps simulator calls on the JDK, resolves converted calls
through `System4D`, registers `jlS_nanoTime`, and uses monotonic platform clocks:
POSIX `clock_gettime`, Darwin mach time, Win32 QPC, and the existing WinCE tick
source scaled to nanoseconds. `Vm.getTimeStamp()` was not changed.

Validation passed:

- SDK distribution build (`/tmp/system-nanotime-sdk-dist.log`)
- Focused converter/API/structural test (`/tmp/system-nanotime-sdk-test.log`)
- macOS native build (`/tmp/system-nanotime-macos-final-build.log`)
- macOS smoke (`deltaNanos=21110084`, `overallPass=true`)
- Copyright-header and diff checks

Windows, Linux, Android, and iOS builds were intentionally not run per the
request.
