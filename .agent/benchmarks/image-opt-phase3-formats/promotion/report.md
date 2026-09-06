<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Promotion

Runtime provenance:

- Exact Phase-2 S1 revision/dylib: `86bfeafe388ce866236c3ae58eecb144664895e2` /
  `32926d24c475ca3b6f04134ce4d6556c37d926862d6877d122cdec517213c4ca`.
- Final runtime revision/dylib: `6fcb50a37651597b11388fec611a599576e7841b` /
  `2864d0ee3ace6d52729bcaccad727902088327caa2bafe0769e66cbc2c0a9caa`.
- Harness: benchmark source `84d70321325e095b244a66b6dad127861b65cc2a`, runner
  `6165bf1900a0317a0ed18aed903b2e7b9d59d6aa`, adapter digest
  `9a0bc2a348b197f597a11f10b2ca9d5787ab7b0f2937c70a643e4fc6f09018ae`.

Matched final-harness S1/S2/S3 each contain 60 samples with median/P95/peak RSS
`63/64/133104`, `63/65/146816`, and `60/62/144032` ms/KiB. Input hashes are
`rgb565-jpeg:000082CE00001B44|gray8-jpeg:0000068500002C94|argb4444-png:0000ADD900004B73`;
S1/S2 output hash is `000010F700007F37`, S3 output hash is `00007DF200004CFE`.

S3 selects `RGB565|GRAY8|ARGB4444`, has zero temporary full-RGBA decode bytes,
and records 720 successful promotions for the 60-sample workload. The smoke
matrix separately verifies full-array equality before/after promotion, no extra
promotion after mutation, and failure preservation/retry for all three formats.

The 200-sample matched RSS gate was `153216/153744` KiB (+0.3%), within the
frozen threshold; no checkpoint diagnostic was required.
