<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image runtime configuration migration specification

## 1. Purpose and boundaries

P1 (`feat/image-runtime-config`) replaces the historical image optimization
integer-mask model with typed startup configuration. It supplies named image
and rendering policy categories, one authoritative default policy, a mapping
from resolved requests to an immutable effective subsystem snapshot, any
short-lived internal adapters required by reconstructed consumers, and test
configuration helpers.

P1 does not implement raster, decode, prefetch, scroll-reuse, or frame-pacing
optimizations. Its integration must preserve the current `Image` and rendering
behavior until the feature PR that owns a behavior change lands. It depends on
the corrected generic RuntimeConfiguration foundation and the generic
RuntimeDiagnostics foundation. It consumes their contracts; it does not copy
their resolver, metadata, persistence, selector, or diagnostics implementation.

Configuration is resolved once during startup, after the environment facts
needed by rules (especially the graphics backend) are final. The image system
then reads a typed immutable/effectively immutable policy snapshot. Image
draw, decode, and frame paths do not look up selectors, parse configuration,
read environment variables, or reconstruct defaults.

## Evidence basis

The completed reconstruction work requested for Agents C, D, E, G, H, and I is
evidence for this contract, not an implementation dependency. The reviewed
snapshots are:

- Raster and compact-format reconstruction: `analysis/image-raster-reconstruction-spec`
  at `9c025a223f8eea463327c5a3239d84001a46dbf2`.
- Async image preparation reconstruction: `analysis/image-prefetch-reconstruction-spec`
  at `f1485b62ec7c289548ac5261863fadcdc20b4eb1`.
- Cross-feature rendering ownership map: `analysis/image-rendering-reconstruction-map`
  at `a58a8f47ad81984be5f25e8310bcb6290056ffd1`.
- Diagnostics classification and gating: `analysis/runtime-diagnostics-inventory`
  at `1a0603804100dfe42bec5fe27ca51896da32e0d6`.
- Scroll and frame-pacing reconstruction: `analysis/scroll-pacing-reconstruction-spec`
  at `6101098a80380306b0907d742b48060d5ea4302e` and the integrated
  `feat/frame-pacing-scheduling-diagnostics` snapshot at
  `7945fc817ad862400f05aded98fd679561fdac95`.
- Historical setting names and IDs: `perf/image-opt-phase1-controls` at
  `4ee0f71527c242b5171753ed4350aefadd3ad2b0`, with raster and format follow-up
  snapshots `77b5275b35a52a064c4170165f44687c8c03d948` and
  `2555d63bd0be366790546315eb8947fd1e91b0c2`.
- Runtime selector contract reference only:
  `feat/runtime-configuration-api` at
  `dc5fcc9dce25571ed0761ba8a0a49a6d42174be7`. P1 must use the corrected
  foundation's final contract and must not depend on the current branch's
  implementation classes.

## 2. Historical ID migration table

“Requested default” below is the policy request P1 supplies when no more
specific application rule changes a public value. For internal controls it is
the built-in rollout request, not an application API. “Effective default” is
conditional on the stated capability and eligibility checks; a safe fallback
preserves the canonical image result.

