<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Add first-class standard output streams with legacy DebugConsole support

This ExecPlan follows `AGENTS.md`, `.agent/PLANS.md`, and
`.agents/skills/logical-commits/SKILL.md`.

## Purpose / Big Picture

Replace the current `System.out` / `System.err` implementation that funnels both
streams through the same `Vm.debug()` accumulator with independent first-class
standard streams backed by native platform output.

At completion:

- `System.out` and `System.err` are different `PrintStream` instances with
  independent Java-side state and locking.
- Desktop applications write to the process standard output and standard error
  handles so a parent process, benchmark runner, shell, or CI process can capture
  them independently.
- Android maps `System.out` to Logcat INFO and `System.err` to Logcat WARN.
- iOS maps `System.out` to Unified Logging INFO and `System.err` to Unified
  Logging ERROR.
- both streams also write to the legacy `<appPath>/DebugConsole.txt`;
- `Vm.debug()` keeps its existing semantics, including `disableDebug`,
  `ERASE_DEBUG`, platform-specific output, and historical durability behavior;
- `System.out.println(Vm.ERASE_DEBUG)` writes that text literally and never
  executes a debug command;
- `Vm.disableDebug` does not disable `System.out` or `System.err`.

This is the foundation for later logging work. Do not implement
`DebugConsole.log`, log rotation, retention, log-policy configuration, formatter
support, or log levels in this plan.

The architecture must allow those later features to be added as sinks or
formatting layers without replacing the Java/native standard-stream contract
introduced here.

## Branch and execution boundary

Create the implementation branch from the remote tip of:

    perf/image-decode-distributed-benchmark

Use:

    git fetch origin perf/image-decode-distributed-benchmark
    git status --short
    git switch -c feat/standard-streams-v1 \
      origin/perf/image-decode-distributed-benchmark

If `feat/standard-streams-v1` already exists because this plan is being resumed,
switch to it instead of recreating it. Never rebase, reset, clean, or overwrite
unrelated local work automatically.

Record the actual base SHA in the state file when the branch is first created:

    git rev-parse origin/perf/image-decode-distributed-benchmark
    git rev-parse HEAD

Do not push unless explicitly requested.

## Working Set and Resume Protocol

Store this plan at:

    .agent/plans/standard-streams-v1.md

Use these supporting files:

    .agent/state/standard-streams-v1.md
    .agent/evidence/standard-streams-v1.md
    .agent/archive/standard-streams-v1-history.md
    .agent/reports/standard-streams-v1-editorial.md

The state file is the first read after interruption. Keep it concise and rewrite
it instead of appending indefinitely. It must contain:

- active milestone and slice;
- base SHA and current branch;
- last logical commit;
- active source paths;
- next concrete action;
- focused validation already completed;
- deferred build or smoke validation and its reason;
- blockers and discoveries that affect the next step;
- deliberate out-of-scope local changes;
- one resume command.

The evidence file is append-only but compact. Record command, revision, result,
relevant counts, log path, and limitation. Do not paste complete build logs into
it.

Read the archive only when historical rationale is required. Update the editorial
report at important milestone completion and final completion, not after every
edit.

Do not reread this entire plan after every context compaction. Resume from the
state file, inspect the active diff and active paths, and open only the plan
section required by the next action.

## Progress

- [x] 2026-09-17 — Milestone 0: created `feat/standard-streams-v1` from
  `5917a4aa3e20123a1ff02c1a5b0cedd9640c0c6b` and committed the supplied plan
  with compact state/evidence artifacts.
- [x] 2026-09-17 — Milestone 1: completed the supported non-formatter
  `PrintStream4D` core and focused JUnit coverage; the SDK test passed.
- [x] 2026-09-17 — Milestone 2: added the Java/native bridge, router, platform
  sinks, lifecycle registration, and passed the permitted macOS build.
- [ ] Milestone 3: extract the shared legacy writer and add native smoke
  coverage.
- [ ] Milestone 4: run final acceptance and prepare the handoff report.

## File-size and artifact policy

Every new file created by this plan must remain below both practical limits:

- approximately 20 KiB;
- approximately 600 lines.

Prefer substantially smaller files.

This applies to new Java, C, header, test, script, plan, state, evidence, archive,
and report files.

Before every commit that adds files, check new-file sizes. A suitable check is:

    git diff --cached --name-only --diff-filter=A |
    while read -r file; do
      test -f "$file" || continue
      bytes=$(wc -c < "$file")
      lines=$(wc -l < "$file")
      printf '%7d bytes %5d lines %s\n' "$bytes" "$lines" "$file"
      test "$bytes" -le 20480
      test "$lines" -le 600
    done

