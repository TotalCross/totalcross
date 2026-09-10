<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Prefetch baseline

- Corpus: `/Users/flsobral/Downloads/win32`, exactly 663 JPEGs.
- Runtime SHA-256: `efef5fb8b062df88054daa7c2e4aeeff98b1b1dd1c2ea49e00aad48ce188a61a`.
- Matrix: 480x720 and 540x960, all four target-color/physical-variant
  combinations, three passes per fresh process; 24 pass records succeeded.
- Command: `TC_IMAGE_CORPUS=/Users/flsobral/Downloads/win32 python3
  scripts/run-image-scroll-real-workload-benchmark.py --image-dir
  "$TC_IMAGE_CORPUS" --tcvm-dylib build-image-scroll-raster/libtcvm.dylib
  --output-dir .agent/benchmarks/image-scroll-prefetch/baseline --skip-build`

Cold p95 / warm p95 / warm2 p95 by resolution and feature profile:

| resolution | target | variant | cold | warm | warm2 |
| --- | --- | --- | ---: | ---: | ---: |
| 480x720 | disabled | disabled | 56 | 11 | 7 |
| 480x720 | disabled | enabled | 55 | 7 | 7 |
| 480x720 | enabled | disabled | 54 | 8 | 11 |
| 480x720 | enabled | enabled | 54 | 9 | 9 |
| 540x960 | disabled | disabled | 66 | 9 | 9 |
| 540x960 | disabled | enabled | 65 | 9 | 9 |
| 540x960 | enabled | disabled | 65 | 9 | 9 |
| 540x960 | enabled | enabled | 66 | 9 | 9 |

Cold decode accounting was 659 targeted + 4 full JPEG decodes at 480x720 and
655 targeted + 8 full decodes at 540x960; all warm and warm2 decode counts were
zero. Raw per-run logs and `results.csv` are retained beside this summary.
