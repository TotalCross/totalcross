<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Milestone 9 Matrix A — isolated Phase 3 storage

The authoritative workload was `milestone9-isolated` on macOS 26.5.2
(`MacBookPro18,1`, Apple M1 Pro, 16 GiB). Each process used Release software
Skia with SDL, three complete warmups, 60 measured samples, and external RSS
sampling every 50 ms.

Runtime and harness provenance:

- S1/pre used exact Phase-2 `6d1c95f77fcb9c74d19b4e9393dba7c82cd37aee`,
  adapter native SHA-256 `6b3a701a5dd75e1447758eb59f061525a50ea80a28da90ecd2562432c234045f`,
  and adapter digest `f2292c73893fb1568c3ba5e56803f4fb0b37d3ff5b7f193f9a992a3e91472f42`.
- S2/S3 used the pre-benchmark Phase-3 tip
  `ecad01ce1ffac609bc7d61c029c4b9e981518145` and dylib SHA-256
  `e62560c60b3cac312206ceae4e3eff4f3d95cfc92edb6621cd9327d82a4353f6`.
- The benchmark source was adapted from `ecad01ce1ffac609bc7d61c029c4b9e981518145`;
  the runner source commit was `a7e19142291d8b491e448e8713ee7b5dc234792c`.

| Run | Median / P95 ms | Mean / SD ms | CV | Peak RSS KiB | Source formats | Output hash |
| --- | ---: | ---: | ---: | ---: | --- | --- |
| S1/pre | 266 / 268.05 | 266.150 / 2.065 | 0.776% | 150,912 | RGBA8888 ×5 | `0000915C00006078` |
| S2/post-disabled | 264 / 272.30 | 265.467 / 6.411 | 2.415% | 152,128 | RGBA8888 ×5 | `0000915C00006078` |
| S3/post-enabled | 184 / 188.05 | 184.533 / 4.241 | 2.298% | 139,840 | RGB565 ×2, GRAY8 ×2, ARGB4444 | `0000941F0000043D` |

The fixture hash was identical in every row:
`rgb565-jpeg:000082CE00001B44|rgb565-png:0000BF0E00009512|gray8-jpeg:0000068500002C94|gray8-png:000042BB00000958|argb4444-png:0000ADD900004B73`.
S1/S2 output parity is exact. S2 versus S1 was −0.75% median elapsed and
+0.81% peak RSS, within the frozen disabled-path gate.

S3 selected all intended compact formats, recorded zero source promotions and
zero temporary full-RGBA decode bytes, and passed the independent quality
oracles: model maximum error 1, ARGB4444 black/white composite maximum error
16/16, and 2,048-byte peak row scratch. The 512×512 fixture backing contract
is 2/1/2 bytes per pixel for RGB565/GRAY8/ARGB4444 (524,288/262,144/524,288
bytes per source); the live per-format counters are retained in the raw CSV
because GC can release earlier sources before a sample is reported.

Direct artifacts are `s1-pre.csv`, `s2-disabled.csv`, `s3-enabled.csv.gz`, and
their adjacent runner summaries. No 200-sample escalation was triggered for
Matrix A: both elapsed CVs were below 5%, and the S2 disabled-path deltas were
below 5% for elapsed time and RSS.
