<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Semaphore v1 execution state

- Part/milestone/slice: Part 1 / Milestone 1 complete; Part 2 / Slice 2D active.
- Branch: `feat/semaphore-v1`.
- Planning base: `0aeea1029f24a7e3e4f29c8f1f339ffad0a141f2`.
- Part 1 final milestone commit: `daef7cee82370feb9995ad410dfb82dd25e190c5`
  (`docs(plan): record semaphore core milestone`). The final Part 1 correctness
  smoke passed against `build-semaphore/libtcvm.dylib`.
- Latest Part 1 functional commit: `b3a4a0e565be1232e3d67d3a24146938db989537`
  (`fix(vm): add semaphore prototypes to generated index`).
- Part 2 plan checkpoint: `694e8c35e` tracks the plan and activates Part 2.
- Active paths: `.agent/plans/semaphore-v1-part-2-stress.md`, this file,
  `.agent/evidence/semaphore-v1.jsonl`,
  `.agent/reports/semaphore-v1-editorial.md`,
  `TotalCrossSDK/src/main/java/jdkcompat/util/concurrent/Semaphore4D.java`,
  `TotalCrossSDK/src/smokeTest/java/totalcross/util/concurrent/`,
  `TotalCrossSDK/build.gradle`, the converter test, and
  `build-semaphore/libtcvm.dylib`.
- Latest Part 2 logical commit: `694e8c35e`
  (`docs(plan): define semaphore stress validation plan`).
- Slice 2B added source-compiled standard-API resolution coverage for all
  supported calls and the four unsupported overloads. Per plan, execution is
  deferred until milestone closure.
- Latest Part 2 logical commit: `3689a905b`
  (`test(converter): cover semaphore v1 compatibility surface`).
- Slice 2C added a deterministic 20,000-handoff smoke with four producers,
  four consumers, Semaphore start/readiness/completion handshakes, exact count
  reconciliation, and a 60-second native-process timeout.
- Next action: add the sequential 20-warm-up/200-sample release-to-acquire
  wake-latency smoke and aggregate output.
- Validation completed: converter test passed 2/2; generator matched 6 native
  prototypes and registrations; SDK dist and smoke compilation passed; CMake
  configure and macOS `tcvm` build passed (123 Ninja steps); deployed smoke
  passed with `fixture=SemaphoreSmokeApp,overallPass=true`. Focused header,
  diff, and later commit-message checks passed. The first converter run failed
  on missing prototypes and passed after the focused fix. Two earlier
  commit-message checks failed because body lines exceeded 80 characters; those
  commits remain unchanged per plan.
- Validation deferred: focused Part 2 converter test, SDK build tasks, native
  macOS correctness/stress/latency runs until milestone closure; Windows,
  Android, Linux, and iOS native builds/runs remain out of scope.
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
- Resume command: read this file, then continue
  `.agent/plans/semaphore-v1-part-2-stress.md` from Slice 2B.
