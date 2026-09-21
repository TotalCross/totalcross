<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Validate raster scroll reuse with dirty-region repaint

This ExecPlan follows `AGENTS.md`, `.agent/PLANS.md`, and
`.agents/skills/logical-commits/SKILL.md`. It is a CPU/raster-only proof of concept.

## Purpose / Big Picture

Create a new branch named `perf/scroll-raster-reuse-poc` from
`perf/writepixels-tail-diagnosis` and validate one minimal optimization:
vertical `ScrollContainer` movement may reuse pixels already present in the
software framebuffer, move those pixels by the scroll delta, and repaint only
the newly exposed strip instead of repainting the complete visible scroll
content.

The implementation must be switchable OFF/ON. OFF is the current behavior and
is the benchmark baseline. ON attempts the fast path and falls back to the
current repaint path whenever eligibility is not proven.

End after the macOS raster benchmark. Report the committed result path and an
evidence-based classification. Do not promote defaults or start follow-up work.

## Working Set and Resume Protocol

Create and maintain:

- `.agent/plans/scroll-raster-reuse-poc.md` — active ExecPlan.
- `.agent/state/scroll-raster-reuse-poc.md` — first read on every resume.
- `.agent/evidence/scroll-raster-reuse-poc.md` — append-only compact evidence.
- `.agent/reports/scroll-raster-reuse-poc-editorial.md` — final factual handoff.
- `.agent/benchmarks/scroll-raster-reuse-poc/final/` — committed benchmark
  summaries and compact per-run evidence.

On resume, read state first and only the active paths it names. Do not reread
historical image/writePixels investigations unless state requires it.

Bootstrap:

1. Read `AGENTS.md`, `.agent/PLANS.md`, and
   `.agents/skills/logical-commits/SKILL.md` once.
2. Verify that `perf/writepixels-tail-diagnosis` exists locally or as a fetched
   remote ref. If it does not exist, stop and report the blocker; do not
   substitute another base branch.
3. Record the exact base SHA in state/evidence.
4. Create `perf/scroll-raster-reuse-poc` from that exact SHA. If resuming an
   existing branch, verify its recorded base instead of recreating it.
5. Preserve unrelated local changes. Do not push.

## Progress

- [x] Bootstrap the branch and commit this plan plus initial state.
- [ ] M1 — implement the minimal raster scroll-reuse path and instrumentation.
- [ ] M2 — add the reduced realistic benchmark and prove visual/behavioral
      correctness on macOS.
- [ ] M3 — run the small diagnostic/performance matrices, commit results, and
      evaluate the outcome at `STOP / REVIEW`.

## Current Architecture and Scope

`ScrollContainer.internalScrollContent(...)` changes the scrollbar value, moves
its `ClippedContainer bag`, and sets `Window.needsPaint = true`. The current
benchmark then calls `repaintNow()`, so the visible content is drawn again.
`ClippedContainer.paintChildren()` already culls controls outside the viewport,
but it still repaints the visible rows after every scroll step.

For SDL software rendering, `initSkia(...)` installs the SDL backbuffer pixels
in the global Skia `SkBitmap`; `flushSkia()` later uploads the complete bitmap
through SDL2 `SDL_UpdateTexture(texture, NULL, ...)` and presents it. Therefore
the proof of concept can move framebuffer pixels directly without introducing a
second surface.

This plan deliberately keeps SDL2 presentation unchanged. Even on a fast-path
hit, the final frame is still uploaded/presented exactly as today. This isolates
savings in UI traversal and raster painting from a future SDL dirty-upload
experiment.

Do not change or reuse the existing `Settings.optimizeScroll`/`takeScreenShot` path.

### Fixed feature shape

Add a small rendering-optimization mask, separate from `ImageOptimizations`,
with a default value of zero and one initial bit:

`SCROLL_RASTER_REUSE = 1`.

Name the Java holder `totalcross.ui.RenderingOptimizations`. Keep the API small:
constant(s), mask getter/setter, and an internal enabled check. Do not enable the
bit by default.

The fast path is vertical-only in this plan. Horizontal scrolling always falls
back.

### Native raster primitive

Add one software-raster primitive exposed through the existing `Graphics`
native-replacement path:

`scrollRasterRegion(x, y, width, height, deltaY)`

Coordinates and delta are physical pixels. The primitive must:

- work only with the software Skia screen bitmap;
- reject null pixels, invalid bounds, zero delta, or `abs(deltaY) >= height`;
- support the current BGRA8888 target and RGB565 without format conversion;
- move only the retained rows with overlap-safe row-wise `memmove`;
- iterate in the safe direction for positive/negative movement;
- call the appropriate SkBitmap pixel-change notification after mutation;
- return success/failure and never present the screen itself.

