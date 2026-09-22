<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Scroll raster reuse POC state

- Active milestone: `STOP / REVIEW` — correction rerun complete.
- Branch: `perf/scroll-raster-reuse-poc`.
- Base branch: `perf/writepixels-tail-diagnosis`.
- Base SHA: `c57c985a9c5e2030ee72a989b5b5917d932103dd`.
- Current source/results HEAD: `d9930c603` (correction implementation
  checkpoint; final evidence records this measurement revision).
- Last logical implementation commit: `d9930c603` — accept disabled fallback
  sentinel.
- Next action: none; stop for review.
- Active paths: `.agent/plans/scroll-raster-reuse-poc-plan.md`,
  `.agent/state/scroll-raster-reuse-poc.md`,
  `.agent/evidence/scroll-raster-reuse-poc.md`.
- Focused validation completed: SDK `classes`, SDK `dist -x test`, smoke Java
  compilation, macOS CMake/Ninja Release build, focused native raster test,
  current-HEAD M2 OFF/ON correctness, and the complete six-process M3 matrix.
- Previous M2/M3 results are invalid because the old harness reset repaint
  state, counted diagnostic hits in accounting-off mode, synthesized endpoint
  repaint work, and reported pixel counts as bytes. They must not be reused.
- Commit-message validation: the first two correction commits are signed and
  conventional, but their body-line checks reported overlong lines. No history
  rewrite will be performed; remaining commits use wrapped body paragraphs.
- Corrected M2: OFF/ON waypoint hashes matched; ON recorded 71/71 local hits,
  zero fallbacks and recoveries, and four-byte BGRA8888 moved-byte accounting.
- Corrected M3: three OFF and three ON processes passed with 75 trace rows and
  71 measured movement frames per pass; accounting and rendering diagnostics
  were disabled, while local outcome metrics remained valid.
- Classification: `PRESENTATION-BOUND`; final artifacts are under
  `.agent/benchmarks/scroll-raster-reuse-poc/final/`.
- Final evidence commit: `ac0abff74` — record corrected reuse results.
- Final review status: `STOP / REVIEW`; no follow-up optimization is in scope.
- Deferred validation: none within this plan.
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
