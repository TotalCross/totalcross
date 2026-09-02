<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Plan: eliminate `Image4D` and unify `Image`

## Motivation

The SDK currently has a JavaSE-facing `Image` implementation and a separate
`Image4D` deployment replacement. That split duplicates image behavior and
lets the Java class shape, native runtime shape, and deployed behavior drift
apart. The replacement also makes the public API boundary and deployment
pipeline harder to reason about.

## Objective

Make `totalcross.ui.image.Image` the single SDK and deployed image class,
remove `Image4D`, and preserve the observable behavior of image loading,
drawing, transformations, scaling, copying, encoding, and decoding across the
supported JavaSE and deployed runtimes.

## ABI and invariants

The native VM addresses `Image` instance fields by storage category and index.
The unified class must retain the native field prefix and field names:

- I32 fields: `surfaceType`, `width`, `height`, `frameCount`, `currentFrame`,
  `widthOfAllFrames`, `transparentColor`, `useAlpha`, `alphaMask`,
  `lastAccess`, `textureId`, `logicalWidth`, `logicalHeight`.
- Object fields: `pixels`, `pixelsOfAllFrames`, `comment`, `gfx`, `changed`,
  `instanceCount`.
- Value64 fields: `hwScaleW`, `hwScaleH`, `contentScale`.

The class must retain the empty constructor needed by native object creation,
the native method signatures and replacement annotations, shared state for
derived instances, and the cleanup semantics used by hardware-accelerated
images. New or moved fields must not precede these ABI-sensitive fields.

## Strategy

1. Add a converter-level baseline test that locks the native field categories,
   order, and names before changing the implementation.
2. Reorder and complete `Image` state so the class is directly deployable,
   including native creation, stream/byte-array parsing bridges, cleanup,
   locking, and derived-instance state.
3. Move the deployed transformation entry points and their JavaSE fallbacks
   into `Image`, preserving native transformations and the historical argument
   contract.
4. Preserve logical dimensions and hardware scale when creating copies or
   instances, and preserve the final `fillColor` behavior for rotations.
5. Keep JavaSE-only image reading isolated from deployed conversion and remove
   the replacement class and its stale references.
6. Add converter, native ABI, smoke, transformation, scaling, encoding/decoding,
   and public-artifact boundary coverage.

## Behavior to preserve

- Native image loading, parsing, frame selection, pixel access, color changes,
  texture management, JPEG creation, and equality behavior.
- JavaSE loading and image manipulation when the native replacement is not in
  use.
- Replicate, smooth, rotated, faded, alpha, and touched-up transformations,
  including multi-frame images and the native bridge.
- Hardware scale and logical/content scale in copies and derived instances.
- Graphics creation without introducing a `Launcher` dependency into
  `Image.getGraphics`.
- Deployment of `Image` as the class named by the native runtime, with no
  public `Image4D` class or other accidental 4D implementation exposure.

## Risks and mitigations

Field reordering can silently corrupt native state, so the converter test and
native field checks must run before and after the migration. JavaSE-only reader
code can leak into deployed output, so it will be isolated behind a deploy-time
replacement boundary. Transformation argument ordering and output geometry can
change subtly, so native smoke assertions and focused image tests will cover
the bridge. Copy paths can lose scale metadata, so derived-image tests will
assert both dimensions and scale state.

## Validation plan

Run focused converter and image unit tests, native field-ABI tests, SDK build
and deployment checks, transformation and scaling tests, encode/decode tests
where supported, a deployed native macOS image smoke test, and artifact/public
API boundary checks. Search production and source trees for stale `Image4D`
references and inspect the generated artifact for unintended 4D classes.

## Success criteria

The native runtime and converter agree on the unified `Image` ABI; `Image4D`
is absent from production sources and public artifacts; all preserved image
behaviors and focused tests remain valid; the affected SDK and native targets
build; and the final change can be reviewed as one implementation with tests
that explain its compatibility constraints.