| ID | Historical meaning | Typed destination | Exposure | Requested default → effective default | Future consumer | Effectiveness conditions | Diagnostics relationship | Temporary adapter and removal point |
|---:|---|---|---|---|---|---|---|---|
| 0 | Zero-copy decode/backing | Internal `RasterCorePolicy.zeroCopyDecode` | INTERNAL | On → on when a compatible native backing/decode route exists; otherwise canonical decode | P2 raster core | Native implementation, source format, ownership, and platform must support the route | Aggregate decode route/bytes belong to RuntimeDiagnostics IMAGE; metrics do not select the route | Map from the effective snapshot only while old consumers remain; P2 removes this bit mapping |
| 1 | Raster opacity metadata | Internal `RasterCorePolicy.opacityMetadata` | INTERNAL | On → on where opacity is known safely; otherwise use conservative opacity handling | P2 raster core | Backing must provide trustworthy metadata; unknown opacity cannot be treated as opaque | Optional aggregate fallback-scan metrics belong to IMAGE/RENDERING diagnostics | P2 migrates the consumer and removes the bit mapping |
| 2 | Opaque `writePixels` path | Internal `RasterCorePolicy.opaqueWritePixels` | INTERNAL | On → on for an eligible opaque source and destination; otherwise generic draw | P2 raster core | Renderer/backend, alpha, geometry, destination, and native call must satisfy the existing exact checks | Aggregate attempts, hits, fallbacks, and useful bytes belong to RENDERING diagnostics | P2 removes the bit mapping |
| 3 | Bounded row readback | Internal `RasterCorePolicy.rowBatchedReadback` | INTERNAL | On → on when the backing's readback API supports bounded rows; otherwise prior readback | P2 raster core | Native backing and platform must support row reads without changing pixel/alpha results | Coarse readback count/bytes may use IMAGE or MEMORY diagnostics | P2 removes the bit mapping |
| 4 | Direct-color materialization | Internal `RasterCorePolicy.directColorMaterialization` | INTERNAL | On → on for supported, parity-preserving conversion; otherwise canonical materialization | P2 raster core | Source, target color type, backend, and conversion implementation must be compatible | Aggregate materialization and conversion bytes belong to IMAGE/RENDERING diagnostics | P2 removes the bit mapping |
| 5 | RGB565 compact storage | Public `ImageStorageProfile.COMPACT`; concrete format choice stays internal | PUBLIC via the high-level profile | Off (`STANDARD`) → off; a COMPACT request is effective only where at least one safe compact route is available | P4 compact formats | A decoder and target path must support the source; per-image alpha, content, and conversion checks may choose standard storage | Aggregate format/decode and memory totals use IMAGE/MEMORY diagnostics, never a mask | During migration the adapter may derive the legacy bit from the effective profile; P4 removes the bit mapping |
| 6 | GRAY8 compact storage | Public `ImageStorageProfile.COMPACT`; concrete format choice stays internal | PUBLIC via the high-level profile | Off (`STANDARD`) → off; COMPACT is best-effort and does not force every image to GRAY8 | P4 compact formats | Eligible grayscale sources and a supported decoder/backing are required; otherwise preserve the standard route | Same IMAGE/MEMORY diagnostic groups; no format-selection control in diagnostics | P4 removes the bit mapping |
| 7 | ARGB4444 compact storage | Public `ImageStorageProfile.COMPACT`; concrete format choice stays internal | PUBLIC via the high-level profile | Off (`STANDARD`) → off; COMPACT is effective only when its alpha/quality contract is supported | P4 compact formats | Source alpha semantics, decoder, backing, and target conversion must remain valid | Same IMAGE/MEMORY diagnostic groups; no diagnostic accounting option | P4 removes the bit mapping |
| 8 | Cache byte-budget reservation; historical helper carried a 64 MiB value | DROP; no cache-budget option | DROP | N/A → N/A | None | Reconstruction evidence does not establish an implemented cache governed by this budget | Memory gauges, if useful, are observations through RuntimeDiagnostics | No adapter; remove the inert reservation and helper with the legacy settings class in P4 |
| 9 | Memory-pressure eviction reservation | DROP; no pressure-eviction option | DROP | N/A → N/A | None | Historical pressure trigger is a placeholder; do not create behavior or an option for it | Memory observations remain diagnostics and cannot trigger eviction | No adapter; remove the inert reservation/helper with the legacy settings class in P4 |
| 10 | GPU discard of CPU backing | DROP; no GPU-discard option | DROP | N/A → N/A | None | No reconstructed product behavior justifies a setting; renderer support alone is not evidence | No image-config diagnostic; any backing totals are generic MEMORY observations | No adapter; P4 removes the historical slot |
| 11 | mmap for large backing; historical helper carried a 4 MiB threshold | DROP; no mmap option | DROP | N/A → N/A | None | No implemented cross-platform mmap backing was established | No image-config diagnostic | No adapter; remove the inert threshold/slot with the legacy settings class in P4 |
| 12 | Ad-hoc diagnostic-accounting gate | DROP from image configuration; metric groups stay in RuntimeDiagnostics | DROP | N/A → N/A | RuntimeDiagnostics foundation and owning feature metrics | Diagnostic availability and group gates are independent of behavior policy | IMAGE, RENDERING, PREFETCH, SCHEDULING, and MEMORY groups use the generic diagnostics mechanism | Never adapt this bit; P1 removes its settings and counter gate, then the diagnostics foundation owns group selection |
| 13 | Target-color conversion | Internal `RasterVariantPolicy.targetColorConversion` | INTERNAL | Off → off | P3 raster variants | If an internal rollout later requests it, require a supported backend/native implementation, eligible opaque content, and exact color parity | Aggregate attempts/hits/fallbacks/converted bytes belong to RENDERING diagnostics | Map only during transition; P3 removes the bit mapping |
| 14 | Physical raster variant cache | Internal `RasterVariantPolicy.physicalVariantCache` | INTERNAL | Off → off | P3 raster variants | If enabled internally later, require supported target/backing, exact keys, bounded storage, and safe invalidation | Aggregate lookup/hit/miss/eviction counts belong to RENDERING diagnostics | Map only during transition; P3 removes the bit mapping |
| 15 | Exact physical identity folding | Internal `RasterCorePolicy.physicalIdentityFolding` | INTERNAL | On → on for proven exact identity cases; otherwise ordinary draw | P3 raster variants | Exact physical geometry and a supported destination are required; transformed or uncertain cases fall back | Aggregate identity hits/fallbacks may belong to RENDERING diagnostics | Map only during transition; P3 removes the bit mapping |

