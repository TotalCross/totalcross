<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Corrected opaque writePixels benchmark matrix

These captures use the corrected 1,536-draw batch at revision
`4af79bc0c4edb0fe8f2f9c1c9c4c4486c87a30e1`. Historical opaque-draw S1/S2/S3
artifacts remain in `../opaque-draw/`. Full target pixel hashes are computed
after each timed batch.

Environment: macOS 26.5.2, Darwin 25.5.0, arm64 MacBookPro18,1, 16 GiB RAM;
Release software Skia; three warmup batches; 60 samples; 50 ms RSS sampling.
The enabled workload explicitly leaves `RASTER_OPACITY_METADATA` disabled, so
the first eligible draw performs the required cached opacity fallback scan.

| Fixture | Batch | S1 median/P95 | S2 median/P95 | S3 median/P95 | S3 attempts/hits/fallbacks | Fallback scan pixels | Full hash |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| JPEG 512x512 | 1,536 | 1,244/1,250 ms | 1,260/1,264 ms | 34/36 ms | 96,771 / 96,769 / 2 | 1 × 262,144 | `6C3E43B0` |
| Opaque PNG 600x600 | 1,536 | 1,707/1,713 ms | 1,729/1,731 ms | 51/52 ms | 96,771 / 96,769 / 2 | 1 × 360,000 | `2E3BE025` |

All final samples were at least 30 ms. S2 kept `RASTER_OPAQUE_WRITE_PIXELS`
disabled; S3 enabled only that feature. The two fallback cases are the
intentional alpha/scaling/geometry guard draws. S3 hashes are stable across
all 60 samples and match S1/S2. The metadata-disabled fallback scan is cached
by backing generation and does not recur for every draw.

Raw CSV files and summaries are named
`scenario-{pre,post-disabled,post-enabled}-{jpeg,png}.*`.
