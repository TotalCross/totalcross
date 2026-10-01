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

## Working Set and Resume Protocol

This plan is intentionally self-contained and is the only planning record for
this feature. Its milestone checkboxes and active milestone identify the next
work. Do not create reconstruction notes, checkpoint diaries, or evidence
indexes. Build output belongs in task-specific temporary logs; the final report
contains concise results and supported limitations only.

## Progress

- [x] Confirmed the base contains P2 authoritative backing state, opacity and
  mutation generation, bounded row readback, and P3 physical identity, exact
  derived-variant keys, and single-slot/pending variant ownership.
- [x] Implemented startup capability resolution, internal format metadata,
  actual row-byte/backing-byte accounting, and row-bounded RGBA observers.
  Compact source selection remains disabled until decode paths land.
- [ ] Implement structural format selection and direct compact JPEG/PNG source
  decode, with compact observer and accounting evidence.
- [ ] Add deterministic compact-format quality fixtures and full observer
  coverage.
- [ ] Add transactional promotion and complete P2/P3 lifecycle coverage.
- [ ] Finish focused validation, macOS smoke and measurements, and the factual
  implementation report.

## Current Architecture and Scope

### Runtime policy

`ImageStorageProfile` is public configuration with `STANDARD` and `COMPACT`.
`ImageRuntimeConfigurationStartup` resolves deployment rules after runtime
backend selection and publishes `ImageRuntimePolicy`. The current policy
intentionally downgrades every compact request using a P1-only reason. Replace
that placeholder with one resolved native compact-capability boolean. A
deployed Skia runtime may opt in; JavaSE/simulator paths and non-Skia builds
must resolve to `STANDARD` with a durable capability reason when native compact
storage is unavailable. Keep application APIs limited to the profile enum.

If startup cannot call the native capability probe across package boundaries,
add one internal Image capability bridge following the existing internal
bridge pattern. Exclude it from `totalcross-api`, the aggregate SDK, and
distributed SDK artifacts, and extend artifact-content/compile-surface checks.
Pass capability once to policy resolution; decoders consume effective policy
instead of repeating platform checks.

### Backing ownership and formats

Java `ImageBacking` owns opacity and monotonically increasing mutation
generation. `NativeImageBacking` owns the native handle and delegates snapshots,
mutability, dimensions, and row readback. In
`TotalCrossVM/src/nm/ui/skia/skia_image_backing_internal.h`,
`NativeImageBackingRecord` currently has a SkImage or SkSurface, dimensions,
generation, opacity-analysis cache, and P3's exact one-slot/one-pending raster
variant. `skia_image_backing.cpp` currently assumes every backing is 4 bytes per
pixel and reads rows through RGBA conversion. Make record metadata authoritative
for format, rowBytes, and resident backing byte count; preserve the existing
generation, cache, snapshot, and ownership behavior.

Internal canonical formats are `RGBA8888`, `RGB565`, `GRAY8`, and `ARGB4444`.
Map them to Skia's RGBA8888, RGB565, Gray8, and ARGB4444 color types with
appropriate opaque or premultiplied alpha. Use Skia conversion and packing
primitives; keep packed byte layout owned by Skia. All new mutable backing,
render target, generic materialization, and P3 derived variant destinations stay
RGBA8888. Only immutable/source content may remain compact.

### Decode and observation

The existing native JPEG decoder has adaptive decode modes and a direct
full-resolution Skia path that currently allocates an RGBA surface plus one
RGBA row. The PNG decoder has libpng progressive callbacks, structure metadata,
and an optional RGBA direct path; unsupported inputs may use the existing
full-precision route. Extend those decoder paths rather than adding new public
decoders. For supported compact sources, row conversion writes into one final
compact candidate, and decoder ownership transfers that candidate to the Image
only after successful completion. Decoder failure and install failure release
an unpublished candidate once and leave retry possible.

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

## Plan of Work

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

### Milestone 5 — Integration and handoff

