<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Phase 2 raster extension handoff

## Status

Extension 01 has completed its rebaseline milestone from the rebased Phase 2
tip. The new workloads are committed, all required macOS/SDK gates passed, and
the existing five controls have additive S1/S2/S3 measurements. No new raster
behavior is claimed yet. Historical Phase 2 evidence remains authoritative and
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
