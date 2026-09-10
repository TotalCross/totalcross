<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Prefetch ScrollContainer images before first draw

This ExecPlan follows `AGENTS.md`, `.agent/PLANS.md`, and
`.agents/skills/logical-commits/SKILL.md` in the TotalCross repository.

## Purpose / Big Picture

Execute this plan only after
`perf/image-scroll-raster-fast-path` has completed the
`image-warm-copyrect-fast-path` ExecPlan and has been pushed.

Create a new branch:

- `perf/image-scroll-prefetch`

from the completed `perf/image-scroll-raster-fast-path` head.

Implement the first ScrollContainer image-prefetch policy as `PREFETCH_ALL`.
After layout, an explicit prefetch request prepares every preparable descendant
without waiting for first draw to trigger JPEG decode or image materialization.

The caller-facing operation is asynchronous: it returns immediately and invokes
its completion callback on the UI thread. Applications can keep a spinner or
loading control visible while the initial images are prepared, then reveal or
enable scrolling content after completion.

The first policy intentionally prepares all descendants because historical
TotalCross images were eager and many applications already tolerate an initial
loading phase. This establishes a compatibility/performance baseline. Do not
implement viewport-sized, directional, velocity-based, or eviction-aware
prefetch in this plan. Keep discovery separate from preparation so later plans
can reduce the working set without redesigning the image API.

Prefetch must use destination content scale known before paint. ScrollContainer
captures it once for the batch and passes it through a preparation context. Do
not call `getGraphics()` on each child just to discover scale.

Observable result:

- `ScrollContainer.prepareForDisplay(...)` prepares all image descendants;
- the call is non-blocking;
- completion runs on the UI thread;
- the 663-JPEG benchmark can wait for prefetch, then scroll without first-use
  JPEG decode caused by entering the viewport;
- all feature-13/14 combinations remain benchmarked.

## Working Set and Resume Protocol

Execution plan path:

- `.agent/plans/image-scroll-prefetch-execplan.md`

Supporting files:

- `.agent/state/image-scroll-prefetch.md`
- `.agent/evidence/image-scroll-prefetch.md`
- `.agent/reports/image-scroll-prefetch-editorial.md`

Committed benchmark evidence:

- `.agent/evidence/image-scroll-prefetch/baseline/`
- `.agent/evidence/image-scroll-prefetch/final/`
- `.agent/benchmarks/image-scroll-prefetch/final-fixed/`

On resume, read state first and then only active paths.

Relevant paths:

- `TotalCrossSDK/src/main/java/totalcross/ui/ScrollContainer.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/ClippedContainer.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/Container.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/Control.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/ImageControl.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/gfx/Graphics.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/image/Image.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/image/ImagePipeline.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/image/ImageDrawingBridge.java`
- encoded-image source/decode classes used by `Image`
- `TotalCrossSDK/src/main/java/totalcross/util/concurrent/AsyncTask.java`
- `TotalCrossSDK/src/main/java/totalcross/util/concurrent/ThreadPool.java`
- focused ScrollContainer/Image tests
- `ImageScrollRealWorkloadBenchmarkApp`
- `ImageRasterBenchmarkSupport`
- `scripts/run-image-scroll-real-workload-benchmark.py`

Do not finish or redesign the legacy `ImageLoader` in this plan. Preparation
belongs at the Image/drawing boundary so a future ImageLoader can reuse it.

Use `TC_IMAGE_CORPUS` for the exact 663-JPEG corpus. Fail if unavailable or if
the count differs.

New files must remain below 20 KiB and approximately 600 lines. Split helpers by
responsibility if needed. Do not refactor existing files only to reduce size.

Commit all intentional plan artifacts and benchmark evidence. Ordinary build
logs/generated outputs remain uncommitted. Split committed benchmark logs
losslessly if they exceed 20 KiB.

Local builds are restricted to SDK and macOS and only at related milestone
ends. Native smoke tests may run only at related milestone ends and final
validation. Do not run other local platform builds.

## Progress

- [x] Verify completed parent and create `perf/image-scroll-prefetch`.
- [x] Run and commit prefetch-plan baseline benchmark matrix.
- [x] Add reusable asynchronous image preparation.
- [x] Add ScrollContainer `PREFETCH_ALL` traversal/batch completion.
- [x] Extend customer benchmark for disabled versus all-prefetch.
- [x] Run final matrix/smokes, commit evidence/report, and push.

## Current Architecture and Scope

