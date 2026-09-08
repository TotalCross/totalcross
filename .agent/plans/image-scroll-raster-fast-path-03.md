<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# ExecPlan 03/03: Prove physical-variant reuse and final scrolling performance

This is the final sequential ExecPlan and follows `.agent/PLANS.md`. Start only
after Plans 01 and 02 are complete and committed on
`perf/image-scroll-raster-fast-path`.

Read Plan 02's `Outcomes & Handoff` once. Use this plan for all remaining state.

## Purpose / Big Picture

Ensure lazy smooth scaling on software-raster destinations stops doing repeated
work after first realization.

Plan 02 makes clipping compatible with physical fast paths. This plan verifies
that the existing `RASTER_PHYSICAL_VARIANT_CACHE` actually provides
"materialize/resample once, then reuse" for scaled sources. If it already does,
do not change production cache code. If counters prove repeated warm
resampling/rebuilding remains, repair the existing cache lookup/population path
without adding a second cache.

Then validate the real 120-image scroll workload and finish the branch.

No async decode/prefetch is added. Cold first-use decode is measured and, if
still significant, documented as follow-up.

## Progress

Use UTC timestamps.

- [ ] Read Plan 02 handoff and record start SHA.
- [ ] Prove current physical-variant behavior with clipping fixed.
- [ ] If necessary, repair existing cache reuse/invalidation.
- [ ] Add/adjust warm-reuse tests.
- [ ] Run final application-equivalent and variant-cache scroll passes.
- [ ] Run final local SDK + macOS native validation.
- [ ] Run final Windows x86-64, Linux x86-64, Linux ARM64 workflow.
- [ ] Verify file sizes, commit plan outcomes, leave clean branch.

## Surprises & Discoveries

Record only facts relevant to variant reuse or final validation.

Important known constraints:

- the existing physical-variant cache key is position-independent;
- do not add destination x/y, scroll offset, or frame number to it;
- the application-equivalent profile keeps
  `RASTER_PHYSICAL_VARIANT_CACHE` disabled;
- the separate variant-cache profile explicitly enables it;
- JPEG target-aware decode may make some draws physical-identity eligible
  without the variant cache, while other source/output ratios still require one
  realization into an exact physical variant.

## Decision Log

Fixed decisions:

1. Reuse the existing physical-variant cache only.
2. Do not add a Java-side duplicate cache.
3. Do not change `ImageOptimizationSettings` feature numbers.
4. Do not silently change `RASTER_PHYSICAL_VARIANT_CACHE` default state.
5. Cache identity remains position-independent.
6. Existing generation/source/pipeline/target invalidation semantics must be
   preserved.
7. Already cached repeated clipped draws must not smooth-resample the same
   generation/size/target again.
8. Scrolling the same image to a different y position must reuse the same
   position-independent variant when all actual key properties are unchanged.
9. No async decode or prefetch in this sequence.
10. Structural counters are pass/fail gates; timing is recorded but no universal
    millisecond threshold is imposed across CI hardware.
11. Local builds: SDK and macOS only, at milestone end/final validation.
12. Remote matrix: Windows x86-64, Linux x86-64, Linux ARM64. No Windows ARM.
13. New files <= 20 KiB/~600 lines; do not refactor existing large files for
    size.

## Outcomes & Retrospective

At completion record:

- final branch SHA;
- whether production cache code changed or Plan 02 alone unlocked reuse;
- cold/warm/warm counters/times for application-equivalent profile;
- cold/warm/warm counters/times for variant-cache profile;
- physical variant lookups/hits/stores and smooth resample counts;
- local SDK/macOS results;
- final GitHub Actions run URL/ID/result for every required lane;
- remaining cold-decode cost and explicit follow-up, if any;
- confirmation that no new cache/public API was added.

## Context and Orientation

Branch:

    perf/image-scroll-raster-fast-path