Do not refactor an existing large file merely to make it fit this policy.

If a genuinely new responsibility cannot fit comfortably in one new file,
split it by responsibility instead of creating a large multipurpose module.

All source, tests, scripts, ExecPlan state/evidence/report files, and other
artifacts intentionally produced for this task must be committed.

Ordinary generated build output, caches, binaries, temporary deployment
directories, and build logs are not task artifacts and must not be committed.

## Build and smoke-test policy

Build operations are allowed only for:

- the SDK;
- native macOS.

Run builds only at the end of a milestone that directly requires them.

Do not locally build Windows, Linux, Android, or iOS during this plan.

Native smoke tests may run:

- at the end of a related milestone;
- once again during final plan validation.

Do not run native smoke tests between implementation slices.

Use static inspection and focused non-build checks while a milestone is in
progress.

Keep verbose build and smoke output outside the repository, preferably under
`/tmp`, and record only compact results and log paths in the evidence file.

Do not run the full image-scroll or image-decode benchmark matrices as part of
this plan. This work exists to unblock those benchmarks, not to execute their
full measurement matrix.

## Current Architecture and Scope

`TotalCrossSDK/src/main/java/jdkcompat/lang/System4D.java` currently defines a
nested `VmDebugStream`. One singleton instance is assigned to both `out` and
`err`. It accumulates text and eventually calls `Vm.debug()`.

That implementation must be removed.

`TotalCrossSDK/src/main/java/jdkcompat/io/PrintStream4D.java` currently contains
only a small subset of `PrintStream`. Complete the non-formatter core required
by this V1.

The native legacy debug path is currently:

    Vm.debug()
        -> tsV_debug_s()
        -> debugStr()
        -> platform debug backend
        -> DebugConsole.txt where supported

`TotalCrossVM/src/util/debug.c` selects the existing platform `debug_c.h`
backend. The platform headers currently own their own `FILE *` state and
`DebugConsole.txt` behavior.

This plan extracts only that file-writer responsibility into a dedicated shared
module so `Vm.debug()` and the new standard-stream router do not independently
open the same file.

Native method registration is driven by:

    TotalCrossVM/src/nm/NativeMethods.txt

with generated/native registration artifacts under:

    TotalCrossVM/src/nm/
    TotalCrossVM/src/init/nativeProcAddressesTC.c

Use the repository generator/convention rather than inventing native symbol
names manually.

`TotalCrossVM/CMakeLists.txt` uses an explicit source list. Add every new native
`.c` source explicitly.

## V1 architecture

The final V1 flow is:

    System.out
        -> PrintStream4D
        -> VmStandardOutputStream(OUT)
        -> native standard-stream bridge
        -> StandardStreamRouter
             -> PlatformStandardStreamSink
             -> LegacyDebugConsoleWriter

    System.err
        -> PrintStream4D
        -> VmStandardOutputStream(ERR)
        -> native standard-stream bridge
        -> StandardStreamRouter
             -> PlatformStandardStreamSink
             -> LegacyDebugConsoleWriter

    Vm.debug()
        -> existing debugStr/privateDebug semantics
        -> LegacyDebugConsoleWriter where the legacy backend currently writes
           DebugConsole.txt

`Vm.debug()` must not be routed through `StandardStreamRouter`.

`System.out` and `System.err` must not call `Vm.debug()`, `debugStr()`,
`privateDebug()`, or interpret `ERASE_DEBUG` / `ALTERNATIVE_DEBUG`.

The router is the permanent extension point for future sinks.

A future rolling logger must be addable as:

    StandardStreamRouter
        -> PlatformStandardStreamSink
        -> LegacyDebugConsoleWriter
        -> RollingLogSink

without changing:

    System4D
    PrintStream4D
    VmStandardOutputStream
    the native method signatures used by VmStandardOutputStream

Formatter support will later sit above the same output stream:

    printf / format
        -> formatter
        -> PrintStream4D
        -> VmStandardOutputStream

## Native source layout

Do not place the new standard-stream implementation in `utils.c`, `debug.c`,
`Vm.c`, or their equivalent platform implementation files.

Create dedicated native files.

Use this layout unless a directly equivalent existing directory convention
requires only a path-name adjustment:

    TotalCrossVM/src/nm/sys/VmStandardOutputStream.c

    TotalCrossVM/src/util/standard_stream.h
    TotalCrossVM/src/util/standard_stream.c

    TotalCrossVM/src/util/win/standard_stream_c.h
    TotalCrossVM/src/util/posix/standard_stream_c.h
    TotalCrossVM/src/util/android/standard_stream_c.h
    TotalCrossVM/src/util/darwin/standard_stream_c.h

    TotalCrossVM/src/util/legacy_debug_console.h
    TotalCrossVM/src/util/legacy_debug_console.c

