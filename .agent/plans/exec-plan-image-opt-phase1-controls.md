<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Phase 1: Establish image optimization controls and benchmark infrastructure

This ExecPlan follows `AGENTS.md`, `.agent/PLANS.md`, and
`.agents/skills/logical-commits/SKILL.md`. Explicit instructions in this plan
take precedence where they intentionally narrow validation or build scope.

## Purpose / Big Picture

Establish the internal control and measurement layer required for all later
Image backing optimizations. At the end of this phase, the repository has:

- a package-private `ImageOptimizationSettings` control surface with independent
  tri-state toggles for every optimization planned in phases 2-4;
- no public/stable SDK API commitment;
- a reusable macOS benchmark runner and reporting format;
- compact diagnostics that identify which backing/draw/decode path was used;
- a measured baseline proving the control/diagnostic plumbing does not introduce
  an unacceptable disabled-path regression;
- committed benchmark inputs, raw samples, summaries, state, evidence, and
  editorial handoff.

No phase 2-4 optimization is implemented here; every new toggle defaults to
current `master` behavior.

The authoritative starting `master` observed when this plan was authored is:

    1898014784b2fba5716cc033e49520740b05f0dd

Current master already has native Skia backing, encoded-source ownership,
deferred semantic pipelines, and allocation-free reusable draw-plan cache hits.

## Branch and Series Contract

Use exactly this branch for this phase:

    perf/image-opt-phase1-controls

Create it from `master`. Record the actual base SHA in state and benchmark
reports. If `master` no longer equals the authored SHA, compare only the
image-related paths touched by this plan against the authored SHA. If those
paths changed materially, update the Decision Log before implementation. Do not
reconstruct unrelated history.

The next plans branch sequentially:

    master
      -> perf/image-opt-phase1-controls
      -> perf/image-opt-phase2-raster
      -> perf/image-opt-phase3-formats
      -> perf/image-opt-phase4-lifecycle

Do not push, open a PR, merge, rebase, amend, or rewrite history unless
explicitly requested later.

## Working Set and Resume Protocol

The active plan is:

    .agent/exec-plan-image-opt-phase1-controls.md

Create and maintain:

    .agent/state/image-opt-phase1-controls.md
    .agent/evidence/image-opt-phase1-controls.jsonl
    .agent/archive/image-opt-phase1-controls-history.md
    .agent/reports/image-opt-phase1-controls-editorial.md
    .agent/design/image-optimization-benchmark-protocol.md

Committed benchmark data lives below:

    .agent/benchmarks/image-opt-phase1-controls/

On resume, read only the state file first. Then read the active milestone in
this plan and the exact source paths named by state. Search evidence selectively;
do not reread the whole evidence file or prior benchmark output.

Primary paths: `Image.java`, `ImagePipeline.java`, `ImageDrawPlan.java`, and
`NativeImageBacking.java` under `TotalCrossSDK/src/main/java/totalcross/ui/image/`,
plus `TotalCrossSDK/build.gradle` and
`scripts/run-image-modifier-memory-smoke.py`. Use
`.agent/reports/image-native-backing-report.md` only when a current semantic
contract is unclear; do not reread old implementation history routinely.

## Non-Negotiable Execution Constraints

All benchmarking is local on macOS.

Only SDK and macOS builds are permitted. Do not build Android, iOS, Linux,
Windows, Docker images, packages, or unrelated targets.

A build or compilation task may run only at the end of a milestone related to
that build. Compilation triggered by Gradle tests counts as a build. Before a
logical commit that precedes a milestone build, use source-level checks,
copyright/header validation, `git diff --check`, and narrowly scoped scripts.
Record build validation as deferred to the milestone gate.

Native smoke tests may run only at the end of a related milestone and at final
plan completion.

Every new file created by this plan must remain below 20 KiB and approximately
600 lines. Prefer 300-450 lines for the active plan and 100-150 lines for state.
If a generated CSV, report, state, evidence, or source file would exceed the
limit, split it by workload/scenario or consolidate completed detail into the
archive. Never truncate evidence to satisfy the limit.

Do not refactor an existing large file solely to reduce its size. Make narrow
edits to existing files and place substantial new logic in focused new files.

Commit the plan itself and every artifact produced directly by the plan,
including benchmark sources, compact raw benchmark samples, benchmark reports,
state, evidence, archive, and editorial handoff. Do not commit indirect build
artifacts, build directories, deployed applications, generated SDK outputs,
temporary profiling files, or verbose build logs.

Store verbose local logs under an ignored path such as:

    artifacts/image-opt-phase1-controls/

## Commit Protocol

For every commit, execute `.agents/skills/logical-commits/SKILL.md` exactly:
inspect only scoped changes, validate headers, stage only intended paths, run
`git diff --check --cached`, review the staged diff, commit one logical behavior
with a valid English scoped Conventional Commit, validate the created message
with the skill's Python check, and update state. Do not amend or include
unrelated work.

