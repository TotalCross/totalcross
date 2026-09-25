<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Frame pacing and scheduling investigation — Part 2: deadlines, event loop, and Windows package

This ExecPlan follows `AGENTS.md`, `totalcross-depot-tools/.agent/PLANS.md`, and
Part 1 at `.agent/plans/frame-pacing-scheduling-part-1.md`. Execute it only
after Stages 1-3 are closed and their macOS results are committed.

## Purpose / Big Picture

Complete the causal pacing investigation by measuring:

4. relative native timer scheduling versus monotonic absolute deadlines;
5. the current polling/Sleep(1) SDL event loop versus event-driven waiting,
   then isolate the semantics of `Thread.yield()`.

Every stage closes with a native macOS benchmark and committed compact evidence.
After Stage 5, push the branch, dispatch `.github/workflows/package.yml` on the
exact pushed revision, download the generated SDK, and create a Windows x64
benchmark package. The Windows package must contain a pure PowerShell 5.1 runner
that executes the complete Stage 1-5 matrix and creates one ZIP of results.

Branch and immutable base remain:

    branch: feat/frame-pacing-scheduling-diagnostics
    base:   d7a9ec93faf9f2da0724611de115649176d0b033

Production defaults remain legacy unless this plan explicitly says otherwise:
relative timer deadlines, polling SDL event loop, and current `Thread.yield()`
behavior. This is an investigation branch, not a default-behavior migration.

## Resume Protocol

First read:

    .agent/state/frame-pacing-scheduling.md

Then read only the active section of this plan. Inspect Stage 1-3 evidence only
when a comparison needs an exact baseline row. Do not reconstruct the earlier
PNG investigation.

Primary paths: `Event.c`, `event/sdl/event_c.h`, `MainWindow.c`, `Thread.c`,
`globals.c/.h`, object-memory wake code when required, the frame-pacing runners
and tests, `package-image-scroll-benchmark.sh`, and the benchmark README.

Every new file must stay below 20 KiB and approximately 600 lines. Split the
PowerShell implementation before either limit is crossed. Do not refactor
existing large files for size alone.

## Fixed measurement contract

Keep Part 1 workload invariants:

- 663 files, content magic 660 JPEG + 3 PNG;
- three columns, 540x960 benchmark screen;
- ImageOptimizations mask 6;
- prefetch on, worker-semaphore;
- no diagnostic accounting in measured processes;
- correctness preflight with accounting on and 663 READY / 0 failed /
  0 unsupported / zero scroll-time cold materialization;
- three fresh processes per measured configuration.

Keep all Part 1 result columns. Add these fields when applicable:

    timerDeadlineMode
    eventLoopMode
    threadYieldMode
    expectedCallbackIntervalNs
    callbackAbsoluteLatenessP50/P95/P99/MaxNs
    callbackDeltaErrorP50/P95/P99/MaxNs

Do not change rendering/image algorithms in Part 2.

## Stage 4 — relative versus absolute timer deadlines

Goal: determine whether rescheduling from "now" accumulates phase error.

Implement a diagnostic native timer policy selected once per process through:

    TC_TIMER_DEADLINE_MODE=relative|absolute

Default is `relative` and must retain existing behavior exactly.

Do not expose a new Java API. `absolute` uses the existing native
`getNanoTime()`, which is monotonic/high-resolution on supported desktop
platforms. Keep Java timer interval arguments in milliseconds; a requested
16 ms remains a 16,000,000 ns period. This stage does not reinterpret 16 ms as
16.666667 ms.

Preserve the current relative state:

    nextTimerTick = getTimeStamp() + intervalMs

For absolute mode add 64-bit nanosecond deadline state. The policy is:

1. When no timer phase exists, schedule `nowNs + intervalNs`.
2. When the previous timer fired and Java reschedules the same interval, derive
   the next deadline from the previous scheduled deadline, not callback finish:
   `previousDeadline + intervalNs`.
3. If that computed deadline is already due, advance by whole interval multiples
   to the first future deadline. Do not dispatch catch-up bursts.
