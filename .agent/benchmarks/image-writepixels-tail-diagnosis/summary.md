<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# writePixels tail diagnosis

This report is generated from the preserved macOS ARM64 raw result directories. `work_time_ns` is the primary timing; `frame_time_ns` is not used for the conclusions.

## Artifacts and identity

| dataset | raw results root | result ZIP SHA-256 |
|---|---|---|
| diagnostic | `/Users/flsobral/repos/totalcross-image-scroll-raster-fast-path/build/writepixels-tail-package-m1-rowfix/image-scroll-benchmark-macos-arm64/results` | `33c4cbb7860f71d2f8f4909716f48dc56487f2d43fbef6dacfa1c63a472bdd7f` |
| diagnostic archive | `/Users/flsobral/repos/totalcross-image-scroll-raster-fast-path/build/writepixels-tail-package-m1-rowfix/image-scroll-benchmark-macos-arm64/results/totalcross-image-benchmark-results-1790029724819212000.zip` | `33c4cbb7860f71d2f8f4909716f48dc56487f2d43fbef6dacfa1c63a472bdd7f` |
| reuse | `/Users/flsobral/repos/totalcross-image-scroll-raster-fast-path/build/writepixels-tail-package-m1-reusefix/image-scroll-benchmark-macos-arm64/results` | `4141a252629bb7232aaa69d7f53a0a4aa1cbacbdf71f1c9783fe55dc53a2cd05` |
| reuse archive | `/Users/flsobral/repos/totalcross-image-scroll-raster-fast-path/build/writepixels-tail-package-m1-reusefix/image-scroll-benchmark-macos-arm64/results/totalcross-image-benchmark-results-1790030412993496000.zip` | `4141a252629bb7232aaa69d7f53a0a4aa1cbacbdf71f1c9783fe55dc53a2cd05` |
| timing | `/Users/flsobral/repos/totalcross-image-scroll-raster-fast-path/build/writepixels-tail-package-m1-timingfix2/image-scroll-benchmark-macos-arm64/results` | `e94de16e3319bddc5e276adbcc1aab96798b88600f0e7c8ec6d34eb63e1051d8` |
| timing archive | `/Users/flsobral/repos/totalcross-image-scroll-raster-fast-path/build/writepixels-tail-package-m1-timingfix2/image-scroll-benchmark-macos-arm64/results/totalcross-image-benchmark-results-1790030894425019000.zip` | `e94de16e3319bddc5e276adbcc1aab96798b88600f0e7c8ec6d34eb63e1051d8` |
| SDK ZIP | `/private/tmp/writepixels-tail-diagnosis-m1/TotalCross-7.2.2-bridgefix.zip` | `ee5f6be1f97424a70598c35cb701d3daec8b67418d774ef5d851cfd20570834a` |

Corpus hash: `588a7e0f4019424a` (663 files).
Target: 1080x1920, software, BGRA8888.

## Coverage

| kind | pair | prefetch | pass | cells | frame rows |
|---|---|---|---|---:|---:|
| diagnostic | 32795->32799 | off | cold | 3 | 121 |
| diagnostic | 32795->32799 | on | cold | 3 | 1134 |
| diagnostic | 32827->32831 | off | cold | 3 | 179 |
| diagnostic | 32827->32831 | on | cold | 3 | 1134 |
| reuse | 32795->32799 | off | cold-forward | 2 | 47 |
| reuse | 32795->32799 | off | warm-forward | 2 | 73 |
| reuse | 32795->32799 | off | warm-reverse | 2 | 67 |
| reuse | 32795->32799 | on | cold-forward | 2 | 756 |
| reuse | 32795->32799 | on | warm-forward | 2 | 756 |
| reuse | 32795->32799 | on | warm-reverse | 2 | 756 |
| reuse | 32827->32831 | off | cold-forward | 2 | 52 |
| reuse | 32827->32831 | off | warm-forward | 2 | 124 |
| reuse | 32827->32831 | off | warm-reverse | 2 | 72 |
| reuse | 32827->32831 | on | cold-forward | 2 | 756 |
| reuse | 32827->32831 | on | warm-forward | 2 | 756 |
| reuse | 32827->32831 | on | warm-reverse | 2 | 756 |
| timing | 32795->32799 | off | cold | 3 | 105 |
| timing | 32795->32799 | on | cold | 3 | 1132 |
| timing-reuse | 32795->32799 | off | cold-forward | 1 | 24 |
| timing-reuse | 32795->32799 | off | warm-forward | 1 | 57 |
| timing-reuse | 32795->32799 | off | warm-reverse | 1 | 32 |
| timing-reuse | 32795->32799 | on | cold-forward | 1 | 378 |
| timing-reuse | 32795->32799 | on | warm-forward | 1 | 376 |
| timing-reuse | 32795->32799 | on | warm-reverse | 1 | 378 |

## Paired percentile deltas

Values are enabled-minus-disabled in nanoseconds. Percentages are computed per process before the table median.

