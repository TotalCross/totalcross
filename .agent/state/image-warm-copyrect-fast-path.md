<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Warm image copyRect execution state

- Plan: `.agent/plans/image-warm-copyrect-fast-path-execplan.md`
- Active milestone: Milestone 3 — complete
- Working tree: `/private/tmp/image-warm-copyrect-fast-path` (detached at the
  rewritten feature-branch head while the user worktrees remain untouched)
- Remote branch after rewrite: `perf/image-scroll-raster-fast-path`
- Last completed checkpoint: `da2c0ba94` copyRect fallback-preservation fix
- Current slice: complete
- Active paths: final matrix, final warm microbenchmark, and evidence index
- Focused validation completed: Python syntax check, `git diff --check`, SDK
  distribution, smoke compilation, macOS native builds, copyRect draw-plan
  smoke, physical-identity benchmark/guard smoke, final workload matrix, and
  final warm microbenchmark.
- Evidence: `.agent/evidence/image-warm-copyrect-fast-path.md` and
  `.agent/evidence/image-warm-copyrect-fast-path/{baseline,final}/`
- Verified remote head: `7b3e0129aa7c18320b4102ea623bc22c9202f5a9` on
  `perf/image-scroll-raster-fast-path`.
- Next action: none; plan execution is complete.
- Deferred validation: full cross-platform release matrix remains outside this
  focused macOS image-path change.
- Recovery: do not touch untracked files in the shared checkout or the existing
  branch worktree; use this isolated worktree and new evidence directories.