`VmStandardOutputStream.c` contains only the Java/native method wrappers and
argument validation necessary at the native boundary.

`standard_stream.c` owns:

- OUT versus ERR channel definitions;
- router lifecycle;
- router synchronization;
- sink fan-out;
- aggregate success/error reporting;
- logical versus durable flush routing;
- logical channel close behavior.

Platform-specific standard output implementation belongs exclusively in the
new `standard_stream_c.h` files.

`legacy_debug_console.c` owns:

- the shared `DebugConsole.txt` file handle;
- lazy opening under `appPath`;
- serialized raw writes;
- legacy line writes;
- logical flush;
- durable flush;
- file erase;
- shared file close.

Existing `debug_c.h` files may be changed only enough to delegate their
`DebugConsole.txt` operations to `legacy_debug_console.*` while preserving their
existing platform debug behavior.

Do not add standard-stream routing or platform standard-stream behavior to those
legacy headers.

`debug.c` itself should require no new standard-stream implementation. Avoid
changing it unless a minimal declaration/lifecycle adaptation proves strictly
necessary.

Do not use `utils.c` as a dumping ground for any part of this feature.

## Java `VmStandardOutputStream`

Add:

    TotalCrossSDK/src/main/java/totalcross/sys/VmStandardOutputStream.java

It is an internal runtime bridge, not a new supported user-facing logging API.

It must extend `java.io.OutputStream`.

It owns one immutable stream identifier:

    OUT
    ERR

It does not know about:

- DebugConsole paths;
- Logcat;
- Unified Logging;
- Windows handles;
- rolling logs;
- retention;
- timestamps;
- log levels.

Its responsibilities are limited to:

- validate Java write ranges using ordinary `OutputStream` semantics;
- forward bytes to native code;
- expose logical flush for `PrintStream4D` auto-flush;
- expose ordinary explicit `flush()` as the durable flush request;
- close its logical channel without closing globally shared sinks;
- convert native failure into `IOException`.

Use byte-array writes as the normal bridge path. Do not convert Java strings in
native code.

The native contract is conceptually:

    standardWrite(stream, byte[], offset, length)
    standardFlush(stream, durable)
    standardClose(stream)

Use the symbol names produced by the repository native-method generator.

`standardClose(stream)` is a logical channel close. It may flush pending
platform text for that stream, but it must not close:

- inherited stdout/stderr OS handles;
- the shared `DebugConsole.txt` file;
- resources needed by the other standard stream;
- resources needed by `Vm.debug()`.

Shared sinks are destroyed only by runtime shutdown.

## `PrintStream4D` V1 contract

Complete the non-formatter core API used by ordinary `PrintStream` output.

Implement or correct:

    write(int)
    write(byte[])
    write(byte[], int, int)
    writeBytes(byte[])

    print(boolean)
    print(char)
    print(int)
    print(long)
    print(float)
    print(double)
    print(char[])
    print(String)
    print(Object)

    println()
    println(boolean)
    println(char)
    println(int)
    println(long)
    println(float)
    println(double)
    println(char[])
    println(String)
    println(Object)

    append(char)
    append(CharSequence)
    append(CharSequence, int, int)

    flush()
    close()
    checkError()
    setError()
    clearError()

Preserve the existing `OutputStream` constructors required for
`System.out` / `System.err`.

Do not implement in this V1:

    printf(...)
    format(...)
    Formatter
    Formatter-dependent behavior
    File-based PrintStream constructors
    filename-based PrintStream constructors
    additional charset constructors

Those constructor families are deliberately outside this V1 and are not needed
for the standard-stream benchmark requirement.

For the methods implemented here, follow JDK `PrintStream` behavior where
practical:

- I/O failures do not normally escape from `PrintStream`; mark the trouble
  state and expose it through `checkError()`;
- `close()` is idempotent;
- output after close sets the error state rather than reopening the stream;
- `String` and `Object` null handling follows `PrintStream`;
- overloads serialize one logical print operation under the stream's lock;
- `println` must not be implemented as an unlocked sequence that allows another
  thread to interleave between value and newline.

`System.out` and `System.err` must have distinct `PrintStream` objects, distinct
Java locks, distinct error state, and distinct underlying
`VmStandardOutputStream` objects.

Do not recreate the current shared `VmDebugStream` accumulator.

