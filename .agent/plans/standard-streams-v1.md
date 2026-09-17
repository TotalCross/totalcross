<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Add first-class standard output streams with legacy DebugConsole support

## Purpose / Big Picture

Complete Standard Streams V1 for TotalCross. Java `System.out` and
`System.err` must be independent, directly capturable native streams while
remaining compatible with the existing `Vm.debug()` and
`DebugConsole.txt` behavior. The implementation must remain additive and
must not introduce formatter or rolling-log policy.

The required data path is:

    System.out/err -> PrintStream4D -> VmStandardOutputStream
      -> StandardStreamRouter -> platform sink + shared DebugConsole writer

`Vm.debug()` remains outside `StandardStreamRouter`.

## Branch and execution boundary

- Branch: `feat/standard-streams-v1`.
- Base: `5917a4aa3e20123a1ff02c1a5b0cedd9640c0c6b`.
- Do not push.
- Build only the SDK and native macOS target.
- Do not build Windows, Linux, Android, or iOS locally.
- Do not run the full image benchmark matrix.
- Commit task artifacts, but never ordinary build outputs or logs.
- Preserve unrelated user files and generated dependency caches.

## Working Set and Resume Protocol

Read `.agent/state/standard-streams-v1.md` first. It records the active slice,
last commit, focused evidence, deferred validation, and unrelated local files.
Read `.agent/reference/standard-streams-v1-reference.md` for the compact
architecture and compatibility contract. Read
`.agent/evidence/standard-streams-v1.md` for command-level evidence and
`.agent/archive/standard-streams-v1-history.md` only for historical rationale.
The final factual handoff is
`.agent/reports/standard-streams-v1-editorial.md`.

Resume with:

    git switch feat/standard-streams-v1
    sed -n '1,220p' .agent/state/standard-streams-v1.md

Inspect only the active paths named in state before expanding the investigation.

## Progress

- [x] M0: establish the branch, plan, state, and evidence records.
- [x] M1: implement the supported non-formatter `PrintStream4D` core and
  focused tests.
- [x] M2: add the Java/native bridge, router, platform sinks, metadata, and
  lifecycle; pass the permitted macOS native build.
- [x] M3: extract the shared legacy writer, add the macOS smoke fixture/runner,
  and prove standard-stream routing.
- [x] M4: pass the original final SDK/macOS gate and produce the initial
  handoff artifacts.
- [x] 2026-09-17 M5: applied the review corrections, split the oversized plan
  into bounded plan/reference/history artifacts, reran the required final
  SDK/macOS/smoke gate, and prepared the final HEAD records.

## Current Architecture and Scope

`System4D.out` and `.err` are separate `PrintStream4D` instances with separate
locks, trouble state, and `VmStandardOutputStream` channels. The Java bridge
validates channel/range inputs and exposes logical versus durable flush.

The native router uses one mutex for standard-stream calls, then the shared
legacy writer's recursive mutex. A single native write is the atomic unit.
Both available sinks are attempted and aggregate failure is returned. Missing
Windows inherited handles are unavailable rather than errors. Standard streams
never call `Vm.debug`, `debugStr`, `privateDebug`, or `closeDebug`.

The shared writer owns lazy `<appPath>/DebugConsole.txt` access, raw bytes,
flush, erase, separator, close, and shutdown. It must not change the caller's
legacy semantics.

## Required contracts

### Java stream core

Implement only the V1 subset:

- `write(int)`, `write(byte[])`, ranged write, and `writeBytes(byte[])`;
- primitive, `char[]`, `String`, and `Object` print/println overloads;
- all three `append` overloads;
- `flush`, `close`, `checkError`, `setError`, and `clearError`.

Follow JDK behavior where practical: String/Object null prints `null`, null
`char[]` throws `NullPointerException`, invalid append ranges fail,
I/O failures set trouble without escaping ordinary print calls, output after
close sets trouble, close is idempotent, and underlying `java.io.PrintStream`
trouble propagates through `checkError()`.

Each value-bearing `println` must build `value + '\n'` and make one underlying
write. `println()` may make one newline write. Do not promise atomicity across
separate application calls such as `print("a"); print("b")`.

Do not add `printf`, `format`, `Formatter`, file/filename constructors, or
additional charset constructors.

Generic `PrintStream4D` instances retain platform-default `getBytes()` behavior.
Only direct `VmStandardOutputStream` instances use Java UTF-8 conversion before
the native bridge.

### Flush and lifecycle

Standard auto-flush is logical only: it emits platform text and performs
`fflush`-equivalent legacy behavior, never durable sync per line. Explicit
`System.out.flush()` and `System.err.flush()` request durable sync. Logical
stream close must not close inherited OS handles or the shared file.

POSIX/Android legacy `Vm.debug` retains per-message durable sync. Windows
legacy debug retains `fflush` without durable sync. Windows desktop explicit
standard flush uses CRT `_fileno`/`_get_osfhandle` and `FlushFileBuffers`;
WinCE keeps its historical behavior and ignores the new durable primitive.

### Legacy debug behavior

Desktop macOS `Vm.debug()` remains stdout-only, including literal
`ERASE_DEBUG`, and does not create, write, or erase `DebugConsole.txt`.
Android/POSIX erase, newline, and durability behavior remain as on the base
branch. Windows console/debug behavior remains unchanged.

Standard stream bytes are literal. `ERASE_DEBUG`, `ALTERNATIVE_DEBUG`, and
`Vm.disableDebug` have no special meaning for standard streams. Standard
output/error still feed the shared legacy file.

### Platform sinks

- Linux/POSIX: fd 1/2 with `write`, EINTR retry, and partial-write handling.
- Windows: inherited `STD_OUTPUT_HANDLE`/`STD_ERROR_HANDLE` with `WriteFile`;
  never allocate or close a console.
