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
- Experimental public mask surface: commit
  `fd86de1b1dfc4ee357681b5788f66a1f32be2a47` made
  `ImageOptimizationSettings` public and added `setMask`, `getMask`, and
  `getEffectiveMask` on the untagged reconstruction line. It is present in
  `feat/frame-pacing-scheduling-diagnostics` but not in `origin/master`; a
  source scan of all 63 repository release tags found none of those public
  declarations.
- Runtime selector contract reference only: the latest reviewed
  `feat/runtime-configuration-api` snapshot at
  `2ec80f3d30d68f9b864117c3881d02d22b817225`; generic diagnostics foundation
  reference `feat/runtime-diagnostics` at
  `3a7efb694dd9214389f65f5e82a9db1f12b46a78`. P1 consumes the merged
  foundations' stable contracts and must not depend on temporary branch class
  names or modify their generic rule annotation/wire schema.

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

The only currently justified public Image behavior request is
`ImageStorageProfile`, owned by P1 in `totalcross.ui.image`:

```java
package totalcross.ui.image;

public enum ImageStorageProfile {
  STANDARD,
  COMPACT
}
```

`STANDARD` is the default. `COMPACT` requests the documented
memory-versus-fidelity tradeoff without naming a bit, cache, or decoder
implementation. It is public because an application can make this product
choice for its assets and deployment targets. The contract is best-effort; it
does not promise a particular backing format or reduced memory for every
image.

P1 also owns a repeatable, feature-specific declaration in the same package:

```java
package totalcross.ui.image;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import totalcross.sys.runtime.RuntimeWhen;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Repeatable(ImageRuntimeRules.class)
public @interface ImageRuntimeRule {
  RuntimeWhen when();
  ImageStorageProfile storage();
}

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface ImageRuntimeRules {
  ImageRuntimeRule[] value();
}
```

The application entry class uses B's marker and the P1 action annotation:

```java
import totalcross.sys.GraphicsBackend;
import totalcross.sys.Platform;
import totalcross.sys.runtime.RuntimeCondition;
import totalcross.sys.runtime.RuntimeConfiguration;
import totalcross.sys.runtime.RuntimeWhen;
import totalcross.ui.image.ImageRuntimeRule;
import totalcross.ui.image.ImageStorageProfile;

@RuntimeConfiguration
@ImageRuntimeRule(
    when = @RuntimeWhen(allOf = @RuntimeCondition(platform = Platform.ANDROID)),
    storage = ImageStorageProfile.COMPACT)
final class App {}
```

`RuntimeConfiguration`, `RuntimeWhen`, and `RuntimeCondition` remain owned by
B in `totalcross.sys.runtime`; `Platform`, `RuntimeFamily`, `GraphicsBackend`,
and `Architecture` remain owned by B in `totalcross.sys`. Use B's enum
vocabulary exactly: `Platform.WINDOWS`, `MACOS`, `LINUX`, `ANDROID`, `IOS`,
and `UNKNOWN`; `RuntimeFamily.DESKTOP`, `MOBILE`, `EMBEDDED`, and `UNKNOWN`;
`GraphicsBackend.RASTER` and `GPU`; `Architecture.X86`, `X86_64`, `ARM32`,
`ARM64`, and `UNKNOWN`. `ImageRuntimeRule` and its repeatable container are
owned by P1 in `totalcross.ui.image`.
`ImageRuntimeRule` carries exactly the selector and typed storage assignment.
`when()` and `storage()` are required members. There is no `NONE` or default
sentinel: every declared rule changes the one storage setting, and no matching
rule means `STANDARD`.

The annotation and its container use CLASS retention so conversion reads the
class file before runtime. No runtime reflection is required. The public
surface contains no `Object` value, generic map, string option key, feature ID,
mask, or benchmark/worker/failure-injection control.

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

A matching `ImageRuntimeRule` supplies the typed application request. P1 then
applies the feature's rollout state and finalized startup capabilities to
produce the image subsystem's effective snapshot. Keep the requested
`ImageStorageProfile` distinct from effective policy. Diagnostics cannot
participate in this decision.

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

## 6. Runtime selector, action transport, and startup integration

### Rule and conflict contract

