<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Native Image Backing Technical Report

## Implementation summary

The image-native-backing implementation makes deployed Skia images use an
explicit native backing instead of a persistent Java raster. Encoded sources
remain authoritative, deferred pipelines preserve semantic operation order,
and JavaSE and non-Skia paths retain their legacy raster behavior.

`ImageBacking` separates raster and native storage. `NativeImageBacking` owns
opaque native images and mutable surfaces with explicit snapshot, readback,
write, and release rules. `EncodedImageSource` captures input ownership
independently from lazy decode and reuses cached source backings. JPEG decode
uses conservative tiers with monotonic, transactional promotion; PNG and JPEG
decoding integrate with native materialization.

`ImagePipeline` remains the semantic source of truth. `ImageDrawPlan` is an
immutable execution optimization carrying ordered operations, frame and scale
metadata, alpha and presentation state, and decode generation. Deferred
geometry and exact compatible color stages execute through the Graphics/Image
bridge without unnecessary canonical materialization. Unsupported or
non-exact operations retain explicit materialization barriers.

## Architectural decisions

* Native backing is canonical after deployed Skia materialization; detached
  `getPixels()` snapshots remain a compatibility observer, not storage.
* Encoded bytes and decoded backing have independent ownership so eviction,
  retry, and adaptive JPEG promotion remain deterministic.
* Draw-plan caches may reuse exact structural work, but cached results never
  replace semantic pipeline state or become a presentation-state cache key.
* Frame selection and layout stay in the frame domain. Repeated or otherwise
  unrepresentable frame-domain operations are segmented or materialized rather
  than silently remapped.
* Native color fusion is capability-based. Exact alpha, fade, touch-up,
  color-key, hidden-RGB, frame-scope, and sampling semantics are preserved;
  approximate filters are not used to enlarge fusion coverage.

## Technically discarded approaches

Persistent Java pixel-array storage was rejected because it scales with image
area and creates avoidable readbacks. JavaSE is not routed through Skia because
the raster implementation is the compatibility reference. Encoded sources
are not discarded after one decode because independent leaves need ownership
and retry semantics. Broad reduced-resolution decoding is rejected because it
can underdecode later operations.

Approximate color filters, operation reordering, global decoded-raster caches,
native traversal of Java pipeline nodes, and GPU command buffering were also
rejected where they changed semantics or lifetime. `APPLY_COLOR2` remains a
materialization barrier when pinned Skia lacks an exact facility.

## Correctness findings and resolutions

The implementation hardens frame selection/layout barriers, repeated
frame-domain operations, frame-scoped fade, alpha and presentation state,
fractional destination scaling, geometry fallbacks, and cache reuse. The eager
resolver and deferred planner share the same frame-domain rule. Cached plans
synchronize mutable presentation inputs on reuse, while direct draws can
resolve at destination scale without mutating the source image.

Native ownership is attached to the backing wrapper and remains safe across
snapshots, aliases, cache eviction, and failed retries. Canonical barriers
resolve at scale one and adopt a native result. Full-raster readback is kept
for compatibility observers and encoders rather than ordinary creation or
decode.

## PNG hidden-RGB diagnosis

The PNG parity regression distinguished raw-buffer parity from visible
semantic parity. The source contains fully transparent pixels with nonzero
hidden RGB. The eager path preserves those bytes, while source-over drawing
into a transparent Skia target produces transparent pixels with zero RGB. The
raw buffers therefore differ only where both expected and actual alpha are
zero; alpha and all visible channels remain equal.

Canonicalizing alpha-zero RGB made all semantic parity cases equal, while the
nonzero-brightness control retained exact raw parity. The permanent smoke
rejects alpha or visible-color differences and accepts only this documented
hidden-RGB representation case.

## Memory and backing results

The ImageModifier workload performs 4,000 direct draws per fixture with no
`getPixels()` or encoding in the loop. The macOS run covered the 600x600 PNG
and retained one native backing of 1,440,000 bytes. It reported one full
decode, zero geometry materializations, zero native-color readbacks, zero
backing readbacks, one live backing at peak, and release to zero after the
root was collected. The stress evidence records the same bounded behavior for
the 512x512 and 1960x1960 JPEG fixtures, with one backing of 1,048,576 and
15,366,400 bytes respectively.

The workload still creates substantial short-lived Java objects—approximately
11,985 `Image` instances, 7,985 pipelines, and 4,000 draw plans in the
reported 4,000-event run—but native backing count and bytes remain bounded.
The observed residual process RSS is consistent with allocator/Skia heap
retention rather than per-event native backing accumulation.

## Validation performed

The following checks passed:

* `./gradlew-agent test --tests 'totalcross.ui.image.*'`.
* Converter/native-backing and encoded-source tests.
* `./gradlew-agent dist -x test --no-daemon --console=plain`.
* macOS arm64 and host-default-architecture CMake/Ninja Release builds of
  `tcvm` and `Launcher`.
* The image smoke matrix covering deferred color, crop/frame, fade/layout,
  presentation, JPEG modifier and pinch, lazy materialization, ImageModifier
  memory and workload, native color, native geometry, native materialization,
  and PNG semantic parity.
* The adaptive encoded-source macOS smoke.
* `git diff --check`, copyright-header validation, native field scans,
  readback accounting, and source/test behavior audits.

The legacy ABI smoke and the high-density portions of the frame-state smoke
still report failures: native pixel mutation remains unchanged in the ABI
fixture, and the high-density frame-layout assertion fails. Independent
comparison with the established implementation baseline reproduced both
signatures, so they are documented baseline limitations rather than masked by
an unrelated behavior change.

## Known limitations and follow-ups

Native smoke validation covers the macOS arm64 Skia path. Android, iOS,
Linux, Windows, sanitizer, and full platform-matrix validation remain outside
this validation scope. Automatic runtime memory-pressure eviction is not
wired because the runtime has no safe repository-wide callback; the explicit
backing eviction boundary remains available.

Java allocation churn in highly varied modifier workloads remains a follow-up
optimization opportunity even though native backing memory is bounded. The
diagnostic formatting path also retains a follow-up for `Integer.toHexString`;
fixed-width conversion is used where semantic parity output requires it, but
broader cleanup is outside this work.

## Conclusion

The implementation establishes native image backing for deployed Skia while
preserving the public Image model, legacy raster fallback, deferred semantic
ordering, frame behavior, exact compatible color operations, and detached
compatibility readback. The remaining limitations are bounded and documented:
exact barriers remain where native capability is insufficient, automatic
eviction awaits a safe runtime callback, and broader platform validation awaits
the corresponding toolchains.
