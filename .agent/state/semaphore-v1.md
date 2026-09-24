<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Semaphore v1 execution state

- Part/milestone/slice: Part 1 / Milestone 1, Part 2 / Milestone 2, blocked-wait
  methodology correction, and diagnostic gating are validated. The Windows
  correctness/stress package is prepared; actual Windows execution is pending.
- Branch: `feat/semaphore-v1`; same worktree, no history rewrite.
- Original follow-up base: `128a1fa36bd4c249ea51d31e2b2be970f455f942`.
- Planning base: `0aeea1029f24a7e3e4f29c8f1f339ffad0a141f2`.
- Part 1 final milestone commit: `daef7cee82370feb9995ad410dfb82dd25e190c5`.
- Part 2 final non-documentation commit: `c76b9b76a`.
- Methodology checkpoint: `8f223f65f`; correction implementation:
  `1ae094b3cb099d2c2c4a54398107b9911c70d64b`
  (`fix(vm): confirm blocked semaphore wake samples`), signed and validated.
- Compile-time gating implementation: `ea5e6e36d`
  (`fix(vm): gate semaphore test diagnostics`), signed and validated.
- Implementation baseline for the Windows package: `f59bb65c399ba53bdf63430bffe92ddf2f590c43`.
- Completed objective: remove diagnostic fields, branches, hook, and
  registration from the default TCVM while opting in for the dedicated macOS
  smoke runtime. Preserve the corrected 20-warm-up/200-sample protocol.
- Active plan and handoff: `.agent/plans/semaphore-v1-part-2-stress.md`,
  `.agent/evidence/semaphore-v1.jsonl`, and
  `.agent/reports/semaphore-v1-editorial.md`.
- Correction: smoke-only `SemaphoreTestDiagnostics.awaitWaiters` observes the
  native waiter count under the Semaphore state mutex. Every latency release
  waited for a positive count; correctness checks also wait for the requested
  count, including all three workers in the three-waiter case. The public
  `java.util.concurrent.Semaphore` API and permit behavior remain unchanged.
- Earlier latency aggregate (min 1,250 ns; p50 3,166 ns; p95 16,208 ns;
  max 63,375 ns; mean 5,239 ns) remains unconfirmed and is not blocked-wake
  evidence.
- Latest gated macOS result: 20 warm-ups and 200 measured samples; all 220
  handshakes confirmed a waiter before release. min 2,042 ns, p50 3,041 ns,
  nearest-rank p95 8,709 ns, max 148,916 ns, rounded mean 5,596 ns. This is
  descriptive evidence for the tested macOS machine only.
- The dedicated enabled CMake build passed with the macro on both
  `concurrent_Semaphore.c` and `nativeProcAddressesTC.c`; `nm` found the hook.
  A fresh configuration with no diagnostic option resolved to OFF and built
  with neither compile define nor hook symbol. CMake caches, compile commands,
  and hashes are indexed in evidence.
- Validation passed: 3 focused converter tests; smoke-source compilation and
  SDK distribution; enabled and disabled macOS CMake/Ninja `tcvm` builds;
  correctness smoke; stress with 20,000 expected/produced/acquired handoffs;
  and latency smoke. All macOS smokes passed their 60-second timeouts.
- SDK summary: 22 tasks seen, 18 actionable, zero Javadoc errors or warnings.
- Enabled native runtime:
  `/Users/flsobral/repos/totalcross-image-scroll-raster-fast-path/build-semaphore/libtcvm.dylib`
  with SHA-256
  `494fa48a6c4ef92c6f07f832bce072d59d56a755c12343fdd906e2e077290202`.
- Default native runtime:
  `/Users/flsobral/repos/totalcross-image-scroll-raster-fast-path/build-semaphore-default-check/libtcvm.dylib`
  with SHA-256
  `ae6412d4dd95bebdec9f8db11c067898f8692c59bf1ea6180da5097e283dbb4b`.
- Platform limit: the blocked-wait proof is validated on macOS POSIX condition
  waits. The Windows wrapper releases its critical section before waiting on
  an event, so this hook does not prove the Windows event wait has started.
  Windows, Android, Linux, and iOS native builds and execution were deferred
  by the requested scope.
- Retained semantic limitation: TotalCross `acquire()` declares
  `InterruptedException`, but the native wait is effectively uninterruptible.
  No `ImagePreparation` performance claim was tested.
- Package preparation commits: `5e6a03b4e`, `76677b26a`, and `f59bb65c3`;
  each is signed. Message checks pass for the last two. The first package
  commit has body lines over 80 characters and is preserved without rewrite.
- Workflow run `36040721060` succeeded from runtime source SHA
  `0badac435cc2c6af31de4bb0adad9ed58e6cd0c5`. The Windows workflow uses the
  CMake default `TC_ENABLE_SEMAPHORE_TEST_DIAGNOSTICS=OFF`.
- Package folder and ZIP:
  `.agent/artifacts/semaphore-windows-validation-run-36040721060/` and
  `.agent/artifacts/semaphore-windows-validation-run-36040721060.zip`.
- Local package preparation passed: Java 17 compile/deploy from the pinned
  artifacts, package ZIP integrity, and every manifest file hash. This is not
  a Windows execution result. Windows test status remains pending.
- Next action: run `run-semaphore-windows-tests.cmd` on Windows and collect
  `results-summary.txt`, both per-test stdout/stderr logs, and updated
  `provenance.json`. Do not record Windows PASS until that execution exists.
- Unrelated dirty paths to preserve: `totalcross.code-workspace`,
  `.agent/benchmarks/image-scroll-prefetch/final-definitive-pass/`,
  `.agent/benchmarks/image-scroll-prefetch/final-fixed/`,
  `.agent/benchmarks/scroll-raster-reuse-poc/m2/`,
  `.agent/plans/image-optimization-mask-01-diagnose.md`,
  `.agent/plans/image-optimization-mask-02-fix.md`,
  `.agent/plans/image-scroll-prefetch-reduction.md`,
  `.agent/plans/prefetch-thread-diagnostics-execplan.md`,
  `.agent/state/image-scroll-prefetch-reduction.md`,
  `ImageScrollRasterFastPathBenchmarkApp.log`,
  `ImageScrollRealWorkloadBenchmarkApp.log`, `TotalCrossSDK/IOSDateFixture.tcz`,
  `TotalCrossSDK/ImageScrollRealWorkloadBenchmarkApp.log`,
  `TotalCrossSDK/etc/launchers/`, `TotalCrossVM/xcode/generated/`, and
  `scripts/__pycache__/`.
- Earlier Part 1 commit-message body-length failures remain unchanged under
  the no-rewrite rule; the initial Windows package commit adds one further
  body-length failure. Later package correction commits passed their checks.
- Unresolved work is the actual Windows execution only; native Windows build
  was taken from the verified workflow artifact and not rebuilt locally.
- Resume: read this state, then run the packaged `.cmd` on Windows. Keep the
  returned summary, logs, and provenance with the artifact before updating
  this state, evidence, and editorial report with results.
