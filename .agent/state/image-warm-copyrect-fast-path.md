<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Warm image copyRect execution state

- Plan: `.agent/plans/image-warm-copyrect-fast-path-execplan.md`
- Active milestone: Milestone 3 — direct-copy proven physical identity draws
- Working tree: `/private/tmp/image-warm-copyrect-fast-path` (detached at the
  rewritten feature-branch head while the user worktrees remain untouched)
- Remote branch after rewrite: `perf/image-scroll-raster-fast-path`
- Last completed checkpoint: `9809156de` copyRect draw-plan routing commit
- Current slice: direct physical identity copy for deferred raster plans
- Active paths:
- Focused validation completed: Python syntax check, `git diff --check`, SDK
  distribution, smoke compilation, macOS native builds, copyRect draw-plan
  smoke, and the physical-identity benchmark/guard smoke.
- Evidence: `.agent/evidence/image-warm-copyrect-fast-path.md` and
  `.agent/evidence/image-warm-copyrect-fast-path/baseline/`
- Next action: run final real-workload and warm microbenchmark comparisons,
  record evidence, and push the rewritten branch normally.
- Deferred validation: final benchmark matrix and closeout evidence remain.
- Recovery: do not touch untracked files in the shared checkout or the existing
  branch worktree; use this isolated worktree and new evidence directories.
