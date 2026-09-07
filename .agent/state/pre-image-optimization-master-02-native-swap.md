<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Pre-image optimization master 02 native swap state

- Active milestone/slice: Milestone 0 — benchmark infrastructure.
- Branch: `fix/pre-image-optimization-master`.
- PLAN1_IMPLEMENTATION_HEAD:
  `8156f62f9cdf41b6d2cd2e18b7ba4b4704ad98b2`.
- PLAN2_BASE: `f918fd4a6ff32c051231657ae58148a040edc6df`.
- Current branch at bootstrap: `f918fd4a6ff32c051231657ae58148a040edc6df`.
- Prerequisite: plan-1 implementation is an ancestor; the only post-
  implementation commit changes the documented plan/state/evidence/report
  handoff paths.
- Last commit: none for plan 2 yet; infrastructure is the active slice.
- Active implementation paths:
  `TotalCrossVM/src/nm/ui/skia/benchmarks/native_swap_benchmark.cpp`,
  `.github/workflows/native-swap-benchmark.yml`.
- Active artifact path:
  `.agent/benchmarks/pre-image-optimization-master/native-swap/`.
- Next exact action: compile and run the required local macOS smoke with three
  warmups and ten paired samples for one representative size, validate raw CSV
  and summary JSON, then run focused header validation and staged diff checks
  before the infrastructure commit.
- Benchmark jobs/results collected: none.
- Validation completed for plan 2: prerequisite ancestry and handoff allowlist
  checks passed; benchmark smoke and header validation remain pending.
- Validation deferred: final local macOS 60/200 checkpoints and all native
  Windows/Linux runner jobs are deferred until the infrastructure commit and
  branch-triggered workflow are available.
- Decisions still active: apply only the fixed 5% rule; keep
  `skia_internal.h` as the sole default-definition site for surviving flags;
  leave opacity, writePixels, and color-type behavior unchanged.
- Blockers: none.
- Deliberate out-of-scope local files: unrelated untracked repository artifacts
  and generated outputs shown by scoped status; do not stage them.
- Resume command: read this state, inspect the active benchmark paths, then
  perform the next exact action above.
