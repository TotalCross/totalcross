<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# State — writePixels tail diagnosis

## Active slice

- Milestones 1–3 are complete; the STOP / REVIEW gate is active and the
  diagnosis is frozen at Outcome 2.
- Branch: `perf/writepixels-tail-diagnosis`.
- Requested base: `perf/image-decode-distributed-benchmark`.
- Base and starting HEAD: `d5a682e0d1a32928f1e96bc62be083f6f2d813df`.
- Last logical commit: `766a27166` (`docs(benchmark): close writePixels tail
  diagnosis`).
- Next action: stop for review without starting optimization work.

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
- Accounting-on diagnostic matrix passed: 24 processes across four masks,
  prefetch off/on, and three rounds; result ZIP SHA-256
  `33c4cbb7860f71d2f8f4909716f48dc56487f2d43fbef6dacfa1c63a472bdd7f`.
- Accounting-on reuse matrix passed: 16 processes, three passes per process;
  result ZIP SHA-256
  `4141a252629bb7232aaa69d7f53a0a4aa1cbacbdf71f1c9783fe55dc53a2cd05`.
- Accounting-off timing matrices passed: 12 cold and 4 reuse processes;
  result ZIP SHA-256
  `e94de16e3319bddc5e276adbcc1aab96798b88600f0e7c8ec6d34eb63e1051d8`.
- Tracked analysis contains 48 paired rows and 108 selected outlier rows;
  68 outliers have equal identity hashes and 40 use nearest-scroll matches.

## Deferred validation

- Android, iOS, Windows, Linux, broad historical matrices, and follow-up
  optimization benchmarks remain deferred because they are outside this
  macOS diagnosis.
- No historical M1–M4 evidence was modified.

## Historical commit-message audit

The post-commit checker found over-80-character or literal escaped paragraph
markers in historical signed commits `a3c1c8820`, `67310f920`, `bc4a207e9`,
and `14ce18a5e`. They are preserved without amendment or history rewriting.
Later commits use separate wrapped message arguments and pass the checker.

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
