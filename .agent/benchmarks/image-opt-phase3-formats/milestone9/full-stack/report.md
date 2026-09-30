<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Milestone 9 Matrix B — full Phase 2 plus Phase 3

The authoritative workload was `milestone9-full-stack` on macOS 26.5.2
(`MacBookPro18,1`, Apple M1 Pro, 16 GiB). Each process used Release software
Skia with SDL, three complete warmups, 60 measured samples, and external RSS
sampling every 50 ms. S1 enabled exactly `PHASE2_FINAL={0,1,2,3,4,13,14,15}`;
S2 enabled that same set with Phase-3 storage and IDs 8–12 disabled; S3 added
exactly `PHASE3_STORAGE={5,6,7}`.

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
| S1/pre | 162 / 163.00 | 161.767 / 0.745 | 0.460% | 141,792 | RGBA8888 ×5 | `0000915C00006078` |
| S2/post-disabled | 159 / 161.00 | 158.983 / 1.127 | 0.709% | 155,472 | RGBA8888 ×5 | `0000915C00006078` |
| S3/post-enabled | 163 / 165.00 | 163.033 / 1.262 | 0.774% | 155,840 | RGB565 ×2, GRAY8 ×2, ARGB4444 | `0000941F0000043D` |

The fixture hash was identical in every row:
`rgb565-jpeg:000082CE00001B44|rgb565-png:0000BF0E00009512|gray8-jpeg:0000068500002C94|gray8-png:000042BB00000958|argb4444-png:0000ADD900004B73`.
S1/S2 output parity is exact. S2 versus S1 was −1.85% median elapsed and
+9.65% peak RSS, which triggered the required 200-sample escalation. S3 had
no required speedup: its median was +0.62% versus S1 and +2.52% versus S2.

S3 selected all intended compact formats, recorded zero source promotions and
zero temporary full-RGBA decode bytes, and passed the independent quality
oracles: model maximum error 1, ARGB4444 black/white composite maximum error
16/16, and 2,048-byte peak row scratch. The final sample counters were
9,600/7,680/1,920 writePixels attempts/hits/fallbacks (160/128/32 per sample).
The 512×512 fixture backing contract is 2/1/2 bytes per pixel for
RGB565/GRAY8/ARGB4444; the live per-format counters are retained raw because
GC can release earlier sources before a sample is reported.

## Required 200-sample escalation

The preserved full-stack rerun is under `escalation-200/`.

| Run | Median / P95 ms | Mean / SD ms | CV | Peak RSS KiB |
| --- | ---: | ---: | ---: | ---: |
| S1/pre | 162 / 163.00 | 161.605 / 1.311 | 0.811% | 141,408 |
| S2/post-disabled | 161 / 162.05 | 160.665 / 1.405 | 0.874% | 153,568 |

The 200-sample S2 elapsed delta was −0.62%; peak RSS remained +8.60%, so
matched residency diagnostics were required and are under
`matched-diagnostics/`. They captured `ps` and `vmmap -summary` at samples 100
and 150 for both scenarios. Diagnostic-run peak RSS was 148,784 KiB (S1) and
159,456 KiB (S2), but vmmap peak physical footprint was 109.3M (S1) versus
107.7M (S2). Checkpoint physical footprints were S1 77.9M/68.5M and S2
75.8M/85.7M at samples 100/150; writable-resident values were S1 62.7M/69.2M
and S2 78.2M/65.8M. The checkpoint values were not progressively increasing
in S2, and S2's peak physical footprint was lower. The RSS signal is therefore
preserved but classified as unconfirmed allocator/residency variation under
the frozen rule; no runtime correction was needed.

Direct artifacts are the three 60-sample scenario files, the two compressed
200-sample escalation files, their summaries, and the four matched diagnostic
checkpoint files. Full application logs remain under `/tmp` and are not
committed.
