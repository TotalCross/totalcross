<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# True-base raster decode matrix

This is the corrected decode S1/S2/S3 matrix. Historical artifacts and the
earlier final-runtime-only corrective captures remain under the sibling
directories. S1 uses the Phase-1 runtime at
`9545c18207fab74d81340b24825c5a82ddbda7fd`; S2 and S3 use the final Phase-2
runtime code at `b15aca03291e345186f0c3364eabfc8631163262`. The final harness
and protocol are committed at `b68f6cd28`; the S1 base-worktree adaptation has
digest `824b665ab9459787ad788d51d305bc965adaa62c0f437b43dd044c8c8b86df12`
and only supplies benchmark-compatible counter shims.

All runs used macOS Release software Skia, three warmups, 60 samples, 50 ms
RSS sampling, batched work, and full decoded-pixel hashes outside timing.

| Fixture | S1 median/P95; RSS | S2 median/P95; RSS | S3 median/P95; RSS | Full pixel hash |
| --- | ---: | ---: | ---: | --- |
| 600x600 PNG | 38/39 ms; 147,536 KiB | 37.5/38 ms; 137,312 KiB | 36/37 ms; 143,376 KiB | `4F45E752` |
| 1960x1960 JPEG | 40/41 ms; 207,568 KiB | 40/41 ms; 207,664 KiB | 32/32 ms; 218,208 KiB | `F90EE46E` |

S1 is the true pre-Phase-2 runtime, not a disabled-feature run on the final
runtime. S2 disables all Phase-2 features; S3 enables only decode zero-copy.
All samples met the 30 ms floor, hashes were stable within and across the
three scenarios, and S3 recorded zero copied final decode buffers.

Raw CSV files and summaries are named `s{1,2,3}-{png,jpeg}.*`.
