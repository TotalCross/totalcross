<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# System nanoTime history

## Milestone 1 — API and conversion

Completed on 2026-09-14 in commit `276b5c6c6`:

- Added the `System4D` native declaration and the `java/lang/System` native
  metadata/registration contract for `jlS_nanoTime`.
- Added converter and simulator tests for the `()J` call and nanosecond
  monotonic behavior.
- Kept `Vm.getTimeStamp()` and `Vm` APIs unchanged.

## Milestone 2 — native implementations

Implemented on 2026-09-14 in the pending native milestone slice:

- Added one `jlS_nanoTime` wrapper and shared `getNanoTime()` utility entry
  point.
- Added `clock_gettime(CLOCK_MONOTONIC)` for Linux/Android, cached Darwin
  mach timebase conversion, cached Win32 QPC frequency conversion, and the
  existing WinCE tick source scaled to nanoseconds.
- Registered the source in CMake and the legacy VC2008 project and added
  structural coverage for platform clock selection and overflow-safe formulas.
- The macOS native build passed after including the SDK's dedicated
  `mach/mach_time.h` header.
