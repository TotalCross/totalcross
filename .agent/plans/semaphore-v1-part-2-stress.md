<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Semaphore v1 — Part 2: compatibility, stress, and wake-latency evidence

This ExecPlan follows `AGENTS.md`, `.agent/PLANS.md`, and
`.agents/skills/logical-commits/SKILL.md`. Execute it only after
`.agent/plans/semaphore-v1-part-1-core.md` is complete.

## Purpose / Big Picture

Finish the second Semaphore milestone without integrating Semaphore into any
production consumer.

Part 1 must already have delivered:

- `jdkcompat.util.concurrent.Semaphore4D`;
- deployed mapping from `java.util.concurrent.Semaphore`;
- real native blocking without polling;
- one-permit, non-fair, untimed v1 operations;
- native registration;
- passing macOS correctness smoke.

Part 2 adds only compatibility verification, repeated concurrency stress, and a
small release-to-acquire wake-latency measurement.

Do not change `ImagePreparation`, `AsyncTask`, image benchmarks, executors,
thread interruption, or default runtime behavior.

Do not expand the Semaphore API.

## Preconditions and Branch

Continue on:

    feat/semaphore-v1

in the same worktree.

Read first:

    .agent/state/semaphore-v1.md

The state must say Part 1 / Milestone 1 is complete and must name the final
Milestone 1 commit and successful macOS correctness smoke.

Verify:

    git branch --show-current
    git merge-base HEAD 0aeea1029f24a7e3e4f29c8f1f339ffad0a141f2

The branch must be `feat/semaphore-v1` and the merge-base must be the planning
base above. Do not create a worktree, stash, reset, rebase, amend, or rewrite.

If Part 1 is incomplete, return to
`.agent/plans/semaphore-v1-part-1-core.md`; do not compensate in this plan.

Store this file as:

    .agent/plans/semaphore-v1-part-2-stress.md

and commit it before Part 2 implementation if it is not already tracked.

## Working Set and Resume Protocol

Reuse:

    .agent/state/semaphore-v1.md
    .agent/evidence/semaphore-v1.jsonl
    .agent/reports/semaphore-v1-editorial.md

On resume, read state first. Search evidence only for the referenced milestone
or revision. Read the editorial report only at finalization or when checking a
recorded limitation.

Every new file must remain below 20 KB or approximately 600 lines. Do not
refactor existing files solely to reduce their size.

All durable test/plan/state/evidence/report artifacts are committed. Raw logs,
deployed binaries, build directories, and per-sample output are not.

## Progress

- [x] Checkpoint 2A: commit this Part 2 plan if not already tracked.
- [x] Add standard-API compatibility/negative-resolution coverage.
- [x] Add deterministic multi-thread Semaphore stress smoke.
- [x] Add 200-sample release-to-acquire wake-latency smoke.
- [x] Run SDK/macOS-only milestone validation.
- [x] Finalize state/evidence/editorial report and close the two-part plan.
- [x] Correct blocked-wait latency confirmation and validate the follow-up.
- [x] Gate the waiter diagnostic out of default production builds.

## Fixed Architecture and Scope

The supported deployed v1 remains exactly:

    Semaphore(int permits)
    acquire()
    acquireUninterruptibly()
    tryAcquire()
    release()

Do not add:

- fairness constructor;
- timed acquire;
- multi-permit overloads;
- queue inspection;
- `availablePermits()`;
- `drainPermits()`;
- serialization support;
- interruption implementation.

The production implementation from Part 1 must remain free of
`Vm.sleep`, `Vm.safeSleep`, polling, and spin loops.

If stress or latency testing reveals a correctness defect, fix only the
Semaphore implementation and add a logical `fix(...)` commit. Do not redesign
unrelated threading infrastructure.

## Plan of Work

### Checkpoint 2A — track the sequential plan

If this file was not committed by Part 1, add it now with the state update that
marks Part 2 active.

Before commit:

    python3 scripts/validate-copyright-headers.sh --files \
      .agent/plans/semaphore-v1-part-2-stress.md \
      .agent/state/semaphore-v1.md
    git diff --check

Suggested commit:

    docs(plan): define semaphore stress validation plan

No build.

### Slice 2B — compatibility and unsupported-API coverage

Extend the focused converter test created in Part 1.

Use source/declaration fixtures that import the standard class:

    java.util.concurrent.Semaphore

Positive coverage must exercise every supported v1 operation.

Negative coverage must prove the compatibility layer does not claim support for
at least:

    new Semaphore(1, true)
    tryAcquire(long, TimeUnit)
    acquire(int)
    release(int)

Prefer declaration-resolution or converter fixtures that fail for the expected
unsupported member. Do not add fake methods to `Semaphore4D` merely to make a
fixture compile/deploy.

