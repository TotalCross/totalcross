<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Standard streams V1 editorial handoff

The original corrected handoff is recorded at implementation/documentation
closure commit `888537b29`; the closed-flush compatibility correction follows
in `b662c3939` and `cd5d5a0d8`, with final documentation closure in
`37d4c0ec8`.

## Editorial Summary

Standard streams V1 gives TotalCross independent Java `System.out` and
`System.err` channels backed by native routing, while retaining compatibility
with `Vm.debug` and `DebugConsole.txt`. The corrected macOS smoke demonstrates
separate parent-process capture, shared standard-stream file output, and the
historical stdout-only desktop macOS `Vm.debug` path.

## Original Plan versus Actual Outcome

The planned Java core, Java/native ABI bridge, five required platform targets
(POSIX/Linux, Windows, Android, macOS, and iOS), shared legacy writer, review
corrections, smoke coverage, bounded plan artifacts, and final SDK/macOS gate
were delivered. macOS was the only target locally built and
runtime-smoke-tested; the Darwin adapter is shared by macOS and iOS. Windows,
Linux, Android, and iOS validation was intentionally deferred because the plan
forbids those local builds.

## What Changed

- `PrintStream4D` now implements the supported non-formatter stream contract.
- Value-bearing `println` calls cross the stream boundary as one byte record;
  null `char[]`, append ranges, underlying trouble, and encoding scope match
  the corrected V1 compatibility contract.
- `System4D` exposes distinct OUT and ERR streams over
  `VmStandardOutputStream`.
- Native metadata, registration, lifecycle, routing, and platform adapters
  were added for POSIX, Windows, Android, and Darwin/iOS-family sinks.
- Legacy debug backends delegate file ownership to one shared
  `DebugConsole.txt` writer; standard streams also feed that writer.
- Desktop macOS legacy debug remains stdout-only; Windows explicit standard
  flush now uses `FlushFileBuffers`, while legacy Windows debug remains
  `fflush`-only.
- `PrintStream4D.flush()` after close now marks trouble as JDK-compatible,
  while `close()` remains idempotent and the regression sequence is covered.
- `VmStandardOutputStream` is excluded from `totalcross-api` and covered by a
  dedicated artifact-boundary test.
- A focused Java test, system-stream test, smoke fixture, and macOS Gradle
  runner provide coverage.

## Decisions and Trade-offs

The router keeps standard streams separate from `Vm.debug`, so debug-disable
and erase commands retain their historical meaning. Logical stream close does
not close the shared legacy file. Auto-flush is a logical flush; explicit
`flush()` reaches the durable underlying contract. No formatter, timestamp,
rotation, retention, or structured logging policy was introduced.

## Unexpected Problems and Discoveries

The first native smoke used `java.nio.charset.StandardCharsets`, which is not
available in the deployed SDK. The implementation was corrected to use the
SDK-supported UTF-8 encoding path and the smoke then passed. Review then
exposed that the initial smoke assertions had accidentally required new
macOS legacy file behavior; those assertions and the backend were corrected.
Native-method generation also revealed unrelated generated-file drift; the
change retained only the focused standard-stream metadata. The original plan
exceeded its soft size limit and was consolidated into bounded active and
reference artifacts. A final compatibility review found the closed-flush
trouble-state deviation; it was fixed without touching native code.

## Validation and Measurable Results

Final focused SDK tests, including the closed-flush regression, the dedicated
artifact-boundary task, `clean dist`,
macOS native configure/build, headers, diff checks, static scope checks,
bounded file-size checks, and the corrected macOS smoke passed. The smoke
reported `stdoutBytes=201`, `stderrBytes=77`, and `debugConsoleBytes=233`,
with assertions for independent capture, shared standard records, and legacy
macOS stdout-only markers. The complete command/log index is in the evidence
file.

## Useful Evidence and Examples

The smoke fixture writes OUT and ERR before and after erase, exercises explicit
flush and disabled-debug behavior, and verifies that `Vm.debug` markers stay
out of the desktop macOS legacy file. The final corrected agent log is
`TotalCrossSDK/agent-logs/20260917-203407-runStandardStreamsSmokeMacOS-agent.log`.

## Limitations, Remaining Work, and Open Questions

Other platform builds and runtime smoke remain for CI or a future permitted
validation run. Formatter support and configurable rolling `DebugConsole.log`
infrastructure are deliberately V2 work. Platform-specific assumptions,
including Windows durable flush and Android/iOS identifiers, should be
verified by the normal platform pipelines.

## Possible Article Angles

- Adding native stdout/stderr capture without breaking a legacy debug API.
- Separating logical stream lifecycle from shared durable file ownership.
- Using a platform router to keep Java compatibility behavior small and testable.

## Suggested Narrative

Start with the benchmark observability gap: native output was historically
coupled to `Vm.debug`. Introduce independent Java streams and a narrow native
bridge, then show the shared legacy writer and the macOS smoke evidence. Close
with the deliberate V2 boundary around formatting and rolling logs.

## Claims Requiring Human Review

Review platform-specific sink behavior in CI on Windows, Linux, Android, and
iOS, especially inherited-handle behavior, Android package context, and Darwin
Unified Logging identifiers. Review the compatibility impact of the supported
UTF-8 path against any downstream SDK profiles.
