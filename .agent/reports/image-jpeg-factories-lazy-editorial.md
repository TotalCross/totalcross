<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Lazy explicit JPEG factory handoff

This focused pre-Phase-4 milestone is complete on
`perf/image-jpeg-factories-lazy`. The frozen Phase-3 final docs tip remains
`224682b15a244201718701ad24be8f9d54b3fa71`, and its retained runtime candidate
is `d2f8195b9faf828f4ee04154c93a93e1136c5bd4`. This milestone's last runtime
candidate is `4ce4c74d2d6310ff0eb603546c81da28ebf1bc9f`; the final docs tip is
the branch tip after this closeout commit.

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
uses private native bridges that decode from the owned native bag. Public native
eager factory replacements were removed from `NativeMethods.txt`, generated
registration/prototype files, and `image_Image.c`; private full/targeted/tiered
decode primitives remain, with new private BEST_FIT and EXPLICIT_RATIO bridges.

## Correctness and exception timing

Focused tests cover BEST_FIT boundaries, explicit ratios `1/8`, `1/2`, `3/4`,
and `1/1`, policy distinction, zero decode before use, one materialization at
the first barrier, chained smooth/alpha operations, missing/non-JPEG/overflow
failures, and deterministic corrupted-payload failure caching. Factory calls
validate arguments, path/source capture, JPEG structure, metadata, and overflow.
Payload/decoder failures occur at first materialization; deterministic failures
remain cached while transient failures leave the deferred pipeline retryable.

The focused deployed macOS smoke passed with:

`bestFitLazy=true, explicitRatioLazy=true, referenceParity=true,
screenDraw=true, repeatedDrawReuse=true, gpuBacking=true, overallPass=true`

The independent full-decode comparison recorded best-fit max/RMSE
`106/9.698844321709562` and explicit-ratio max/RMSE
`93/5.505494888917498`, within the smoke's documented JPEG envelope.

## Benchmark

The 60-sample macOS benchmark passed all accounting gates:
`factory_only_zero_decode=true`, `first_draw_one_decode=true`,
`repeated_draw_one_decode=true`. Representative final samples were:

`eager_reference=8 ms / decode=1 / materialization=1`,
`lazy_factory_only=1 ms / decode=0 / materialization=0`,
`lazy_first_draw=5 ms / decode=1 / materialization=1`, and
`lazy_repeated_draws=5 ms / decode=1 / materialization=1`.

## Validation and remaining blockers

Passed: focused `totalcross.ui.image.*` tests, `git diff --check`, focused
copyright-header validation, SDK `dist -x test`, Release macOS software-Skia
CMake/Ninja, the legacy lazy materialization smoke, the focused JPEG factory
screen/GPU smoke, native registration checks, and the 60-sample benchmark.
Android native dependency/build/package validation also passed:
`:tcvm:fetchNativeDependencies`, `:tcvm:externalNativeBuildRelease`, and
`:app:assembleStandardRelease`.

The packaged Android smoke APK/AAB was created successfully, but installation
on the connected device was blocked by
`INSTALL_FAILED_USER_RESTRICTED: Install canceled by user`; therefore Android
GPU execution is not claimed. No GitHub Actions run exists for this unpublished
branch/candidate, so the existing cross-platform matrix was not rerun here.
These are the remaining blockers for a Phase-4 start, not failures of the
local implementation or build checks.

## Decision

`NO-GO` for Phase 4. Stop at this lazy-JPEG milestone; run the final candidate
GitHub matrix and an installable Android GPU smoke before changing Phase-4
scope or lifecycle behavior.