For string-to-byte conversion, use UTF-8 consistently for the standard-stream
path. Native code receives bytes and never performs Java character conversion.

Generic `PrintStream4D` tests must include non-ASCII text so this contract is
explicit rather than accidental.

## Flush semantics

Preserve the distinction between automatic stream flushing and an explicit
durability request from the beginning.

For `System.out` / `System.err`:

    println / auto-flush
        -> logical flush
        -> platform output is emitted
        -> DebugConsole.txt receives fflush-equivalent behavior
        -> no fsync caused solely by auto-flush

An explicit:

    System.out.flush()
    System.err.flush()

must request:

    durable = true

The legacy file sink then performs its supported durable synchronization after
flushing userspace buffers.

`PrintStream4D` may recognize `VmStandardOutputStream` specifically for this
internal logical-flush operation. Keep that dependency narrow and documented.

Do not map every auto-flush to durable synchronization.

For `Vm.debug()`, preserve historical durability behavior. In particular, do
not use this feature as an excuse to remove the existing per-message durable
sync on legacy POSIX/Android paths.

## Router failure semantics

The standard-stream router must attempt every enabled/available sink even when
one fails.

An unavailable platform sink is not itself a write error. Example: a Windows GUI
process may have no inherited standard handle. In that case the platform sink is
considered unavailable and `DebugConsole.txt` may still succeed.

If a sink was available and an actual write/flush operation fails:

- continue attempting the other sink;
- return aggregate failure to `VmStandardOutputStream`;
- let `PrintStream4D` set its internal error state.

Do not throw native exceptions directly for ordinary sink write failure.

## Concurrency

Use one native router mutex for standard-stream emission so OUT and ERR records
cannot concurrently corrupt shared sink state.

The legacy debug-console writer must also own synchronization because it is
shared by:

    System.out
    System.err
    Vm.debug()

Do not rely exclusively on the separate Java `PrintStream` locks for native
file safety.

Keep lock ordering fixed:

    StandardStreamRouter lock
        -> LegacyDebugConsoleWriter lock

`Vm.debug()` acquires only the legacy writer lock.

Do not introduce a reverse acquisition path.

A single native write call is the atomic unit. Do not promise atomicity across
multiple application calls such as two separate `print()` calls.

## Desktop platform sinks

### Windows

For OUT use:

    GetStdHandle(STD_OUTPUT_HANDLE)

For ERR use:

    GetStdHandle(STD_ERROR_HANDLE)

Use `WriteFile` and handle partial writes.

Do not use:

    AllocConsole()
    freopen("conout$", ...)
    printf()
    fprintf(stderr, ...)

as the new standard-stream implementation.

Never close inherited standard handles.

If the returned handle is `NULL` or `INVALID_HANDLE_VALUE`, mark that platform
channel unavailable and continue with the legacy file sink.

This behavior is required so a GUI TotalCross process started by a benchmark
runner or CI process can inherit and write to redirected stdout/stderr.

WinCE is not a new V1 target, but the legacy debug-writer extraction must not
break its existing compilation or behavior.

### Linux

Write OUT to:

    STDOUT_FILENO

Write ERR to:

    STDERR_FILENO

Use `write()` with retry for `EINTR` and partial writes.

Do not introduce stdio buffering around the process descriptors.

### macOS desktop

Use the same POSIX stdout/stderr behavior as Linux:

    STDOUT_FILENO
    STDERR_FILENO

Do not route macOS desktop `System.out` / `System.err` through `NSLog` or
Unified Logging.

The macOS process streams must remain directly redirectable by the benchmark
runner.

## Mobile platform sinks

### Android

Map:

    OUT -> ANDROID_LOG_INFO
    ERR -> ANDROID_LOG_WARN

Use the real Android package/application ID as the Logcat tag.

Resolve it from the Android application context/package manager path already
available to the VM. Cache it for the sink lifetime.

Do not use:

    "TotalCross"

as the normal tag.

Do not use `Settings.applicationId`; that field has a different historical
meaning in TotalCross.

If the package ID cannot be resolved, use `"TotalCross"` only as a defensive
fallback and record the fallback path in code comments and tests/static
validation.

The Android platform sink is text-oriented. Maintain separate bounded OUT and
ERR line buffers. Emit complete lines on newline; emit any remaining partial
line on logical flush or close. Do not allocate an unbounded line buffer.

Choose one fixed internal chunk capacity no larger than 4 KiB and document it
as an implementation limit rather than public API.

### iOS

Map:

    OUT -> Unified Logging INFO
    ERR -> Unified Logging ERROR

Use:

    subsystem = final CFBundleIdentifier
    category = "System.out" or "System.err"

