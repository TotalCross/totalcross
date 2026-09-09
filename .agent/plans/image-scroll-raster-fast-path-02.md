<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# ExecPlan 02/03: Make physical image raster fast paths clip-aware

This is the second of three sequential ExecPlans and follows `.agent/PLANS.md`.
Start only after Plan 01 is complete and committed on
`perf/image-scroll-raster-fast-path`.

Read Plan 01's `Outcomes & Handoff` once, then use this plan as the execution
record. Keep `Progress`, `Surprises & Discoveries`, `Decision Log`, and
`Outcomes & Handoff` current.

## Purpose / Big Picture

Fix the proven software-raster eligibility defect: ordinary UI clipping must not
force an otherwise exact physical image draw through generic smooth geometry.

Preserve clipping semantics. The solution is not to disable clipping; it is to
make clipping explicit in physical fast-path planning and attempt those paths
before `skia_setClip(...)` mutates the canvas.

After this plan:

- fully visible eligible clipped draws use physical identity/variant fast paths;
- partially visible eligible axis-aligned physical rasters copy the exact
  visible source/destination subrect;
- no-intersection draws become a correct no-op;
- unsupported geometry still uses the current generic clipped path;
- pixel output remains correct;
- the change is validated locally only at milestone end on SDK/macOS and
  remotely on Windows x86-64, Linux x86-64, Linux ARM64.

No Windows ARM.

## Progress

Use UTC timestamps.

- [x] 2026-09-09T07:46Z Read Plan 01 handoff; baseline checkpoint was
      `151007d8be1645d052aa1bb8c4a6d1a60d395d03` and execution started from
      `bfc777769f9865495bf49cb4f28cf510ebdece8a`.
- [x] 2026-09-09T04:39Z Made clip an explicit input to physical raster
      eligibility and evaluated physical paths before canvas clip mutation.
- [x] 2026-09-09T04:39Z Implemented exact partially clipped physical copies.
- [x] 2026-09-09T07:37Z Added focused pixel/path tests for partial edges,
      corners, no intersection, scale 1/2, transformed fallback, and cached
      physical variants.
- [x] 2026-09-09T07:37Z Ran milestone-end SDK + macOS native validation.
- [x] 2026-09-09T07:44Z Ran the required remote matrix on checkpoint
      `d9a7ddef7ddd6cffaec92355747a084c0400b054`.
- [x] 2026-09-09T07:46Z Recorded results and handoff to Plan 03.

## Surprises & Discoveries

Copy only Plan 01 facts that directly change implementation. Do not paste its
full history.

Expected relevant facts:

- normal `Graphics.drawImage(...)` uses clipping;
- `skia_setClip(...)` performs save + clip;
- current physical eligibility rejects altered save count/full-device-clip
  mismatch;
- synthetic `doClip=false` benchmarks avoid this state.

Implementation discoveries:

- `skiaDrawGeometryPlan` must preflight the physical path with the logical
  Graphics clip before `skia_setClip(...)`; a clipped decline then uses a
  generic-only wrapper so the existing canvas clip flow is preserved.
- Physical identity and physical variant planning need separate visible-clip
  logic. A cached variant is already physicalized, so requiring the identity
  planner to accept it would incorrectly reject clipped variant reuse.
- The focused deterministic partial-clip source validates pixels at scale 1
  and 2; the application-equivalent clipped JPEG scroll pass is the identity
  counter assertion.

Append implementation-specific discoveries with exact function/path evidence.

## Decision Log

Architecture is fixed:

1. Keep lazy `Image` behavior and logical scaling unchanged.
2. Keep the existing physical identity and physical variant mechanisms.
3. Do not add a second cache.
4. Resolve the Graphics clip before calling `skia_setClip(...)`.
5. Convert/intersect it in the same physical coordinate system and using the
   same rounding helpers as the existing raster physical plan.
6. Intersect the explicit Graphics clip with any pre-existing device clip.
7. Attempt eligible physical fast paths before per-draw clip mutation.
8. Fully contained eligible draw: execute existing physical path without
   pushing that Graphics clip.
9. Partially visible **already physicalized** axis-aligned draw: copy a 1:1
   physical subrectangle.
10. No intersection: successful no-op.
11. Rotation, skew, perspective/non-axis-aligned mapping, uncertain rounding,
    unsupported alpha/blend/color behavior, or unrelated canvas state: fall
    back to the existing generic clipped geometry path.
12. Do not relax alpha, opacity, color-type, ownership, or `writePixels`
    safety checks merely to increase hit rate.
13. Do not change optimization default states in this plan.
14. No async decode or prefetch.
15. New files <= 20 KiB/~600 lines. Do not refactor existing large files just
    for size.
16. No local builds while editing. SDK/macOS builds and native smoke only at the
    end of the milestone.

