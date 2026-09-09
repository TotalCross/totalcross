<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Warm image copyRect execution state

- Plan: `.agent/plans/image-warm-copyrect-fast-path-execplan.md`
- Active milestone: Milestone 1 — central effective defaults
- Working tree: `/private/tmp/image-warm-copyrect-fast-path` (detached at the
  rewritten feature-branch head while the user worktrees remain untouched)
- Remote branch after rewrite: `perf/image-scroll-raster-fast-path`
- Last completed checkpoint: baseline evidence commit after harness revision
- Current slice: centralize `DEFAULT` resolution for the six requested features
- Active paths:
- Focused validation completed: Python syntax check, `git diff --check`, SDK
  distribution, smoke compilation, macOS native build, both benchmark
  deployments, the eight-case baseline matrix, and the warm microbenchmark.
- Evidence: `.agent/evidence/image-warm-copyrect-fast-path.md` and
  `.agent/evidence/image-warm-copyrect-fast-path/baseline/`
- Next action: implement the central default policy, update settings tests and
  native mask synchronization, then run focused SDK tests.
- Deferred validation: copyRect/native plan routing and direct-copy smoke are
  deferred to their related milestones.
- Recovery: do not touch untracked files in the shared checkout or the existing
  branch worktree; use this isolated worktree and new evidence directories.
