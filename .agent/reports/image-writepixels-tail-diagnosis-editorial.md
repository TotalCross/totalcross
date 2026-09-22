<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# writePixels tail diagnosis — final report

## Outcome

Outcome 2: direct timing shows that `writePixels` itself is not responsible
for the expensive tail. It remains an opportunistic fast path, while the
largest fresh tails are dominated by remaining paint work and, for prefetch-off
rows, decode/materialization activity. No policy, default, GPU, dirty-region,
or scroll-reuse redesign is authorized by this diagnosis.

## Scope and method

- Branch: `perf/writepixels-tail-diagnosis`, based on
  `perf/image-decode-distributed-benchmark` at
  `d5a682e0d1a32928f1e96bc62be083f6f2d813df`.
- Workload: 663 sorted JPEG-named images, logical 540x960, macOS ARM64,
  software BGRA8888 target, corpus hash `588a7e0f4019424a`.
- Primary pair: `32795` disabled versus `32799` enabled. The stratified
  backing pair is `32827` versus `32831`, with RGB565 enabled in both cells.
- Historical coverage is retained in the compact artifacts. The correction
  uses fresh coverage only: 8 accounting-on cold processes, 4 accounting-on
  three-pass reuse processes, and 4 accounting-off cold controls, all on
  `32795 -> 32799` with prefetch OFF/ON.

The instrumentation adds accounting-gated frame and scroll/paint segment
deltas for writePixels, materialization, JPEG, target-color, physical-variant,
backing bytes, storage format, dimensions, and visible-control identity. It
does not change rendering policy or optimization defaults.

## Findings

The correction artifact retains 10 paired percentile rows and 30 fresh P95,
P99, and MAX outlier rows. All selected enabled outliers have writePixels
hits. Direct timing records total path, preparation, actual canvas-copy, and
RGB565 conversion time, plus scroll/paint segment fields.

The largest fresh prefetch-off tail was 373.4 ms with 1.064 ms total
writePixels (1.037 ms actual copy, 25.8 us preparation). The largest
prefetch-on cold tail was 39.1 ms with 0.514 ms total writePixels (0.508 ms
actual copy, 5.6 us preparation). The largest reuse tails show the same shape:
380.7 ms with 1.156 ms total writePixels off and 30.1 ms with 0.300 ms total
writePixels on. The highest selected copy/work fraction was 11.8% on a 4.6 ms
prefetch-on tail; the larger tails were below 1.3% copy/work.

Prefetch-off selected tails also carried roughly 29–39 ms JPEG decode time,
while remaining paint work was 99.7% of work after subtracting paint-segment
writePixels time. Materialization is recorded as activity rather than a
duration, so decode and remaining-paint fractions are context and not additive
independent buckets. The primary 32-bit pair observed full RGBA8888 hits;
RGB565 conversion timing was available but zero in these cells.

## Evidence artifacts

- [summary.md](../benchmarks/image-writepixels-tail-diagnosis/summary.md)
- [pairwise.csv](../benchmarks/image-writepixels-tail-diagnosis/pairwise.csv)
- [outliers.csv](../benchmarks/image-writepixels-tail-diagnosis/outliers.csv)
- [analyze-writepixels-tail-diagnosis.py](../../scripts/analyze-writepixels-tail-diagnosis.py)
- [timing-correction/summary.md](../benchmarks/image-writepixels-tail-diagnosis/timing-correction/summary.md)
- [timing-correction/pairwise.csv](../benchmarks/image-writepixels-tail-diagnosis/timing-correction/pairwise.csv)
- [timing-correction/outliers.csv](../benchmarks/image-writepixels-tail-diagnosis/timing-correction/outliers.csv)

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

Correction package identity: SDK ZIP
`a809110df548b40d558cb89b87c0ccb670ed75facfb08abc9f0d132ffc0db721`;
runtime `1f161b5741a45b8f498a5b403d78d6cf35c58bf1a48531d128b879e37f4da743`.
Correction raw archives are hashed in
`timing-correction/summary.md`.

## Validation

Passed:

- `python3 -m py_compile` for the benchmark runner and analyzer.
- Focused copyright-header validation and `git diff --check`.
- SDK distribution through `TotalCrossSDK/gradlew-agent dist -x test`.
- macOS ARM64 Release `tcvm` and `Launcher` build.
- Native `skia_surface_test`, package self-test, and six packaged smoke cases.
- Diagnostic, reuse, timing, and timing-reuse runner invariant validation.
- Analyzer validation: 48 pairwise rows and 108 outlier rows.
- Corrected package self-test, six macOS smoke cases, and 16-process direct
  timing matrix validation.
- Direct analyzer validation: 10 paired rows and 30 timing outlier rows.

Deferred: Android, iOS, Windows, Linux, broad historical matrices, and any
optimization benchmark after this diagnosis. They are outside the requested
macOS diagnostic scope.

## STOP / REVIEW

Diagnosis is frozen at the corrected Outcome 2. Review the direct timing
artifacts before any new optimization or policy work begins.
