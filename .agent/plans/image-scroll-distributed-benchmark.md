<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Build a distributable image-scroll benchmark suite

This ExecPlan follows `AGENTS.md`, `.agent/PLANS.md`, and the
`.agents/skills/logical-commits/SKILL.md` workflow.

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
- [x] Repository, ExecPlan, `logical-commits`, and header-validation
  instructions read and followed.
- [x] Checkpoint 1 — commit `beac137c3`: public process mask override,
  unknown-bit rejection, zero-mask semantics, and focused tests passed.
- [x] Checkpoint 2 — commit `9aa9d099d`: existing real-workload app accepts
  mask/prefetch/run/output/corpus arguments, validates the 663 image entries,
  keeps the corpus external, and runs one cold pass per process.
- [x] Checkpoint 3 — commit `495b1133d`: time-based pacing, raw frames/timeline,
  percentile/stall metrics, and feature-hit/`NOT_EXERCISED` classification;
  focused SDK test and benchmark-JAR compilation passed.
- [x] Checkpoint 4 — commit `43225cbfe`: low-overhead process memory,
  lifecycle checkpoints, Skia surface metadata, and common environment fields;
  native Release build, focused SDK test, benchmark-JAR compilation, header
  validation, and whitespace validation passed.
- [x] Checkpoint 5 — commit `44f0f1061`: common results schema, deterministic
  mask plan, short fresh-process self-test, same-machine mask-zero
  aggregation, internal accounting fields, and application-owned ZIP
  creation; deploy conversion and focused SDK test passed.
- [x] Checkpoint 6 — commit `30ff7efa5`: run-all controller/launchers execute
  the five-round fresh-process matrix, and the packager emits the
  platform-specific launcher templates with a root bundle manifest.
- [x] Checkpoint 7 — commits `216d47787`, `ba7d51b19`: package-SDK deploy tooling consumes a
  real package.yml SDK ZIP, uses `tc.Deploy` without native overlay, and emits
  all five requested bundle formats; structure and 663-file corpus checks
  passed on the host (`/tmp/image-scroll-final-packages.m4ZZrI`).
- [x] Checkpoint 8 — commits `e1bccc8c9`, `4ac0a53b5`: the benchmark app is
  one-process/one-cold-measurement only; SDK extraction, compilation, and
  deployment are bound to one official JAR; and the bundle runner owns the
  self-test, four smokes, 210-process matrix, aggregation, and final ZIP;
  commits `5f97ba301` and `6b807c5d9` removed the legacy native runner and
  added packaged-ZIP wrapper handling.
- [x] Checkpoint 9 — commit `882b31a3c`: the distributed runner bounds every
  native process to 180 seconds and records a fail-fast timeout in its log.
- [ ] Final closeout: the external self-test passed, but the first smoke
  (`0/off`) was terminated after more than six minutes without output or
  artifacts; no other smoke or matrix process was started. The bundle used the
  official `TotalCross-7.2.2` artifact from Actions run `34883508658`.

## Current Architecture and Scope

The benchmark app now accepts only `--mode=benchmark` plus the explicit corpus,
mask, prefetch, run, output, and dataset-hash arguments needed for one cold
scroll measurement. The external runner owns validation, fresh native-process
launches, the deterministic 210-process plan, aggregation, and ZIP creation.
The packaged bundle carries exactly 663 JPEGs, the official device chime, the
540x960 screen argument, and compile/deploy SDK SHA-256 values that must match.

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

### Checkpoint 8 — harness correction

Remove controller behavior from `ImageScrollRealWorkloadBenchmarkApp`, keep
the individual artifact schema, and require `/scr -1,-1,540,960` with an
explicit mask and `off|on` prefetch setting. Compile only the app and shared
benchmark support against the extracted official SDK JAR, use that same JAR
for `tc.Deploy`, record matching SHA-256 values, and copy one external Python
runner into each bundle. The runner validates the manifest/corpus/chime,
executes every combination in a fresh native process, and stops on the first
nonzero exit or signal.

Commits: `e1bccc8c9` and `4ac0a53b5`; legacy runner removal and final plan
closeout remain active.

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

Required harness commands:

    bash -n scripts/package-image-scroll-benchmark.sh
    PYTHONDONTWRITEBYTECODE=1 python3 -m py_compile scripts/run-image-scroll-distributed-benchmark.py
    python3 scripts/validate-copyright-headers.sh --files <changed files>
    git diff --check

Also perform one external bundle self-test followed by four fresh-process
smoke runs (`0/off`, `0/on`, `32799/off`, `32799/on`). Stop immediately on a
nonzero exit or signal, parse JSON/CSV/timeline outputs, verify `540x960`,
requested/effective masks, prefetch values, the external corpus, and the
distinct prefetch paths. Only if all four smokes pass, run the 210-process
matrix, aggregate it, and verify the final ZIP can be opened.
Validate the packager against the real package.yml SDK artifact from Actions
run `34883508658`; record its published artifact digest and the extracted
SDK-JAR SHA-256 in the final evidence. Do not substitute a local SDK or native
runtime for that artifact.

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

The implementation commits are `e1bccc8c9`, `4ac0a53b5`, `5f97ba301`,
`6b807c5d9`, `1b6007483`, and `882b31a3c`. The official package artifact was
downloaded, extracted, compiled, deployed, and passed manifest/corpus/hash
checks; the extracted SDK JAR SHA-256 was
`229ec02e9dcd60e7d9d114aabe098ce9b9f44f26ac7509fe3b13b4745aa0d2c1` for both
compile and deploy. The external self-test passed. The required smoke
sequence halted at `0/off` after a native-process hang, so no matrix ZIP
exists.

## Revision Note

Initial plan created before source edits on 2026-09-11 after the required
branch setup and baseline feature-ID verification.
