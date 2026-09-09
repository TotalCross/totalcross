<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# ExecPlan 01/03: Reproduce and measure clipped lazy-image raster scrolling

This is the first of three sequential ExecPlans. Keep `Progress`, `Surprises &
Discoveries`, `Decision Log`, and `Outcomes & Handoff` current while executing
it. It follows `.agent/PLANS.md`.

Save the three supplied plans as:

    .agent/plans/image-scroll-raster-fast-path-01.md
    .agent/plans/image-scroll-raster-fast-path-02.md
    .agent/plans/image-scroll-raster-fast-path-03.md

Plan 02 must not start until this plan is complete and committed.

## Purpose / Big Picture

Create a clean branch from `perf/image-jpeg-factories-lazy`, reproduce the
software-raster scrolling regression with normal UI clipping, make the relevant
fast-path decisions observable, and add a dedicated GitHub Actions validation
workflow.

Do not change production raster behavior in this plan. Its output is a
counter-proven baseline that Plan 02 can act on without reconstructing the
investigation.

Target real workload:

- 120 square images;
- 3 images per row;
- about 4 visible rows;
- vertical scrolling through the real clipped `ScrollContainer` /
  `ClippedContainer` hierarchy;
- deterministic repository-local JPEG input;
- logical scaling producing at least 2x physical output;
- clipping enabled;
- cold top-to-bottom, warm bottom-to-top, then second warm top-to-bottom.

The expected problem is per-visible-image/per-repaint work, not drawing all 120
images every frame: `ClippedContainer` already culls offscreen children.

## Progress

Use UTC timestamps.

- [x] Record actual fetched base SHA: `9ca331017d32ca553155d7fda59b22378fd8198a`.
- [x] Create `perf/image-scroll-raster-fast-path` in the isolated sibling worktree.
- [x] Commit all three ExecPlans (`7f897cdef`).
- [x] Add deterministic clipped-scroll fixture (`840f871e7`).
- [x] Add/reset/report required raster diagnostics (`b17763cea`).
- [x] Add dedicated Windows/Linux GitHub validation workflow (`454bb64fc`).
- [x] Run milestone-end SDK + macOS native validation only.
- [x] Run remote Windows x86-64, Linux x86-64, Linux ARM64 baseline; Linux passed and Windows x64 exposed a pre-existing native crash.
- [x] Record baseline and handoff; commit the updated plan.

## Surprises & Discoveries

Known at authoring time:

- Observed base branch HEAD:
  `9ca331017d32ca553155d7fda59b22378fd8198a`. The branch may advance; record the
  actual fetched SHA rather than forcing this one.
- `Image.getSmoothScaledInstance(...)` defers `ImagePipeline.SMOOTH_SCALE`.
- device `Graphics.drawImage(...)` prefers a native draw plan; normal two-arg
  drawing enables clipping.
- the generic Skia smooth geometry path can cubic-resample.
- `skia_setClip(...)` saves the canvas and clips it.
- physical raster eligibility currently depends on an unmodified canvas/full
  device clip.
- existing physical-identity and physical-variant benchmarks use
  `drawImage(..., false)`, so they do not model ordinary UI clipping.
- first use of an encoded image can synchronously decode/materialize during
  paint; warm passes distinguish that cost from repeated raster work.
- the physical-variant cache key is already position-independent.
- the existing dependency-preparation script stages most Windows libraries as
  `windows/x86`; the dedicated workflow fetches each lane's exact architecture
  so its Windows job configures and runs a real x86-64 binary.
- `physicalVariantMaterializations` is the existing counter mapped to the
  plan's `physicalVariantStores`; generic geometry and smooth-resample counts
  are recorded only for successful final generic geometry draws.
- The Linux validation job now mirrors `.github/workflows/build.yml` directly:
  it uses `totalcross/linux-amd64:v1.0.7` and
  `totalcross/linux-arm64:v1.0.7`, mounts the source/build/cache paths, and
  runs the same in-container `cmake ... -G Ninja && ninja` sequence. Host apt
  packages are only for the post-build Xvfb runtime.
- The Linux native launcher required `PATH_MAX` for glibc's fortified
  `realpath`; the old 1024-byte `MAX_PATHNAME` caused a buffer-overflow abort
  before module loading. The runtime bundle also needs every `TotalCrossSDK/dist/vm/*.tcz`,
  not only the application TCZ.
