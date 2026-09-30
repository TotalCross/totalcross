<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# System.nanoTime() support

This plan follows `AGENTS.md` and `.agent/PLANS.md`.

## Context and objective

Deployed TotalCross code needs the Java `System.nanoTime()` elapsed-time API.
Add the API and native implementation with monotonic clocks while leaving
`Vm.getTimeStamp()` and all existing `Vm` APIs unchanged.

## Scope and final behavior

Declare `System4D.nanoTime()` and register the Java `()J` method through the
existing converter/native method path. Keep one native wrapper and one shared
`getNanoTime()` utility entry point. Use `clock_gettime(CLOCK_MONOTONIC)` on
Linux and Android, cached mach timebase conversion on Darwin, cached
QueryPerformanceCounter frequency on Windows desktop, and the existing
monotonic millisecond source scaled to nanoseconds on WinCE. Keep conversions
overflow-safe and never derive this value from wall-clock time.

Preserve API/converter/simulator and structural tests plus a deployed macOS
smoke that checks monotonic increase after a short sleep.

## Compatibility constraints

Keep the native declaration mapped to the `()J` ABI. Do not change the
`Vm.getTimeStamp()` contract or use `nanoTime()` to alter scheduling behavior.
Retain source implementations for platforms that cannot be built here.

## Validation

Run focused converter/API/structural SDK tests, the SDK distribution build,
and a native macOS Release configure/build plus deployed macOS smoke. Do not
build Android, Windows, Linux, WinCE, or iOS targets.

## Out of scope

Exclude frame pacing, timer scheduling, benchmark clock selectors, image or
prefetch work, and unrelated platform or runtime diagnostics.