- Android: INFO/WARN, real cached package ID tag, separate bounded 4 KiB line
  buffers, and `TotalCross` only as a defensive fallback.
- macOS desktop: POSIX fd 1/2 so parent capture remains redirectable.
- iOS/Darwin: Unified Logging INFO/ERROR with bundle ID subsystem and
  `System.out`/`System.err` categories.

Keep standard-stream sources in dedicated native files; do not move them into
`debug.c`, `utils.c`, or `Vm.c`.

### Artifact boundary

`VmStandardOutputStream` may remain public for converter/runtime reasons, but it
must be excluded from `totalcross-api` and covered by an artifact-boundary
test. It may remain in `totalcross-runtime-java`.

## Plan of Work

### Completed implementation milestones

1. Java core and focused tests (`b8eefe31c`).
2. Bridge/router/platform metadata and lifecycle (`8e644678a`).
3. Shared writer and localized debug-backend delegation (`a9e973480`),
   supported deployed UTF-8 path (`67bcc1076`), and native smoke coverage
   (`a6edf33dc`).
4. Initial final gate and handoff artifacts (`535e3f14a`, `39dcb0b41`,
   `c3cba7e9b`).

### M5 review correction slices

1. Preserve macOS/Windows/base legacy semantics and add Windows durable
   explicit flush.
2. Align `PrintStream4D` null, append, error, encoding, and atomic-record
   behavior; add focused tests.
3. Enforce the runtime bridge artifact boundary and test it.
4. Update the smoke to distinguish standard-file routing from macOS legacy
   stdout-only behavior.
5. Replace this oversized active plan with this bounded plan plus the bounded
   technical reference; update state/evidence/archive/report.
6. Run the final SDK/macOS/smoke and static acceptance gate.

## Decision Log

- Preserve the dedicated standard-stream native files and existing router.
- Keep `Vm.debug()` outside the router and treat the writer as shared storage,
  not a shared caller policy.
- Use one native write for each `println` record.
- Scope UTF-8 to the standard bridge; do not change generic stream encoding.
- Keep the bridge internal at the API artifact boundary.
- Make explicit flush durable on Windows desktop without changing WinCE.
- Defer formatter and configurable rolling `DebugConsole.log` work to V2.

## Validation and Acceptance

At correction/final closure, run only the following build operations:

    cd TotalCrossSDK
    ./gradlew-agent test --tests 'jdkcompat.io.PrintStream4DTest' \
      --tests 'jdkcompat.lang.System4DTest' \
      --tests 'tc.tools.ArtifactBoundariesTest' \
      --no-daemon --console=plain
    ./gradlew-agent clean dist --no-daemon --console=plain
    cd ../
    cmake -S TotalCrossVM -B /tmp/tc-standard-streams-v1-final-corrected \
      -DCMAKE_BUILD_TYPE=Release -G Ninja
    cmake --build /tmp/tc-standard-streams-v1-final-corrected
    cd TotalCrossSDK
    ./gradlew-agent runStandardStreamsSmokeMacOS \
      -PtcvmDylib=/tmp/tc-standard-streams-v1-final-corrected/libtcvm.dylib \
      --no-daemon --console=plain

Focused tests/static checks must cover atomic `println`, null `char[]`, error
propagation, standard-only UTF-8, logical versus explicit flush, and the API
boundary. The smoke must prove stdout/stderr separation, shared standard output
in `DebugConsole.txt`, literal standard `ERASE_DEBUG`, unaffected standard
streams under `Vm.disableDebug`, and desktop macOS `Vm.debug` stdout-only with
no legacy-file marker or erase.

Also run header validation, `git diff --check`, new-file size/line checks,
static platform mapping review, prohibited-feature/source-scope review, and
commit-message validation for every task commit. Historical M0/M1 message
deviations may remain only if recorded; all new commits must pass.

## Risks and Open Questions

- Windows and WinCE cannot be locally compiled; inspect preprocessor guards
  and rely on the normal platform pipeline.
- Darwin Unified Logging cannot be locally compiled under the restricted
  validation policy; keep it isolated from the desktop POSIX path.
- Converter/runtime packaging may require the bridge class in the runtime jar,
  but it must not become supported API.
- Preserve the base debug path and exact caller-specific flush/erase behavior.

## Idempotence and Recovery

Do not use destructive Git commands or clean unrelated caches. Re-running
metadata generation may touch generated files; retain only the focused bridge
symbols. Re-running the smoke may replace only its own temporary deployment.
Build logs belong under `/tmp/tc-standard-streams-v1-*` or ignored agent-log
directories and must not be committed.

Before each logical commit, stage only intended paths, run cached diff and
header checks, inspect the staged stat, and validate the created message.
Update state after each logical correction slice and append compact evidence.

## Outcomes & Retrospective

V1 now preserves desktop macOS stdout-only `Vm.debug`, Windows legacy
`fflush`, POSIX/Android legacy durability, atomic value-bearing `println`
records, JDK-compatible null/error behavior, standard-only UTF-8, the internal
bridge artifact boundary, and durable Windows desktop explicit flush. The five
required targets are POSIX/Linux, Windows, Android, macOS, and iOS, with a
shared Darwin adapter; only macOS was locally built and smoke-tested. Formatter
and rolling-log work remains intentionally absent. The benchmark can capture
native stdout/stderr without `Vm.debug()`.

The oversized supplied plan was consolidated into this bounded active plan and
`.agent/reference/standard-streams-v1-reference.md`; history, evidence, and
editorial handoff remain in their bounded supporting files.

The bounded technical reference, history archive, evidence index, and editorial
report are required final artifacts. Every new plan-related file must remain
below approximately 20 KiB and 600 lines.
