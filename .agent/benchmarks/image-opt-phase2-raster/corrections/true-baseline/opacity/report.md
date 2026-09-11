<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# True-base structural-opacity matrix

This corrected matrix uses S1 from the Phase-1 runtime
`9545c18207fab74d81340b24825c5a82ddbda7fd`. S2 and S3 use the final Phase-2
runtime code `b15aca03291e345186f0c3364eabfc8631163262`; the final harness and
protocol are committed at `b68f6cd28`. The S1 base-worktree harness adaptation
has digest `824b665ab9459787ad788d51d305bc965adaa62c0f437b43dd044c8c8b86df12`
and changes only benchmark counter access.

All runs used macOS Release software Skia, three warmups, 60 samples, 50 ms
RSS sampling, and full decoded-pixel hashes outside timing.

| Fixture | S1 median/P95; RSS | S2 median/P95; RSS | S3 median/P95; RSS | Full pixel hash |
| --- | ---: | ---: | ---: | --- |
| JPEG 512x512 | 37/38 ms; 142,960 KiB | 37/38 ms; 156,560 KiB | 38/38 ms; 164,848 KiB | `6C3E43B0` |
| RGB PNG | 48/49 ms; 121,088 KiB | 48/48 ms; 121,040 KiB | 49/49 ms; 122,704 KiB | `116E386A` |
| Alpha PNG, all opaque | 35/36 ms; 264,416 KiB | 35/36 ms; 250,912 KiB | 38/40 ms; 250,144 KiB | `2E3BE025` |
| Alpha PNG, translucent | 41/42 ms; 118,320 KiB | 42/43 ms; 118,512 KiB | 42/44 ms; 118,480 KiB | `63184865` |

S1 is the true pre-Phase-2 runtime. S2 disables opacity metadata; S3 enables
only opacity metadata. The source/decode proof counters are present in S3,
and all samples met the 30 ms floor with stable matching hashes.

Raw CSV files and summaries are named `s{1,2,3}-{fixture}.*`.
