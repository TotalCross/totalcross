<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization benchmark protocol

Status: established for phase 1; update only when the executed measurement
regime materially differs from this contract.

## Scope

This protocol measures image backing, draw, decode, storage, cache, and
diagnostic changes across the phase series on macOS. It is local evidence, not
a cross-platform performance claim. Only the SDK and macOS software-Skia
builds are in scope for phase 1.

## Required scenarios

Each item has three scenarios using identical fixture bytes, workload
parameters, build type, renderer, graphics backend, windowing backend, machine,
and sample regime:

1. `S1/pre`: exact pre-implementation commit.
2. `S2/post-disabled`: post-implementation code with the target explicitly
   disabled.
3. `S3/post-enabled`: the same post-implementation code with only the target
   explicitly enabled.

Every run resets `ImageOptimizationSettings` and explicitly disables every
other optimization introduced by this series. `DEFAULT` resolves to disabled.

## Build and sample regime

The default native configuration is:

```text
-DCMAKE_BUILD_TYPE=Release
-DTC_GRAPHICS_SOFTWARE=ON
-DTC_RENDERER_SKIA=ON
-DTC_WINDOWING_SDL=ON
```

The phase-1 workload warms up with three complete batches before recording 60
measured samples. The runner samples process RSS externally every 50 ms. If a
comparison has coefficient of variation above 5% or is near its acceptance
boundary, rerun that comparison with 200 samples and record the reason. Do not
exceed 200 samples without a documented justification.

The benchmark runner records elapsed time for a sufficiently large batch;
individual nanosecond-scale operations are not timed with millisecond TCVM
timing. Peak RSS is sampled externally while the workload is active.

## Report requirements

Each report records exact scenario SHAs, machine/macOS/CPU/RAM, runner and
build commands, CMake flags, sample and workload counts, median/p95 elapsed
time, mean/stddev when useful, peak RSS, relevant backing/counter diagnostics,
S2-vs-S1 and S3-vs-S1/S2 deltas, correctness/quality status, and limitations.

Raw samples live under
`.agent/benchmarks/<plan>/<item>/scenario-{1,2,3}.csv` (for this plan,
`<plan>` is `image-opt-phase1-controls`).
Split raw files by workload before exceeding 20 KiB. Verbose local logs belong
under `artifacts/image-opt-phase1-controls/` and are not committed.

## Phase-1 control workload

The control benchmark exercises repeated cached deferred draws, Image/Pipeline
creation churn, the existing accounting-disabled path, and active-workload
RSS. Post-settings runs use S2 with all optimization features disabled and S3
with only `DIAGNOSTIC_ACCOUNTING` enabled. S3 is intentionally a diagnostic
trade-off, so no fabricated success threshold is applied.

## Interpretation

A confirmed post-disabled regression greater than 5% in median elapsed time or
peak RSS must be fixed before accepting the milestone. Enabled diagnostic
overhead and any observed accounting benefit are reported separately. Results
apply only to the measured workload and machine.
