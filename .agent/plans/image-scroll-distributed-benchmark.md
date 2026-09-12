<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Build a distributable image-scroll benchmark suite

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`. The requested
`logical-commits` skill is not installed in this environment; its required
small, semantic checkpoint discipline is applied manually below.

## Purpose / Big Picture

Create `perf/image-scroll-distributed-benchmark` from
`origin/perf/image-scroll-prefetch` and turn the existing
`ImageScrollRealWorkloadBenchmarkApp` into a fully automated, cold-process,
real-corpus image-scroll benchmark. It will consume an already packaged
`TotalCross-<version>.zip`, deploy the Java app with `tc.Deploy`, keep the 663
files external under `corpus/`, run the deterministic 21-mask × 2-prefetch ×
5-sample matrix, and produce reproducible JSON/CSV/TSV results and a final
application-created ZIP.

## Working Set and Resume Protocol

The active plan is this file. Existing user-local benchmark logs, generated
launchers, SDK artifacts, and prior plans under `.agent/` are deliberate local
state and must not be removed or staged. On resume, read `Progress`, the active
checkpoint, `Decision Log`, and `Validation and Acceptance`; then inspect only
the paths named by the active checkpoint.

Primary implementation paths:

- `TotalCrossSDK/src/main/java/totalcross/ui/image/ImageOptimizationSettings.java`
- `TotalCrossSDK/src/test/java/totalcross/ui/image/ImageOptimizationSettingsTest.java`
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageScrollRealWorkloadBenchmarkApp.java`
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageRasterBenchmarkSupport.java`
- `TotalCrossSDK/build.gradle`
- `TotalCrossVM/src/nm/ui/skia/skia_image_backing.cpp` and its headers
- `scripts/` benchmark/deploy tooling and platform launchers

Supporting evidence belongs in `.agent/evidence/image-scroll-distributed-benchmark.md`
only if repeated validation needs a durable record; keep it compact and do not
copy raw logs into the plan.

## Progress

- [x] Branch created from `origin/perf/image-scroll-prefetch`; baseline feature
  mapping verified as bits 0–15 with default mask 32799.
- [x] Repository and ExecPlan instructions read; unavailable
  `logical-commits` skill noted.
- [x] Checkpoint 1 — commit `beac137c3`: public process mask override,
  unknown-bit rejection, zero-mask semantics, and focused tests passed.
- [x] Checkpoint 2 — commit `9aa9d099d`: existing real-workload app accepts
  mask/prefetch/run/output/corpus arguments, validates the 663 image entries,
  keeps the corpus external, and runs one cold pass per process.
- [x] Checkpoint 3 — commit `495b1133d`: time-based pacing, raw frames/timeline,
  percentile/stall metrics, and feature-hit/`NOT_EXERCISED` classification;
  focused SDK test and benchmark-JAR compilation passed.
- [x] Checkpoint 4 — pending commit: low-overhead process memory, lifecycle
  checkpoints, Skia surface metadata, and common environment fields are
  implemented; native Release build, focused SDK test, benchmark-JAR
  compilation, header validation, and whitespace validation passed.
- [ ] Checkpoint 5: result schema, self-test, aggregation, and Java ZIP.
- [ ] Checkpoint 6: deterministic suite plan and platform launchers.
- [ ] Checkpoint 7: package-SDK deploy tooling and five bundle formats.
- [ ] Final closeout: required tests, four-combination smoke, packaged-SDK
  validation when available, scoped status/log evidence, and no new local
  changes left uncommitted.

## Current Architecture and Scope

The Java settings class currently has a tri-state per-feature API and a
`DEFAULT_EFFECTIVE_MASK` of 32799, but no process-level mask override. Native
draw/decode mirrors are private static `Image` fields. The existing real
workload accepts `--image-dir`, target-color, variant-cache, and legacy
prefetch profiles, builds 663 JPEG-named files in 221 rows of three, and runs
three passes in one process. The distributed suite must replace that benchmark
control flow with one cold measured scroll per process while preserving the
same dimensions, layout, image paths, and scroll endpoint trajectory.

Native accounting is currently exposed through test helpers in `Image` and
`NativeImageBacking`, while the Skia backing manager owns the physical/raster
storage paths. New counters should remain in-memory and be emitted once per
run. Diagnostic accounting must not gate observation of other feature hits.

The packaged SDK is assembled by `.github/workflows/package.yml`; local
benchmark packaging must accept its ZIP as an input and must never overlay a
locally rebuilt `tcvm` or launcher. `tc.Deploy` is the only deployment path.

## Plan of Work

### Checkpoint 1 — explicit mask API

Add public `setMask(long)`, `getMask()`, and `getEffectiveMask()` while keeping
the existing tri-state methods for compatibility. `setMask(0)` is an explicit
override, unknown bits and negative values are rejected, and an unset override
continues to resolve the existing default. Keep `effectiveMask` as the
effective result; do not add `setEffectiveMask`. Synchronize both native
mirrors after validation. Add tests for default preservation, zero, all
known-bit combinations used by the suite, unknown bits, and reset behavior.

Commit: `feat(sdk): add image optimization mask override`.

### Checkpoint 2 — CLI and cold application

Adapt the existing real-workload app to accept `--image-optimization`,
`--prefetch=off|on`, `--run`, `--output`, and `--corpus`. Keep corpus files
external, validate exactly 663 files using the existing `TC_IMAGE_CORPUS`
convention, and retain native density/default dimensions and three columns.
Use one process for one run, with prefetch completion preceding the measured
scroll only in `on` mode. Do not run warm or warm2 passes. Write an explicit
run status and requested/effective masks.

Commit: `feat(sdk): run cold image scroll workloads`.

### Checkpoint 3 — pacing and feature hits

Drive scroll position from elapsed monotonic time, preserving the existing
trajectory and endpoint. Record frame durations and all requested percentile,
threshold, stall, and consecutive-stall metrics. Add low-overhead per-feature
and per-path hit counters for paths exercised by the workload; emit counters
at completion. For every enabled feature with zero hits, mark its status
`NOT_EXERCISED`, and never interpret that as no impact. Keep observation
independent of `DIAGNOSTIC_ACCOUNTING`.

Commit: `perf(sdk): time image scroll frames and hits`.

### Checkpoint 4 — memory and environment

Sample process memory about every 250 ms and record the required lifecycle
checkpoints, current/peak resident memory, and platform-specific fields when
available. Add internal accounting fields for encoded, decoded, raster,
physical-variant, prefetch, tracked-texture, image, backing, and color-type
counts/bytes where the runtime can provide them cheaply. Collect OS, CPU/GPU,
RAM, logical/physical/drawable/screen dimensions, density/scales, refresh rate,
SDL/Skia properties, backend, color/alpha/rowBytes, endianness, commit/version,
dataset identity, and columns. Unavailable optional values are null or
`unavailable`.

Commit: `feat(sdk): capture benchmark memory environment`.

### Checkpoint 5 — results, self-test, and ZIP

Implement the common `results/` schema, parseable `summary.csv`, validation
of requested/effective masks and invalid configurations, and deterministic
aggregation against same-machine same-prefetch mask-zero baselines only.
Implement the pre-matrix self-test, including corpus/hash/output/flags/mask
checks, renderer/environment/result checks, and a test ZIP. The Java tooling
must create `totalcross-image-benchmark-results-<timestamp>.zip` without
external compression tools.

Commit: `feat(sdk): aggregate image benchmark results`.

### Checkpoint 6 — suite plan and launchers

Generate the 21 unique masks exactly as specified, with seed 73001 and a
deterministic pseudo-random permutation in each of five rounds. Write
`manifest.json`, `environment.json`, and `suite-plan.tsv`; run every one of
the 42 combinations once per round in fresh processes, for 210 processes.
Provide only `run-all.bat`, `run-all.command`, or `run-all.sh` in each platform
bundle and make the launcher perform self-test, matrix execution, aggregation,
ZIP creation, and the completion message.

Commit: `feat(benchmark): add deterministic suite launchers`.

### Checkpoint 7 — packaged SDK deploy and bundles

Add an explicit packager accepting a package.yml-generated SDK ZIP and
`TC_IMAGE_CORPUS`. Extract only to a controlled work directory, locate the
SDK's `tc.Deploy`, deploy the existing benchmark JAR, copy the corpus beside
the app, validate 663 files and dataset manifest, and select only the proper
launcher/runtime from the SDK. Produce:
`image-scroll-benchmark-windows-x64.zip`,
`image-scroll-benchmark-macos-arm64.zip`,
`image-scroll-benchmark-linux-x64.tar.gz`,
`image-scroll-benchmark-linux-arm64.tar.gz`, and
`image-scroll-benchmark-linux-armv7.tar.gz`.

Commit: `build(benchmark): package cross-platform image bundles`.

## Decision Log

- Decision: use the existing real-workload application and Gradle JAR task.
  Rationale: preserves the customer layout, decode/render path, and corpus
  behavior without introducing a second equivalent app.
- Decision: make the process-level mask override authoritative only when
  `setMask` is called. Rationale: preserves existing defaults for callers that
  do not opt in, while allowing `0` to mean all optimizations off.
- Decision: reject unknown bits before changing mirrors or process state.
  Rationale: an invalid configuration must fail clearly rather than silently
  measuring a different mask.
- Decision: keep optional platform memory/environment fields nullable.
  Rationale: the suite must run without Python, profilers, or user-installed
  tools, and missing complements must not abort.
- Decision: use Java-owned ZIP creation and a small stored/deflated ZIP writer
  if the TotalCross runtime lacks a usable ZIP API. Rationale: the result ZIP
  must not depend on external `zip`, PowerShell, or Python.

## Validation and Acceptance

Per checkpoint, run focused tests plus `git diff --check` before committing;
stage only intended paths and validate cached whitespace. Use the SDK wrapper
`TotalCrossSDK/gradlew-agent` and save verbose logs outside tracked source.

Required final commands:

    ./TotalCrossSDK/gradlew-agent test --no-daemon --console=plain
    ./TotalCrossSDK/gradlew-agent dist -x test --no-daemon --console=plain
    ./TotalCrossSDK/gradlew-agent jarImageScrollRasterFastPathBenchmark --no-daemon --console=plain
    git diff --check

Also perform four fresh-process smoke runs for masks 0 and 32799 with prefetch
off/on, parse JSON/CSV/timeline outputs, verify external corpus and distinct
prefetch paths, and verify the final ZIP can be opened by the Java reader.
Validate the packager against a real package.yml SDK artifact when GitHub
workflow access and artifact download are available; otherwise use the newest
packaged SDK already present and report the infrastructure limitation. Do not
substitute a local native rebuild for the package.yml artifact.

Expected validation level is 2 for Java/API slices, 3 for frame/memory/native
instrumentation, and 4 for final packaging and the requested matrix. Expensive
platform builds not available in the current host are deferred and recorded.

## Risks and Open Questions

- TotalCross's Java subset may not include all standard ZIP, process, or OS
  APIs; inspect existing SDK utilities and keep the writer/runtime probing
  compatible with deployed apps.
- Some requested native surface/GPU/texture fields may be unavailable through
  public runtime APIs; report null/unavailable rather than inventing values.
- Windows, macOS ARM, Linux ARM64, and Linux ARMv7 bundles cannot all be
  executed on this host. Bundle structure and launcher syntax can be checked
  locally; execution is deferred unless matching runners/artifacts exist.
- Existing untracked benchmark logs and generated artifacts predate this
  branch and must remain untouched; only task-owned paths will be staged.

## Idempotence and Recovery

All build/deploy outputs go under a caller-selected output directory or a
temporary directory. Re-running a run ID writes only its own run directory.
The packager validates before copying and refuses a corpus count other than
663. A failed process becomes an invalid run and does not enter statistics.
Never remove or reset user-local files; recover a partial task by resuming
from the latest checkpoint commit and inspecting only the active paths.

## Outcomes & Retrospective

Pending. At completion, record the seven commit IDs, focused/final validation,
smoke results, bundle locations, package.yml artifact provenance, and optional
metrics unavailable on each platform.

## Revision Note

Initial plan created before source edits on 2026-09-11 after the required
branch setup and baseline feature-ID verification.
