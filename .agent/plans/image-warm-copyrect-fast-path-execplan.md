<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Make warm image draws cheap and make copyRect draw-plan aware

This ExecPlan follows `AGENTS.md`, `.agent/PLANS.md`, and
`.agents/skills/logical-commits/SKILL.md` in the TotalCross repository.

## Purpose / Big Picture

Execute this plan on `perf/image-scroll-raster-fast-path`.

The first operation is to remove commit
`640e327cd584342e3137260272e733f5b47a39f7` from that branch and force-push the
rewritten branch back to its parent
`81bb027e650712af29d7df8ecde6a6caadaf763e`.

After that cleanup, improve the raster image path so an already prepared
deferred image is as close as practical to the cost of the old materialized
`copyRect` path. A physical 1:1 draw already proven by
`RASTER_PHYSICAL_IDENTITY_FOLDING` must not pay for `snapshot()`, shader
creation, matrix/clip reconstruction, or `geometryDrawCompiled()` when direct
pixel transfer is safe.

Also make `Graphics.copyRect(Image, ...)` consume an `ImageDrawPlan` before
forcing materialization, just as `drawImage` does. Existing applications that
continue to use `copyRect` must gain the lazy/deferred raster optimizations
without changing application code.

Change the effective default policy of `ImageOptimizationSettings`. `DEFAULT`
remains a distinct state, but resolves to enabled for exactly:

- `DECODE_ZERO_COPY`
- `RASTER_OPACITY_METADATA`
- `RASTER_OPAQUE_WRITE_PIXELS`
- `RASTER_ROW_READBACK`
- `RASTER_DIRECT_COLOR_MATERIALIZATION`
- `RASTER_PHYSICAL_IDENTITY_FOLDING`

All other features remain disabled when their state is `DEFAULT`. Explicit
`ENABLED` and `DISABLED` always override that policy.

The customer-style 663-JPEG workload must use the plan-aware path through
`copyRect`; warm passes must avoid unnecessary geometry/shader work on physical
identity draws; and features 13/14 must remain opt-in and benchmarked in all
four on/off combinations.

## Working Set and Resume Protocol

Execution plan path:

- `.agent/plans/image-warm-copyrect-fast-path-execplan.md`

Supporting files:

- `.agent/state/image-warm-copyrect-fast-path.md`
- `.agent/evidence/image-warm-copyrect-fast-path.md`
- `.agent/reports/image-warm-copyrect-fast-path-editorial.md`

Committed benchmark evidence:

- `.agent/evidence/image-warm-copyrect-fast-path/baseline/`
- `.agent/evidence/image-warm-copyrect-fast-path/final/`

On resume, read the state file first, then only active milestone paths and
relevant evidence entries. Do not reread full plans/logs/history.

Inspect only as needed:

- `TotalCrossSDK/src/main/java/totalcross/ui/image/ImageOptimizationSettings.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/image/Image.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/image/ImagePipeline.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/image/ImageDrawingBridge.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/gfx/Graphics.java`
- `TotalCrossVM/src/nm/ui/skia/skia_image_geometry.cpp`
- `TotalCrossVM/src/nm/ui/skia/skia_image_backing.cpp`
- corresponding Skia internal headers and focused tests
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageScrollRealWorkloadBenchmarkApp.java`
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageRasterBenchmarkSupport.java`
- `scripts/run-image-scroll-real-workload-benchmark.py`

Use `rg`, headings, and narrow ranges. The real corpus comes from
`TC_IMAGE_CORPUS` and must contain exactly 663 JPEGs.

New files created by this plan must remain below 20 KiB and approximately 600
lines. Split oversized benchmark evidence losslessly into numbered parts. Do not
refactor existing files merely to reduce size.

Commit all intentional plan artifacts and benchmark evidence. Ordinary
Gradle/CMake/Ninja logs and generated build outputs remain uncommitted.

