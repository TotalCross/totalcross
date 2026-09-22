<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Scroll raster reuse POC — final results

- HEAD: `99929d06f` on `perf/scroll-raster-reuse-poc`.
- Workload: 120 sorted JPEGs, three columns, 540x960 macOS software raster,
  `ImageOptimizations=0`, prefetch profile ON, two cold/warm passes.
- Matrix: three independent OFF processes and three independent ON processes;
  M3 accounting was OFF as required. M2 accounting-ON correctness was also
  completed before this matrix.
- Classification: `PRESENTATION-BOUND`.

## Correctness and reuse

All six M3 processes completed. OFF and ON hashes matched at all five
waypoints in both passes, and hashes were stable across all three rounds.
ON recorded 71 attempts, 71 hits, zero fallbacks, and zero post-move
recoveries in every pass. OFF recorded zero hits. Accounting was consistent:
`71 = 71 + 0`, and `117126000 + 22431600 = 139557600` pixels. Reuse coverage
was `0.839266`, with 117126000 moved bytes per pass.

The fast path reduced average row/image paints by 66.8% cold and 66.7% warm:

| pass | OFF rows/images | ON rows/images |
| --- | ---: | ---: |
| cold | 6.013 / 18.040 | 1.996 / 5.987 |
| warm | 6.018 / 18.053 | 2.004 / 6.013 |

## Work and presentation timing

Values below are P50/P95/P99/MAX milliseconds over 225 frames per mode/pass.

| pass | work OFF | work ON | screen update OFF | screen update ON |
| --- | ---: | ---: | ---: | ---: |
| cold | 4.079/4.532/4.887/5.033 | 2.681/3.854/4.209/4.256 | 1.473/1.675/1.842/1.951 | 1.505/1.760/1.853/1.906 |
| warm | 4.022/4.384/4.844/6.219 | 2.634/4.063/4.500/6.446 | 1.464/1.648/2.014/3.057 | 1.516/1.832/2.186/4.791 |

Median work fell 34.3% cold and 34.5% warm, but the unchanged full-frame
presentation remained about 1.5 ms at the median and was slightly higher ON.
The time-paced pass totals therefore did not improve: cold medians were
1251.108 ms OFF versus 1251.577 ms ON; warm medians were 1250.402 ms OFF
versus 1261.126 ms ON. The result is presentation-bound rather than a reason
to add SDL dirty uploads in this plan.

Supporting compact CSVs are in this directory: `diagnostic-summary.csv`,
`performance-summary.csv`, `waypoint-hashes.csv`, `frame-summary.csv`, and
`top-frames.csv`. No follow-up optimization was implemented.