- The required Windows x86-64 lane builds successfully but the fresh runtime
  exits with `0xC0000005` before Java fixture startup. WinDbg reports the
  fault in `tcvm!trace` from `startVM`; this remains an upstream/runtime
  compatibility issue outside the Linux raster baseline.

Append only discoveries that materially affect Plan 02.

## Decision Log

These decisions are fixed for the sequence:

- Preserve lazy encoded images and logical-unit scaling.
- Do not add prefetch, async decode, a background worker, or a new public API.
- Do not add a second physical-image cache.
- Primary reproduction uses the application-equivalent optimization profile:

      DECODE_ZERO_COPY
      RASTER_OPACITY_METADATA
      RASTER_OPAQUE_WRITE_PIXELS
      RASTER_ROW_READBACK
      RASTER_DIRECT_COLOR_MATERIALIZATION
      RASTER_TARGET_COLORTYPE_CONVERSION
      RASTER_PHYSICAL_IDENTITY_FOLDING

  Keep `RASTER_PHYSICAL_VARIANT_CACHE` and `STORAGE_RGB565` disabled there.
- Add a separate scaled-source scenario with
  `RASTER_PHYSICAL_VARIANT_CACHE` explicitly enabled.
- Timing is observational. Counters and pixel correctness are the stable gates.
- Required remote matrix: Windows x86-64, Linux x86-64, Linux ARM64.
  Windows ARM is explicitly out of scope.
- Every new file must be <= 20 KiB and approximately <= 600 lines. Do not
  refactor existing large files merely to reduce size.
- Local builds are forbidden during implementation. Only at this plan's final
  milestone may the agent build the SDK and macOS native targets.
- Native smoke may run only at the milestone end.
- Commit normal source/plan/test/workflow artifacts. Do not commit build
  directories, binaries, packages, screenshots, or ordinary build/runtime logs.

## Outcomes & Handoff

Fill this before Plan 02 begins.

Record:

- base SHA and current checkpoint SHA;
- exact fixture location and invocation;
- names/mapping of diagnostics;
- cold/warm/warm counters and timings;
- clipped versus `doClip=false` control counters;
- macOS local result;
- GitHub Actions run URL/ID and result for each required lane;
- exact functions proven to reject clipped fast paths;
- any unexpected dominant path.

Baseline handoff:

- Base SHA: `9ca331017d32ca553155d7fda59b22378fd8198a`.
- Current checkpoint: `85c5b731006756f9a8decb0a28ad06918b5f79a0`.
- Fixture: `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageScrollRasterFastPathBenchmarkApp.java`.
  The Gradle task is `jarImageScrollRasterFastPathBenchmark`; the native
  launcher runs the generated `ImageScrollRasterFastPathBenchmarkApp.tcz`.
- Diagnostic mapping: physical identity attempts/hits/fallbacks,
  physical variant lookups/hits/stores, target-color attempts/hits/fallbacks,
  opaque write-pixel attempts/hits, generic geometry draws, and smooth
  resample draws. The counters are exposed through package-private SDK test
  accessors and native bindings.
- Local validation: `./TotalCrossSDK/gradlew-agent test --no-daemon
  --console=plain`, `./TotalCrossSDK/gradlew-agent dist -x test --no-daemon
  --console=plain`, and the fixture jar task passed. macOS native CMake/Ninja
  build and fresh deploy/run passed with `overallPass=true`; clipped and
  unclipped pixel hash was `00009D4A00006964` at scale 2. Clipped cold pass:
  37 frames, 444 identity attempts, 0 hits, 444 fallbacks, 444 generic and
  smooth draws. Unclipped cold pass: 37 frames, 444 identity attempts, 444
  hits, 0 fallbacks, and 0 generic/smooth draws. Warm reverse/forward passes
  and the variant-cache scenario passed.
- Remote workflow:
  `https://github.com/TotalCross/totalcross/actions/runs/34295746881`.
  Linux x86-64 and Linux ARM64 passed build, native identity checks, deploy,
  and runtime markers. Windows x86-64 built but failed before fixture output
  with process exit `-1073741819` (`0xC0000005`); cdb placed the fault in
  `tcvm!trace` during `startVM`.
- The clipped path is rejected by the existing full-device-clip/modified-canvas
  eligibility checks in `physicalIdentityCanvasEligible` and
  `physicalVariantCanvasEligible`; the unclipped control reaches the physical
  identity fast path. No production raster fast-path behavior was changed in
  this plan.

