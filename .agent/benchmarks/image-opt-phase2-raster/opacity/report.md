<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Opacity metadata S1 baseline and S2/S3 results

This report records the pre-implementation opacity baseline required before
adding structural opacity metadata.

## Revision and environment

- Scenario: `S1/pre`
- Benchmark revision: `f4f1a6ad9`
- Build: the post-zero-copy Release macOS software-Skia build with
  `TC_GRAPHICS_SOFTWARE=ON`, `TC_RENDERER_SKIA=ON`, and `TC_WINDOWING_SDL=ON`
- Runner: `scripts/run-image-optimization-benchmark.py`, 3 warmups and 60
  measured samples, 50 ms RSS sampling interval
- Host: macOS 26.5.2 (Darwin 25.5.0, build 25F84), MacBookPro18,1, arm64,
  10 CPUs, 16 GiB RAM

## Results

| Fixture | Dimensions | Median | P95 | Peak RSS | Copied decodes | Final bytes |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| JPEG | 512x512 | 2 ms | 3 ms | 139856 KiB | 60 | 62914560 |
| RGB PNG | 220x220 | 2 ms | 2 ms | 119952 KiB | 60 | 11616000 |
| Alpha PNG, all opaque | 600x600 | 2 ms | 2 ms | 169504 KiB | 60 | 86400000 |
| Alpha PNG, translucent | 36x36 | 0 ms | 0 ms | 113584 KiB | 60 | 311040 |

All 240 measured samples completed successfully. The benchmark forces a fresh
encoded-source materialization for each iteration and reports the existing
decode accounting fields. At this revision, no structural opacity proof or
post-decode alpha classification is implemented; the four fixture kinds are
the semantic gate for S2/S3.

Raw samples and runner summaries are stored in the matching
`scenario-1-*.csv` and `scenario-1-*-summary.txt` files.

## Post-implementation comparison

The opacity implementation was committed at `f6a4e1227`. S2 disabled all
phase-2 toggles; S3 enabled only `RASTER_OPACITY_METADATA`. The sequential
runner completed all 480 post-implementation samples successfully.

| Fixture | Scenario | Median | P95 | Peak RSS | Opacity | Source proofs | Decode proofs | Fallback scans |
| --- | --- | ---: | ---: | ---: | --- | ---: | ---: | ---: |
| JPEG | S2/post-disabled | 2 ms | 2 ms | 130896 KiB | UNKNOWN | 0 | 0 | 0 |
| RGB PNG | S2/post-disabled | 2 ms | 2 ms | 120672 KiB | UNKNOWN | 0 | 0 | 0 |
| Alpha PNG, all opaque | S2/post-disabled | 1 ms | 2 ms | 166400 KiB | UNKNOWN | 0 | 0 | 0 |
| Alpha PNG, translucent | S2/post-disabled | 0 ms | 0 ms | 113536 KiB | UNKNOWN | 0 | 0 | 0 |
| JPEG | S3/post-enabled | 2 ms | 2 ms | 147744 KiB | OPAQUE | 60 | 0 | 0 |
| RGB PNG | S3/post-enabled | 2 ms | 3 ms | 118160 KiB | OPAQUE | 60 | 0 | 0 |
| Alpha PNG, all opaque | S3/post-enabled | 2 ms | 2 ms | 166496 KiB | OPAQUE | 0 | 60 | 0 |
| Alpha PNG, translucent | S3/post-enabled | 0 ms | 0 ms | 113552 KiB | TRANSLUCENT | 0 | 60 | 0 |

S3 adds no separate full-image opacity scan for JPEG or alpha-free RGB PNG.
Alpha PNG classification occurs during row decoding, and the semantic probes
reported the expected opacity class without changing visible pixels. The
timing gate shows no confirmed regression versus the S1 fixture baselines;
these remain local macOS software-Skia measurements.

Raw post-implementation samples and runner summaries are stored in the
matching `scenario-2-*.csv`, `scenario-3-*.csv`, and summary files.