Suggested subjects below define intended logical boundaries; adjust wording only
when the actual slice differs.

## Benchmark Protocol Established by This Phase

Create `.agent/design/image-optimization-benchmark-protocol.md` before any
optimization implementation. Later phases must follow it without redefining the
measurement regime.

For each performance- or memory-affecting item, the protocol is:

1. Create the benchmark workload before implementing the item.
2. Build at the benchmark-baseline milestone boundary.
3. Run scenario 1 on the exact pre-implementation commit.
4. Implement the item.
5. Build at the implementation milestone boundary.
6. Run scenario 2 with the target feature explicitly `DISABLED`.
7. Run scenario 3 with the target feature explicitly `ENABLED`.
8. Generate and commit a report comparing all three scenarios.

For isolation, every benchmark run must reset `ImageOptimizationSettings` and
explicitly disable every non-target optimization introduced by this series.
Existing master behavior that predates this series remains unchanged.

Scenario definitions:

- `S1/pre`: code before the item implementation; the target toggle may exist but
  the implementation must not.
- `S2/post-disabled`: post-implementation code with target toggle `DISABLED`.
- `S3/post-enabled`: identical post-implementation code with only the target
  toggle `ENABLED`.

Use identical fixture bytes, workload parameters, build type, renderer,
graphics backend, windowing backend, machine, and sample regime for S1/S2/S3.

Default local raster build configuration is explicit even though macOS currently
defaults to software graphics:

    -DTC_GRAPHICS_SOFTWARE=ON
    -DTC_RENDERER_SKIA=ON
    -DTC_WINDOWING_SDL=ON
    -DCMAKE_BUILD_TYPE=Release

Each workload performs internal warm-up before recording samples. Start with 60
measured samples. If S1 versus S2 or S2 versus S3 shows more than 5% coefficient
of variation or a result near the acceptance boundary, rerun that comparison
with 200 measured samples. Do not exceed 200 without recording the reason.

Each report includes exact scenario SHAs, machine/macOS/CPU/RAM, commands and
CMake flags, sample/workload counts, median/p95 time, mean/stddev when useful,
externally sampled peak RSS, relevant backing/counter metrics, S2-vs-S1 and
S3-vs-S1/S2 deltas, correctness/quality status, and limitations.

A post-disabled regression larger than 5% in median elapsed time or peak RSS
must be rerun with 200 samples. If confirmed, fix the disabled-path regression
before accepting the milestone. Do not explain away a confirmed disabled-path
regression.

For S3, do not fabricate a success threshold when an item is explicitly a
trade-off. Report measured gains and losses. Item-specific acceptance rules in
later plans decide whether the enabled path is acceptable.

Write compact raw samples under:

    .agent/benchmarks/<plan>/<item>/scenario-1.csv
    .agent/benchmarks/<plan>/<item>/scenario-2.csv
    .agent/benchmarks/<plan>/<item>/scenario-3.csv
    .agent/benchmarks/<plan>/<item>/report.md

If a raw file would exceed 20 KiB, split by workload before adding more samples.

GPU benchmark cases are a special rule used in phase 4: create all three
scenario definitions and the report template, but do not execute any GPU
benchmark scenario during these plans. Mark the report `NOT EXECUTED BY PLAN`
and make no performance claim.

## Current Architecture and Scope

`ImagePipeline` currently records test creation counters from constructors, and
`Image` owns the existing conditional accounting flag/counters. Preserve
existing smoke-test semantics.

Do not add stable public settings to `Settings` or stable methods to `Image`.
This phase intentionally introduces an internal experimental substrate only.

Implement one package-private class:

    TotalCrossSDK/src/main/java/totalcross/ui/image/ImageOptimizationSettings.java

Use tri-state values:

    DEFAULT  = 0
    ENABLED  = 1
    DISABLED = 2

Do not add a `FORCED` state. `ENABLED` means “use when semantic and platform
preconditions are satisfied,” never “violate correctness to force the path.”

The settings are process-global for this series. Do not implement per-Image
overrides yet. Future public policies may map to the same feature bits without
changing the native optimization implementations.

Reserve feature IDs for all phase 2-4 work:

    DECODE_ZERO_COPY
    RASTER_OPACITY_METADATA
    RASTER_OPAQUE_WRITE_PIXELS
    RASTER_ROW_READBACK
    RASTER_DIRECT_COLOR_MATERIALIZATION
    STORAGE_RGB565
    STORAGE_GRAY8
    STORAGE_ARGB4444
    CACHE_BYTE_BUDGET
    CACHE_MEMORY_PRESSURE_EVICTION
    GPU_DISCARD_CPU_BACKING
    STORAGE_MMAP_LARGE_BACKINGS
    DIAGNOSTIC_ACCOUNTING

