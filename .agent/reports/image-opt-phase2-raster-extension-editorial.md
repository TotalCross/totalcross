<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Phase 2 raster extension handoff

## Status

Extension 01 has completed physical identity folding from the rebased Phase 2
tip. The new workloads are committed, all required macOS/SDK gates passed, and
the existing five controls have additive S1/S2/S3 measurements. ID 15 is
committed and validated; historical Phase 2 evidence remains authoritative and
unchanged.

## Scope

This continuation will add exact physical-identity folding, then hand off to
target-color conversion and bounded physical-variant reuse in extensions 02 and
03. Only SDK and macOS software-Skia builds are permitted by the execution
plans; other platform results will be recorded as deferred where applicable.

## Milestone 1 evidence

The S1/S2/S3 integrated workload produced stable pixel, PNG, and color hashes.
The measurements below use the protocol's three warmups and timed batches of
1024 draws.

| Scenario | Runtime | Samples | Median | P95 | CV | Peak RSS |
| --- | --- | ---: | ---: | ---: | ---: | ---: |
| S1 true base | `a8a9480...` | 60 | 876 ms | 878 ms | 0.12% | 124016 KiB |
| S2 disabled | `25a43c0...` | 60 | 877 ms | 879 ms | 0.16% | 128352 KiB |
| S3 controls 0-4 | `25a43c0...` | 200 | 59 ms | 63 ms | 4.22% | 132816 KiB |

S2 is within the required elapsed and RSS regression boundary versus S1.
S3's final counters were decode zero-copy 400, opacity-known 200,
opacity-determined 200, writePixels attempts/hits 204800/204800, row readbacks
307200, full readbacks 0, and direct color materializations 200. The 60-sample
S3 escalation trigger and the authoritative 200-sample samples are both
preserved under the extension benchmark directory.

The physical-identity disabled control at the current production revision was
captured separately: 60 samples, median 5629 ms, P95 5643 ms, CV 0.20%, and
peak RSS 114208 KiB. Its source and target physical dimensions were both
200x200.

Focused macOS smokes passed for adaptive JPEG tiers and targeted-decode retry,
APPLY_COLOR2 and mutation parity, zero-copy ownership/retry, opacity
invalidation, writePixels parity, geometry/readback parity, and native
materialization. Full logs remain in the SDK agent-log directory and `/tmp`
task logs; binaries and deploy outputs are not committed.

## Milestone 2 evidence

ID 15 was implemented in production revision
`31c3d0fa40d955806df18e1dad1ba79fa301a606`. The ephemeral native
`RasterPhysicalPlan` proves exact software-raster physical identity from the
existing compiled geometry. The fallback contract remains active for
non-identity dimensions, alpha/save state, and dynamic hardware scale; crop
and frame-selection identity cases also passed.

| Scenario | Samples | Median | P95 | CV | Peak RSS | Result |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| ID15 S2 disabled | 60 | 5483 ms | 5516 ms | 0.62% | 114080 KiB | counters zero |
| ID15 S3 enabled | 60 | 1060 ms | 1062 ms | 0.21% | 115728 KiB | 1024 hits/batch |

Both S2 and S3 produced pixel hash `000000D600000165`; S3 avoided 1024
resamples per timed batch. The 60-sample runs are authoritative because both
CVs remained below the 5% escalation threshold. The exact runtime revision for
extension 02's target-color and physical-variant S1 is the final extension 01
production revision recorded above.

The required runtime commit's body contained an overlong original line and
the local commit-message check reported it; no history rewrite was performed.

## Extension 02 S1 handoff

Extension 01's final handoff HEAD is `de5ad089e68e39a1224a87247aad026e6d31baab`.
The final pre-ID13/14 production and harness revision is
`07912a0f2d5d48705a5b7caa65f82a71ed7d3b57`; it includes only test-raster
factory registration corrections on top of the ID 15 runtime.

The target-color RGBA control completed 60 samples at a 5477 ms median, 5493
ms P95, 0.22% CV, and 115296 KiB peak RSS. The physical-variant repeat control
completed 60 samples at a 1326 ms median, 1328 ms P95, 0.13% CV, and 114160 KiB
peak RSS. Both produced `000000D600000165`. One-sample focused probes for
BGRA, RGB565, translucent, first-use, mutation, and size cases also passed;
their hashes are preserved in the additive benchmark directories.

## Extension 02 Milestone 1 — target-color conversion

ID 13 is committed in `37f489600449c81c4ead3eb677a01ec35d12112f`. The native
implementation keeps the source backing authoritative and owns one target-aware
Skia raster slot per source backing. It admits the slot on the second identical
eligible draw, reuses it on later draws, and clears it on source-root mutation.
RGBA is the control representation; BGRA8888 and opaque RGB565 use Skia's
conversion path, while translucent content falls back.

