<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# State — writePixels tail diagnosis

## Active slice

- Milestone 1 bounded per-frame attribution is complete; Milestone 2 fresh
  SDK/macOS package and matrix execution is active.
- Branch: `perf/writepixels-tail-diagnosis`.
- Requested base: `perf/image-decode-distributed-benchmark`.
- Base and starting HEAD: `d5a682e0d1a32928f1e96bc62be083f6f2d813df`.
- Last logical commit: `bc4a207e9` (`test(benchmark): add per-frame writePixels
  attribution`).
- Next action: run the fresh accounting-on diagnostic and reuse matrices, then
  the accounting-off timing controls.

## Working set

- Plan: `.agent/plans/image-writepixels-tail-diagnosis.md`
- State: `.agent/state/image-writepixels-tail-diagnosis.md`
- Evidence: `.agent/evidence/image-writepixels-tail-diagnosis.md`
- Final report:
  `.agent/reports/image-writepixels-tail-diagnosis-editorial.md`
- Final compact artifacts:
  `.agent/benchmarks/image-writepixels-tail-diagnosis/`
- Primary app:
  `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageScrollRealWorkloadBenchmarkApp.java`
- Runner: `scripts/run-image-scroll-distributed-benchmark.py`
- Native measurement:
  `TotalCrossVM/src/nm/ui/skia/skia_image_backing.cpp` and its bridge files.

## Validation completed

- Read `AGENTS.md`, `.agent/PLANS.md`, and
  `.agents/skills/logical-commits/SKILL.md` in full.
- Inspected the M1–M4 state/evidence, current runner profiles, app frame schema,
  native writePixels implementation, and existing accounting bridge.
- Created the branch from the checked-out
  `perf/image-decode-distributed-benchmark` branch without touching existing
  untracked artifacts.
- Confirmed `/Users/flsobral/Downloads/win32/win32/imag` contains 663
  JPEG-named files and the branch starts at the requested base commit.
- Added frame-scope writePixels attempts, hits, fallbacks, copied bytes,
  clipped/full counts, dimensions, and backing format metrics.
- Added scroll-versus-paint frame deltas for JPEG decode, materialization,
  target-color, physical-variant, backing, and storage-format counters.
- Extended the packaged macOS runner with diagnostic, reuse, and timing
  profiles while keeping rendering policy and defaults unchanged.
- SDK distribution passed through `TotalCrossSDK/gradlew-agent dist -x test`;
  native macOS `tcvm` and `Launcher` Release build passed.
- Native `skia_surface_test`, bundle self-test, and all six packaged macOS
  smoke cases passed after fixing bridge-name truncation and CSV termination.
- Fresh SDK ZIP: `/tmp/writepixels-tail-diagnosis-m1/TotalCross-7.2.2-bridgefix.zip`.
- SDK ZIP SHA-256:
  `ee5f6be1f97424a70598c35cb701d3daec8b67418d774ef5d851cfd20570834a`.
- Runtime SHA-256:
  `ca59b0a0436ada76fd34a4ec1dcdafd37dcf418acb5e89f3c00d36b5beb7d975`.

## Deferred validation

- No fresh diagnostic or timing matrix has been run yet; it starts from the
  packaged bundle after this checkpoint.
- No historical M1–M4 evidence was modified.

## Historical commit-message audit

The post-commit checker found over-80-character body lines in `a3c1c8820` and
`67310f920`. The implementation commit `bc4a207e9` also has one over-80
body line. All three signed commits are preserved without amendment or history
rewriting. Subsequent commit messages will be wrapped before validation.

## Active decisions

- Primary A/B: `32795` versus `32799`.
- Source-backing stratum: `32827` versus `32831`.
- Attribution uses accounting-on; accounting-off is a later timing control.
- `work_time_ns` is primary; scroll and paint timings are attribution fields.
- No policy/default/renderer behavior changes are authorized.

## Deliberate out-of-scope local files

Preserve all pre-existing untracked logs, generated benchmark directories,
`TotalCrossSDK/IOSDateFixture.tcz`, `TotalCrossSDK/etc/launchers/`, and
`scripts/__pycache__/`. Do not stage them.

## Resume command

Read this file, then the active milestone in
`.agent/plans/image-writepixels-tail-diagnosis.md`, and inspect only the paths
listed under the active slice before taking the next action.
