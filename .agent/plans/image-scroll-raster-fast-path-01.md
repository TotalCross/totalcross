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
- [x] Add test-only physical-raster rejection reasons, repaint/frame accounting, and native update/present counters in checkpoint `151007d8b` (2026-09-09 UTC).
- [x] Run the six local `pre`/`post-disabled`/`post-enabled` × `clipped`/`unclipped` fixture cases; all passed with matching pixel hashes.
- [x] Run the exact Linux Docker build/runtime lanes from checkpoint `151007d8b`; Linux x86-64 and ARM64 passed, while Windows x86-64 retained its pre-fixture native crash.
- [x] Replace the artificial repaint/timer probe with a natural pen-event plus existing timer-update scenario, and correct scheduler-deadline and back-to-back frame diagnostics in checkpoint `1aa9ea2c3` (2026-09-09 UTC).
- [x] Run the corrected local natural clipped/unclipped cases and the exact Linux Docker lanes from checkpoint `1aa9ea2c3`; both Linux architectures passed all four frame-path/case combinations, while Windows retained its pre-fixture native crash.

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
- In the real clipped UI path, all 444 cold identity attempts were rejected by
  the canvas-state gate (`canvas->getSaveCount() != 1`), not by device-clip or
  partial-intersection geometry. The matching `doClip=false` control had 444
  identity hits and no generic/smooth draws. The test-only counters therefore
  distinguish canvas state, surface/destination, device clip, partial
  intersection, mapping geometry, backing incompatibility, and execution
  failure without changing the production decision.
- Adding new native diagnostic methods to the AOT-deployed
  `NativeImageBacking` caused a pre-fixture `NoSuchMethodError`/native crash.
  The final bridge preserves the existing getter ABI and multiplexes the seven
  rejection counters into unused upper 16-bit lanes; the SDK accessors mask the
  original low 16-bit counter. The existing physical-identity benchmark still
  passed its focused two-sample regression.
- The frame probe produced 79 repaint requests, 79 active-window traversals,
  and 79 effective paints: 75 event paints plus 4 timer-update paints, with no
  `repaintNow` calls. Java recorded 79 update-screen requests, but the macOS
  headless harness reached zero native update/present calls, so it does not
  provide evidence about presentation frequency. Clipped paint duration was
  median/p95/p99/max `21/32/41/41 ms` with 40 over 16.67 ms, 1 over 33.33 ms,
  and 15 back-to-back late starts; unclipped was `5/9/18/18 ms`, with 1 over
  16.67 ms, 0 over 33.33 ms, and no back-to-back late starts.
- The Windows x86-64 job still exits `-1073741819` (`0xC0000005`) before
  fixture output; cdb reports the access violation in `tcvm!strcasecmp` from
  `trace`/`startVM`. This is recorded as a Windows runtime blocker, not as a
  Linux build or raster result.
- The corrected natural fixture drives `_postEvent(PEN_DOWN/DRAG/UP)` and the
  existing `_onTimerTick(true)` path; it does not call `Control.repaint()`,
  `Window.repaintActiveWindows()`, or force `repaintNow`. A 36-frame run moved
  the real scrollbar from zero to about 136-137 logical pixels and produced
  one event-origin paint plus 35 timer-update-origin paints.
- Natural macOS results were 35 repaint requests, 36 active-window calls, and
  36 effective paints in both cases, with no `repaintNow` calls. Clipped paint
  duration p50/p95/p99/max was `27/30/45/45 ms`, with 36 paints over 16.67 ms
  and 1 over 33.33 ms; unclipped was `8/14/28/28 ms`, with 1 over 16.67 ms
  and none over 33.33 ms. The corrected deadline-based late count and
  end-to-next-start back-to-back count were both zero locally; start-to-start
  intervals were `66/68/69 ms` clipped and `48/50/54 ms` unclipped.
- The natural counters show no meaningful request coalescing in this fixture:
  35 `ScrollContainer` invalidations led to 36 effective paints (one extra
  scheduler paint), while 36 Java update-screen requests were recorded. The
  macOS and Xvfb Linux harnesses reported zero native update/present calls, so
  present-per-paint and present intervals remain unavailable rather than being
  inferred from Java requests.
- The exact Linux CI natural results vary with architecture but preserve the
  same shape. x86-64 clipped/unclipped had 36 paints, duration p50/p95/p99
  `33/36/50` and `10/16/28 ms`, respectively, with late/back-to-back
  `0/0` and `2/0`; ARM64 had `18/21/31` and `6/10/21 ms`, with `3/0` and
  `0/0`. All four Linux natural and manual cases had `overallPass=true` and
  pixel hash `00009D4A00006964`. The manual path's forced paint counts are
  retained only for raster-cost control and are not scheduler evidence.

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

Final Plan 01 handoff (2026-09-09 UTC):

- Base SHA: `9ca331017d32ca553155d7fda59b22378fd8198a`; implementation
  checkpoint: `151007d8be1645d052aa1bb8c4a6d1a60d395d03`.
- Fixture invocation after `jarImageScrollRasterFastPathBenchmark`:
  `ImageScrollRasterFastPathBenchmarkApp --scenario=post-enabled --case=clipped`
  and the same command with `--case=unclipped`. The local matrix also ran
  both cases for `pre` and `post-disabled`. Every case reported
  `overallPass=true` and pixel hash `00009D4A00006964` at validation scale 2.