Keep this test focused on compatibility mapping; do not duplicate native stress
logic in JUnit.

Before commit, only focused header checks and `git diff --check`.

Suggested commit when this is a distinct logical slice:

    test(converter): cover semaphore v1 compatibility surface

No build until milestone closure.

### Slice 2C — deterministic producer/consumer stress

Create a separate native smoke source, for example:

    TotalCrossSDK/src/smokeTest/java/totalcross/util/concurrent/SemaphoreStressSmokeApp.java

Use ordinary Java source imports of `java.util.concurrent.Semaphore`.

Do not call `Semaphore4D` directly.

The stress must:

- use at least four consumer threads;
- use four producer threads when the TotalCross thread environment permits;
- use a deterministic fixed operation count;
- use no sleep/poll/spin coordination;
- use Semaphore handshakes for start and completion;
- account for every produced and acquired permit;
- finish with exact equality between expected handoffs and completed acquires;
- emit one concise PASS/FAIL summary.

Initial workload:

    20,000 total handoffs

Divide work deterministically among producers and consumers. Do not raise the
count unless the native macOS run is so short that it fails to exercise
concurrency meaningfully.

Do not use thread joins that are absent from the TotalCross compatibility
surface. Use completion Semaphores.

The native process wrapper must have a hard timeout so a deadlock becomes a
test failure rather than an indefinite run.

Add/stage Gradle tasks following the Part 1 macOS smoke structure.

Before commit, only focused header validation and `git diff --check`.

### Slice 2D — release-to-acquire wake-latency measurement

Add a dedicated small smoke source or keep this measurement in the stress app
only if the resulting file stays comfortably below the size limit and the two
concerns remain readable.

Measure only the Semaphore wake path.

Use two worker-control Semaphores plus the tested Semaphore so samples cannot
overlap:

1. consumer signals it is ready for the next sample;
2. producer receives readiness;
3. producer records `System.nanoTime()`;
4. producer calls `release()`;
5. consumer returns from `acquireUninterruptibly()`;
6. consumer records/exports the elapsed duration;
7. consumer signals completion;
8. producer starts the next sample only after completion.

Run:

    20 warm-up handshakes
    200 measured handshakes

Exclude warm-up values.

Compute and report:

- count;
- minimum;
- median/p50;
- p95;
- maximum;
- arithmetic mean.

Use deterministic integer nanosecond data. Do not introduce an absolute
latency pass/fail threshold. The test passes when all 200 samples complete
without timeout/lost wakeup and every recorded duration is non-negative.
For the even sample count, report p50 as the floor of the midpoint between the
two central values, p95 with the nearest-rank method, and mean rounded to the
nearest integer nanosecond.

Raw per-sample values are ordinary test output. Do not commit them. Append only
the compact aggregate result to `.agent/evidence/semaphore-v1.jsonl`.

Suggested commit for stress + latency when implemented together coherently:

    test(sdk): stress semaphore blocking semantics

If the latency smoke is a separate functional artifact, it may be a separate
logical test commit.

No build before the milestone boundary.

## Milestone 2 Closure Validation

Build operations are allowed only now, only for SDK and macOS.

From `TotalCrossSDK`:

    ./gradlew-agent test --tests tc.tools.converter.SemaphoreConverterTest
    ./gradlew-agent dist -x test
    ./gradlew-agent compileSmokeTestJava

Do not run `clean` unless stale output is demonstrated.

### Native runtime reuse rule

Read the Milestone 1 state/evidence record.

If no production native Semaphore/thread source changed after the recorded
Milestone 1 macOS `libtcvm.dylib` build, reuse that exact dylib for all Part 2
native smokes. Do not rebuild native code just to repeat it.

If production native source did change in Part 2, rebuild only macOS:

    cmake -S TotalCrossVM -B build-semaphore -DCMAKE_BUILD_TYPE=Release -G Ninja
    ninja -C build-semaphore tcvm

Do not build Windows, Android, Linux, or iOS.

Run against the selected exact dylib:

1. Part 1 Semaphore correctness smoke;
2. producer/consumer stress smoke;
3. wake-latency smoke.

All must finish within their process timeouts.

The wake-latency result is descriptive evidence, not a performance gate.

## Final Validation and Acceptance

Run focused copyright/header validation for every changed first-party source.
Then:

    git diff --check

For every created commit, run the commit-message validator from:

    .agents/skills/logical-commits/SKILL.md

Inspect the scoped worktree and verify:

- every durable plan/state/evidence/editorial/test artifact is tracked and
  committed;
- no build output/log/native binary is staged;
- no out-of-scope consumer changed;
- no unsupported Semaphore API was added.

Part 2 is accepted only if:

