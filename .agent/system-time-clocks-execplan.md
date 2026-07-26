<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Replace legacy timestamps with explicit monotonic and Unix clocks

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`.

## Purpose / Big Picture

Implement the standard Java time sources `System.nanoTime()` and
`System.currentTimeMillis()` consistently across every TotalCross runtime platform,
then migrate the runtime and SDK away from the ambiguous native C function
`getTimeStamp()`.

After this work:

- `System.nanoTime()` returns a high-resolution monotonic time source suitable for
  measuring elapsed time, scheduling deadlines, implementing timeouts, ordering
  in-process events, and profiling. Its origin is arbitrary and only differences
  between values are meaningful.
- `System.currentTimeMillis()` returns Unix time in milliseconds, in UTC, suitable
  for dates, persisted timestamps, interoperability, logging, and other wall-clock
  use.
- Native code uses two explicit APIs:

      // Monotonic time, for intervals and deadlines.
      TC_API int64 getMonotonicTimeNano(void);

      // Absolute Unix/UTC wall-clock time.
      TC_API int64 getUnixTimeMillis(void);

- The native C function `getTimeStamp()` and its platform-specific
  `privateGetTimeStamp()` implementations no longer exist.
- The Java API `Vm.getTimeStamp()` remains temporarily for source and binary
  compatibility, is marked deprecated, documents `System.nanoTime()` as its
  replacement, and derives its legacy millisecond result from monotonic nanoseconds.
- All SDK code that currently calls `Vm.getTimeStamp()` uses `System.nanoTime()`
  with units, variable types, comparisons, and overflow behavior updated correctly.
- Native timers, deadlines, timeout loops, event timestamps, GC measurements,
  image-recency tracking, diagnostics, and benchmarks use monotonic nanoseconds.
- Camera and other generated filenames use a dedicated `createUniqueFileName`
  utility rather than a clock API intended for elapsed-time measurement.
- The questionable SDL `handleMouseEvent` behavior is not changed by this plan. It
  is documented and tracked by GitHub issue `TotalCross/totalcross#428`.
- `SSLContextMbedtls.c` contains the monotonic timestamp preparation needed by its
  future timeout loop, but the initialization remains commented while the timeout
  condition remains disabled.

A developer can observe completion by compiling and running a small application
that verifies both standard `System` methods, by running focused native clock tests
on each supported platform, by confirming timeout behavior does not change when
the system clock changes, and by searching the repository to prove that the C
symbol `getTimeStamp` has been removed.

## Working Set and Resume Protocol

Use these supporting files while executing the plan:

- `.agent/state/system-time-clocks.md` as the first read when resuming.
- `.agent/evidence/system-time-clocks.md` as append-only validation evidence.
- `.agent/archive/system-time-clocks-history.md` for completed milestone detail.
- `.agent/reports/system-time-clocks-editorial.md` for the final factual handoff.

Before implementation, read `AGENTS.md`, `.agent/PLANS.md`, the native utility
clock files, `Vm.java`, `Vm4D.java`, `System4D.java`, native method registration
files, and every repository match for the old and new clock symbols.

## Progress

- [x] Reviewed the legacy native clock implementation and classified direct C
  consumers by elapsed-time versus wall-clock purpose.
- [x] Created issue #428 for the deferred SDL `handleMouseEvent` review.
- [x] Defined the compatibility boundary: remove the C symbol while retaining and
  deprecating Java `Vm.getTimeStamp()`.
- [ ] Capture a fresh repository-wide clock-use inventory.
- [ ] Implement native monotonic-nanosecond and Unix-millisecond primitives.
- [ ] Expose `System.nanoTime()` and `System.currentTimeMillis()`.
- [ ] Migrate native elapsed-time consumers.
- [ ] Deprecate `Vm.getTimeStamp()` and migrate SDK callers.
- [ ] Introduce and adopt `createUniqueFileName`.
- [ ] Remove the native legacy clock API.
- [ ] Complete focused and platform validation.

## Current Architecture and Scope

