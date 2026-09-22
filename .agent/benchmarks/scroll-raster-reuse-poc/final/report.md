<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Scroll raster reuse POC — invalidated prior M3 results

> INVALIDATED: these M3 measurements were collected at `d9930c603` before the
> timer-gating correction at `f7e662508`. Do not use them for final
> classification; the replacement six-process M3 matrix is pending.

- Base: `perf/writepixels-tail-diagnosis@c6cc3bcbf9e28ead3edabb69c12e5c31926a55d0`.
- Invalidated measurement HEAD: `d9930c603`.
- Replacement measurement HEAD: `f7e662508`.
- Workload: 120 sorted JPEGs, three columns, 540x960 software raster,
  image optimization mask 0, prefetch ON, two cold/warm passes.
- M2: one OFF and one ON correctness process with accounting ON.
- M3: exactly three independent OFF and three independent ON processes with
  image accounting OFF and rendering diagnostics OFF.
- Classification: `PRESENTATION-BOUND`.

## Correctness and accounting

The current M2 OFF/ON pair matched every full, top-slice, and bottom-slice
waypoint hash in both passes. ON recorded 71/71 local hits per pass, zero
fallbacks, zero post-move recoveries, and no pending repaint after measured
frames. OFF recorded zero hits. The target was BGRA8888 with rowBytes 4320
and four bytes per pixel; ON moved `117126000 * 4 = 468504000` bytes per pass.

All six M3 processes also matched every waypoint hash across all rounds. Each
pass emitted 75 trace rows, of which 71 were actual movement frames; endpoint
rows remain in the trace with zero work and are excluded from every percentile.
M3 diagnostics and accounting were disabled, while local hit/outcome metrics
still reported 71/71 ON hits and zero OFF hits.

The fast path reduced average row/image paints by about 70.4%:

| pass | OFF rows/images | ON rows/images |
| --- | ---: | ---: |
| cold | 6.000 / 18.000 | 1.775 / 5.324 |
| warm | 6.014 / 18.042 | 1.784 / 5.352 |

Reuse coverage was `0.839266` (`117126000 / 139557600`) in every ON pass.

## Movement-frame timing

Values are P50/P95/P99/MAX milliseconds over 213 measured frames per mode and
pass, aggregated across the three M3 processes.

| pass | work OFF | work ON | screen OFF | screen ON |
| --- | ---: | ---: | ---: | ---: |
| cold | 4.099/4.463/4.561/4.760 | 2.712/3.274/3.471/3.635 | 1.501/1.699/1.935/2.056 | 1.524/1.775/1.824/1.848 |
| warm | 4.047/4.374/4.520/4.963 | 2.622/3.425/3.779/3.828 | 1.503/1.721/1.900/1.994 | 1.498/1.800/2.019/2.216 |

Median measured work fell 33.8% cold and 35.2% warm. Screen-update P50 stayed
near 1.5 ms, with a small cold increase and no material warm improvement. The
time-paced pass totals were effectively unchanged: cold medians were 1250.831
ms OFF versus 1251.545 ms ON; warm medians were 1250.516 ms OFF versus
1249.873 ms ON. The optimization removes real raster/UI work, but unchanged
full-frame presentation remains the limiting end-to-end stage.

The compact CSVs in this directory contain the current-HEAD diagnostic,
movement-frame, waypoint-hash, performance, and top-frame evidence. The
previous result set was invalidated and is not mixed into these files.
