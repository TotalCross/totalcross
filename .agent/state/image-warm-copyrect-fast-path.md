<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Warm image copyRect execution state

- Plan: `.agent/plans/image-warm-copyrect-fast-path-execplan.md`
- Active milestone: Complete — final transition validated and pushed
- Working tree: `/Users/flsobral/repos/totalcross-image-scroll-raster-fast-path`
- Remote branch after rewrite: `perf/image-scroll-raster-fast-path`
- Last completed checkpoint: `434ca0966` final remote verification
- Current slice: remote verification after normal fast-forward push
- Active paths: `.agent/evidence/.../copyrect-revalidated/`, state, and plan
- Focused validation completed: copyRect full/partial hashes, one first
  fallback materialization and plan, zero cached-repeat plans or physical
  variant stores, destination-scale invalidation, source decode-generation
  invalidation, unchanged drawImage plan execution, and the variant-first →
  final-raster transition with one equivalent eviction and zero
  non-equivalent evictions. Startup defaults, physical-variant creation
  without a final raster, and warm micro lanes pass.
- Evidence: `.agent/evidence/image-warm-copyrect-fast-path.md` and
  `.agent/evidence/image-warm-copyrect-fast-path/{baseline,final,copyrect-revalidated}/`
- Fresh-process matrix: 663 JPEGs, 480x720 and 540x960, four 13/14 profiles,
  and three passes per profile; all 24 records passed. Warm JPEG decodes and
  warm physical-variant stores/bytes were zero. Warm p95 was 9 ms at both
  resolutions. Cold backing live/peak bytes were 940232584 and 1019597208.
- Runtime SHA-256: `32cce8727e61f1f6f1bc06301e62fdd466e507d3a439c513e3b8e00fd251e832`.
- Verified remote head before this slice: `8c9bebd8f`.
- Remote verification: `origin/perf/image-scroll-raster-fast-path` equals
  `434ca0966` after the normal fast-forward pushes.
- Next action: none; the execution plan is complete.
- Deferred validation: full cross-platform release matrix remains outside this
  focused macOS image-path change.
- Recovery: preserve the untracked generated launcher, benchmark log, and
  `IOSDateFixture.tcz`; continue in this feature worktree.
