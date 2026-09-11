<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Complete diagnostic accounting gate

This corrective item preserves the original control-plumbing report at
`.agent/benchmarks/image-opt-phase1-controls/control-plumbing/report.md` and
measures the missing Java readback and native Skia backing gates separately.

## Revisions and regime

| Scenario | Revision | Configuration |
| --- | --- | --- |
| S1 / pre | `f33760435572d6ef5a1ff73cd33a21ca3d127f13` | pre-fix Phase 1 workload |
| S2 / post-disabled | `8399b8b0af37a154c3cc460c16766c5e329dd659` | corrected code, diagnostic accounting disabled |
| S3 / post-enabled | `8399b8b0af37a154c3cc460c16766c5e329dd659` | corrected code, only diagnostic accounting enabled |

All scenarios use the deterministic 96x72 PNG fixture, 3 warmup batches, 256
cached draws, 32 image churn operations, and one timed 16x16 native backing
create/readback/release probe per sample. Each has 60 measured samples and
external 50 ms RSS sampling. The build is macOS Release with software
graphics, Skia, and SDL:

```text
cmake -S TotalCrossVM -B build/image-opt-phase1-corrective-macos -DCMAKE_BUILD_TYPE=Release -G Ninja -DTC_GRAPHICS_SOFTWARE=ON -DTC_RENDERER_SKIA=ON -DTC_WINDOWING_SDL=ON
ninja -C build/image-opt-phase1-corrective-macos tcvm Launcher
```

Machine: Darwin 25.5.0, macOS 26.5.2, arm64, 17,179,869,184 bytes RAM.

## Timing and RSS

| Scenario | Median ms | P95 ms | Mean ms | Stddev ms | CV | Peak RSS KB |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| S1 / pre | 676 | 678 | 676.23 | 1.17 | 0.173% | 115712 |
| S2 / post-disabled | 677 | 680 | 677.53 | 1.62 | 0.239% | 116128 |
| S3 / post-enabled | 671 | 673 | 671.25 | 1.17 | 0.175% | 116112 |

S2 versus S1 is +0.148% median and +0.360% peak RSS. S3 versus S1 is
-0.740% median and +0.346% peak RSS. No comparison approached the protocol's
5% escalation boundary, so 200-sample reruns were not required.

## Accounting results

| Diagnostic fields | S1 / pre | S2 / post-disabled | S3 / post-enabled final |
| --- | ---: | ---: | ---: |
| Java image / pipeline / draw-plan created | 0 / 0 / 0 | 0 / 0 / 0 | 5752 / 3832 / 1920 |
| Java draw-plan hits / direct draws | 0 / 0 | 0 / 0 | 15360 / 17280 |
| Java backing readbacks | 60 | 0 | 60 |
| Native backing created / released | 60 / 60 | 0 / 0 | 60 / 61 |
| Native backing live / peak live | 2 / 3 | 0 / 0 | 2 / 4 |
| Native backing live bytes / peak bytes | 334848 / 335872 | 0 / 0 | 334848 / 363520 |

S2 proves the complete gate: every emitted diagnostic field is zero. S3 proves
that enabling the same gate restores Java accounting, backing readback
accounting, and native create/release accounting. The pre-fix S1 result shows
the original defect: Java counters were gated, but backing readback and native
backing counters still incremented.

## Validation and artifacts

Passed: focused `totalcross.ui.image.*` tests; SDK `dist -x test`; macOS
Release CMake/Ninja; exact-dylib deployment; existing
`runImageNativeGeometryMacOS` and `runImageDrawPresentationStateMacOS` smokes;
and the three 60-sample process runs with exit/sample-count checks.

Raw samples and runner summaries are under the phase-generic contract path:
`.agent/benchmarks/<plan>/<item>/scenario-{1,2,3}.csv`, instantiated here as
`.agent/benchmarks/image-opt-phase1-controls/complete-diagnostic-gating/`.
Verbose logs remain under the ignored
`artifacts/image-opt-phase1-controls/` directory. Results are local evidence
from one macOS arm64 machine; other platforms remain outside Phase 1.