Local builds are restricted to SDK and macOS, and only at the end of related
milestones. Native smoke tests may run only at related milestone ends and final
validation. Do not run Android, Linux, Windows, or iOS local builds.

## Progress

- [ ] Rewrite the branch and remove `640e327cd`.
- [ ] Add the 13/14 benchmark matrix and commit baseline evidence.
- [ ] Implement central effective defaults.
- [ ] Make `copyRect(Image, ...)` draw-plan aware.
- [ ] Add the direct physical-identity warm-copy path.
- [ ] Run final benchmarks/smokes, commit evidence/report, and push.

Update progress only at logical commits or milestone boundaries.

## Current Architecture and Scope

`Graphics.drawImage` first obtains a cached native draw plan and attempts native
geometry execution. It materializes only if that path cannot handle the draw.

`Graphics.copyRect(GfxSurface, ...)` currently resolves/materializes an `Image`
before native copy, preventing deferred pipelines from reaching the same raster
optimizations.

`ImagePipeline` already caches draw plans by destination scale and source decode
generation. Do not add another Java draw-plan cache.

On software raster, physical identity folding already proves supported 1:1
physical mappings, including clipping. A hit can still execute
`snapshot() + geometryDrawCompiled()` when the plan is not considered trivial
for `writePixels`. Thus a deferred `SMOOTH_SCALE` may avoid resampling but still
pay shader/geometry overhead on every warm draw.

The physical proof, not the original operation name, determines whether direct
copying is safe.

Features 13/14 are:

- 13: `RASTER_TARGET_COLORTYPE_CONVERSION`
- 14: `RASTER_PHYSICAL_VARIANT_CACHE`

They stay disabled by default. Benchmark 00, 10, 01, and 11 for these two bits
while holding all other benchmark features constant.

Out of scope: cache eviction, memory-pressure policy, prefetch, GPU residency,
mmap allocation, and rendering scheduler changes.

## Plan of Work

### Milestone 0 — Rewrite branch and establish baseline

Require a clean worktree. Do not stash/discard unrelated changes. If dirty,
stop with a blocker.

Run:

```bash
git switch perf/image-scroll-raster-fast-path
test "$(git rev-parse HEAD)" = "640e327cd584342e3137260272e733f5b47a39f7"
test "$(git rev-parse HEAD^)" = "81bb027e650712af29d7df8ecde6a6caadaf763e"
test -z "$(git status --porcelain)"

remote_head="$(git ls-remote --heads origin   refs/heads/perf/image-scroll-raster-fast-path | awk '{print $1}')"
test "$remote_head" = "640e327cd584342e3137260272e733f5b47a39f7"

git reset --hard 81bb027e650712af29d7df8ecde6a6caadaf763e
git push   --force-with-lease=refs/heads/perf/image-scroll-raster-fast-path:640e327cd584342e3137260272e733f5b47a39f7   origin perf/image-scroll-raster-fast-path

test "$(git rev-parse HEAD)" = "81bb027e650712af29d7df8ecde6a6caadaf763e"
```

Before production changes, extend the real-workload benchmark:

1. Add independent command-line controls for feature 13 and feature 14.
2. Explicitly disable all optimization features, then enable the common set:
   0, 1, 2, 3, 4, and 15.
3. Apply 13/14 from the requested matrix. Never rely on `DEFAULT`.
4. Run all four 13/14 combinations at 480x720 and 540x960 in fresh processes.
5. Preserve cold, warm, warm2.
6. Record decode/materialization counts, draw-plan cache activity when
   available, physical identity counters, writePixels counters/bytes,
   target-color counters/bytes, physical-variant counters/bytes, generic
   geometry, smooth resample, live/peak backing bytes, frame p50/p95/p99/max,
   and frames >=17 ms / >=34 ms.
7. Add a small warm microbenchmark comparing explicitly materialized copy,
   deferred `drawImage`, and deferred `copyRect`, including partial clipping.
   Report ratios; a single noisy timing sample is not a correctness gate.

Commit the measurement harness before running baseline:

