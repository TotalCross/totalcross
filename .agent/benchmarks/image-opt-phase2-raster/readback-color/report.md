<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Raster readback and color materialization

## Scope

This report covers `RASTER_ROW_READBACK` and
`RASTER_DIRECT_COLOR_MATERIALIZATION`. The benchmark uses the existing
1960x1960 JPEG fixture and the macOS software-Skia build. Each scenario
explicitly controls all phase-2 raster optimization toggles, and each run uses
three warmups followed by 60 measured samples with 50 ms RSS sampling.

## S1 baseline

Revision: `8f52cfdf92e621d2768687ca6acba9ce33c59740` (`test(image): record
opaque raster copy results`). All phase-2 optimization toggles were disabled.
The benchmark was run sequentially for `pixels`, `encode`, and `color`.

| Operation | Median / P95 (ms) | Peak RSS (KiB) | Output bytes | Checksum |
| --- | ---: | ---: | ---: | ---: |
| `pixels` | 23 / 23 | 177856 | 15366400 | 1051912184 |
| `encode` | 463 / 465 | 191952 | 7181039 | 7181039 |
| `color` | 37 / 39 | 178160 | 15366400 | 434415212 |

The S1 artifacts are the three `scenario-1-*.csv` files and their compact
summaries in this directory. They were collected before enabling either
readback optimization. The disabled implementation used full-image readback;
the implementation instrumentation was added after this baseline.

## S2/S3

Revision: `ee7b9005165e299cb2af1ca5f3e6ab2e810bf534` (`perf(image): batch
native row readback`). S2 kept both target features disabled. S3 enabled
`RASTER_ROW_READBACK` for `pixels` and `encode`, and
`RASTER_DIRECT_COLOR_MATERIALIZATION` for `color`.

| Scenario | Operation | Median / P95 (ms) | Peak RSS (KiB) | Checksum | Row reads | Full reads | Row scratch | Full scratch | Direct color |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| S2 | `pixels` | 23 / 23 | 177632 | 1051912184 | 0 | 63 | 0 | 15366400 | 0 |
| S2 | `encode` | 462 / 464 | 191520 | 7181039 | 0 | 0 | 0 | 0 | 0 |
| S2 | `color` | 37 / 37 | 177680 | 434415212 | 0 | 63 | 0 | 15366400 | 0 |
| S3 | `pixels` | 22 / 22 | 147216 | 1051912184 | 123480 | 0 | 7840 | 0 | 0 |
| S3 | `encode` | 462 / 464 | 191664 | 7181039 | 123480 | 0 | 7840 | 0 | 0 |
| S3 | `color` | 37 / 38 | 177984 | 434415212 | 0 | 63 | 0 | 15366400 | 63 |

The counters include three warmups plus 60 measured samples. `pixels` and
`color` each perform one final checksum readback per invocation; the S3 color
full-readback counter is that verification consumer, not scratch allocated by
the direct color transform. The direct color path itself reads source rows,
allocates one destination backing, and records 63 successful materializations.

S2 output checksums and timings match S1 within normal run variance. S3
checksums match S1 for all three operations. The enabled row path keeps its
temporary buffer at one 1960-pixel RGBA row (7,840 bytes) and lowers measured
`pixels` RSS by 30,640 KiB without a timing regression. Encoding remains
dominated by the encoder and has equivalent timing and RSS.

## Acceptance

- S2 output checksums must match S1.
- S3 output checksums must match S1.
- `pixels` and `encode` must report row-bounded scratch when
  `RASTER_ROW_READBACK` is enabled.
- `color` must report one direct color materialization and no avoidable full
  source scratch when `RASTER_DIRECT_COLOR_MATERIALIZATION` is enabled; the
  benchmark's post-transform checksum readback is reported separately above.
