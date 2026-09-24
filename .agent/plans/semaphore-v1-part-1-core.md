<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Semaphore v1 — Part 1: core blocking primitive and native correctness

This ExecPlan follows `AGENTS.md`, `.agent/PLANS.md`, and
`.agents/skills/logical-commits/SKILL.md`. It is Part 1 of 2 and must be
executed before `semaphore-v1-part-2-stress.md`.

## Purpose / Big Picture

Implement the core deployed `java.util.concurrent.Semaphore` compatibility
surface in TotalCross and prove, on native macOS, that waiting threads block
without polling and wake correctly after `release()`.

The work is intentionally limited to Semaphore itself. Do not modify
`ImagePreparation`, image benchmarks, `AsyncTask`, executors, or any production
consumer.

Application source must continue to use:

    import java.util.concurrent.Semaphore;

The TotalCross converter must resolve that API to:

    jdkcompat.util.concurrent.Semaphore4D

The v1 public surface is exactly:

    Semaphore(int permits)
    void acquire() throws InterruptedException
    void acquireUninterruptibly()
    boolean tryAcquire()
    void release()

Do not implement fairness, the `(int, boolean)` constructor, timed acquire,
multi-permit overloads, queue inspection, `availablePermits()`,
`drainPermits()`, or serialization in this plan.

TotalCross does not currently provide Java-compatible thread interruption.
Therefore deployed `acquire()` keeps the Java signature but blocks with the
same effective uninterruptible semantics as `acquireUninterruptibly()`.
Document this limitation; do not implement interruption here.

Initial permits are signed. Negative initial values are valid. `release()`
increments by one. An acquire succeeds only when the permit count is strictly
positive and then decrements it. `release()` at `Integer.MAX_VALUE` must throw
`Error("Maximum permit count exceeded")` rather than wrap.

No Semaphore wait path may use `Vm.sleep`, `Vm.safeSleep`, busy-waiting, or
periodic polling.

## Planning Base and Branch

Planning base:

    perf/prefetch-thread-diagnostics
    0aeea1029f24a7e3e4f29c8f1f339ffad0a141f2

Execution branch:

    feat/semaphore-v1

Use the current worktree only. Do not create another worktree, clone, stash,
reset, clean, amend, rebase, or rewrite history.

At the start:

    git status --short
    git rev-parse perf/prefetch-thread-diagnostics

The base ref must equal the SHA above. If it moved, stop before implementation
and report the changed base.

If the feature branch does not exist:

    git switch perf/prefetch-thread-diagnostics
    git switch -c feat/semaphore-v1

If it already exists, switch only after confirming:

    git merge-base feat/semaphore-v1 0aeea1029f24a7e3e4f29c8f1f339ffad0a141f2

returns the planning SHA.

Preserve unrelated local changes. If they overlap a required path or block the
branch switch, stop; do not stash or discard them.

Store this file in the repository as:

    .agent/plans/semaphore-v1-part-1-core.md

## Working Set and Resume Protocol

Create and commit these durable execution artifacts:

    .agent/plans/semaphore-v1-part-1-core.md
    .agent/state/semaphore-v1.md
    .agent/evidence/semaphore-v1.jsonl
    .agent/reports/semaphore-v1-editorial.md

On every resume, read `.agent/state/semaphore-v1.md` first. The state file is
rewritten, not appended, and records current part/milestone/slice, last logical
commit, active paths, next exact action, validation completed, validation
deferred, blockers, unrelated dirty paths, and resume command.

The evidence JSONL is append-only and contains compact records only. Search it
by milestone/revision when needed; do not reread it in full.

Create the editorial report only at Milestone 1 closure. Part 2 will finalize
it.

Every new durable file must stay below 20 KB or approximately 600 lines.
Existing files must not be refactored merely to reduce size.

Commit all durable plan/source/test/state/evidence/report artifacts. Do not
commit ordinary Gradle/CMake/Ninja output, native binaries, TCZ/JAR build
outputs, caches, or logs.

## Progress

- [ ] Checkpoint 0: establish branch and commit plan/state/evidence.
- [ ] Slice 1A: add the internal VM blocking condition abstraction.
- [ ] Slice 1B: add native Semaphore storage/operations and registration.
- [ ] Slice 1C: add `Semaphore4D`, converter coverage, and correctness smoke.
- [ ] Milestone 1 closure: SDK + macOS build and native smoke.
- [ ] Hand off to Part 2.

## Current Architecture and Fixed Decisions

### Java compatibility mapping

Do not add source under literal `java.util.concurrent`.

Create:

    TotalCrossSDK/src/main/java/jdkcompat/util/concurrent/Semaphore4D.java

