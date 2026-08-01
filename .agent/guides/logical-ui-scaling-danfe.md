<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# DANFE end-to-end validation fixture

Read this file only when creating or running the final document-rendering proof.
The fixture must be implemented from current master and this specification, not
copied from source changes created by earlier plans.

## Purpose

The fixture proves the original failure mode: text drawn into a default image must
not inherit screen density. It also proves that the broader logical-unit API does
not fix the issue by merely shrinking text or by breaking shapes, barcodes, pixel
synchronization, or exports.

## Document variants

Render at least these variants with the same logical drawing commands:

1. default fixed-pixel image:

       new Image(360, 540)

   Expected logical size: `360 x 540`.
   Expected physical size and PNG: `360 x 540`.

2. explicit logical scale-2 image:

       Image.createLogical(360, 540, 2)

   Expected logical size: `360 x 540`.
   Expected physical size and PNG: `720 x 1080`.

3. default image while the application window runs at scale `1`.

4. default image while the application window runs at scale `2` or the actual
   Retina/high-density scale.

The two default-image exports must be identical when renderer, fonts, locale, and
fixture data are the same.

## Required content

Use deterministic, non-private sample values. Include:

- a DANFE title and document identifier;
- issuer and recipient blocks;
- accented Portuguese labels and values;
- a long company name that approaches its assigned width;
- address, city, state, postal code, and tax identifiers;
- a product table with several rows;
- quantities, unit prices, totals, and decimal punctuation;
- bold and regular text;
- horizontal and vertical rules;
- outlined rectangles and filled headers;
- a footer close to the lower document edge;
- a deterministic barcode containing exactly 31 dark runs.

Do not use real customer, company, invoice, tax, email, phone, or address data.

Keep each fixture or helper file below 20 KiB and approximately 600 lines. Split
data, renderer, barcode analysis, and assertions into separate focused files when
needed.

## Layout rules

All fixture geometry is logical. The default image's scale `1` makes one logical
unit equal one pixel. The scale-2 variant uses the same logical coordinates under
a larger backing buffer.

Use fixed logical font sizes approved in the fixture. Do not dynamically reduce
font size merely to satisfy containment. Where wrapping is intended, specify the
maximum logical width and expected line count or range.

Save key rectangles in fixture metadata so automated tests can compare text
metrics and painted bounds against their assigned regions.

## Automated assertions

### Dimensions

Assert:

    default.getWidth() == 360
    default.getHeight() == 540
    default.getPixelWidth() == 360
    default.getPixelHeight() == 540

For scale 2:

    logical width and height == 360 x 540
    physical width and height == 720 x 1080

Decode exported PNGs independently and assert their physical dimensions.

### Text containment

For each critical text block, assert shaped logical advance, line height, line
count, baseline positions, and logical ink bounds against its assigned rectangle.

Required critical blocks:

- title;
- issuer name;
- recipient name;
- long product description;
- totals;
- footer.

Allow a small approved margin for antialiasing. Fail when text crosses its box,
the footer leaves the image, or baselines overlap.

### Anti-over-shrinking checks

Containment alone is not sufficient. Record approved ranges for:

- title glyph height;
- regular body glyph height;
- bold body glyph height;
- known string advance;
- line height;
- bold-to-regular advance or weight distinction.

The fixed output must remain visually comparable to the reference design. A patch
that reduces all font sizes to fit must fail these ranges.

### Barcode structure

Generate deterministic bars with exactly 31 dark runs along the selected scan
line. Decode or analyze the exported pixel row and assert:

- 31 dark runs;
- expected quiet zones;
- no clipped first or last run;
- scale-2 output preserves the same logical run structure;
- alpha and background values remain correct.

Do not rely only on visual inspection.

### Density independence

Render the default image with application/window `contentScale` values `1` and
`2` or the closest available high-density value. Assert:

- PNG dimensions remain `360 x 540`;
- logical metrics are equal;
- physical pixels and PNG hash are equal within the same renderer and font set;
- no font size reads the window's scale through a global setting.

### Scaled-image behavior

Render the same fixture into the scale-2 logical image. Assert:

- logical metrics and line wrapping match the default image;
- physical output doubles in each dimension;
- selected logical coordinates map to expected physical positions;
- no shape or text is scaled twice.

### Renderer equivalence

Run the semantic fixture against Skia and Java. Require equal:

- integer FontMetrics compatibility values;
- preferred component sizes used by any fixture UI;
- logical line counts and approved wrapping points;
- barcode run count;
- logical containment;
- image logical and physical dimensions.

Permit documented small double-metric tolerance and antialiasing differences.
After the non-Skia native path is implemented, run the same assertions there.

### Pixel synchronization

Before export, exercise both directions:

- modify a known Java pixel and ensure it reaches the exported/native image;
- draw a known native/Skia mark and ensure Java readback sees it;
- repeat synchronization and ensure neither change is lost;
- include an alpha-128 pixel;
- use an odd-width auxiliary image to test row pitch.

## macOS run

Launch the fixture in a real TotalCross window on macOS. Record sanitized metadata:

    commit
    renderer
    macOS and Java versions
    logical window size
    physical framebuffer size
    contentScale
    fontScale
    default PNG hash
    scale-2 PNG hash

Capture only the application window according to the screenshot guide. Include a
side-by-side comparison generated from the two DANFE PNGs rather than exposing the
desktop.

## Android run

Run the same fixture on at least one Android device or emulator with density above
`1`. Record:

    device or emulator profile
    Android version
    density/contentScale
    renderer
    commit
    PNG dimensions and hashes
    assertion summary

Copy the generated PNG through a documented, non-destructive path. Do not rely on
a desktop re-render as Android proof.

## Human visual review

Create sanitized comparisons for:

- approved reference versus fixed default image;
- scale-1 window versus scale-2 window default export;
- default image versus scale-2 logical image, displayed at equal logical size;
- application window on macOS Retina.

Review:

- text size and clarity;
- baseline alignment;
- bold distinction;
- clipping and overdraw;
- footer position;
- barcode integrity;
- shape and text alignment.

Record a factual conclusion. Do not call the output equivalent solely because all
text is contained.

## Evidence package

Place under:

    artifacts/logical-ui-scaling/danfe/

Include:

    default-scale1.png
    default-scale2-window.png
    logical-image-scale2.png
    comparison-default-density.png
    comparison-reference.png
    danfe-assertions.json
    danfe-run-metadata.json
    concise test logs

Names may change, but metadata must map each artifact to commit, platform,
renderer, logical size, physical size, contentScale, fontScale, and hash.

Run the privacy and integrity checks before recording final hashes.