`ClippedContainer` already knows how to locate visible children efficiently and
`ScrollContainer` owns the scrolling `bag`. `PREFETCH_ALL` does not need a
visible range yet, but discovery must remain separate from preparation so later
work can replace "all descendants" with "visible plus near-visible".

`ImageControl` owns the current Image and post-layout logical geometry. Image
pipeline owns deferred operations. Destination surface owns content scale. Keep
those responsibilities separate.

Preparation means doing expensive work that next draw would otherwise trigger,
without drawing pixels. `DRAW_READY` prepares targeted decoded backing and a
cached draw plan for the requested destination scale. `COPY_READY`, used by the
default `ImageControl` copyRect path, additionally materializes and caches the
final raster before scrolling. Repeated `COPY_READY` requests use that final
cache without new work.

The native backing registry is global and must not be mutated concurrently from
an arbitrary worker while UI draw uses it. Therefore background prefetch has two
stages:

1. worker: read/decode immutable encoded image data into a detached prepared
   backing candidate not registered in the global backing map;
2. UI: adopt/install that candidate into the encoded source, update decode
   generation through existing source semantics, and satisfy the requested
   readiness level by building a draw plan or materializing/caching the final
   raster.

Worker stage must not call `registerBackingRecord`, mutate live
`NativeImageBackingRecord`, or touch `SkCanvas`. UI adoption must not perform
JPEG decode. `COPY_READY` may perform the required final materialization on the
UI side, after detached decode and adoption have completed.

Use one background decode worker initially. Queue all requests but keep active
decode concurrency at one.

If an image is already ready for same destination scale and source generation,
complete from cache. Duplicate in-flight requests join the same request.

Unsupported mutable/non-encoded cases retain normal draw behavior. They must not
force a large synchronous fallback from worker; report them as
not-prefetchable.

This plan intentionally does not implement cache eviction. Record PREFETCH_ALL

After `COPY_READY` has a final cached variant, the intermediate decoded source
backing and transient draw plan may be released when no final cached variant
still references that backing. This is preparation lifecycle cleanup, not an
LRU, viewport, or memory-pressure eviction policy.

## Plan of Work

### Milestone 0 — Create branch and record baseline

Require clean worktree.

Run:

```bash
git switch perf/image-scroll-raster-fast-path
git fetch origin
test -z "$(git status --porcelain)"

parent_head="$(git rev-parse HEAD)"
remote_head="$(git ls-remote --heads origin   refs/heads/perf/image-scroll-raster-fast-path | awk '{print $1}')"
test "$parent_head" = "$remote_head"

git switch -c perf/image-scroll-prefetch
git push -u origin perf/image-scroll-prefetch
```

Record `parent_head` in state/evidence as immutable prefetch base.

At this milestone end run the real-workload benchmark exactly as left by the
first plan: both resolutions and all four feature-13/14 combinations, with the
explicit common optimization profile.

Commit newly executed baseline evidence; do not copy prior result files:

- `test(image): record prefetch baseline`

### Milestone 1 — Add reusable asynchronous image preparation

Add one internal image operation that prepares what a plan-aware draw needs
without drawing.

Use preparation key:

```text
Image identity / pipeline
destination contentScale
source decode generation
```

Required states:

```text
not requested -> queued -> decoding -> adopting -> ready
                              \-> failed/not-prefetchable
```

A repeated ready request completes without new decode. A repeated in-flight key
joins existing completions.

For supported encoded immutable sources:

1. resolve target decode size from existing pipeline and requested destination
   scale, reusing draw target-decode policy;
2. decode on the single worker into detached native candidate;
3. post adoption with `MainWindow.getMainWindow().runOnMainThread(...)`;
4. adopt through existing encoded-source decoded-backing installation and
   update generation exactly once;
5. for `DRAW_READY`, build/cache the draw plan; for `COPY_READY`, materialize
   and cache the final raster, then release only unreferenced intermediates;
6. complete callbacks on UI thread.

Do not duplicate JPEG denominator/resize policy in UI classes.

`DRAW_READY` must not perform final smooth resampling when the draw-fusable
physical identity path can use the targeted backing. `COPY_READY` intentionally
does the final materialization required by copyRect before completion.

Cancellation is generation-based in this version: stale request may finish and
its immutable decoded result may remain cacheable, but it must not complete a
newer batch. Do not build a general cancellation framework.

Expose cross-package access through the existing hidden
`ImageDrawingBridge`; avoid a broad new public Image API.

Focused tests:

- ready/in-flight deduplication;
- exact destination-scale keying;
- source-generation invalidation;
- UI-thread completion;
- detached worker decode followed by UI adoption;
- no final raster materialization for fusable smooth-scale JPEG;
- failure/not-prefetchable leaves normal draw fallback intact.