- Rejection mapping is exposed as
  `physical_identity_rejections_{canvas_state,surface_destination,device_clip,partial_intersection,mapping_geometry,backing_incompatible,execution_failure}`.
  The existing attempts/hits/fallbacks counters remain semantically unchanged.
  The repaint report is `repaint_frame=...`; native counters are
  `native_update_screen_calls` and `native_present_calls`.
- Representative `post-enabled` macOS results were:
  clipped: cold `1220 ms/37 frames`, `444/0/444` identity attempts/hits/fallbacks,
  `444` canvas-state rejections and `444` generic plus `444` smooth draws;
  warm reverse `30 ms/1 frame` with `12/0/12`; warm forward
  `353 ms/37 frames` with `30/0/30`. Unclipped: cold `377 ms/37 frames`,
  `444/444/0` and zero generic/smooth draws; warm reverse `6 ms/1 frame`
  with `12/12/0`; warm forward `271 ms/37 frames` with `444/444/0`.
  The separate variant-cache probe reported 14 lookups, 12 hits, and 1 store.
- The proven clipped rejection gate is `buildRasterPhysicalPlan`, called from
  `geometryDraw`; its first real-path failure is the modified-canvas/save-count
  check. The same helper contains the device-clip, destination-intersection,
  mapping, and backing bounds classifications. `drawPhysicalVariant` uses the
  helper for its eligibility check as well.
- Local validation passed for SDK tests, SDK distribution, fixture packaging,
  macOS native `tcvm`/`Launcher` build, deploy, the six fixture cases, and the
  focused two-sample existing physical-identity benchmark. The default
  60-sample physical benchmark was not completed because its runtime was about
  one second per sample; no full-60 claim is made.
- The Linux workflow is based directly on `.github/workflows/build.yml`: the
  x86-64 and ARM64 jobs use its versioned Docker images, dependency environment
  forwarding, source/build/cache mounts, and in-container `cmake ... -G Ninja`
  followed by `ninja`. Run
  `https://github.com/TotalCross/totalcross/actions/runs/34317998230` at the
  exact implementation SHA passed Linux x86-64 (job
  `102358200282`) and Linux ARM64 (job `102358200103`), including build,
  architecture check, deploy, and clipped/unclipped runtime markers. Windows
  x86-64 built successfully but failed its fresh runtime (job `102358200362`)
  with the pre-fixture crash above.
- Plan 02 has not started. Its next decision is whether the measured
  canvas-state eligibility constraint can be relaxed while retaining pixel
  correctness; scheduler/30-FPS policy and production fast-path behavior were
  not changed here.

Corrected natural repaint/frame handoff (2026-09-09 UTC):

- Current implementation checkpoint: `1aa9ea2c340eb2342da20838ae956f72f9712312`.
  The code changes are diagnostic/fixture-only; no production raster fast-path
  or scheduler policy was changed.
- Natural invocation:
  `ImageScrollRasterFastPathBenchmarkApp --scenario=post-enabled --case=clipped --frame-path=natural`
  and the same command with `--case=unclipped`. The fixture sends real pen
  down/drag/up events through `Window._postEvent`, lets the normal
  `ScrollContainer` drag path mark `Window.needsPaint`, and calls the existing
  `MainWindow._onTimerTick(true)` so the scheduler path consumes that state.
  It does not directly call `Control.repaint()`, `Window.repaintActiveWindows()`,
  or `repaintNow`; the manual path remains available as a pure raster-cost
  control.
- Natural diagnostic mapping: `repaint_requests` counts actual scroll dirty
  invalidations; `repaint_active_windows_calls` counts scheduler traversals;
  `effective_paints` counts `_doPaint` entries; `paint_from_event` and
  `paint_from_timer_update` identify origins; `scheduler_due_*` records the
  event/timer due timestamp; `late_paint_starts` compares paint start with
  that due timestamp; `back_to_back_paints` compares each paint start with the
  previous paint end; `update_screen_*` records Java update-screen requests;
  native update/present counters are reported when the runtime reaches them.
- Final local macOS natural results: clipped `35/36/36` requests/active
  calls/effective paints, event/timer origins `1/35`, duration p50/p95/p99/max
  `27/30/45/45 ms`, over-16.67/over-33.33 `36/1`, intervals
  `66/68/69 ms`, late/back-to-back `0/0`; unclipped has the same
  `35/36/36` and `1/35`, duration `8/14/28/28 ms`, over-16.67/over-33.33
  `1/0`, intervals `48/50/54 ms`, late/back-to-back `0/0`. Both moved the
  scrollbar and passed with pixel hash `00009D4A00006964`; native update and
  present calls were zero in the headless macOS harness.
- Exact Linux Docker validation run:
  `https://github.com/TotalCross/totalcross/actions/runs/34320669169`.
  Linux x86-64 job `102366347138` and ARM64 job `102366346713` passed the
  direct `.github/workflows/build.yml`-based container build, deploy,
  architecture checks, and all manual/natural clipped/unclipped runtime
  markers. Linux Xvfb also reported zero native update/present calls. The
  Windows x86-64 job `102366347059` built but retained the known
  `0xC0000005` crash before fixture output.
- The measurements do not justify a scheduler change: the natural scenario
  has no observed coalescing, no local back-to-back burst, and platform/headless
  native presentation is unavailable. Plan 02 may use the raster rejection
  data and this frame evidence, but must obtain non-headless presentation
  evidence before making a frame-pacing claim. Plan 02 has not started.

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
