<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Warm image copyRect execution state

- Plan: `.agent/plans/image-warm-copyrect-fast-path-execplan.md`
- Active milestone: Milestone 4 — warm micro and workload revalidation
- Working tree: `/Users/flsobral/repos/totalcross-image-scroll-raster-fast-path`
- Remote branch after rewrite: `perf/image-scroll-raster-fast-path`
- Last completed checkpoint: `55667d635` direct-copy physical raster variants
- Current slice: independent warm micro lanes and exact identity accounting
- Active paths: `ImageWarmCopyRectBenchmarkApp` and its macOS smoke registration
- Focused validation completed: corrected warm micro with independent materialized,
  deferred draw/copy, and identity-direct lanes; full/partial hashes match, and
  identity-direct records `physical_identity_hits=2560`, `write_pixels_hits=2560`,
  `generic_geometry_draws=0`, `smooth_resample_draws=0`.
- Evidence: `.agent/evidence/image-warm-copyrect-fast-path.md` and
  `.agent/evidence/image-warm-copyrect-fast-path/{baseline,final}/`
- Verified remote head before this slice: `7c5f633b83690628d10f1d2e07c420638adf6b8f`.
- Diagnostic checkpoint: fresh-process 663-JPEG matrix completed at 480x720 and
  540x960; mapping geometry is the dominant rejection (`4509` and `5898` per
  pass), with backing rejection zero. Feature-14 warm p95 remained near the
  disabled baseline after the direct-copy fix is still to be remeasured.
- Next action: commit the warm micro correction, then rerun the final fresh-process
  13/14 matrix and collect bounded evidence/report artifacts.
- Deferred validation: full cross-platform release matrix remains outside this
  focused macOS image-path change.
- Recovery: preserve the untracked generated launcher, benchmark log, and
  `IOSDateFixture.tcz`; continue in this feature worktree.
