<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Semaphore v1 execution state

- Part/milestone/slice: 1 / 1 complete; Part 2 is next.
- Branch: `feat/semaphore-v1`.
- Planning base: `0aeea1029f24a7e3e4f29c8f1f339ffad0a141f2`.
- Last logical commit: `b3a4a0e565be1232e3d67d3a24146938db989537`
  (`fix(vm): add semaphore prototypes to generated index`).
- Active paths: `.agent/plans/semaphore-v1-part-1-core.md`, this file,
  `.agent/evidence/semaphore-v1.jsonl`,
  `TotalCrossSDK/src/main/java/jdkcompat/util/concurrent/Semaphore4D.java`,
  `TotalCrossSDK/src/smokeTest/java/totalcross/util/concurrent/`,
  `TotalCrossSDK/build.gradle`, the converter test, and `build-semaphore/`.
- Next action: continue with `.agent/plans/semaphore-v1-part-2-stress.md` only
  when that part is explicitly requested.
- Validation completed: converter test passed 2/2; generator matched 6 native
  prototypes and registrations; SDK dist and smoke compilation passed; CMake
  configure and macOS `tcvm` build passed (123 Ninja steps); deployed smoke
  passed with `fixture=SemaphoreSmokeApp,overallPass=true`. Focused header,
  diff, and later commit-message checks passed. The first converter run failed
  on missing prototypes and passed after the focused fix. Two earlier
  commit-message checks failed because body lines exceeded 80 characters; those
  commits remain unchanged per plan.
- Validation deferred: Windows, Android, Linux, and iOS native builds/runs;
  Part 2 stress coverage.
- Blockers: none.
- Unrelated dirty paths to preserve: `totalcross.code-workspace`,
  `.agent/benchmarks/image-scroll-prefetch/final-definitive-pass/`,
  `.agent/benchmarks/image-scroll-prefetch/final-fixed/`,
  `.agent/benchmarks/scroll-raster-reuse-poc/m2/`,
  `.agent/plans/image-optimization-mask-01-diagnose.md`,
  `.agent/plans/image-optimization-mask-02-fix.md`,
  `.agent/plans/image-scroll-prefetch-reduction.md`,
  `.agent/plans/prefetch-thread-diagnostics-execplan.md`,
  `.agent/plans/semaphore-v1-part-2-stress.md`,
  `.agent/state/image-scroll-prefetch-reduction.md`,
  `ImageScrollRasterFastPathBenchmarkApp.log`,
  `ImageScrollRealWorkloadBenchmarkApp.log`,
  `TotalCrossSDK/IOSDateFixture.tcz`,
  `TotalCrossSDK/ImageScrollRealWorkloadBenchmarkApp.log`,
  `TotalCrossSDK/etc/launchers/`, `TotalCrossVM/xcode/generated/`, and
  `scripts/__pycache__/`.
- Resume command: read this file, then open
  `.agent/plans/semaphore-v1-part-2-stress.md` if Part 2 is requested.
