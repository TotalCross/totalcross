<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Add `java.lang.System.nanoTime()` support

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`.

## Purpose / Big Picture

Expose Java-compatible `System.nanoTime()` to converted/device applications while
leaving the Java simulator on the JDK implementation. The converted class will
use `jdkcompat.lang.System4D`, which the converter presents as
`java.lang.System`; the VM will resolve the resulting native method to a
platform monotonic clock returning nanoseconds in an `int64`/Java `long`.

## Working Set and Resume Protocol

The active implementation and test paths are the `System4D` API,
`TotalCrossSDK/src/main/java/tc/tools/converter/`, the native method metadata
under `TotalCrossVM/src/nm/`, the platform helpers under
`TotalCrossVM/src/util/`, and the native smoke-test/build wiring under
`TotalCrossSDK/src/smokeTest/` and `TotalCrossVM/CMakeLists.txt`.

Supporting files are:

- `.agent/state/system-nanotime.md`: rewritten after each milestone; read first
  when resuming to find the active slice, last commit, focused validation, and
  next command.
- `.agent/evidence/system-nanotime.md`: compact append-only validation records;
  consult selectively when reporting prior results.
- `.agent/archive/system-nanotime-history.md`: completed milestone detail, read
  only when a later change needs historical rationale.
- `.agent/reports/system-nanotime-editorial.md`: final factual handoff, updated
  at milestone completion and plan completion.

The pre-existing untracked benchmark artifacts, logs, generated launcher files,
and plans listed by the initial scoped status check are deliberately out of
scope and must remain untouched.

## Progress

- [x] (2026-09-14) Read the user-provided requirements, repository guidance, and
  execution-plan rules; switched from `perf/image-scroll-distributed-benchmark`
  through updated `perf/image-scroll-prefetch` to `feat/system-nanotime`.
- [x] (2026-09-14) Milestone 1: add the `System4D` API, converter coverage,
  native metadata contract, and focused tests; commit `276b5c6c6`.
- [x] (2026-09-14) Milestone 2: add shared POSIX, Darwin, Win32, and WinCE
  monotonic-clock implementations and native structural coverage; macOS native
  build passed; commit the logical slice.
- [x] (2026-09-14) Milestone 3: add and run the macOS native smoke test, then
  perform only the permitted final SDK/macOS validations.

## Current Architecture and Scope

`System4D` is the device-owned replacement for `java.lang.System`; it currently
contains the `arraycopy` bridge, while `GlobalConstantPool` has a special
`arraycopy` mapping to `totalcross/sys/Vm`. General device method resolution
finds `jdkcompat.lang.System4D` for `java.lang.System` calls and the converter
removes the `4D` class suffix when emitting device names.

Native methods are declared in `TotalCrossVM/src/nm/NativeMethods.txt`. The
prototype/header and native address table are generated conventionally from
that source-of-truth, with a Java class method such as
`java/lang/System.nanoTime()` mapping to `jlS_nanoTime`. The native wrapper
belongs near the other `java.lang` methods in a new `TotalCrossVM/src/nm/lang/System.c`.

Existing `getTimeStamp()` remains based on `privateGetTimeStamp()` and
`firstTS`; no `Vm` API or implementation is to be changed. New clock helpers
will be exposed through `utils.h`, implemented in the platform-selected
`utils_c.h`, and called only by `jlS_nanoTime`.

Platform contract:

- POSIX Linux and Android: `clock_gettime(CLOCK_MONOTONIC, ...)`.
- Darwin macOS and iOS: `mach_absolute_time()` with cached, safely initialized
  `mach_timebase_info_data_t` and overflow-safe integer quotient/remainder
  conversion.
- Win32 desktop: cached `QueryPerformanceFrequency()` plus
  `QueryPerformanceCounter()` and overflow-safe quotient/remainder conversion,
  without `__int128`.
- WinCE: the existing `privateGetTimeStamp()` source multiplied by
  `1_000_000`, with no new clock.

## Plan of Work

### Milestone 1 — API and conversion

Add `public static native long nanoTime();` to `System4D`, add the
`java/lang/System` declaration and generated registration artifacts for the
`jlS_nanoTime` symbol, and include focused JUnit coverage that checks the
device-owned declaration, the Java `long` descriptor, and converter resolution
of a class that invokes `System.nanoTime()`. Add direct simulator tests for
monotonicity, elapsed nanoseconds, and rapid-call coherence using the JDK
`System.nanoTime()` naturally. Do not run an SDK build until this milestone is
complete; use only focused tests/static checks before its commit.

Acceptance: converted bytecode identifies the call as `java/lang/System` with
the `()J` signature, the device class resolves `nanoTime`, and all generated
native metadata names agree on `jlS_nanoTime`.

### Milestone 2 — native implementations

Implement the shared helper declarations and platform bodies, add the new native
source to CMake, and add native unit/structural checks for the wrapper and
platform formulas. Keep `getTimeStamp()` untouched except for any required
header includes that do not alter its behavior. Use integer arithmetic for all
conversions and cache only immutable clock calibration data.

Acceptance: each platform branch has the required clock source and no forbidden
source (`gettimeofday` for nanoTime, QPC on WinCE, or `__int128`) while the
native wrapper returns the helper value in `p->retL`.

### Milestone 3 — native validation

Add a small macOS smoke application/test that records two `System.nanoTime()`
values around `Vm.sleep(20)` and accepts a broad lower bound such as
`10_000_000L`. Build/deploy/run it only through the existing macOS smoke flow.
At closure, rerun only the permitted SDK build, macOS native build, and macOS
smoke; do not run Windows, Linux, Android, or iOS builds locally.

Acceptance: the macOS smoke passes, focused converter/API tests pass, and the
final diff contains no `Vm.nanoTime()`, no changed `Vm.getTimeStamp()`
semantics, and no platform duplication where sharing is possible.

## Decision Log

- Decision: use `jlS_nanoTime` as the native symbol and `java/lang/System` as
  the native metadata class, while the Java source declaration remains in
  `jdkcompat/lang/System4D.java`.
  Rationale: this follows the existing `*4D` resolver and the native generator's
  class-prefix convention after converter suffix removal.
  Date: 2026-09-14.
- Decision: keep monotonic-clock helpers in the platform-selected `utils_c.h`
  files and expose only one native wrapper in `nm/lang/System.c`.
  Rationale: existing time primitives already live in those utility headers,
  and this avoids duplicating the Java/native boundary per platform.
  Date: 2026-09-14.
- Decision: use quotient/remainder decomposition for Darwin and Win32 scaling.
  Rationale: it meets the no-overflow and MSVC portability requirements without
  changing the `int64` ABI or relying on compiler extensions.
  Date: 2026-09-14.

## Validation and Acceptance

Milestone 1 uses Level 1/2 validation: focused Gradle JUnit tests for the API
and converter, source-of-truth consistency checks, `git diff --check`, and the
focused copyright-header validator on changed first-party code. The SDK build
is deferred until the milestone commit as explicitly required by the user.

Milestone 2 uses Level 2/3 validation: focused native source checks and the
available native unit test path if it can be run without a disallowed platform
build, then a macOS build only at the milestone boundary. Expensive non-macOS
builds are intentionally deferred.

Milestone 3 uses the allowed macOS smoke flow plus the final SDK/macOS commands.
Full platform matrix validation is skipped because the user explicitly forbids
local Windows, Linux, Android, and iOS builds.

## Risks and Open Questions

- The repository keeps generated native metadata under version control; after
  editing `NativeMethods.txt`, regenerate the prototype/header artifacts using
  the repository's generator or make an exactly equivalent focused update.
- Native smoke deployment requires a current SDK distribution and the existing
  macOS toolchain; if either is unavailable, retain the exact failed command and
  compact error in the evidence/state files without attempting forbidden builds.
- Darwin's `mach_timebase_info()` initialization must be thread-safe without
  per-call allocation; a static `mach_timebase_info_data_t` initialized through
  the system API is sufficient if the API's documented initialization contract
  is respected.

## Idempotence and Recovery

All edits are additive or localized. Re-running focused tests and metadata
generation is safe; regenerate the same files rather than hand-merging stale
output. Preserve the pre-existing untracked files listed above. If interrupted,
read `.agent/state/system-nanotime.md`, verify the current branch and scoped
diff, then continue with the recorded next action. Never clean generated
dependencies or use destructive Git commands.

## Outcomes & Retrospective

Completed on 2026-09-14. `System.nanoTime()` now has a Java/device declaration,
converter/native registration, shared platform implementations, and a passing
macOS deployment smoke. The focused test and validation details are indexed in
`.agent/evidence/system-nanotime.md`; completed rationale is in
`.agent/archive/system-nanotime-history.md` and the factual handoff is in
`.agent/reports/system-nanotime-editorial.md`.

## Revision Note

Initial plan created on 2026-09-14 after architecture reconnaissance. All three
milestones completed with passing final SDK, macOS native, focused-test, and
smoke validation.
