<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# RGB565

Runtime provenance:

- Exact Phase-2 S1 revision: `86bfeafe388ce866236c3ae58eecb144664895e2`;
  deployed true-base `libtcvm.dylib` SHA-256:
  `32926d24c475ca3b6f04134ce4d6556c37d926862d6877d122cdec517213c4ca`.
- Final runtime revision: `6fcb50a37651597b11388fec611a599576e7841b`;
  deployed `libtcvm.dylib` SHA-256:
  `2864d0ee3ace6d52729bcaccad727902088327caa2bafe0769e66cbc2c0a9caa`.
- Harness: benchmark source tree `84d70321325e095b244a66b6dad127861b65cc2a`,
  runner tree `6165bf1900a0317a0ed18aed903b2e7b9d59d6aa`, true-base adapter
  digest `9a0bc2a348b197f597a11f10b2ca9d5787ab7b0f2937c70a643e4fc6f09018ae`.

The exact true-base S1, S2, and S3 each contain 60 samples. Median/P95/peak
RSS are `62/64/139520`, `63/64/129184`, and `35/36/135408` ms/KiB.
Input hashes are `rgb565-jpeg:000082CE00001B44|rgb565-png:0000BF0E00009512`;
S1/S2 output hash is `00006CE80000D1C2`, and compact S3 output hash is
`0000CACD0000807B`.

S3 selects `RGB565|RGB565`, stores 524,288 bytes per 512x512 source (1,048,576
bytes for the pair), uses a 2,048-byte row scratch peak, and reports zero
temporary full-RGBA decode bytes and zero promotions. Model max error is 1.
