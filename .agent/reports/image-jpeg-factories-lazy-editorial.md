<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Lazy explicit JPEG factory handoff

This corrective pre-Phase-4 milestone is locally and cross-platform complete on
`perf/image-jpeg-factories-lazy`, with one external Android execution blocker.
The frozen Phase-3 final docs tip remains
`224682b15a244201718701ad24be8f9d54b3fa71`, and its retained runtime candidate
is `d2f8195b9faf828f4ee04154c93a93e1136c5bd4`. The lazy-JPEG runtime commit is
`3dd8ccd78d5ed47b6b7a1e5cf1e86b3d8d4fca7c`; the runtime/test candidate is
`b54380800131f036a50879b921b8d30d3af1f154`. The final docs closeout tip is
not created because the physical Android gate remains blocked.

## Architecture

`ImageDecodePolicy` is an immutable package-private value carried by the
`ImagePipeline` root and inherited by appended nodes. Its policy kinds are:

- `TARGET_DECODE` — the existing automatic destination-scale selection;
- `BEST_FIT(targetWidth,targetHeight)` — the inclusive 1/8, 1/4, 1/2, or 1/1
  libjpeg denominator selected from the caller's target;
- `EXPLICIT_RATIO(numerator,denominator)` — the caller's exact positive ratio,
  including arbitrary `3/4`, with ceiling metadata calculated eagerly.

`EncodedImageSource` remains only the owned byte/native-bag source and metadata
authority; per-call policy is not stored on it. The public factories capture
and structurally validate the source, expose dimensions immediately, and leave
pixel decode to the existing Java materialization barriers. JavaSE uses the
existing full decoder plus smooth fallback for arbitrary ratios; deployed code
uses private native bridges that decode from the owned native bag. TCZ and
filesystem path capture now create the native encoded bag directly, avoiding
the deployed Java full-file copy while retaining JavaSE filesystem fallback.
Public native eager factory replacements were removed from `NativeMethods.txt`,
generated registration/prototype files, and `image_Image.c`; private
full/targeted/tiered decode primitives remain, with new private BEST_FIT and
EXPLICIT_RATIO bridges.

## Correctness and exception timing

Focused tests cover BEST_FIT boundaries, explicit ratios `1/8`, `1/2`, `3/4`,
and `1/1`, including an odd `1601x901` filesystem JPEG, real `getPixels()`
barriers, and independent full-decode raster comparison. They also cover policy
distinction, zero decode before use, one materialization at the first barrier,
chained smooth/alpha operations, missing/non-JPEG/overflow failures, and
deterministic corrupted-payload failure caching. Factory calls validate
arguments, path/source capture, JPEG structure, metadata, and overflow.
Payload/decoder failures occur at first materialization; deterministic failures
remain cached while transient failures leave the deferred pipeline retryable.

The converter ABI regression is fixed: `getJpegBestFit` and `getJpegScaled`
remain Java/non-native after conversion, while
`decodeEncodedSourceBestFit` and `decodeEncodedSourceExplicitRatio` convert to
native replacement bridges. Existing Image field-prefix checks remain intact.

The focused deployed macOS smoke passed with:

`bestFitLazy=true, explicitRatioLazy=true, referenceParity=true,
screenDraw=true, repeatedDrawReuse=true, gpuBacking=true,
nativeSourceCapture=true, overallPass=true`

The independent full-decode comparison recorded best-fit max/RMSE
`106/9.698844321709562` and explicit-ratio max/RMSE
`93/5.505494888917498`, within the smoke's documented JPEG envelope.

The focused compact smoke also passed both eligible packaged JPEGs:
RGB565 and GRAY8 canonical backing, one compact decode each, zero temporary
RGBA staging, zero promotions, reuse after the first barrier, and zero
raster-only counters on the validated macOS software path. Its Android
OpenGL path requires the same counters to remain zero.

## Benchmark

The 60-sample macOS benchmark passed all accounting gates:
`factory_only_zero_decode=true`, `first_draw_one_decode=true`,
`repeated_draw_one_decode=true`. Representative final samples were:

`eager_reference=8 ms / decode=1 / materialization=1`,
`lazy_factory_only=1 ms / decode=0 / materialization=0`,
`lazy_first_draw=5 ms / decode=1 / materialization=1`, and
`lazy_repeated_draws=5 ms / decode=1 / materialization=1`.

## Validation and remaining blockers

Passed: focused `totalcross.ui.image.*` tests plus converter/ABI tests,
`git diff --check`, focused copyright-header validation, SDK `dist -x test`,
Release macOS software-Skia CMake/Ninja, the legacy lazy materialization
smoke, the focused JPEG factory screen/GPU smoke, the compact factory smoke,
native registration/prototype/header checks, and the 60-sample accounting
benchmark. The exact candidate also passed hosted run `34209665445` at head
`b54380800131f036a50879b921b8d30d3af1f154` across SDK, macOS, Windows,
Windows-native-legacy, Linux amd64/arm64/arm32v7, Android, and iOS; only the
intentional Linux arm32 cross job was skipped. Android APK/AAB packaging also
passed locally.

The packaged Android smoke APK/AAB was created successfully, but normal
installation on the connected physical device was blocked by
`INSTALL_FAILED_USER_RESTRICTED: Install canceled by user`; therefore Android
GPU execution is not claimed. This is the sole remaining external blocker for
the Phase-4 gate, not a failure of the local implementation or hosted build
matrix.

## Decision

`NO-GO` for Phase 4. Stop at this lazy-JPEG milestone; obtain an installable
Android device session and run the focused production OpenGL smoke before
creating `docs(image): finalize lazy jpeg factory handoff` or changing Phase-4
scope or lifecycle behavior.
