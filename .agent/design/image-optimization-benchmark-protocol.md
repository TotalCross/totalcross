<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization benchmark protocol

Status: established for Phase 1; the cross-platform policy below is
prospective for Phase 2 and later.

## Scope

This protocol measures image backing, draw, decode, storage, cache, and
diagnostic changes across the phase series. macOS/software-Skia defines the
primary and authoritative local raster benchmark methodology. Phase 1 uses
only local macOS evidence. Phase 2+ may add complementary cross-platform
evidence according to the `Cross-platform policy for Phase 2+` section.

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

## Cross-platform policy for Phase 2+

This section is prospective for Phase 2 and later. It does not invalidate or
require rerunning historical Phase 1 benchmark reports or samples.

- Local macOS software Skia remains the primary and authoritative environment
  for S1/S2/S3 raster benchmarks and small regression decisions.
- Android device runs use a physical device via `adb` and the real production
  GPU/OpenGL ES backend. Do not add or require Android software-raster builds
  solely for benchmarking.
- Windows GitHub-hosted runners and Linux GitHub-hosted runners are valid for
  native software-raster cross-platform validation and comparisons. Prefer
  Linux x64 and, when available and practical, native Linux ARM64; emulated or
  QEMU ARM runs are not performance evidence.
- GitHub-hosted performance results are secondary and indicative because
  hosted-runner variability can distort small deltas. Correctness failures are
  blockers, but hosted CI results alone must not accept or reject a small
  performance delta near the existing 5% threshold.
- Compared GitHub configurations run on the same provisioned runner/job with
  identical build flags and workload, preferably interleaved or counterbalanced
  in execution order. Do not compare absolute timings from unrelated jobs or
  runners.
- Run optimizations only on semantically relevant backends. For `RASTER_*`
  optimizations, macOS, Windows, and Linux software raster are the relevant
  performance targets. On Android GPU, validate relevant CPU-side decode and
  backing behavior and, where applicable, verify that raster-only fast paths
  remain unused or zero-hit rather than forcing a raster renderer.
- Expand platform coverage at meaningful milestone or closeout boundaries,
  rather than multiplying every micro-benchmark across every platform.

The existing 60-to-200 sample escalation, RSS rules, and local macOS
methodology remain unchanged for all applicable comparisons.

The Phase 1 addendum reserves, in addition to the existing IDs 0-12:

```text
RASTER_TARGET_COLORTYPE_CONVERSION = 13
RASTER_PHYSICAL_VARIANT_CACHE = 14
RASTER_PHYSICAL_IDENTITY_FOLDING = 15
FEATURE_COUNT = 16
```

These are reservation-only controls. They remain package-private,
process-global, tri-state, opt-in, and inert. This protocol does not reserve
controls for `USE_NATIVE_SWAP`, adaptive JPEG, `getJpegBestFit`/
`getJpegScaled`, `hwScaleW`/`hwScaleH`, or the invariant that GPU rendering
must not use `writePixels`.

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

If a peak-RSS difference above 5% persists after the required 200-sample rerun,
capture equivalent memory/residency diagnostics at matched execution points
before classifying it as a regression. On macOS, use `vmmap -summary` plus RSS
and physical-footprint measurements when available. This supplements the
existing local macOS benchmark requirement; it does not change the sample
regime or reclassify historical results.

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

## Post-stabilization rebaseline

The Phase 1 rebaseline uses current master
`7add0f29e9366a19d894237119a415416e6bb557` for S1/pre. S2/post-disabled uses
the final Phase 1 code with every optimization explicitly disabled, including
`DIAGNOSTIC_ACCOUNTING`; S3/post-enabled uses the same Phase 1 code with only
`DIAGNOSTIC_ACCOUNTING` enabled. The authored Phase 1 base
`1898014784b2fba5716cc033e49520740b05f0dd` remains historical metadata, not
the new S1 source. The new samples and report belong under
`post-stabilization-rebaseline/`; earlier benchmark artifacts are immutable.

## Interpretation

A confirmed post-disabled regression greater than 5% in median elapsed time or
peak RSS must be fixed before accepting the milestone. Enabled diagnostic
overhead and any observed accounting benefit are reported separately. Results
apply only to the measured workload and machine.