4. If a pending deadline exists and a caller requests an earlier wake such as
   `setTimerInterval(1)`, keep the earlier of the pending deadline and
   `nowNs + requestedInterval`.
5. An interval of zero clears pending deadline and phase state.
6. A changed interval starts a new phase at `nowNs + newIntervalNs`.

Keep legacy `nextTimerTick` for relative mode rather than silently changing its
semantics. New absolute fields belong in native globals with explicit names.

`checkTimer()` selects the relative or absolute path. Immediately before calling
`_onTimerTick`, record the scheduled deadline as the last-fired deadline and
clear only the pending deadline. Java may then schedule the next one.

Add focused native tests for phase preservation, skipped-late intervals, early
1 ms preemption, interval change, zero/clear, and default-relative selection.

Stage 4 matrix, three fresh processes each:

- `timer-60-nano-relative`
- `timer-60-nano-absolute`
- `update-nano-relative`
- `update-nano-absolute`

Use `System.nanoTime()` for Flick physics in all four so the only experimental
variable within each pair is native deadline policy.

Before benchmarking, commit scheduler implementation/tests logically. At
milestone close, a macOS native build is permitted and required:

    cmake -S TotalCrossVM -B build/frame-pacing-macos \
      -DCMAKE_BUILD_TYPE=Release \
      -DCMAKE_OSX_ARCHITECTURES=arm64 -G Ninja
    cmake --build build/frame-pacing-macos \
      --target tcvm Launcher --parallel

Rebuild the SDK only if Java/SDK sources changed after Stage 3. Assemble a
temporary SDK ZIP with this new macOS runtime, package the benchmark, run Stage
4, and commit:

    .agent/evidence/frame-pacing-stage-4-macos.csv
    .agent/evidence/frame-pacing-stage-4-macos.json

Acceptance: twelve measured processes pass and every row records the selected
deadline policy. Relative remains the process default when the environment
variable is absent.

## Stage 5 — event-driven SDL wait and real yield diagnostic

Goal: quantify jitter caused by polling `Sleep(1)` in the SDL main event loop,
then isolate whether the runtime's current `Thread.yield()` implementation adds
measurable delay.

### SDL event-driven wait mode

Add an internal process selector:

    TC_EVENT_LOOP_MODE=poll|wait

Default `poll` preserves the existing `Event.c` loop including its current
Sleep(1) behavior.

Implement `wait` only for the SDL windowing backend. Do not change Android,
iOS, or native-legacy event backends in this plan.

Refactor `TotalCrossVM/src/event/sdl/event_c.h` so SDL event dispatch can be
called both from non-blocking poll and from a wait result. Keep one dispatcher
for window, input, text, wheel, quit, and custom wake events.

Register exactly one SDL custom wake event after SDL initialization. Provide an
internal wake function that pushes that event. The wake event carries no user
payload and is consumed only to unblock the main event loop.

In wait mode the main event loop must:

1. process already-due GC/timer work;
2. process any already-queued SDL event without sleeping;
3. compute time until the next timer deadline;
4. set an `eventLoopWaiting` state;
5. recheck pending work/deadline after setting that state to close the lost-wake
   race;
6. block in `SDL_WaitEvent` when no deadline exists, or
   `SDL_WaitEventTimeout` when one exists;
7. clear waiting state immediately after return and dispatch the returned event.

Round an SDL timeout up to milliseconds only at the final API boundary. Deadline
truth remains nanoseconds in absolute mode. Never busy-spin the final fraction.

A thread that changes an earlier timer deadline while the loop is waiting must
wake it. `setTimerInterval()` is the primary wake path used by
`runOnMainThread()`. Wake only when wait mode is active and the event loop may
be blocked; avoid queuing self-wake events during ordinary main-thread timer
rescheduling.

`callGConMainThread = true` is another asynchronous condition currently relying
on polling. When that flag is set from the object-memory path, call a small
platform-neutral `wakeMainEventLoop()` helper; it is a no-op outside SDL wait
mode.

