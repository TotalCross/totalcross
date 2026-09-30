<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Image Raster Core

## Context

Image operations cross Java raster arrays, native Skia backing, `EncodedImageSource` decode caches, and deferred `ImagePipeline` materialization. These layers can cause redundant full-raster copies and readbacks, make it unclear which storage is authoritative and mutable, and make opacity difficult to prove. Image backing changes also need a reliable mutation generation. Without stable ownership and mutation state, later physical-identity and variant caching cannot safely distinguish reusable storage from exposed or changed pixels. Meanwhile, bounded reads, opaque writes, color operations, and simple draw plans can require expensive full-image materialization even when their input and output regions are known.

## Objectives

- Establish one authoritative backing for each materialized `Image`.
- Keep `EncodedImageSource` decode generation independent from each `Image` backing mutation generation.
- Track opacity conservatively and use opaque-only assumptions only when proven.
- Decode directly into the final backing where format, dimensions, ownership, and frame semantics are exact.
- Support checked row and rectangle readback without copying row padding.
- Support bounded writes when input opacity, clipping, and destination bounds are proven.
- Apply `APPLY_COLOR2` directly to authoritative storage when its output matches established behavior exactly.
- Consume trivial draw plans without unnecessary intermediate images.
- Provide aggregate `IMAGE` diagnostics whose disabled path performs no counter recording or diagnostic-only work.
- Preserve public `Image` and `Graphics` behavior, exceptions, and fallback semantics.

## Scope

This work consumes exactly these `ImageRuntimePolicy.RasterCorePolicy` fields:

- `zeroCopyDecode`
- `opacityMetadata`
- `opaqueWritePixels`
- `rowReadback`
- `directColorMaterialization`

`physicalIdentity` remains unused by this feature. Physical-identity checks and variant caching require later work built on the ownership and escape guarantees established here.

## Architecture

`Image` owns dimensions, frame selection, deferred operations, and one authoritative `ImageBacking`. `RasterImageBacking` owns Java ARGB storage; `NativeImageBacking` owns a native Skia handle. `EncodedImageSource` owns immutable encoded input and may retain a decoded backing for reuse. Its decoded-cache generation is independent from the mutation generation associated with an `Image` backing.

Replacement stages a candidate and validates it before publication. A failed decode or replacement leaves the previous valid state usable. Successful replacement publishes the candidate and releases the displaced native handle exactly once. Backing release is idempotent.

Backing opacity has three states: `UNKNOWN`, `OPAQUE`, and `HAS_ALPHA`. Only proven `OPAQUE` may enable opaque-only assumptions. Mutations preserve opacity only when their result is proven; otherwise they set it to `UNKNOWN` or `HAS_ALPHA` as appropriate.

`Image.getPixels()` preserves its backing-specific contract. Raster readback returns the live Java array. When the raster backing is shared with the encoded-source cache, detach it before exposure; on first live-array exposure mark the current backing's mutable storage escaped, advance its mutation generation once, set opacity to `UNKNOWN`, and make it ineligible for later identity or cache reuse. Native readback returns a detached Java array and remains a pure read. These internal state guarantees prepare for later physical-identity work without adding public API.

The decode and draw pipelines stage state before publishing it. Fast paths run only when policy and semantic preconditions are satisfied; unsupported geometry, formats, and transient failures use established materialization or fallback paths.

## Compatibility constraints

- Keep all public `Image` and `Graphics` method signatures unchanged.
- Preserve live-array behavior for raster `Image.getPixels()` and detached-copy behavior for native readback.
- Preserve observable `Graphics.setRGB` behavior, including clipping, partial input handling, current-frame selection, and exceptions.
- Preserve JavaSE pixel, alpha, scale, and frame semantics.
- Preserve established exception classification and fallback behavior when an optimized path is unsupported or fails transiently.
- Keep internal diagnostics and raster feature bridges out of application-facing SDK artifacts.
- Do not approximate geometry, color conversion, or opacity when exact behavior cannot be proven.

## Milestone 1 — authoritative backing and decode

