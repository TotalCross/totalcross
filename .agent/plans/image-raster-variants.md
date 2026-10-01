<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Image Raster Variants

## Context

Image drawing can repeatedly materialize the same raster even when a stable
native backing already has the required physical pixels or when an exact
derived representation is reused. P3 introduces direct physical-identity
drawing and a bounded cache for exact derived rasters. These paths must retain
the established rendering path whenever geometry, color, alpha, or backing
state cannot be proven equivalent.

P2 provides one authoritative `ImageBacking`, a monotonic mutation generation,
an independent encoded-source decode generation, conservative opacity, and
`backingIdentityStableForCaching()`. Raster `getPixels()` remains a live array;
once it escapes, that backing is permanently ineligible for derived caching.
Native readback remains detached. Runtime policy already has the P3 fields and
IMAGE aggregate diagnostics use internal bridges.

`ImageDrawPlan` carries the source pipeline, operation and parameter arrays,
frame and presentation state, scales, alpha and transparent-color state, and
decode generation. The native Skia path imports this plan into
`SkiaImageDrawPlanData`; geometry compilation and materialization live under
`TotalCrossVM/src/nm/ui/skia/`. The source backing registry is the owner for
native-only derived variant state. `ImagePipeline` has an independent
two-entry draw-plan cache and a legacy two-entry first-use materialized-image
cache; P3 removes the latter policy.

## Objectives

- Draw directly from authoritative pixels when physical source and destination
  mapping are exactly equivalent.
- Reuse at most one exact derived raster and one pending exact key per native
  backing, admitting a candidate on its second consecutive observation.
- Support opt-in target-color conversion and physical-canvas variants while
  preserving authoritative pixels and format.
- Keep uncertain, unsupported, or failed cases on the established fallback.
- Add focused diagnostics and coverage without adding public application API,
  public configuration, or public metric identifiers.

## Scope

In scope: physical identity; shared `TARGET_COLOR` and `PHYSICAL` slot and
pending-key state; target-color conversion behind its disabled default;
physical variants behind their disabled default; invalidation on backing
mutation, replacement, and release; focused aggregate diagnostics, tests,
smoke coverage, and measured evidence.

Out of scope: compact authoritative formats, byte-budget or global caches,
memory-pressure eviction, GPU-only backing, mmap, lazy JPEG factories, scroll
fast path or reuse, asynchronous preparation or prefetch, worker scheduling,
PNG prefetch, and frame pacing. P3 does not expose implementation details or
metric IDs as application SDK API.

## Architecture

### Physical identity

Consume the typed `physicalIdentity` policy before either variant lookup. Use
the authoritative native raster only when storage is stable and unescaped,
backing and decode generations match, the frame and source region are valid,
the geometry is an invertible diagonal transform, and source and destination
coordinates and dimensions map to the same integer physical pixels. Require
compatible color, alpha, transparency, presentation, clipping, and sampling
semantics. Reject fractional mapping, resampling, unsupported transforms, and
any uncertain compatibility; existing geometry drawing remains authoritative
for rejected cases.

### Shared exact variant state

The native backing registry owns one materialized slot and one pending
candidate. The slot records its kind, exact typed key, immutable native image,
and proven opacity when required. The key includes every source, plan,
geometry, color, alpha, scale, and destination field that can change output,
including exact floating-point bit patterns where those values are part of
identity.

The first eligible request records its exact key and uses the fallback. A
different key replaces the pending candidate. A second consecutive observation
of the same key may materialize and replace the slot; later exact matches hit.
A transient materialization failure clears pending and falls back. The slot and
pending candidate are shared by `TARGET_COLOR` and `PHYSICAL` so a single draw
cannot cause the two kinds to displace each other's admission state. Backing
mutation, replacement, and release clear both. Pure reads do not.

Resolution order is physical identity, then a target-color variant when that
conversion applies, then a physical variant only when target-color did not
claim the request, and finally the established generic fallback. Target-color
and physical variants share one pending key and slot; trying both kinds for a
single request could alternate admission and prevent either exact candidate
from converging.

Keep the independent draw-plan cache. Remove the legacy Java two-slot,
first-use materialized-image policy. If Java retains a separate materialized
representation, it must use one exact entry, second-use admission, and must not
duplicate a derived raster owned by the native backing.

### Target-color conversion

Consume `targetColorConversion` using the shared slot. Support known RGBA8888
and BGRA8888 targets only when the source is stable, alpha metadata is known,
and color-space requirements match. RGB565 is eligible only when explicitly
required by the destination and source opacity is proven. Never discard alpha.
Unknown or unsupported targets and allocation or conversion failures fall back
without changing authoritative storage. Do not add GRAY8 or ARGB4444 behavior.

### Physical variants

Consume `physicalVariantCache` after identity rejection and only when a
target-color variant did not claim the request. Admit stable native sources
with exactly representable geometry, then materialize bounded RGBA8888 or
BGRA8888 canvas output through existing Skia geometry infrastructure. The
variant remains derived and disposable; it never becomes the authoritative
`Image` backing.

