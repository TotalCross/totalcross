<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Semaphore v1 — completion handoff

## Editorial Summary

Semaphore v1 now has a deployed compatibility surface for the standard Java
class, a native blocking implementation, and focused compatibility and
concurrency checks. Part 2 verified the four supported operations, kept
unsupported overloads unresolved, reconciled 20,000 handoffs, and measured 200
confirmed blocked-waiter release-to-acquire intervals on macOS. A follow-up
added a smoke-only native waiter diagnostic and corrected the earlier
measurement protocol. No production consumer or public Semaphore API was
changed.

## Original Plan versus Actual Outcome

Both sequential plans completed on `feat/semaphore-v1` from the specified
planning base. Part 1 supplied the one-permit, non-fair, untimed native-backed
API and macOS correctness smoke. Part 2 added source-based converter coverage,
deterministic stress, and descriptive wake-latency evidence. The build matrix
remained limited to the SDK and macOS as planned.

The compatibility fixture imports `java.util.concurrent.Semaphore`, compiles
against Java 8 APIs, and resolves its emitted bytecode calls through the
converter's device mapping. It covers the supported v1 members and verifies
that the fairness constructor, timed acquire, and multi-permit operations are
not claimed by `Semaphore4D`.

## What Changed

- Added a deterministic macOS smoke with four producer and four consumer
  threads, Semaphore readiness/start/completion handshakes, and 20,000 total
  handoffs. Neither smoke uses sleep or polling for coordination.
- Added a separate sequential wake-latency smoke with 20 warm-ups and 200
  measured handshakes, each released only after native waiter confirmation.
- Added a smoke-source-only native `awaitWaiters` bridge used by correctness
  and latency smokes; the production Semaphore API remains unchanged.
- Added Gradle compile, jar, deploy, and timed native-run tasks for both smokes.
- Extended converter coverage to compile a standard-Java source fixture and
  resolve the supported and intentionally unsupported declarations.
- Rebuilt and used the macOS native dylib after adding the waiter diagnostic.

## Decisions and Trade-offs

The supported API remains `Semaphore(int)`, `acquire()`,
`acquireUninterruptibly()`, `tryAcquire()`, and `release()`. Fairness, timed
operations, multi-permit operations, queue inspection, serialization, and
interruption support remain outside v1.

The converter fixture targets Java 8 bytecode so the repository's bundled ASM
5.2 parser can read it. Wake-latency p50 is the floored midpoint of the two
central values, p95 uses nearest rank, and the arithmetic mean is rounded to
the nearest integer nanosecond. There is no absolute latency threshold.

## Unexpected Problems and Discoveries

The initial Part 2 converter run referenced `Opcodes.ASM9`, which is absent
from the SDK's ASM 5.2 dependency. Switching to ASM5 exposed a second issue:
the host javac's default classfile version was too new for that parser. The
fixture now uses ASM5 and `javac --release 8`; the focused suite passed 3/3.
Both failed attempts and the passing rerun are indexed in the evidence file.

The Part 1 SDK distribution summary reported 100 Javadoc errors and 100
warnings without attributing them. The Part 2 distribution completed with no
Javadoc errors or warnings. Two earlier Part 1 commit-message checks failed on
body-line length; those commits remain unchanged under the plan's no-rewrite
rule. All Part 2 commit-message checks passed.

Reviewing the latency protocol found that `consumerReady` was released before
`acquireUninterruptibly()`. The old aggregate is retained as unconfirmed
interval data and is not blocked-wait evidence. A smoke-only native diagnostic
now waits for a requested `waiters` count under the Semaphore state mutex. On
the validated macOS POSIX path, this confirms the consumer entered the
condition wait before release. The legacy Windows wrapper unlocks before its
event wait, so this proof is not claimed for Windows.

## Validation and Measurable Results

- `./gradlew-agent test --tests tc.tools.converter.SemaphoreConverterTest`:
  passed, 3 tests / 0 failures. Follow-up log:
  `TotalCrossSDK/agent-logs/20260924-144041-test-agent.log`.
- `./gradlew-agent dist -x test`: passed in 9 seconds; 21 tasks seen, 17
  actionable, zero Javadoc errors and warnings. Log:
  `TotalCrossSDK/agent-logs/20260924-024354-dist-agent.log`.