`MethodDeclarationResolver` already searches `jdkcompat` for a `4D` class when
resolving `java.*` owners. `ConcurrentLinkedQueue4D` is the package precedent.

Native declarations use the Java-facing owner. Follow the `Locale4D` precedent:
entries in `TotalCrossVM/src/nm/NativeMethods.txt` must use:

    java/util/concurrent/Semaphore

not the `jdkcompat` implementation name.

### Existing synchronization infrastructure

`totalcross.lang.Thread4D` states that general Java monitor synchronization is
not implemented by the TCVM. `totalcross.util.concurrent.Lock` is special-cased
and owns a native mutex in an opaque Java byte array.

`TotalCrossVM/src/tcvm/tcthread.h` already provides `MUTEX_TYPE`:

- Windows: `CRITICAL_SECTION`;
- POSIX/Android: recursive `pthread_mutex_t`.

The POSIX thread startup path already uses `pthread_cond_t`. Reuse OS blocking
primitives; do not emulate them with sleeps.

### Internal condition abstraction

Add a VM-internal condition abstraction in `tcthread.h`. Keep it private to the
runtime; do not expose a Java condition API.

Use one consistent family of names, conceptually:

    THREAD_CONDITION_TYPE
    INIT_THREAD_CONDITION
    WAIT_THREAD_CONDITION(condition, mutex)
    SIGNAL_THREAD_CONDITION
    DESTROY_THREAD_CONDITION

POSIX/Android/macOS:

- type: `pthread_cond_t`;
- init: `pthread_cond_init`;
- wait: `pthread_cond_wait`;
- signal: `pthread_cond_signal`;
- destroy: `pthread_cond_destroy`.

Windows family:

- type wraps an auto-reset event;
- init with `CreateEvent(NULL, FALSE, FALSE, NULL)`;
- wait releases the associated `CRITICAL_SECTION`, blocks with
  `WaitForSingleObject(..., INFINITE)`, then reacquires the critical section;
- signal with `SetEvent`;
- destroy with `CloseHandle`.

Use the event backend for all Windows-family code in this plan. Do not introduce
`CONDITION_VARIABLE`; this avoids adding a Vista-era API dependency to legacy
Windows/WinCE surfaces.

The Windows event does not accumulate repeated signals. The Semaphore permit
count is therefore authoritative. After a waiter consumes one permit, if
`permits > 0 && waiters > 0`, signal once more before unlocking. This chains
wakeups until currently available permits have been consumed.

Every blocking acquire uses:

    while (permits <= 0)

never `if`, so redundant/spurious wakes are harmless.

### Native Semaphore state

Create:

    TotalCrossVM/src/nm/util/concurrent_Semaphore.c

Keep it below the new-file limit.

Store native state in an opaque byte-array field owned by `Semaphore4D`,
following `Lock`. The state contains conceptually:

    MUTEX_TYPE mutex;
    THREAD_CONDITION_TYPE condition;
    int32 permits;
    int32 waiters;
    bool initialized;

Initialization is all-or-nothing. If condition initialization fails after mutex
initialization, destroy the mutex and throw a runtime initialization error.
Never leave a partial state.

`destroy()` is private/finalization-only and idempotent. Do not add public
close/dispose.

### Operation semantics

`acquireUninterruptibly()`:

1. lock state mutex;
2. while `permits <= 0`:
   - increment `waiters`;
   - condition-wait, atomically releasing the mutex;
   - after reacquiring, decrement `waiters`;
3. decrement `permits`;
4. on Windows, if permits remain and waiters exist, signal again;
5. unlock.

`acquire()` uses the same deployed blocking primitive. Preserve its declared
`InterruptedException`; do not synthesize interruption.

`tryAcquire()` locks, succeeds only if `permits > 0`, decrements once, applies
the Windows chained-signal rule when necessary, unlocks, and returns the result.

`release()` locks, checks for `Integer.MAX_VALUE`, increments once, signals one
waiter if any exist, then unlocks. Overflow throws `java.lang.Error`.

A release before an acquire is stored only in `permits`; it must not depend on
a waiter already sleeping.

### Native source registration

Add `concurrent_Semaphore.c` wherever `concurrent_Lock.c` is explicitly listed:

    TotalCrossVM/CMakeLists.txt
    TotalCrossVM/src/jni/Android.mk
    TotalCrossVM/vc2008/TCVM.vcproj

Keep these native registration artifacts synchronized:

    TotalCrossVM/src/nm/NativeMethods.txt
    TotalCrossVM/src/nm/NativeMethodsPrototypes.txt
    TotalCrossVM/src/nm/NativeMethods.h
    TotalCrossVM/src/init/nativeProcAddressesTC.c