The legacy Java contract describes milliseconds elapsed since program start. The
Windows implementation uses `GetTickCount()` masked to 30 bits, while POSIX uses
`gettimeofday()`, which is a wall-clock source and can jump. The new architecture
must separate elapsed time from civil time explicitly.

`System.nanoTime()` returns `long` nanoseconds from an arbitrary monotonic origin.
`System.currentTimeMillis()` returns `long` Unix milliseconds. Native methods must
return through the VM's established 64-bit result path.

All migrated native deadlines and durations must use `int64`. Millisecond values
must be converted explicitly with `1000000LL`, and elapsed checks should prefer
subtraction rather than wrapping absolute `int32` deadlines.

For POSIX-like targets, prefer `clock_gettime(CLOCK_MONOTONIC, ...)` and
`clock_gettime(CLOCK_REALTIME, ...)`, using `gettimeofday()` only where required by
an older supported target. For Windows, prefer `QueryPerformanceCounter()` with
safe conversion to nanoseconds and an appropriate system-time API converted from
the Windows epoch to Unix milliseconds. Document fallbacks for older Windows CE
baselines.

The instruction to remove `getTimeStamp` applies to the native C helper and its
private platform implementations. Java `Vm.getTimeStamp()` remains temporarily,
is deprecated, and delegates to monotonic nanoseconds converted to legacy
milliseconds.

Do not fix `handleMouseEvent` in this work. Issue #428 owns the later event-modifier
review. Avoid mechanically replacing its value with a new clock call.

In `SSLContextMbedtls.c`, update both timestamp-related expressions to monotonic
nanoseconds, but keep the initialization and timeout condition commented together
for future reactivation.

Create a reusable `createUniqueFileName` utility. Its first implementation uses
`new Time().getTimeLong()` plus a synchronized process-local counter. Prefer moving
default camera filename selection to Java and passing the selected path to native
code.

## Plan of Work

### Milestone 1: Inventory and freeze contracts

Search for `getTimeStamp`, `privateGetTimeStamp`, `firstTS`, `Vm.getTimeStamp`,
`nanoTime`, `currentTimeMillis`, native methods returning `long`, generated native
registration files, camera filename construction, and timing fields such as
`lastGC`, `nextTimerTick`, and `lastAccess`.

Classify every result as monotonic timing, wall clock, compatibility API, filename
uniqueness, generated declaration, test/documentation, disabled code, or deferred
issue #428.

### Milestone 2: Add native clock primitives

Declare and implement:

    TC_API int64 getMonotonicTimeNano(void);
    TC_API int64 getUnixTimeMillis(void);

Add focused tests for monotonic ordering, positive elapsed sleep, plausible Unix
milliseconds, and 64-bit arithmetic. Document suspend behavior and platform
fallbacks.

### Milestone 3: Implement standard Java System methods

Add `public static long nanoTime()` and `public static long currentTimeMillis()` to
the TotalCross `System` compatibility surface. Support both companion/JVM execution
and deployed/native execution. Update native registration source files and
regenerate derived declarations or tables.

### Milestone 4: Deprecate Vm.getTimeStamp

Mark both public and deployed forms deprecated. Javadoc must explain the legacy
millisecond `int` behavior and show the correct replacement:

    long start = System.nanoTime();
    performOperation();
    long elapsedNanos = System.nanoTime() - start;
    long elapsedMillis = elapsedNanos / 1_000_000L;

Correct `tsV_getTimeStamp` to use `getMonotonicTimeNano()` and convert to legacy
milliseconds. Remove wall-clock-based companion startup state.

### Milestone 5: Migrate native monotonic consumers

Migrate timers, deadlines, socket timeouts, event timestamps, long-press timing,
wake polling, GC measurements, image recency, diagnostics, and benchmarks to
`getMonotonicTimeNano()` with `int64` storage and explicit units.

Preserve public field units at intentional boundaries. Do not retain `int32`
internal timing merely under new names.

Convert the two disabled SSL timeout points and leave them commented together.
Do not change SDL mouse event semantics.

### Milestone 6: Migrate SDK callers

