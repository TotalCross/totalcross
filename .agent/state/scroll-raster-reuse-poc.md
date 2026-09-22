<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Scroll raster reuse POC state

- Active milestone: STOP / REVIEW — all planned work complete.
- Branch: `perf/scroll-raster-reuse-poc`.
- Base branch: `perf/writepixels-tail-diagnosis`.
- Base SHA: `c57c985a9c5e2030ee72a989b5b5917d932103dd`.
- Current HEAD: `99929d06f`.
- Last logical commit: `99929d06f` — complete the scroll reuse repaint path.
- Next action: commit the final evidence/report artifacts, then refresh this
  state with the resulting documentation commit hash.
- Active paths: `.agent/plans/scroll-raster-reuse-poc-plan.md`,
  `.agent/state/scroll-raster-reuse-poc.md`,
  `.agent/evidence/scroll-raster-reuse-poc.md`.
- Focused validation completed: SDK `dist` passed after the dirty-background
  fix; `compileSmokeTestJava`, changed-file copyright validation, and staged
  diff checks passed; macOS CMake/Ninja Release build and focused native raster
  tests passed; the corrected M2 macOS OFF/ON pair matched every cold/warm
  waypoint hash with 71/71 ON hits and zero recoveries.
- Commit-message validation: the current commit was created signed, but the
  local checker found a literal escaped-newline sequence as one overlong body
  line; no history rewrite will be performed.
- Deferred validation: none; the six-process performance matrix and final
  result aggregation are complete.
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
