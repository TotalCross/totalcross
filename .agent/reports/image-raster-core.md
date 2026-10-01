<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Image Raster Core — Implementation Report

## Summary

Image Raster Core establishes one authoritative mutable backing per materialized `Image` and adds conservative decode, read, write, color, and draw paths. Backing ownership, mutation generations, opacity state, and aggregate image diagnostics make eligible operations bounded while preserving established output and fallback behavior.

## Final architecture

`Image` owns dimensions, frame selection, deferred operations, and one `ImageBacking`. `RasterImageBacking` owns Java ARGB storage; `NativeImageBacking` owns and releases its native Skia handle. `EncodedImageSource` owns immutable encoded bytes and an optional decoded backing cache. Decode-cache generation remains independent from the backing mutation generation associated with each `Image`.

Replacement validates a staged candidate before publishing it. Failure preserves the prior valid backing. Successful replacement releases the displaced native handle exactly once, and release remains idempotent.

The feature consumes `zeroCopyDecode`, `opacityMetadata`, `opaqueWritePixels`, `rowReadback`, and `directColorMaterialization`. `physicalIdentity` remains deferred; no compact format, target-color variant cache, or scroll optimization was added.

## Backing ownership and generations

Ordinary backing reads do not advance mutation generation. Successful backing replacement and visible mutations advance the owning Image generation and synchronize backing generation monotonically. Encoded-source cache installation and eviction retain their separate decode generation. Snapshots and independent replacement backings begin unescaped.

Native backing field offsets include the inherited generation and escape state so native dimensions remain aligned with Java backing fields. Native resource release is idempotent, and replacement releases a displaced handle once.

## Mutable pixel exposure

Raster `Image.getPixels()` continues to return the live Java array. If its backing is shared with an encoded source, the raster backing is detached before exposure. First exposure marks the backing's mutable storage escaped, advances its Image generation once, sets opacity to `UNKNOWN`, and invalidates cache stability. Repeated exposures do not advance generation again. Later raw array writes cannot be observed, so escaped storage remains ineligible for future identity or cache reuse.

Native `getPixels()` returns a detached Java copy and is a pure read. The shared native backing remains attached; its generation, opacity, escape state, and cache stability do not change. Mutating the returned array does not change native backing pixels.

## Opacity model

Opacity is `OPAQUE`, `HAS_ALPHA`, or `UNKNOWN`. Only proven `OPAQUE` enables opaque-only assumptions. Proven JPEG and non-alpha PNG decodes start opaque. Alpha-bearing writes mark alpha; ambiguous mutations invalidate the proof. A native no-alpha write promotes opacity only when it successfully covers the complete, unclipped surface of a single-frame image. Partial writes preserve a prior proof only when justified. When opacity metadata is disabled, state remains unknown.

## Direct decode

With `zeroCopyDecode` enabled, direct decode is limited to full-resolution, single-frame PNG and JPEG sources. JavaSE decodes through ImageIO into the final ARGB destination raster. The deployed Skia PNG/JPEG path decodes into a candidate native backing and publishes it only after validation. Unsupported formats and transient allocation failures use the established decoder; deterministic corruption retains the existing cached-failure behavior.

Direct native PNG/JPEG backing state, allocation, row writes, and cleanup are guarded by `#if TC_RENDERER_SKIA`. Non-Skia builds retain the legacy pixel-array decoder path.

## Bounded read/write paths

Bounded row and rectangle reads validate dimensions, coordinates, output offsets, stride, frame offsets, and overflow. They copy visible pixels without row padding and preserve alpha. Unavailable or failed bounded readback falls back to the established storage path.

`Graphics.setRGB` retains its public signature and observable clipping, frame, input, and exception behavior. JavaSE copies directly into raster backing only for proven opaque input and eligible bounds. The native Skia path uses `writePixels` for eligible opaque rectangles; alpha-bearing and ambiguous input use the conversion path. Mutation state advances only after successful writes, and opacity becomes `OPAQUE` only when whole-image coverage is proven.

## APPLY_COLOR2

When `directColorMaterialization` is enabled, `APPLY_COLOR2` runs against authoritative raster storage only where alpha and color behavior match the established path exactly, including frame storage. Unsupported cases retain eager or canonical materialization. Opacity is preserved only when the resulting alpha is proven; otherwise it becomes `UNKNOWN`.

## Draw-plan integration

Native draw plans are consumed directly only for color-only plans that need no resampling, target-color conversion, or transformed variant. A non-fusable multi-frame geometry pipeline snapshots its root and uses established eager per-step operations to preserve frame and scale semantics. Unsupported draw plans continue through normal materialization.

## Diagnostics

`RuntimeDiagnosticSnapshot.Domain.IMAGE` reports aggregate direct-decode success and fallback, opaque-write success, bounded-read success, direct color materialization, and raster fallback/error counters. `RuntimeDiagnosticsFeatureBridge` provides generic internal domain operations; `ImageRasterFeatureBridge` contains raster feature calls. Both bridges remain in `totalcross-runtime-java` and are excluded from application-facing API, aggregate SDK, and distributed SDK artifacts. `ImageDrawingBridge` retains its public surface. Disabled diagnostics do not allocate or record image counters, and diagnostics do not affect pixels or select runtime policy.

## Compatibility

Public `Image` and `Graphics` method signatures remain unchanged. Raster `getPixels()` remains live-array access; native `getPixels()` remains detached readback. `Graphics.setRGB` preserves observable behavior, including clipping and exceptions. JavaSE semantics remain covered, and unsupported operations use established fallback paths. Internal feature bridges are not exposed in application SDK artifacts.

## Validation

- Focused SDK tests passed with diagnostics disabled and enabled for backing ownership, raster operations, encoded sources, decoder guards, native backing conversion, diagnostics, and runtime metric conversion.
- `artifactContentTest` and `dist -x test` passed with diagnostics disabled and enabled.
- macOS ARM64 Release `tcvm` and `Launcher` builds passed with diagnostics disabled and enabled. Raster decode/write/color/readback, encoded-source behavior, deferred geometry, color mutation, native ABI, native materialization, and runtime diagnostics smokes passed.
- GitHub Merge Flow passed the enabled SDK, macOS ARM64, Windows, `windows-native-legacy`, Linux, Android, and iOS jobs. The workflow-disabled `linux-arm32v7-cross` job remained skipped.
- Focused copyright validation and `git diff --check` passed.

## Known limitations

Direct decode is limited to full-resolution, single-frame PNG/JPEG cases. Opacity remains conservative after mutations that cannot prove the alpha result. Later raw writes through an escaped raster array are inherently unobservable. Native runtime smoke coverage exercises macOS ARM64; other supported platforms are covered by the enabled build matrix.

## Deferred work

Physical-identity optimization and target-color variant caching remain deferred. Compact RGB565/GRAY8/ARGB4444 storage, lazy JPEG factory behavior, scroll acceleration and reuse, asynchronous preparation and prefetch, worker scheduling, and pacing work are outside this feature.
