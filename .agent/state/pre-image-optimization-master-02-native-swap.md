<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Pre-image optimization master 02 native swap state

- Active milestone/slice: Complete — final corrective handoff.
- Branch: `fix/pre-image-optimization-master`.
- PLAN1_IMPLEMENTATION_HEAD:
  `8156f62f9cdf41b6d2cd2e18b7ba4b4704ad98b2`.
- PLAN2_BASE: `f918fd4a6ff32c051231657ae58148a040edc6df`.
- Current branch at bootstrap: `f918fd4a6ff32c051231657ae58148a040edc6df`.
- Prerequisite: plan-1 implementation is an ancestor; the only post-
  implementation commit changes the documented plan/state/evidence/report
  handoff paths.
- Last content commit: `557a79f21` (`docs(image): close corrected native swap
  benchmark`).
- Corrected benchmark artifacts, evidence, revised rule, and handoff report are
  committed; this final state update closes the bookkeeping slice.
- Active implementation paths:
  `TotalCrossVM/src/nm/ui/skia/benchmarks/native_swap_benchmark.cpp`,
  `.github/workflows/native-swap-benchmark.yml`.
- Active artifact path:
  `.agent/benchmarks/pre-image-optimization-master/native-swap/`.
- Next exact action: verify the final local HEAD and scoped worktree state.
- Benchmark jobs/results collected: corrected local macOS arm64 and workflow
  run `34072184561` on native Linux x86-64/ARM64 and Windows x86-64/ARM64; all
  60/200 checkpoints for 512x512, 1920x1080, and 3840x2160 are under
  `.agent/benchmarks/pre-image-optimization-master/native-swap/`.
- Validation completed for the correction: direct-loop 3-warmup/10-pair smoke;
  corrected local matrix; successful native workflow run 34072184561; 30
  corrected summary/raw parity checks; no indirect benchmark dispatch remains.
- Validation completed for prior production cleanup: Release macOS
  software-Skia `skia_surface_test` passed. No Windows/Linux TotalCross build
  is permitted here; no new production macro change is required by corrected
  evidence because the existing policy is retained.
- Final audit: after this state commit, verify the final local HEAD and scoped
  worktree state; no further implementation action is required.
- Decisions still active: retain `USE_NATIVE_SWAP` and its current defaults
  under corrected rule 2 because Windows ARM64 has stable wins and no enabled
  Windows/Linux target has a stable regression; keep `skia_internal.h` as the
  sole default-definition site for surviving flags; leave opacity, writePixels,
  and color-type behavior unchanged.
- Blockers: none.
- Deliberate out-of-scope local files: unrelated untracked repository artifacts
  and generated outputs shown by scoped status; do not stage them.
- Resume command: none; if resumed, verify `git rev-parse HEAD` against the
  final response and leave the branch unmerged and unrebased.