Do not call SDL or add GPU behavior here.

Add a test-only raster-region hash primitive beside it. It must hash the exact
physical viewport bytes deterministically so the benchmark can compare OFF/ON
frames at fixed waypoints without using `takeScreenshot`.

### Fast-path sequence

For an actual vertical scrollbar movement, compute the real post-clamp logical
delta, not the requested delta. On an eligible attempt:

1. Compute the scrolling-content viewport in window coordinates, excluding
   opaque scrollbar space.
2. Convert all viewport edges and the actual delta to physical coordinates
   using the current main-window content scale. Require exact integral physical
   geometry; otherwise fall back.
3. Move the `bag` to its new logical position using the existing path.
4. Move the old framebuffer viewport pixels by `-actualScrollDelta` in physical
   Y. The framebuffer still represents the pre-scroll frame at this point.
5. Compute the newly exposed logical strip at the opposite edge.
6. Repaint only that strip: repaint the bag background clipped to the strip and
   paint only bag children that intersect it. Nested children of an intersecting
   row paint normally.
7. Repaint the vertical scrollbar itself in the same frame only if it is visible
   and does not overlap the content viewport. A transparent/overlay scrollbar
   makes the fast path ineligible for this first proof of concept.
8. Present once through the existing `safeUpdateScreen()` path. SDL2 continues
   to upload/present the full frame.
9. Do not set `Window.needsPaint` for a successful hit. On fallback, preserve
   the current behavior exactly.

If a failure occurs after pixels were moved, immediately force a complete normal
repaint before presenting. Count this as post-move recovery; the benchmark gate
requires zero such recoveries.

### Dirty painting

Extend `ClippedContainer` with one package-level dirty-region painting path. Do
not add a generic application-wide damage system.

The dirty path must reuse the existing visible-child search but intersect it
with the supplied dirty strip. For `verticalOnly`, use the dirty Y range as the
search range rather than scanning every visible row. Do not change ordinary
`paintChildren()` semantics.

Do not add per-control raster surfaces or caches.

### Conservative eligibility and fallback reasons

Every non-zero vertical scroll with the feature enabled is an attempt. A hit
requires all of the following:

- software raster backend is available;
- the parent window is topmost;
- no unrelated `Window.needsPaint` was pending before the scroll;
- no legacy `bag.offscreen`/`offscreen0` screenshot is active;
- the scroll container/bag can repaint the exposed background without needing
  transparent-parent reconstruction;
- the physical viewport and delta are integral;
- `0 < abs(deltaPhysicalY) < viewportPhysicalHeight`;
- no visible external control painted above the scroll container intersects the
  viewport; walk ancestor sibling paint order conservatively;
- no transparent/overlay scrollbar intersects the reusable viewport;
- the native raster move succeeds.

Record one primary fallback reason per failed attempt. Use stable reason names:
`unsupportedBackend`, `notTopmost`, `pendingRepaint`, `legacyOffscreen`,
`transparentContent`, `nonIntegralPhysicalGeometry`, `deltaOutOfRange`,
`overlappingControl`, `overlayScrollbar`, `nativeMoveFailure`, and
`postMoveRecovery`. Horizontal movement is recorded separately as
`unsupportedHorizontal` and is not silently treated as a hit.

## Instrumentation Contract

Instrumentation must prove execution, explain fallback, and measure saved work.

Keep accounting switchable so performance runs do not pay diagnostic overhead.
Reuse existing benchmark conventions rather than printing per-frame logs to
stdout.

For each process and pass record:

- attempts, hits, fallbacks, and each fallback-reason count;
- logical/physical viewport dimensions and actual scroll delta distribution;
- viewport pixels, reused pixels, dirty pixels, bytes moved;
- `reuseCoverage = reusedPixels / affectedViewportPixels`;
- raster-move time, eligibility/decision time, dirty-paint time;
- screen-update/presentation time measured around `safeUpdateScreen()` for both
  baseline and fast-path runs;
- total frame/work time P50/P95/P99/MAX;
- scroll-work and paint-work P50/P95/P99/MAX;
- direct row/control paint count and image `onPaint` count.

For the benchmark only, use small measured subclasses for row containers and
`ImageControl` so actual paint calls are counted in both OFF and ON runs without
adding general per-control instrumentation to the SDK.

Persist a compact per-frame CSV containing at least frame index, elapsed time,
scroll position, requested/actual delta, total work time, screen-update time,
fast-path hit/fallback reason, moved bytes, dirty pixels, row paints, and image
paints. Keep every generated committed file below 20 KB / about 600 lines;
split by process/pass when necessary.