Use the fixture and diagnostics created by Plan 01 and the clip-aware raster path
implemented by Plan 02.

Application-equivalent profile:

    DECODE_ZERO_COPY
    RASTER_OPACITY_METADATA
    RASTER_OPAQUE_WRITE_PIXELS
    RASTER_ROW_READBACK
    RASTER_DIRECT_COLOR_MATERIALIZATION
    RASTER_TARGET_COLORTYPE_CONVERSION
    RASTER_PHYSICAL_IDENTITY_FOLDING

with:

    RASTER_PHYSICAL_VARIANT_CACHE = disabled

Variant-cache profile: the same flags plus:

    RASTER_PHYSICAL_VARIANT_CACHE = enabled

The real workload remains 120 square JPEGs, 3 columns, ~4 visible rows,
deterministic clipped vertical scrolling, cold/warm/warm passes.

## Plan of Work

### Milestone 1 — Verify existing physical-variant reuse before changing it

No local build while editing. Use the already built/validated Plan 02 behavior
or run the fixture only at the final milestone boundary if no valid current
binary exists.

Inspect the exact existing physical variant:

- key construction;
- lookup;
- store/population;
- invalidation/generation checks;
- clipped draw use.

Do not redesign it.

Use diagnostics to answer one question:

> After one eligible scaled draw has produced a physical variant, do repeated
> clipped draws of the same generation/physical size/target reuse it without
> another smooth resample?

Decision criterion:

- If yes, make no production cache change. Add/adjust only regression tests.
- If no, repair only the proven lookup/population/invalidation defect.

A valid cache repair must retain relevant source/pipeline/decode generation and
physical output/target properties. It must not include destination position.

If a production repair is needed, commit:

    fix(image): reuse physical variants for clipped lazy scaling

If no repair is needed, record that in `Surprises & Discoveries` and do not
create an empty/no-op commit.

### Milestone 2 — Lock warm reuse with deterministic assertions

No local build yet.

Create or adjust a focused test:

1. reset diagnostics;
2. draw one eligible scaled source with physical variant cache enabled;
3. record exactly one initial miss/store as appropriate;
4. repeat many clipped draws at the same physical size;
5. move destination y to simulate scrolling while retaining the same physical
   variant properties;
6. assert no new physical variant is created merely because position changed;
7. assert already cached repeated draws add zero smooth-resample events for that
   same variant;
8. assert reference pixels remain correct.

Structural acceptance for the deterministic microbenchmark after population:

- >=95% of eligible repeated draws hit physical identity or physical variant
  reuse; a deterministic tight loop should normally be effectively 100%;
- zero additional smooth-resample events for the already cached variant;
- cache-store count does not grow with frame count or scroll position.

Commit:

    test(image): assert warm physical variant reuse

If existing Plan 02 tests already provide all these assertions, do not duplicate
them; record the fact instead.

### Milestone 3 — Final real-scroll and platform validation

Only now may local build/smoke run.

Run the 120-image fixture with two configurations.

#### A. Application-equivalent profile

Keep physical variant cache disabled.

Capture:

- cold top-to-bottom;
- warm bottom-to-top;
- second warm top-to-bottom;
- identity attempts/hits/fallbacks;
- generic geometry/smooth resample counts;
- timing.

Acceptance:

- ordinary clipping no longer causes otherwise eligible physical-identity
  folding to fail;
- warm eligible identity draws do not route through generic smooth resampling
  only because a Control clip exists.

#### B. Variant-cache profile

Enable `RASTER_PHYSICAL_VARIANT_CACHE`.

Capture the same passes plus lookups/hits/stores.

Acceptance:

- first necessary physical realization may resample/store;
- repeated warm draws reuse it;
- second warm traversal's `smoothResampleDraws` is bounded by real new variant
  misses, not visible-image-draw-count times repaint-count;
- scrolling position does not create position-specific variants.

