<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Standard streams V1 history

## Scope

This archive records the completed execution of
`.agent/plans/standard-streams-v1.md` from base
`5917a4aa3e20123a1ff02c1a5b0cedd9640c0c6b` on branch
`feat/standard-streams-v1`. The work adds first-class standard output and
error streams while preserving the legacy `Vm.debug` and `DebugConsole.txt`
contracts.

## Milestones

### M0 — plan and branch

The supplied plan was committed as `ed56aff81`. The branch was created from the
refreshed remote base. Its body-format check reported one line over 80
characters; the commit was intentionally not amended.

### M1 — Java stream core

Commit `b8eefe31c` replaced the formatter-shaped `PrintStream4D` behavior with
the supported stream core: byte and character writes, primitive and object
printing, append overloads, UTF-8 encoding, error state, locking,
auto-flush, close, and protected error controls. Focused `PrintStream4DTest`
coverage passed. The commit retained one historical 401-character body line
reported by the message checker.

### M2 — Java/native router

Commit `8e644678a` added `VmStandardOutputStream`, distinct `System.out` and
`System.err` instances, native method metadata, lifecycle registration, and
platform sinks. The native router fans out to the selected platform sink and
the shared legacy writer without routing standard streams through `Vm.debug`.
The permitted macOS native build and focused SDK tests passed. Native-method
generation exposed unrelated pre-existing generated drift; only the three
focused symbols were retained.

### M3 — legacy compatibility and smoke

Commit `a9e973480` extracted one synchronized lazy `DebugConsole.txt` writer
and localized the existing POSIX, Android, Windows, and desktop macOS debug
backend changes. Standard-stream logical close does not close the shared file;
explicit flush retains the durable contract, while auto-flush avoids fsync per
line. Commit `67bcc1076` changed UTF-8 conversion to an API available in the
deployed SDK after the first smoke exposed an unavailable `StandardCharsets`
class. Commit `a6edf33dc` added the standard-stream smoke fixture and focused
Gradle runner. The corrected macOS smoke passed with separate stdout/stderr
capture and shared-file assertions.

### M4 — final gate

The final focused SDK tests, clean SDK distribution, permitted macOS native
configure/build, and macOS smoke passed. Headers, whitespace, prohibited
scope, new-file sizes, and all task commit messages were audited. The two
historical message deviations remain documented; all later commits pass.

### Review correction pass

The follow-up review identified six implementation issues and one plan
artifact issue. Commits `a4c321804` and `a0011d954` corrected the Java subset:
null `char[]` now throws, underlying `PrintStream` trouble propagates,
generic encoding remains platform-default, standard encoding remains UTF-8,
and each value-bearing `println` is one write. Commit `cfeb4f51c` restored
desktop macOS `Vm.debug` stdout-only behavior, retained Windows legacy
`fflush`, and added Windows desktop `FlushFileBuffers` for durable explicit
stream flush. Commit `e36203795` removed the internal bridge from
`totalcross-api` and added boundary coverage. Commit `4d0ded3db` replaced the
oversized active plan with bounded plan/reference artifacts. The corrected
macOS smoke retains pre-erase standard records, rejects legacy debug markers
in the file, and requires legacy macOS markers on stdout only.

The corrected gate passed focused SDK tests, the dedicated artifact-boundary
task, clean `dist`, native macOS configure/build, and the updated macOS smoke.
The correction closure documentation was committed in `888537b29`.

### Closed-flush compatibility correction

Review found that `PrintStream4D.flush()` silently returned after close. Commit
`b662c3939` makes that path set trouble without changing idempotent `close()`;
commit `cd5d5a0d8` updates the existing close test and preserves the required
close/reset/flush/`checkError()` regression. The focused `PrintStream4DTest`
and `System4DTest` rerun passed. No native source or architecture changed.
The correction documentation was closed in `72e34070d`.

### Closed check-error compatibility correction

The first closed-flush fix made `checkError()` call the new closed-flush error
path and therefore changed a clean post-close check to true. Commit
`566aed163` makes `checkError()` flush only while open, still propagates an
underlying `java.io.PrintStream` trouble state while open, and leaves explicit
closed `flush()` error reporting unchanged. The focused test now independently
asserts clean `close()`/`checkError()` and close/reset/flush/`checkError()`.
The documentation closure was recorded in `de68b18da`.

## Decisions preserved

- OUT and ERR have independent Java stream state and native channels.
- Platform routing is explicit: POSIX fd 1/2, inherited Windows handles,
  Android INFO/WARN with package context, and Darwin Unified Logging with
  bundle context.
- The shared legacy file remains `DebugConsole.txt`; no formatter, rotation,
  retention, timestamp, or structured-logging policy was added.
- `Vm.debug` retains debug commands and disabling semantics.
- Windows, Linux, Android, and iOS builds were not run because the plan
  explicitly forbids them. Full image benchmark matrices were not part of V1.
- The active plan is 12,474 bytes/272 lines and the technical reference is
  6,569 bytes/146 lines; both satisfy the bounded plan-artifact guideline.

## Evidence

Command-level records and log paths are append-only in
`.agent/evidence/standard-streams-v1.md`. The final smoke agent log reports
`stdoutBytes=201`, `stderrBytes=77`, and `debugConsoleBytes=189`.
