<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Integrated Phase-2 raster matrix

This workload exercises two lossless decodes (PNG and JPEG), 1,024 eligible
opaque draws, a full pixel readback, PNG encoding, and direct APPLY_COLOR2
materialization in every timed sample. Hashing occurs after timing and covers
all three outputs: target pixels, encoded PNG bytes, and color-materialized
pixels.

S1 uses the true Phase-1 runtime at
`9545c18207fab74d81340b24825c5a82ddbda7fd`. S2 and S3 use final Phase-2
runtime code `b15aca03291e345186f0c3364eabfc8631163262`. The final harness is
committed at `b68f6cd28` and has digest
`2576fca4c8c5a831058d654f09d7f24b2f1af0dc85ff7dbccb087f34708bd913`; the
S1 base-worktree adaptation digest is
`824b665ab9459787ad788d51d305bc965adaa62c0f437b43dd044c8c8b86df12`.

All scenarios used macOS Release software Skia, three warmups, 60 samples,
RSS sampling every 50 ms, and a 30 ms minimum sample floor. S2 explicitly
disabled all five Phase-2 features. S3 enabled exactly these five:
`DECODE_ZERO_COPY`, `RASTER_OPACITY_METADATA`,
`RASTER_OPAQUE_WRITE_PIXELS`, `RASTER_ROW_READBACK`, and
`RASTER_DIRECT_COLOR_MATERIALIZATION`. Future storage/cache/GPU/mmap features
were explicitly disabled.

| Scenario | Median | P95 | Peak RSS | S1→S2 / S2→S3 median | S1→S2 / S2→S3 P95 | S1→S2 / S2→S3 RSS |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| S1 true base | 877 ms | 881 ms | 122,688 KiB | — | — | — |
| S2 final, all disabled | 878 ms | 884 ms | 131,296 KiB | +0.1% / — | +0.3% / — | +7.0% / — |
| S3 final, all enabled | 56 ms | 62 ms | 140,352 KiB | — / -93.6% | — / -93.0% | — / +6.9% |

| Output | S1 hash | S2 hash | S3 hash | Equality |
| --- | --- | --- | --- | --- |
| Target pixels | `6C3E43B0` | `6C3E43B0` | `6C3E43B0` | exact |
| Encoded PNG bytes | `675B5B57` | `675B5B57` | `675B5B57` | exact |
| Color-materialized pixels | `2DBAC894` | `2DBAC894` | `2DBAC894` | exact |

All 60 samples in each scenario had stable hashes and elapsed times above the
floor; S1/S2/S3 therefore provide exact output parity for this workload.

The final counter row proves that S3 exercised every target optimization:

| Counter | S1 | S2 | S3 |
| --- | ---: | ---: | ---: |
| Decode zero-copy operations | 0* | 0 | 120 |
| Opacity source proofs | 0* | 0 | 60 |
| Opacity decode proofs | 0* | 0 | 60 |
| `writePixels` attempts / hits / fallbacks | 0* / 0* / 0* | 0 / 0 / 0 | 61,440 / 61,440 / 0 |
| Row readbacks | 0* | 0 | 92,160 |
| Full readbacks | 0* | 120 | 0 |
| Direct color materializations | 0* | 0 | 60 |

`*` The base runtime predates these Phase-2 counters; the S1 harness adapter
reports zero for unavailable counters and does not alter the measured runtime
path. S3 counter values are native runtime counters.

Raw files are `s1.csv`, `s2.csv`, `s3.csv` and their matching summaries.