1. Make each materialized `Image` use one authoritative `ImageBacking`, while keeping encoded source bytes and their optional decoded cache immutable and separately versioned.
2. Stage, validate, and publish replacements atomically; retain the prior valid backing after failed candidates and release displaced native handles exactly once.
3. Track per-Image backing mutation generations and tri-state opacity across replacement, visible mutations, and direct pixel exposure. Ordinary reads do not advance generations.
4. Track escaped raster storage on the first live `getPixels()` exposure. Keep snapshots and independent replacements unescaped; keep native detached-copy readback a pure read.
5. Use `zeroCopyDecode` for full-resolution, single-frame PNG/JPEG cases whose destination layout and ownership are explicit. Distinguish deterministic corrupt input from transient allocation or native infrastructure failure; preserve established fallback behavior elsewhere.
6. Add focused tests for backing validity, replacement and release, mutation generations, opacity transitions, live-array exposure, direct decode, deterministic failure, and retry after transient failure.

## Milestone 2 — bounded raster operations and diagnostics

1. Add checked row and rectangle reads that validate coordinates, output bounds, physical dimensions, stride, frame offsets, and integer overflow. Copy visible pixels only, preserve alpha, and fall back when bounded readback is unavailable or fails.
2. Add the opaque bounded-write path at `Graphics.setRGB` only when source opacity, destination backing, clipping, current frame, and bounds are proven. Preserve input errors and use the established conversion path for alpha-bearing or ambiguous input. Advance mutation state only after successful writes.
3. Apply `APPLY_COLOR2` directly to authoritative raster storage only when color and alpha math exactly matches established eager and native behavior, including frame storage; retain the existing materialization path otherwise.
4. Consume only trivial draw plans that require no resampling, transformed variant, target-color conversion, or unsupported frame geometry. Route all other plans through established materialization.
5. Add aggregate `RuntimeDiagnosticSnapshot.Domain.IMAGE` counters for direct decode and fallback, opaque writes, bounded reads, direct color materialization, and raster fallback or error. Gate counters before diagnostic-only allocation or synchronization. Keep operations internal through `RuntimeDiagnosticsFeatureBridge` and `ImageRasterFeatureBridge`.
6. Enforce artifact boundaries for both internal bridges and preserve the public `ImageDrawingBridge` surface. Add JavaSE/native parity, clipping, alpha, frame, fallback, diagnostics-off/on, artifact, and converter/native ABI coverage.

## Validation

- Run focused SDK tests with diagnostics disabled and enabled for backing ownership, decode, bounded operations, drawing, and diagnostics behavior.
- Run `artifactContentTest` to check public API and internal bridge placement; run `dist -x test` with diagnostics disabled.
- Build macOS ARM64 Release `tcvm` and `Launcher`, then run the affected raster, decode, geometry, color, mutation, and native ABI smokes.
- Cover converter output and native field-layout/ABI assumptions in focused tests or smokes.
- Require GitHub Merge Flow to pass its enabled SDK, macOS, Windows (including `windows-native-legacy`), Linux, Android, and iOS jobs. The workflow-disabled `linux-arm32v7-cross` job may be skipped.
- Run `git diff --check` on the final change set.

## Risks and tradeoffs

- Writes through a live raster array cannot be observed individually after exposure. The backing must remain escaped and ineligible for identity or cache reuse after the first exposure.
- Opacity is proof-based. Ambiguous alpha or mutations must leave it `UNKNOWN` rather than enable an unsafe opaque path.
- Direct decode is limited to formats and layouts with exact output dimensions, frame semantics, and ownership; other inputs retain the established decoder.
- Native and Java backing generations and field layouts must remain synchronized across the converter and runtime ABI.
- Geometry, clipping, frame selection, and partial-input behavior are easy to approximate incorrectly. Unproven cases must use the existing fallback.
- Fast paths trade copies for stronger state invariants, so replacement and transient failure handling must preserve the last valid backing.

## Out of scope

- Physical-identity optimization and target-color variant caching.
- Compact RGB565, GRAY8, or ARGB4444 storage.
- Lazy JPEG factory behavior.
- Scroll fast paths and scroll raster reuse.
- Asynchronous preparation or prefetch, worker scheduling, and pacing work.