- `./gradlew-agent compileSmokeTestJava`: passed in 2 seconds. Log:
  `TotalCrossSDK/agent-logs/20260924-024409-compileSmokeTestJava-agent.log`.
- Part 1 correctness smoke passed with
  `fixture=SemaphoreSmokeApp,overallPass=true`.
- Stress passed with 4 producers, 4 consumers, 20,000 expected/produced/
  acquired handoffs, and no timeout.
- The prior latency smoke completed 200 intervals, but did not confirm a
  blocked waiter before release. Its aggregate (minimum 1,250 ns; p50 3,166 ns;
  nearest-rank p95 16,208 ns; maximum 63,375 ns; rounded mean 5,239 ns) is
  superseded and must not be cited as blocked-waiter latency.
- Follow-up `./gradlew-agent compileSmokeTestJava dist -x test`: passed, 22
  tasks seen, 18 actionable, zero Javadoc errors and warnings. Log:
  `TotalCrossSDK/agent-logs/20260924-144110-compileSmokeTestJava-agent.log`.
- CMake configure and `ninja -C build-semaphore tcvm` passed; 7 Ninja steps
  built the macOS dylib. Logs: `/tmp/semaphore-correction-cmake.log` and
  `/tmp/semaphore-correction-ninja.log`.
- The rebuilt dylib is
  `build-semaphore/libtcvm.dylib`, SHA-256
  `8caeee9e84485666604405fd8bd70206d393aac45b685f87cf9afd99612492de`.
- All three deployed macOS smokes passed their 60-second process timeouts.
  Correctness passed; stress reconciled 20,000 expected, produced, and
  acquired handoffs. Latency reported 200 samples with all 220 warm-up and
  measured handshakes confirmed: min 1,500 ns, p50 2,500 ns, nearest-rank p95
  6,041 ns, max 27,834 ns, rounded mean 3,201 ns. Full log:
  `TotalCrossSDK/agent-logs/20260924-144156-runSemaphoreSmokeMacOS-full.log`.
- Focused copyright-header, diff, and signed commit-message checks passed.

Only SDK and macOS were built. Windows, Android, Linux, and iOS native builds
and execution were deferred. Full commands, sample aggregates, and logs are
indexed in `.agent/evidence/semaphore-v1.jsonl`.

## Useful Evidence and Examples

The converter fixture resolves the one-argument constructor and all four v1
operations to `jdkcompat.util.concurrent.Semaphore4D`. It checks the
`(IZ)V` fairness constructor, `acquire(I)V`, timed `tryAcquire`, and
`release(I)V` as unsupported members.

The stress result reconciles `expected=20000`, `produced=20000`, and
`acquired=20000`. The corrected latency summary was
`count=200,blockedConfirmed=200,confirmedHandshakes=220,minNs=1500,p50Ns=2500,p95Ns=6041,maxNs=27834,meanNs=3201`.
The prior summary remains historical, unconfirmed interval data.

## Limitations, Remaining Work, and Open Questions

The corrected latency result is descriptive evidence for this macOS machine;
it is not a performance threshold or a cross-platform prediction. The proof
uses macOS POSIX condition behavior. The Windows event path has not been
executed and is not covered by the blocked-wait claim. Android, Linux, and iOS
native validation remains deferred. The TotalCross VM does not currently
interrupt a blocked `acquire()` despite its Java declaration.

No `ImagePreparation` performance claim was tested. Any future evaluation of
Semaphore as a production wake mechanism belongs in a separate plan.

## Possible Article Angles

- Keeping a blocking primitive's permit count authoritative across condition
  waits and platform-specific wake mechanisms.
- Verifying API compatibility from compiled standard-Java calls rather than
  source-name checks alone.
- Testing concurrency with handshakes and process timeouts instead of sleeps.

## Suggested Narrative

Introduce the compatibility boundary and native blocking path, then show how
the converter fixture protects the v1 contract. Follow with exact handoff
reconciliation and the 200-sample wake measurement. Close by separating the
macOS results from deferred platform execution and unmeasured consumer gains.

## Claims Requiring Human Review

- Confirm Windows and WinCE event API behavior on supported toolchains before
  claiming runtime support there.
- Decide how merge policy should handle the two unchanged Part 1
  commit-message body-length failures.
- Treat the latency aggregate as descriptive evidence for this tested macOS
  machine only.
