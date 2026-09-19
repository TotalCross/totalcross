<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# M2 variant and eligibility audit

## Scope and package

The diagnostic-only M2 profile ran one accounting-on, prefetch-on process for
each mask `8192`, `16384`, `32768`, and `57344`. No cache policy, default
mask, materialization timing, or shared-slot behavior was changed.

The review-correction implementation commit was `ca71cbe0b`.

- Corpus: 663 JPEGs, FNV-1a digest `588a7e0f4019424a`.
- Target: software `1080x1920`, rowBytes `4320`, BGRA8888, alpha `2`,
  `kN32=4`.
- SDK JAR SHA-256:
  `4c1d1a70069dae3c8cc5e11b77eb55d69c300793a896726dfb27bf3b8538e711`.
- Native runtime SHA-256:
  `ead97b59f0a3839e549f76a0afdf770be070ceee70f831145bbbc4f00483254f`.
- Bundle ZIP SHA-256:
  `e2b97f8d8f7d125f8937d2339007661f6653aa7f654eb4d9aa55787b632c3f7f`.
- Result ZIP SHA-256:
  `b94034944425e7a0e83b307062c36e48cc960407fbe7dba7505419b4d0515433`.

## Per-mask observations

| Mask | Target attempts | Target rejects | Physical lookups / misses | Physical full / no-size keys | Identity attempts / mapping rejects |
| ---: | ---: | ---: | ---: | ---: | ---: |
| 8192 | 3 / 3 mapping | 3 | 0 / 0 | 0 / 0 | 0 / 0 |
| 16384 | 0 | 0 | 3 / 3 | 1 / 1 | 0 / 0 |
| 32768 | 0 | 0 | 0 / 0 | 0 / 0 | 3 / 3 |
| 57344 | 3 / 3 mapping | 3 | 3 / 3 | 1 / 1 | 3 / 3 |

Aggregate counters are target attempts/fallbacks/materializations/hits
`6/6/0/0`, physical lookups/misses/materializations/hits/evictions
`6/6/0/0/0`, and identity attempts/hits/fallbacks `6/0/6`. All six target and
identity mapping rejections were classified as `root-to-device`; physical
variants had no mapping rejection. The target and physical rejection counters
recorded no canvas/save, surface, device-clip, backing, or execution rejection.
Physical misses were first observations of pending keys; no second observation
occurred in this one-round workload.

## Identity and eligibility calculations

- Target full keys / unique sources: `0 / 0` (undefined; no target key became
  eligible for acquisition).
- Target full keys / intrinsic keys: `0 / 0` (undefined for the same reason).
- Physical full keys / no-surface-size keys: `2 / 2 = 1.00`.
- Save-state rejection buckets are separated by feature. Target-color,
  physical-variant, and identity buckets are all `[0, 0, 0, 0, 0, 0]` for
  `saveCount` `0`, `1`, `2`, `3`, `4`, and `5+`; the observed ratios are
  `0/6`, `0/6`, and `0/6`, respectively.
- The published M2 diagnostic contract omits `PARTIAL_INTERSECTION`: this
  workload has no real classified partial-intersection condition. The legacy
  native enum value remains only for compatibility and is not emitted.
- Mapping subreason totals are target-color `root-to-device=6`,
  physical-variant none, and identity `root-to-device=6`; the other nine
  subreason buckets are zero.

The observed zero hits are therefore dominated by eligibility and delayed
observation: target-color acquisition was blocked by root-to-device mapping,
the physical-variant path observed each key once and did not reach its second
observation, and identity folding fell back on the same mapping condition.
There is no observed evidence in this workload for destination-position
target-key fragmentation, target-dimension physical-key fragmentation,
cross-kind slot replacement, or canvas/save-state rejection.

## Raw artifacts

The authoritative per-process `counters.json`, `summary.json`, logs, and
result archive are under:

`build/image-raster-policy-03-package-m2-review/image-scroll-benchmark-macos-arm64/results`

The runner summary is `results/summary.csv`. This file intentionally stops at
the evidence boundary; exact M3 structural changes require explicit review.
