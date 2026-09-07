<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Phase 1 addendum: reserve later raster controls

This ExecPlan follows `AGENTS.md`, `.agent/PLANS.md`, and
`.agents/skills/logical-commits/SKILL.md`.

## Purpose / Big Picture

Keep Phase 1 compatible with the later raster work by reserving three stable
package-private feature positions. This addendum changes no raster execution;
the new controls remain inert and disabled by default.

## Working Set and Resume Protocol

Work only on `perf/image-opt-phase1-controls`. The active files are:

- `TotalCrossSDK/src/main/java/totalcross/ui/image/ImageOptimizationSettings.java`
- `TotalCrossSDK/src/test/java/totalcross/ui/image/ImageOptimizationSettingsTest.java`
- `.agent/design/image-optimization-benchmark-protocol.md`
- `.agent/state/image-opt-phase1-controls.md`
- `.agent/evidence/image-opt-phase1-controls.jsonl`
- `.agent/archive/image-opt-phase1-controls-history.md`
- `.agent/reports/image-opt-phase1-controls-editorial.md`
- `.agent/benchmarks/image-opt-phase1-controls/post-stabilization-rebaseline/`

Read state first on resume. Completed Phase 1 implementation and benchmark
history stays in the existing archive, editorial report, and historical
benchmark directories; do not rewrite those artifacts.

## Branch and Provenance

The authored Phase 1 base remains historical metadata:

    1898014784b2fba5716cc033e49520740b05f0dd

The current master baseline for this rebaseline is:

    7add0f29e9366a19d894237119a415416e6bb557

Record both SHAs in state, evidence, and the new rebaseline report. Do not
switch this worktree to another branch, push, merge, rebase, amend, or rewrite
history.

## Current Architecture and Scope

`ImageOptimizationSettings` is package-private, process-global, tri-state,
opt-in, and uses a `long` effective feature mask. Existing IDs 0-12 must stay
unchanged. Append exactly:

    RASTER_TARGET_COLORTYPE_CONVERSION = 13
    RASTER_PHYSICAL_VARIANT_CACHE = 14
    RASTER_PHYSICAL_IDENTITY_FOLDING = 15
    FEATURE_COUNT = 16

`effectiveMask()`, state validation, reset, and `describeForTest()` must cover
the new positions. `DEFAULT` remains disabled for optimization callers. The
existing `DIAGNOSTIC_ACCOUNTING` gate and clear/reset behavior must not change.
No color conversion, physical variant cache, physical identity folding, or
other Phase 2 optimization is implemented here.

Do not add controls for `USE_NATIVE_SWAP`, adaptive JPEG,
`getJpegBestFit`/`getJpegScaled`, `hwScaleW`/`hwScaleH`, or the invariant that
GPU rendering must not use `writePixels`.

## Benchmark Protocol Addendum

Benchmark only on macOS with the existing Release software-Skia configuration:

    -DCMAKE_BUILD_TYPE=Release
    -DTC_GRAPHICS_SOFTWARE=ON
    -DTC_RENDERER_SKIA=ON
    -DTC_WINDOWING_SDL=ON

Use identical fixtures, workload, warmup, machine, renderer, backend, and
sample regime. The rebaseline scenarios are:

- `S1/pre`: current master `7add0f29e9366a19d894237119a415416e6bb557`.
- `S2/post-disabled`: final Phase 1 code, every optimization explicitly
  disabled, including `DIAGNOSTIC_ACCOUNTING`.
- `S3/post-enabled`: the same Phase 1 code with only
  `DIAGNOSTIC_ACCOUNTING` enabled.

Use three warmup batches and 60 measured samples initially. If the existing
coefficient-of-variation or acceptance-boundary rule triggers, rerun the
affected comparison with 200 samples. A post-disabled median or peak-RSS
regression above 5% must be confirmed with 200 samples. If a peak-RSS
difference above 5% persists after that rerun, capture equivalent
memory/residency diagnostics at matched execution points before classifying it
as a regression; on macOS use `vmmap -summary` plus RSS and physical-footprint
measurements when available.

Write a new report and compact samples under:

    .agent/benchmarks/image-opt-phase1-controls/post-stabilization-rebaseline/

Keep `control-plumbing/` and `complete-diagnostic-gating/` unchanged. The
local macOS benchmark requirement is unchanged.

