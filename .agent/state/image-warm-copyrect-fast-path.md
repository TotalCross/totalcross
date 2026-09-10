<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Warm image copyRect execution state

- Plan: `.agent/plans/image-warm-copyrect-fast-path-execplan.md`
- Active milestone: Milestone 5 — evidence closeout and push
- Working tree: `/Users/flsobral/repos/totalcross-image-scroll-raster-fast-path`
- Remote branch after rewrite: `perf/image-scroll-raster-fast-path`
- Last completed checkpoint: `0366e909f` independent warm raster lanes
- Current slice: final fresh-process matrix and bounded evidence/report artifacts
- Active paths: `.agent/evidence/.../final-revalidated/`, editorial report, and plan outcome
- Focused validation completed: final matrix across 663 JPEGs, two resolutions,
  four 13/14 profiles, and three passes per profile; warm p95 is 9 ms in every
  profile, warm JPEG decodes are zero, and variant-enabled warm writes are
  4509/5898 hits. Startup, physical-variant, and warm-micro smokes also pass.
- Evidence: `.agent/evidence/image-warm-copyrect-fast-path.md` and
  `.agent/evidence/image-warm-copyrect-fast-path/{baseline,final}/`
- Verified remote head before this slice: `7c5f633b83690628d10f1d2e07c420638adf6b8f`.
- Diagnostic checkpoint: fresh-process 663-JPEG matrix completed at 480x720 and
  540x960; mapping geometry is the dominant rejection (`4509` and `5898` per
  pass), with backing rejection zero. Feature-14 warm p95 remained near the
  disabled baseline after the direct-copy fix is still to be remeasured.
- Next action: stage the bounded evidence/report/plan/state closeout, validate
  the final diff, push normally, and verify the remote head.
- Deferred validation: full cross-platform release matrix remains outside this
  focused macOS image-path change.
- Recovery: preserve the untracked generated launcher, benchmark log, and
  `IOSDateFixture.tcz`; continue in this feature worktree.
