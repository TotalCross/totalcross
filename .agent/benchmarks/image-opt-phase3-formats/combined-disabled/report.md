<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Combined, Phase 2 disabled

Runtime provenance:

- Exact Phase-2 S1 revision/dylib: `86bfeafe388ce866236c3ae58eecb144664895e2` /
  `32926d24c475ca3b6f04134ce4d6556c37d926862d6877d122cdec517213c4ca`.
- Final runtime revision/dylib: `6fcb50a37651597b11388fec611a599576e7841b` /
  `2864d0ee3ace6d52729bcaccad727902088327caa2bafe0769e66cbc2c0a9caa`.
- Harness: benchmark source `84d70321325e095b244a66b6dad127861b65cc2a`, runner
  `6165bf1900a0317a0ed18aed903b2e7b9d59d6aa`, adapter digest
  `9a0bc2a348b197f597a11f10b2ca9d5787ab7b0f2937c70a643e4fc6f09018ae`.

The final post-optimization 60-sample matrix is:

| Run | Median/P95 ms | Peak RSS KiB | Formats | Promotions | Temp RGBA |
| --- | ---: | ---: | --- | ---: | ---: |
| S1 pre | 263/264 | 145424 | RGBA8888 x5 | 0 | 0 |
| S2 disabled | 263/264 | 154352 | RGBA8888 x5 | 0 | 0 |
| S3 enabled | 184/186 | 154304 | RGB565 x2, GRAY8 x2, ARGB4444 | 0 | 0 |

Input hash is `rgb565-jpeg:000082CE00001B44|rgb565-png:0000BF0E00009512|gray8-jpeg:0000068500002C94|gray8-png:000042BB00000958|argb4444-png:0000ADD900004B73`.
S1/S2 output hash is `0000915C00006078`; S3 is `0000941F0000043D`. S3 row
scratch peak is 2,048 bytes and model/composite max errors are 1/16/16.

The matched 200-sample RSS gate was `147904/151504` KiB (+2.4%), within the
frozen threshold. The exact combined S3 format order and zero-promotion/zero-
temporary-decode assertions are enforced per sample by the benchmark harness.