Use `tc.tools.NativeMethodsPrototypeGenerator` and the repository's established
generation flow where practical. Do not invent generated native symbols when
the generator can derive them.

### Build policy

Build operations are allowed only for:

- the SDK;
- native macOS.

They are allowed only at the end of this milestone.

Do not build Windows, Android, Linux, or iOS. Source integration for those
targets is required, but native validation is deferred and must be reported as
such.

## Plan of Work

### Checkpoint 0 — resumable execution artifacts

1. Verify base and create/switch to `feat/semaphore-v1`.
2. Save this plan to its repository path.
3. Create `.agent/state/semaphore-v1.md` with:
   - part `1`;
   - milestone `1`;
   - slice `1A`;
   - base SHA and branch;
   - active paths;
   - unrelated dirty paths;
   - next exact action;
   - build validation deferred to milestone closure;
   - resume command.
4. Create `.agent/evidence/semaphore-v1.jsonl` with one planning record.
5. Validate headers for the new Markdown/JSONL artifacts as applicable and run
   `git diff --check`.
6. Commit only these durable planning artifacts.

Suggested commit:

    docs(plan): define semaphore core execution plan

No build here.

### Slice 1A — internal blocking condition primitive

Modify `TotalCrossVM/src/tcvm/tcthread.h` only as needed for the condition
abstraction specified above.

Do not refactor existing mutex code and do not migrate existing thread-start
logic to the new abstraction.

Add concise comments explaining:

- real OS blocking;
- Windows auto-reset-event compatibility;
- permit-count authority;
- chained Windows wakeup requirement.

Before commit, only:

    python3 scripts/validate-copyright-headers.sh --files TotalCrossVM/src/tcvm/tcthread.h
    git diff --check -- TotalCrossVM/src/tcvm/tcthread.h

Record SDK/macOS build as deferred by plan policy.

Suggested commit:

    feat(vm): add blocking thread condition primitive

### Slice 1B — native Semaphore operations

Create `concurrent_Semaphore.c` with the exact native semantics above.

Update the three native source inventories and the four native method
registration/source-of-truth files.

Create a focused test:

    TotalCrossSDK/src/test/java/tc/tools/converter/SemaphoreConverterTest.java

It must verify:

- `java.util.concurrent.Semaphore` maps to `Semaphore4D`;
- the one-argument constructor is discoverable;
- all five v1 operations are discoverable;
- unsupported overloads are not claimed;
- every Semaphore native entry appears consistently in declarations,
  prototypes, header, and native-address registration;
- `concurrent_Semaphore.c` is listed in CMake, Android.mk, and the legacy
  Visual Studio project.

Before commit, use focused header validation and `git diff --check`. No Gradle,
CMake, Ninja, or platform builds yet.

Suggested commit:

    feat(vm): implement native semaphore operations

### Slice 1C — Java compatibility class and correctness smoke

Create `Semaphore4D.java` with only the fixed v1 API.

Use private `@ReplacedByNativeOnDeploy` bridge methods where useful for
constructor/lifecycle handling. The normal desktop application path uses the
real JDK Semaphore, so do not spend scope on a full direct-JVM behavioral
fallback for `Semaphore4D`.

Document:

- non-fair v1;
- unsupported overloads;
- effective uninterruptible deployed `acquire()`;
- signed initial permits.

Create a native smoke under:

    TotalCrossSDK/src/smokeTest/java/totalcross/util/concurrent/

Use application source imports of `java.util.concurrent.Semaphore`, never
direct `Semaphore4D`.

The smoke must cover without sleep/poll coordination:

1. one initial permit: first `tryAcquire()` true, second false;
2. release-before-acquire is remembered;
3. zero-permit worker does not progress until release;
4. both `acquire()` and `acquireUninterruptibly()` wake after release;
5. negative initial permits require enough releases to cross above zero;
6. three waiters require three releases;
7. permit overflow throws `Error`.

Use Semaphores for ready/completion handshakes. A process-level timeout is the
deadlock detector.

Add Gradle tasks following existing macOS native-smoke patterns:

- stage/package the smoke;
- deploy for macOS;
- run using explicit `-PtcvmDylib=<absolute path>`;
- require `fixture=SemaphoreSmokeApp,overallPass=true`;
- fail on non-zero exit, timeout, or missing marker.

Keep every new test/smoke file under the size limit.

Before milestone closure, only header checks and `git diff --check`.

Suggested commit:

    feat(sdk): add semaphore v1 compatibility surface

### Milestone 1 closure validation

Build only now.

From `TotalCrossSDK`:

    ./gradlew-agent test --tests tc.tools.converter.SemaphoreConverterTest
    ./gradlew-agent dist -x test
    ./gradlew-agent compileSmokeTestJava