`ImageRuntimeRule` is P1's typed action carrier. B's generic `RuntimeRule`
remains a selector-only annotation; P1 does not add Image fields to it. The
P1 converter parser reads `ImageRuntimeRule` and its repeatable container,
converts each `when()` using B's `RuntimeWhen`/`RuntimeCondition` dimensions,
and creates a P1-owned typed pair `(B selector, ImageStorageProfile action)`. It uses B's
selector model and resolver semantics rather than implementing another
specificity score. P1's ASM parser preserves B's `allOf`/`anyOf` structure and
per-dimension enum alternatives, then uses B's typed selector model. The
`RuntimeCondition` member name for runtime family is `family`; backend values
are `GraphicsBackend.RASTER` or `GraphicsBackend.GPU`.

No matching Image rule requests `ImageStorageProfile.STANDARD`. For a matching
rule, the more-specific selector overrides a less-specific selector. Equal
specificity is evaluated after deploy-time pruning with B's specificity
semantics. Different storage assignments at equal specificity fail
startup-time resolution deterministically; declaration order never selects a
winner. Equal-specificity assignments with the same value are not
contradictory.

For example, these actions resolve to `STANDARD` on an
`Platform.ANDROID` runtime using `GraphicsBackend.RASTER`:

```java
@RuntimeConfiguration
@ImageRuntimeRule(
    when = @RuntimeWhen(allOf = @RuntimeCondition(platform = Platform.ANDROID)),
    storage = ImageStorageProfile.COMPACT)
@ImageRuntimeRule(
    when = @RuntimeWhen(allOf = @RuntimeCondition(
        platform = Platform.ANDROID, backend = GraphicsBackend.RASTER)),
    storage = ImageStorageProfile.STANDARD)
final class App {}
```

The second selector constrains two dimensions and overrides the matching
platform-only selector. Other valid B selector examples are
`family = RuntimeFamily.DESKTOP` and `architecture = Architecture.ARM64`.

Two equally specific contradictory rules fail. For example:

```java
@ImageRuntimeRule(
    when = @RuntimeWhen(allOf = @RuntimeCondition(
        platform = Platform.WINDOWS, backend = GraphicsBackend.RASTER)),
    storage = ImageStorageProfile.STANDARD)
@ImageRuntimeRule(
    when = @RuntimeWhen(allOf = @RuntimeCondition(
        architecture = Architecture.X86_64, backend = GraphicsBackend.RASTER)),
    storage = ImageStorageProfile.COMPACT)
```

On an environment with `Platform.WINDOWS`, `Architecture.X86_64`, and
`GraphicsBackend.RASTER`, both rules match and each constrains two dimensions.
B reports an equal-specificity conflict; P1 reports the storage setting and
conflicting typed values without choosing by declaration order.

### Converter and deployer ownership

The P1 converter parser is feature-owned and ASM-based. It reads the
CLASS-retained Image annotation from the same application entry class bytes
that B scans. The generic parser continues to process only B's
`@RuntimeConfiguration`/`@RuntimeRule` declarations. P1 requires B's
`@RuntimeConfiguration` marker when an `@ImageRuntimeRule` is present, reports
malformed fields with class/rule context, and does not require any generic
`@RuntimeRule` to accompany the Image rule. Both parser results are retained
separately by the deployer.

At deployment, P1 passes each paired selector through B's existing
deployment-target pruning operation as a one-selector input. If pruning removes
the selector, P1 removes its paired storage action too. Otherwise P1 keeps the
pruned selector and the same typed action together. This per-rule association
preserves specificity after pruning and avoids trying to reconstruct which
parallel action belonged to a removed selector. An empty retained Image rule
set needs no Image metadata entry and resolves to the default.

B owns generic selector parsing semantics, target facts, pruning, normalized
selector representation, and match/specificity/conflict behavior. Deployment
facts unavailable to B at packaging time, including an unfinalized graphics
backend, stay dynamic for startup resolution. P1 owns
Image annotation discovery/validation, typed action parsing, pairing, and
Image-specific error context. No generic foundation source or
`RuntimeRule` member changes are part of P1.

### Feature metadata and startup binding