At milestone end run focused SDK tests/build. If native decode/adoption changed,
run permitted macOS native build and focused native image smoke.

Commit separately from ScrollContainer policy:

- `feat(image): add asynchronous display preparation`

### Milestone 2 — Add ScrollContainer PREFETCH_ALL

Add a small internal display-preparation protocol in `totalcross.ui`.

Context contains at least:

- destination content scale;
- batch generation/token.

Do not store content scale permanently on ImageControl.

Use a package-private preparable contract:

- ordinary Control: no preparation;
- Container: recursively forwards to descendants;
- ImageControl: prepares the image that will actually draw and a distinct
  background image if present through `ImageDrawingBridge`.

Do not prepare both `img` and `img0` when they represent the same effective
source; image preparation key handles deduplication.

Expose one explicit ScrollContainer operation:

```java
prepareForDisplay(Runnable onComplete)
```

Required behavior:

- returns immediately;
- first implementation prepares all preparable descendants of scrolling bag;
- completion runs once on UI thread after all requests are ready, failed, or
  not-prefetchable;
- repeated call during same active batch attaches completion without duplicating
  work;
- call after layout/content-scale relevant change creates a new batch;
- stale batch completions never complete newer batch.

Capture destination scale once per batch from destination/window surface. For
the current main-window ScrollContainer path use destination-owned main-window
content scale without calling child `getGraphics()`.

Keep child discovery separate from preparation. Add a small range/traversal
helper in `ClippedContainer` only if useful for future visible-range policy. Do
not implement directional or viewport-limited policy now.

ScrollContainer does not own a spinner. Caller controls loading UI:

```text
show spinner
scroll.prepareForDisplay(callback)
callback -> hide spinner / reveal content
```

Tests:

- nested row Containers with ImageControls;
- zero children;
- duplicate image references;
- callback ordering;
- content-scale change;
- repeated calls.

Add one smoke fixture proving UI thread remains responsive during background
decode using a timer/update counter rather than subjective animation.

At milestone end run focused SDK tests/build and permitted macOS smoke.

Expected logical commits:

- `feat(ui): add display preparation traversal`
- `feat(ui): prefetch ScrollContainer descendants`

### Milestone 3 — Extend customer benchmark for prefetch

Add:

- `--prefetch=disabled`
- `--prefetch=all`

For disabled, preserve current behavior.

For all:

1. build/layout the same 663-image UI;
2. start prefetch timer;
3. call `scroll.prepareForDisplay(...)`;
4. start scroll passes only in completion callback;
5. reset decode/draw accounting after prefetch and before cold pass;
6. record `prefetch_elapsed_ms`, request count, ready count,
   failed/not-prefetchable count, and live/peak backing bytes at completion;
7. run cold/warm/warm2 exactly as disabled profile.

At both 480x720 and 540x960 run fresh processes for:

```text
prefetch disabled x 13 off x 14 off
prefetch disabled x 13 on  x 14 off
prefetch disabled x 13 off x 14 on
prefetch disabled x 13 on  x 14 on
prefetch all      x 13 off x 14 off
prefetch all      x 13 on  x 14 off
prefetch all      x 13 off x 14 on
prefetch all      x 13 on  x 14 on
```

Keep six common optimization features explicitly enabled so default semantics
do not contaminate the matrix.

Commit benchmark harness before final measurement:

- `test(image): add ScrollContainer prefetch benchmark`

### Milestone 4 — Final benchmark and closeout

At milestone end run complete final matrix plus focused native/UI smoke tests.

Acceptance for `prefetch=all`:

- all 663 image requests accounted for;
- supported JPEG requests reach ready with no failure;
- completion callback runs on UI thread;
- responsiveness smoke shows update/timer progress while worker decode is active;
- after accounting reset, cold scroll performs no first-use JPEG decode for
  successfully prefetched images;
- default `ImageControl` requests `COPY_READY`, so cold scroll has at most the
  three non-prefetchable/full-decode cases and at most three final/native
  materializations;
- warm behavior has zero new decodes/materializations and no p95 regression
  beyond the rounded 10% disabled-profile ceiling;
- all 13/14 combinations remain correct and separately measured.

Report:

- cold p95/p99 and >=17 ms frames versus disabled;
- prefetch elapsed time versus former cold-scroll decode time;
- live/peak backing memory after prefetch;
- warm metrics.

Do not add viewport, LRU, or memory-pressure eviction heuristics. Report the
live/peak backing memory after `COPY_READY` releases only unreferenced decoded
source intermediates.