At fixed scroll waypoints record the native viewport hash. OFF and ON hashes
must match for the same pass/waypoint/configuration.

## Benchmark Workload

Use the first 120 sorted corpus images, not the complete 663-image traversal,
and keep three images per row.

Force these conditions for every measured run:

- `ImageOptimizations = 0`;
- prefetch ON and completed before the first measured pass;
- identical corpus/order/window size;
- same controls, images, native backings, and caches for cold and warm passes;
- no cache clearing or UI reconstruction between passes;
- legacy `Settings.optimizeScroll` screenshot behavior must not activate.

Each process runs two measured passes.

`cold-search` is the first scroll after prefetch. Let `V` be the vertical
visible-items/viewport extent and clamp every waypoint to the valid maximum.
Use this exact search pattern:

`0 -> 7V -> 4V -> 5V -> 4.5V`.

Interpolate monotonically between waypoints using `500 ms`, `250 ms`, `200 ms`,
and `150 ms` segments.

After `cold-search`, perform an unmeasured reset to position zero using normal
scrolling/repaint, wait until presentation completes, and reset only diagnostic
counters. Do not recreate or clear content.

`warm-search` repeats the exact same waypoint pattern and timings. It therefore
measures the same visual work after the same content region has already been
visited.

## Plan of Work

### M0 — Bootstrap and plan state

Create the branch and supporting files. Record the exact base SHA and deliberate
out-of-scope areas. Commit the plan/state as one logical documentation commit.
Do not build.

Acceptance: branch ancestry is proven, state is resumable, no unrelated files
are staged.

### M1 — Implement raster reuse and diagnostics

Implement, in logical commits:

1. the native software raster move/hash primitive and focused native tests;
2. `RenderingOptimizations`, `ScrollContainer` eligibility/execution, dirty
   repaint support in `ClippedContainer`, and diagnostic/reset accessors;
3. benchmark-visible screen-update timing needed by the A/B measurement.

Do not change SDL2 texture update/present code except for strictly diagnostic
measurement if the existing Java timing cannot observe `safeUpdateScreen()`
reliably. Prefer Java-side timing.

After these implementation commits, perform the first allowed build gate:

- SDK build using `TotalCrossSDK/gradlew-agent`, without `clean` unless stale
  output is demonstrated;
- macOS software/SDL native build for the affected targets only;
- focused native raster primitive smoke/test.

Capture verbose output under `/tmp`; do not commit normal build logs/output.
Record compact status in evidence.

M1 PASS requires: OFF behavior unchanged in focused tests; native move/hash tests
pass for positive/negative Y, overlap, bounds, BGRA8888, and RGB565 where the
existing test infrastructure supports those formats; ON can hit on a simple
unobscured vertical scroll; all tested fallback reasons preserve normal repaint.

### M2 — Reduced realistic benchmark and correctness gate

Adapt the existing image-scroll benchmark/runner rather than creating a second
large framework. Add a dedicated short profile for this plan with the fixed
120-image workload and two passes above.

Diagnostic matrix, accounting ON:

- raster reuse OFF: 1 process;
- raster reuse ON: 1 process.

Both processes use `ImageOptimizations=0`, prefetch ON, `cold-search` and
`warm-search`.

Run the macOS package/self-test only at the end of this milestone. Reuse M1
build outputs when valid; do not clean/rebuild merely to repeat validation.

M2 PASS requires:

- both processes complete both passes and all waypoints;
- OFF/ON viewport hashes match at every corresponding waypoint;
- ON records fast-path hits in the unobscured workload and zero post-move
  recoveries;
- attempts = hits + fallbacks;
- reused + dirty coverage is internally consistent with viewport/delta;
- diagnostic OFF run records no fast-path hits;
- committed compact evidence identifies any fallback reason that occurred.

If hashes differ, stop M2 and fix correctness before performance measurement.
Do not benchmark a known visual mismatch.

### M3 — Performance matrix, evaluation, and final gate

Performance matrix, accounting OFF:

- raster reuse OFF: 3 independent processes/rounds;
- raster reuse ON: 3 independent processes/rounds.

Each process contains both cold and warm passes. Six processes total. Do not add
more rounds unless variance makes the conclusion impossible; if more are needed,
record the reason before running them.

Aggregate per pass and mode:

- frame/work P50/P95/P99/MAX;
- screen-update time P50/P95/P99/MAX;
- cold versus warm deltas;
- row/image paint counts and reduction;
- moved bytes, dirty pixels, reuse coverage and hit rate from the diagnostic
  matrix;
- top five expensive frames per mode/pass with their scroll position and
  fast-path evidence.

