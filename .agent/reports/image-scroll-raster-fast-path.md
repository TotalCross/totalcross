<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image Scroll Raster Fast Path — Implementation Report

## Summary

`Graphics.copyRect(Image, ...)` can reuse an already cached final raster, copy from a deferred draw plan, or use a direct software-raster copy after P3 proves exact physical identity. Clipping, no-op handling, source generations, and fallback behavior remain part of the same draw pipeline.

## Final architecture

- `ImagePipeline` retains its existing one-slot materialized fallback and separate two-entry draw-plan cache.
- A narrow `ImageDrawingFeatureBridge` exposes the non-materializing cached-final probe without changing the two-method `ImageDrawingBridge` surface. Artifact tests exclude the internal bridge from application-facing SDK artifacts.
- Native `copyRectPlanNative` accepts explicit source and destination rectangles and applies translation and clipping before it draws.
- P6 direct copy executes inside P3's physical-identity stage. P3's target-color, physical-variant, and generic-rendering order remains unchanged.
- P4's compact backing remains authoritative: same-format RGB565 direct copy is supported, while GRAY8 and ARGB4444 use P3/Skia fallback without source promotion.

## copyRect plan-aware path

Image sources follow this resolution order:

1. Probe the current cached final raster and use the existing native surface copy on a hit.
2. On a miss, get the draw plan and attempt native copy with explicit source and destination geometry.
3. If the native plan cannot handle the operation, resolve the Image through the existing path and use native surface copy.

The JavaSE path continues through its existing pixel-copy behavior. Native argument, frame, and draw-state semantics are preserved.

## Cached-final raster reuse

A cached raster is reusable only for the exact destination-scale identity accepted by the P3 cache, the matching source decode generation, the current pipeline/source state, and a still-valid cached Image backing. A miss does not materialize an Image or change cache admission state. Evicting an invalid cached raster clears only that slot and preserves a pending admission observed for another scale/generation; mutation and pipeline barriers clear both. No additional materialized-image cache was added.

## Physical identity direct copy

The shared P3 proof requires stable authoritative backing and matching generations, valid source/frame bounds, exact integer mapping, positive axis-aligned transforms, equal physical source and destination extents, and the existing `physicalIdentity` policy. Direct copy additionally requires effective alpha 255, no shader-dependent color/filter/fill operation, matching source and destination color type, alpha type, and color space, readable source pixels, writable software-raster target pixels, and non-overlapping storage.

RGBA/BGRA 8888 and RGB 565 are supported when those conditions match. A smooth-scale plan remains eligible only when the final physical mapping is 1:1 and no pixel-center sampling or resampling remains; the scale-2 physical-identity case hits, while unit output scale keeps the smooth shader path. The pinned SkSurface write API returns no status, so the implementation uses SkCanvas' bool-returning writePixels call; a false result reports no direct-copy hit and continues through P3 identity drawing, variants, or generic rendering. Unsupported or uncertain cases use the same fallback chain.

## Clipping and fallback behavior

The native bridge trims the requested source and destination rectangles after translation and against the current rectangular clip. Direct copy writes only the visible intersection. An empty intersection is a handled no-op and leaves destination pixels and backing generations unchanged. Complex clips, fractional or rotated geometry, alpha, filters, incompatible formats, overlap, and unavailable raster access fall through without broadening the copied area.

## Diagnostics

P6 records copy-plan attempt, handled, and fallback counts plus physical direct-copy hits through the existing IMAGE diagnostics domain. The counters do not select or alter a fast path. No public diagnostics domain, metric key, runtime toggle, or optimization mask was added.

## Compatibility

- The public `Graphics` API is unchanged, and `ImageDrawingBridge` retains its existing two methods.
- No second materialized-raster cache was added; P3 cache capacity, admission frequency, and variant ordering are unchanged. Invalid-slot cleanup preserves a pending admission for a different scale/generation.
- JavaSE behavior remains on its established fallback path.
- Q/P8 explicit async preparation and UI adoption remain unchanged; P6 does not introduce automatic preparation.
- Scheduler cadence, event-loop timing, framebuffer reuse, and dirty-strip algorithms were not changed.
- Artifact and compile-surface tests passed for the internal bridge boundary.

## Validation

