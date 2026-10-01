<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Lazy JPEG Factories — Implementation Report

## Summary

`Image.getJpegBestFit(...)` and `Image.getJpegScaled(...)` return deferred
images. Each factory captures the encoded JPEG and required metadata before
returning; pixel decode happens later when an existing image operation requires
pixels. Materialization uses the existing `ImagePipeline` and preserves exact
public dimensions and JPEG quality rules.

## Public compatibility

Both public descriptors and declared exceptions are unchanged:

```java
public static Image getJpegBestFit(String path, int targetWidth, int targetHeight)
    throws java.io.IOException, ImageException

public static Image getJpegScaled(String path, int scaleNumerator, int scaleDenominator)
    throws java.io.IOException, ImageException
```

Argument checks, exception behavior, best-fit tier selection, explicit-ratio
rounding, frame and content-scale metadata, and the diagnostic `path` field are
preserved. Ordinary `new Image(path)` behavior is unchanged.

## Request-owned decode policy

Package-private immutable `ImageDecodePolicy` is owned by the root
`ImagePipeline`, not by `EncodedImageSource`. Its semantic modes are
`TARGET_DECODE`, `BEST_FIT`, and `EXPLICIT_RATIO`.

`BEST_FIT` stores the requested target width and height, the selected native
JPEG denominator, and the exact logical output dimensions.

`EXPLICIT_RATIO` stores the requested numerator and denominator, the
conservative native JPEG denominator, and the exact rounded logical output
dimensions independently. Tiered native decode minimizes source work; the
remaining transform preserves the exact public result.

## Source capture and lifetime

Each factory opens and reads the path once. The complete encoded byte sequence
and required source metadata are owned by `EncodedImageSource` before return,
and the input is closed before return. Replacing or deleting the original file
after return does not change the image. Later decode and materialization never
reopen the source path. The retained `path` is metadata for diagnostics only.

## Materialization behavior

The factories return with dimensions and logical metadata available but without
pixel decode, final raster allocation, or a native decode call. Drawing,
`getPixels()`, mutation, encoding, equality, hashing, and other existing pixel
barriers materialize from the captured bytes using the recorded policy.

Best-fit preserves its selected 1/8, 1/4, 1/2, or full-resolution tier.
Explicit ratios preserve the requested ratio's exact rounded dimensions,
regardless of the conservative native tier. JPEG results retain opaque pixel
semantics.

## Integration with authoritative backing

The implementation uses the existing single authoritative `ImageBacking` and
its transactional publication contract. Image backing mutation generation is
independent of the encoded source's decoded generation. Backing opacity metadata
is preserved. When an image shares a decoded backing with an encoded source,
mutation detaches the image before changing pixels. Native `getPixels()` returns
a detached readback copy.

## Integration with raster variants

`ImagePipeline` continues to combine the request-owned decode policy with the
existing raster-variant behavior:

- one Java fallback materialized-variant slot;
- one pending exact candidate admitted on its second observation;
- an independent two-entry `ImageDrawPlan` cache.

P5 adds no second variant cache and does not restore the old two-slot first-use
materialized cache. Lazy JPEG decode updates the source decoded generation
through the existing encoded-source contract. P3 generation checks naturally
invalidate stale plans and materialized representations.

## Failure semantics

Deterministic corrupt or invalid encoded-source failures may be cached and reused.
Transient native failures, injected retryable failures, and retryable allocation
failures remain retryable and are not permanently cached. `OutOfMemoryError`
remains outside deterministic failure caching. No partial backing is published
when decode or materialization fails.

## Native/deployed routing

The public JPEG factories execute their Java bodies on deployed targets; they
are no longer native replacements. Stale public factory declarations,
registrations, prototypes, and path-reading handlers are removed. Private
materialization/decode bridges remain available and consume the captured
encoded source. Unrelated JPEG APIs such as `nativeResizeJpeg` remain intact.

## Validation

- Focused SDK image/JPEG/ABI/runtime-configuration regressions: 165 tests
  across 20 suites; 0 failures, errors, or skips.
- `artifactContentTest`: 13 tests passed.
- `dist -x test`: passed.
- macOS ARM64 Release `tcvm` and `Launcher` built successfully.
- Deployed lazy JPEG factory smoke passed lazy-return, later materialization,
  replacement/deletion lifetime, parity, deterministic failure caching,
  transient retry, Java factory routing, and private native decode checks.
- Raster-core, JPEG pinch/modifier, and six image runtime-configuration smokes
  passed. The SDK test task run through the smoke build passed 464 tests across
  92 suites, with 10 skipped.
- The post-rewrite GitHub Merge Flow passed all enabled SDK, macOS ARM64,
  Windows, legacy Windows, Linux, Android, and iOS jobs. The configured
  `linux-arm32v7-cross` job was skipped.
- Copyright headers and `git diff --check` passed.

## Known limitations

The deployed lazy-factory smoke specifically exercises macOS ARM64. WinCE was
not built. The test and build matrix does not claim WinCE deployment coverage.

## Deferred work

P5 adds no backing/storage or opacity policy, materialized-variant behavior,
physical variants, compact storage, asynchronous prefetch, PNG prefetch, or new
diagnostics. Those responsibilities remain with their existing image
architecture or separate feature work.
