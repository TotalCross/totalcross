<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Semaphore v1 execution state

- Part/milestone/slice: Part 1 / Milestone 1, Part 2 / Milestone 2, and the
  blocked-wait latency methodology correction are complete.
- Branch: `feat/semaphore-v1`; same worktree, no history rewrite.
- Original follow-up base: `128a1fa36bd4c249ea51d31e2b2be970f455f942`.
- Planning base: `0aeea1029f24a7e3e4f29c8f1f339ffad0a141f2`.
- Part 1 final milestone commit: `daef7cee82370feb9995ad410dfb82dd25e190c5`.
- Part 2 final non-documentation commit: `c76b9b76a`.
- Methodology checkpoint: `8f223f65f`; correction implementation:
  `1ae094b3cb099d2c2c4a54398107b9911c70d64b`
  (`fix(vm): confirm blocked semaphore wake samples`), signed and validated.
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
- Corrected macOS result: 20 warm-ups and 200 measured samples; all 220
  handshakes confirmed a waiter before release. min 1,500 ns, p50 2,500 ns,
  nearest-rank p95 6,041 ns, max 27,834 ns, rounded mean 3,201 ns. This is
  descriptive evidence for the tested macOS machine only.
- Validation passed: 3 focused converter tests; smoke-source compilation and
  SDK distribution; macOS CMake/Ninja `tcvm` build; correctness smoke; stress
  smoke with 20,000 expected/produced/acquired handoffs; and latency smoke.
  Logs and full commands are indexed in `.agent/evidence/semaphore-v1.jsonl`.
- SDK summary: 22 tasks seen, 18 actionable, zero Javadoc errors or warnings.
  All three deployed macOS smokes passed their 60-second timeouts.
- Rebuilt native runtime:
  `/Users/flsobral/repos/totalcross-image-scroll-raster-fast-path/build-semaphore/libtcvm.dylib`
  with SHA-256
  `8caeee9e84485666604405fd8bd70206d393aac45b685f87cf9afd99612492de`.
- Platform limit: the blocked-wait proof is validated on macOS POSIX condition
  waits. The Windows wrapper releases its critical section before waiting on
  an event, so this hook does not prove the Windows event wait has started.
  Windows, Android, Linux, and iOS native builds and execution were deferred
  by the requested scope.
- Retained semantic limitation: TotalCross `acquire()` declares
  `InterruptedException`, but the native wait is effectively uninterruptible.
  No `ImagePreparation` performance claim was tested.
- Earlier Part 1 commit-message body-length failures remain unchanged under
  the no-rewrite rule; the correction commit message validation passed.
- Blockers: none.
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
- Resume: no implementation or validation work remains. If revisited, read
  this state first and keep any new latency claim platform-scoped.
