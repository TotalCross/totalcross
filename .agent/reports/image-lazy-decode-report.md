<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image lazy-decode pipeline report

## Result

The Image lazy-decode feature is implemented across the Java SDK, deployed
TCVM, native image loaders, Graphics drawing boundaries, converter metadata,
unit tests, smoke applications, and SDK build wiring.

Encoded sources are consumed and structurally validated during construction,
while raster allocation and pixel decoding remain deferred until a dependent
operation needs them. Deferred transforms remain declarative, and drawing can
resolve geometric results at the destination content scale without changing the
source Image. Existing public signatures, Image field prefixes, explicit JPEG
APIs, and backend compatibility rules remain intact.

## Changes delivered

### Encoded source capture and validation

`EncodedImageSource` snapshots byte arrays, streams, and paths without retaining
caller-owned resources. JavaSE retains an owned byte array; deployed TCVM uses
an independently owned native `ImageEncodedBag`. TCZ-first path capture closes
handles on success and failure, and bag release is idempotent.

`ImageEncodedStructure` validates PNG, JPEG, GIF, and BMP structure without
inflating compressed data or producing a pixel raster. PNG validation covers
signature, IHDR, supported color/depth combinations, palette and transparency
ordering, IDAT ordering, CRCs, IEND, comments, and FC frame metadata. JPEG
validation covers marker/segment framing, byte stuffing, restart markers,
progressive scans, supported SOF dimensions, SOS, and EOI; deployed validation
also uses the libjpeg header reader over the native bag. JavaSE GIF and BMP
validation walks tables, blocks, dimensions, payload bounds, and RLE framing
without LZW expansion or scanline generation.

Malformed structure is rejected during capture. A source whose container is
valid but whose compressed payload is corrupt may still be captured and fails
when materialized, preserving the distinction between structural and pixel
validation.

### Lazy materialization and lifecycle

Encoded `Image` constructors eagerly capture source bytes and metadata but do
not allocate decoded pixels. Metadata access remains decode-free, including
dimensions, logical dimensions, content scale, frame count, path, and comments.
Writable and logical raster constructors remain eager.

Canonical materialization resolves at scale one into temporary complete state,
checks metadata parity, and adopts the result atomically into the same Image
object. A checked barrier preserves `ImageException`; non-throwing barriers use
the established unchecked compatibility boundary. Failed attempts leave the
deferred source intact.

All pixel readers, writers, exporters, frame operations, equality/hash paths,
Graphics creation, texture operations, and native Image/Graphics consumers are
protected by materialization barriers. Java wrappers call private native bridges
only after materialization, so native code never dereferences a deferred image
with null pixels. Existing public descriptors and Image field ordering remain
unchanged.

Deterministic encoded-payload failures are cached on the source. Allocation,
decoder setup, infrastructure, and other resource failures remain retryable.
Benign libpng warnings remain nonfatal, while incomplete decoded rows are
rejected before pixels are published. Multi-frame buffer allocation failures
also preserve the deferred source for retry.

### Deferred transforms

`ImagePipeline` is an immutable linked root-plus-operation representation for
nearest scale, smooth scale, rotation/scale, touch-up, fade, and alpha. An
encoded root is shared without decoding or copying its source. A materialized
mutable root is deep-snapshotted in `RasterImageSource`, including pixels,
multi-frame storage, dimensions, frame metadata, comments, transparency,
content scale, hardware scale, and source metadata.

Public transformation methods append nodes while the existing eager Java and
native kernels perform the actual work during resolution. Nodes are resolved in
caller order without fusion or reordering. No-op identity behavior, frame
semantics, rotation fill color, large-image hashing, hardware-scale behavior,
and the historical native rotation parameter ordering are preserved.

### Destination scale and JPEG resolution

Graphics source-image boundaries resolve deferred images against the destination
`contentScale` without adopting the returned variant into the original Image.
Geometric nodes use checked physical dimensions derived from logical dimensions;
color-only nodes preserve their input scale. Canonical barriers still adopt a
scale-one result, and ordinary materialized Images are not regenerated for a
HiDPI destination.

Each pipeline leaf keeps at most two exact-scale LRU variants. Cache misses
resolve from the authoritative encoded source or detached raster snapshot;
failed resolutions are not cached. Eviction and canonical adoption release
textures without forcing materialization, while discarded CPU rasters remain
eligible for normal garbage collection. Presentation state is synchronized onto
returned variants.

