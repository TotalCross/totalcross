<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Phase 2 raster extension 01: rebaseline and fold physical identity

This ExecPlan follows `AGENTS.md`, `.agent/PLANS.md`, and
`.agents/skills/logical-commits/SKILL.md`.

Execute this plan first. Then execute
`.agent/plans/exec-plan-image-opt-phase2-raster-extension-02.md`.

## Purpose / Big Picture

Extend Phase 2 after its rebase onto the frozen Phase 1 branch. Preserve and
revalidate the five existing Phase 2 lossless raster optimizations, then add the
first missing raster-plan optimization: physical identity folding.

At the end of this plan:

- the rebased Phase 2 runtime is validated against the frozen Phase 1 base;
- the existing Phase 2 correctness fixes remain intact;
- benchmarks for all three new raster controls exist before their
  implementation;
- `RASTER_PHYSICAL_IDENTITY_FOLDING` can remove a logically requested scale
  when the final physical source and destination are already 1:1;
- the GPU path remains untouched by raster-only fast paths;
- the branch is ready for target-color conversion and physical variant caching
  in the second plan.

This is a continuation of Phase 2, not a new optimization phase.

## Working Set and Resume Protocol

Work only on:

    perf/image-opt-phase2-raster

The frozen Phase 1 base is:

    a8a9480bd61aa510de423569af494d8dde69e8f2

The expected Phase 2 tip when this plan was authored is:

    a225d10165b8b60c4bf7bf2f95f5bf3b395f3a92

At first execution, verify that the frozen Phase 1 commit is an ancestor of
HEAD. If HEAD differs from the expected tip, inspect only commits after the
expected tip. Continue automatically if they are documentation/evidence-only.
Stop and report if they change runtime image behavior.

Create and use these continuation files:

    .agent/state/image-opt-phase2-raster-extension.md
    .agent/evidence/image-opt-phase2-raster-extension.jsonl
    .agent/archive/image-opt-phase2-raster-extension-history.md
    .agent/reports/image-opt-phase2-raster-extension-editorial.md
    .agent/benchmarks/image-opt-phase2-raster-extension/

The state file is the first normal read after interruption. Rewrite it
compactly. Read evidence only for a specific milestone/benchmark. Do not
normally reread the old 629-line Phase 2 ExecPlan; use `rg` for only the needed
historical section.

Stable documents:

- `.agent/design/image-optimization-benchmark-protocol.md` defines S1/S2/S3,
  60-to-200 escalation, RSS handling, and Phase 2+ platform policy. Read it
  before preparing benchmark scenarios and at final acceptance.
- `.agent/state/image-opt-phase2-raster.md` records the completed pre-extension
  Phase 2 state. Read it once during bootstrap, then use the extension state.
- `.agents/skills/logical-commits/SKILL.md` is mandatory for every commit.

Use `rg`, narrow `sed` ranges, and direct reads; do not reconstruct history.

## Progress

- [x] (2026-09-07) Bootstrap continuation state and verify branch ancestry;
  evidence is recorded in the extension state/evidence files.
- [x] (2026-09-07) Add all three physical-plan benchmark workloads before
  implementation; SDK and macOS software-Skia build gates passed.
- [x] (2026-09-07) Revalidate/rebaseline the five existing Phase 2
  optimizations with true-base S1, disabled S2, and enabled S3; the S3
  comparison was escalated to 200 samples after the 60-sample CV exceeded 5%.
- [x] (2026-09-07) Implement and validate `RASTER_PHYSICAL_IDENTITY_FOLDING`.
- [x] (2026-09-07) Hand off exact continuation state to extension plan 02.

## Current Architecture and Scope

