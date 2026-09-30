<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Corrected structural-opacity benchmark matrix

These captures use the corrected batched workload at revision
`b15aca032`. Historical opacity S1/S2/S3 artifacts remain in `../opacity/`.
Hashes are computed over every decoded pixel outside the timed section.

Environment: macOS 26.5.2, Darwin 25.5.0, arm64 MacBookPro18,1, 16 GiB RAM;
Release software Skia; three warmup batches; 60 samples; 50 ms RSS sampling.

| Fixture | Batch | S1 median/P95 | S2 median/P95 | S3 median/P95 | S3 opacity/proof counters | Full pixel hash |
| --- | ---: | ---: | ---: | ---: | --- | --- |
| JPEG 512x512 | 24 | 37/38 ms | 37/38 ms | 38/39 ms | OPAQUE; source 1,440 | `6C3E43B0` |
| RGB PNG | 24 | 48/49 ms | 48/49 ms | 48/49 ms | OPAQUE; source 1,440 | `116E386A` |
| Alpha PNG, all opaque | 24 | 35/36 ms | 35/36 ms | 38/39 ms | OPAQUE; decode 1,440 | `2E3BE025` |
| Alpha PNG, translucent | 1024 | 42/43 ms | 42/44 ms | 42/43 ms | TRANSLUCENT; decode 61,440 | `63184865` |

The fixture batches were increased until every timed sample was at least
30 ms in the final matrix. The full hashes are stable across every scenario
and sample. JPEG and alpha-free PNG use source proofs; alpha PNG
classification occurs during row decode without a second full-image scan.

Raw CSV files and summaries are named
`scenario-{pre,post-disabled,post-enabled}-{kind}.*`.