Target-aware decode is intentionally limited to an encoded JPEG whose first
operation is a smooth downscale. JavaSE and TCVM use equivalent `TARGET_DECODE`
selection: choose the largest native reduction whose ceiling dimensions still
meet both physical target axes, then apply the existing smooth kernel. Thus a
`1600x900` JPEG targeting `200x400` uses 1/2 (`800x450`) before producing the
exact `200x400` result; it cannot use 1/8 because its height would be
insufficient. Public `getJpegBestFit` uses a separate `BEST_FIT` mode based on
the limiting axis, while `getJpegScaled` uses `EXPLICIT_RATIO` and passes its
positive ratio directly to libjpeg. PNG, GIF, BMP, direct JPEG, nearest-scale,
rotation-first, and color-before-smooth paths use full-decode fallback.

## Problems found and resolved

The implementation had to preserve several historical boundaries. Libpng
warnings could not become fatal merely because pixel errors are deferred, but a
truncated decoded raster could not be published. The final loader status
distinguishes deterministic corruption from transient allocation or resource
failures so retry behavior is reliable.

Explicit JPEG loading and scaling factories remain eager. JPEG export now uses
the same canonical materialization boundary as other pixel consumers. On
JavaSE, each JPEG factory captures one `EncodedImageSource`, validates its
format, uses its intrinsic metadata, and materializes the eager result from
that same source instead of rereading the path through `SimpleImageInfo` and a
second `Image` construction. Native temporary file and buffer objects are
released on all best-fit paths.

JavaSE and libjpeg use different rounding details for non-integral JPEG scaling;
the implementation uses ceiling-compatible dimensions and tests the boundary
cases. Encoded-bag CRC arithmetic is portable, and scaled dimensions are checked
for overflow before allocation. These corrections are covered by focused
best-fit and native smoke tests.

## Decisions and compatibility

- Source I/O is eager and snapshotted; pixel decode is lazy.
- Encoded storage is immutable and independently owned. Paths, streams, PDB or
  TCZ handles, borrowed arrays, memory maps, and bag aliases are not retained.
- The pipeline field is appended to the existing Image object-field ABI; old
  field indices and public method descriptors are unchanged.
- Canonical barriers resolve at scale one and adopt only complete successful
  results into the original Image object.
- Native code receives materialized Images only; Java traverses pipeline nodes
  and reuses existing native kernels.
- Transform nodes preserve caller order and are not algebraically optimized.
- Destination variants are local to a leaf and bounded to two entries; there is
  no global or unbounded raster cache.
- Reduced JPEG decoding is restricted to the first smooth-downscale shape and
  uses a fixed similarity tolerance rather than byte equality.
- Existing backend-specific no-op, frame, rotation, hardware-scale, and
  explicit-JPEG semantics take precedence over simplification.

## Alternatives discarded

Full decode during structural inspection was rejected because it would defeat
the timing and memory goals and conflate container validity with payload
decodability. Retaining file or stream handles, borrowed native pointers, or
mapped storage was rejected because source lifetime must be independent of the
caller resource.

Native traversal of Java pipeline nodes and a global decoded-image cache were
rejected to keep ABI boundaries explicit and memory bounded. Reduced-resolution
PNG, GIF, and BMP decoding, target-aware nearest or rotation decoding, and
color-before-smooth reordering were rejected because their semantic differences
are not covered by the conservative JPEG contract. Skia sampling changes and
deferred mutable color operations remain separate work.

## Validation and evidence

The focused Java and converter suites passed for encoded-source ownership and
structure, lazy materialization and retry behavior, Image field ABI, deferred
transforms, destination-scale resolution, Graphics integration, and JPEG
best-fit boundaries. The SDK distribution completed successfully with
`./gradlew-agent dist -x test`.

A fresh macOS Release TCVM was built with CMake/Ninja. The Image ABI,
EncodedImageSource, and Image lazy-materialization smokes passed against the
exact runtime. Their machine-readable checks covered source ownership,
metadata-before-decode, barriers, decoder failure classification, transform
equivalence, snapshot isolation, destination-scale cache behavior, JPEG target
decode and fallback, no-clip HiDPI drawing, and native ABI stability; each
reported `overallPass=true`.

Copyright-header validation, new-file size checks, native registration checks,
and `git diff --check` passed. Android, iOS, Linux, Windows, packaging, and the
full platform matrix were not run because their toolchains or release artifacts
were outside the available focused validation scope.

## Known limitations

Reduced JPEG decode is intentionally similarity-based rather than byte
identical. Only the eligible first smooth-downscale pipeline receives reduced
decoder sizing; all other shapes retain full-decode fallback. Deferred mutable
color operations outside the implemented pipeline families remain eager
barriers. Skia sampling-policy behavior is not changed by this feature.

## Conclusion

The feature now provides eager source ownership and validation, lazy and
retry-safe materialization, immutable deferred transforms, destination-aware
drawing, bounded variant lifetime, and conservative JPEG best-fit decoding
while preserving the established SDK/VM compatibility contracts.
