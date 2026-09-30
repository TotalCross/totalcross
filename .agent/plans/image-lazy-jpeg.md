<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Lazy JPEG Factories

## Context

`Image.getJpegBestFit(...)` and `Image.getJpegScaled(...)` provide JPEG-specific
sizing while preserving the requested logical result. A useful factory result
must expose its dimensions and path metadata immediately while delaying pixel
decode until an operation needs pixels. The encoded input must remain usable if
the original file changes or disappears after the factory returns.

## Objectives

- Capture the complete encoded JPEG and required metadata during factory
  invocation.
- Return a deferred `Image` without decoding pixels, allocating the final
  raster, or crossing a native decode bridge.
- Preserve public descriptors, validation, declared exceptions, logical
  dimensions, JPEG tier choices, and failure behavior.
- Materialize from captured bytes through the existing image pipeline and its
  existing pixel barriers.

## Scope

This feature owns JPEG factory behavior and request-specific decode policy. It
reuses `EncodedImageSource`, `ImagePipeline`, existing materialization
requirements, and existing native decode bridges.

It does not change ordinary `new Image(path)` behavior or add a parallel lazy
image implementation.

## Architecture

`EncodedImageSource` owns immutable encoded bytes and parsed source metadata,
including format, intrinsic dimensions, and frame information. It may retain a
compatible decoded backing and its deterministic decode failure. It does not
own a request's target or scale policy, because multiple requests may share the
same encoded source.

The root `ImagePipeline` owns the immutable `ImageDecodePolicy` for one requested
result. Pipeline transforms retain that root policy. `Image.initializeDeferred`
publishes dimensions, frame metadata, content scale, and path metadata before
pixels exist. At materialization, the pipeline combines the source, policy, and
existing `ImageDecodeRequirement` rules, then publishes the resulting backing
through the image's existing backing contract.

## Request-owned decode policy

`ImageDecodePolicy` is package-private and immutable. Its semantic modes are
`TARGET_DECODE`, `BEST_FIT`, and `EXPLICIT_RATIO`.

`TARGET_DECODE` preserves ordinary pipeline target-selection behavior.

`BEST_FIT` stores the requested target width and height, the selected
conservative JPEG denominator, and the exact logical output dimensions. It
preserves the supported 1/8, 1/4, 1/2, and full-resolution selection behavior.

`EXPLICIT_RATIO` stores the validated numerator and denominator, the
conservative native JPEG denominator, and the exact rounded logical dimensions
as independent values. The native denominator must not replace the requested
ratio or alter the public result.

## Source capture and lifetime

Each factory validates its arguments, reads the path once, owns the complete
encoded byte sequence in an `EncodedImageSource`, and parses enough JPEG
structure to establish format and intrinsic dimensions. The input is closed
before the factory returns. The returned image retains the original `path` value
for diagnostics; it retains no live file or stream.

Replacing, modifying, deleting, or making the path inaccessible after return
must not affect later materialization. No post-return operation may reopen the
source path.

## Materialization boundaries

Factory return and metadata access do not decode pixels. Existing operations
that require pixels remain materialization barriers, including drawing,
`getPixels()`, mutation, encoding, equality, and hashing.

Materialization selects the existing full or targeted/tiered JPEG decoder using
the captured source and policy. Any remaining transform produces the exact
logical result. A failure must not publish a partial backing.

## Deployed routing

The public factories execute their Java bodies on deployed targets so they can
capture bytes and construct the deferred pipeline. Native acceleration remains
behind private materialization-time decode bridges that receive the captured
source. No public factory replacement or path-reading native handler may
intercept these methods. Unrelated APIs such as `nativeResizeJpeg` remain intact.

## Compatibility constraints

Keep these descriptors and declared exceptions unchanged:

```java
public static Image getJpegBestFit(String path, int targetWidth, int targetHeight)
    throws java.io.IOException, ImageException

public static Image getJpegScaled(String path, int scaleNumerator, int scaleDenominator)
    throws java.io.IOException, ImageException
```

Preserve argument validation, exception types, best-fit denominator selection,
`jpegScaledDimension` rounding, logical/content-scale metadata, single-frame
semantics, and diagnostic path behavior. Keep existing materialization barriers
and the encoded-source deterministic-versus-transient failure contract.

## Milestone 1 — lazy factory and decode policy

Attach immutable request policy to the root `ImagePipeline`, capture source
bytes before return, and return deferred results with complete logical metadata.

Acceptance checks prove that factories perform no pixel decode before return;
metadata is immediately available; dimensions and JPEG tiers match established
behavior; explicit ratios retain exact rounding; public descriptors are
unchanged; and file replacement or deletion after return does not affect the
captured image.

## Milestone 2 — deployed routing and failure semantics

Remove public native replacements and their stale registrations and
path-reading handlers. Keep private decode bridges for materialization. Verify
full-size and tiered best-fit cases, arbitrary ratios, up/downscaling,
progressive/color/grayscale JPEG inputs when supported by fixtures, repeated
backing reuse, post-materialization mutation, deterministic failure caching,
transient retry, and deployed Java-factory/native-materializer routing.

Acceptance checks prove existing barriers still materialize, no source path is
reopened, deterministic failures are reused, retryable failures remain
retryable, and public methods retain executable Java bodies on deployed targets.

## Validation

Run focused SDK regressions for encoded-source capture, decode requirements,
deferred pipeline behavior, lazy materialization, JPEG scaling, ABI conversion,
and image runtime configuration. Validate artifact contents and build the SDK
distribution with tests excluded after focused tests have passed.

Build the macOS ARM64 Release VM and launcher for deployed JPEG factory,
raster, JPEG pinch/modifier, and runtime-configuration smokes. Verify that the
factory smoke proves zero decode calls before return, captured-source lifetime,
parity, failure caching/retry, Java public routing, and private native decode.

## Risks and tradeoffs

- JPEG decoders support a finite set of native scale denominators; exact public
  dimensions therefore remain separate from the native decode tier.
- Java-side file capture must preserve public path and error behavior across
  deployed platforms.
- A shared encoded source may serve requests with different targets, so decode
  policy must remain request-owned.
- Source decoded-generation changes must continue to invalidate cached
  materialized representations and draw plans through existing generation
  checks.

## Out of scope

P5 does not own authoritative backing or opacity semantics, backing generations,
materialized-variant caching, physical variants, compact storage, asynchronous
prefetch, PNG prefetch, or a new diagnostics domain. Those behaviors remain in
their existing shared image architecture.