P1 writes one reserved, feature-owned TCZ resource, `tc.imageruntimeconfig`.
Its exact version-1 payload uses big-endian integers: four ASCII magic bytes
`TCIR`; one version byte (`1`); an unsigned 16-bit retained-rule count; then,
per rule, an unsigned 32-bit selector-payload length, that many
selector-payload bytes, and one storage tag (`0x01` for `STANDARD`, `0x02` for
`COMPACT`). Counts above 65,535 and selector payload lengths above
`Integer.MAX_VALUE` are rejected. The selector payload is B's versioned
generic selector encoding for exactly one selector after deploy-time pruning.
P1 omits the entry when the retained-rule count is zero. Record order has no
precedence meaning.

P1 owns the outer magic/version, framing, and explicit closed action-tag
mapping; it rejects an unknown magic, version, tag, malformed nested selector,
length, or trailing bytes and never uses Java enum ordinals. These private wire
tags are not public numeric feature IDs, option keys, or masks. B owns the
nested selector payload format and its versioning. The deployer reserves the
Image resource name and rejects an application resource collision. The
`tc.imageruntimeconfig` resource is an internal container name, not a public
string option key.

At native startup, after the generic runtime environment and graphics facts
are finalized but before the application main class loads, B performs generic
environment initialization and P1's Image startup binder decodes
`tc.imageruntimeconfig`. The simulator parses the main class's Image
annotations before class initialization, then passes typed actions to the same
P1 binder at the corresponding environment-finalization point. Neither path
uses reflection. P1 pairs each decoded B selector with its typed action and
submits the storage assignments to B's generic action-resolution contract;
B owns selector matching, specificity, and equal-specificity conflict
detection, while P1 owns the single Image storage setting descriptor and
translates a conflict into a deterministic Image configuration startup error.

The end-to-end ownership path is:

1. P1 parses the CLASS-retained annotation into a B selector plus typed
   `ImageStorageProfile` action.
2. The deployer uses B's target pruning for that selector and keeps the action
   paired only if the selector survives.
3. P1 writes the pair to its versioned feature resource, nesting B's unchanged
   selector payload.
4. P1 startup decodes the feature resource and B decodes each nested selector.
5. B's generic rule resolution selects the requested profile and reports
   equal-specificity conflicts.
6. P1 maps the requested profile to capability/rollout-limited effective
   policy and publishes the immutable snapshot.

The winning value is the requested `ImageStorageProfile`. P1 then evaluates it
once against the finalized `RuntimeEnvironment` and image feature
capabilities/rollout, producing the immutable effective Image policy described
in section 5. A COMPACT request unsupported by the runtime becomes effective
STANDARD. Later per-image format/alpha eligibility may choose standard backing
without changing the immutable startup request or policy. RuntimeDiagnostics
never participates in this resolution.

## 7. Internal adapter strategy

At P1, remove the unreleased public `setMask(long)`, `getMask()`, and
`getEffectiveMask()` descriptors. An adapter is required only while a
reconstructed consumer still accepts an integer mask or the historical
`ImageOptimizationSettings` helper. The adapter:

1. is internal and one-way from the typed effective snapshot to the remaining
   legacy consumer;
2. is derived once at startup, never from the persistence/wire representation;
3. contains no defaults of its own and cannot become a second source of truth;
4. is never exposed to applications or public reflection/API metadata; and
5. cannot be queried from draw, decode, or frame hot paths.

Migration/removal order is exact: P2 replaces IDs 0–4 at raster-core call
sites; P3 replaces IDs 13–15 at variant/identity call sites; P4 replaces IDs
5–7 at format selection and removes the adapter, historical settings helper,
all remaining image-mask state, and the inert IDs 8–12 reservations. P1 routes
diagnostic collection through F and never adapts ID 12; P4 removes any
remaining historical accounting slot with the rest of the old settings
scaffolding. Each owner PR deletes its mapped portion rather than leaving
compatibility scaffolding behind. P5–P11 consume named policy contracts and must not reintroduce a mask.

