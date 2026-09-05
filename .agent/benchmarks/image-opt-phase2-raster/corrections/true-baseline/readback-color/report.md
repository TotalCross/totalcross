<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# True-base readback and color matrix

S1 uses the Phase-1 runtime at
`9545c18207fab74d81340b24825c5a82ddbda7fd`; S2 and S3 use final Phase-2
runtime code `b15aca03291e345186f0c3364eabfc8631163262`. The final harness and
protocol are committed at `b68f6cd28`; the S1 base-worktree adaptation digest is
`824b665ab9459787ad788d51d305bc965adaa62c0f437b43dd044c8c8b86df12`.

All runs used macOS Release software Skia, three warmups, 60 samples, 50 ms
RSS sampling, and full output hashes outside timing.

| Operation | S1 median/P95; RSS | S2 median/P95; RSS | S3 median/P95; RSS | Full output hash |
| --- | ---: | ---: | ---: | --- |
| `pixels` | 45/46 ms; 192,224 KiB | 45/46 ms; 207,648 KiB | 44/45 ms; 177,488 KiB | `F90EE46E` |
| `encode` | 461/467 ms; 192,400 KiB | 464/468 ms; 215,328 KiB | 465/469 ms; 214,944 KiB | `AAF450B2` |
| `color` | 37/38 ms; 177,024 KiB | 37/39 ms; 192,640 KiB | 38/39 ms; 192,544 KiB | `E3A50B95` |

S1 is the true pre-Phase-2 runtime. S2 disables row readback and direct color
materialization; S3 enables only the feature corresponding to the operation.
All samples met the 30 ms floor and hashes matched across scenarios.

Raw CSV files and summaries are named `s{1,2,3}-{pixels,encode,color}.*`.
