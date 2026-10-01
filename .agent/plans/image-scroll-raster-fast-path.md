<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image Scroll Raster Fast Path

This ExecPlan follows `.agent/PLANS.md` and `AGENTS.md`.

## Context

Deferred Image pipelines already support a native draw plan, a two-entry draw-plan cache, and a one-entry materialized fallback admitted after a repeated request. Native P3 drawing first checks exact physical identity, then considers target-color and physical raster variants, then uses generic geometry. `Graphics.copyRect(GfxSurface, ...)` currently resolves an Image before native copy, so it cannot use those existing facilities while the image remains deferred.

## Purpose / Big Picture

Allow `Graphics.copyRect(Image, ...)` and physically exact Image draws to reuse existing pixels during scrolling. Warm copies should be able to use an already cached final raster, a native plan-aware rectangle copy, or a safe direct software-raster copy. Unsupported or uncertain cases keep the existing materialization and rendering behavior.

## Objectives

- Make only the Image-source branch of `Graphics.copyRect` plan-aware, with explicit source and destination rectangles.
- Probe and reuse the existing cached final materialization without creating another cache or materializing on a miss.
- Reuse P3's exact physical identity proof for a bounded direct software copy.
- Preserve source/frame semantics, translation, clipping, copy operation state, JavaSE behavior, and fallback behavior.
- Add focused SDK/native tests, an artifact-safe internal bridge if needed, minimal IMAGE diagnostics, a warm-path smoke measurement, and a factual implementation report.

## Scope

P6 owns plan-aware `copyRect(Image, ...)`, reuse of the current one-slot materialized fallback, and direct copy after exact physical identity is proven. It does not change scheduler cadence, framebuffer scroll reuse, P3 cache capacity/admission, P3 variant priority, public runtime policy, or public API.

## Current Architecture and Scope

- `Graphics.copyRect(GfxSurface, ...)` resolves an Image before invoking the existing surface copy. Native copy accepts source x/y/width/height and destination x/y.
- `Graphics.copyImageRect(Image, ...)` already attempts an `ImageDrawPlan`, but its native plan-copy API places the result at the origin. P6 must use a separate native operation with explicit destination coordinates; it must not translate the canvas to simulate placement.
- `ImageDrawingBridge` has exactly two public static methods: `resolveForDrawing` and `drawPlanForDrawing`. Keep that surface unchanged.
- `ImagePipeline` owns one JavaSE materialized representation with one pending observation and two independent draw-plan cache entries. P6 must not add another materialized cache.
- The P3 native geometry path checks `physicalIdentity` before target-color conversion and physical variants. `ImageRuntimePolicy.RasterCorePolicy.physicalIdentity()` is enabled by default; target-color conversion and physical-variant caching remain disabled by default.
- P2 backing state is authoritative for backing identity, mutation generation, validity, and opacity.

## Architecture

Keep the public API unchanged. Add only private native declarations to `Graphics` and, if a cache probe cannot use the existing two-method `ImageDrawingBridge`, a separate unsupported internal `ImageDrawingFeatureBridge`. The new bridge may expose only the cache probe required by P6, must not expose `ImagePipeline`, and must be absent from `totalcross-api`, `totalcross-sdk`, and distributed app SDK artifacts. Extend artifact and compile-surface tests to prove that boundary.

Make native copy planning explicit: pass the plan, source rectangle, destination point, and clipping/state needed by the operation. Preserve the current `copyRectNative` path as the fallback. Keep JavaSE on its existing pixel-copy behavior.

Refactor the existing P3 physical mapping proof into an internal `RasterPhysicalPlan`-like result that contains the exact visible source and destination physical rectangles and whether direct copy is safe. P3 and P6 must consume the same conservative proof. Execute raw direct copy only for compatible software/raster backings and proven-safe pixel representations; otherwise continue through existing P3 or Skia rendering.

## copyRect plan path