| kind | pair | prefetch | pass | P50 delta ns | P95 delta ns | P99 delta ns | MAX delta ns |
|---|---|---|---|---:|---:|---:|---:|
| diagnostic | 32795->32799 | off | cold | -88334 | 5337625 | 3264833 | 3264833 |
| diagnostic | 32795->32799 | on | cold | -1316791 | 1688083 | -78958 | 484457 |
| diagnostic | 32827->32831 | off | cold | 2555083 | 5394959 | 5394959 | 5394959 |
| diagnostic | 32827->32831 | on | cold | -1150500 | 439751 | 1897334 | 1276167 |
| reuse | 32795->32799 | off | cold-forward | 538375.0 | 28935354.0 | 28935354.0 | 28935354.0 |
| reuse | 32795->32799 | off | warm-forward | -26434708.5 | -3591250.0 | -766541.5 | -766541.5 |
| reuse | 32795->32799 | off | warm-reverse | 35967229.5 | 314749.5 | 28157374.5 | 28157374.5 |
| reuse | 32795->32799 | on | cold-forward | -693041.0 | 90313.0 | 3132709.0 | 7156125.0 |
| reuse | 32795->32799 | on | warm-forward | -840271.0 | -694104.0 | -623354.0 | -320021.0 |
| reuse | 32795->32799 | on | warm-reverse | -833167.0 | -686396.0 | -697896.0 | -239875.0 |
| reuse | 32827->32831 | off | cold-forward | 296813.0 | 1599354.0 | 1599354.0 | 1599354.0 |
| reuse | 32827->32831 | off | warm-forward | -25772833.0 | 30368396.0 | 47316708.5 | 47316708.5 |
| reuse | 32827->32831 | off | warm-reverse | 69910583.5 | -261499.5 | -1630479.0 | -1630479.0 |
| reuse | 32827->32831 | on | cold-forward | -808666.5 | -83396.0 | 1915021.5 | 17387271.0 |
| reuse | 32827->32831 | on | warm-forward | -719270.0 | -721271.0 | -678250.0 | 1469750.0 |
| reuse | 32827->32831 | on | warm-reverse | -778666.5 | -484687.5 | 4629333.5 | 4806625.5 |
| timing | 32795->32799 | off | cold | -2165917 | -917125 | -917125 | -917125 |
| timing | 32795->32799 | on | cold | -620750 | 804167 | 1641168 | -270625 |
| timing-reuse | 32795->32799 | off | cold-forward | -3243250 | -12182708 | -12182708 | -12182708 |
| timing-reuse | 32795->32799 | off | warm-forward | -56612833 | 34887250 | 45449792 | 45449792 |
| timing-reuse | 32795->32799 | off | warm-reverse | -120039292 | -898791 | -898791 | -898791 |
| timing-reuse | 32795->32799 | on | cold-forward | -93709 | 2465500 | -1269083 | -26499916 |
| timing-reuse | 32795->32799 | on | warm-forward | -620750 | 804167 | -36745875 | -276031041 |
| timing-reuse | 32795->32799 | on | warm-reverse | -462916 | 1406584 | 1147958 | 1219667 |

## Copied-byte scaling and format evidence

The copy columns are per-frame byte totals. The diagnostic does not claim that bytes are copy duration; correlation is association.

| mask | prefetch | frames | copy frames | total bytes | copy P50 | copy P95 | max copy | work P50 on copy frames | work P95 on copy frames | corr(bytes,work) | full | clipped | write formats | RGB565 peak | RGBA8888 peak |
|---:|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|---:|---:|
| 32799 | off | 57 | 57 | 440692272 | 7732800 | 7732800 | 7732800 | 65925709 | 382416958 | 0.124467 | 1026 | 0 | 0 | 0 | 754667844 |
| 32799 | on | 567 | 567 | 4384222656 | 7732800 | 7732800 | 7732800 | 3360042 | 7009375 | 0.011915 | 10224 | 0 | 0 | 0 | 565796864 |
| 32831 | off | 74 | 74 | 572149872 | 7732800 | 7732800 | 7732800 | 56358500 | 315029375 | 0.240095 | 1335 | 0 | 0 | 283578230 | 284915592 |
| 32831 | on | 567 | 567 | 4384119552 | 7732800 | 7732800 | 7732800 | 3333958 | 6949167 | 0.012663 | 10227 | 0 | 0 | 113269868 | 340282440 |

## Selected outlier attribution

Selected rows: 108 (all ties at P95/P99/MAX are kept).
writePixels-active rows: 108; materialization/decode rows: 62; paint-dominant rows: 108.

Every enabled diagnostic outlier has a raw frame path, a matched disabled frame, visible-control identity hashes, writePixels copy totals/dimensions, and scroll-versus-paint timing. The outlier CSV records whether matching was exact or nearest by scroll position.

Of the 108 selected outlier rows, 68 had equal visible-control identity hashes
after matching and 40 required nearest-scroll matching with a different hash.
Those nearest matches are retained as context, not treated as causal timing
equivalence.

## Interpretation and STOP / REVIEW outcome

The enabled cells exercise writePixels consistently: the selected diagnostic outliers have writePixels hits, while the paired disabled cells have zero writePixels activity. However, frame copy totals are bounded at roughly one visible-tile batch, all observed hits are full rather than clipped, and copy-bytes/work correlation is weak in the prefetch-on cold cells. Cold outliers coincide with decode/materialization activity in the raw rows; warm/reuse outliers often persist without new materialization. Accounting-off controls preserve the timing comparison while marking attribution columns unavailable.

Outcome 2: writePixels is behaving as an opportunistic fast path, and the expensive tail is dominated by materialization/decode, reuse/pacing, or other same-frame pipeline work rather than a localized copied-byte scaling cause. This is a diagnosis, not a follow-up optimization recommendation.

Format enum `0` is RGBA8888. The RGB565 stratum did create substantial RGB565
backing bytes, but the observed writePixels hits reported format `0`, so the
data does not show a compact-format writePixels copy in these cells.

Limitations: the instrumentation records copied bytes and the last hit dimensions, not a separate copy-duration timer; nearest position matches are not causal timing matches; backing-byte columns are live/peak snapshots, and writePixels hits in the RGB565 stratum report the observed source format at hit time.
