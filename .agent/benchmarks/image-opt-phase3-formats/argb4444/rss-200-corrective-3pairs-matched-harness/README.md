<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# ARGB4444 corrective RSS recheck

This is the authoritative three-pair recheck of the prior ARGB4444 RSS signal.
The order is deliberately alternating: pair 1 `S1 -> S2`, pair 2 `S2 -> S1`,
pair 3 `S1 -> S2`. Every case uses 200 samples, three warmups, the same
ARGB4444 fixture, `--workload=argb4444`, `--phase2=false`, and the existing
benchmark runner with 50 ms RSS sampling.

S1 uses the exact Phase-2 runtime revision
`86bfeafe388ce866236c3ae58eecb144664895e2` and true-base dylib SHA-256
`32926d24c475ca3b6f04134ce4d6556c37d926862d6877d122cdec517213c4ca`.
Its benchmark app/support are the corrected final workload deployed through the
Phase-2 conservative hook adapter; the overlay harness digest is
`edd9a79ebb30d081681b141a245a8525788b74df4a55712113ecf7a9c2335d7d`.
The deployed S1 application bundle SHA-256 is
`4108b6a268ee682013c435a44fb0787f9e86e253a92f8dd1326d5cb50ecb4940`.

S2 uses final Phase-3 runtime revision
`6fcb50a37651597b11388fec611a599576e7841b` and dylib SHA-256
`2864d0ee3ace6d52729bcaccad727902088327caa2bafe0769e66cbc2c0a9caa`.
The final runner tree is `6165bf1900a0317a0ed18aed903b2e7b9d59d6aa`, the
benchmark source tree is `84d70321325e095b244a66b6dad127861b65cc2a`, and the
deployed S2 application bundle SHA-256 is
`afd964e9ecac8d340ac066d5854b995547ab04cf3a9ffafa5d821e375f257845`.

The fixture input hash is `argb4444-png:0000ADD900004B73`; every case produced
`RGBA8888`, output hash `00003EB4000044CE`, and zero temporary RGBA decode bytes.

| Pair | S1 median/P95/peak KiB | S2 median/P95/peak KiB | S2 peak delta |
| --- | ---: | ---: | ---: |
| 1, S1 -> S2 | 61/62/120608 | 61/61/119792 | -0.7% |
| 2, S2 -> S1 | 61/62/121440 | 61/62/130080 | +7.1% |
| 3, S1 -> S2 | 61/62/134336 | 61/61/128512 | -4.3% |

`vmmap -summary` and `ps` were captured at samples 100 and 150 for both cases
in every pair. S2 peak physical footprint versus S1 was −4.9%, −2.6%, and
`+2.0%`; current physical/private writable residency changed with run order
and page/allocator state. The >5% signal is therefore rejected as a
reproducible Phase-3 disabled-path regression under the frozen rule. Runtime
source was not changed.