Also provide numeric settings:

    cacheMaxBytes
    mmapThresholdBytes

Use validated package-private mutators, `resetForTest()`, `state(feature)`,
`isEnabled(feature, defaultEnabled)`, and `describeForTest()`. Invalid feature
IDs/states fail immediately with `IllegalArgumentException`.

All new optimization features resolve `DEFAULT` to disabled throughout these
four plans. Do not change a feature default to enabled merely because its local
benchmark is positive. Policy/default selection is a later task.

Native calls must not look up Java static fields on every draw or pixel. Later
phases pass an effective enabled-feature bitmask across the Java/native boundary
where needed. `ImageOptimizationSettings` therefore provides an allocation-free
integer/long effective mask accessor. Do not create a native global settings
registry in phase 1.

Add a package-private no-op:

    ImageOptimizationSettings.triggerMemoryPressureForTest()

Phase 4 will connect this stable benchmark hook to the real manager. This lets
the phase-4 benchmark workload be committed before the memory-pressure
implementation exists.

## Plan of Work

### Milestone 0 — Bootstrap the branch and commit the ExecPlan

From a worktree that preserves unrelated local changes:

    git switch master
    git status --short
    git switch -c perf/image-opt-phase1-controls

Record `git rev-parse HEAD` in state. If the branch already exists, switch to it
and resume instead of recreating it.

Place this plan at its repository path, create the state/evidence/archive/report
skeletons, and commit them before implementation.

Suggested commit:

    docs(image): add optimization controls execplan

No build is allowed in this milestone. Validate only headers, Markdown size,
`git diff --check`, and commit message.

Acceptance: the plan and resumption files are committed and every new file is
below the size limit.

### Milestone 1 — Create the benchmark harness and capture S1

Create a reusable macOS benchmark runner under `scripts/`. Keep runner and
reporter separate if one file would approach the size limit.

Create package-private smoke benchmark support under
`TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/`. Reuse the existing
`registerImageMacSmoke` deployment pattern rather than creating another deploy
system.

Create `ImageOptimizationControlBenchmarkApp` before
`ImageOptimizationSettings`. Its S1 workload must compile against the current
master API and measure:

- repeated cached deferred draws;
- Image/Pipeline creation churn;
- the existing accounting-disabled path;
- RSS while the workload is active.

The workload must be large enough that recorded batches are at least tens of
milliseconds; do not measure individual nanosecond-scale operations with
millisecond TCVM timing.

Commit the benchmark source and runner before running S1.

Suggested commit:

    test(image): add optimization benchmark harness

At milestone end only, run the SDK and macOS Release builds. Redirect verbose
output to uncommitted logs.

SDK:

    cd TotalCrossSDK
    ./gradlew-agent dist -x test --no-daemon --console=plain

macOS software Skia:

    cmake -S TotalCrossVM -B build/image-opt-phase1-macos \
      -DCMAKE_BUILD_TYPE=Release -G Ninja \
      -DTC_GRAPHICS_SOFTWARE=ON \
      -DTC_RENDERER_SKIA=ON \
      -DTC_WINDOWING_SDL=ON
    ninja -C build/image-opt-phase1-macos tcvm Launcher

Deploy the benchmark with the exact newly built `libtcvm.dylib`, then run S1
with 60 samples. Save compact committed samples and machine metadata.

Commit S1 artifacts before implementing settings.

Suggested commit:

    test(image): record optimization control baseline

Acceptance: the benchmark can be reproduced from committed source and S1 data
identifies its exact pre-settings commit.

### Milestone 2 — Implement internal settings and diagnostics gating

Add `ImageOptimizationSettings` exactly as specified above. Keep it
package-private and absent from public SDK documentation.

Integrate `DIAGNOSTIC_ACCOUNTING` with existing Image/native-backing accounting
without adding synchronization to hot paths. Existing smoke methods that
explicitly reset/start accounting must continue to work even when the diagnostic
feature default is disabled.

Do not modify decode, pixel format, writePixels, cache policy, GPU backing, or
mmap behavior in this milestone. Their feature IDs exist but have no effect.

Add focused Java tests for tri-state validation, reset behavior, effective
feature mask, numeric settings, and the no-op memory-pressure test hook.

Update the control benchmark so post-settings S2 explicitly disables diagnostic
accounting and S3 explicitly enables it. Do not change the timed workload.

Suggested implementation commit:

    feat(image): add internal optimization controls

Before committing, use only source-level/header/diff validation. Defer Gradle
and native compilation to the milestone gate.

At milestone end, build the SDK and the same macOS software Skia configuration.
Run relevant Image unit tests and native smoke tests only now.

