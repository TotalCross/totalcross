<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Scaled image design

Read this file before Milestone 4. It defines how `Image`, its backing buffer,
codecs, and its `Graphics` separate logical dimensions from physical pixels.

## Core model

An image has:

    logicalWidth
    logicalHeight
    pixelWidth
    pixelHeight
    contentScale

The scale is immutable and positive. Physical dimensions are:

    pixelWidth = ceil(logicalWidth * contentScale)
    pixelHeight = ceil(logicalHeight * contentScale)

The implementation may preserve existing field positions required by native ABI,
but public and internal names must make unit ownership clear. Add bridge fields or
native accessors rather than repurposing an ABI-sensitive field without auditing
native offsets.

## Construction

The existing constructor remains fixed-pixel:

    new Image(width, height)

It creates:

    logicalWidth = width
    logicalHeight = height
    pixelWidth = width
    pixelHeight = height
    contentScale = 1

Ordinary PNG, JPEG, GIF, or BMP loading also defaults to scale `1`.

Add an explicit factory or constructor whose name makes logical dimensions clear,
for example:

    Image.createLogical(width, height, contentScale)

A scale-2 logical `360 x 540` image allocates a `720 x 1080` buffer. Reject
non-positive, NaN, or infinite scale. Reject non-positive or overflowing physical
dimensions using the existing `ImageException` style.

Do not add a mutating scale setter. Changing scale would require reallocating the
buffer, invalidating frames, graphics, textures, native surfaces, and caches.

## Public dimensions

`getWidth()` and `getHeight()` return logical dimensions. Add:

    int getPixelWidth();
    int getPixelHeight();
    double getContentScale();

If the existing API exposes public or protected width and height fields, preserve
ABI layout while ensuring public behavior and internal callers use the correct
unit. Add internal physical getters rather than allowing codecs or pixel loops to
use logical width accidentally.

`getPixels()` and any pixel-array API return the physical buffer. Its length is
`pixelWidth * pixelHeight` per frame. Document that callers operating on this
array must use physical dimensions and pitch.

## Graphics behavior

`Image.getGraphics()` returns a `Graphics` whose logical surface size is
`logicalWidth x logicalHeight` and whose `contentScale` is the image scale. The
backend allocates and targets a physical surface of `pixelWidth x pixelHeight`.

Every logical drawing operation, including text, shapes, clips, translations,
stroke widths, and image destinations, is transformed by the image scale. Raw
pixel reads, writes, source rectangles, and codec row pitch remain physical.

A default scale-1 image therefore preserves current fixed-pixel document behavior.
A screen cache must be created explicitly with the window scale; it must not
silently inherit a global screen density.

## Drawing an image

Drawing an image without an explicit destination size uses its logical dimensions.
A scale-2 logical `100 x 50` image naturally occupies `100 x 50` logical units on
the destination, regardless of its `200 x 100` backing buffer.

When a source rectangle is expressed as pixels, its API and documentation must say
so. If an existing public method ambiguously uses source coordinates, preserve its
current behavior for compatibility and add an internal explicit physical path.
Do not reinterpret encoded image data without a focused compatibility decision.

Sampling chooses from the physical buffer and maps to the logical destination.
Avoid treating `contentScale` as visual zoom; it is backing resolution.

## Export and codecs

PNG and JPEG encode the physical backing buffer and therefore export
`pixelWidth x pixelHeight`. A default `360 x 540` image exports `360 x 540`. A
logical scale-2 image exports `720 x 1080`.

File formats do not automatically restore TotalCross logical scale unless a
specific resource convention is designed and tested. Images loaded from ordinary
files use scale `1`.

All codec allocation, row stride, frame stride, CRC, compression, and native image
descriptors use physical dimensions. Audit every multiplication by width or
height.

## Frames and transformations

For multiframe images, every frame shares one physical frame width, height, and
scale. Frame arrays and offsets use physical dimensions. Logical dimensions
describe one visible frame.

Scaling, rotation, smooth resizing, nine-patch processing, hardware scaling, and
frame extraction must state whether they produce:

- a new fixed-pixel image with scale `1`;
- a new logical image preserving scale;
- a visual transform only.

Preserve existing behavior unless it conflicts with the new public contract.
Add focused tests for methods that currently depend on width/height fields.

`hwScaleW` and `hwScaleH` remain visual display transforms and must not be confused
with `contentScale`.

## Java/native synchronization

Before editing `applyChanges()` or native synchronization, map current direction:

- Java pixel array to native/Skia surface upload;
- native/Skia drawing to Java pixel array readback;
- dirty ownership after either side changes;
- failure behavior and return status.

Do not make synchronization unconditional in both directions. Use explicit
operations or tracked ownership. A valid design may include internal methods such
as:

    uploadPixelsIfDirty()
    readPixelsIfDirty()

Names are illustrative. Public compatibility can continue through existing
methods if their documented direction is preserved.

Do not clear dirty state when upload or readback fails. Preserve alpha exactly,
including partial alpha such as `128`.

## Pixel-boundary audit

Audit at least:

- constructors and loaders;
- `getGraphics`;
- `getPixels`;
- `applyChanges`;
- native surface creation and deletion;
- PNG/JPEG/GIF/BMP parsing and export;
- frame arrays and frame selection;
- texture upload;
- scaling and rotation methods;
- screenshots and offscreen control caches;
- nine-patch and image controls;
- JavaSE `BufferedImage` conversion;
- native copy, pitch, and row loops.

For each caller, record whether it consumes logical or physical dimensions. Add a
small helper or explicit getter rather than repeating conversions.

## Required tests

Test default scale `1` and explicit scales `1.5`, `2`, and `3`.

Use physical widths `1`, `3`, `7`, and `360` to catch pitch and rounding errors.
Test transparent, opaque, and alpha-128 pixels. Cover:

- constructor and factory dimensions;
- natural-size drawing;
- explicit destination drawing;
- export dimensions;
- Java pixel edit followed by drawing and export;
- Skia drawing followed by pixel readback and export;
- repeated upload/readback;
- alternating Java and native ownership;
- failed synchronization;
- multiframe offsets;
- loaded-file scale `1`;
- screen-cache construction with window scale.

The default image output must not change when only the display scale changes.
An explicitly scaled image must preserve logical placement while increasing
physical resolution.