Do not run `clean` unless stale output is demonstrated.

From repository root:

    cmake -S TotalCrossVM -B build-semaphore -DCMAKE_BUILD_TYPE=Release -G Ninja
    ninja -C build-semaphore tcvm

Locate the generated macOS `libtcvm.dylib` under `build-semaphore`, resolve its
absolute path, and run the new Gradle macOS Semaphore correctness smoke against
that exact dylib.

Do not run any other native platform build.

If validation exposes a defect, make the smallest correction and a separate
logical `fix(...)` commit. Never amend earlier commits.

At closure:

1. append compact validation evidence to
   `.agent/evidence/semaphore-v1.jsonl`;
2. rewrite state with Part 1 complete and Part 2 next;
3. create `.agent/reports/semaphore-v1-editorial.md` with:
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
   - Claims Requiring Human Review;
4. update this plan's progress/outcomes only with milestone facts;
5. commit durable state/evidence/report changes logically.

Suggested checkpoint commit:

    docs(plan): record semaphore core milestone

## Validation and Acceptance

Inside the milestone: no build operations.

Before each logical commit:

- inspect scoped diff;
- focused header validation;
- `git diff --check`;
- stage only intended paths;
- `git diff --check --cached`;
- validate the commit message using the exact command from
  `.agents/skills/logical-commits/SKILL.md`.

Milestone 1 is accepted only if:

- converter coverage passes;
- SDK distribution succeeds;
- macOS `tcvm` builds;
- deployed macOS correctness smoke passes;
- zero/negative-permit waits are real blocking waits;
- release-before-acquire is preserved;
- no Semaphore wait path uses sleep/poll/spin;
- overflow does not wrap;
- durable execution artifacts are committed;
- Windows/Android/iOS native execution is explicitly reported as deferred.

## Risks and Predetermined Responses

- Windows event signals can coalesce.
  Use permit count as truth and chained wakeups after successful acquire.

- Condition initialization can fail.
  Unwind any initialized resource immediately and throw; no partial state.

- Native smoke can deadlock.
  Let the process timeout fail the test; do not add sleeps to hide the bug.

- Converter may resolve unsupported APIs.
  Keep `Semaphore4D` narrow and test negative resolution; do not add stubs.

- Platform code may be unvalidated outside macOS.
  Keep it minimal and report the explicit deferral; do not violate build policy.

- New file approaches 20 KB/~600 lines.
  Split by responsibility; never refactor existing files just for size.

## Idempotence and Recovery

Resume from `.agent/state/semaphore-v1.md`.

Safe retries at the milestone boundary:

    ./gradlew-agent test --tests tc.tools.converter.SemaphoreConverterTest
    ./gradlew-agent dist -x test
    ./gradlew-agent compileSmokeTestJava
    cmake -S TotalCrossVM -B build-semaphore -DCMAKE_BUILD_TYPE=Release -G Ninja
    ninja -C build-semaphore tcvm

If stale native output is proven, remove only `build-semaphore`, not source or
unrelated caches.

If a commit exists but state was not updated, inspect that commit, add only
missing compact evidence, and rewrite state. Do not duplicate or amend commits.

Do not push, open PRs, merge, tag, or publish under this plan.

## Surprises & Discoveries

Initial known facts:

- POSIX TCVM thread startup already uses `pthread_cond_t`;
- `Lock` already stores an opaque native mutex in Java-owned memory;
- converter mapping already supports `jdkcompat.util.concurrent.*4D`;
- `AsyncTask.Executor` polls today, but it is out of scope.

Add only discoveries that materially affect Part 2.

## Decision Log

- Use exact base `0aeea1029f24a7e3e4f29c8f1f339ffad0a141f2`.
- Use branch `feat/semaphore-v1` in the current worktree.
- Use `jdkcompat.util.concurrent.Semaphore4D`.
- Implement only one-permit, non-fair, untimed v1 APIs.
- Preserve negative initial permits.
- Treat deployed `acquire()` as effectively uninterruptible.
- POSIX/Android/macOS use pthread conditions.
- Windows uses auto-reset events with chained wakeups.
- Build only SDK/macOS, only at milestone closure.
- Do not modify any Semaphore consumer.

All decisions dated 2026-09-24.

## Outcomes & Retrospective

At closure, record factual delivery and validation only. Do not predict
`ImagePreparation` performance.

The required next step after successful completion is:

    .agent/plans/semaphore-v1-part-2-stress.md

Do not start Part 2 until Part 1 state says Milestone 1 is complete.

## Revision Note

Initial revision, 2026-09-24: split from a larger plan to comply with the
20 KB/~600-line new-file limit and scoped Part 1 to the core primitive,
compatibility class, registration, and native macOS correctness proof.
