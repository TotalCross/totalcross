<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Scroll raster reuse POC state

- Active milestone: M2 — reduced realistic benchmark and correctness gate.
- Branch: `perf/scroll-raster-reuse-poc`.
- Base branch: `perf/writepixels-tail-diagnosis`.
- Base SHA: `c57c985a9c5e2030ee72a989b5b5917d932103dd`.
- Current HEAD: `a11ced0d5949cf48e4e367f9217aa05b8a85e522`.
- Last logical commit: `a11ced0d5949cf48e4e367f9217aa05b8a85e522` — add the
  reduced scroll-raster-reuse correctness profile.
- Next action: free or obtain host disk space, deploy the updated macOS smoke
  app, then run the OFF and ON two-pass waypoint matrix.
- Active paths: `.agent/plans/scroll-raster-reuse-poc-plan.md`,
  `.agent/state/scroll-raster-reuse-poc.md`,
  `.agent/evidence/scroll-raster-reuse-poc.md`.
- Focused validation completed: SDK `dist` passed after the implementation and
  profile changes; macOS CMake/Ninja Release build passed; the focused native
  raster test passed for BGRA8888/RGB565, overlap, bounds, and hash cases;
  smoke Java compilation and changed-file copyright validation passed; staged
  diff checks passed.
- Commit-message validation: the bootstrap commit was created, but the local
  checker reported one body line over 80 characters; no history rewrite will be
  performed, and subsequent commit bodies will be wrapped.
- Deferred validation: macOS deploy/run of the new profile, OFF/ON waypoint
  hash comparison, the six-process performance matrix, and final aggregation.
- Decisions still active: vertical-only software-raster reuse; default mask
  value zero; unchanged SDL full-frame upload/presentation; no ImageOptimization
  bits enabled.
- Deliberate out-of-scope local files: `.agent/benchmarks/image-scroll-prefetch/**`,
  `.agent/plans/image-optimization-mask-*.md`, `TotalCrossSDK/IOSDateFixture.tcz`,
  `TotalCrossSDK/ImageScrollRealWorkloadBenchmarkApp.log`,
  `TotalCrossSDK/etc/launchers/**`, and `scripts/__pycache__/**`.
- Blockers: the host data volume has approximately 202 MiB free; TCZ deployment
  fails with `No space left on device` while generating the updated application
  artifact. This is an environment gate, not a source failure.
- Resume command: read this file, then inspect the active plan's M1 paths and
  the latest evidence record before taking the next action.