- standard Java source resolves every supported v1 operation;
- intentionally unsupported APIs remain unsupported;
- focused converter tests pass;
- SDK distribution succeeds;
- macOS correctness smoke still passes;
- 20,000-handoff stress reconciles exactly;
- 200 measured wake samples complete;
- no test relies on sleep/polling for correctness;
- no production Semaphore wait path polls or sleeps;
- Windows/Android/iOS native execution remains explicitly deferred, not claimed.

## Commit Policy

Follow `.agents/skills/logical-commits/SKILL.md`.

Before each commit:

1. `git status --short -- <task paths>`;
2. inspect `git diff --stat` and scoped diff;
3. focused header validation;
4. smallest allowed non-build validation;
5. stage only intended paths;
6. `git diff --check --cached`;
7. review staged stat/diff;
8. signed logical Conventional Commit with English non-trivial body;
9. validate the commit message;
10. rewrite `.agent/state/semaphore-v1.md`.

Do not amend/rewrite history.

Any correctness fix found by milestone validation must be a separate logical
fix commit.

Suggested final documentation commit:

    docs(plan): complete semaphore v1 validation

Do not push, open PRs, merge, tag, or publish unless separately requested.

## Evidence and Editorial Finalization

Append one compact evidence record for:

- focused converter tests;
- SDK dist;
- native runtime selection/rebuild;
- correctness smoke;
- stress smoke;
- wake-latency aggregate.

Do not duplicate raw logs or 200 samples in evidence.

Finalize `.agent/reports/semaphore-v1-editorial.md` with:

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

Explicitly state:

- only SDK/macOS were built;
- other native platform execution was deferred;
- wake latency is measured evidence, not a cross-platform prediction;
- no `ImagePreparation` performance claim was tested.

Rewrite `.agent/state/semaphore-v1.md` to final/complete and record the final
logical commit plus any deferred validations.

## Risks and Predetermined Responses

- Stress deadlocks.
  Let the process timeout fail; inspect the smallest relevant log and fix
  Semaphore. Do not add sleeps.

- Stress counts mismatch.
  Treat as correctness failure. Do not reduce concurrency to hide it.

- Latency has high variance.
  Keep 200 samples unless variance makes the aggregate unusable. If more are
  required, record the reason before increasing; do not exceed scope casually.

- Native code changed after Milestone 1.
  Rebuild only macOS at closure, then rerun all three native smokes.

- Unsupported overload unexpectedly resolves.
  Fix the compatibility surface; do not add placeholder semantics.

- Windows/Android/iOS remain unbuilt.
  Record the explicit plan constraint as the limitation. Do not claim support
  from source inspection alone.

## Idempotence and Recovery

Resume from `.agent/state/semaphore-v1.md`.

Safe retries at the milestone boundary:

    ./gradlew-agent test --tests tc.tools.converter.SemaphoreConverterTest
    ./gradlew-agent dist -x test
    ./gradlew-agent compileSmokeTestJava

The stress and latency smokes are deterministic in operation count and may be
rerun safely.

If stale native output is proven and a rebuild is required, remove only the
dedicated generated `build-semaphore` directory.

If a commit exists but state/evidence missed it, recover from the commit;
do not recreate or amend it.

## Decision Log

Inherited fixed decisions from Part 1:

- exact base `0aeea1029f24a7e3e4f29c8f1f339ffad0a141f2`;
- branch `feat/semaphore-v1`, same worktree;
- `jdkcompat.util.concurrent.Semaphore4D`;
- one-permit non-fair untimed v1;
- negative initial permits preserved;
- deployed acquire effectively uninterruptible;
- real native blocking, never polling;
- SDK/macOS-only builds at milestone boundaries;
- no production consumer integration.

Part 2 decisions:

- stress begins at 20,000 deterministic handoffs;
- wake measurement uses 20 warm-ups + 200 samples;
- no absolute latency threshold;
- reuse the Part 1 dylib unless production native source changed.

All decisions dated 2026-09-24.

## Outcomes & Retrospective

Part 2 closed after the focused converter suite passed 3/3, SDK distribution
and smoke compilation passed, and all three deployed native macOS smokes
passed against the unchanged Part 1 dylib. The stress reconciled 20,000
produced and acquired permits across four producers and four consumers.

The original 200 release-to-acquire intervals reported min 1,250 ns, p50
3,166 ns, nearest-rank p95 16,208 ns, max 63,375 ns, and rounded mean
5,239 ns. A follow-up review found that the producer could release before the
consumer entered the native wait. These values are unconfirmed intervals and
must not be cited as blocked-waiter latency.

Windows, Android, Linux, and iOS native builds and execution remain deferred.
The deployed `acquire()` still cannot be interrupted by the TotalCross VM.
No image-prefetch performance was measured or inferred.