| Scenario | Samples | Median | P95 | CV | Peak RSS | Result |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| S1 RGBA control | 60 | 5477 ms | 5493 ms | 0.221% | 115296 KiB | frozen pre-ID13 |
| S2 RGBA disabled | 60 | 5629 ms | 5646 ms | 0.178% | 113280 KiB | zero counters |
| S3 RGBA enabled | 60 | 5642 ms | 5648 ms | 0.133% | 115744 KiB | control path |
| S2 BGRA disabled | 60 | 5667 ms | 5677 ms | 0.128% | 114240 KiB | zero counters |
| S3 BGRA enabled | 60 | 1083 ms | 1089 ms | 0.329% | 109184 KiB | 1 materialization, 64510 hits |

All RGBA and BGRA timed hashes were stable. The exact RGBA S2 median delta was
2.775% versus S1 and its RSS delta was -1.748%; no 200-sample escalation was
required. Focused smokes additionally passed RGB565 conversion, translucent
fallback, source-root mutation invalidation, BGRA-to-RGB565 replacement,
disabled parity, and canonical ARGB readback.

The raw samples and compact result record are under
`.agent/benchmarks/image-opt-phase2-raster-extension/target-color-s2-60/`,
`target-color-s3-60/`, and `target-color-s2-s3-60/results.txt`. Full SDK agent
logs remain uncommitted in `TotalCrossSDK/agent-logs/`.

## Extension 02 Milestone 2 — physical variant cache

ID 14 is committed in `c6515a8f0`. It reuses the ID13 source-owned slot for
bounded CPU raster variants. The key is built from explicit plan scalars,
exact floating-point bit values, operation parameters, dimensions, source
generation/decode generation, target dimensions, and target color type. The
admission policy is exactly two observations: first draw records the key,
second materializes, subsequent draws hit. Mutation clears the slot and
pending key; key changes replace one materialized candidate at a time.

| Scenario | Samples | Median | P95 | CV | Peak RSS | Result |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| S1 true pre-ID14 | 60 | 1326 ms | 1328 ms | 0.129% | 114160 KiB | frozen pre-ID14 |
| S2 ID14 disabled | 60 | 1323 ms | 1334 ms | 0.329% | 116608 KiB | zero feature path |
| S3 ID14 enabled | 60 | 263 ms | 265 ms | 0.280% | 116320 KiB | stable physical variant reuse |

All hashes were `000000D600000165`. S2/S3 median elapsed deltas versus S1
were -0.226%/-80.166%; matched RSS deltas were +2.144%/+1.892%. The CV
threshold stayed below 5%, so no 200-sample escalation was needed.

Focused smokes passed repeat, identity precedence, replacement/eviction,
mutation invalidation, encoded-root decode-generation invalidation, crop, alpha,
rotation, hardware-scale, disabled parity,
and ID13 BGRA/translucent compatibility. The combined ID13+ID14 BGRA smoke
passed with one physical materialization, one hit, no duplicate target-color
materialization, and stable output parity. The implementation reports bounded
bytes (repeat160000, replacement307456, mutation160000) and no extra backing
record for the derived variant. Raw samples are under
`.agent/benchmarks/image-opt-phase2-raster-extension/physical-variant-s2-60/`
and `physical-variant-s3-60/`; the compact record is
`physical-variant-s2-s3-60/results.txt`.

The physical-variant benchmark uses the exact pre-ID14 production/harness
revision `07912a0f2d5d48705a5b7caa65f82a71ed7d3b57` and runtime revision
`c6515a8f0`. Full SDK agent logs remain uncommitted.

## Extension 02 Milestone 3 — handoff to integrated closeout

Extension 02 is complete. Native runtime code is frozen at `c6515a8f0`; the
combined-target and encoded-root decode-generation smoke follow-ups are
`4b5de2931` and `2ff711ffe`, and the physical-variant evidence is committed in
`8db53eab3`. The final extension-02 branch tip before this documentation
handoff is `2ff711ffe`; no native runtime code changed after `c6515a8f0`.

The target-color and physical-variant S1 controls both use production/harness
revision `07912a0f2d5d48705a5b7caa65f82a71ed7d3b57`. Their 60-sample S2/S3
records, exact hashes, focused counters, RSS diagnostics, and source/decode
invalidation evidence are preserved in the additive benchmark directories.
The next sequential plan is
`.agent/plans/exec-plan-image-opt-phase2-raster-extension-03.md`, which must
freeze the branch tip it starts from and distinguish final Phase 2 behavior
from the later Phase 3 compact-source work.