Phase 1 reserves these controls and Phase 2 propagates their effective mask to
native draw/decode boundaries:

    0  DECODE_ZERO_COPY
    1  RASTER_OPACITY_METADATA
    2  RASTER_OPAQUE_WRITE_PIXELS
    3  RASTER_ROW_READBACK
    4  RASTER_DIRECT_COLOR_MATERIALIZATION
    13 RASTER_TARGET_COLORTYPE_CONVERSION
    14 RASTER_PHYSICAL_VARIANT_CACHE
    15 RASTER_PHYSICAL_IDENTITY_FOLDING

IDs 13-15 are currently reserved. Do not renumber any feature.

Existing Phase 2 behavior that must survive unchanged:

- zero-copy decode owns the final decode buffer exactly once, including decoder
  failure/longjmp paths, and failure leaves a retryable non-backed state;
- adaptive JPEG denominator behavior from master remains authoritative;
- APPLY_COLOR2 preserves the legacy alpha calculation;
- mutable `Graphics` writes advance backing generation and invalidate opacity;
- opacity metadata is proof-based: only `OPAQUE` is a fast-path proof;
- `RASTER_OPAQUE_WRITE_PIXELS` remains independently testable from opacity
  metadata and may perform one cached fallback opacity scan per backing
  generation;
- ordinary native draws and trivial draw plans use the same conservative
  writePixels eligibility;
- readback/color materialization preserves public ARGB and hidden-RGB semantics;
- all optimization controls remain opt-in/default-disabled.

Relevant native paths:

    TotalCrossVM/src/nm/ui/skia/skia_image_backing_internal.h
    TotalCrossVM/src/nm/ui/skia/skia_image_backing.cpp
    TotalCrossVM/src/nm/ui/skia/skia_image_geometry.cpp
    TotalCrossVM/src/nm/ui/skia/skia_image_geometry_materialize.cpp
    TotalCrossVM/src/nm/ui/skia/skia_image_geometry_internal.h
    TotalCrossVM/src/nm/ui/skia/skia.h
    TotalCrossVM/src/nm/ui/ImageTestAccounting_c.h

Relevant SDK/test paths:

    TotalCrossSDK/src/main/java/totalcross/ui/image/Image.java
    TotalCrossSDK/src/main/java/totalcross/ui/image/NativeImageBacking.java
    TotalCrossSDK/src/main/java/totalcross/ui/image/ImageOptimizationSettings.java
    TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/
    TotalCrossSDK/src/test/java/totalcross/ui/image/
    TotalCrossSDK/build.gradle
    scripts/run-image-optimization-benchmark.py

Do not implement compact storage formats, cache byte budgets, memory-pressure
eviction, GPU-only backing, mmap, KTX/compressed textures, or public APIs.

## Architecture Contract

Introduce one internal concept named `RasterPhysicalPlan` or an equivalently
clear private type. It is an ephemeral per-draw decision object; it is not a
public API and is not persisted as an image representation in this plan.

Derive it from `SkiaImageDrawPlanData`, the compiled geometry, the target
canvas, and the source backing. It must expose enough facts for later work:

- final physical output width and height;
- normalized source pixel rectangle/frame;
- whether the pipeline is pure geometry;
- whether the effective physical transform is exact 1:1;
- whether the target is an eligible software-raster canvas;
- whether color/alpha/filter/clip/matrix state disqualifies raster fast paths.

Use existing `skia_image_geometry_compile()` logic as the source of truth for
geometry semantics. Do not create a second independent transformation engine.

For persisted output dimensions, follow the existing materialization contract:
use `ceil(outputWidth * outputContentScale)` and
`ceil(outputHeight * outputContentScale)`, with the existing frame-layout rules.
Use `destinationScale` and actual destination coordinates when deciding whether
the final draw maps those physical pixels 1:1 to the target.

Physical identity folding is allowed only when all of these are true:

- `RASTER_PHYSICAL_IDENTITY_FOLDING` is enabled;
- the target is the supported software-raster path;
- the remaining pipeline contains only frame selection/layout, crop, scale, or
  smooth-scale operations;
