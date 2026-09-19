<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# M2 variant and eligibility audit

## Scope and package

The diagnostic-only M2 profile ran one accounting-on, prefetch-on process for
each mask `8192`, `16384`, `32768`, and `57344`. No cache policy, default
mask, materialization timing, or shared-slot behavior was changed.

The source-scoping implementation commit was `f4dab5e23`.

- Corpus: 663 JPEGs, FNV-1a digest `588a7e0f4019424a`.
- Target: software `1080x1920`, rowBytes `4320`, BGRA8888, alpha `2`,
  `kN32=4`.
- SDK JAR SHA-256:
  `4c1d1a70069dae3c8cc5e11b77eb55d69c300793a896726dfb27bf3b8538e711`.
- Native runtime SHA-256:
  `7730aff3bf03272396bdd67d4f752d70e00ec9993851fa1685c2109de5182845`.
- Bundle ZIP SHA-256:
  `a261d3f6f60a2b4074bb8c6f6979d201b03cc2aa443145d7c979a28381663b89`.
- Result ZIP SHA-256:
  `72f5602092befc2aae0d4937dc77939ecc9db3587ed3fac24d45a88cdca332eb`.

## Per-mask observations

| Mask | Target attempts / unique sources | Target rejects | Physical lookups / misses | Physical sources / full / no-size keys | Identity attempts / mapping rejects |
| ---: | ---: | ---: | ---: | ---: | ---: |
| 8192 | 3 / 3 | 3 mapping | 0 / 0 | 0 / 0 / 0 | 0 / 0 |
| 16384 | 0 / 0 | 0 | 3 / 3 | 3 / 3 / 3 | 0 / 0 |
| 32768 | 0 / 0 | 0 | 0 / 0 | 0 / 0 / 0 | 3 / 3 |
| 57344 | 3 / 3 | 3 mapping | 3 / 3 | 3 / 3 / 3 | 3 / 3 |

Aggregate counters are target attempts/fallbacks/unique sources/acquisition
sources `6/6/6/0`, target full/no-destination/intrinsic keys `0/0/0`, physical
lookups/misses/unique sources/full/no-surface-size keys `6/6/6/6/6`, physical
materializations/hits/evictions `0/0/0`, and identity attempts/hits/fallbacks
`6/0/6`. All six target and identity mapping rejections were classified as
`root-to-device`; physical variants had no mapping rejection. The target and
physical rejection counters recorded no canvas/save, surface, device-clip,
backing, or execution rejection. Physical misses were first observations of
pending keys; no second observation occurred in this one-round workload.

## Identity and eligibility calculations

- Target requests / unique sources: `6 / 6`; target acquisition sources: `0`.
  Full/no-destination/intrinsic key ratios are `0/0`, `0/0`, and `0/0`, all
  undefined because eligibility rejected every target request before
  acquisition.
- Physical unique sources / full keys / no-surface-size keys are `6 / 6 / 6`;
  full-to-source and no-surface-size-to-source ratios are both `1.00`.
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
the physical-variant path observed three source-scoped keys per target-enabled
run but did not reach a second observation, and identity folding fell back on
the same mapping condition. Source-scoping changed the physical key count from
the prior collapsed `2/2` aggregate to the corrected `6/6`; it does not change
the conclusion because no second observation occurred. There is no observed
evidence in this workload for destination-position target-key fragmentation,
target-dimension physical-key fragmentation, cross-kind slot replacement, or
canvas/save-state rejection.

## Raw artifacts

The authoritative per-process `counters.json`, `summary.json`, logs, and
result archive are under:

`build/image-raster-policy-03-package-m2-scoped/image-scroll-benchmark-macos-arm64/results`

The runner summary is `results/summary.csv`. This file intentionally stops at
the evidence boundary; exact M3 structural changes require explicit review.