Resolve and cache the bundle identifier from the main bundle.

Use the Unified Logging C API from the dedicated Darwin standard-stream backend.
Do not add this implementation to the existing Darwin debug implementation.

As on Android, use separate bounded text buffers and flush complete lines or
explicit partial lines.

## Legacy `DebugConsole.txt`

Keep the path:

    <appPath>/DebugConsole.txt

Do not add timestamps, stream names, severity prefixes, session markers, or
other formatting in this V1.

Standard streams write their actual byte stream to this file.

For example:

    System.out.print("foo");
    System.err.println("bar");

must not be rewritten into a structured log record.

The file writer is shared, but the semantics of callers are not.

For standard streams:

- bytes are written literally;
- `ERASE_DEBUG` has no special meaning;
- `ALTERNATIVE_DEBUG` has no special meaning;
- `Vm.disableDebug` is ignored;
- logical flush does not perform durable sync;
- explicit flush may perform durable sync.

For `Vm.debug()`:

- preserve its existing newline behavior;
- preserve `ERASE_DEBUG`;
- preserve `ALTERNATIVE_DEBUG`;
- preserve `Vm.disableDebug`;
- preserve historical platform output;
- preserve historical durability behavior.

`System.out.println(Vm.ERASE_DEBUG)` must therefore write:

    !erase debug!

literally.

`Vm.debug(Vm.ERASE_DEBUG)` must retain its historical erase behavior.

## Compatibility during legacy-writer extraction

The legacy writer extraction is a mechanical ownership refactor, not a redesign
of `Vm.debug()`.

For each existing platform backend, first document its current behavior and
preserve it.

Important examples:

- Android continues its existing debug-specific Logcat behavior.
- iOS continues its existing debug-specific path.
- Windows debug output and `ENABLE_CONSOLE` behavior remain unchanged.
- the existing macOS desktop special handling remains unchanged for
  `Vm.debug()`.
- existing DebugConsole termination/separator behavior remains unchanged.
- the existing `DebugConsole.txt` path remains based on `appPath`.

Do not make `Vm.debug()` automatically write to any future/new log.

Do not make the standard-stream router call `privateDebug()`.

## Runtime lifecycle

Add explicit standard-stream lifecycle functions in the new native module.

Initialize the standard-stream subsystem from startup only after the runtime has
the state required by its sinks.

Do not hide standard-stream initialization inside `debug.c`.

At shutdown:

1. allow existing memory/debug teardown ordering to retain its historical
   ability to emit legacy debug messages;
2. flush/destroy standard-stream-specific buffers before the shared legacy
   debug writer is finally closed;
3. do not close inherited process stdout/stderr handles;
4. close the shared DebugConsole writer exactly once.

Make only the minimal startup/shutdown edits needed to call these dedicated
module lifecycle functions.

## Native registration

Declare the native methods for
`totalcross.sys.VmStandardOutputStream` in:

    TotalCrossVM/src/nm/NativeMethods.txt

Regenerate the repository-managed native registration outputs rather than
inventing their symbol names manually.

Expected generated/updated paths include the appropriate entries in:

    TotalCrossVM/src/nm/NativeMethods.h
    TotalCrossVM/src/nm/NativeMethodsPrototypes.txt
    TotalCrossVM/src/init/nativeProcAddressesTC.c

Implement the generated wrappers in:

    TotalCrossVM/src/nm/sys/VmStandardOutputStream.c

Do not place these wrappers in `Vm.c`.

## Test strategy

Add focused Java unit coverage for `PrintStream4D`.

Use an in-memory `OutputStream` and a failing `OutputStream`.

Cover at least:

- every primitive `print`;
- every primitive `println`;
- String and Object null behavior;
- `char[]`;
- UTF-8/non-ASCII output;
- `append` overloads;
- byte writes and slices;
- `writeBytes`;
- newline behavior;
- independent stream instances;
- auto-flush;
- explicit flush;
- close idempotence;
- writes after close;
- `checkError`;
- I/O failure setting the error flag.

Where practical, compare observable bytes/state against JDK `PrintStream`
behavior for the supported V1 subset.

Do not add formatter tests.

## Native macOS smoke

Add one small smoke application under the existing smoke-test source tree:

    TotalCrossSDK/src/smokeTest/java/totalcross/sys/StandardStreamsSmokeApp.java

Keep the fixture machine-readable and self-terminating.

The smoke must exercise:

1. a partial `System.out.print` followed by `println`;
2. a partial `System.err.print` followed by `println`;
3. explicit `out.flush()` and `err.flush()`;
4. a normal `Vm.debug()` record;
5. `Vm.debug(Vm.ERASE_DEBUG)` erasing the legacy file;
6. `System.out.println(Vm.ERASE_DEBUG)` writing the literal string afterward;
7. `Vm.disableDebug = true` suppressing a `Vm.debug()` message;
8. standard OUT and ERR messages while `Vm.disableDebug` is true;
9. restoration of `Vm.disableDebug` before exit;
10. exit code zero on successful fixture completion.

Add a focused macOS smoke runner only if no current reusable runner cleanly
supports process stdout/stderr capture.

The runner must launch the deployed native application with stdout and stderr
captured separately and assert:

- the OUT marker is present in captured stdout;
- the OUT marker is absent from captured stderr;
- the ERR marker is present in captured stderr;
- the ERR marker is absent from captured stdout;
- final `DebugConsole.txt` contains both post-erase standard-stream markers;
- final `DebugConsole.txt` contains the literal `!erase debug!`;
- final `DebugConsole.txt` contains the post-erase normal `Vm.debug()` marker;
- content emitted before `Vm.debug(Vm.ERASE_DEBUG)` is absent afterward;
- the `Vm.debug()` message emitted while `Vm.disableDebug` was true is absent;
- standard-stream messages emitted while `Vm.disableDebug` was true are present.

Do not make this smoke depend on the full image benchmark.

## Plan of Work

### Milestone 0 — establish branch, plan, and resumable state

Create `feat/standard-streams-v1` from
`origin/perf/image-decode-distributed-benchmark`.

Save this ExecPlan, initialize the state and compact evidence files, and record
the base SHA and initial scoped status.

Read once:

    AGENTS.md
    .agent/PLANS.md
    .agents/skills/logical-commits/SKILL.md
    .agents/skills/validate-headers/SKILL.md

Inspect only the implementation paths named in this plan.

Do not build.

Commit the planning/resume artifacts as one logical commit:

    docs(agent): add standard streams v1 plan

After the commit, update state with the commit SHA and next action.

Acceptance:

- branch ancestry is correct;
- unrelated local files are untouched;
- plan/state/evidence are below the new-file size limits;
- `git diff --check --cached` and header validation pass.

### Milestone 1 — complete the non-formatter PrintStream core

Implement the V1 `PrintStream4D` contract and its focused unit tests.

Do not introduce `VmStandardOutputStream` yet unless compilation of the final
logical-flush hook requires its type. If that type is required, add only the
minimal Java class contract needed for compilation and defer native methods to
Milestone 2.

During implementation use only static checks.

At the end of the milestone, run the focused SDK test operation. This is the
first build/test operation permitted by the plan.

Prefer a focused command such as:

    cd TotalCrossSDK
    ./gradlew-agent test \
      --tests '<focused PrintStream4D test class>' \
      --no-daemon --console=plain

Do not run `clean dist` here unless the focused test cannot validate the source
through the normal SDK build graph.

Run header validation and `git diff --check`.

Commit:

    feat(sdk): complete print stream core api

The commit body must describe the excluded formatter/constructor scope and the
focused validation performed.

Acceptance:

- all listed non-formatter methods behave as specified;
- the unit test passes;
- no formatter implementation exists;
- no native platform behavior has been added to Java classes.

### Milestone 2 — add the standard-stream bridge and dedicated native router

Add:

- `VmStandardOutputStream`;
- NativeMethods metadata;
- generated native registration changes;
- `VmStandardOutputStream.c`;
- `standard_stream.h`;
- `standard_stream.c`;
- dedicated Windows, POSIX, Android, and Darwin platform sink files;
- CMake source registration;
- minimal startup/shutdown lifecycle hooks.

Change `System4D` to remove `VmDebugStream` and instantiate separate OUT and ERR
streams.

Do not integrate `DebugConsole.txt` yet except for a temporary router slot that
is inactive until Milestone 3.

Do not modify `debug.c`, `utils.c`, or `Vm.c` to implement the feature.

Use static checks while editing.

At the milestone boundary, perform one macOS native build only.

Use a task-specific out-of-tree build directory, for example:

    cmake -S TotalCrossVM \
      -B /tmp/tc-standard-streams-v1-macos \
      -DCMAKE_BUILD_TYPE=Release \
      -G Ninja
    cmake --build /tmp/tc-standard-streams-v1-macos

Do not run any other platform build.

If the host cannot perform the macOS build, record the exact limitation and do
not substitute a Linux/Android/iOS/Windows build.

Commit the bridge/router/platform family as logical commits if the diff is too
large for one reviewable unit. Preferred boundaries are:

    feat(runtime): add native standard stream router