- `test(image): add raster option benchmark matrix`

At this milestone end only, build macOS native if needed:

```bash
cmake -S TotalCrossVM -B build-image-scroll-raster   -G Ninja -DCMAKE_BUILD_TYPE=Release
cmake --build build-image-scroll-raster --target tcvm
```

Run the benchmark with explicit baseline evidence output. The existing runner
may perform SDK Gradle build/deploy at this milestone boundary.

Commit normalized results, compact summary, and compact per-run logs, splitting
files that exceed the new-file limit:

- `test(image): record warm path baseline`

Acceptance: exact 663-image corpus, both resolutions, four 13/14 combinations,
and three passes per combination.

### Milestone 1 — Make DEFAULT resolve centrally

Implement one central default-policy function in
`ImageOptimizationSettings`. Do not initialize state entries to `ENABLED`.

Required semantics:

```text
ENABLED  -> true
DISABLED -> false
DEFAULT  -> central feature default
```

The central policy returns true only for 0, 1, 2, 3, 4, and 15.

Update `effectiveMask()` so native draw/decode masks include effective
default-enabled features. Update `resetForTest()` so resetting to `DEFAULT`
reinstalls the effective default mask rather than zero.

Remove caller-owned default booleans from production resolution so callers
cannot disagree about the same feature. This is internal; update focused callers.

Tests must prove:

- six requested defaults enabled after reset;
- 13/14 and all other features disabled after reset;
- explicit `DISABLED` overrides an enabled default;
- `setState(feature, DEFAULT)` restores the product default;
- explicit `ENABLED` enables a default-disabled feature;
- native effective mask equals Java effective state.

Do not change benchmark profiles to use defaults.

At milestone end run focused SDK tests/build, copyright validation, and
`git diff --check`.

Commit:

- `fix(image): resolve optimization defaults centrally`

### Milestone 2 — Route copyRect through ImageDrawPlan

Change `Graphics.copyRect(GfxSurface, x, y, width, height, dstX, dstY)` for an
`Image` source to:

1. obtain the draw plan using destination `Graphics` content scale;
2. attempt native plan-aware copy with explicit source rectangle,
   `dstX/dstY`, and normal `copyRect` clipping;
3. return if handled;
4. otherwise resolve/materialize and use the existing fallback.

Do not translate the canvas to reuse an API whose destination is implicitly
`(0,0)`. Extend/add the native bridge so source rectangle and destination are
explicit.

Unify `copyImageRect` with the same internal plan-aware helper where practical,
using destination `(0,0)`.

Preserve arbitrary source subrectangles/destinations, clipping, handled
no-intersection without mutation, current-frame semantics, JavaSE fallback, and
generic unsupported-plan fallback.

Add focused Java/native tests for full image, subrect, partial clip,
no-intersection, non-zero destination, frame cases, unsupported fallback, and a
deferred smooth-scaled image proving `copyRect` reaches the plan path without
prior materialization.

At milestone end run focused SDK tests; if native bridge changed, run permitted
macOS native build and focused native smoke.

Commit:

- `perf(image): route copyRect through draw plans`

### Milestone 3 — Direct-copy proven physical identity draws

Keep `buildRasterPhysicalPlan` as the authority for proving physical 1:1
mapping and visible clipped source/destination rectangles.

Add/generalize a backing-level direct-copy helper accepting physical source and
destination subrectangles. It must:

- require equal integer extents;
- use the already-computed visible clip;
- require effective alpha 255;
- reject color/filter/fill operations not admitted by pure physical geometry;
- copy directly from backing pixels to software destination when compatible;
- use existing compact-row conversion only when its enabled features make it
  safe;
- avoid creating a physical variant solely for an identity copy;
- never call `geometryDrawCompiled()` after successful direct copy;
- treat an empty physical plan as handled no-op without mutation.

A deferred `SMOOTH_SCALE` is eligible when physical proof shows that targeted
decode already maps 1:1. Do not reject solely because the logical pipeline
contains `SMOOTH_SCALE`.