## Plan of Work

1. Complete the effective-mask test for all three new bits while preserving
   IDs 0-12. Land as `test(image): complete raster reservation mask coverage`.
2. Compact this active plan to this authoritative addendum and retain completed
   detail in the existing archive/editorial files. Record both provenance SHAs.
3. Build the current-master harness overlay and Phase 1 harness with the same
   Release software-Skia native runtime. Capture S1, S2, and S3 at 60 samples,
   escalating only under the protocol rules, and create the new report.
4. Run focused Image tests, SDK distribution, the required macOS native build,
   exact-dylib deployment, and benchmark/smoke execution. Record final state,
   evidence, and editorial handoff without changing historical artifacts.

## Validation and Acceptance

Required checks at completion:

    cd TotalCrossSDK
    ./gradlew-agent test --tests 'totalcross.ui.image.*' --no-daemon --console=plain
    ./gradlew-agent dist -x test --no-daemon --console=plain

    cmake -S TotalCrossVM -B build/image-opt-phase1-macos -G Ninja \
      -DCMAKE_BUILD_TYPE=Release -DTC_GRAPHICS_SOFTWARE=ON \
      -DTC_RENDERER_SKIA=ON -DTC_WINDOWING_SDL=ON
    ninja -C build/image-opt-phase1-macos tcvm Launcher

Also run the existing exact-dylib Image benchmark/smoke deployment path, the
three-scenario runner, focused copyright validation, and `git diff --check`.
Keep verbose logs under `artifacts/image-opt-phase1-controls/` or temporary
logs; do not commit generated builds or deployed binaries.

Acceptance requires:

- branch is `perf/image-opt-phase1-controls`;
- IDs 0-12 are unchanged and IDs 13-15 plus `FEATURE_COUNT=16` are present;
- all three new controls are tested default-disabled and runtime-inert;
- no excluded control or Phase 2 optimization is implemented;
- current-master and authored/base provenance are recorded separately;
- the new S1/S2/S3 report uses identical workload/regime and remains historical
  separate from prior reports;
- any required 200-sample escalation and matched RSS diagnostics are recorded;
- the active plan is below 20 KiB and approximately 600 lines.

## Risks and Open Questions

The current master does not contain the Phase 1 benchmark harness, so S1 may
use a temporary harness overlay in a detached worktree while retaining the
exact current-master production sources. Do not commit that overlay to master.
If native or smoke results differ materially from the prior workload, stop and
record the discrepancy rather than changing the workload.

## Idempotence and Recovery

Never alter unrelated local files or historical benchmark artifacts. Temporary
detached worktrees, build directories, deployed apps, and verbose logs are
disposable generated state; committed samples must use a new directory for the
new baseline. Resume from state and verify each scenario SHA before rerunning.

## Progress

- [x] Reserve IDs 13-15 and raise `FEATURE_COUNT` to 16.
- [x] Add tests for ID stability, masks, reset, descriptions, and inertness.
- [x] Record the explicit bit-15 effective-mask assertion.
- [x] Compact the active plan to the authoritative addendum and preserve the
  authored SHA as historical metadata.
- [ ] Record current master `7add0f29e9366a19d894237119a415416e6bb557` and
  capture the post-stabilization S1/S2/S3 rebaseline.
- [ ] Run final required validations and record the Phase 2 rebase handoff.

## Decision Log

- Decision: reserve only the three requested raster IDs after the existing
  sequence and keep them inert/default-disabled.
  Rationale: establish stable positions without changing Phase 1 behavior.

- Decision: treat `1898014784b2fba5716cc033e49520740b05f0dd` as the authored
  historical base and `7add0f29e9366a19d894237119a415416e6bb557` as the current
  master baseline for the new comparison.
  Rationale: correct provenance after master stabilization without rewriting
  historical evidence.

- Decision: require matched memory/residency diagnostics for a persistent
  post-rerun peak-RSS difference above 5%, using macOS `vmmap -summary`, RSS,
  and physical footprint when available.
  Rationale: separate residency effects from sampling noise before regression
  classification.

## Outcomes & Retrospective

The addendum implementation and focused tests are complete. The current
master rebaseline and final Phase 2 handoff remain to be recorded in the new
post-stabilization report, state, evidence, and editorial addendum.
