<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# M2 variant and eligibility audit

## Scope and package

The diagnostic-only M2 profile ran one accounting-on, prefetch-on process for
each mask `8192`, `16384`, `32768`, and `57344`. No cache policy, default
mask, materialization timing, or shared-slot behavior was changed.

- Corpus: 663 JPEGs, FNV-1a digest `588a7e0f4019424a`.
- Target: software `1080x1920`, rowBytes `4320`, BGRA8888, alpha `2`,
  `kN32=4`.
- SDK JAR SHA-256:
  `4c1d1a70069dae3c8cc5e11b77eb55d69c300793a896726dfb27bf3b8538e711`.
- Native runtime SHA-256:
  `8916c991f85c8ad688351727aba0af60bf8f8f297ae66feef51d559fdfd9c4a0`.
- Bundle ZIP SHA-256:
  `a9fe55d035fd99fe768b801c6f619e1b37741ab3999f8bfc91960edc5a370120`.
- Result ZIP SHA-256:
  `95e4d7c97bb95a12b7ee911a350572bc967f25d6001a7464834e2f1d01df9971`.

## Per-mask observations

| Mask | Target attempts | Target rejects | Physical lookups / misses | Physical full / no-size keys | Identity attempts / mapping rejects |
| ---: | ---: | ---: | ---: | ---: | ---: |
| 8192 | 3 / 3 mapping | 3 | 0 / 0 | 0 / 0 | 0 / 0 |
| 16384 | 0 | 0 | 3 / 3 | 1 / 1 | 0 / 0 |
| 32768 | 0 | 0 | 0 / 0 | 0 / 0 | 3 / 3 |
| 57344 | 3 / 3 mapping | 3 | 3 / 3 | 1 / 1 | 3 / 3 |

Aggregate counters are target attempts `6`, target fallbacks `6`, target
materializations/hits `0/0`, physical lookups/misses `6/6`, physical
materializations/hits/evictions `0/0/0`, and identity attempts/hits/fallbacks
`6/0/6`. All six identity rejections were mapping-geometry rejections. The
target and physical rejection counters recorded no canvas/save, surface,
device-clip, backing, or execution rejection. Physical misses were first
observations of pending keys; no second observation occurred in this one-round
workload.

## Identity and eligibility calculations

- Target full keys / unique sources: `0 / 0` (undefined; no target key became
  eligible for acquisition).
- Target full keys / intrinsic keys: `0 / 0` (undefined for the same reason).
- Physical full keys / no-surface-size keys: `2 / 2 = 1.00`.
- Save-state rejections / identity feature attempts: `0 / 6 = 0%`.
  Across target, physical-variant, and identity attempts together, the same
  numerator is `0 / 18 = 0%`.

The observed zero hits are therefore dominated by eligibility and delayed
observation: target-color acquisition was blocked by mapping geometry, the
physical-variant path observed each key once and did not reach its second
observation, and identity folding fell back on mapping geometry. There is no
observed evidence in this workload for destination-position target-key
fragmentation, target-dimension physical-key fragmentation, cross-kind slot
replacement, or canvas/save-state rejection.

## Raw artifacts

The authoritative per-process `counters.json`, `summary.json`, logs, and
result archive are under:

`build/image-raster-policy-03-package-m2/image-scroll-benchmark-macos-arm64/results`

The runner summary is `results/summary.csv`. This file intentionally stops at
the evidence boundary; exact M3 structural changes require explicit review.