Run:

- S2 with all optimization features and diagnostic accounting disabled;
- S3 with only diagnostic accounting enabled.

If S2 is more than 5% slower or uses more than 5% additional peak RSS versus S1,
rerun with 200 samples. A confirmed regression must be fixed before completion.

Generate:

    .agent/benchmarks/image-opt-phase1-controls/control-plumbing/report.md

The report distinguishes disabled-path overhead from intentionally enabled
diagnostic overhead.

Suggested result commit:

    test(image): record optimization controls benchmark

Acceptance:

- S2 preserves current behavior and has no confirmed >5% disabled-path
  performance/RSS regression;
- S3 proves accounting can be intentionally enabled;
- all reserved feature toggles remain behaviorally inert;
- current Image smoke semantics remain unchanged.

### Milestone 3 — Finalize the reusable protocol and handoff

Finalize `.agent/design/image-optimization-benchmark-protocol.md` from the
measurement regime actually used. Do not copy raw tables into the plan.

Run final focused Image tests, SDK dist, macOS Release build only if the previous
milestone build is no longer at HEAD, and the relevant native image smokes.

Update state, evidence, archive, and editorial report. The editorial report must
state measured control overhead, exact sample regime, known measurement limits,
and the branch HEAD that phase 2 must use.

Suggested final commit:

    docs(image): complete optimization controls phase

## Validation and Acceptance

Only SDK and macOS validations are allowed. At applicable milestone closes:

    python3 scripts/validate-copyright-headers.sh --files <changed files>
    git diff --check

SDK Image tests:

    cd TotalCrossSDK
    ./gradlew-agent test --tests 'totalcross.ui.image.*' \
      --no-daemon --console=plain

SDK distribution:

    ./gradlew-agent dist -x test --no-daemon --console=plain

macOS native build: use the explicit software-Skia CMake configuration above.

Run only the native Image smoke tasks affected by changed accounting/benchmark
plumbing. Do not run unrelated platform matrices.

Final acceptance requires:

- branch created from recorded master SHA;
- plan and direct artifacts committed;
- no new file above 20 KiB/~600 lines;
- no public stable API added;
- all future optimization toggles default to disabled/current behavior;
- S1/S2/S3 control benchmark report committed;
- no confirmed >5% disabled-path regression;
- phase-2 handoff SHA recorded.

## Risks and Open Questions

If an internal feature would require changing public Image ABI/stable API,
stop that slice and record the blocker; do not invent another surface.

If benchmark timing is too coarse, increase operations per measured batch; do
not switch to a different benchmark regime between scenarios.

If the existing Gradle smoke registration cannot pass a benchmark mode through
the deployed executable, use `MainWindow.getCommandLine()` and invoke the
deployed executable directly from the benchmark runner. Do not introduce a
second runtime configuration mechanism.

## Idempotence and Recovery

Never delete or reset unrelated local changes.

Repeated benchmark runs may overwrite only the exact scenario artifact for the
same commit and workload. If the commit SHA differs, create a new result file or
replace the old one only when state explicitly marks the previous run invalid.

Build directories and `artifacts/image-opt-phase1-controls/` remain
uncommitted. On interruption, resume from state, verify HEAD and the last
committed scenario, and continue the recorded next action. Do not rerun completed
benchmarks unless source, binary revision, or measurement regime changed.

## Progress

- [ ] Bootstrap `perf/image-opt-phase1-controls` and commit this plan.
- [ ] Create benchmark harness and commit S1.
- [ ] Implement internal settings and diagnostics gating.
- [ ] Run and commit S2/S3 plus benchmark report.
- [ ] Final validation and phase-2 handoff.

## Decision Log

- Decision: use package-private process-global fine-grained settings, not a
  public policy API.
  Rationale: this phase exists for exhaustive controlled experimentation.
  Date: 2026-09-05.

- Decision: all new optimization defaults remain disabled throughout phases 1-4.
  Rationale: measured opt-in behavior must precede product default policy.
  Date: 2026-09-05.

- Decision: pass effective feature bits at native call boundaries rather than
  reading Java static fields from native hot paths.
  Rationale: preserve low overhead and avoid a native global settings lifecycle.
  Date: 2026-09-05.

- Decision: benchmark data and reports are committed; verbose build logs and
  generated binaries are not.
  Rationale: preserve reproducible direct evidence without repository noise.
  Date: 2026-09-05.

## Outcomes & Retrospective

Complete this section only at milestone boundaries. Record delivered behavior,
measured results, and deferred limitations. Do not write estimates as results.

## Revision Note

Initial plan authored for the post-`feat/image-native-backing` master. Phase 5
GPU representation optimizations such as KTX2/Basis, ASTC, ETC2, BCn, and R8
asset formats are explicitly outside this series.
