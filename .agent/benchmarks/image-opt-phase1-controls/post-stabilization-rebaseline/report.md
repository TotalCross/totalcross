<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Phase 1 post-stabilization rebaseline

Status: complete. This is a new comparison; earlier Phase 1 benchmark
artifacts remain unchanged.

## Provenance and setup

- Branch: `perf/image-opt-phase1-controls`
- Authored Phase 1 historical base: `1898014784b2fba5716cc033e49520740b05f0dd`
- S1 current-master baseline: `7add0f29e9366a19d894237119a415416e6bb557`
- S2/S3 final Phase 1 code: `1d8deacb14cd3847cffb4d238c3f0fa97830951d`
- Host: macOS 26.5.2, Darwin 25.5.0, arm64, 16 GiB
- Native configuration: Release software Skia with SDL windowing
- CMake flags: `-DCMAKE_BUILD_TYPE=Release -DTC_GRAPHICS_SOFTWARE=ON
  -DTC_RENDERER_SKIA=ON -DTC_WINDOWING_SDL=ON`
- Workload: three warmup batches, then 60 measured samples initially;
  required rerun used 200 samples. Each sample performs 256 cached draws and
  32 Image/Pipeline churn operations.

S1 used a temporary detached worktree containing the pre-existing benchmark
harness overlay because current master does not include that harness. The
production SDK/native sources were checked out at the S1 SHA; the overlay was
not committed. S2 and S3 used the Phase 1 harness and the same native runtime.

## Results

The initial 60-sample run crossed the peak-RSS review threshold, so the
required 200-sample rerun was performed. Timing is elapsed milliseconds per
batch; RSS is the externally sampled process peak.

| scenario | configuration | samples | median | p95 | mean | CV | peak RSS |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: |
| S1/pre | current master | 200 | 697 ms | 704 ms | 697.625 ms | 0.469% | 113680 KB |
| S2/post-disabled | all features disabled, including diagnostics | 200 | 695 ms | 699 ms | 695.360 ms | 0.313% | 115888 KB |
| S3/post-enabled | only `DIAGNOSTIC_ACCOUNTING` enabled | 200 | 698 ms | 706 ms | 698.970 ms | 0.543% | 113920 KB |

Final deltas:

- S2 versus S1: median -0.287%, peak RSS +1.942%.
- S3 versus S1: median +0.143%, peak RSS +0.211%.
- S3 versus S2: median +0.432%, peak RSS -1.698%.

All scenarios passed process-exit and sample-count checks. S2 emitted zero
diagnostic counters. S3 showed the expected diagnostic activity, ending at
19,173 Image creations, 12,773 pipelines, 6,400 draw plans, and 51,200 draw
plan cache hits; the reserved raster controls remained inert.

The initial 60-sample RSS values were 104480 KB (S1), 115776 KB (S2), and
113984 KB (S3). Because the differences above 5% did not persist in the
required 200-sample rerun, no matched-point `vmmap -summary` capture was
required. The protocol rule remains documented for future runs.

## Artifacts and limitations

Raw samples and compact summaries are in this directory. The 60-sample files
are retained with an `-initial-60` suffix to document the escalation; the
canonical `scenario-{1,2,3}.csv` files contain the 200-sample results. Verbose
logs remain in `/tmp/phase1-rebaseline-*.log` and generated build/deployment
outputs are not committed.

This is one local arm64 macOS machine and one software-Skia workload. It is a
Phase 1 control comparison, not evidence for any unimplemented raster
optimization or for other platforms/renderers.
