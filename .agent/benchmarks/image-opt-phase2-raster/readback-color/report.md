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

To be filled after the row-readback and direct-color implementation is
committed. S2 will keep both target features disabled; S3 will enable exactly
the operation's target feature.

## Acceptance

- S2 output checksums must match S1.
- S3 output checksums must match S1.
- `pixels` and `encode` must report row-bounded scratch when
  `RASTER_ROW_READBACK` is enabled.
- `color` must report one direct color materialization and no avoidable full
  source scratch when `RASTER_DIRECT_COLOR_MATERIALIZATION` is enabled.
