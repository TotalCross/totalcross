<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Scroll raster reuse POC — final timer-gating M3 results

- Source/results HEAD: `f7e662508` on `perf/scroll-raster-reuse-poc`.
- Base: `perf/writepixels-tail-diagnosis@c6cc3bcbf9e28ead3edabb69c12e5c31926a55d0`.
- Workload: 120 sorted JPEGs, three columns, 540x960 logical software raster,
  image optimization mask 0, prefetch ON, two cold/warm passes.
- M3: exactly three independent OFF and three independent ON processes; image
  accounting and rendering diagnostics were OFF in every process.
- Classification: `PRESENTATION-BOUND`.

## Correctness and timer gate

All six fresh M3 processes completed with `overallPass=true`. Every cold and
warm pass matched the five full/top/bottom waypoint hashes. ON recorded 71/71
local fast-path hits per pass, zero fallbacks, and zero post-move recoveries;
OFF recorded zero hits. The target remained BGRA8888 with rowBytes 4320 and
four bytes per pixel, so ON moved `117126000 * 4 = 468504000` bytes per pass.

The source audit found all nine diagnostic `System.nanoTime()` expressions in
`tryRasterReuse` conditional on diagnostics being enabled. The six runtime
frame traces independently reported zero decision, move, and dirty-paint
diagnostic timer deltas while diagnostics were OFF. Benchmark-local hit and
screen-update timing remained active.

The fast path reduced average row/image paints by 70.38% cold and 70.60%
warm:

| pass | OFF rows/images | ON rows/images |
| --- | ---: | ---: |
| cold | 6.023 / 18.070 | 1.784 / 5.352 |
| warm | 6.005 / 18.014 | 1.765 / 5.296 |

Reuse coverage was `0.839266` in every ON pass.

## Movement-frame timing

Values are P50/P95/P99/MAX milliseconds over 213 measured movement frames per
mode and pass, aggregated across the three fresh processes. Endpoint rows were
retained for waypoint traceability but excluded from every distribution.

| pass | work OFF | work ON | screen OFF | screen ON |
| --- | ---: | ---: | ---: | ---: |
| cold | 4.096/4.580/4.668/4.746 | 2.857/7.267/8.133/8.362 | 1.455/1.654/1.700/1.722 | 1.578/4.633/5.342/5.824 |
| warm | 4.031/4.565/4.852/5.053 | 2.664/6.610/7.742/9.514 | 1.447/1.655/1.880/2.173 | 1.456/4.550/5.398/8.001 |

Median work improved 30.2% cold and 33.9% warm, but the ON P95/P99/MAX work
tails regressed materially. The corresponding screen-update tails account for
the regression, while median screen-update time stayed near 1.5 ms. Therefore
the fresh matrix does not satisfy the plan's no-material-tail-regression gate
for `PROMISING`; unchanged full-frame presentation is the next bottleneck and
the strict existing classification is `PRESENTATION-BOUND`. Fixed-duration
pass totals were not used for this classification.

The previous M3 set at `d9930c603` was invalidated before this replacement and
is not mixed into these artifacts. M2 correctness remains retained from the
prior correction checkpoint and was not rerun.
