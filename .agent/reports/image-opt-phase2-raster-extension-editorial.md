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