For disabled profile, performance must remain within normal benchmark noise of
baseline. If repeated warm p95 regresses >10%, identify/correct before closeout.

Commit final evidence/report, mark state complete, recheck remote head, push
normally:

- `test(image): record ScrollContainer prefetch results`
- `docs(plan): close image prefetch plan`

## Surprises & Discoveries

Record only observations that change remaining work: detached decode limitation,
unexpected final resampling, batch/layout race, or disabled-profile regression.

- The startup layout can change the content-scale value after a preparation
  batch captures its request key. Batch completion therefore relies on active
  batch identity; the captured scale still keys each preparation request.
- Benchmark execution is deferred to the next UI timer tick after the async
  completion runner so `repaintNow()` is not suppressed by the runner drain.
- The original final matrix removed JPEG decode but still performed roughly
  645--648 final/native materializations during cold scrolling. The corrected
  matrix moves those materializations into `COPY_READY` preparation and leaves
  at most three cold materializations.
- Releasing the decoded source after final caching reduced corrected live
  backing memory to 282--357 MB without adding general cache eviction.

Preserve fixed architecture: worker decode remains detached; global backing
adoption remains on UI thread.

## Decision Log

- Create `perf/image-scroll-prefetch` from completed warm-path branch.
- First policy is all descendants to approximate historical eager loading.
- Prefetch is explicit and asynchronous.
- ScrollContainer owns discovery; Image owns preparation.
- Destination content scale is captured once per batch.
- Worker produces detached candidates; UI thread adopts global backing.
- Use one worker initially.
- Do not integrate cache eviction here.
- `COPY_READY` is the explicit contract for default copyRect descendants;
  releasing unreferenced intermediate source backing is part of that lifecycle,
  not a cache policy.

## Validation and Acceptance

Before every commit follow `.agents/skills/logical-commits/SKILL.md`: read state,
inspect scoped changes, validate copyright headers, stage only intended files,
run `git diff --check --cached`, use English scoped Conventional Commits with
body, validate commit message, update state.

Local build restrictions:

- SDK only at related milestone end;
- macOS native only at related milestone end;
- native smoke only at related milestone end and final closeout;
- no Android/Linux/Windows/iOS local build.

Before/after benchmark evidence is mandatory and committed. Final evidence
includes every feature-13/14 combination.

## Risks and Open Questions

No architectural choices are intentionally delegated.

Do not:

- make ScrollContainer decode JPEGs;
- call child `getGraphics()` for content scale;
- spawn one thread per image;
- register native backings from worker;
- add global draw-path locking;
- automatically manage application spinner visibility;
- implement viewport/directional prefetch;
- implement cache eviction/Phase 4A.

Unsupported images remain not-prefetchable and use normal draw.

## Idempotence and Recovery

Branch creation is safe only when parent local/remote heads match and worktree
is clean.

If `perf/image-scroll-prefetch` already exists, use it only when state records
the same parent base; otherwise stop instead of resetting/force-updating it.

Repeated `prepareForDisplay` calls are deduplicated by batch and image key.
Stale batch never completes newer batch. Abandoned detached candidates must
release native resources.

Benchmark reruns use new evidence paths; do not overwrite committed
measurements. All pushes are fast-forward; no force push is allowed.

## Outcomes & Retrospective

At completion summarize parent/final commits, preparation worker/adoption
behavior, ScrollContainer batch semantics, request/ready/failure counts,
disabled versus all-prefetch cold metrics, warm metrics, 13/14 results,
prefetch time/memory cost, and limitations for later viewport-sized prefetch.
The corrected final matrix passed 16 macOS processes and 48 records: 663
requests/660 ready/0 failed/3 not-prefetchable, zero cold targeted decodes,
at most three cold final/native materializations, zero warm decodes or
materializations, cold p95 at most 6 ms, warm all-prefetch p95 at most 5 ms,
and live backing memory of 282--357 MB. See the committed final-fixed summary
and 295--370 MB after cold/warm scrolling; target-color and physical-variant
bytes were zero in all-prefetch records. See the committed final-fixed summary
and editorial report for the complete matrix.

Point to committed evidence instead of duplicating raw output.

## Revision Note

Closed on 2026-09-10. The corrected implementation adds explicit
`DRAW_READY`/`COPY_READY` semantics, bounded detached preparation, UI-thread
adoption, active-batch invalidation, per-request optimization-mask capture,
and final-raster caching for copyRect descendants. `PREFETCH_ALL`, one worker,
and no general cache-eviction policy remain fixed decisions.
