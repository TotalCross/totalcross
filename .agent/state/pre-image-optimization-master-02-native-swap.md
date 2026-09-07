<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Pre-image optimization master 02 native swap state

- Active milestone/slice: Complete — final validation and master/rebase handoff.
- Branch: `fix/pre-image-optimization-master`.
- PLAN1_IMPLEMENTATION_HEAD:
  `8156f62f9cdf41b6d2cd2e18b7ba4b4704ad98b2`.
- PLAN2_BASE: `f918fd4a6ff32c051231657ae58148a040edc6df`.
- Current branch at bootstrap: `f918fd4a6ff32c051231657ae58148a040edc6df`.
- Prerequisite: plan-1 implementation is an ancestor; the only post-
  implementation commit changes the documented plan/state/evidence/report
  handoff paths.
- Last content commit: `94656e8e6` (`docs(image): complete pre-optimization
  master fixes`).
- Final handoff content is committed; the bookkeeping state commit follows.
- Active implementation paths:
  `TotalCrossVM/src/nm/ui/skia/benchmarks/native_swap_benchmark.cpp`,
  `.github/workflows/native-swap-benchmark.yml`.
- Active artifact path:
  `.agent/benchmarks/pre-image-optimization-master/native-swap/`.
- Next exact action: verify the final local HEAD and scoped worktree state;
  then mark the execution goal complete. No further implementation action is
  required.
- Benchmark jobs/results collected: local macOS arm64 and workflow run
  `34070711950` on native Linux x86-64/ARM64 and Windows x86-64/ARM64; all
  60/200 checkpoints for 512x512, 1920x1080, and 3840x2160 are committed under
  `.agent/benchmarks/pre-image-optimization-master/native-swap/`.
- Validation completed for plan 2: prerequisite ancestry and handoff allowlist
  checks; optimized macOS arm64 smoke and 60/200 checkpoints; successful native
  workflow run 34070711950; 30 summary/raw parity checks; focused header
  validation and staged diff checks.
- Validation completed for production cleanup: reconfigured Release macOS
  software-Skia with `TC_BUILD_NATIVE_STARTUP_TEST=ON`, built
  `skia_surface_test`, and ran it successfully. No Windows/Linux TotalCross
  build is permitted here.
- Final audit completed: plan-1 BASE_SHA diff is limited to plan-1 correctness
  work plus native-swap benchmark/evidence, macro cleanup, and handoff
  documentation; workflow is dispatch-only; all staged checks passed.
- Decisions still active: retain `USE_NATIVE_SWAP` and its current defaults due
  to the Windows ARM64/Linux x86-64 conflict; keep `skia_internal.h` as the
  sole default-definition site for surviving flags; leave opacity, writePixels,
  and color-type behavior unchanged.
- Blockers: none.
- Deliberate out-of-scope local files: unrelated untracked repository artifacts
  and generated outputs shown by scoped status; do not stage them.
- Resume command: none; if resumed, verify `git rev-parse HEAD` against the
  final response and leave the branch unmerged and unrebased.