Do not infer or estimate image-prefetch gains.

A later, separate ExecPlan may evaluate Semaphore as the wake mechanism for a
persistent `ImagePreparation` worker.

## Revision Note

Initial revision, 2026-09-24: sequential Part 2 split from the larger Semaphore
plan to satisfy the 20 KB/~600-line new-file limit. Scope is compatibility,
stress, wake-latency evidence, and final handoff only.

## Wake-Latency Methodology Correction

The original latency smoke signaled `consumerReady` before calling
`acquireUninterruptibly()`. That handshake did not prove that the consumer had
incremented the native waiters count or entered the OS wait. Retain the old
aggregate only as historical, unblocked/unconfirmed interval evidence.

Follow-up work on the same branch and worktree:

- [x] Add smoke-source-only `SemaphoreTestDiagnostics.awaitWaiters`; do not
  add a method to `java.util.concurrent.Semaphore`.
- [x] Update the correctness and latency smokes to confirm the required waiter
  count before release; preserve the 20 + 200 protocol and statistics.
- [x] Rebuild SDK and macOS `tcvm`, then rerun converter, correctness, stress,
  and latency validation against that exact dylib.

The diagnostic hook waits on a lazy diagnostic condition associated with the
Semaphore state. `acquireSemaphore()` signals it after incrementing
`waiters`, while holding the same state mutex, only when a diagnostic waiter
is registered. `awaitWaiters(minimumWaiters)` loops until the native count
reaches the requested value under that mutex. On macOS, the POSIX condition
wait atomically releases the mutex while waiting, so the diagnostic hook can
reacquire it only after the acquiring thread enters the OS wait. The
subsequent production `release()` must acquire that same mutex. This proves
the measured macOS waiter is blocked without sleeps or timed polling.

Keep the diagnostic Java class in `src/smokeTest/java` and use it only from
the smoke apps. Initialize its condition lazily so ordinary Semaphore use
does not allocate diagnostic OS resources. The public v1 API and permit
predicate remain unchanged.

The Windows condition wrapper releases its critical section before waiting on
an auto-reset event, so this waiter-count hook does not prove the event wait
has started on Windows. The corrected blocked-wait claim applies to the
validated macOS POSIX path only; no Windows latency claim is made.

Call the corrected result **blocked waiter release-to-acquire wake latency**
only if the run confirms all 220 handshakes, including every one of the 200
measured samples, observed `waiters > 0` before release. The completed run
reported 200 blocked samples and 220 confirmed handshakes. Otherwise report a
run as incomplete and do not use it as blocked-wake evidence.

## Correction Outcome

The corrected macOS result was min 1,500 ns, p50 2,500 ns, nearest-rank p95
6,041 ns, max 27,834 ns, and rounded mean 3,201 ns. It describes this machine
and this measurement run only. The original 200 intervals remain historical,
unconfirmed data and are not blocked-wake evidence. Detailed commands, logs,
the rebuilt dylib hash, and counts are indexed in
`.agent/evidence/semaphore-v1.jsonl`.

## Compile-Time Diagnostic Gating

This slice removes diagnostic state and branches from normal TCVM builds while
retaining the corrected macOS smoke. It adds the default-off CMake option
`TC_ENABLE_SEMAPHORE_TEST_DIAGNOSTICS`; define its C macro only on `tcvm` when
enabled. Guard the extra Semaphore state fields, acquire signal, destruction,
native hook, and native address registration. Keep Android and Windows build
files at their default-off behavior.

Acceptance passed: converter assertions protect the public v1 surface, verify
the default native state/acquire path and conditional registration, and keep
the Java bridge outside `src/main`. `build-semaphore` built with diagnostics
ON and passed all macOS smokes; the separate default-OFF `tcvm` build passed
with no hook symbol. Compile commands and both artifact identities are in the
evidence index.

Use explicit configurations:

    cmake -S TotalCrossVM -B build-semaphore \
      -DCMAKE_BUILD_TYPE=Release -G Ninja \
      -DTC_ENABLE_SEMAPHORE_TEST_DIAGNOSTICS=ON
    ninja -C build-semaphore tcvm
    cmake -S TotalCrossVM -B build-semaphore-default-check \
      -DCMAKE_BUILD_TYPE=Release -G Ninja
    ninja -C build-semaphore-default-check tcvm

Run the SDK converter test, `compileSmokeTestJava dist -x test`, then
`runSemaphoreSmokeMacOS`, `runSemaphoreStressSmokeMacOS`, and
`runSemaphoreWakeLatencySmokeMacOS` with `-PtcvmDylib` pointing to the enabled
`build-semaphore/libtcvm.dylib`.
