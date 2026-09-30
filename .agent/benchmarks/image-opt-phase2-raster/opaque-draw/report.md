<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Opaque raster copy S1 baseline and S2/S3 results

## S1 baseline

The pre-writePixels baseline was captured at `0df4bafa3`, before the runtime
implementation. The macOS Release software-Skia build used
`TC_GRAPHICS_SOFTWARE=ON`, `TC_RENDERER_SKIA=ON`, and `TC_WINDOWING_SDL=ON`.
The runner used 3 warmups, 60 measured samples, and 50 ms RSS sampling.

| Fixture | Median | P95 | Peak RSS | Samples |
| --- | ---: | ---: | ---: | ---: |
| JPEG 512x512 | 7 ms | 7 ms | 113520 KiB | 60 |
| Opaque PNG 600x600 | 9 ms | 9 ms | 120880 KiB | 60 |

## Post-implementation comparison

The guarded native `writePixels()` path was implemented at `eb192e6fe`; the
full-image parity check was added at `c590ec290`. S2 disabled all phase-2
toggles and S3 enabled only `RASTER_OPAQUE_WRITE_PIXELS`. The finalized
sequential matrix completed all 240 post-implementation samples.

| Fixture | Scenario | Median | P95 | Peak RSS | Attempts | Hits | Fallbacks | Copied bytes |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| JPEG 512x512 | S2/post-disabled | 7 ms | 7 ms | 117808 KiB | 0 | 0 | 0 | 0 |
| Opaque PNG 600x600 | S2/post-disabled | 9 ms | 9 ms | 121472 KiB | 0 | 0 | 0 | 0 |
| JPEG 512x512 | S3/post-enabled | 0 ms | 1 ms | 123200 KiB | 507 | 505 | 2 | 529530880 |
| Opaque PNG 600x600 | S3/post-enabled | 0 ms | 1 ms | 129296 KiB | 507 | 505 | 2 | 727200000 |

The S2 timing is baseline-equivalent. S3 shows the measured software-renderer
benefit on this workload: the coarse millisecond median reaches 0 ms and P95
falls to 1 ms for both formats. The two fallback cases are the guarded
ineligible draws; the enabled smoke reports `semantic_parity=true` for both
JPEG and PNG by comparing a disabled-path draw with a fast-path draw pixel for
pixel. These are local macOS software-Skia measurements, not cross-platform
claims.

Raw samples and runner summaries are stored in the matching
`scenario-1-*.csv`, `scenario-2-*.csv`, and `scenario-3-*.csv` files.
