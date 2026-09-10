<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Warm image copyRect execution state

- Plan: `.agent/plans/image-warm-copyrect-fast-path-execplan.md`
- Active milestone: Milestone 3 — physical variant direct-copy path
- Working tree: `/Users/flsobral/repos/totalcross-image-scroll-raster-fast-path`
- Remote branch after rewrite: `perf/image-scroll-raster-fast-path`
- Last completed checkpoint: `965243c87` corrected packed rejection-lane decoding
- Current slice: direct-copy exact physical variant hits with cached opacity proof
- Active paths: Skia raster variant backing/geometry/materialization and physical-variant smoke
- Focused validation completed: macOS native `tcvm` build, SDK distribution,
  physical-variant repeat smoke with `write_pixels_hits=1`, and alpha/hwscale/
  rotation/combined fallback guards.
- Evidence: `.agent/evidence/image-warm-copyrect-fast-path.md` and
  `.agent/evidence/image-warm-copyrect-fast-path/{baseline,final}/`
- Verified remote head before this slice: `7c5f633b83690628d10f1d2e07c420638adf6b8f`.
- Diagnostic checkpoint: fresh-process 663-JPEG matrix completed at 480x720 and
  540x960; mapping geometry is the dominant rejection (`4509` and `5898` per
  pass), with backing rejection zero. Feature-14 warm p95 remained near the
  disabled baseline after the direct-copy fix is still to be remeasured.
- Next action: commit this direct-copy slice, then correct and run the warm
  micro benchmark with independent draw/copy lanes and an identity lane.
- Deferred validation: full cross-platform release matrix remains outside this
  focused macOS image-path change.
- Recovery: preserve the untracked generated launcher, benchmark log, and
  `IOSDateFixture.tcz`; continue in this feature worktree.
