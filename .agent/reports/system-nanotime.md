<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# System.nanoTime() report

## Summary

Added deployed `System.nanoTime()` support as a monotonic elapsed-time source.
`Vm.getTimeStamp()` and the existing `Vm` API remain unchanged.

## Final implementation

`System4D.nanoTime()` maps to the Java `()J` native registration and a single
`jlS_nanoTime` wrapper calling the shared `getNanoTime()` utility. POSIX uses
`clock_gettime(CLOCK_MONOTONIC)`, Darwin uses cached mach timebase conversion,
Windows desktop uses cached QueryPerformanceCounter frequency, and WinCE scales
its existing monotonic tick source. Conversion avoids multiplying full counter
values before division. The native source is included through CMake; legacy
`Android.mk` and `TCVM.vcproj` are unchanged.

## Decisions and tradeoffs

One shared native entry point keeps platform clock selection in existing
utility headers. The API reports elapsed time and makes no promise about its
origin or wall-clock alignment. No scheduling policy consumes the new value.

## Validation

- `./gradlew-agent test --tests tc.tools.converter.SystemNanoTimeConverterTest
  --no-daemon --console=plain` — passed, 4 tests, 0 failures after rebase.
- `./gradlew-agent dist -x test --no-daemon --console=plain` — passed.
- Release native macOS configure/build passed with 112 Ninja build steps.
- `runSystemNanoTimeSmokeMacOS` passed after `Vm.sleep(20)`; measured monotonic
  delta was 21,011,750 ns.
- `git diff --check origin/master...HEAD` and focused copyright validation
  passed.

## Limitations and deferred work

Android, Windows, Linux, WinCE, and iOS builds were omitted under the task's
platform restrictions. Their native implementations are retained but were not
validated here. Frame pacing, timer scheduling, benchmark clock selectors,
image/prefetch work, and generalized runtime diagnostics remain out of scope.