Complete durable focused SDK tests and a macOS native smoke app covering all
three compact formats, expected byte use, readback quality, observers, draw,
promotion/failure retry, and P3. Record a small current-branch measurement of
actual bytes, temporary decode bytes, row scratch, direct-decode counts, and
one controlled promotion cost. Do not claim FPS improvements.

Run the smallest relevant SDK tests at each milestone. At finalization, run
focused diagnostics-on IMAGE tests with `-PruntimeDiagnostics=true`,
`artifactContentTest`, `dist -x test`, macOS ARM64 Release `tcvm` and `Launcher`,
the compact smoke and directly affected P2/P3 smokes. Do not build Android,
Windows, Linux, WinCE, or iOS locally. Make a PR against `master`, require a
fresh green GitHub Merge Flow on enabled jobs, and do not merge.

Create `.agent/reports/image-compact-storage.md` last as a factual technical
handoff. It reports the final capability rule, precedence and per-format
eligibility, actual storage sizes/rowBytes, direct decode and ownership,
observer behavior, promotion failure/retry, P2/P3 behavior, tests and builds
actually run, measurements and limitations. It contains no worktree, rebase,
commit-SHA, raw-log, checkpoint, or reconstruction history.

## Decision Log

- Decision: COMPACT is a runtime opt-in with deterministic internal
  representation; it is not public pixel-format selection.
  Rationale: preserve a small application-facing contract and stable selection.
- Decision: only immutable encoded-source backings use compact storage;
  mutable and derived destinations remain RGBA8888.
  Rationale: observers and drawing must preserve canonical storage, while
  mutation continues to meet full-precision semantics after transactional
  promotion.
- Decision: format eligibility is based on encoded structure, not a raster
  scan or optional opacity analysis.
  Rationale: selection stays bounded, repeatable, and independent of image
  dimensions and metadata-cache success.

## Validation and Acceptance

Use the smallest relevant SDK/native check at each implementation slice.
Validation escalation follows `AGENTS.md`; do not repeat expensive builds after
documentation-only changes. Test commands and durable smoke entry points will
be added to the SDK/native build configuration as their owners are identified.
All build output goes to task-specific logs, summarized without dumping full
logs.

Final acceptance requires focused policy/format/decode/observer/promotion/P2/P3
tests, diagnostics-on IMAGE tests, artifact boundaries, SDK distribution,
macOS ARM64 Release native targets, compact and affected regression smokes,
memory/quality measurements, fresh enabled GitHub Merge Flow, and
`git diff --check origin/master...HEAD`. Record only commands actually run.

## Risks and Open Questions

- Confirm the repository's pinned Skia revision exposes stable Gray8 and
  ARGB4444 raster allocation/readback support on macOS ARM64.
- Confirm libpng's current transformations and JPEG's adaptive scaling can
  write compact scanlines without an intermediate full RGBA image.
- Confirm `tRNS` semantics across supported PNG color types and preserve the
  existing public transparency behavior.
- Choose capability-bridge direction and native capability symbol after
  confirming generated Java/native ABI conventions.
- Promotion in-place must update byte accounting and native generation
  atomically; replacement-handle promotion must preserve native object
  ownership and exactly-once release.
- P3 target-color conversion must accept compact source color types safely or
  use its existing fallback without replacing canonical source storage.
- CI platform jobs remain the compatibility gate for platforms that must not
  be built locally.

## Idempotence and Recovery

Perform all implementation in the feature branch created from fresh
`origin/master`. Never alter unrelated worktree files or generated dependencies.
Decoder candidates remain private until complete success; release candidate
storage once on every failure path. Promotion builds a complete replacement
before publishing, so failures are retryable. Rebase only after fetching; if
`origin/master` advanced, resolve conflicts without discarding either side and
rerun directly affected focused checks. Open one PR and leave it unmerged.

## Outcomes & Retrospective

Complete this section when the feature is done. Summarize the accepted compact
backing behavior, the validation actually completed, known limitations, and
deferred checks. Keep implementation detail and measured values in the final
report rather than turning the plan into a checkpoint diary.