Plan 02 should read only this section plus its own plan unless a specific source
file needs inspection.

## Context and Orientation

Repository: `TotalCross/totalcross`

Base:

    perf/image-jpeg-factories-lazy

New branch:

    perf/image-scroll-raster-fast-path

Use targeted `git ls-files` / `rg` once to locate the exact files containing:

- `Image`, `ImagePipeline`, `ImageOptimizationSettings`, `Graphics`;
- `skiaDrawGeometryPlan`;
- `buildRasterPhysicalPlan`;
- `physicalVariantCanvasEligible`;
- `skia_image_backing_draw_geometry_to_surface`;
- `skia_setClip`;
- `ImageRasterPhysicalIdentityBenchmarkApp`;
- `ImageRasterVariantBenchmarkApp`.

Place the new fixture beside the existing image raster benchmark apps and reuse
their launch/test conventions.

Read once at start:

- repository `AGENTS.md`;
- `.agents/skills/logical-commits/SKILL.md`;
- `.agent/guides/macos-native-runtime-validation.md`;
- this plan.

Do not repeatedly reread old image optimization reports/branches.

## Plan of Work

### Milestone 0 — Branch and plan bootstrap

Do not build.

If the worktree is clean:

    git fetch origin
    git switch perf/image-jpeg-factories-lazy
    git merge --ff-only origin/perf/image-jpeg-factories-lazy
    BASE_SHA="$(git rev-parse HEAD)"
    git switch -c perf/image-scroll-raster-fast-path
    printf '%s\n' "$BASE_SHA"

If unrelated pre-existing edits make switching unsafe, do not stash/reset/clean
them. Create a sibling worktree instead:

    git fetch origin
    git worktree add ../totalcross-image-scroll-raster-fast-path \
      -b perf/image-scroll-raster-fast-path \
      origin/perf/image-jpeg-factories-lazy

Save all three plans under `.agent/plans/`. Verify each new file:

    wc -c .agent/plans/image-scroll-raster-fast-path-0*.md
    wc -l .agent/plans/image-scroll-raster-fast-path-0*.md

Commit the sequence together:

    git status --short
    git diff --check
    git add \
      .agent/plans/image-scroll-raster-fast-path-01.md \
      .agent/plans/image-scroll-raster-fast-path-02.md \
      .agent/plans/image-scroll-raster-fast-path-03.md
    git diff --cached --check
    git diff --cached
    git commit -m "docs(plan): define image raster fast-path sequence"

### Milestone 1 — Deterministic real-scroll reproduction

Do not build until the end of Milestone 3.

Create one native-deployable fixture beside the existing raster benchmark apps.

It must:

1. construct 120 deterministic square JPEG images from repository-local data;
2. place 3 per row in a real clipped scrolling hierarchy;
3. size the viewport for about 4 visible rows;
4. drive scrolling programmatically and deterministically;
5. run cold forward, warm reverse, second warm forward passes;
6. reset diagnostics before each measured pass;
7. report elapsed time and counter deltas;
8. run with the application-equivalent feature mask above;
9. include an optional `doClip=false` control, never used as the main gate;
10. include a compact non-identity scaled-source scenario with
    `RASTER_PHYSICAL_VARIANT_CACHE` enabled.

Do not use network I/O or manual interaction. Do not assert a hard millisecond
budget.

Commit:

    test(image): add clipped scroll raster workload

### Milestone 2 — Diagnostics and dedicated CI workflow

Do not build yet.

Reuse existing accounting structures. Add only missing counters:

    physicalIdentityAttempts
    physicalIdentityHits
    physicalIdentityFallbacks
    physicalVariantLookups
    physicalVariantHits
    physicalVariantStores
    targetColorAttempts
    targetColorHits
    targetColorFallbacks
    writePixelsAttempts
    writePixelsHits
    genericGeometryDraws
    smoothResampleDraws

Equivalent existing names are acceptable; record the mapping in this plan.
Counters must be resettable and cheap/no-op when diagnostics are disabled.
Prefer package-private/test-only exposure. Do not add hot-path console logging.

Commit diagnostics separately:

    test(image): expose raster fast-path diagnostics

Create:

    .github/workflows/image-scroll-raster-validation.yml

Requirements:

