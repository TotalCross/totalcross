<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Implement the Image lazy-decode pipeline

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`.

## Purpose / Big Picture

Make encoded `Image` construction cheap in memory and predictable in timing:
read and validate the complete source immediately, expose its metadata without
decoding pixels, and materialize pixels only when a pixel-dependent operation
needs them. Keep writable images eager, preserve public APIs and native field
ABIs, and let deferred draws resolve transformed images at the destination
content scale.

The feature must preserve the supported format matrix and existing visual
semantics. JavaSE continues to support PNG, JPEG, GIF, and BMP; deployed TCVM
continues to support PNG and JPEG. Existing eager image kernels remain the
implementation of actual pixel work.

## Problem and Goals

The existing image path combines source I/O, encoded parsing, pixel allocation,
decoding, and transformation. A constructor therefore allocates a full raster
even when an image is never drawn, and a transformed encoded image may decode a
large source before producing a small destination result. Native entry points
also assume that `Image_pixels` is available.

This feature separates those responsibilities:

- consume and own encoded input during construction;
- inspect container structure and metadata without producing pixels;
- defer decode and transform work behind explicit pixel barriers;
- preserve same-object canonical materialization and public exception behavior;
- resolve deferred geometric results for 1x, 2x, and other valid destination
  scales without mutating the source image;
- use conservative target-aware JPEG decoding only where operation semantics
  permit it;
- bound retained draw variants and native resources.

Out of scope are new public image APIs, asynchronous or GPU decoding, global
image caches, reduced-resolution PNG/GIF/BMP decoding, Skia sampling policy
changes, and deferred mutable color operations that are not represented by the
pipeline families defined below.

## Working Set and Resume Protocol

The implementation is concentrated in:

- `TotalCrossSDK/src/main/java/totalcross/ui/image/` for source objects,
  pipeline resolution, and Image behavior;
- `TotalCrossSDK/src/main/java/totalcross/ui/gfx/Graphics.java` for destination
  scale and source-image drawing boundaries;
- `TotalCrossVM/src/nm/ui/image/` and `ui/image_Image.c` for native bags,
  decoding, and native barriers;
- `TotalCrossVM/third_party/jpeg/` and `third_party/png/` for loader status and
  memory-source behavior;
- `TotalCrossSDK/src/test/java/`, `src/smokeTest/java/`, and `build.gradle`
  for unit, converter, smoke, and build wiring.

Keep Java and native source metadata contracts synchronized. When resuming a
partially implemented slice, inspect the active Image, pipeline, source, native
registration, and test paths together before changing an ABI-sensitive field or
bridge.

## Current Architecture and Scope

### Encoded source ownership

Introduce an internal source model:

    ImageSource
      ├── EncodedImageSource
      └── RasterImageSource

`EncodedImageSource` is immutable after capture except for an internal cached
deterministic decode failure. It stores format, encoded length, intrinsic and
logical dimensions, frame count, comment/frame metadata, and the owned source
storage. JavaSE stores an owned byte array. Deployed TCVM stores one native
`ImageEncodedBag` containing an independent byte copy.

Path input is read completely during construction. Stream input is consumed to
the existing end-of-source boundary and the Stream reference is discarded.
Public byte-array input is copied over exactly the visible range. PDB loading
may reuse the established Stream flow but must not retain the PDB object. A
deployed path capture preserves TCZ-first lookup and closes the TCZ handle on
every success and failure path.

The native bag owns only its byte pointer and length. It never borrows a file,
TCZ, Stream, VM array, or mapped range. Release is idempotent and occurs once
the owning source is finalized; decoders may borrow the bytes only during a
synchronous call.

### Decode-free structural inspection

`ImageEncodedStructure` and its native counterpart identify and validate the
complete supported container structure without inflating compressed data,
running entropy or LZW decoding, allocating a raster, or calling a pixel
loader.

PNG inspection requires the signature, first and unique `IHDR`, positive
dimensions, supported color/depth combinations, legal critical-chunk ordering,
valid palette/transparency data, ordered `IDAT` chunks, valid CRCs for every
chunk, and a terminal `IEND`. It extracts `Comment` and `FC=` metadata. A
structurally valid but corrupt deflate stream may pass inspection and fail only
when pixels are materialized.

JPEG inspection walks marker and segment framing, fill bytes, byte stuffing,
restart markers, progressive scans, supported positive-dimension SOF markers,
SOS presence, and reachable EOI without interpreting entropy data. Deployed
validation additionally uses `jpeg_mem_src` and `jpeg_read_header` only.

JavaSE GIF inspection walks logical-screen data, color tables, extensions,
sub-blocks, image descriptors, LZW code-size bounds, frame count, and trailer
without expanding pixels. JavaSE BMP inspection validates the supported DIB,
planes, dimensions, depths, compression, palette, offsets, payload bounds, and
RLE framing without rendering scanlines.

### Image states and barriers

Append exactly one `ImagePipeline` reference at the end of the existing Image
object-field ABI category. Do not reorder or remove any existing field.

An ordinary raster Image has `pipeline == null` and pixel storage. A deferred
encoded Image has a pipeline root, metadata, no pixel arrays, and no texture.
Writable and logical raster constructors remain eager.

`materializeCanonicalChecked()` resolves at scale 1 into a temporary complete
Image, verifies metadata, and atomically adopts pixels and state into the same
Image object. `materializeCanonicalUnchecked()` wraps checked failures in the
existing unchecked compatibility boundary. A failed attempt leaves the source
Image deferred and unchanged.

Metadata-only accessors such as dimensions, content scale, path, frame count,
and comments must not decode. Every operation that reads, writes, exports,
hashes, compares, uploads, or passes pixel storage to native code must first
materialize. This includes pixel access, Graphics creation, frame operations,
color/mutation methods, exports, equality/hash helpers, and native wrappers.

Java wrappers delegate to private native bridges after materialization. Native
code must never traverse a pipeline or dereference pixels from a deferred
Image. Preserve public descriptors, checked exception declarations, and all
existing Image field prefixes.

Deterministic encoded-payload corruption is cached on the immutable source so
repeated attempts fail consistently. Out-of-memory, allocation, decoder setup,
and other transient resource failures remain retryable. Nonfatal libpng
warnings remain compatible, while incomplete decoded rows are rejected before
publishing pixels.

### Deferred transforms

Represent these result-producing operations as immutable linked nodes:

- nearest scale;
- smooth scale;
- rotation/scale with angle and fill color;
- touch-up;
- fade;
- alpha adjustment.

Encoded roots are shared without decoding or copying their storage. A
materialized mutable root is deep-snapshotted once into `RasterImageSource`,
including single- and multi-frame pixels, dimensions, frame metadata, comments,
source path, transparency, alpha, hardware-scale, and content-scale state. The
snapshot shares no mutable pixels, texture, Graphics instance, or sharing
bookkeeping.

Nodes preserve caller order exactly. Do not fuse, reorder, discard, or
algebraically simplify operations. Existing Java and native eager kernels are
called only with materialized inputs. Canonical pipeline resolution walks from
root to leaf at scale 1 and adopts the result through the normal barrier.

No-op and invalid-argument identity behavior remains backend-compatible. The
historical native rotation bridge parameter ordering remains unchanged.

### Destination-scale resolution

Graphics source-image boundaries resolve a deferred image using the destination
`contentScale`, without adopting the returned variant into the original Image.
An ordinary materialized image is passed through unchanged, even on a HiDPI
destination.

Geometric nodes produce physical dimensions with checked `ceil(logical * scale)`
arithmetic and retain logical dimensions separately. Rotation uses existing
logical bounds and scales physical geometry consistently. Color-only nodes
preserve their input raster scale and remain in caller order. Hardware scale is
presentation state, not pipeline raster sizing.

Each leaf pipeline owns at most two exact destination-scale variants with LRU
recency. A cache miss resolves from the authoritative encoded source or raster
snapshot. Failed resolutions are not cached. Eviction and canonical adoption
release textures without forcing materialization; discarded CPU variants become
eligible for normal garbage collection. A color-only pipeline normalizes its
cache key because its raster does not change with destination scale.

### Conservative JPEG target decode

Only an encoded JPEG whose first operation is a smooth downscale is eligible for
target-aware decode. The requested physical output must be smaller than the
intrinsic image in both dimensions and fit the useful decoder reduction rule.
Nearest scaling, rotation-first, color-before-smooth, direct JPEG, and all
non-JPEG formats use full decode fallback.

JavaSE uses ImageIO source subsampling followed by the existing smooth kernel.
TCVM reuses the existing libjpeg target-sizing/best-fit machinery over the
native bag. The result has exact requested physical dimensions and preserves
logical dimensions and content scale. Reduced JPEG results use a documented
similarity tolerance rather than byte equality.

## Plan of Work

### Milestone 1 — Source capture and structural validation

Add Java source capture/inspection, native bag allocation/copy/release,
metadata parity, native registration, converter expectations, and focused unit
tests. Prove ownership isolation, path/Stream snapshot behavior, PNG/JPEG
structural rejection, no decode during inspection, and JavaSE GIF/BMP behavior.

Acceptance: encoded sources are fully captured and validated while constructors
remain eager to decode; no public Image constructor behavior changes yet.

### Milestone 2 — Lazy materialization and lifecycle

Add the pipeline root and deferred constructor initialization. Implement
canonical checked/unchecked materialization, temporary decode, atomic adoption,
metadata-only access, all Java pixel barriers, and private native bridge wrappers.
Keep explicit JPEG factories and writable images eager. Add retryable resource
status handling, multi-frame allocation coverage, export coverage, ABI tests,
and deployed macOS lazy-materialization smoke checks.

Acceptance: encoded construction has eager source I/O and lazy raster decode;
the first pixel barrier materializes exactly once on success, native code never
receives deferred null-pixel state, and failures preserve retry semantics.

### Milestone 3 — Deferred transforms

Separate eager kernels from public methods, add immutable operation nodes and
detached raster snapshots, route the six transform families through the
pipeline, and preserve frame, fill-color, no-op, hash, and ABI behavior. Add
Java equivalence tests and deployed smoke coverage for chained transforms,
sharing, snapshot isolation, and canonical barriers.

Acceptance: encoded transform chains remain undecoded until a barrier or draw;
materialized-source mutation after derivation cannot affect the result; all
operations resolve in caller order at canonical scale.

### Milestone 4 — Destination scale and JPEG optimization

Implement scale-aware resolution, physical-dimension overflow checks,
presentation-state synchronization, the two-entry leaf cache, texture-only
release, Graphics integration, and internal resolver coverage. Then add the
eligible JPEG target-decode path, explicit fallback tests, best-fit rounding,
native temporary cleanup, source reuse after eviction, and reduced-image
similarity coverage.

Acceptance: deferred geometric results resolve correctly at multiple
destination scales without changing the source Image; canonical barriers still
adopt scale 1; eligible JPEG downscales reduce decode work while all ineligible
shapes retain full-decode semantics.

### Milestone 5 — Integration and compatibility gate

Run the relevant SDK unit/converter tests, SDK distribution, fresh macOS Release
TCVM build, Image ABI smoke, encoded-source smoke, lazy-materialization smoke,
and destination/JPEG smoke. Validate copyright headers, file sizes, native
method registration, field indices, and whitespace. Inspect all supported
platform implications before release; platform builds not available in the
active environment remain explicit limitations.

## Decision Log

- Source capture is eager and pixel decode is lazy; construction-time
  structural validation does not guarantee compressed-payload decodability.
- Encoded storage is immutable and independently owned. No path, Stream, PDB,
  TCZ, borrowed array, memory map, or native bag alias is retained.
- The Image field ABI is append-only for the pipeline reference. Public method
  descriptors and checked exception contracts do not change.
- Canonical materialization always resolves at scale 1 and adopts into the same
  Image object only after complete success.
- Native code receives only materialized Images; Java owns pipeline traversal and
  invokes existing native kernels through private bridges.
- Transform nodes are immutable, ordered, and unoptimized. Mutable raster roots
  use deep snapshots; encoded roots are shared.
- Destination-scale variants are local to a pipeline leaf and bounded to two
  exact keys. There is no global or source-level raster cache.
- Only a first-node smooth JPEG downscale may use target-aware decode; all other
  shapes and formats use full decode fallback.
- Deterministic payload errors may be cached, but transient allocation,
  infrastructure, and resource failures must remain retryable.
- Existing backend-specific no-op, frame, rotation, hardware-scale, and
  explicit-JPEG semantics take precedence over simplification.

## Validation and Acceptance

Use focused validation at each milestone and escalate only as the affected ABI
and platform risk requires:

- Java unit tests for source ownership, structure, lazy barriers, transforms,
  destination resolution, JPEG best-fit, and failure retry/caching;
- converter and Image field ABI tests for every new private/native bridge;
- `./gradlew-agent dist -x test --no-daemon --console=plain` for distributable
  SDK wiring;
- a fresh Release Ninja `tcvm` build on macOS;
- exact-runtime Image ABI, encoded-source, lazy-materialization, and
  destination/JPEG smokes with machine-readable `overallPass=true`;
- focused copyright-header validation, new-file size checks, and
  `git diff --check`.

Acceptance requires metadata-only calls to remain decode-free, source snapshots
to survive caller/path mutation, all pixel barriers to be safe, ABI prefixes to
remain unchanged, transform order to be preserved, destination variants to be
bounded, and the eligible JPEG path to meet its dimension and similarity
contracts. Android, iOS, Linux, Windows, packaging, and full matrix validation
are separate release work unless their toolchains are available and the feature
requires them.

## Risks and Open Questions

The highest risks are native ABI drift, partial adoption after a failed decode,
incorrect multi-frame dimensions, cache variants retaining textures, and
rounding differences between JavaSE and libjpeg. Resolve these with field-prefix
tests, atomic temporary state, metadata parity checks, explicit resource status,
and boundary fixtures.

Do not redesign the feature to solve unrelated rendering policy, add a new
public API, broaden decoder support, or introduce an unbounded cache. Any
backend-specific visual difference outside the documented reduced-JPEG
tolerance requires a focused compatibility decision.

## Idempotence and Recovery

Source capture must be repeatable without reopening a caller-owned resource.
Materialization may be retried after transient failure because the deferred
source remains authoritative. Cache eviction may release textures repeatedly;
all release bridges must tolerate an already-released resource. Canonical
adoption clears the pipeline only after the resolved state is complete.

## Outcomes & Retrospective

At completion, record delivered behavior, supported platforms, validation
results, known limitations, and any follow-up work in
`.agent/reports/image-lazy-decode-report.md`. Distinguish measured behavior
from estimates and document any deliberate tolerance or fallback.

## Revision Note

This plan consolidates the encoded-source, lazy-materialization,
deferred-transform, and destination-scale specifications into one feature-level
implementation plan.
