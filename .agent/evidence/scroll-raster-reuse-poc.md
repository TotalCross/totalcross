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
