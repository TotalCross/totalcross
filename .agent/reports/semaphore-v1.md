<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Semaphore V1 reconstruction report

## Result

Reconstructed the nonfair, untimed, single-permit `Semaphore` surface on top of
current master, which already provides deployed `System.nanoTime()`. This
branch adds no duplicate monotonic clock implementation. The SDK maps only
one-argument construction,
`acquire()`, `acquireUninterruptibly()`, `tryAcquire()`, and `release()`. Native
state uses a signed permit count, a mutex, and a blocking condition. Test-only
waiter diagnostics are available to smoke sources only and compile/register
only when `TC_ENABLE_SEMAPHORE_TEST_DIAGNOSTICS=ON` (default `OFF`).

The native semaphore source is registered through CMake. Legacy `Android.mk`
and `TCVM.vcproj` remain unchanged. The branch does not include the historical
image prefetch, benchmark, or Windows packaging/CI work. Wake latency is timed
with monotonic `System.nanoTime()` values and kept in nanoseconds.

## Validation

- `./gradlew-agent test --tests tc.tools.converter.SemaphoreConverterTest
  --tests tc.tools.converter.SystemNanoTimeConverterTest --no-daemon
  --console=plain` — passed, 7 tests total.
- `./gradlew-agent dist -x test --no-daemon --console=plain` — passed.
- `./gradlew-agent compileSemaphoreSmoke compileSemaphoreStressSmoke compileSemaphoreWakeLatencySmoke --no-daemon --console=plain` — passed.
- Native macOS Release Ninja builds with diagnostics OFF and ON — passed,
  113 steps each using the existing CMake configurations. A fresh configure
  could not fetch the pinned qrcodegen macOS/arm64 asset (HTTP 404).
- Deployed macOS `SemaphoreSmokeApp` — passed, 7 checks.
- Deployed macOS `SemaphoreStressSmokeApp` — passed, 20,000 produced and acquired handoffs.
- Deployed macOS `SemaphoreWakeLatencySmokeApp` — passed, 200 measured samples,
  220 blocked-waiter handshakes, and 3 simultaneously confirmed waiters.
  Latency summary: min 1,666 ns, p50 3,646 ns, p95 33,500 ns,
  max 4,436,625 ns, mean 30,100 ns.
- Focused copyright validation and `git diff --check` — passed.

Android, Windows, Linux, WinCE, and iOS builds were not run, per the requested
platform scope. No benchmark was run.