For an Image source, preserve null and argument behavior, then check the exact cached final raster for the destination content scale. On a hit, call the existing native surface `copyRect` with that raster. On a miss, obtain the current draw plan and attempt native plan-aware copy with explicit source and destination geometry under the current transform and clip. Return when handled, including an empty visible intersection. If unsupported or unhandled, resolve/materialize through the existing Image path and call the current native copy. Do not materialize before the plan attempt.

Plan copy must preserve arbitrary source subrectangles, destination coordinates, current translation and clip, partial clipping, current-frame behavior, destination content scale, and copy operation semantics. It must never enlarge the copied region. A fully clipped request is a handled no-op with no destination or backing generation mutation.

## Cached-final reuse

The probe returns only an already existing cached final Image accepted by P3's current validity rules: exact destination-scale identity, matching source decode generation, current source/pipeline state, and a still-valid cached Image. A miss does not materialize or change cache admission state. Keep the existing one-slot/one-pending policy and two-entry draw-plan cache independent.

## Physical direct copy

The shared proof must require enabled `physicalIdentity` policy, stable authoritative source backing and matching relevant generations, exact integer mapping, equal physical extents, positive axis-aligned transforms, valid frame/source bounds, exact visible clip intersection, effective alpha 255, no color/filter/fill semantics requiring shaders, compatible source/destination pixel representation, and a destination with a safe writable software-raster path. Smooth-scale operations remain eligible only when the final physical mapping is exactly 1:1. Empty intersection is handled without mutation. Any uncertainty falls back.

When P4 compact storage is present after rebase, use only its existing bounded row-read primitive if it safely supports the copy; otherwise fall back without promoting or mutating the compact source. Do not add independent compact conversion logic.

## Compatibility constraints

- No optimization mask, feature bit, new public toggle, or added public `ImageDrawingBridge` method.
- Do not reorder P3 resolution: exact identity, target-color when applicable, physical variant, generic fallback. P6 direct copy is an execution within the identity stage.
- No second materialized raster cache and no change to one-slot/one-pending admission.
- Keep JavaSE semantic fallback and preserve P3 variant behavior.
- Do not modify scheduler cadence, event-loop timing, display/vsync, framebuffer reuse, or dirty-strip repaint algorithms.
- Keep new files below approximately 20 KB or 600 lines; do not split existing large files just to meet this limit.

## Plan of Work

1. **Plan-aware copyRect.** Add explicit plan-aware native copy and tests for full/subrect copies, non-zero destination, clipping, frame selection, destination scale, unsupported geometry/color fallback, and JavaSE parity. Acceptance: native plan attempt happens before Image resolution and preserves copy semantics.
2. **Cached-final reuse.** Add the narrow non-materializing probe and artifact-surface tests. Cover exact hit, scale and decode-generation invalidation, invalid cached Image, and a miss that reaches draw-plan handling without materialization. Acceptance: no additional materialized cache exists.
3. **Direct physical copy.** Factor the exact P3 mapping proof and use it for clipping and safe software-raster copy. Cover identity hits, smooth-scale physical identity, fallback cases, no-intersection no-mutation, policy disabled, and pixel-hash parity. Acceptance: hits avoid generic geometry and smooth resampling; P3 variant behavior stays unchanged.
4. **Measurement and integration.** Run focused P2/P3 regressions, artifact checks, diagnostics-off SDK artifact validation, relevant IMAGE diagnostics-on tests, the warm-path smoke measurement, and the allowed macOS ARM64 native build/smokes. Run the optional 663-image workload only if the established corpus is present and exactly 663 images. Prepare the report, final diff checks, and one PR against `master`; do not merge.

## Progress

- [x] (2026-10-01) Added plan-aware native `copyRect(Image, ...)`; focused Java parity and macOS native smoke pass.
- [x] (2026-10-01) Added the non-materializing cached-final probe, exact scale/decode-generation/backing-validity checks, and artifact exclusions/tests; focused SDK, distribution, and native smoke checks pass.
- [ ] Milestone 3: shared physical proof, direct copy, and native correctness tests.
- [ ] Milestone 4: measurement, integration validation, report, and PR.

