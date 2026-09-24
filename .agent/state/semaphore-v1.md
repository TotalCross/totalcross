<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Semaphore v1 execution state

- Part/milestone/slice: 1 / 1 / 1A.
- Branch: `feat/semaphore-v1`.
- Planning base: `0aeea1029f24a7e3e4f29c8f1f339ffad0a141f2`.
- Last logical commit: none; checkpoint 0 artifacts are being prepared.
- Active paths: `.agent/plans/semaphore-v1-part-1-core.md`, this file,
  `.agent/evidence/semaphore-v1.jsonl`, and `TotalCrossVM/src/tcvm/tcthread.h`.
- Next action: commit the checkpoint 0 plan, state, and planning evidence; then
  add only the internal blocking condition abstraction to `tcthread.h`.
- Validation completed: base SHA matched; branch created; no build run.
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
