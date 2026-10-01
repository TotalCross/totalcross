<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image Scroll Raster Fast Path

This plan follows `.agent/PLANS.md` and `AGENTS.md`.

## Purpose / Big Picture

Let `Graphics.copyRect(Image, ...)` reuse an existing final raster, copy from a deferred draw plan, or use a direct software-raster copy when P3 proves exact physical identity. The observable result is pixel parity with the established copy path while avoidable image materialization and generic geometry work are skipped on eligible warm paths.

## Current Architecture and Scope

- `Graphics.copyRect(GfxSurface, ...)` uses native surface copying. The Image-source overload can preserve deferred work by requesting a draw plan and passing explicit source and destination rectangles to native code.
- `ImagePipeline` owns one materialized fallback slot with one pending admission and two independent draw-plan cache entries. P6 reuses these structures without increasing their capacity or changing admission frequency.
- P3 checks exact physical identity before target-color conversion and physical variants, then falls back to generic geometry. P6 direct copying runs inside that identity stage and shares its mapping proof.
- A narrow internal `ImageDrawingFeatureBridge` exposes the cached-final probe without adding a method to the two-method `ImageDrawingBridge`. It is excluded from application-facing SDK artifacts.

## Copy Resolution

For an Image source, preserve argument, frame, transform, and clip semantics and resolve in this order:

1. Reuse a valid cached final raster accepted for the exact destination scale and source decode generation.
2. On a cache miss, attempt native plan-aware rectangle copying without materializing the Image.
3. If the plan path cannot handle the operation, use the existing resolve/materialize path and native surface copy.

A cache miss must not materialize a raster or alter admission. The cached raster is valid only for its exact scale key, current source/pipeline generation, matching decode generation, and a valid backing. Removing an invalid cached raster clears only that slot; it preserves a different pending scale/generation observation. Mutations and pipeline barriers clear both cached and pending admission data. Draw-plan caches remain independent.

## Physical Identity Copy

Use P3's shared proof for stable backing and matching generations, valid source/frame bounds, exact integer mapping, axis-aligned positive transforms, equal physical source and destination extents, and enabled `physicalIdentity` policy. Trim source and destination rectangles to the actual rectangular clip before the proof. A fully clipped operation is a handled no-op and does not mutate the destination.

The direct write additionally requires effective alpha 255, no shader-dependent filter or fill, matching source and target color type, alpha type, and color space, readable source pixels, writable software-raster pixels, supported RGBA/BGRA 8888 or RGB 565 representation, and non-overlapping storage. A direct path reports success only when the bool-returning pixel-write operation succeeds. A failed write reports no direct-copy hit and continues through P3 identity drawing, variants, and generic rendering; the result must match that fallback.

Smooth operations qualify only when the final physical mapping is 1:1 and pixel-center sampling leaves no resampling. Unit output scale retains the smooth sampling path; the tested scale-2 case can copy directly.

## Compatibility

- Keep public drawing APIs, runtime policy, optimization masks, and toggles unchanged. Do not add another materialized-raster cache.
- Preserve P3's exact-identity, target-color, physical-variant, generic-rendering order.
- Preserve JavaSE copy semantics, P5 request-owned lazy JPEG decoding, current-frame selection, content scale, rotation/skew/fractional/format/overlap fallbacks, and RGB 565 copies.
- Keep P11's `SCHEDULING` diagnostics domain and ordering unchanged. Add only IMAGE-domain counters needed to observe P6; counters do not select a path.
- Do not change scheduling cadence, framebuffer reuse, dirty-strip painting, display/vsync, or P7 behavior.
- P4 compact storage remains authoritative. Same-format compact RGB565 can use P6 direct copy; GRAY8 and ARGB4444 retain the P3/Skia fallback. P6 adds no compact conversion or source promotion.
- Preserve Q/P8 explicit async preparation and adoption semantics; P6 does not trigger preparation automatically.

## Implementation Outline

1. Make the Image-source `copyRect` overload probe the current cached final raster and attempt plan-aware native copy before the established materialization fallback.
2. Reuse the exact existing one-slot materialized cache, preserving pending admission when only an invalid cached backing is removed.
3. Share P3's physical-identity mapping proof with a bounded direct software-raster copy, including clipping and fallback when pixel writing fails.
4. Verify semantic parity, cache admission, native fallbacks, diagnostics, artifact boundaries, warm-path behavior, and the enabled platform build matrix.

## Validation and Acceptance

- Focused tests cover cached-final resolution order, exact scale/decode-generation validity, pending-B admission after invalid cached-A removal, and the two independent draw-plan cache entries.
- `Graphics.copyRect` tests cover source/destination rectangles, translation, partial and empty clipping, current frames, content scale, JavaSE behavior, and unchanged fallback semantics.
- Native tests cover direct hits, failed pixel writes with P3 fallback parity and no direct-hit status, RGB 565, smooth sampling boundaries, alpha/filter/policy guards, fractional placement, rotation, skew, format mismatch, overlap, clipping, and no-op mutation behavior.
- Run focused and full SDK tests with diagnostics disabled and enabled, `artifactContentTest`, `compileSmokeTestJava`, and `dist -x test`. Build and run the macOS ARM64 native surface, P2 Raster Core, P3 geometry/materialization, P4 Compact and STANDARD Storage, P5 lazy JPEG, P6 fast/warm path, and Q/P8 async preparation smokes.
- The PR Merge Flow must pass every enabled SDK, native, Android, and iOS job. A workflow-disabled job may be skipped.
- Run the optional 663-image workload only when its established corpus is available and contains exactly 663 images; otherwise report it as unavailable.

## Delivered Result

`copyRect(Image, ...)` now follows the cache → plan-aware native copy → existing materialization chain. Direct copy uses P3's proof, writes only the visible clipped rectangle, and falls back when the write operation reports failure. Cache invalidation preserves an unrelated pending admission. Validation on master containing Q/P8 and S/P4 confirms compact RGB565 direct-copy parity and source identity/generation/opacity preservation; GRAY8 and ARGB4444 preserve their compact backing through fallback; target-color materialization/reuse stays in P3 order; and cached-final reuse preserves the compact authoritative source. The public API, P3 variant ordering, P5 lazy JPEG path, Q/P8 explicit preparation, P11 scheduling diagnostics, and framebuffer behavior remain unchanged.