The public compact-storage choice is deliberately one high-level preference,
not three public format selectors. RGB565, GRAY8, and ARGB4444 are storage
implementations whose eligibility can vary by image. COMPACT is a best-effort
preference; a standard backing remains valid when a format is unsupported or
would violate the feature's pixel/alpha contract. The application is not
promised a particular concrete compact format.

## 3. Proposed typed configuration model

### Public application request

Propose one public semantic type, `ImageStorageProfile`, with `STANDARD` and
`COMPACT` values. `STANDARD` is the default. `COMPACT` lets an application
request a documented memory-versus-fidelity tradeoff without naming a bit,
cache, or decoder implementation. It is public because an application can
make this product-level tradeoff for its assets and deployment targets. The
contract is best-effort; it does not promise a specific backing format or
reduced memory for every image.

No public switch is proposed for zero-copy, opacity metadata, `writePixels`,
readback batching, direct-color conversion, physical identity folding, target
color conversion, physical-variant cache policy, scroll raster reuse, worker
selection, diagnostic accounting, or frame drivers. They are implementation,
rollout, or test concerns rather than stable application behavior.

### Internal startup policy

Use small orthogonal internal categories, resolved from the public request and
the startup capability snapshot:

- `RasterCorePolicy`: zero-copy decode, opacity metadata, opaque `writePixels`,
  row readback, direct-color materialization, and exact identity folding.
- `RasterVariantPolicy`: target-color conversion and physical-variant reuse.
- `ScrollRasterReusePolicy`: vertical software framebuffer reuse.
- `ImagePreparationPolicy`: no global automatic prefetch switch; the existing
  explicit preparation request starts preparation.
- `PrefetchWorkerPolicy`: worker rollout selection, internal only. The legacy
  per-entry thread route remains the default.

These names describe policy responsibilities, not a commitment to a concrete
class layout. Do not combine them into a public mega-enum. Do not add public
worker modes, benchmark profiles, feature IDs, string keys, native masks,
failure injection, or implementation cache policies.

### Separate test forcing

Tests may install a named immutable policy through package-private or
otherwise test-source-only support. Test support may force an unsupported
request, fake renderer capabilities, choose a worker implementation, or inject
allocation/decode failures. None of these controls is serialized, deployable,
visible to application code, or read by production hot paths.

## 4. Authoritative default policy

P1 defines one source of defaults. It must not recreate a second default table
inside the adapter or each consumer. The typed default is:

