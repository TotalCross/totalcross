<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Standard streams V1

This plan follows `AGENTS.md` and `.agent/PLANS.md`.

## Context and objective

`System.out` and `System.err` currently share the legacy debug stream. Give
deployed TotalCross applications independent standard output and error
channels while preserving `Vm.debug` and `DebugConsole.txt` behavior.

## Scope and final behavior

Provide the supported `PrintStream4D` byte and character writes, primitive and
object printing, `println`, append, locking, auto-flush, error state, and close
semantics. Standard streams encode as UTF-8; generic wrapped streams keep the
platform-default encoding. Preserve null `char[]` behavior, one-write
value-bearing `println`, closed-flush trouble reporting, idempotent close, and
the final closed `checkError()` and wrapped-stream rules.

Give OUT and ERR independent Java state and native channels. Route each stream
explicitly to POSIX, Android, Darwin, or Windows sinks without calling
`Vm.debug`. Keep the synchronized lazy `DebugConsole.txt` writer shared;
closing one stream does not close it. Preserve durable explicit flush where
required, avoid durable sync on auto-flush, retain Windows file durability, and
keep desktop macOS `Vm.debug` stdout-only behavior. Use integer ABI types for
public native boolean-like stream parameters and returns.

Keep `VmStandardOutputStream` internal to runtime artifacts. Retain focused
PrintStream tests and the deployed macOS smoke fixture.

## Compatibility constraints

Preserve existing `Vm.debug` commands and enable/disable behavior. Standard
stream changes must not alter `Vm` APIs or expose the internal bridge through
the public API artifact.

## Validation

Run the focused SDK PrintStream, System, and artifact-boundary tests. At
milestone close, run the SDK distribution and permitted native macOS Release
build plus deployed standard-stream smoke. Do not build Android, Windows,
Linux, WinCE, or iOS targets.

## Out of scope

Exclude `System.nanoTime()`, image or raster work, prefetch, frame pacing,
benchmarks, generalized diagnostics, and unrelated runtime logging policy.
