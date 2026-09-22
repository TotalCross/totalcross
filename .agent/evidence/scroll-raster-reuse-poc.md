<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Scroll raster reuse POC evidence

## 2026-09-21 — M0 bootstrap

- revision: `c57c985a9c5e2030ee72a989b5b5917d932103dd`
- branch: `perf/scroll-raster-reuse-poc`
- base: `perf/writepixels-tail-diagnosis`
- status: verified
- command: `git switch -c perf/scroll-raster-reuse-poc perf/writepixels-tail-diagnosis`
- result: target branch created from the exact required base; no build run.
- scope: unrelated local artifacts preserved and excluded from the bootstrap
  commit.

## 2026-09-21 — M0 documentation checkpoint

- revision: `b91ab518ead60beba1002f60edb94f9d389086ef`
- status: committed
- paths: plan, state, and initial evidence only
- validation: focused copyright-header validation and staged diff check passed;
  the local commit-message checker reported one body line over 80 characters.
- limitation: no history rewrite; later commits use wrapped body lines.

## 2026-09-21 — M1 implementation and focused gates

- revisions: `6b94959c9257d7ac760976cc0406e2551d6906aa`,
  `f221006feeef1aa78a12ac5a361c423a08317784`
- status: implementation committed
- native: software raster move/hash primitive, Java bridge, generated native
  registrations, and BGRA8888/RGB565 overlap/bounds tests.
- SDK: rendering mask, conservative eligibility/fallbacks, dirty-strip paint,
  screen-update timing, and diagnostics committed.
- validation: `./gradlew-agent dist` passed; CMake/Ninja Release build passed;
  `skia_surface_test` passed; changed-file headers and diff checks passed.
- note: the profile run below is the runtime ON-hit proof required before
  closing the M1/M2 correctness gate.

## 2026-09-21 — M2 correctness harness

- revision: `a11ced0d5949cf48e4e367f9217aa05b8a85e522`
- status: harness committed; matrix not yet executed
- profile: existing real-corpus runner, first 120 sorted JPEGs, two cold/warm
  passes, waypoints `0 -> 7V -> 4V -> 5V -> 4.5V`, segment durations
  `500/250/200/150 ms`, per-frame paint counters, and viewport hashes.
- validation: smoke Java compilation passed after rebuilding the SDK; native
  surface test passed after the shortened native hash name fix.
- blocker: macOS deploy retried twice and failed with `No space left on device`
  during TCZ generation. The host data volume reported about 202 MiB free;
  no benchmark result is claimed.

## 2026-09-21 — M2 correctness gate

- revision: `99929d06f`
- status: PASS
- workload: 120 sorted JPEGs, three columns, 540x960 macOS software raster,
  image optimization mask 0, prefetch ON, two cold/warm passes.
- correctness: OFF/ON hashes matched at all five waypoints in both passes;
  ON recorded 71/71 hits, zero fallbacks, and zero post-move recoveries;
  OFF recorded zero hits.
- accounting: `attempts = hits + fallbacks`; ON reused 117126000 pixels and
  repainted 22431600 of 139557600 viewport pixels, moving 117126000 bytes.
- fix: explicit dirty-strip fills were required because equal-color parent
  backgrounds intentionally skip normal `onPaint` fills; row-selective nested
  repaint then matched the normal repaint hashes.
- validation: SDK dist, smoke Java compilation, native Release build/test,
  focused header validation, and diff checks passed.

## 2026-09-21 — M3 performance matrix

- revision: `99929d06f`; accounting OFF; three independent OFF and three
  independent ON processes, each with cold and warm passes.
- status: PASS; all six processes completed and all waypoint hashes remained
  stable across rounds.
- performance: combined 225-frame medians improved from 4.079 ms to 2.681 ms
  cold and 4.022 ms to 2.634 ms warm for measured work; average row/image
  paints fell from 6.013/18.040 to 1.996/5.987 cold and from 6.018/18.053 to
  2.004/6.013 warm.
- presentation: screen-update P50 remained approximately 1.5 ms, and paced
  pass totals did not improve materially; classify `PRESENTATION-BOUND`.
- artifacts: compact summaries, waypoint hashes, frame distributions, and
  top-frame evidence are in
  `.agent/benchmarks/scroll-raster-reuse-poc/final/`.
