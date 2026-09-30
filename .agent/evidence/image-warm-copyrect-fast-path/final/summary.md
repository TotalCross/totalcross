<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Final warm copyRect evidence

## Real workload matrix

- Runtime SHA-256: `6d2ae5b79c6c886079190657940345b9d55e0502a0af29777b844aac9e2aeab9`.
- Corpus: 663 JPEGs, 480x720 and 540x960, four target/variant profiles, three
  passes per profile; all 24 runs passed.
- Warm targeted/full JPEG decodes were zero for every profile. Warm smooth
  resample draws were also zero.
- Warm p95 frame times were 9/9/33/34 ms for the four 480x720 profiles and
  9/11/49/50-51 ms for the four 540x960 profiles. The default disabled/
  disabled profiles matched the baseline at 9 ms p95 for both resolutions.
- The customer fixture exercised the new draw-plan path while unsupported
  physical proofs used the existing materialized fallback, preserving the
  warm behavior and avoiding generic smooth resampling.

Artifact: `results.csv` and the eight per-profile logs in this directory.

## Warm microbenchmark

All six materialized/deferred full and partial cases passed with matching
pixel hashes:

| clip | materialized | deferred drawImage | deferred copyRect |
| --- | ---: | ---: | ---: |
| full | 4.0 ms | 4.1 ms | 3.9 ms |
| partial | 1.9 ms | 1.7 ms | 1.8 ms |

Artifacts: `warm-micro.log` and `summary.md`.

## Direct-copy and fallback smoke coverage

- Physical identity smoke: 3,072 attempts, 3,072 hits, zero fallbacks, and
  3,072 avoided resamples across three samples.
- Physical guard smoke: 1,024 attempts and hits, zero fallbacks.
- copyRect draw-plan smoke passed full, clipped, no-intersection, frame,
  unsupported-fallback, and feature-15 default/disabled assertions.

The committed smoke artifacts are `physical-identity-smoke.log`,
`physical-guards-smoke.log`, and `copyrect-smoke.log`.