## Outcomes & Handoff

Before Plan 03, record:

- Start SHA: `bfc777769f9865495bf49cb4f28cf510ebdece8a`.
- Implementation checkpoint: `d9a7ddef7ddd6cffaec92355747a084c0400b054`.
  The final plan-update commit is the end SHA recorded by Git history.
- Native functions changed: `skiaDrawGeometryPlan` in
  `TotalCrossVM/src/nm/ui/GraphicsPrimitivesSkia_c.h`, the internal clip
  declarations in `TotalCrossVM/src/nm/ui/skia/skia.h`, and
  `buildRasterPhysicalPlan`, `buildPhysicalVisibleClip`,
  `drawTargetColorVariant`, `drawPhysicalVariant`, `drawPhysicalFastPath`,
  and `geometryDraw` in `TotalCrossVM/src/nm/ui/skia/skia_image_geometry.cpp`.
- Full clipped identity draws now hit: the clipped macOS cold pass recorded
  444 attempts / 444 hits / 0 fallbacks, matching the 444-draw workload.
- Partial clips use physical subrects: left/top/right/bottom/corner and empty
  intersection cases matched the reference hash at surface scales 1 and 2;
  the no-intersection target remained unchanged. The focused hash was
  `0000DD4000009725`.
- Unsupported transformed geometry matched its reference and recorded
  `generic=1`, hash `0000DD600000CC65`.
- Warm fixture delta versus Plan 01: reverse pass was 12/12 identity hits;
  forward pass was 444 attempts with 30 identity hits and 414 mapping-geometry
  declines, with 0 generic or smooth-resample draws. The variant scenario
  recorded 14 lookups, 12 hits, 1 store, 1 generic materialization, and 1
  smooth materialization.
- Local macOS passed: SDK `test`, SDK `dist -x test`, CMake configure, native
  `tcvm Launcher` build, clipped fixture, and unclipped control. The clipped
  and unclipped pixel hash was `00009D4A00006964`; each reported
  `overallPass=true`.
- Remote run `34325072695`:
  https://github.com/TotalCross/totalcross/actions/runs/34325072695
  passed Linux x86-64 (job `102380349593`) and Linux ARM64 (job
  `102380349761`). Windows x86-64 (job `102380349926`) reached the runtime
  step but hit the pre-existing launcher/VM access violation `0xC0000005`
  (`-1073741819`) in `tcvm!trace` before fixture output, matching the Plan 01
  Windows failure; no raster assertion ran. Windows ARM was not run.
- No new repeated smooth resampling or physical-variant miss was proven by
  the counters; the first variant materialization was followed by clipped
  cache hits.

Plan 03 reads only this handoff plus its own plan.

## Context and Orientation

Branch:

    perf/image-scroll-raster-fast-path

Use Plan 01's exact located paths. The important native flow is conceptually:

    Graphics.drawImage(...)
      -> native geometry plan
      -> clip setup
      -> image backing geometry draw
      -> physical identity / physical variant / generic geometry

The defect to remove is ordering/eligibility, not UI layout.

The existing physical raster plan already computes exact physical geometry.
Extend/reuse it; do not create a parallel coordinate conversion model.

## Plan of Work

### Milestone 1 — Evaluate physical fast paths before per-draw clip mutation

No local build.

In the native geometry entry point containing `skiaDrawGeometryPlan` or its
current equivalent:

1. capture the logical Graphics clip before applying it to the Skia canvas;
2. use existing surface/content-scale conversion helpers to obtain physical clip
   bounds;
3. obtain any pre-existing canvas/device clip and intersect it;
4. pass the effective physical clip to the raster physical planner;
5. evaluate physical identity and physical variant eligibility;
6. if a fast path handles the draw, return without calling
   `skia_setClip(...)`;
7. if it declines, preserve the exact existing generic flow, including
   `skia_setClip(...)`.

Do not merely remove `getSaveCount()==1` or full-clip checks. Reframe them:

- the per-draw Graphics clip is no longer represented by a canvas mutation when
  testing the physical path;
- unrelated pre-existing canvas state must still make the path decline when
  correctness cannot be proven.

Commit this ordering/eligibility unit:

    fix(skia-image): evaluate raster fast paths before canvas clip

### Milestone 2 — Add exact visible physical subrect planning

No local build.

Extend the existing raster physical plan with explicit full and visible
rectangles. Keep names consistent with current code.

Required conceptual fields:

    fullDestinationPixels
    visibleDestinationPixels
    visibleSourcePixels

Do not necessarily introduce these exact field names if existing fields already
represent them.

For a physical variant whose dimensions equal the full physical destination
`D`, and visible destination `V = intersect(D, effectiveClip)`, derive:

    source.left   = V.left - D.left
    source.top    = V.top  - D.top
    source.right  = source.left + V.width
    source.bottom = source.top  + V.height