- manual `workflow_dispatch`;
- validate the exact pushed SHA;
- Windows x86-64 target;
- Linux x86-64 target;
- Linux ARM64 target;
- no Windows ARM;
- reuse dependency preparation/build conventions from current `build.yml`;
- build the SDK needed by the fixture;
- build fresh native `tcvm`/Launcher for the lane;
- run the native-deployed fixture against that runtime;
- use Xvfb on Linux if a display is required;
- fail on fixture assertion, runtime load error, or wrong runtime identity;
- write concise counters/timings to the Actions job summary;
- logs/build outputs may be uploaded as ephemeral Actions artifacts, preferably
  on failure, but never committed.

For Windows, actually configure an x86-64 validation target. If repository
dependency tooling cannot produce runnable x86-64 without a broader Windows
architecture port, record that blocker and leave the lane failing. Do not label
a Win32 binary as x86-64 and do not broaden this plan into a platform port.

Keep the workflow itself <= 20 KiB/~600 lines.

Commit:

    ci(image): add cross-platform raster validation workflow

### Milestone 3 — Baseline validation and handoff

Only now may local builds run.

SDK:

    cd TotalCrossSDK
    ./gradlew-agent test --no-daemon --console=plain
    ./gradlew-agent dist -x test --no-daemon --console=plain
    cd ..

macOS native only:

    cmake -S TotalCrossVM -B build-image-scroll-raster \
      -DCMAKE_BUILD_TYPE=Release \
      -DCMAKE_OSX_ARCHITECTURES=arm64 \
      -G Ninja
    cmake --build build-image-scroll-raster \
      --target tcvm Launcher --parallel

Run the native macOS fixture following
`.agent/guides/macos-native-runtime-validation.md`. A JavaSE/AWT Launcher run is
not native validation. Prove the deployed app uses the freshly built
`libtcvm.dylib`.

Record baseline for:

- clipped application-equivalent JPEG cold/warm/warm;
- unclipped control;
- clipped variant-cache scenario.

Expected signature, to verify rather than assume:

- clipped identity/variant hits are much lower than unclipped equivalents;
- generic geometry and/or smooth resampling grows on repeated warm clipped draws.

If counters disprove this, do not implement the preselected clip fix blindly.
Record the counter-proven dominant path in `Surprises & Discoveries`; Plan 02
may then touch only that proven eligibility/order issue while preserving its
architectural constraints.

Push exact HEAD and run the dedicated workflow on all required lanes.

Update `Outcomes & Handoff`, then:

    git status --short
    git diff --check
    git add .agent/plans/image-scroll-raster-fast-path-01.md
    git diff --cached --check
    git diff --cached
    git commit -m "docs(plan): record clipped raster baseline"

Plan 01 is complete only with a clean plan-owned working tree and committed
handoff.

## Validation and Acceptance

Accept Plan 01 when:

- fixture reproduces normal clipped UI drawing;
- diagnostics distinguish identity, variant, generic, and resample paths;
- the application-equivalent mask is preserved;
- the separate variant-cache scenario exists;
- local SDK + native macOS baseline completes;
- required GitHub workflow exists and has been run on the exact checkpoint SHA;
- Windows ARM was not added;
- every new file meets the size limit;
- baseline facts are committed into this plan.

A failing Windows x86-64 lane due to a proven pre-existing architecture/tooling
blocker may be recorded, but must not be mislabeled as passing. Plan 03 cannot
claim full final acceptance until the required lane passes or the maintainer
explicitly changes scope.

## Idempotence and Recovery

After interruption:

    git status --short
    git branch --show-current
    git log --oneline --decorate -10

Then read this plan from the first unchecked item. Inspect only the current
milestone diff and named working-set files.

Never use `git reset --hard`, `git clean -fd`, broad restore, or automatic stash
to recover. Never discard unrelated edits. Stage exact files, never
`git add .`.

Completed baseline runs do not need repetition unless code affecting the measured
path changes.

## Commit Discipline

Follow `.agents/skills/logical-commits/SKILL.md`.

Before every commit:

    git status --short
    git diff --check
    git add <exact files>
    git diff --cached --check
    git diff --cached

Use `<type>(<subsystem>): <imperative summary>`. Do not amend unrelated commits.

## Artifacts and Notes

Commit authored plan/source/test/workflow files. Record durable baseline facts in
this plan rather than creating extra evidence reports.

Do not commit normal build/runtime output.

## Interfaces and Dependencies

Do not add third-party runtime dependencies. Do not renumber optimization IDs.
Do not expose Skia types through Java APIs. Keep diagnostics internal/test-only
unless an existing public diagnostic surface already fits.