- no rotation, touch-up, fade, alpha, apply-color, transparent-color, or other
  color stage remains;
- effective alpha is 255;
- the effective source pixel rectangle and destination physical rectangle have
  identical integer width and height;
- the compiled transform reduces to unit scale plus integer translation for the
  pixels being drawn;
- no active target matrix/clip/save state violates the existing writePixels
  safety rules;
- source and destination bounds are valid.

Do not fold rotation in this phase. Do not approximate a near-identity
resample. If exact identity cannot be proved, use the existing path.

When identity is proven:

1. do not materialize an intermediate scaled raster;
2. do not execute a resampling draw solely because the logical pipeline contains
   SCALE/SMOOTH_SCALE;
3. reuse the existing root/source raster and normalized source rectangle;
4. attempt the existing conservative `writePixels` path only if all of its
   independent eligibility checks pass;
5. otherwise draw the source through the normal Skia path without an
   intermediate resample.

`hwScaleW/H` is dynamic presentation state. It may participate in the per-draw
identity calculation, but this plan must not cache a raster because of hwScale.

The canonical acceptance cases are:

    source 200x200 physical
    smooth result 100x100 logical
    outputContentScale 2
    => final physical 200x200
    => identity fold, zero resample, zero materialization

and:

    source 400x400 physical
    smooth result 100x100 logical
    outputContentScale 2
    => final physical 200x200
    => not identity; normal resample in this plan

## Diagnostic Contract

Extend the existing package-private/native test accounting path. Do not expose a
public SDK API.

Add counters equivalent to:

    physicalIdentityAttempts
    physicalIdentityHits
    physicalIdentityFallbacks
    physicalIdentityResamplesAvoided

Counter increments must be observational only. With the feature disabled, the
runtime path must remain the rebased Phase 2 behavior.

The second plan will add target-color and variant counters. Keep this
instrumentation small and reusable.

## Benchmark Contract

Reuse existing fixtures, Phase 2 runners, and benchmark support. Do not add a
new generic benchmark framework.

Before implementing IDs 13-15, commit benchmark workloads covering:

1. physical identity:
   - 200 -> 100 logical at physical scale 2;
   - 400 -> 100 logical at physical scale 2 as a non-identity control;
   - nearest and smooth scale;
   - an ineligible alpha/matrix/clip case outside the primary timing.
2. target color conversion, for plan 02:
   - opaque source to RGBA8888 control target;
   - opaque source to BGRA8888 target;
   - opaque source to RGB565 target;
   - translucent source as a forced fallback.
3. physical variant reuse, for plan 02:
   - repeated 400 -> 100 logical at physical scale 2;
   - first-use versus repeated-use behavior;
   - mutation invalidation;
   - changed target size/key eviction.

If ordinary TotalCross surfaces cannot deterministically select all target
color types, add a test-only native/package-private in-memory raster-surface
helper. It may create only RGBA8888, BGRA8888, and RGB565 raster surfaces. It
must never be called by production code or become public API.

Every timed batch must run for at least 30 ms. Keep hashes/parity checks outside
the timed region.

Use three warmup batches and 60 measured samples. Escalate the affected
comparison to 200 samples when CV exceeds 5%, the result is near the acceptance
boundary, or a post-disabled elapsed/RSS regression exceeds 5%. If peak RSS
remains above 5% after the 200-sample rerun, capture matched
`vmmap -summary`, RSS, and physical-footprint evidence before classification.

Commit raw benchmark samples/reports. Do not commit build logs, binaries,
deploy outputs, or ordinary generated files.

## Plan of Work

### Milestone 0 — Bootstrap the continuation

Verify:

    git switch perf/image-opt-phase2-raster
    git status --short
    git rev-parse HEAD
    git merge-base --is-ancestor \
      a8a9480bd61aa510de423569af494d8dde69e8f2 HEAD

Do not stash, reset, clean, or overwrite unrelated work.

