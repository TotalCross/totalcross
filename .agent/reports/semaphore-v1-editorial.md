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
- Added default-off `TC_ENABLE_SEMAPHORE_TEST_DIAGNOSTICS` CMake gating so
  production builds omit diagnostic state, acquire branches, hook, and address
  registration. The dedicated macOS smoke runtime opts in explicitly.
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

The follow-up removed diagnostic storage and work from default builds. The
default macOS `tcvm` compiled with the option OFF and had no diagnostic symbol;
the dedicated macOS runtime compiled with the option ON and exported the hook.
The compile databases confirmed the define appears only in the enabled build.

## Validation and Measurable Results

- `./gradlew-agent test --tests tc.tools.converter.SemaphoreConverterTest`:
  passed, 3 tests / 0 failures. Gating log:
  `TotalCrossSDK/agent-logs/20260924-145931-test-agent.log`.
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
- `./gradlew-agent compileSmokeTestJava dist -x test`: passed, 22 tasks seen,
  18 actionable, zero Javadoc errors and warnings. Log:
  `TotalCrossSDK/agent-logs/20260924-150028-compileSmokeTestJava-agent.log`.
- The diagnostic-enabled macOS configure/build passed with option ON and 123
  Ninja steps. Logs: `/tmp/semaphore-gating-cmake-on.log` and
  `/tmp/semaphore-gating-ninja-on.log`.
- A fresh normal macOS configure without a diagnostic option resolved to OFF
  and built in 123 Ninja steps. Its compile commands had no diagnostic define
  and `nm -g` found no hook symbol. Logs: `/tmp/semaphore-gating-cmake-default.log`
  and `/tmp/semaphore-gating-ninja-default.log`.
- Compile commands contain `TC_ENABLE_SEMAPHORE_TEST_DIAGNOSTICS=1` for
  `concurrent_Semaphore.c` and `nativeProcAddressesTC.c` only in the enabled
  build. `nm -g` found `tucSTD_awaitWaiters_si` in the enabled dylib and not
  in the default dylib.
- Enabled runtime:
  `build-semaphore/libtcvm.dylib`, SHA-256
  `494fa48a6c4ef92c6f07f832bce072d59d56a755c12343fdd906e2e077290202`.
- Default runtime: `build-semaphore-default-check/libtcvm.dylib`, SHA-256
  `ae6412d4dd95bebdec9f8db11c067898f8692c59bf1ea6180da5097e283dbb4b`.
- All three deployed macOS smokes passed their 60-second process timeouts.
  Correctness passed; stress reconciled 20,000 expected, produced, and
  acquired handoffs. The latest latency run confirmed all 220 handshakes and
  200 measured samples: min 2,042 ns, p50 3,041 ns, nearest-rank p95 8,709
  ns, max 148,916 ns, rounded mean 5,596 ns. Full log:
  `TotalCrossSDK/agent-logs/20260924-150057-runSemaphoreSmokeMacOS-full.log`.
- Focused copyright-header, diff, and signed commit-message checks passed.

The original SDK/macOS validation did not build Windows, Android, Linux, or
iOS locally. A Python-free Windows correctness/stress package was later
prepared from successful workflow run `36040721060`, built from
runtime source SHA `0badac435cc2c6af31de4bb0adad9ed58e6cd0c5`. It uses the
default-off diagnostic configuration and includes the workflow Windows
`tcvm.dll`, deployed executables, PowerShell 5.1 runner, and artifact hashes.
Package deployment and ZIP/hash checks passed on macOS; Windows execution is
still pending, so no Windows runtime pass is claimed. See the package folder
and ZIP under `.agent/artifacts/semaphore-windows-validation-run-36040721060`
and the latest record in `.agent/evidence/semaphore-v1.jsonl`.

## Useful Evidence and Examples

The converter fixture resolves the one-argument constructor and all four v1
operations to `jdkcompat.util.concurrent.Semaphore4D`. It checks the
`(IZ)V` fairness constructor, `acquire(I)V`, timed `tryAcquire`, and
`release(I)V` as unsupported members.

The stress result reconciles `expected=20000`, `produced=20000`, and
`acquired=20000`. The corrected latency summary was
`count=200,blockedConfirmed=200,confirmedHandshakes=220,minNs=2042,p50Ns=3041,p95Ns=8709,maxNs=148916,meanNs=5596`.
Earlier corrected runs remain descriptive too; the original unconfirmed
aggregate is historical interval data only.

## Limitations, Remaining Work, and Open Questions

The corrected latency result is descriptive evidence for this macOS machine;
it is not a performance threshold or a cross-platform prediction. The proof
uses macOS POSIX condition behavior. The Windows event path has not been
executed and is not covered by the blocked-wait claim. The Windows package
does not claim to observe a thread inside `WaitForSingleObject`; it checks
functional behavior with Semaphore handshakes and process timeouts. Android,
Linux, and iOS native validation remains deferred. The TotalCross VM does not
currently interrupt a blocked `acquire()` despite its Java declaration.

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
- Decide how merge policy should handle the unchanged Part 1 commit-message
  body-length failures and the body-length failure in signed commit
  `5e6a03b4e`, which was preserved without rewriting history.
- Treat the latency aggregate as descriptive evidence for this tested macOS
  machine only.
