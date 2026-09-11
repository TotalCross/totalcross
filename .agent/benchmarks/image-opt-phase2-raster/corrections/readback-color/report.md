<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Corrected readback and color benchmark matrix

These captures use the corrected post-timing hash workload at revision
`7cfa7521f`. Historical readback/color S1/S2/S3 artifacts remain in
`../readback-color/`. Full output hashes are calculated after timing.

Environment: macOS 26.5.2, Darwin 25.5.0, arm64 MacBookPro18,1, 16 GiB RAM;
Release software Skia; three warmup batches; 60 samples; 50 ms RSS sampling.

| Operation | Batch | S1 median/P95 | S2 median/P95 | S3 median/P95 | S3 scratch/counters | Full output hash |
| --- | ---: | ---: | ---: | ---: | --- | --- |
| `pixels` | 2 | 45/46 ms | 45/46 ms | 44/44 ms | 7,840 row bytes; 235,200 row reads | `F90EE46E` |
| `encode` | 1 | 463/465 ms | 462/466 ms | 463/466 ms | 7,840 row bytes; 117,600 row reads | `AAF450B2` |
| `color` | 1 | 36/37 ms | 37/37 ms | 37/38 ms | direct materializations 60; post-check full readback | `E3A50B95` |

All samples met the 30 ms floor and all hashes were stable across the 60
samples and scenarios. S2 kept both readback/color features disabled; S3
enabled only the feature corresponding to the operation. The direct color
path's post-operation verification readback is reported separately from its
row-bounded source processing.

Raw CSV files and summaries are named
`scenario-{pre,post-disabled,post-enabled}-{pixels,encode,color}.*`.