Replace production SDK calls to `Vm.getTimeStamp()` with `System.nanoTime()`.
Change storage to `long`, convert thresholds correctly, prefer elapsed subtraction,
and preserve public millisecond fields only through explicit conversions.

Expected categories include events, animation, flick and drag timing, UI effects,
key preprocessing, launcher/event loop timing, profiling, tests, `safeSleep`, and
UI robot scheduling.

### Milestone 7: Add createUniqueFileName

Add the utility with `new Time().getTimeLong()` plus a synchronized counter. Define
prefix, extension, directory, collision, and concurrency behavior. Test multiple
calls in one clock tick.

Migrate camera filename construction in generic and Darwin native paths, preferably
by selecting the default name in Java before native capture.

### Milestone 8: Remove native getTimeStamp

Remove the declaration, wrapper, private platform implementations, typedefs,
`firstTS`, startup initialization, exports, and obsolete tests. Regenerate native
method tables where required.

Final search must show no standalone native `getTimeStamp`, `privateGetTimeStamp`,
or `firstTS` symbol. Java occurrences may remain only for the deprecated API,
bridge name, compatibility tests, migration documentation, and issue references.

### Milestone 9: Platform checkpoint

Validate the host native VM, SDK, Android, Linux, Apple targets, and Windows targets
where environments are available. Run a deployed sample using both standard
methods. Record unavailable platform validation without claiming support based only
on source inspection.

## Decision Log

- Remove the native C API completely while retaining deprecated Java compatibility.
- Use monotonic nanoseconds and `int64` for internal elapsed-time paths.
- Use Unix UTC milliseconds for wall-clock time.
- Preserve public field units unless a separate compatibility change is justified.
- Defer SDL mouse modifier semantics to issue #428.
- Keep both prepared SSL timeout lines disabled together.
- Use `Time.getTimeLong()` plus a synchronized process-local counter for the first
  unique filename implementation.

## Validation and Acceptance

Follow the escalation policy in `AGENTS.md`: static checks, focused tests, focused
integration tests, module builds, smoke deploy, then broader builds only when needed.

Acceptance requires:

- monotonic reads do not decrease;
- measured sleep produces a reasonable positive interval;
- wall-clock changes do not affect available timeout tests;
- Unix milliseconds represent a plausible current instant;
- companion and deployed applications call both standard methods;
- native elapsed-time code uses monotonic nanoseconds;
- generated filenames use the dedicated utility;
- no native C `getTimeStamp` symbol remains;
- Java `Vm.getTimeStamp()` is deprecated and functional;
- production SDK callers no longer invoke it;
- issue #428 remains the owner of deferred SDL behavior.

## Risks and Open Questions

- Old Windows CE targets may require clock fallbacks.
- QPC conversion must avoid overflow and precision loss.
- Old POSIX toolchains may require feature or link adjustments.
- Suspend behavior differs among platform monotonic clocks.
- Legacy `Vm.getTimeStamp()` documented a 30-bit wrap; decide whether compatibility
  requires preserving it.
- Java event timestamps remain `int` milliseconds and require boundary conversion.
- The `System4D` deployment transformation must be inspected before selecting the
  exact native/companion implementation.
- Camera default filename selection may expose platform-specific path assumptions.
- Timestamp-plus-counter names should check existing files when returning a full
  path.

## Idempotence and Recovery

Keep the migration additive until the final removal milestone. Regenerate native
registration files repeatably and verify a second generation produces no diff.
Use isolated build directories and do not remove unrelated local caches or changes.
Record unavailable platform validations explicitly.

No commit, push, or pull request beyond this plan file is automatic. Issue #428 has
already been created for the deferred SDL review.

## Outcomes & Retrospective

Planning establishes explicit monotonic and Unix clock contracts, reconciles Java
compatibility with native removal, separates filename uniqueness from elapsed-time
measurement, prepares the disabled SSL timeout path, and defers SDL event semantics
to issue #428. Implementation outcomes and measured platform support will be added
as milestones complete.

## Revision Note

2026-07-26: Initial ExecPlan created from the policy in
`TotalCross/totalcross-depot-tools/.agent/PLANS.md` and attached to issue #425 on a
dedicated branch.