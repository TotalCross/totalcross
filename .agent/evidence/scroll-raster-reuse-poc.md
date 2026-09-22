<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Scroll raster reuse POC evidence

## 2026-09-21 — M0 bootstrap (pre-rebase history)

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
- results commit: `4acd53808`.

## 2026-09-21 — correction rerun invalidation

- current implementation checkpoints: `83ac08834`, `b2ea8e2fc`, `5fcd2ab95`.
- status: prior M2/M3 result set invalidated.
- reason: the prior harness manually cleared `Window.needsPaint`, used
  rendering diagnostics to identify hits while accounting was off, included
  no-movement endpoint samples in work measurements, and treated pixel pitch
  as bytes. The fast path also lacked direct bag0 overlay rejection.
- required replacement: rebuild current HEAD, rerun the M2 OFF/ON correctness
  pair, rerun exactly three OFF and three ON M3 processes with accounting and
  rendering diagnostics disabled, and regenerate all final distributions.

## 2026-09-22 — timer-gating rerun invalidation

- current base: `perf/writepixels-tail-diagnosis` at
  `c6cc3bcbf9e28ead3edabb69c12e5c31926a55d0`.
- current source revision: `f7e662508` — diagnostic decision, raster-move,
  and dirty-paint clock reads are now conditional on diagnostics being enabled.
- status: the prior M3 performance set from `d9930c603` is invalidated and
  must not be used for final classification. M2 correctness remains retained
  and is not rerun.
- required replacement: rebuild only the affected SDK/macOS targets, run
  exactly three OFF and three ON M3 processes with image accounting and
  rendering diagnostics disabled, and regenerate all final artifacts from
  those fresh movement frames.

## 2026-09-22 — final timer-gating M3 rerun

- source/results revision: `f7e662508`; base:
  `perf/writepixels-tail-diagnosis@c6cc3bcbf9e28ead3edabb69c12e5c31926a55d0`.
- build gate: SDK `dist -x test` and smoke compilation passed. The affected
  Java SDK/macOS package was rebuilt without a clean; the existing native
  macOS runtime was reused because the correction is SDK-only.
- matrix: exactly three OFF and three ON independent processes completed with
  accounting and rendering diagnostics OFF, 540x960 software BGRA8888, and
  150 trace rows per pass including 142 movement rows. Endpoint rows were
  excluded from all timing distributions.
- correctness: all six processes matched every waypoint hash. ON recorded
  71/71 local hits per pass, zero fallbacks and recoveries; OFF recorded zero
  hits. Reuse coverage was `0.839266`; moved bytes were `468504000` per ON
  pass using four bytes per pixel.
- timer gate: source audit found all nine `tryRasterReuse` diagnostic clock
  reads conditional on diagnostics; all six runtime traces reported zero
  decision, move, and dirty-paint diagnostic timer deltas.
- performance: aggregate work P50/P95/P99/MAX was
  `4.096/4.580/4.668/4.746 ms` OFF versus
  `2.857/7.267/8.133/8.362 ms` ON cold, and
  `4.031/4.565/4.852/5.053 ms` OFF versus
  `2.664/6.610/7.742/9.514 ms` ON warm. Paint reduction was 70.38% cold
  and 70.60% warm.
- outcome: ON median work improved, but material ON tails were caused by
  screen-update tails, so the strict classification is
  `PRESENTATION-BOUND`, not `PROMISING`. Fixed-duration pass totals were not
  used. The prior M3 set and discarded precondition/deploy-only attempts are
  not mixed into the final artifacts.

## 2026-09-21 — corrected M2/M3 rerun

- source/results revision: `d9930c603`; branch:
  `perf/scroll-raster-reuse-poc`.
- M2: one OFF and one ON process completed cold and warm passes. All five
  waypoint hashes matched in both passes; ON recorded 71/71 local hits per
  pass, zero fallbacks and recoveries, and OFF recorded zero hits.
- M2 accounting: target BGRA8888, rowBytes 4320, four bytes per pixel;
  viewport `139557600`, reused `117126000`, dirty `22431600`, and moved bytes
  `468504000` per ON pass. The fast path preserved repaint state after hits.
- M3: exactly three independent OFF and three independent ON processes
  completed. Each pass emitted 75 trace rows with 71 measured movement frames;
  endpoint rows were retained for traceability but excluded from percentiles.
  Rendering diagnostics and image accounting were disabled; local metrics
  reported 71/71 ON hits and zero OFF hits.
- performance: combined movement-frame work P50/P95/P99/MAX was
  `4.099/4.463/4.561/4.760 ms` OFF versus `2.712/3.274/3.471/3.635 ms`
  ON cold, and `4.047/4.374/4.520/4.963 ms` OFF versus
  `2.622/3.425/3.779/3.828 ms` ON warm. Screen-update P50 remained near
  `1.5 ms`; paced pass totals were effectively unchanged.
- outcome: `PRESENTATION-BOUND`; average row/image paints fell from
  `6.000/18.000` to `1.775/5.324` cold and from `6.014/18.042` to
  `1.784/5.352` warm. Compact current-HEAD evidence is in
  `.agent/benchmarks/scroll-raster-reuse-poc/final/`.
