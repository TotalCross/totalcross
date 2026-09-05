<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# True-base opaque writePixels matrix

S1 uses the Phase-1 runtime at
`9545c18207fab74d81340b24825c5a82ddbda7fd`; S2 and S3 use final Phase-2
runtime code `b15aca03291e345186f0c3364eabfc8631163262`. The final harness and
protocol are committed at `b68f6cd28`; the S1 base-worktree adaptation digest is
`824b665ab9459787ad788d51d305bc965adaa62c0f437b43dd044c8c8b86df12`.

All runs used macOS Release software Skia, three warmups, 60 samples, 50 ms
RSS sampling, full target pixel hashes outside timing, and 1,536 eligible draw
operations per sample. `RASTER_OPACITY_METADATA` is disabled in the S3
workload so its cached fallback scan is measured independently.

| Fixture | S1 median/P95; RSS | S2 median/P95; RSS | S3 median/P95; RSS | S3 attempts/hits/fallbacks | Full pixel hash |
| --- | ---: | ---: | ---: | ---: | --- |
| JPEG 512x512 | 1,257/1,261 ms; 124,272 KiB | 1,257/1,262 ms; 117,744 KiB | 34/39 ms; 151,712 KiB | 96,771 / 96,769 / 2 | `6C3E43B0` |
| Opaque PNG 600x600 | 1,725.5/1,735 ms; 121,312 KiB | 1,727/1,731 ms; 121,424 KiB | 47/49 ms; 159,040 KiB | 96,771 / 96,769 / 2 | `2E3BE025` |

S1 is the true pre-Phase-2 runtime. S2 disables opaque writePixels; S3 enables
only that feature. The two intentional guard cases remain fallbacks, and the
metadata-disabled opacity path performs one cached scan per backing generation.
All samples met the 30 ms floor and hashes matched across scenarios.

Raw CSV files and summaries are named `s{1,2,3}-{jpeg,png}.*`.
