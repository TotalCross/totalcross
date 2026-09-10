<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Warm image copyRect execution state

- Plan: `.agent/plans/image-warm-copyrect-fast-path-execplan.md`
- Active milestone: Milestone 2 — rejection diagnostics
- Working tree: `/Users/flsobral/repos/totalcross-image-scroll-raster-fast-path`
- Remote branch after rewrite: `perf/image-scroll-raster-fast-path`
- Last completed checkpoint: `762b17b2f` startup default mask initialization
- Current slice: expose compact physical-plan rejection reasons and byte totals
- Active paths: `NativeImageBacking.java`, real-workload counters, and benchmark runner
- Focused validation completed: Python syntax check, `git diff --check`, SDK
  distribution, focused `ImageOptimizationSettingsTest`, smoke compilation,
  and fresh-process startup-default smoke.
- Evidence: `.agent/evidence/image-warm-copyrect-fast-path.md` and
  `.agent/evidence/image-warm-copyrect-fast-path/{baseline,final}/`
- Verified remote head before this slice: `7c5f633b83690628d10f1d2e07c420638adf6b8f`.
- Next action: rerun the 663-JPEG workload from a writable `/private/tmp`
  worktree and record the dominant rejection reason.
- Deferred validation: full cross-platform release matrix remains outside this
  focused macOS image-path change.
- Recovery: preserve the untracked generated launcher, benchmark log, and
  `IOSDateFixture.tcz`; continue in this feature worktree.