Cold may remain slower because first-use decode/materialization is synchronous.
If warm structural acceptance passes and cold remains expensive, document
prefetch/background decode as future work only.

### Local final validation

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

Run the native fixture following
`.agent/guides/macos-native-runtime-validation.md`; prove the deployed executable
uses the freshly built dylib.

Do not run local Windows, Linux, Android, or iOS builds.

### Remote final validation

Push the exact final candidate SHA and run:

    .github/workflows/image-scroll-raster-validation.yml

Required:

- Windows x86-64;
- Linux x86-64;
- Linux ARM64.

No Windows ARM.

All required lanes must pass for final acceptance. A previously recorded
Windows x86-64 tooling blocker remains a blocker unless the maintainer explicitly
changes scope; do not claim success by substituting Win32 or omitting the lane.

Record exact workflow run URL/ID and per-lane result.

## Validation and Acceptance

The sequence is complete only when:

1. lazy/logical image API semantics remain unchanged;
2. clipping correctness tests pass;
3. fully visible clipped eligible physical draws hit their fast path;
4. partial clipped physical rasters copy exact visible subrectangles;
5. unsupported transforms retain generic fallback;
6. the application-equivalent JPEG workload benefits from clip-aware identity
   folding without requiring variant cache to be enabled;
7. with variant cache enabled, repeated scaled warm draws do not resample the
   same physical variant every repaint;
8. no new physical cache exists;
9. SDK + native macOS final smoke pass;
10. Windows x86-64, Linux x86-64, Linux ARM64 workflow lanes pass on the exact
    accepted SHA;
11. every new file is within size limits;
12. all authored plan/source/test/workflow artifacts are committed;
13. normal build/log/binary outputs are not committed;
14. branch has no plan-owned uncommitted changes.

Timing must be reported for comparison, but not used as a single cross-platform
absolute threshold.

## Concrete Steps

Before final commits:

    git status --short
    git diff --check
    git log --oneline --decorate --no-merges \
      "$(git merge-base HEAD origin/perf/image-jpeg-factories-lazy)"..HEAD

List all newly added files:

    BASE="$(git merge-base HEAD origin/perf/image-jpeg-factories-lazy)"
    git diff --name-only --diff-filter=A "$BASE"..HEAD

For every new file:

    wc -c <file>
    wc -l <file>

Require <=20 KiB and approximately <=600 lines.

Inspect staged files exactly; never `git add .`.

Update this plan's `Outcomes & Retrospective` and commit:

    git add .agent/plans/image-scroll-raster-fast-path-03.md
    git diff --cached --check
    git diff --cached
    git commit -m "docs(plan): record final raster fast-path results"

## Idempotence and Recovery

After interruption:

    git status --short
    git branch --show-current
    git log --oneline --decorate -12

Read this plan from the first unchecked item. If one fact from Plan 02 is needed,
read only its `Outcomes & Handoff`.

Never reset/clean/stash away unrelated work.

A GitHub workflow result applies only to the SHA it tested. If code changes
afterward, rerun the affected final workflow.

Do not rerun historical baseline work unless a final regression requires a
specific comparison.

## Commit Discipline

Follow `.agents/skills/logical-commits/SKILL.md`.

Possible final implementation commits:

    fix(image): reuse physical variants for clipped lazy scaling   # only if needed
    test(image): assert warm physical variant reuse                # if needed
    docs(plan): record final raster fast-path results

Keep production fix, tests, and plan evidence separate logical commits.

## Artifacts and Notes

The three ExecPlans are the durable execution record. Prefer updating this plan
over creating another report.

Commit authored source/tests/workflow/plans. Do not commit generated build
directories, native binaries, SDK packages, ordinary logs, screenshots, or
downloaded workflow artifacts.

## Interfaces and Dependencies

No new runtime dependency. No new public image API. No Skia types exposed to
Java. No optimization ID renumbering. The generic Skia geometry path remains the
correctness fallback.