| Category | Typed default |
|---|---|
| Application storage request | `ImageStorageProfile.STANDARD` |
| Raster/decode core (IDs 0–4) | Requested on; effective wherever safe and supported, with canonical fallback otherwise |
| Exact physical identity folding (ID 15) | Requested on; effective only on proven exact identity cases |
| Compact formats (IDs 5–7) | Off; only requested by `COMPACT` |
| Target-color conversion and physical variants (IDs 13–14) | Off |
| Scroll framebuffer row reuse | Off |
| Automatic image prefetch | Off; only an explicit existing preparation request schedules work |
| Semaphore worker rollout | Off; retain the legacy per-entry thread default |
| Frame-driver/pacing experiments | Off; production scheduling remains as reconstructed |
| Diagnostic collection | Controlled by RuntimeDiagnostics group defaults, never by image policy |

This is the typed equivalent of the historical final intent: core paths and
safe identity folding on, compact formats and target/variant caches off, and
scroll reuse and experiments off. The default is described by named values;
P1 must not encode or document it as an integer mask.

## 5. Requested versus effective state

A runtime selector supplies an application request. P1 then applies the
feature's rollout state and the finalized startup capabilities to produce the
image subsystem's effective snapshot. Keep both values conceptually distinct.
Diagnostics cannot participate in this decision.

`requested != effective` is expected when the runtime lacks an implementation
or capability; when the selected renderer/backend cannot use the route; when a
platform restriction applies; when the image/decoder format is unsupported;
when per-image alpha, geometry, or ownership checks fail; or when an internal
feature is rollout-disabled. A disallowed optimization falls back to the
canonical path and preserves the existing pixel, alpha, ownership, and error
semantics.

For `ImageStorageProfile.COMPACT`, the startup effective policy means compact
storage is allowed on this runtime. It does not promise each image can use a
compact representation. Per-image format and alpha eligibility may still pick
standard storage without changing the startup snapshot. If the runtime has no
usable compact decoder/backing at all, effective storage is `STANDARD` while
the request remains `COMPACT`.

The effective snapshot is created once after required renderer facts are
final. If a selector depends on a backend that the generic foundation reports
as pending, P1 follows that foundation's pending-resolution contract; it does
not guess the backend or add per-draw resolution. RuntimeDiagnostics may later
observe aggregate effective routes or fallbacks, but it never turns a policy on
or off.

## 6. Runtime selector integration

P1 consumes the generic selector model over platform, runtime family,
graphics backend, and architecture. The following examples are conceptual
rules; the generic foundation owns syntax, normalization, specificity, pending
resolution, and conflict handling:

- **Platform-specific:** request `COMPACT` for `platform = Android`.
- **Runtime-family-specific:** request `STANDARD` for `runtime family = JavaSE`.
- **Graphics-backend-specific:** request `COMPACT` for `graphics backend = Software`.
- **Architecture-specific:** request `COMPACT` for `architecture = ARM64`.
- **More-specific override:** a platform-only Android rule requests
  `COMPACT`; a matching Android + Software-backend rule requests
  `STANDARD`. The more-specific matching selector wins under the generic
  resolver's established ordering.
- **Equal-specificity conflict:** two distinct matching selectors of equal
  specificity request different storage profiles. P1 returns the generic
  resolver's conflict result; it adds no declaration-order or image-specific
  tie-break.

P1 must not redefine selector semantics, add image-only selector axes, or
resolve rules in image draw/decode/frame code. Application configuration is
resolved once and its result feeds the typed requested policy.

## 7. Internal adapter strategy

An adapter is required only while a reconstructed consumer still accepts an
integer mask or the historical `ImageOptimizationSettings` helper. The
adapter:

1. is internal and one-way from the typed effective snapshot to the remaining
   legacy consumer;
2. is derived once at startup, never from the persistence/wire representation;
3. contains no defaults of its own and cannot become a second source of truth;
4. is never exposed to applications or public reflection/API metadata; and
5. cannot be queried from draw, decode, or frame hot paths.

Migration/removal order is exact: P2 replaces IDs 0–4 at raster-core call
sites; P3 replaces IDs 13–15 at variant/identity call sites; P4 replaces IDs
5–7 at format selection and removes the adapter, historical settings helper,
all remaining image-mask state, and the inert IDs 8–12 reservations. Each owner
PR deletes its mapped portion rather than leaving compatibility scaffolding
behind. P5–P11 consume named policy contracts and must not reintroduce a mask.