and, when useful:

    feat(runtime): add standard stream platform sinks

Do not split solely by arbitrary file count.

Acceptance:

- `System.out` and `System.err` are different objects;
- neither calls `Vm.debug`;
- macOS native build passes;
- macOS standard output maps to fd 1/2;
- Windows implementation uses inherited standard handles;
- Android and iOS code exists only in dedicated standard-stream backend files;
- no new standard-stream implementation appears in `debug.c`, `utils.c`, or
  `Vm.c`;
- all new files remain within size limits.

### Milestone 3 — share the legacy DebugConsole writer

Create `legacy_debug_console.h/.c`.

Move ownership of the legacy file handle, lazy open, erase, flush, sync, and
close behavior into this module.

Adapt the existing platform `debug_c.h` files only enough to call this shared
writer while preserving all existing `Vm.debug()` semantics.

Connect the standard-stream router to the same writer using raw byte semantics.

Do not route `Vm.debug()` through the standard-stream router.

Do not add `DebugConsole.log`.

Add the native macOS smoke fixture and runner in this milestone because the full
observable contract now exists.

Use static validation while implementing.

At the milestone boundary:

1. run the focused SDK tests;
2. run one macOS native build;
3. run the native macOS standard-stream smoke.

Do not build any other native platform.

Commit the writer extraction/integration separately from smoke coverage when
that produces clearer logical history:

    refactor(runtime): share legacy debug console writer

then:

    test(runtime): cover native standard stream routing

Acceptance:

- one shared writer owns `DebugConsole.txt`;
- no second independent `FILE *` for the same file exists in the new path;
- `Vm.debug` legacy semantics remain intact;
- standard streams ignore debug commands/settings;
- stdout/stderr capture works separately on macOS;
- `DebugConsole.txt` contains the expected post-erase OUT/ERR/legacy markers;
- all milestone smoke assertions pass.

### Milestone 4 — final acceptance and handoff

Do not add new feature scope.

Inspect the complete branch diff from the recorded base SHA.

Verify explicitly that this branch contains no implementation of:

    Formatter
    printf/format support
    DebugConsole.log
    rotation
    retention
    log-policy configuration
    timestamped/structured logging

Run final permitted validation only:

1. focused SDK tests;
2. SDK distribution build;
3. macOS native build;
4. macOS native standard-stream smoke;
5. changed-file copyright validation;
6. `git diff --check`;
7. commit-message validation for every task commit;
8. new-file size/line checks.

SDK distribution build:

    cd TotalCrossSDK
    ./gradlew-agent clean dist

Run it only now, at final milestone closure.

Use the existing task-specific macOS build directory rather than creating
another repository-local build tree.

Do not execute full image benchmark matrices.

Update:

    .agent/state/standard-streams-v1.md
    .agent/evidence/standard-streams-v1.md
    .agent/archive/standard-streams-v1-history.md
    .agent/reports/standard-streams-v1-editorial.md
    .agent/plans/standard-streams-v1.md

Consolidate completed milestone detail into history rather than letting the
active plan/state grow indefinitely.

Commit final plan/report/evidence reconciliation:

    docs(agent): finalize standard streams v1 handoff

Acceptance:

- repository task artifacts are committed;
- ordinary build/log artifacts are not committed;
- branch is clean except for explicitly recorded pre-existing unrelated local
  changes;
- final smoke proves the benchmark-relevant stdout/stderr behavior;
- final report clearly states that Windows/Linux/Android/iOS implementation was
  not locally built under this plan's build restriction.

## Commit policy

Follow `.agents/skills/logical-commits/SKILL.md` before every commit.

Read the active state first.

Inspect only scoped changes:

    git status --short -- <task paths>
    git diff --stat -- <task paths>
    git diff -- <task paths>

Validate copyright headers on changed first-party files.

Stage only intended paths.

Always run:

    git diff --check --cached

Review staged content before committing.

Use English commit messages with repository-valid type and scope.

Do not amend or rewrite previous commits.

Do not combine unrelated cleanup with this work.

Do not push.

The expected logical history is approximately:

    docs(agent): add standard streams v1 plan
    feat(sdk): complete print stream core api
    feat(runtime): add native standard stream router
    feat(runtime): add standard stream platform sinks
    refactor(runtime): share legacy debug console writer
    test(runtime): cover native standard stream routing
    docs(agent): finalize standard streams v1 handoff

Combine adjacent runtime commits only when they form one inseparable functional
contract. Add a focused fix commit when milestone validation discovers a real
defect; do not hide the fix by amending an earlier commit.

## Validation and Acceptance