Use OFF versus ON only. Do not introduce ImageOptimization combinations,
prefetch OFF, SDL partial upload, GPU, animation, or generalized dirty regions.

Write committed results under:

`.agent/benchmarks/scroll-raster-reuse-poc/final/`

At minimum commit `report.md`, `diagnostic-summary.csv`,
`performance-summary.csv`, `waypoint-hashes.csv`, and compact per-process/pass
frame files needed to support P95/P99/MAX claims. Do not generate a new result
file that exceeds the 20 KB / 600-line limit; split it instead.

Classify the result in `report.md` using these categories:

- `PROMISING`: correctness passes, the ON workload achieves high hit/reuse
  coverage, substantially reduces row/image paints, and improves total frame
  distribution without a material P95/P99 regression.
- `PRESENTATION-BOUND`: correctness and work reduction pass, but total frame
  improvement is small because screen-update/presentation time dominates the
  remaining frame cost. This is evidence for a later SDL2 presentation study,
  not permission to implement it here.
- `NOT BENEFICIAL`: correctness passes but total frame time is neutral/worse and
  presentation does not explain the result.
- `INVALID`: any visual/hash mismatch, inconsistent accounting, or unresolved
  post-move recovery remains.

Report measured numbers; do not force a favorable classification from an
arbitrary percentage threshold.

Finish at `STOP / REVIEW`. Do not implement follow-up optimization.

## Validation and Acceptance

Use the smallest validation that proves each logical commit. Before every
commit:

1. run focused copyright-header validation for changed first-party files;
2. stage only task paths;
3. run `git diff --check --cached`;
4. inspect staged stat and staged diff;
5. commit signed using the `logical-commits` skill;
6. validate the resulting commit message with the skill's local checker;
7. update state after the logical commit.

Build operations are permitted only for the SDK and macOS and only at the end
of related milestones. Native smoke tests are permitted only at the end of
related milestones and at final execution. Do not run Android, Linux, Windows,
iOS, Docker, or GPU builds in this plan.

Run `clean` only when stale output is demonstrated and recorded.

Final acceptance requires M2 correctness PASS, the complete six-process M3
performance matrix, committed result artifacts, final state/evidence/editorial
report, and a clean scoped `git diff --check`.

The final agent response must include:

- final branch/HEAD;
- repo-relative path `.agent/benchmarks/scroll-raster-reuse-poc/final/`;
- exact result classification;
- concise OFF/ON cold and warm P50/P95/P99/MAX deltas;
- hit rate/reuse coverage and paint reduction;
- whether presentation time appears to limit the gain;
- validations run and any explicitly deferred work.

## Commit Checkpoints

Use frequent behavior-oriented commits. Expected boundaries are:

1. `docs(benchmark): plan raster scroll reuse validation`
2. `feat(vm): add raster screen scroll primitive`
3. `feat(sdk): add scroll raster reuse fast path`
4. `test(benchmark): add realistic scroll reuse workload`
5. follow-up `fix(...)` commits only for defects proven by validation;
6. `docs(benchmark): record scroll reuse benchmark results`

Do not combine unrelated cleanup, rewrite history, or push.

## Risks and Open Questions

The only open questions to resolve by measurement are performance questions:
how much raster/UI work is removed, how much total frame time improves, and
whether the unchanged full SDL upload/present becomes dominant.

Architecture is not open for agent choice during this plan. In particular:

- do not replace framebuffer reuse with per-control surfaces;
- do not add tiles;
- do not add GPU paths;
- do not add `SDL_UpdateTexture` dirty rectangles;
- do not materialize a separate Skia surface merely for scrolling;
- do not modify `PathAnimation`, `TopMenu`, `SideMenu`, or screenshot logic;
- do not enable any ImageOptimization bit.

If overlay safety is not proven, fall back.

## Idempotence and Recovery

All benchmark profiles must write into an explicit run directory and refuse to
silently merge samples from different HEADs. Rerunning a failed process may
replace only that process's incomplete output before final aggregation.

Never mix OFF/ON samples built from different implementation HEADs. If code that
can affect rendering or measurement changes after M3 begins, invalidate and
rerun the complete six-process performance matrix.

Keep build output/logs outside Git. Commit plan-created source, state/evidence/
report, benchmark changes, and compact results. Preserve unrelated files.

## Outcomes & Retrospective

At each milestone record factual outcome and evidence path. At completion state
correctness, paint reduction, cold/warm deltas, and whether rendering or
full-frame presentation appears dominant.

## Revision Note

Initial plan: fixed a vertical-only CPU/raster proof of concept, preserved SDL2
full-frame presentation, disabled all ImageOptimizations, required prefetch,
and replaced the historical long traversal with a short realistic search
pattern and small A/B matrices.