If native interop temporarily still accepts a mask, keep that conversion in
the same internal startup adapter. Do not persist it, expose it to apps, pass
it through generic RuntimeConfiguration metadata, or use it as a feature
identity. Test migration parity by comparing effective named requests with the
old consumer's behavior before deleting the bridge.

## 8. Diagnostics relationship

Configuration chooses behavior; diagnostics observes behavior. P1 removes the
historical diagnostic-accounting setting from image policy. Metric groups are
selected through RuntimeDiagnostics and remain independent from requested and
effective image options. A diagnostics-off build or group cannot change
configuration effectiveness.

Expected later aggregate metric ownership is:

| Future feature | RuntimeDiagnostics domain(s) |
|---|---|
| P2 raster core and decode | IMAGE; MEMORY for backing totals |
| P3 physical variants | RENDERING; MEMORY where backing bytes are useful |
| P4 compact formats | IMAGE and MEMORY |
| P5 lazy JPEG | IMAGE and MEMORY where useful |
| P6 clipped raster fast path | RENDERING |
| P7 scroll framebuffer reuse | RENDERING |
| P8 asynchronous preparation | PREFETCH and IMAGE |
| P9 serialized Semaphore worker | PREFETCH; waiter probes remain test-only, not a production THREADING group |
| P10 static PNG preparation | PREFETCH and IMAGE |
| P11 frame-pacing diagnostics | SCHEDULING |

Only bounded aggregate observations are in scope for future metric groups. Do
not define metric IDs, expose feature flags through diagnostics, keep historical
accounting bits, or let a metric snapshot/reset affect runtime decisions.

## 9. Feature ownership

| Feature | Configuration owned by P1 | Added by feature PR | Default, visibility, and role | Backend/platform/environment dependency |
|---|---|---|---|---|
| P2 raster core | Internal core policy for IDs 0–4 | No new application setting | Core behavior requested on; internal implementation policy | Native backing/decode availability and exact alpha/color/ownership support |
| P3 raster variants | Internal policy for IDs 13–15 | No new public option | Identity folding requested on where safe; target conversion and variant cache off; internal rollout | Exact software/native target, geometry, opaque source, supported conversion, and invalidation rules |
| P4 compact formats | Public `ImageStorageProfile`; internal eligibility policy | No concrete format selector | `STANDARD` by default; `COMPACT` is opt-in and best-effort product behavior | Decoder/backing availability and per-image color, alpha, and format constraints |
| P5 lazy JPEG | Inherits core and storage policy; no lazy toggle | None | Lazy factory path follows its explicit factory/API contract; no rollout selector | JPEG source/decoder capability and existing eager argument/path/metadata error contract |
| P6 raster fast path | Inherits core/identity policy | No new setting or ID | Exact clip-aware path is internal product behavior | Supported native raster target and exact visible physical subrect; otherwise generic draw |
| P7 scroll raster reuse | Internal `ScrollRasterReusePolicy` | No public setting | Off; internal rollout/product behavior | Software framebuffer, vertical geometry, repaint state, and safe recovery only |
| P8 async prefetch | No global enable switch; explicit preparation request is the request | No runtime config value required | No automatic preparation; an explicit API request schedules it | Supported source decoder, worker/UI handoff, current source identity, and lifecycle |
| P9 Semaphore worker | Internal `PrefetchWorkerPolicy` | No public worker selector | Off; legacy per-entry threads stay the default; rollout/test concern | Semaphore V1 availability and queue lifecycle; test mode may force variants |
| P10 PNG prefetch | Inherits explicit P8 request and internal worker policy | No public setting | Static PNG joins the existing request; animated/multiframe PNG stays unsupported | Static one-frame PNG and Java decoder/adoption support |
| P11 frame-pacing diagnostics | No image configuration | RuntimeDiagnostics SCHEDULING group only; driver/clock forcing is test-only | Diagnostic collection follows its group gate; production driver/default stays unchanged | Platform driver and clock availability; experiments stay out of application policy |

