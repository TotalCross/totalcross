<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image Raster Variants — Implementation Report

## Summary

P3 adds exact physical-identity drawing and opt-in derived raster variants for
TotalCross images. Identity can draw directly when the physical pixels and
destination mapping are equivalent. Target-color and physical-canvas variants
reuse an exact native raster representation after the same request appears a
second time.

Physical identity is enabled by default. Target-color conversion and physical
variant caching remain disabled by default.

## Final architecture

Each native backing record owns at most one materialized raster variant and one
pending exact key. The materialized image is derived and disposable; it never
replaces the backing used as the Image's authoritative pixels. The slot is
shared by `TARGET_COLOR` and `PHYSICAL` kinds, so neither path creates a second
bitmap cache.

The Java fallback retains one exact-scale materialized entry with second-use
admission. Its independent two-entry draw-plan cache remains because it stores
plan metadata, not additional materialized image variants.

No public application API, public configuration mask, or public metric IDs
were added. Internal smoke fixtures can select the policies for validation.

## Physical identity

The identity path runs before variant lookup. It requires a stable native
backing with matching source and backing mutation generations; geometry with no
fill or smooth resampling; a positive axis-aligned transform with integer
translation; and an axis-aligned destination matrix with matching scale. The
source rectangle must be in bounds and physically aligned, and the mapped
destination edges must be integral and have the same physical dimensions.

Eligible draws use the source image directly and avoid geometry
materialization. Any unsupported mapping continues through the established
draw path.

## Exact variant key

The native `RasterVariantKey` contains the variant kind and exact serialized
fields for:

- source decode, source mutation, backing mutation, and native backing
  generations;
- frame, operation count, every operation, all operation parameters, and all
  output dimensions;
- root and output pixel/logical dimensions, frame counts, and frame widths;
- alpha masks, transparent color, and source opacity state;
- exact bit patterns for content, destination, output, and hardware scales;
- source and destination rectangle coordinates;
- target canvas dimensions, color type, alpha type, color-space identity, and
  full matrix;
- native source dimensions.

All serialized words compare exactly. A different kind or any changed field is
a different key.

## One-slot cache and second-use admission

The first observation of an exact key records it as pending and draws through
the normal fallback. A different key replaces the pending candidate. The
second consecutive observation of the same key may materialize one variant;
later exact matches hit it. A materialization failure clears the pending
candidate and falls back. A hit for another kind cannot use the stored image.

The former Java two-slot, first-use materialized variant cache was removed.
The two-entry draw-plan cache remains separate because it stores no raster
pixels.

## Resolution priority

Each draw resolves in this order: physical identity, then a target-color
variant when applicable, then a physical variant only when target-color did not
claim the request, and finally the generic fallback. Target-color and physical
variants share one pending key and one slot. Trying both kinds for a single
draw could make them replace each other's pending key or slot, causing
alternating admission and preventing either candidate from converging. This
fixed priority is the intended behavior of the shared-slot design.

## Target-color conversion

Conversion supports known RGBA8888 and BGRA8888 targets when the source backing
is stable, alpha metadata is known, and source and destination color spaces are
the same. Conversion produces a derived source image in the destination color
type; the original backing format is unchanged.

RGB565 is eligible only for proven-opaque input, full alpha masks, and
geometry-only plans. Alpha-bearing or unknown-opacity sources, unsupported
color types, incompatible color spaces, unstable backings, and conversion or
allocation failures use the existing draw fallback. Gray and ARGB4444 paths
were not added.

## Physical variants

The physical path runs after identity rejection. It requires a stable native
source, a non-recording raster canvas no larger than 4,194,304 pixels, an
RGBA8888 or BGRA8888 target with alpha, and a finite positive axis-aligned
matrix with integral translation and mapped destination bounds. It builds a
full-canvas derived image with the existing Skia geometry path, then replays
that image through the current destination canvas state.

The first exact request falls back, the second can materialize the physical
variant, and later exact requests replay it. Different geometry competes for
the same pending key and slot.

## Mutation and lifetime rules

Image backing mutation clears both the materialized slot and pending key.
Backing replacement and release discard their native backing records and their
variants. Rejected or failed candidates do not change authoritative pixels or
format. The smoke verifies invalidation through the same backing-mutation
bridge used by SDK mutation paths.

## Diagnostics

Internal IMAGE counters cover identity, target-color, and physical-variant
attempts, hits, materializations, and fallbacks. They are collected only when
the IMAGE diagnostics group is enabled. The default diagnostics-off SDK build
does not retain the counter storage.

## Compatibility

The application-facing Image API and its authoritative storage format are
unchanged. All P3 variant policies remain configurable only through existing
internal runtime policy plumbing and the native implementation flags used by
draw plans. No new public configuration mask was added.

Physical identity defaults to enabled; target-color conversion and physical
variant caching default to disabled. P4 remains responsible for compact
authoritative storage; these derived variants do not change the Image's source
format.

## Validation

- 22 focused SDK test classes passed with diagnostics disabled, covering runtime
  policy, encoded sources, backing contracts, deferred transforms, draw plans,
  and converter/native ABI.
- Seven focused SDK test classes passed with
  `-PruntimeDiagnostics=true`.
- `compileSmokeTestJava` and `artifactContentTest` passed.
- `dist -x test` passed with diagnostics disabled.
- macOS ARM64 Release `tcvm` and `Launcher` builds passed.
- The macOS ARM64 raster smoke passed identity, exact-key admission, target
  color conversion, RGB565 opacity checks, fallback parity, mutation
  invalidation, physical variants, runtime configuration, and diagnostics.
- The scoped copyright validator passed for 16 applicable files without
  changing headers. `git diff --check origin/master...HEAD` passed.
- GitHub Merge Flow passed SDK, macOS ARM64, Windows, legacy Windows, enabled
  Linux variants, Android, and iOS. The workflow-disabled Linux ARM32 cross job
  was skipped.

## Performance evidence

The identity smoke recorded one direct physical-identity hit and zero geometry
materializations. In the physical-variant smoke, 1,000 repeated exact hits took
5 ms on macOS ARM64. Across the initial request sequence and a second key, the
smoke recorded 1,001 hits and two materializations. This focused measurement
does not predict application frame rates or unmeasured workloads.

## Known limitations

- Target-color conversion is conservative about color-space identity and
  supported Skia color types; other cases fall back.
- RGB565 requires proven opaque source pixels, full alpha masks, and
  geometry-only operations.
- Physical variants are limited to supported raster surfaces and bounded
  axis-aligned destination mappings; larger canvases and other surface types
  fall back.
- Both variant policies remain disabled by default. No broad workload benchmark
  was run because the focused smoke is the workload that exercises these paths.

## Deferred work

P4 compact authoritative image storage remains separate. Additional destination
formats or surface types should be considered only with explicit pixel-parity
and fallback coverage. Broader performance evaluation is deferred until a
representative workload exercises these exact variant paths.