This work crosses Java API compatibility and the Java/native ABI, so validation
escalates by milestone.

Milestone 0 uses static validation only.

Milestone 1 uses Level 2 focused SDK validation at milestone closure.

Milestone 2 is an ABI/platform-family change. Because the user restricts local
builds, validate the ABI through the permitted macOS build at milestone closure
and use static source review for the other backends.

Milestone 3 adds observable native behavior. Run the permitted macOS native
smoke only after the milestone implementation is complete.

Milestone 4 is the final gate and repeats only the allowed SDK/macOS
build/smoke operations.

The final observable acceptance criteria are:

- `System.out != System.err`;
- OUT and ERR have independent error/lock state;
- parent-process stdout/stderr capture works on macOS;
- the Windows implementation targets inherited stdout/stderr handles rather
  than allocating a console;
- Linux targets fd 1 and fd 2;
- Android targets INFO/WARN with the real package ID;
- iOS targets INFO/ERROR with the bundle identifier;
- both standard streams feed the shared legacy file writer;
- `Vm.debug` still owns debug commands and debug disabling;
- standard streams do not call the debug API;
- standard-stream auto-flush does not cause fsync-per-line;
- explicit flush reaches the durable-flush contract;
- the code is structurally ready for a future third rolling-log sink.

## Risks and Open Questions

There are no architectural choices intentionally left to the implementing agent.

Implementation discoveries may require a narrow mechanical adjustment when a
repository convention differs from an assumed filename or generated native
symbol. Such an adjustment is allowed only when it preserves the contracts in
this plan.

Potential implementation risks to record rather than redesign around are:

- converter/native registration may require generated metadata updates beyond
  the obvious three files;
- Android package-ID retrieval must use existing JNI/runtime context without
  adding a new Android application API merely for logging;
- iOS Unified Logging compilation cannot be locally proven because iOS builds
  are forbidden by this plan;
- Windows handle behavior cannot be locally proven because Windows builds are
  forbidden by this plan;
- extracting the legacy writer may expose a subtle historical difference
  between desktop macOS and iOS `Vm.debug()` behavior. Preserve the pre-change
  behavior instead of normalizing it;
- shared writer lifetime must not let `System.out.close()` or
  `System.err.close()` close resources still used by `Vm.debug()`.

If a discovery would require changing one of the architecture decisions above,
stop that slice, record the evidence in state/evidence, and leave the branch in
a resumable state rather than silently redesigning the feature.

## Idempotence and Recovery

All new modules are additive and all changes to legacy debug files must be
localized delegations.

Do not use destructive Git commands.

Do not delete local build caches or unrelated generated files to make status
clean.

Use `/tmp/tc-standard-streams-v1-*` for native build, deployment, capture, and
smoke outputs where practical.

Re-running metadata generation must produce deterministic tracked files.

Re-running the smoke may replace only its own temporary deployment/capture
directory.

On interruption:

1. read `.agent/state/standard-streams-v1.md`;
2. confirm `feat/standard-streams-v1`;
3. inspect the scoped status/diff named in state;
4. do not rerun already successful expensive milestone validation unless the
   relevant source changed;
5. continue from the recorded next action.

## Outcomes & Retrospective

Keep this section short while the plan is active.

At each milestone completion record only:

- delivered behavior;
- logical commit SHA(s);
- validation outcome;
- material limitation or discovery;
- evidence/history reference.

At final completion state clearly:

- which V1 contracts were delivered;
- which five platform backends were implemented;
- which platform was actually built/smoke-tested locally;
- which future V2 work remains intentionally absent;
- whether the benchmark can now capture TotalCross native stdout/stderr without
  relying on `Vm.debug()`.

## Editorial report requirements

The final
`.agent/reports/standard-streams-v1-editorial.md` must contain:

- Editorial Summary;
- Original Plan versus Actual Outcome;
- What Changed;
- Decisions and Trade-offs;
- Unexpected Problems and Discoveries;
- Validation and Measurable Results;
- Useful Evidence and Examples;
- Limitations, Remaining Work, and Open Questions;
- Possible Article Angles;
- Suggested Narrative;
- Claims Requiring Human Review.

Keep it factual. Do not claim that Windows, Linux, Android, or iOS were built or
runtime-tested when this plan explicitly forbids those local builds.

## Revision Note

Initial V1 plan.

The scope intentionally stops after first-class standard streams, the dedicated
native routing/platform layer, and compatible `DebugConsole.txt` integration.

Formatter support and the configurable rotating `DebugConsole.log`
infrastructure are deferred to a later ExecPlan that must extend this router
rather than replace it.