## Compatibility constraints

- Preserve public `Image` and `Graphics` APIs, public configuration masks, and
  application-visible defaults.
- Keep `physicalIdentity=true`,
  `targetColorConversion=false`, and `physicalVariantCache=false`.
- Preserve authoritative image storage format and use the established draw
  path whenever a proof or conversion is unavailable.
- Keep ABI-sensitive Java field ordering stable where possible. If fields are
  necessary, update native field mapping and ABI coverage in the same change.
- When diagnostics are disabled, add no diagnostic allocation,
  synchronization, or clock work.

## Milestone 1 — physical identity

Add exact physical identity analysis before variant lookup. Require matching
backing and decode generations, stable unescaped storage, valid frame and
region, exact integer physical source and destination bounds, one-to-one pixel
mapping, and compatible presentation, alpha, transparency, transform, and
clipping semantics. Normalize geometry to integers and compare exact scale
bits wherever scale remains part of identity. Every rejected draw uses the
existing geometry path.

Add internal attempt, hit, and fallback diagnostics. Cover exact hits,
logical/physical scale equivalence, fractional and size/crop mismatches,
presentation and transform mismatches, stale generations, escaped storage,
disabled policy, clipping parity, and avoided resampling or materialization.
Acceptance: uncertain cases always fall back and pixel output matches the
established path.

## Milestone 2 — shared one-slot state and exact key

Add one native-owned materialized slot and one pending candidate to the
authoritative backing. Give both explicit valid/empty state. Include source
decode and mutation generations, backing identity, frame and source region,
destination geometry, output dimensions, color and alpha types, geometry
signature, alpha and transparent-color state, exact scale bits, and every
draw-plan operation, parameter, and dimension affecting pixels in the key.

Use second-consecutive-observation admission. A different key replaces
pending; materialization replaces the slot and clears pending; transient
failure clears pending and falls back. Invalidate both on mutation,
replacement, and release. Remove the legacy two-slot first-use materialized
cache while preserving the independent draw-plan cache.

Cover K/K admission, K/J/K replacement, cross-kind contention, one-slot
bounds, mutation and release invalidation, transient failure, unsupported
mapping, and one-field key mismatches. Acceptance: at most one retained and
one pending candidate exist per backing, with no duplicate owner for a
derived image.

## Milestone 3 — target-color variants

Add target-color conversion using the shared slot and second-use admission.
Permit supported RGBA8888 and BGRA8888 targets; permit RGB565 only for proven
opaque input when required by the destination. Reject unknown or unsupported
color types, alpha-risk cases, incompatible color spaces, unstable backings,
and failed allocation or conversion. Preserve the authoritative source
format and fall back for every rejection.

Cover opt-in observe/materialize/hit behavior, color-type key separation,
opaque RGB565, alpha and unknown-opacity rejection, unsupported targets,
failure fallback, unchanged authoritative format, mutation invalidation, and
pixel parity.

## Milestone 4 — physical variants

After identity rejection and target-color resolution, add physical variants for
stable sources and exactly representable native geometry. Bound canvas size and
supported raster/color types. Materialize via existing Skia geometry
infrastructure and replay through current destination canvas state. The first
request falls back, the second exact request may materialize, and subsequent
matches hit. A different key first replaces the pending candidate and
competes for the shared slot only after a second observation.

Cover first-use fallback, second-use materialization, later hits, key
replacement, priority relative to identity and target-color conversion,
mutation/release invalidation, pixel parity, and a focused affected-path
measurement. Do not claim frame-rate impact from a focused measurement.

## Validation

At each milestone, use the smallest relevant validation. Cover P1 policy,
P2 backing and decode generations, deferred image pipeline, draw plans,
artifact boundaries, diagnostics off/on, converter/native ABI, and pixel
parity. Native draw or backing-state changes require macOS ARM64 build and
rendering smoke coverage. Run `artifactContentTest`, the diagnostics-off
distribution build, focused diagnostics-on SDK suites, scoped copyright
validation, and `git diff --check` at final closure. Cross-platform CI is the
release gate; do not locally build unaffected Android, Windows, Linux, WinCE,
or iOS targets.

Performance evidence must exercise the changed path. Use the focused smoke to
measure identity materialization avoidance and exact physical-variant hits;
do not rerun the broad image benchmark unless its workloads exercise these
paths or a milestone decision requires it.

## Risks and tradeoffs

- Confirm backing-registry ownership and its mutation and release hooks before
  choosing the native slot representation.
- Skia surfaces vary in supported color types and alpha representation; reject
  cases that cannot be proven safe.
- Clipping and source-rectangle equivalence must match the compiled draw plan
  and geometry path exactly.
- A JavaSE materialized representation, if retained, must not duplicate native
  derived images and must obey the same exact-key and admission rules.
- A shared slot limits memory but makes target-color and physical variants
  contend; fixed target-color priority for applicable requests avoids
  alternating admission.
