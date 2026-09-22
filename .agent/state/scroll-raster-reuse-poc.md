<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Scroll raster reuse POC state

- Active milestone: M2 correction rerun — previous result set invalidated.
- Branch: `perf/scroll-raster-reuse-poc`.
- Base branch: `perf/writepixels-tail-diagnosis`.
- Base SHA: `c57c985a9c5e2030ee72a989b5b5917d932103dd`.
- Current HEAD: `5fcd2ab95` (correction implementation checkpoint).
- Last logical commit: `5fcd2ab95` — measure actual scroll movement frames.
- Next action: rebuild/deploy the current HEAD, rerun M2 OFF/ON correctness,
  then rerun the complete six-process M3 matrix before replacing final results.
- Active paths: `.agent/plans/scroll-raster-reuse-poc-plan.md`,
  `.agent/state/scroll-raster-reuse-poc.md`,
  `.agent/evidence/scroll-raster-reuse-poc.md`.
- Focused validation completed: SDK `classes`, SDK `dist -x test`, smoke Java
  compilation, macOS CMake/Ninja Release build, and the focused native raster
  test passed for the correction implementation.
- Previous M2/M3 results are invalid because the old harness reset repaint
  state, counted diagnostic hits in accounting-off mode, synthesized endpoint
  repaint work, and reported pixel counts as bytes. They must not be reused.
- Commit-message validation: the first two correction commits are signed and
  conventional, but their body-line checks reported overlong lines. No history
  rewrite will be performed; remaining commits use wrapped body paragraphs.
- Deferred validation: corrected macOS M2 and the six-process M3 matrix.
- Decisions still active: vertical-only software-raster reuse; default mask
  value zero; unchanged SDL full-frame upload/presentation; no ImageOptimization
  bits enabled.
- Deliberate out-of-scope local files: `.agent/benchmarks/image-scroll-prefetch/**`,
  `.agent/plans/image-optimization-mask-*.md`, `TotalCrossSDK/IOSDateFixture.tcz`,
  `TotalCrossSDK/ImageScrollRealWorkloadBenchmarkApp.log`,
  `TotalCrossSDK/etc/launchers/**`, and `scripts/__pycache__/**`.
- Blockers: none after removing only stale generated package artifacts and
  increasing the TCZ deploy child heap for the large benchmark staging jar.
- Resume command: read this file, then inspect the active plan's M1 paths and
  the latest evidence record before taking the next action.