Do not remove the historical `isEventAvailable()` Sleep(1) or convert socket,
GC-lock, or context-owner polling in this plan. They are separate follow-up
families and are not required to measure the main SDL loop.

### Thread.yield diagnostic mode

Today deployed `Thread.yield()` maps to `Sleep(1)`. Add a diagnostic process
selector:

    TC_THREAD_YIELD_MODE=legacy|native

Default `legacy` preserves Sleep(1).

`native` uses:

- Windows desktop: `SwitchToThread()`, falling back to `Sleep(0)` when it cannot
  switch;
- POSIX desktop, including macOS: `sched_yield()`.

Do not change `Thread.sleep`, `Vm.sleep`, or other Sleep(1) sites. Cache the
selector once per process; do not call `getenv()` on every yield.

### Stage 5 matrix

Use `update + nano + absolute deadline` as the fixed animation path. Run three
fresh processes for each:

- `poll-legacy-yield`
- `wait-legacy-yield`
- `wait-native-yield`

The first pair isolates event-loop polling versus wait. The second pair isolates
yield semantics under the same wait loop.

Add focused native tests for custom wake dispatch, earlier-deadline wake,
no-deadline wait, timeout conversion, lost-wake recheck, GC wake, default poll,
and yield selector behavior.

Commit event-loop implementation separately from yield implementation when both
are non-trivial. Then commit harness/matrix updates.

At milestone close rebuild native macOS `tcvm` and `Launcher`. Rebuild SDK only
if Java/SDK source changed. Run Stage 5 and commit:

    .agent/evidence/frame-pacing-stage-5-macos.csv
    .agent/evidence/frame-pacing-stage-5-macos.json

Acceptance: nine measured processes pass; wait mode shows no missed callback,
hang, or lost main-thread work; process defaults remain poll + legacy yield.

## Windows runner and complete matrix

Create a pure PowerShell 5.1 entry point:

    run-frame-pacing-benchmark-windows.ps1

If size requires, dot-source one companion PowerShell file. Both must be pure
PowerShell and each remain below 20 KiB/~600 lines.

The runner must not invoke Python, `py`, Java, Git, GitHub CLI, a package
manager, or the network. It launches only the packaged benchmark executable.

Use:

    Set-StrictMode -Version Latest
    $ErrorActionPreference = 'Stop'

Follow the proven process-launch contract from the PNG runner:

- `Start-Process -PassThru`;
- force `$process.Handle` acquisition;
- timed `WaitForExit(timeout)`;
- then parameterless `WaitForExit()` before reading final files;
- use ordinary PowerShell arrays, not `Generic.List[object]`;
- failure handling must still write failure metadata and a results ZIP.

Initialize every metadata variable, including dataset hash, before any metadata
writer can read it.

Validate `manifest.json`, executable/runtime presence, runtime SHA-256, the
663-file dataset hash, and content magic counts 660 JPEG / 3 PNG before the
first measured process.

Execute exactly these matrices, three fresh processes per configuration:

Stage 1:
- synthetic-current-16ms
- synthetic-60hz

Stage 2:
- timer-40-millis
- timer-60-millis
- update-millis

Stage 3:
- timer-60-millis
- timer-60-nano
- update-millis
- update-nano

Stage 4:
- timer-60-nano-relative
- timer-60-nano-absolute
- update-nano-relative
- update-nano-absolute

Stage 5:
- poll-legacy-yield
- wait-legacy-yield
- wait-native-yield

Total measured process count: 48.

For each process, set and restore
`TC_TIMER_DEADLINE_MODE`, `TC_EVENT_LOOP_MODE`, and
`TC_THREAD_YIELD_MODE` according to the configuration. Never leak one
configuration's environment into the next.

Write:

    results/<run-id>/stage-1-summary.csv
    results/<run-id>/stage-2-summary.csv
    results/<run-id>/stage-3-summary.csv
    results/<run-id>/stage-4-summary.csv
    results/<run-id>/stage-5-summary.csv
    results/<run-id>/execution-metadata.json
    results/<run-id>/logs/...
    results/frame-pacing-windows-results-<timestamp>.zip

