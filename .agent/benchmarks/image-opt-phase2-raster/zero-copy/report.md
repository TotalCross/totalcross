<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Zero-copy decode S1 baseline

This report records the pre-implementation baseline required before changing
the zero-copy decode path.

## Revision and environment

- Scenario: `S1/pre`
- Benchmark revision: `8b586d48a755d23bf1a5689dda47f5f2d14f8ff8`
- Native build: `cmake -S TotalCrossVM -B build -G Ninja
  -DCMAKE_BUILD_TYPE=Release -DTC_GRAPHICS_SOFTWARE=ON
  -DTC_RENDERER_SKIA=ON -DTC_WINDOWING_SDL=ON`, followed by `ninja -C build`
- SDK build: `cd TotalCrossSDK && ./gradlew-agent dist -x test --no-daemon
  --console=plain`
- Runner: `scripts/run-image-optimization-benchmark.py`, 3 warmups and 60
  measured samples, 50 ms RSS sampling interval
- Host: macOS 26.5.2 (Darwin 25.5.0, build 25F84), MacBookPro18,1, arm64,
  10 CPUs, 16 GiB RAM

## Results

| Workload | Fixture | Median elapsed | P95 elapsed | Peak RSS | Samples |
| --- | --- | ---: | ---: | ---: | ---: |
| Decode | 600x600 RGBA PNG | 4 ms | 4 ms | 139264 KiB | 60 |
| Decode | 1960x1960 JPEG | 20 ms | 21 ms | 162512 KiB | 60 |

The PNG samples are in
`scenario-1-png.csv`; the JPEG samples are in
`scenario-1-jpeg.csv`. The runner summaries are in the matching
`scenario-1-*-summary.txt` files. Both processes exited successfully and
recorded all 60 samples.

The timed section creates a fresh encoded-source `Image` and forces native
materialization through `getGraphics()`. It does not call `getPixels()` in the
timed decode section. The decoded-buffer byte totals are 1,440,000 bytes per
PNG iteration and 15,366,400 bytes per JPEG iteration.

No post-disabled or post-enabled result exists yet; this report is the S1 gate
for the implementation milestone.
