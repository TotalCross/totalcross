<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Warm image copyRect execution state

- Plan: `.agent/plans/image-warm-copyrect-fast-path-execplan.md`
- Active milestone: Complete — evidence closeout pushed
- Working tree: `/Users/flsobral/repos/totalcross-image-scroll-raster-fast-path`
- Remote branch after rewrite: `perf/image-scroll-raster-fast-path`
- Last completed checkpoint: `d55be3907` final evidence and state closeout
- Current slice: remote verification after normal push
- Active paths: `.agent/evidence/.../copyrect-revalidated/`, state, and plan
- Focused validation completed: copyRect full/partial hashes, one first
  fallback materialization and plan, zero cached-repeat plans or physical
  variant stores, destination-scale invalidation, source decode-generation
  invalidation, and unchanged drawImage plan execution. Startup defaults,
  physical-variant creation without a final raster, and warm micro lanes pass.
- Evidence: `.agent/evidence/image-warm-copyrect-fast-path.md` and
  `.agent/evidence/image-warm-copyrect-fast-path/{baseline,final,copyrect-revalidated}/`
- Fresh-process matrix: 663 JPEGs, 480x720 and 540x960, four 13/14 profiles,
  and three passes per profile; all 24 records passed. Warm JPEG decodes and
  warm physical-variant stores/bytes were zero. Warm p95 was 9 ms at 480x720
  and 9–13 ms at 540x960. Cold backing live/peak bytes were 940232584 and
  1019597208.
- Runtime SHA-256: `efef5fb8b062df88054daa7c2e4aeeff98b1b1dd1c2ea49e00aad48ce188a61a`.
- Verified remote head before this slice:
  `72a1ee599cd538c3343f8b6ed5292eba968e9246`.
- Remote verification: `origin/perf/image-scroll-raster-fast-path` equals
  `d55be3907` after normal fast-forward pushes of code and evidence.
- Next action: none; the execution plan is complete.
- Deferred validation: full cross-platform release matrix remains outside this
  focused macOS image-path change.
- Recovery: preserve the untracked generated launcher, benchmark log, and
  `IOSDateFixture.tcz`; continue in this feature worktree.