P6 must normally add no public setting. P11 diagnostics and driver experiments
must never become image configuration. P8's explicit preparation operation is
not a global selector that silently starts work for every application image.

## 10. Compatibility plan

- Preserve the public `Settings` class and existing field/method descriptors.
  In particular, `Settings.optimizeScroll` controls the historical
  image-based scroll behavior. Keep that behavior compatible and do not map the
  field to P7's distinct software framebuffer row-reuse feature.
- The reconstructed `ImageOptimizationSettings` is package-private in the
  reviewed phase snapshots. It is not a reason to retain a public legacy API.
  Remove it after P4 deletes the last format-bit consumer.
- Keep existing public `Image` constructors, factories, fields, and method
  descriptors unchanged. P1 adds only the documented typed application
  surface; it does not change image decode/render behavior before its owner
  feature PR.
- If released SDK artifacts show that an older public mask/settings method
  shipped, preserve that exact descriptor with a deprecated, one-way shim into
  the new typed request. Do not invent a guessed compatibility API. The shim
  must not persist masks and is removed only under the repository's public ABI
  compatibility policy.
- Older deployed applications without runtime configuration keep the typed
  defaults and existing behavior. Do not require them to add configuration
  metadata or initialize a new public settings singleton.

## 11. Tests required for future P1 implementation

P1 should add focused tests for:

1. The authoritative typed defaults, including requested and effective states.
2. Each public and internal option category, validation, and immutable result.
3. Platform, runtime-family, backend, and architecture rule application.
4. More-specific override and equal-specificity conflict through the generic
   resolver contract.
5. Requested/effective divergence for unsupported renderer/backend,
   unavailable native implementation, runtime/platform restriction, unsupported
   format, and rollout-disabled internal options.
6. Adapter parity for all still-present legacy consumers, followed by tests
   that each owner PR removes its adapter portion.
7. API inspection proving there is no public integer-mask API, numeric feature
   ID, raw native mask, string key, or benchmark/worker/failure-injection
   option.
8. No image diagnostic-accounting control in configuration; metric group
   selection is delegated to RuntimeDiagnostics.
9. Startup snapshot immutability and no re-resolution when environment or
   caller-owned rule objects later change.
10. Draw/decode/frame path checks proving selector/configuration lookup and
    environment access happen only at startup, not in hot paths.
11. A baseline parity test showing P1 alone does not change Image or Rendering
    behavior before the owning feature PR is present.
12. Compatibility checks for `Settings`, public `Image` descriptors, and any
    released API shim confirmed by artifact inspection.

## 12. Ordered P1 implementation checklist

### Ordered checklist

1. Rebase the feature work on the corrected RuntimeConfiguration and generic
   RuntimeDiagnostics foundations. Read their final contracts; use only the
   stable selector and group-gating behavior.
2. Confirm the released public API surface and preserve existing `Settings`
   and `Image` descriptors. Do not infer a public mask API from internal test
   helpers.
3. Add the single public `ImageStorageProfile` request with the
   `STANDARD`/best-effort `COMPACT` contract. Keep all implementation and test
   controls internal.
4. Define one built-in defaults policy and separate requested values from
   capability- and rollout-limited effective values.
5. Resolve selectors and the finalized environment once during startup, then
   publish one immutable effective image-policy snapshot.
6. Keep diagnostics group selection outside image configuration and verify
   that diagnostic enablement does not change effectiveness.
7. Add the one-way startup adapter only for current legacy consumers; test
   parity and record its removal in P2, P3, and P4 as specified above.
8. Add test-only named configuration helpers and the focused test contract;
   keep forced modes and failure hooks out of application/runtime metadata.
9. Verify that P1 adds no raster/prefetch/frame behavior change. Hand P2–P11
   only the typed contracts they own; remove each adapter segment in its owner
   PR.

## 13. Genuine open questions

- Verify released SDK artifacts for any historically public image-mask or
  `ImageOptimization` API. The reviewed reconstruction class is package-private
  and the reviewed current branch exposes no public mask surface, but only the
  shipped API artifacts can settle binary compatibility for older applications.
