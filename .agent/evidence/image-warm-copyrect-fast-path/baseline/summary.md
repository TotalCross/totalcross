<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Warm copyRect baseline

- Revision: `9e959282c5f6f39d79e4c3c12d96d96c342aef44`
- Corpus: 663 recursive JPEGs under `/Users/flsobral/Downloads/win32`
- Native build: macOS arm64, Release, software Skia; the pinned depot-tools
  metadata required `QRCODEGEN_RELEASE_TAG=qrcodegen-20250123-r2` and
  `SQLITE3_RELEASE_TAG=sqlite3-3.32.3-r2` because the defaults were stale.
- Native runtime SHA-256:
  `73a2e6f6b3ceaaf4ad4f55c8312d5d4ca574f5261fc715245f5e9fb9685b68e8`
- Matrix: 480x720 and 540x960, with target-color and physical-variant cache
  independently disabled/enabled, three fresh-process passes per case.

The raw CSV and eight per-process logs in this directory are authoritative.
Every case built 663 controls and produced cold, warm, and warm2 records.

| resolution/profile | cold ms / p95 | warm ms / p95 / p99 | warm2 ms / p95 / p99 | cold JPEG targeted/full | warm JPEG targeted/full | writePixels attempts/hits | backing live bytes |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 480x720 00 | 12859 / 57 | 2432 / 9 / 10 | 2433 / 9 / 10 | 659 / 4 | 0 / 0 | 5093 / 0 | 940232584 |
| 480x720 01 | 13438 / 60 | 2432 / 9 / 10 | 2433 / 9 / 10 | 659 / 4 | 0 / 0 | 5093 / 0 | 940232584 |
| 480x720 10 | 12803 / 57 | 2432 / 9 / 10 | 2433 / 9 / 10 | 659 / 4 | 0 / 0 | 5093 / 0 | 940232584 |
| 480x720 11 | 12748 / 57 | 2433 / 9 / 10 | 2433 / 9 / 10 | 659 / 4 | 0 / 0 | 5093 / 0 | 940232584 |
| 540x960 00 | 15960 / 70 | 2739 / 10 / 10 | 2725 / 9 / 10 | 655 / 8 | 0 / 0 | 6552 / 0 | 1019597208 |
| 540x960 01 | 15990 / 70 | 2726 / 9 / 10 | 2725 / 9 / 10 | 655 / 8 | 0 / 0 | 6552 / 0 | 1019597208 |
| 540x960 10 | 16033 / 71 | 2725 / 9 / 10 | 2726 / 9 / 10 | 655 / 8 | 0 / 0 | 6552 / 0 | 1019597208 |
| 540x960 11 | 16014 / 70 | 2724 / 9 / 10 | 2726 / 9 / 10 | 655 / 8 | 0 / 0 | 6552 / 0 | 1019597208 |

All warm and warm2 cases recorded zero JPEG decodes, image materializations,
native geometry materializations, physical identity hits, target-color work,
physical-variant work, generic geometry, and smooth resamples. The current
`ImageControl` path is still Java `copyRect`, so the baseline has no plan-aware
draw counters and writePixels falls back on every attempt.

The separate warm microbenchmark passed all six hash comparisons. Average ms
per 256 operations were 4.0/3.9/3.9 for materialized copy, deferred
`drawImage`, and deferred `copyRect` without clipping; partial clipping was
1.9/1.8/1.9 in the same order. Its raw output is `warm-micro.log`.