The final ZIP contains all stage summaries, per-process summary/counter output,
logs, execution metadata, package manifest, and runner copies.

Extend static Python contract tests to prove the exact 48-process matrix,
PowerShell forbidden-command contract, strict-mode initializer ordering,
failure ZIP behavior, and manifest fields. Static tests may use Python on the
development host; the packaged Windows runner may not.

## Package integration

Extend `scripts/package-image-scroll-benchmark.sh` so Windows bundles include
the pacing PowerShell runner(s). Add manifest fields:

    framePacingRunner
    framePacingSchemaVersion
    framePacingMeasuredProcessCount = 48
    framePacingStages = [1,2,3,4,5]

Keep the existing generic Python runner if other benchmark profiles need it;
the Windows pacing workflow must not depend on it.

Update `scripts/README-image-benchmarks.md` with one Windows command:

    powershell -ExecutionPolicy Bypass \
      -File .\run-frame-pacing-benchmark-windows.ps1

Do not document diagnostic environment variables as supported public API.

## Final local validation before publication

After Stage 5 evidence is committed, run only focused non-build validation:

    python3 scripts/test-frame-pacing-benchmark.py
    python3 scripts/test-image-scroll-distributed-benchmark.py
    bash -n scripts/package-image-scroll-benchmark.sh
    git diff --check
    python3 scripts/validate-copyright-headers.sh --files <changed files>

Validate every task commit message with the logical-commits check, verify all
task commits are signed, and verify every new file remains below 20 KiB and
approximately 600 lines.

Do not run local Windows/Linux/Android/iOS builds.

## Push, GitHub package action, and SDK retrieval

Record the exact pre-publication HEAD as:

    ACTION_SOURCE_SHA=$(git rev-parse HEAD)

Push only the task branch:

    git push -u origin feat/frame-pacing-scheduling-diagnostics

Re-read the remote branch SHA and require it equals `ACTION_SOURCE_SHA`.

Dispatch the existing package workflow on that branch:

    gh workflow run package.yml \
      --ref feat/frame-pacing-scheduling-diagnostics

Locate the workflow run by both branch and exact `headSha`; do not select a run
only because it is the newest one. Record run ID and URL in evidence.

Wait for completion with `gh run watch <run-id> --exit-status`. If it fails,
inspect only the failed job/step logs needed to diagnose it. Fix source in a
logical signed commit, push, record a new `ACTION_SOURCE_SHA`, and dispatch a
new run. Never reuse an SDK artifact from a failed or stale SHA.

After success, query the run artifacts and select the packaged SDK artifact
whose name starts with `TotalCross-` and is not the `-jars` artifact. Download
it with `gh run download`. Record artifact ID/name, workflow run ID, head SHA,
SDK ZIP SHA-256, and version.

Do not substitute a local Windows build.

## Create the Windows x64 benchmark package

Use the successful action SDK ZIP and the exact action source SHA:

    bash scripts/package-image-scroll-benchmark.sh \
      --sdk-zip <downloaded TotalCross-version.zip> \
      --corpus "$TC_IMAGE_CORPUS" \
      --output <temporary output dir> \
      --target windows-x64 \
      --source-commit "$ACTION_SOURCE_SHA" \
      --sdk-source-commit "$ACTION_SOURCE_SHA"

Run static package checks only; do not execute the Windows binary on macOS.
Require the package to contain the action-built `tcvm.dll`, deployed benchmark
EXE/TCZ, 663-image corpus, manifest, and pure PowerShell pacing runner(s).

Compute the final Windows ZIP SHA-256. Commit a compact provenance file:

    .agent/evidence/frame-pacing-windows-package.json

It records action source SHA, workflow run ID/URL, SDK artifact ID/name/hash,
final package hash, `tcvm.dll` hash, PowerShell runner hash(es), dataset hash,
content counts, and the statement `windowsExecuted=false`.

