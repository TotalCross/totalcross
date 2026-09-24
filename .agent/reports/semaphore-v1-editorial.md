<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Semaphore v1 — Part 1 editorial handoff

## Editorial Summary

Part 1 adds the deployed TotalCross compatibility surface for
`java.util.concurrent.Semaphore` and proves its core behavior on native macOS.
The converter maps the Java owner to `jdkcompat.util.concurrent.Semaphore4D`.
The implementation uses OS condition waits, signed permit counts, and an
auto-reset event with chained wakeups on Windows.

## Original Plan versus Actual Outcome

The planned v1 API, base revision, execution branch, source inventories,
macOS-only native validation, and Part 2 handoff were followed. A converter
test found that the generated prototype declarations were missing from the
declaration section of `NativeMethodsPrototypes.txt`; a separate focused fix
added them, and the rerun passed. No Semaphore consumer was changed.

## What Changed

- Added `THREAD_CONDITION_TYPE` and OS wait/signal/destroy macros in
  `TotalCrossVM/src/tcvm/tcthread.h`.
- Added native Semaphore state and single-permit operations in
  `TotalCrossVM/src/nm/util/concurrent_Semaphore.c`.
- Registered the six native bridges in the metadata, generated prototype,
  header, native-address, CMake, Android.mk, and legacy Visual Studio files.
- Added the Java compatibility class, converter coverage, and a deployed
  macOS smoke with ready/completion semaphore handshakes.

## Decisions and Trade-offs

The public surface remains non-fair and untimed, with one-permit operations
only. Negative initial counts are kept as signed values. `acquire()` preserves
its Java declaration but is effectively uninterruptible on TotalCross because
the VM does not currently provide Java-compatible thread interruption.
Windows uses auto-reset events for legacy platform compatibility; the permit
count remains authoritative and a consuming waiter re-signals when permits and
waiters remain.

## Unexpected Problems and Discoveries

The first converter run passed API mapping but failed registration coverage
because only the generated implementation stubs had been added. Adding the six
prototype declarations fixed the mismatch. The SDK distribution task completed
successfully while its agent summary reported 100 Javadoc errors and 100
warnings; those diagnostics were not attributed to this change.

The local commit-message check failed for the planning commit and the condition
primitive commit because their body lines exceeded 80 characters (the first
also contained literal `\n` text). The plan prohibits amending earlier
commits, so those commits remain unchanged. Later commit-message checks passed.

## Validation and Measurable Results

- `./gradlew-agent test --tests tc.tools.converter.SemaphoreConverterTest`:
  passed, 2 tests.
- `NativeMethodsPrototypeGenerator`: passed, all 6 prototypes and registrations
  matched the checked-in metadata.
- `./gradlew-agent dist -x test`: passed in 18 seconds.
- `./gradlew-agent compileSmokeTestJava`: passed in 2 seconds.
- CMake Release/Ninja configure: passed; `ninja -C build-semaphore tcvm` passed
  in 7 seconds with 123 build steps.
- `runSemaphoreSmokeMacOS` against the exact generated macOS dylib: passed in
  5 seconds and emitted `fixture=SemaphoreSmokeApp,overallPass=true`.
- Focused copyright-header and diff checks passed for the changed source and
  execution artifacts.

Detailed commands, counts, logs, and the initial converter failure are indexed
in `.agent/evidence/semaphore-v1.jsonl`.

## Useful Evidence and Examples

The native implementation loops while `permits <= 0`, stores each release in
the signed count, signals one waiter per release, and throws
`Error("Maximum permit count exceeded")` at the positive integer limit. The
macOS smoke covers release-before-acquire, both acquire forms, negative
permits, three waiters, and overflow without sleep or polling loops.

## Limitations, Remaining Work, and Open Questions

Windows, Android, Linux, and iOS native builds and execution remain unvalidated
in this part. The Windows event path is source-integrated but not proven by a
Windows run. Stress, race, and higher-load coverage belongs to
`.agent/plans/semaphore-v1-part-2-stress.md`. Review the two invalid commit
bodies before proposing this branch for merge; the plan's no-amend rule was
preserved.

## Possible Article Angles

- Using a permit count as the source of truth behind a blocking condition.
- Chaining auto-reset-event wakeups without polling on legacy Windows targets.
- Testing blocked progress with handshakes and a process timeout rather than
  timing sleeps.

## Suggested Narrative

Start with the converter compatibility boundary, explain how signed permit
state works with POSIX conditions and Windows auto-reset events, then show the
macOS build and handshake-driven smoke. Close with the cross-platform
validation limits and the planned Part 2 stress work.

## Claims Requiring Human Review

- Confirm the Windows/WinCE event API availability and behavior on supported
  toolchains before claiming Windows runtime support.
- Decide how the two commit-message validation failures should be handled under
  repository merge policy; this execution did not rewrite history.
- Attribute the Javadoc diagnostics against a baseline before treating them as
  a regression or a clean documentation result.
