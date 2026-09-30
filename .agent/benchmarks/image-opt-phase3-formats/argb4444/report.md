<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# ARGB4444

Runtime provenance:

- Exact Phase-2 S1 revision/dylib: `86bfeafe388ce866236c3ae58eecb144664895e2` /
  `32926d24c475ca3b6f04134ce4d6556c37d926862d6877d122cdec517213c4ca`.
- Final runtime revision/dylib: `6fcb50a37651597b11388fec611a599576e7841b` /
  `2864d0ee3ace6d52729bcaccad727902088327caa2bafe0769e66cbc2c0a9caa`.
- Harness: benchmark source `84d70321325e095b244a66b6dad127861b65cc2a`, runner
  `6165bf1900a0317a0ed18aed903b2e7b9d59d6aa`, adapter digest
  `9a0bc2a348b197f597a11f10b2ca9d5787ab7b0f2937c70a643e4fc6f09018ae`.

The original exact-base S1 was `35/35/119728` ms/KiB but used the shorter
pre-correction workload. Matched final-harness S1/S2/S3 each contain 60
samples with median/P95/peak RSS `61/62/126048`, `61/61/136768`, and
`46/47/132112` ms/KiB.

Input hash is `argb4444-png:0000ADD900004B73`. S1/S2 output hash is
`00003EB4000044CE`; S3 output hash is `0000D591000068DE`. S3 selects
`ARGB4444`, stores 524,288 bytes per source, uses a 2,048-byte row scratch
peak, has zero temporary full-RGBA decode bytes and zero promotions, model max
error 0, and black/white composite max error 16.

## Historical single-pair RSS gate

The earlier matched final-harness S1/S2 were `127088/139456` KiB (+9.7%). At sample 100,
`ps` RSS was 98,080/114,208 KiB and at sample 150 it was 100,624/113,744 KiB.
`vmmap -summary` captured the required checkpoints. S2 current physical
footprint was 72.9M/69.3M versus S1 63.6M/62.8M, but S2 peak physical
footprint was 81.3M versus S1 84.4M; the difference was concentrated in
allocator residency. Backing formats, output hashes, and compact counters were
identical. This is recorded as an unconfirmed sampled-RSS signal, not a live
compact-backing leak or confirmed peak-footprint regression.

## Authoritative three-pair matched-harness recheck

The alternating corrective runs are under
`rss-200-corrective-3pairs-matched-harness/`. S2/S1 peak-RSS deltas were
`-0.7%`, `+7.1%`, and `-4.3%`; S2 peak physical-footprint deltas were `-4.9%`,
`-2.6%`, and `+2.0%`. Current physical/private writable residency changed with
run order and allocator/page state. The frozen >5% regression rule therefore
rejects the anomaly as reproducible; runtime source was not changed.