Order software handling as:

```text
physical identity proof
  -> direct physical copy when safe
  -> target-color / physical-variant paths
  -> generic geometry fallback
```

On uncertainty preserve fallback.

Add a diagnostic counter for identity hits completed by direct copy if existing
writePixels accounting cannot distinguish them.

Tests must prove full/clipped smooth-scale identity direct copies, zero smooth
resample, no generic geometry after direct hit, matching hashes,
no-intersection non-mutation, fallback correctness, feature 15 `DISABLED`
blocking the optimization, and feature 15 `DEFAULT` enabling it.

At milestone end run permitted macOS build and related native smoke.

Commit:

- `perf(skia-image): direct-copy physical identity draws`

### Milestone 4 — Final benchmark and closeout

Run the same real-workload and warm microbenchmark regime as baseline. If a
measurement bug requires a harness change, rerun both baseline and final under
the corrected regime and mark older evidence invalid.

Run both resolutions and all four 13/14 combinations.

Behavior acceptance:

- warm/warm2 perform zero JPEG decodes;
- supported identity cases perform zero smooth resamples;
- direct-copy hits do not use generic geometry;
- `copyRect` reaches draw-plan/identity counters instead of forcing
  materialization;
- feature 13/14 state does not disable feature-15 direct identity;
- hashes/clipping remain correct.

Performance acceptance:

- warm p95/p99 improve over baseline;
- deferred `copyRect` and `drawImage` converge toward materialized-copy cost;
- if median warm direct lane remains >20% slower than materialized reference
  after three samples, record the gap and the measured remaining stage instead
  of masking it.

Do not optimize scheduler cadence here.

Commit final benchmark evidence and editorial report, mark state complete,
recheck remote head, then push normally with no further force push:

- `test(image): record warm path final results`
- `docs(plan): close warm image optimization`

## Surprises & Discoveries

Record only discoveries that change remaining implementation, such as a format
restriction, unexpected warm materialization, or benchmark flaw. Do not use this
as a command log.

## Decision Log

- Remove `640e327cd...` before implementation; compatibility belongs in
  `copyRect`, not `ImageControl`.
- Keep `DEFAULT` distinct and resolve defaults centrally.
- Keep features 13 and 14 disabled by default.
- Decide direct warm copy from proven physical mapping, not operation name.
- Preserve all current generic/materialization fallbacks.

## Validation and Acceptance

Before every commit follow `.agents/skills/logical-commits/SKILL.md`: read state,
inspect scoped changes, validate copyright headers, stage only intended paths,
run `git diff --check --cached`, use English scoped Conventional Commits with a
body, validate the commit message, and update state.

Build policy:

- SDK local build only at related milestone end.
- macOS native local build only at related milestone end.
- native smoke only at related milestone end and final closeout.
- no other local platform builds.

Before/after benchmark evidence is mandatory and committed.

## Risks and Open Questions

No architectural choices are intentionally left to the executor.

Do not enable 13/14 by default, add a new long-lived raster cache, weaken
correctness for benchmark hits, or broaden into eviction/prefetch.

If a source format cannot safely use direct copy, retain fallback and record the
limitation.

## Idempotence and Recovery

The initial force push is the only history rewrite. It is allowed only when
local HEAD, parent, remote HEAD, and clean-worktree checks match exactly. If any
check fails, stop rather than forcing over concurrent work.

After rewrite, all commits/pushes are fast-forward.

Benchmark reruns use new evidence paths; never silently overwrite committed
measurements. Preserve unrelated local files/caches.

## Outcomes & Retrospective

At completion summarize rewritten/final heads, default policy, `copyRect`
plan-aware behavior, direct identity behavior, baseline/final warm metrics for
all 13/14 combinations, observed memory deltas, and any remaining measured warm
cost. Point to evidence instead of copying raw output.

## Revision Note

Initial plan. Architecture and validation policy are fixed for Luna execution.