- The full SDK test suite passed with diagnostics disabled and enabled on the rebased tree. `artifactContentTest` and `dist -x test` also passed.
- The requested focused image, storage, lazy-JPEG, async-preparation, scroll-preparation, diagnostics, and artifact-boundary tests passed with diagnostics disabled and enabled. `compileSmokeTestJava` passed.
- `ImageDestinationScaleTest` covers cached scale A, pending scale B, invalidation of A, an intervening probe of A, and admission/reuse of B.
- `GraphicsDeferredImageTest` verifies current-frame selection and destination content-scale parity for deferred `copyRect`. P6 smoke verifies cache-hit ordering, plan-copy behavior without materialization, and smooth physical-identity eligibility.
- Native surface coverage includes RGB 565 parity and fallback cases for fractional placement, rotation, skew, incompatible formats, overlap, plus one-shot write failure. The injected failure reports no direct-copy hit, reaches P3 identity drawing, and matches fallback pixels.
- Focused IMAGE diagnostics tests and the full SDK diagnostics suite passed. The diagnostics-enabled P6 smoke recorded four expected IMAGE events for one clipped direct copy.
- macOS ARM64 `tcvm`, `Launcher`, and surface-test builds passed with diagnostics disabled and enabled. The native surface tests, P2 Raster Core, P3 geometry, P6 correctness, and warm-path smokes passed; the diagnostics-enabled P6 smoke also passed.
- The macOS ARM64 P4 Compact Storage smoke passed with compact RGB565 direct-copy parity and source/root identity, generation, opacity, and format preserved; GRAY8 and ARGB4444 fallback parity; P3 target-color fallback/materialization/hit ordering; and cached-final reuse. The P4 STANDARD Storage smoke passed with JPEG/PNG/alpha RGBA8888 parity and zero compact decodes/bytes.
- The P3 native materialization, P5 lazy JPEG, P6 fast-path and warm-path, and Q/P8 async-preparation macOS smokes passed. The async smoke exited with SIGBUS once in a combined multi-smoke invocation, then passed when rerun alone with all required markers.
- `git diff --check` and focused copyright-header validation passed on the final change set.
- The optional 663-image corpus workload was unavailable because `TC_IMAGE_CORPUS` is unset.
- This rebase was validated on `origin/master` containing Q/P8 and S/P4. A fresh Merge Flow is required for the pushed head; the earlier PR #484 run is superseded and does not satisfy that check.
- Non-macOS local builds were not run; the PR Merge Flow covers enabled platforms.

## Performance evidence

The repository smoke used independent Images for three lanes and compared full-visible and partial-clip cases. Each reported observation covers 1,200 copies; five observations were collected without timing thresholds.

| Clip | Lane | Pixel hash | Draw-plan hits | Identity attempts / hits | Direct-copy hits | Materializations / decodes | Generic / smooth draws | Median ms |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Full | Materialized `copyRect` | 824464513 | 0 | 0 / 0 | 0 | 0 / 0 | 0 / 0 | 3 |
| Full | Deferred `drawImage` | 824464513 | 6,000 | 6,000 / 6,000 | 0 | 0 / 0 | 0 / 0 | 3 |
| Full | Deferred `copyRect` | 824464513 | 6,000 | 6,000 / 0 | 6,000 | 0 / 0 | 0 / 0 | 3 |
| Partial | Materialized `copyRect` | 127775649 | 0 | 0 / 0 | 0 | 0 / 0 | 0 / 0 | 2 |
| Partial | Deferred `drawImage` | 127775649 | 6,000 | 6,000 / 6,000 | 0 | 0 / 0 | 0 / 0 | 3 |
| Partial | Deferred `copyRect` | 127775649 | 6,000 | 6,000 / 0 | 6,000 | 0 / 0 | 0 / 0 | 3 |

The hashes matched across lanes within each clip case. Millisecond-scale observations are noisy smoke measurements, not a performance threshold or broad speed claim.

## Known limitations

Direct writes require an opaque source, exact integer physical identity, a rectangular clip, compatible software-raster formats, and non-overlapping storage. Other pixel representations use the existing fallback path; P6 adds no independent compact conversion. The tested smooth case uses output content scale 2; unit-scale smooth plans retain pixel-center sampling and fall back.

## Deferred work

663-image workload: not available in this environment

Cross-platform SDK, Windows, Linux, Android, and iOS validation is left to the PR's Merge Flow.
