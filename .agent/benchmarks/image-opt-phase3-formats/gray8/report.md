<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# GRAY8

Runtime provenance:

- Exact Phase-2 S1 revision/dylib: `86bfeafe388ce866236c3ae58eecb144664895e2` /
  `32926d24c475ca3b6f04134ce4d6556c37d926862d6877d122cdec517213c4ca`.
- Final runtime revision/dylib: `6fcb50a37651597b11388fec611a599576e7841b` /
  `2864d0ee3ace6d52729bcaccad727902088327caa2bafe0769e66cbc2c0a9caa`.
- Harness: benchmark source `84d70321325e095b244a66b6dad127861b65cc2a`, runner
  `6165bf1900a0317a0ed18aed903b2e7b9d59d6aa`, adapter digest
  `9a0bc2a348b197f597a11f10b2ca9d5787ab7b0f2937c70a643e4fc6f09018ae`.

The original exact-base S1 was `58/60/151584` ms/KiB but used the shorter
pre-correction workload. The matched final-harness S1/S2/S3 each contain 60
samples with median/P95/peak RSS `111/112/160064`, `110/111/162992`, and
`48/50/151488` ms/KiB.

Input hashes are `gray8-jpeg:0000068500002C94|gray8-png:000042BB00000958`.
S1/S2/S3 output hash is `00005D680000106C`. S3 selects `GRAY8|GRAY8`, stores
262,144 bytes per source (524,288 bytes for the pair), uses a 2,048-byte row
scratch peak, has zero temporary full-RGBA decode bytes and zero promotions,
and is exact against the reference model (max error 0).
