<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Semaphore v1 execution state

- Part/milestone/slice: 1 / 1 / closure validation.
- Branch: `feat/semaphore-v1`.
- Planning base: `0aeea1029f24a7e3e4f29c8f1f339ffad0a141f2`.
- Last logical commit: `38f86debe33082842dfa5d22a1d3254323f35e59`
  (`feat(sdk): add semaphore v1 compatibility surface`).
- Active paths: `.agent/plans/semaphore-v1-part-1-core.md`, this file,
  `.agent/evidence/semaphore-v1.jsonl`,
  `TotalCrossSDK/src/main/java/jdkcompat/util/concurrent/Semaphore4D.java`,
  `TotalCrossSDK/src/smokeTest/java/totalcross/util/concurrent/`,
  `TotalCrossSDK/build.gradle`, the converter test, and `build-semaphore/`.
- Next action: commit the six generated prototype declarations with the failure
  evidence, then rerun the converter test before continuing closure.
- Validation completed: base SHA matched; branch created; plan/state/evidence
  header validation and diff check passed; Slice 1A header validation and diff
  check passed. Both earlier commit-message checks found body lines over 80
  characters; earlier commits remain unchanged per plan. Slice 1B header and
  diff checks plus commit-message validation passed. Slice 1C header, staged
  diff, and commit-message checks passed. The first converter-test run failed:
  the mapping test passed, but generated prototypes were missing. SDK and native
  builds have not started.
- Validation deferred: SDK and native macOS builds until Milestone 1 closure;
  non-macOS native builds are deferred by plan policy.
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
- Resume command: read this file, then continue at the next action above.
