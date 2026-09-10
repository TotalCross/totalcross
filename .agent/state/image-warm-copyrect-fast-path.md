<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Warm image copyRect execution state

- Plan: `.agent/plans/image-warm-copyrect-fast-path-execplan.md`
- Active milestone: Milestone 1 — startup defaults complete; Milestone 2 next
- Working tree: `/Users/flsobral/repos/totalcross-image-scroll-raster-fast-path`
- Remote branch after rewrite: `perf/image-scroll-raster-fast-path`
- Last completed checkpoint: `fb5d3d045` plan-state commit
- Current slice: initialize optimization masks at real Image startup
- Active paths: `Image.java`, startup smoke, and focused settings validation
- Focused validation completed: Python syntax check, `git diff --check`, SDK
  distribution, focused `ImageOptimizationSettingsTest`, smoke compilation,
  and fresh-process startup-default smoke.
- Evidence: `.agent/evidence/image-warm-copyrect-fast-path.md` and
  `.agent/evidence/image-warm-copyrect-fast-path/{baseline,final}/`
- Verified remote head before this slice: `7c5f633b83690628d10f1d2e07c420638adf6b8f`.
- Next action: commit startup initialization, then add rejection diagnostics
  and measure the 663-JPEG disabled/disabled profile.
- Deferred validation: full cross-platform release matrix remains outside this
  focused macOS image-path change.
- Recovery: preserve the untracked generated launcher, benchmark log, and
  `IOSDateFixture.tcz`; continue in this feature worktree.
