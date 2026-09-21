<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# M4 — reuse and RGB565 versus target color

Implementation HEAD: `b6140c1a2`.

M4 added a three-pass workload while keeping the historical single-pass
default unchanged. The passes are `cold-forward`, `warm-reverse`, and
`warm-forward`; each run records its own frames, summary, counters, memory,
and timeline artifacts. The runner accepts zero activity in a warm pass when
the cold pass exercised the enabled path and the counters remain consistent.

## Target and package gate

The final macOS ARM64 bundle is
`build/image-raster-policy-04-package-m4-corrected2/image-scroll-benchmark-macos-arm64`.
Self-test passed with 663 JPEGs, corpus hash `588a7e0f4019424a`, and SDK JAR
hash `4c1d1a70069dae3c8cc5e11b77eb55d69c300793a896726dfb27bf3b8538e711`.

| field | value |
|---|---|
| logical window | `540x960` |
| physical target | `1080x1920` |
| rowBytes | `4320` |
| target classification | `BGRA8888` |
| target color type | `6` |
| alpha type | `2` |
| renderer | software |
| runtime SHA-256 | `6b5a5d7590b6fdb862a0c1e213c277ba47508d7a4bca557ed9c24c518fe8b84a` |
| bundle ZIP SHA-256 | `8a9ac1c624f2ed96e1c82bc6b316c2b010bb097d4b576cb86aaaa07e3071df05` |

The target is BGRA8888 on this machine. Therefore the 8192 path attempts
target-color conversion toward BGRA8888; it is not evidence of an RGB565
surface target. RGB565 remains the separate source-storage comparison in the
allowed pairwise interpretations.

## Matrix results

| matrix | masks | prefetch | accounting | rounds/processes | pass rows | result ZIP SHA-256 |
|---|---|---|---|---:|---:|---|
| diagnostic | `0,32,8192,8224` | off/on | on | `1 / 8` | `24` | `f0a8ff44d4d5c5d60ea8fcd77a4574027ab72e102130754cbdac232d0e98a2a5` |
| performance | `0,32,8192,8224` | off/on | off | `3 / 24` | `72` | `80a0ff53aa2533161d6c8eb1e657dcd53d57bab6914995a06d1007a5a3bb3f7a` |

Both matrices passed. Automatic scrolling reached the validated endpoint for
all 32 processes, and all 96 pass records were validated in the order above.
Independent inspection of every `frames.csv` proved 24/24 diagnostic and
72/72 performance trajectories: forward passes start at `0`, end at `39091`,
and are non-decreasing; reverse passes start at `39091`, end at `0`, and are
non-increasing. Every pass has multiple frames and covers the full range.
The complete per-pass work P50/P95/P99/MAX, paint P50/P95/P99/MAX, process and
round identity, lifecycle counters, bytes, target classification, key
multiplicity, and shared-slot fields are in:

- `results/m4-reuse-passes.csv` — 24 diagnostic rows or 72 performance rows;
- `results/m4-reuse-pairwise.csv` — 24 diagnostic or 72 performance pairwise
  rows for the four allowed comparisons.

The files are preserved inside the corresponding result ZIPs. The diagnostic
CSV hashes are `6f92250676a3b4d4ac0ebe2cc28e3e182381dd56c17af11baee2c79e1f038cec`
and `134dfa4b289de98265ad4d0a37e52d609f3e20e41060cfee623b942829847fa6`.
The final performance CSV hashes are
`d4100bc0245ac4831cb173c404c6080fddef6350e73b86c6dd78cc2f9e0f7eeb` and
`982f172e9efcf89c407347d44abb9b976ee8c8ec5e3ef45ca0cf3f394b2816f1`.

## Measured lifecycle and reuse behavior

Across the accounting-on diagnostic matrix for masks 8192 and 8224,
target-color counters summed to `1002` attempts, `1002` fallbacks, zero hits,
zero target-color materializations, and zero converted bytes. The observed
BGRA8888 target explains the fallback classification; this is not a causal
performance claim. Target-color pending replacements and all shared-slot
transitions/pending replacements were zero.

The largest scoped target-key set observed `195` source keys and `195` full,
no-destination, intrinsic, and acquisition keys. The runner retained the
source-scoped containment checks, so these counts are diagnostic multiplicity,
not active-cache identity claims. Accounting-on raster bytes ranged from
`216605032` to `763997392` across the per-pass artifacts.

Prefetch-on warm passes for masks 8192 and 8224 recorded zero target-color
attempts and zero image/native-geometry materializations after the cold pass.
This is the intended reuse observation. Prefetch-off passes still exposed
lazy materialization work, so the warm-pass counters are reported rather than
collapsed into a single reuse number.

The performance pairwise file contains 72 rows: 15 were classified
`CONSISTENT_DIRECTION` and 57 were `INCONCLUSIVE_VARIANCE`. The plan permits
only `0->32`, `0->8192`, `32->8224`, and `8192->8224`; no other comparison was
interpreted, and no timing-only causal claim was promoted.

## Validation and boundary

Passed:

- direct Java compilation of the three smoke-test sources against the SDK JAR;
- Python syntax compilation and `git diff --check`;
- package deployment and bundle self-test;
- M4 diagnostic matrix and aggregation;
- M4 performance matrix and aggregation;
- focused copyright-header validation and signed commit-message audit.

A full `./gradlew-agent dist` rebuild was deferred because the SDK JAR was
unchanged; the package script compiled and deployed the changed smoke app
against that existing JAR. No defaults, delayed materialization policy, or
shared raster-variant slot changed.

`STOP / REVIEW 4` is ready. Further optimization, RGB565 default changes,
slot splitting, and cross-platform generalization remain outside this gate.