The downloaded SDK ZIP and final Windows benchmark ZIP are packaging/build
products and remain outside Git; their source, manifests, hashes, and canonical
provenance are committed. Do not commit large binary distribution artifacts.

Update the editorial report factually. Create one final signed evidence/docs
commit and push it. Record both `ACTION_SOURCE_SHA` and final branch HEAD.

## Commit checkpoints

Use the logical-commits skill and signed commits. Expected logical boundaries
are:

- `feat(runtime): add absolute timer deadline mode`
- `test(benchmark): compare timer deadline policies`
- `docs(plan): record stage four pacing evidence`
- `feat(runtime): add event driven SDL wait mode`
- `perf(runtime): add native yield diagnostic mode`
- `test(benchmark): compare event loop pacing modes`
- `docs(plan): record stage five pacing evidence`
- `build(benchmark): package frame pacing diagnostics`
- `docs(plan): close frame pacing investigation`

Split further when behavior is independently reviewable; all messages must pass
the repository validator and keep body lines <=80 characters.

## Progress

- [x] Stage 4 implementation, macOS benchmark, committed evidence.
- [x] Stage 5 implementation, macOS benchmark, committed evidence.
- [x] Pure PowerShell Windows 48-process runner and package integration.
- [x] Final focused validation and signature/size audit.
- [x] Push exact source SHA and run `package.yml` successfully.
- [x] Download successful action SDK and create the statically checked
  Windows package.
- [x] Commit package provenance/editorial closure and push the final evidence commit.

## Decision Log

- Absolute native scheduling is opt-in; relative remains default.
- SDL wait mode is desktop-SDL-only and opt-in; polling remains default.
- Native yield semantics are opt-in; legacy Sleep(1) remains default.
- No busy-spin pacing is introduced.
- `SDL_WaitEvent[Timeout]` plus an SDL custom wake event is the Stage 5 wait
  mechanism; no Windows-specific high-resolution waitable timer is added here.
- `isEventAvailable()`, socket polling, GC lock waits, and context-owner waits
  are explicitly outside this plan.
- Windows is packaged but not executed by the agent.
- Large SDK/package ZIPs are build/package outputs; commit their provenance and
  hashes, not the binaries themselves.

## Validation and Acceptance

At every functional commit run focused tests, copyright validation, and
`git diff --check`. Native source changes require focused native tests. Builds
occur only at milestone close and only for SDK/macOS.

Stage 4 acceptance: 12/12 measured macOS processes valid.
Stage 5 acceptance: 9/9 measured macOS processes valid.
Windows package acceptance: static package contract valid, action-built runtime
attested, pure PowerShell matrix exactly 48 processes, package hash recorded.

No performance threshold determines pass/fail. The purpose is measurement.
Do not promote a driver, clock, deadline policy, wait mode, or yield mode based
on a single run/configuration. Record observations and defer interpretation.

## Risks and Open Questions

`SDL_WaitEventTimeout` has millisecond timeout resolution. Stage 5 tests whether
removing polling still improves pacing/CPU; it does not claim sub-millisecond
wake precision.

A custom wake path must not lose GC or `runOnMainThread` work. Treat any hang,
missing callback, or delayed main-thread runner as correctness failure, not a
performance result.

## Idempotence and Recovery

Workflow dispatch is retryable only after checking exact branch head SHA.
Never overwrite evidence from a different action SHA without recording the
superseded attempt.

Windows package creation is retryable into a new temporary directory. Verify
hashes before replacing the canonical package provenance file.

Preserve unrelated worktree changes throughout. Do not use destructive Git
cleanup.

## Outcomes & Retrospective

At completion reconcile all five stage evidence files in
`.agent/reports/frame-pacing-scheduling-editorial.md`. State what was measured,
not what was expected. Include the exact Windows package hash and clearly mark
Windows runtime execution as deferred to the operator.

## Revision Note

Part 2 records the remaining design and publication workflow for resumption.