Read once:

    sed -n '1,180p' .agent/state/image-opt-phase2-raster.md
    sed -n '1,190p' .agent/design/image-optimization-benchmark-protocol.md

Create the continuation state/evidence/archive/editorial skeletons and record:

- frozen Phase 1 base SHA;
- actual starting Phase 2 HEAD;
- active plan path;
- active milestone;
- relevant paths;
- next exact command;
- build/platform restrictions below.

Do not rewrite historical benchmark artifacts.

Commit:

    docs(image): start phase two raster extension

No build is required for this documentation-only milestone.

### Milestone 1 — Commit workloads and rebaseline rebased Phase 2

Create all three new benchmark workloads before any ID 13-15 implementation.
Keep new files below 20 KiB and approximately 600 lines; split support classes
rather than creating a large benchmark app.

Commit benchmark sources first:

    test(image): add physical raster benchmarks

Then perform one related milestone-end validation/build:

SDK:

    cd TotalCrossSDK
    ./gradlew-agent test --tests 'totalcross.ui.image.*' \
      --no-daemon --console=plain
    ./gradlew-agent dist -x test --no-daemon --console=plain

macOS native:

    cmake -S TotalCrossVM -B build/image-opt-phase2-extension-macos -G Ninja \
      -DCMAKE_BUILD_TYPE=Release \
      -DTC_GRAPHICS_SOFTWARE=ON \
      -DTC_RENDERER_SKIA=ON \
      -DTC_WINDOWING_SDL=ON
    ninja -C build/image-opt-phase2-extension-macos tcvm Launcher

Use the exact built `libtcvm.dylib` for smokes/benchmarks.

Rebaseline the existing five Phase 2 features with one integrated workload:

- S1: frozen Phase 1 `a8a9480...`;
- S2: rebased Phase 2 runtime with all optimization features explicitly
  disabled;
- S3: the same Phase 2 runtime with only features 0-4 enabled.

Reuse the committed true-base adapter pattern. Do not modify the frozen Phase 1
tree. Record exact production SHAs and harness SHA separately.

Also run focused correctness cases for:

- adaptive JPEG denominator 2/4/8 versus independent full decode;
- fresh/cached targeted decode equivalence;
- APPLY_COLOR2 alpha parity;
- mutation invalidating opacity;
- zero-copy failure/retry ownership;
- writePixels metadata-disabled fallback scan;
- getPixels/readback parity.

Do not rerun every historical individual benchmark unless this integrated
rebaseline exposes a failure.

Commit samples/report/evidence:

    test(image): record rebased raster baseline

Acceptance:

- no correctness/hash regression;
- S2 disabled behavior has no confirmed >5% elapsed or RSS regression after
  required escalation;
- the five existing feature counters prove the S3 workload exercises them;
- historical pre-rebase evidence remains unchanged.

At the end of this milestone capture the physical-identity S1 using the same
production commit with ID 15 disabled and all other optimization features
disabled.

### Milestone 2 — Implement physical identity folding

Implement the `RasterPhysicalPlan` scaffold and ID 15 behavior exactly as
specified above.

Keep the identity decision adjacent to the existing geometry/draw path. Do not
duplicate `compileGeometry`. Keep the current `tryWritePixels` safety helper as
the final authority for actual writePixels use.

Add focused Java/native smoke assertions for:

- 200 -> 100 at scale 2: identity hit and zero resample/materialization;
- 400 -> 100 at scale 2: identity fallback;
- smooth and nearest identity;
- alpha !=255 fallback;
- non-identity canvas matrix fallback;
- active clip/save fallback;
- crop/frame exact identity only when integer source/destination rectangles
  match;
- hwScale does not create persistent materialization;
- adaptive JPEG already decoded to the required physical dimensions is not
  resampled again.

No rotation folding.

Commit runtime/tests:

    perf(image): fold physical identity draws

