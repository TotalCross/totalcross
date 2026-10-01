<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Implement Compact Image Storage

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`.

## Purpose / Big Picture

Make the existing `ImageStorageProfile.COMPACT` runtime request reduce the
resident storage of eligible immutable images on runtimes that can provide
native Skia backings. Applications continue to select only `STANDARD` or
`COMPACT`. Standard images remain full precision. Compact images choose an
internal representation from encoded-source structure, expand transparently
for public pixel observers, and promote transactionally before an operation
needs a mutable full-precision backing.

The completed feature is visible through focused SDK tests, native backing
accounting, a durable macOS smoke app, and the runtime's resolved Image policy.
The observable contract includes actual resident bytes, public ARGB/RGBA
semantics, retry-safe decode and promotion, and unchanged P2/P3 lifecycle rules.

## Architecture and Scope

### Runtime policy

`ImageStorageProfile` is public configuration with `STANDARD` and `COMPACT`.
`ImageRuntimeConfigurationStartup` resolves deployment rules after runtime
backend selection and publishes `ImageRuntimePolicy`. Resolve COMPACT only when
the deployed native Skia runtime can create all compact formats. Resolve
JavaSE, simulator, and non-Skia runtimes to STANDARD with the reason `native
compact backing is unavailable`. Keep application APIs limited to the profile
enum. The internal startup capability bridge must stay out of `totalcross-api`,
the aggregate SDK, and distributed SDK artifacts. Resolve capability once;
decoders consume the effective policy instead of repeating platform checks.

### Backing ownership and formats

Java `ImageBacking` owns opacity and monotonically increasing mutation
generation. `NativeImageBacking` owns the native handle and delegates snapshots,
mutability, dimensions, and row readback. In
`TotalCrossVM/src/nm/ui/skia/skia_image_backing_internal.h`,
`NativeImageBackingRecord` owns a SkImage or SkSurface, dimensions, format,
rowBytes, resident backing byte count, generation, opacity-analysis cache, and
P3's exact one-slot/one-pending raster variant. Keep this metadata authoritative
for snapshots, mutations, reads, accounting, and format reporting.

Internal canonical formats are `RGBA8888`, `RGB565`, `GRAY8`, and `ARGB4444`.
Map them to Skia's RGBA8888, RGB565, Gray8, and ARGB4444 color types with
appropriate opaque or premultiplied alpha. Use Skia conversion and packing
primitives; keep packed byte layout owned by Skia. All new mutable backing,
render target, generic materialization, and P3 derived variant destinations stay
RGBA8888. Only immutable/source content may remain compact.

### Decode and observation

Extend the adaptive native JPEG decoder and libpng progressive callbacks rather
than adding public decoders. For supported compact sources, row conversion
writes into one final compact candidate, and decoder ownership transfers that
candidate to the Image only after successful completion. Decoder failure and
install failure release an unpublished candidate once and leave retry possible.

Selection is structural and does not inspect all decoded pixels. Under
effective COMPACT: structurally grayscale and opaque selects GRAY8; otherwise
source without alpha or tRNS selects RGB565; alpha/tRNS selects ARGB4444; an
unsafe or unsupported representation falls back to RGBA8888. JPEG component
metadata supplies grayscale/color and JPEG is opaque. PNG color type and tRNS
metadata determine eligibility. RGB/RGBA values that happen to be gray do not
change format, and transparent-capable sources never choose RGB565. Opacity
metadata success does not affect format choice.

`Image.getPixels`, bounded/row readback, encoders, equality/hash paths, and
drawing must observe the compact backing without changing its canonical format.
Readback/encoding should convert row by row with scratch bounded to about one
RGBA row. Native `getPixels()` continues to return detached Java data. Draw may
sample a compact SkImage directly; any P3 target-color conversion is a derived
variant and cannot replace authoritative compact storage.

### Mutation and P2/P3 contracts

Before a mutable or full-precision-only operation, expand into a replacement
RGBA8888 allocation. Preserve the compact backing until all rows convert
successfully. On commit, advance generation monotonically, derive/preserve
opacity state, invalidate P3 cached and pending variants, publish the
replacement, and release old storage exactly once. On allocation or conversion
failure, leave the old compact backing, generation, and observable pixels
intact; a later retry must be possible. Prefer updating the current handle's
record if Java ownership permits; otherwise use P2's transactional replacement
semantics. Immutable snapshots may retain compact format; shared source
backings must detach before mutation.

P3 physical identity, exact variant keying, target-color behavior, derived
RGBA/BGRA variants, one native variant slot, and mutation invalidation remain
unchanged. A compact source may legitimately fail physical identity for an RGBA
destination and use the normal draw fallback.

### Diagnostics and quality

Reuse `RuntimeDiagnosticSnapshot.Domain.IMAGE`; do not add public metric IDs or
domains. Gather test evidence for live/peak bytes by format, direct compact
decode and temporary RGBA bytes, compact readback and row-scratch peak,
promotion attempts/results/bytes, and current format. Diagnostics-disabled
production paths must not gain avoidable allocations or synchronization.

Durable deterministic fixtures cover opaque color JPEG/PNG, structurally gray
JPEG/PNG where supported, and translucent/transparent PNG. Compare RGB565 and
ARGB4444 to independent packing reference models; verify grayscale exactness,
channel order, alpha edges, hidden RGB under zero alpha, compositing quality,
and no repeated-observer quantization. Set quality thresholds before reviewing
the results.

## Implementation Sequence and Acceptance

### Milestone 1 — Capability and generic backing

Resolve COMPACT from requested profile and actual native compact capability.
Add the internal format enum and format-aware record creation, dimensions,
rowBytes, actual-byte accounting, snapshots, RGBA row expansion, and generic
test accessors. Keep source selection disabled until safe generic storage and
readback exist. Add policy, JavaSE fallback, native capability, and artifact
boundary tests.

Acceptance: STANDARD and mutable destinations remain RGBA8888; Skia capability
resolves COMPACT only on a backend that can create those representations;
JavaSE/simulator and non-Skia paths retain an explanatory downgrade; a generic
compact backing snapshots and expands correctly using actual rowBytes.

### Milestone 2 — Opaque compact source decode

Use JPEG component metadata and safe PNG color/tRNS structure to select GRAY8
before RGB565. Decode supported opaque inputs directly into the final compact
storage with one row conversion buffer at most. Preserve adaptive JPEG decode
tiers, P2 failure cleanup, and immutable source sharing.

Acceptance: structurally gray opaque JPEG/PNG uses exact GRAY8; other opaque
JPEG/PNG uses RGB565 and matches an independent 565 model; no full-size RGBA
staging occurs; reads, encodes, and draws leave the source compact. Unsupported
or unsafe decoder cases use RGBA8888.

### Milestone 3 — Alpha compact source decode

Select ARGB4444 for PNG with alpha or tRNS and implement decoder-row packing
using Skia-compatible premultiplied representation. Ensure tRNS is respected
structurally even if no decoded pixels are transparent.

Acceptance: only alpha/tRNS eligible sources select ARGB4444; expansion matches
the independent 4-bit premultiplied model within predeclared compositing error;
transparent RGB does not create halos; observers do not quantize again.

### Milestone 4 — Transactional promotion

Route mutable/full-precision operations through an atomic compact-to-RGBA8888
promotion. Cover both in-place record promotion and P2 backing replacement as
appropriate. Preserve opacity and monotonic generation, detach shared sources,
invalidate all P3 variant state, and release each owned allocation exactly
once.

Acceptance: the first mutation promotes once; later reads/mutations remain
RGBA8888; injected allocation/conversion failure preserves format, pixels,
generation, opacity and variants; retry succeeds; shared immutable siblings
remain compact; P3 draw and target-color fallback stay correct.

### Milestone 5 — Integration and current-branch evidence

Complete focused SDK tests and macOS native smoke apps covering all three
compact formats, expected byte use, readback quality, observers, draw,
promotion/failure retry, and a STANDARD-profile control. Keep compact-smoke
orchestration separate from fixture models, expected pixels, and accounting.
Exercise compact encoded sources through P3 physical-variant reuse,
target-color conversion, and incompatible-physical fallback. Record actual
bytes, temporary decode bytes, row scratch, direct-decode counts, and one
controlled promotion cost. Do not claim FPS improvements.

Run the smallest relevant SDK tests at each milestone. At finalization, run
focused diagnostics-on IMAGE tests with `-PruntimeDiagnostics=true`,
`artifactContentTest`, `dist -x test`, macOS ARM64 Release `tcvm` and
`Launcher`, both compact and STANDARD storage smokes, and the directly affected
P2/P3 and Q/P8 diagnostics smokes. Do not build Android, Windows, Linux, WinCE,
or iOS locally. Require a fresh green GitHub Merge Flow on enabled jobs for a
PR against `master`; leave the PR unmerged.

## Validation Design

Use the smallest relevant SDK/native check at each implementation slice.
Validation escalation follows `AGENTS.md`; do not repeat expensive builds after
documentation-only changes. Store build output in task-specific logs and report
summaries instead of full logs.

Acceptance covers focused policy/format/decode/observer/promotion/P2/P3 tests,
diagnostics-on IMAGE tests, artifact boundaries, SDK distribution, macOS ARM64
Release native targets, compact and affected regression smokes, memory/quality
measurements, fresh enabled GitHub Merge Flow, and
`git diff --check origin/master...HEAD`.

## Compatibility Limits

- Skia must support Gray8 and ARGB4444 allocation and conversion in addition to
  RGB565. Unsupported runtimes resolve COMPACT requests to STANDARD.
- Unsupported or structurally unsafe decoder inputs use RGBA8888. Compact
  alpha is premultiplied ARGB4444 and may differ from the full-precision source
  within the established black/white compositing bound.
- Local native validation is macOS ARM64. Other platform compatibility is
  determined by enabled CI jobs; storage accounting does not imply an FPS
  improvement.
