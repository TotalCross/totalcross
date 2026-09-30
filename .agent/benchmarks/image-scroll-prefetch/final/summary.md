<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image scroll prefetch final benchmark

- Date: 2026-09-10
- Corpus: `/Users/flsobral/Downloads/win32`, exactly 663 JPEGs.
- Matrix: 16 fresh processes and 48 pass records; every process reported
  `overallPass=true`.
- Runtime SHA-256:
  `88e379320ab6c292830588d4291f73d24b142ed02ea4a67e2a35c68fb540a48d`.
- Command: `python3 scripts/run-image-scroll-real-workload-benchmark.py
  --image-dir /Users/flsobral/Downloads/win32
  --tcvm-dylib build-image-scroll-prefetch/libtcvm.dylib
  --output-dir .agent/benchmarks/image-scroll-prefetch/final --skip-build`.

Cold and warm frame p95 values are milliseconds. Each row compares the same
resolution and feature-13/14 profile with prefetch disabled and enabled.

| resolution | target | variant | disabled cold | all cold | disabled warm | all warm |
| --- | --- | --- | ---: | ---: | ---: | ---: |
| 480x720 | disabled | disabled | 53 | 49 | 9 | 4 |
| 480x720 | disabled | enabled | 53 | 49 | 9 | 4 |
| 480x720 | enabled | disabled | 53 | 51 | 9 | 4 |
| 480x720 | enabled | enabled | 58 | 50 | 9 | 9 |
| 540x960 | disabled | disabled | 66 | 64 | 9 | 9 |
| 540x960 | disabled | enabled | 67 | 64 | 9 | 10 |
| 540x960 | enabled | disabled | 65 | 62 | 9 | 9 |
| 540x960 | enabled | enabled | 67 | 59 | 9 | 9 |

The `prefetch=all` profile accounted for 663 requests in every run: 660 ready,
zero failed, and 3 not-prefetchable. Prefetch elapsed time ranged from 2400 to
2540 ms; live and peak detached backing ranged from 666119176 to 676934088
bytes. After the accounting reset, cold scrolls performed zero targeted JPEG
decodes and three full JPEG decodes, while the disabled profile performed
659/655 targeted plus 4/8 full decodes at 480x720/540x960 respectively.

Raw per-process logs and `results.csv` are retained in this directory.
