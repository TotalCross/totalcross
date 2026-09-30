<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Semaphore V1 reconstruction plan

## Goal

Reconstruct the bounded Semaphore V1 compatibility surface from the historical
implementation on current master, which provides `System.nanoTime()`. Treat
that API as a required monotonic clock dependency. Do not add a duplicate
nanoTime implementation or bring along image preparation, prefetch, benchmark,
or Windows packaging/CI work.

## Scope

- Expose native-backed `Semaphore(int)`, `acquire()`,
  `acquireUninterruptibly()`, `tryAcquire()`, and `release()` through the SDK
  compatibility mapping.
- Implement a nonfair, untimed, single-permit semaphore over a reusable native
  blocking thread condition primitive. Keep test diagnostics opt-in and absent
  from the default SDK/runtime build.
- Add converter coverage for the exact supported API, deterministic correctness
  and stress smoke coverage, and a macOS wake-latency smoke fixture. Measure
  elapsed time with `System.nanoTime()` and keep deltas in nanoseconds.
- Register the native source through CMake and the current native method
  inventory. Leave `Android.mk` and the legacy VC2008 project unchanged.
- Validate only SDK and native macOS builds, as requested.

## Commits and validation

Keep this branch to a small sequence: this plan first, native blocking and
semaphore support, SDK mapping and focused coverage, then the report last.
Run focused converter tests, the SDK distribution build, a native macOS Release
build, and a deployed macOS smoke if the available build artifacts permit it.
Do not build Android, Windows, Linux, WinCE, or iOS targets. Do not add or run
benchmarks.
