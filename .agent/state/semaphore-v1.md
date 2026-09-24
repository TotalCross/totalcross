<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Semaphore v1 execution state

- Part/milestone/slice: Part 1 / Milestone 1 and Part 2 / Milestone 2 complete;
  blocked-wait latency methodology correction active.
- Branch: `feat/semaphore-v1`.
- Follow-up baseline HEAD: `128a1fa36bd4c249ea51d31e2b2be970f455f942`.
- Planning base: `0aeea1029f24a7e3e4f29c8f1f339ffad0a141f2`.
- Part 1 final milestone commit: `daef7cee82370feb9995ad410dfb82dd25e190c5`.
- Part 2 plan checkpoint: `694e8c35e`.
- Part 2 slices: compatibility test `3689a905b`; deterministic stress
  `c5ecff6c2`; wake latency `479172ffe`; converter fixture fix `c76b9b76a`.
- Part 2 final non-documentation commit: `c76b9b76a`
  (`fix(test): align semaphore fixture with bundled ASM`). Final plan, state,
  evidence, and editorial closure are committed as
  `docs(plan): complete semaphore v1 validation`.
- Active/completed paths: `.agent/plans/semaphore-v1-part-1-core.md`,
  `.agent/plans/semaphore-v1-part-2-stress.md`, this file,
  `.agent/evidence/semaphore-v1.jsonl`,
  `.agent/reports/semaphore-v1-editorial.md`,
  `TotalCrossSDK/build.gradle`,
  `TotalCrossSDK/src/main/java/jdkcompat/util/concurrent/Semaphore4D.java`,
  the focused converter test, and three Semaphore smoke apps.
- Prior Part 2 validation passed: focused converter suite, 3 tests / 0 failures;
  `./gradlew-agent dist -x test`; `./gradlew-agent compileSmokeTestJava`;
  Part 1 correctness smoke; 20,000-handoff stress with 20,000 produced and
  20,000 acquired; 200-sample wake-latency smoke. Evidence and log paths are
  indexed in `.agent/evidence/semaphore-v1.jsonl`.
- SDK distribution reported 21 tasks seen, 17 actionable, and no Javadoc
  errors or warnings. The three deployed macOS smokes each passed their
  60-second process timeout.
- Native runtime: reused
  `/Users/flsobral/repos/totalcross-image-scroll-raster-fast-path/build-semaphore/libtcvm.dylib`
  (SHA-256 `797c1e7ce24fa4d0742fea17ac5006153547e925ff67f815b64aa3bdd2dba415`).
  No `TotalCrossVM` path changed after the Part 1 closure commit, so no native
  rebuild was needed.
- The prior wake-latency aggregate (min 1,250 ns; p50 3,166 ns; p95 16,208 ns;
  max 63,375 ns; mean 5,239 ns) is superseded as blocked-wake evidence because
  readiness was signaled before `acquireUninterruptibly()` entered native wait.
- Follow-up design: add a smoke-source-only native `awaitWaiter` hook backed
  by a lazily initialized diagnostic condition under the Semaphore state
  mutex. The hook must return only after observing `waiters > 0`; use it before
  every latency release and in a correctness blocked-acquire case.
- Next action: implement the diagnostic hook and call it from the smoke apps.
  Then update the converter fixture and validation artifacts before the
  required SDK/macOS rebuild and reruns.
- Closure discovery: two initial focused converter attempts exposed the
  bundled ASM 5.2 API and classfile-version constraints. The fixture now uses
  ASM5 and `javac --release 8`; the rerun passed.
- Follow-up validation not yet run: implementation is incomplete. At closure,
  rebuild macOS `tcvm` and rerun correctness, stress, and latency smokes against
  that exact dylib. Windows, Android, Linux, and iOS remain deferred; only SDK
  and macOS are in scope. No image-prefetch performance claim was tested.
- Retained semantic limitation: `acquire()` declares
  `InterruptedException`, but the TotalCross native wait is effectively
  uninterruptible.
- Earlier Part 1 commit-message checks failed on body-line length; those
  commits remain unchanged as required by the plan. New Part 2 commit-message
  checks passed.
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
  `ImageScrollRealWorkloadBenchmarkApp.log`,
  `TotalCrossSDK/IOSDateFixture.tcz`,
  `TotalCrossSDK/ImageScrollRealWorkloadBenchmarkApp.log`,
  `TotalCrossSDK/etc/launchers/`, `TotalCrossVM/xcode/generated/`, and
  `scripts/__pycache__/`.
- Resume: continue the “Wake-Latency Methodology Correction” section in
  `.agent/plans/semaphore-v1-part-2-stress.md`; treat the earlier latency
  aggregate as unconfirmed until all 220 waiter checks pass.
