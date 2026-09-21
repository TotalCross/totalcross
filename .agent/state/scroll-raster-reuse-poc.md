<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Scroll raster reuse POC state

- Active milestone: M0 — bootstrap and plan state.
- Branch: `perf/scroll-raster-reuse-poc`.
- Base branch: `perf/writepixels-tail-diagnosis`.
- Base SHA: `c57c985a9c5e2030ee72a989b5b5917d932103dd`.
- Current HEAD: `c57c985a9c5e2030ee72a989b5b5917d932103dd`.
- Next action: commit this plan, state, and initial evidence as the M0
  documentation checkpoint, then inspect the native raster and SDK scroll paths
  named by the plan before implementing M1.
- Active paths: `.agent/plans/scroll-raster-reuse-poc-plan.md`,
  `.agent/state/scroll-raster-reuse-poc.md`,
  `.agent/evidence/scroll-raster-reuse-poc.md`.
- Focused validation completed: required base branch/ref verified; no build
  run, as required for M0.
- Deferred validation: all SDK/native builds, smoke tests, benchmark runs, and
  final result aggregation until their planned milestones.
- Decisions still active: vertical-only software-raster reuse; default mask
  value zero; unchanged SDL full-frame upload/presentation; no ImageOptimization
  bits enabled.
- Deliberate out-of-scope local files: `.agent/benchmarks/image-scroll-prefetch/**`,
  `.agent/plans/image-optimization-mask-*.md`, `TotalCrossSDK/IOSDateFixture.tcz`,
  `TotalCrossSDK/ImageScrollRealWorkloadBenchmarkApp.log`,
  `TotalCrossSDK/etc/launchers/**`, and `scripts/__pycache__/**`.
- Blockers: none.
- Resume command: read this file, then inspect the active plan's M1 paths and
  the latest evidence record before taking the next action.