## Decision Log

- Decision: Keep cache probing outside the two-method `ImageDrawingBridge` surface if required.
  Rationale: That bridge's two-method public internal surface is an explicit compatibility constraint; a cache bridge must stay narrow and excluded from application artifacts.
  Date: 2026-10-01.
- Decision: Share P3's exact physical mapping proof rather than define P6's own identity approximation.
  Rationale: A single conservative proof keeps P3 and P6 pixel semantics aligned.
  Date: 2026-10-01.
- Decision: An empty visible intersection is handled as a no-op.
  Rationale: It preserves clipping semantics and prevents spurious destination mutation.
  Date: 2026-10-01.
- Decision: Keep the native copy-plan method name within the VM's 32-character symbol limit.
  Rationale: Longer generated symbols are truncated during native method lookup.
  Date: 2026-10-01.

## Validation and Acceptance

Use the smallest validation that proves each slice. Run focused SDK tests for each milestone; run native tests/builds where the C++ path changes. At integration, run `artifactContentTest` and `dist -x test` with diagnostics disabled, relevant IMAGE diagnostics tests with `-PruntimeDiagnostics=true`, and only macOS ARM64 Release `tcvm` and `Launcher` builds/smokes. Do not locally build Android, Windows, Linux, WinCE, or iOS. Run the warm microbenchmark with independent Images and full-visible/partial-clip cases; timing has no pass threshold. The 663-image workload is optional and is not a merge blocker.

Acceptance includes P2 Raster Core and P3 Raster Variants regressions; Image deferred-transform and draw-plan tests; `Graphics.copyRect` semantics; converter/native ABI; artifact boundaries; diagnostics off/on; direct pixel-hash parity; and `git diff --check`. Record any deferred expensive validation and reason in the state file.

## Risks and Open Questions

- The existing Skia P3 physical proof may depend on geometry-specific state; confirm its inputs before factoring it so P3 behavior does not widen.
- The native surface abstraction may not expose a safe writable software raster for every target; unsupported destinations must fall back.
- Translation and clipping must be mapped to exact physical source/destination rectangles without fractional rounding or overdraw.
- Build and test targets for macOS ARM64 depend on the available Xcode/native dependency environment.
- The optional 663-image corpus may not be available.

## Working Set and Resume Protocol

- `.agent/state/image-scroll-raster-fast-path.md` records the active milestone, paths, next safe action, focused validation, deferred checks, and resume command. Read it first when resuming.
- `.agent/evidence/image-scroll-raster-fast-path.md` indexes compact validation and benchmark results. Read only entries relevant to the active milestone.
- `.agent/reports/image-scroll-raster-fast-path.md` is the final factual implementation handoff; update at milestone completion and completion.
- `.agent/archive/image-scroll-raster-fast-path-history.md` is reserved for completed milestone detail that would otherwise make this active plan too long; it is not read by default.

## Idempotence and Recovery

Keep changes limited to the paths listed in the active state file. Preserve unrelated local files and generated artifacts. Retry focused validation after fixing its cause; use task-specific log files and do not remove dependency caches or build outputs unless a specific stale artifact is proven to block the relevant command. A failed plan attempt must leave source/cache semantics unchanged.

## Outcomes & Retrospective

Milestone 1 routes deferred Image-source `copyRect` calls through a native geometry helper with explicit source and destination coordinates. Unsupported plans fall back to the existing materialized surface copy. The smoke verifies subrect, nonzero destination, translation, partial clipping, and empty-intersection generation stability.

Milestone 2 probes the existing one-slot materialized-variant cache before building a draw plan. Cache hits require the exact effective scale and current encoded-source decode generation, and invalid cached backing is evicted without disturbing the independent draw-plan cache. The narrow `ImageDrawingFeatureBridge` stays out of application SDK artifacts; the pre-existing two-method `ImageDrawingBridge` surface is unchanged. The native smoke confirms cache misses continue to plan-aware copy while deferred sources remain unmaterialized. See the evidence index.
