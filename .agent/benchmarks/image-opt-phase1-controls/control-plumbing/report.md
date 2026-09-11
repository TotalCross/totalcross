<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization control-plumbing benchmark

Status: passed. This report measures the phase-1 control and diagnostic
plumbing only; it does not claim an optimization benefit.

## Scenarios

| Scenario | Revision | Control state |
| --- | --- | --- |
| S1/pre | `d00d7c1dfec4bddc14bdfbb8293b30dfe8b3a3c6` | No settings class; existing accounting-disabled path |
| S2/post-disabled | `613762c45ed887733b1dab9a12b20b32280e0fca` | All 13 features explicitly `DISABLED` |
| S3/post-enabled | `613762c45ed887733b1dab9a12b20b32280e0fca` | Only `DIAGNOSTIC_ACCOUNTING` explicitly `ENABLED` |

The S1 revision is the exact pre-settings benchmark commit. S2 and S3 use
identical post-settings code and differ only in the requested diagnostic state.

## Environment and commands

- Host: Darwin 25.5.0, macOS 26.5.2, arm64, 16 GiB RAM.
- Build: Release, Ninja, `TC_GRAPHICS_SOFTWARE=ON`,
  `TC_RENDERER_SKIA=ON`, `TC_WINDOWING_SDL=ON`.
- Workload: 60 measured samples after 3 warmups; each sample performs 256
  cached deferred draws and 32 deferred Image/Pipeline churn operations.
- Runner: `python3 scripts/run-image-optimization-benchmark.py <executable>
  --scenario <scenario> --samples 60 --output <csv> --log <log>
  --summary <summary>`.
- Build commands: `cd TotalCrossSDK && ./gradlew-agent dist -x test
  --no-daemon --console=plain`; `cmake -S TotalCrossVM -B
  build/image-opt-phase1-macos -DCMAKE_BUILD_TYPE=Release -G Ninja
  -DTC_GRAPHICS_SOFTWARE=ON -DTC_RENDERER_SKIA=ON -DTC_WINDOWING_SDL=ON`;
  `ninja -C build/image-opt-phase1-macos tcvm Launcher`.

## Results

| Scenario | Samples | Median ms | P95 ms | Mean ms | Stddev ms | CV | Peak RSS KB |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| S1/pre | 60 | 668 | 671 | 668.15 | 1.48 | 0.22% | 107456 |
| S2/post-disabled | 60 | 669 | 671 | 669.12 | 0.93 | 0.14% | 107280 |
| S3/post-enabled | 60 | 669 | 671 | 668.87 | 1.37 | 0.21% | 109008 |

Relative to S1, S2 is +0.15% in median elapsed time and -0.16% in peak RSS.
The disabled path therefore has no confirmed regression above the 5% rule.
S3 is +0.15% in median elapsed time and +1.44% in peak RSS versus S1; versus
S2 it is 0.00% in median elapsed time and +1.61% in peak RSS. The enabled
diagnostic cost is reported as a trade-off, not as an optimization threshold.

## Diagnostic and correctness evidence

All three processes exited with code 0 and recorded exactly 60 samples. S2
recorded zero Java diagnostic counters after its explicit disable. S3 recorded
nonzero accounting during the measured workload, ending at 5752 Images, 3832
pipelines, 1920 draw plans, and 15360 draw-plan cache hits. Existing native
backing live counts remained bounded at 2–3 in the sampled rows; full decode
and geometry-materialization counters remained zero during the measured phase
because setup and warmup precede sampling.

Raw samples and per-scenario host metadata are committed beside this report in
`scenario-{1,2,3}.csv` and `scenario-{1,2,3}-summary.txt`. Verbose deployment
and process logs remain under the ignored `artifacts/image-opt-phase1-controls/`
directory.

## Limitations

This is one local arm64 macOS machine and one software-Skia workload. RSS is
sampled externally at 50 ms intervals and is a process peak, not an allocator
breakdown. Timing uses millisecond VM timestamps around a deliberately large
batch. The benchmark does not measure Android, iOS, GPU, compressed formats,
or any phase-2–4 optimization. No product default is selected by these data.