At this milestone end, run the same SDK and macOS Release software-Skia build
commands from Milestone 1, plus the related native image smokes.

Run identity S2/S3:

- S2: post-implementation, ID 15 disabled, every other optimization disabled;
- S3: same runtime, only ID 15 enabled.

Require exact output parity. Record counters and RSS.

Commit benchmark artifacts:

    test(image): record identity folding results

### Milestone 3 — Consolidate and hand off

Update continuation state/evidence/archive/editorial with factual results only.
Do not duplicate raw tables.

Record:

- final HEAD of this plan;
- exact target-color and physical-variant S1 production SHA to use in plan 02;
- whether 60 or 200 samples were authoritative for each comparison;
- any matched memory diagnostics;
- unresolved blockers only.

Commit:

    docs(image): hand off raster variant work

No additional build is needed if runtime code has not changed after the
Milestone 2 build.

## Validation and Acceptance

Build operations in this plan are permitted only for the SDK and macOS native
target, and only at the end of the related milestones above. Do not build
Android, Windows, Linux, or iOS.

Native smokes may run only at the end of a related milestone or at final plan
completion.

Before every commit:

1. invoke `.agents/skills/logical-commits/SKILL.md`;
2. stage only intended task paths;
3. run focused header validation for changed first-party files;
4. run `git diff --check --cached`;
5. inspect `git diff --cached --stat` and the staged diff.

Do not amend or rewrite history. Do not push.

New files created by this plan must remain <=20 KiB or approximately 600 lines.
Existing files must not be refactored merely to satisfy that limit.

## Surprises & Discoveries

Record only observations that change the remaining implementation. Move resolved
detail to the extension archive at milestone boundaries.

If the current geometry contract makes the canonical 200 -> 100 at scale 2 case
non-identity, stop and document the exact computed transform and metadata.
Do not weaken identity proof to make the benchmark pass.

## Decision Log

- Decision: keep all eight lossless/raster controls in Phase 2.
  Rationale: compact storage and lifecycle remain separate later phases.
- Decision: simplify physical identity before any materialization/cache.
  Rationale: the cheapest raster is the raster that does not need to be made.
- Decision: physical identity is proof-based and exact.
  Rationale: no image-quality change is allowed.
- Decision: do not cache rotation or dynamic hwScale in this continuation.
  Rationale: avoid cache churn and fill/edge semantic risk.
- Decision: preserve all historical Phase 2 reports.
  Rationale: rebase evidence is additive, not a rewrite.

## Risks and Open Questions

There are no architectural choices left to the executor.

Stop the affected slice and report if:

- exact identity cannot be proven without changing image semantics;
- adaptive JPEG metadata would need to be redefined;
- the implementation requires a public API;
- disabled ID 15 changes output or confirmed performance/RSS beyond the
  protocol threshold;
- a required fix belongs to master independently of Phase 2.

A pre-existing unrelated functional bug should be reported separately; do not
silently absorb it into this Phase 2 extension.

## Idempotence and Recovery

Benchmark directories are append/new-slice only. Never overwrite historical
samples.

Temporary build directories, exact-dylib deployments, and verbose logs are
disposable and uncommitted.

After interruption:

1. read `.agent/state/image-opt-phase2-raster-extension.md`;
2. verify branch and HEAD;
3. inspect only the active paths named there;
4. reuse completed benchmark samples when their recorded SHA, workload, build,
   and regime still match;
5. rerun only the incomplete scenario or the protocol-required escalation.

Preserve unrelated modified/untracked files.

## Outcomes & Retrospective

Update at milestone boundaries with measured facts only. At completion, state
whether identity folding shipped behind its toggle, its measured effect, and
any limitation carried into plan 02.

## Revision Note

Initial continuation plan after the Phase 2 rebase. It intentionally separates
rebaseline/identity work from target-color/variant-cache work so each ExecPlan
stays small, resumable, and below the repository artifact-size limit.
