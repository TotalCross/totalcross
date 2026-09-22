<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Scroll raster reuse POC state

- Active milestone: `STOP / REVIEW` — final timer-gating M3 rerun complete.
- Branch: `perf/scroll-raster-reuse-poc`.
- Base branch: `perf/writepixels-tail-diagnosis`.
- Base SHA: `c6cc3bcbf9e28ead3edabb69c12e5c31926a55d0`.
- Current source HEAD: `f7e662508` (diagnostic timer-gating correction).
- Last logical implementation commit: `f7e662508` — gate scroll reuse
  diagnostic timers.
- Next action: none; stop for review. M2 was not rerun.
- Active paths: `.agent/plans/scroll-raster-reuse-poc-plan.md`,
  `.agent/state/scroll-raster-reuse-poc.md`,
  `.agent/evidence/scroll-raster-reuse-poc.md`.
- Focused validation completed: SDK `classes`, SDK `dist -x test`, smoke Java
  compilation, source timer-gating audit, and the complete six-process M3
  matrix. Prior macOS native and M2 gates remain retained.
- The prior M2 correctness result remains retained; only the prior M3
  performance set is invalidated for this timer-gating correction.
- Prior correction issues were fixed: repaint state, endpoint filtering,
  native byte accounting, diagnostics-off local hit measurement, and overlay
  fallback. They are not reopened by this timer-only change.
- Commit-message validation: the first two correction commits are signed and
  conventional, but their body-line checks reported overlong lines. No history
  rewrite will be performed; remaining commits use wrapped body paragraphs.
- Corrected M2: OFF/ON waypoint hashes matched; ON recorded 71/71 local hits,
  zero fallbacks and recoveries, and four-byte BGRA8888 moved-byte accounting.
- Prior corrected M3 checkpoint: three OFF and three ON processes passed with
  75 trace rows and 71 measured movement frames per pass; it is superseded by
  the timer-gating rerun.
- Final M3: three OFF and three ON processes passed with 150 trace rows and
  142 measured movement frames per pass; accounting and rendering diagnostics
  were disabled, all diagnostic timer deltas were zero, and local outcome
  metrics remained valid.
- Prior classification: `PRESENTATION-BOUND` (invalidated pending rerun);
  final artifacts are under `.agent/benchmarks/scroll-raster-reuse-poc/final/`.
- Previous final evidence commit: `ac0abff74` — invalidated M3 results.
- Final evidence commit: `8d086003f` — record timer-gated M3 results.
- Final classification: `PRESENTATION-BOUND`; fresh artifacts are under
  `.agent/benchmarks/scroll-raster-reuse-poc/final/`.
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
