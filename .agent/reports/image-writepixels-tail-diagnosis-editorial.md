<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# writePixels tail diagnosis — final report

## Outcome

Outcome 2: `writePixels` is behaving as an opportunistic fast path, while the
expensive tail is dominated by materialization/decode, reuse/pacing, or other
same-frame pipeline work. No policy, default, GPU, dirty-region, or
scroll-reuse redesign is authorized by this diagnosis.

## Scope and method

- Branch: `perf/writepixels-tail-diagnosis`, based on
  `perf/image-decode-distributed-benchmark` at
  `d5a682e0d1a32928f1e96bc62be083f6f2d813df`.
- Workload: 663 sorted JPEG-named images, logical 540x960, macOS ARM64,
  software BGRA8888 target, corpus hash `588a7e0f4019424a`.
- Primary pair: `32795` disabled versus `32799` enabled. The stratified
  backing pair is `32827` versus `32831`, with RGB565 enabled in both cells.
- Fresh coverage: 24 accounting-on cold diagnostic processes, 16 accounting-on
  cold/warm reuse processes, 12 accounting-off cold timing processes, and 4
  accounting-off reuse timing processes.

The instrumentation adds accounting-gated frame and scroll/paint segment
deltas for writePixels, materialization, JPEG, target-color, physical-variant,
backing bytes, storage format, dimensions, and visible-control identity. It
does not change rendering policy or optimization defaults.

## Findings

The compact artifacts retain 48 paired percentile rows and 108 selected P95,
P99, and MAX outlier rows. All selected enabled outliers have writePixels
hits; paired disabled cells have zero writePixels activity. Across the enabled
diagnostic frames, copied bytes are bounded at 7,732,800 bytes per frame, all
observed hits are full rather than clipped, and prefetch-on cold
copy-bytes/work correlations are 0.011915 and 0.012663 for the two enabled
masks.

Cold outliers commonly contain JPEG decode and materialization activity. Warm
outliers can persist without new materialization, which points to reuse,
pacing, or other pipeline work rather than a steadily scaling copy. The
RGB565 stratum creates substantial RGB565 backing bytes, but the observed
writePixels hit format is enum `0` (RGBA8888); these samples do not show a
compact-format writePixels copy.

Visible-control identity hashes are equal for 68 of 108 selected rows. The
remaining 40 use documented nearest-scroll matches with a different hash and
are retained as context, not causal timing equivalence.

## Evidence artifacts

- [summary.md](../benchmarks/image-writepixels-tail-diagnosis/summary.md)
- [pairwise.csv](../benchmarks/image-writepixels-tail-diagnosis/pairwise.csv)
- [outliers.csv](../benchmarks/image-writepixels-tail-diagnosis/outliers.csv)
- [analyze-writepixels-tail-diagnosis.py](../../scripts/analyze-writepixels-tail-diagnosis.py)

Raw result ZIP hashes:

- diagnostic:
  `33c4cbb7860f71d2f8f4909716f48dc56487f2d43fbef6dacfa1c63a472bdd7f`
- reuse:
  `4141a252629bb7232aaa69d7f53a0a4aa1cbacbdf71f1c9783fe55dc53a2cd05`
- timing:
  `e94de16e3319bddc5e276adbcc1aab96798b88600f0e7c8ec6d34eb63e1051d8`

Package identity: SDK ZIP
`ee5f6be1f97424a70598c35cb701d3daec8b67418d774ef5d851cfd20570834a`; runtime
`ca59b0a0436ada76fd34a4ec1dcdafd37dcf418acb5e89f3c00d36b5beb7d975`.

## Validation

Passed:

- `python3 -m py_compile` for the benchmark runner and analyzer.
- Focused copyright-header validation and `git diff --check`.
- SDK distribution through `TotalCrossSDK/gradlew-agent dist -x test`.
- macOS ARM64 Release `tcvm` and `Launcher` build.
- Native `skia_surface_test`, package self-test, and six packaged smoke cases.
- Diagnostic, reuse, timing, and timing-reuse runner invariant validation.
- Analyzer validation: 48 pairwise rows and 108 outlier rows.

Deferred: Android, iOS, Windows, Linux, broad historical matrices, and any
optimization benchmark after this diagnosis. They are outside the requested
macOS diagnostic scope.

## STOP / REVIEW

Diagnosis is frozen at Outcome 2. Review the paired artifacts before any new
optimization or policy work begins.