The same rule applies to a true physical-identity source.

This is a crop of an already physicalized raster, not a new scale. The actual
copy must remain 1:1 in physical pixels.

Cases to implement:

- destination fully inside clip;
- left/top/right/bottom partial clips;
- corner/two-axis clips;
- empty intersection;
- destination partially outside the surface if current semantics allow it.

If the source is not already physicalized to the destination dimensions, do not
pretend it is. Let the existing first materialization/resample or generic path
handle it; Plan 03 addresses repeated variant reuse.

Commit:

    fix(skia-image): copy clipped physical raster subrects

### Milestone 3 — Correctness and path regression tests

No local build until all test code is ready.

Add focused tests in the existing native/image harness. Prefer deterministic
pixel assertions/hashes over large golden assets.

Required tests:

1. fully visible clipped physical identity:
   output matches reference and identity-hit counter increments;
2. partial left clip;
3. partial top clip;
4. partial right clip;
5. partial bottom clip;
6. corner clip;
7. no intersection:
   destination unchanged, no generic draw needed;
8. cached physical variant:
   first creation followed by clipped reuse;
9. rotated/skewed/non-axis-aligned case:
   generic fallback counter increments and output matches current reference;
10. surface scale 1x and 2x; add 3x only if deterministic in the existing
    harness;
11. existing alpha/color-type restrictions remain enforced.

Do not add a large screenshot corpus.

Commit:

    test(skia-image): cover clipped physical raster paths

### Milestone 4 — Milestone-end validation

Only now may local build/smoke run.

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

Run the Plan 01 fixture as a native macOS deployment using the freshly built
`libtcvm.dylib` and
`.agent/guides/macos-native-runtime-validation.md`.

Run:

- application-equivalent clipped JPEG workload;
- unclipped diagnostic control;
- variant-cache scenario;
- focused pixel/path tests.

Push the exact checkpoint SHA. Run
`.github/workflows/image-scroll-raster-validation.yml` for:

- Windows x86-64;
- Linux x86-64;
- Linux ARM64.

Do not run Windows ARM.

Record all results in this plan.

Commit the plan update:

    docs(plan): record clip-aware raster validation

## Validation and Acceptance

Plan 02 passes only if:

- fully visible eligible clipped draws hit the same physical path they can hit
  with `doClip=false`;
- partial eligible draws use a physical source/destination subrect and preserve
  reference pixels;
- no-intersection is correct;
- unsupported transforms still fall back;
- no existing alpha/color safety restriction was weakened incorrectly;
- primary clipped warm counters show the clip no longer causes automatic
  physical-identity rejection;
- SDK and native macOS validation pass;
- all non-blocked required remote lanes pass on the exact checkpoint SHA;
- no new cache or public API was introduced.

Do not use one absolute timing number as a cross-platform gate.

## Concrete Steps

During editing, use only static/focused checks:

    git status --short
    git diff --check
    git diff --stat
    rg '<exact symbol>' <focused paths>
    git diff -- <focused paths>

Do not run local compilation as an edit loop.

Before each commit:

    git status --short
    git diff --check
    git add <exact files>
    git diff --cached --check
    git diff --cached

At milestone end, verify new files only:

    BASE="$(git merge-base HEAD origin/perf/image-jpeg-factories-lazy)"
    git diff --name-only --diff-filter=A "$BASE"..HEAD

Check each with `wc -c` and `wc -l`.

## Idempotence and Recovery

After interruption:

    git status --short
    git branch --show-current
    git log --oneline --decorate -10

Read this plan from its first unchecked item. Do not reconstruct Plan 01; use
only its `Outcomes & Handoff` if a baseline fact is needed.

Never reset/clean/stash away unrelated work. Stage exact files only.

If a code change after validation touches the raster path, invalidate only the
affected validation and rerun it at the next allowed milestone boundary.

## Commit Discipline

Follow `.agents/skills/logical-commits/SKILL.md`.

Expected logical commits:

    fix(skia-image): evaluate raster fast paths before canvas clip
    fix(skia-image): copy clipped physical raster subrects
    test(skia-image): cover clipped physical raster paths
    docs(plan): record clip-aware raster validation

If implementation proves two production changes are inseparable in the current
function, one combined `fix(skia-image)` commit is acceptable only if the staged
diff is still one reviewable behavior change. Do not mix tests/plan updates into
that commit.

## Artifacts and Notes

Commit production source, focused tests, and this plan update.

Do not commit CMake/Gradle outputs, dylibs/executables, runtime logs, screenshots,
or downloaded Actions artifacts.

## Interfaces and Dependencies

No new third-party runtime dependencies. No new public Java image API. No Skia
types exposed across Java interfaces. Keep the generic geometry renderer as the
correctness fallback.
