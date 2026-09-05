<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Corrected zero-copy decode benchmark matrix

These captures use the corrected batched workload at revision
`4af79bc0c4edb0fe8f2f9c1c9c4c4486c87a30e1`. The historical zero-copy S1/S2/S3
artifacts remain in `../zero-copy/`; these files recapture the three runner
states with a fixed batch duration and full decoded-pixel hashes outside the
timed section.

Environment: macOS 26.5.2, Darwin 25.5.0, arm64 MacBookPro18,1, 16 GiB RAM.
Native configuration was Release software Skia with
`TC_GRAPHICS_SOFTWARE=ON`, `TC_RENDERER_SKIA=ON`, and `TC_WINDOWING_SDL=ON`.
Each run used three warmup batches, 60 samples, and 50 ms RSS sampling.

| Fixture | Batch | S1 median/P95 | S2 median/P95 | S3 median/P95 | S3 counters | Full pixel hash |
| --- | ---: | ---: | ---: | ---: | --- | --- |
| 600x600 PNG | 10 | 37/38 ms | 37/38 ms | 36/37 ms | zero-copy 600, copied 0 | `4F45E752` |
| 1960x1960 JPEG | 2 | 40/41 ms | 40/41 ms | 31/32 ms | zero-copy 120, copied 0 | `F90EE46E` |

All samples met the 30 ms timing floor. S2 kept `DECODE_ZERO_COPY` disabled;
S3 enabled only that feature. S2/S3 hashes were stable across all 60 samples
and matched the control hash. S3 copied zero decoded bytes while retaining one
final RGBA buffer per decode.

Raw CSV files and runner summaries are named
`scenario-{pre,post-disabled,post-enabled}-{png,jpeg}.*`.