A temporary integer representation is internal startup compatibility only. It
is never public API, feature metadata, persistence, a wire format, or the input
to B's selector resolver. No historical diagnostic-accounting control is ever
adapted.

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
- An untagged reconstruction branch temporarily made
  `ImageOptimizationSettings` public and exposed `setMask(long)`, `getMask()`,
  and `getEffectiveMask()`. The reviewed repository release tags and
  `origin/master` contain none of these declarations, so they are not a
  released binary/source compatibility contract. P1 removes those public
  descriptors and keeps any transitional adapter package-private and
  one-way. P4 deletes the remaining helper and mask state after the last
  format-bit consumer migrates.
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
3. `ImageRuntimeRule` CLASS retention, repeatable-container parsing, typed
   `storage()` decoding, required `@RuntimeConfiguration` marker, and errors
   for malformed/missing members.
4. Platform, runtime-family, backend, and architecture selectors using B's
   exact enum values and `RuntimeWhen`/`RuntimeCondition` shapes.
5. Converter/deployer pairing of each typed action with its selector, including
   rules removed by deployment pruning and rules whose specificity changes
   only by B's pruning contract.
6. Versioned Image metadata round-trip, rejection of unknown action/container
   versions, TCZ resource collision handling, and no change to B's generic
   selector wire payload.
7. More-specific override and contradictory equal-specificity conflict,
   including reversed annotation declaration order; same-value equal-specific
   actions remain valid.
8. Requested/effective divergence for unsupported renderer/backend,
   unavailable native implementation, runtime/platform restriction, unsupported
   format, and rollout-disabled internal options.
9. Adapter parity for all still-present legacy consumers, followed by tests
   that each owner PR removes its adapter portion.
10. API inspection proving there is no public integer-mask API, numeric feature
    ID, raw native mask, string option key, untyped map, or benchmark/worker/
    failure-injection option.
11. No image diagnostic-accounting control in configuration; metric group
    selection is delegated to RuntimeDiagnostics.
12. Startup snapshot immutability, one-time rule resolution, and no runtime
    reflection or hot-path selector/configuration/environment lookup.
13. A baseline parity test showing P1 alone does not change Image or Rendering
    behavior before the owning feature PR is present.
14. Compatibility checks for `Settings`, public `Image` descriptors, and any
    released API shim confirmed by artifact inspection.

## 12. Ordered P1 implementation checklist

1. Consume merged B RuntimeConfiguration and F RuntimeDiagnostics foundations;
   use their final stable selector, deployment-pruning, resolver, startup, and
   group-gating contracts.
2. Add `ImageStorageProfile` in `totalcross.ui.image` with only `STANDARD` and
   best-effort `COMPACT` values.
3. Add CLASS-retained, TYPE-targeted, repeatable `ImageRuntimeRule` and its
   `ImageRuntimeRules` container in `totalcross.ui.image`, requiring B's
   `@RuntimeConfiguration` marker.
4. Add the P1 converter parser and extend deployer integration to parse the
   typed Image action alongside B's generic selector-only rules.
5. Prune each Image selector through B's deployment logic and keep its typed
   action paired with the surviving selector.
6. Persist the pair in the versioned P1-owned `tc.imageruntimeconfig` resource,
   nesting B's selector payload without changing `tc.runtimeconfig`.
7. Decode the feature resource and resolve its selectors once at startup using
   B's generic specificity/conflict semantics, before the app main class loads.
8. Create the requested `ImageStorageProfile`, apply finalized environment,
   capability, and rollout effectiveness once, then publish the immutable
   effective Image policy.
9. Remove the unreleased public mask methods, then create the temporary
   internal startup mask adapter only for existing consumers; never put that
   representation in metadata or public APIs. Remove IDs 0–4 in P2, IDs 13–15
   in P3, and IDs 5–7 plus remaining mask/settings scaffolding in P4; keep IDs
   8–12 dropped and never adapt diagnostic accounting.
10. Verify P1 alone changes no rendering/decode behavior and that configuration
    lookups stay out of hot paths.

## 13. Genuine open questions

None remain in the P1 rule/action or public-API design based on the inspected
repository state. The historical mask methods were present only on an
untagged reconstruction branch; the scan of 63 release tags and current
`origin/master` found no released declaration to preserve. Existing released
`Settings` and `Image` descriptors remain covered by the compatibility plan.
