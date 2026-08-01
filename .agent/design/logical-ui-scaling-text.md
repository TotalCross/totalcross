<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Logical text and FontMetrics design

Read this file before Milestone 3. It defines shaping, metrics, compatibility,
control sizing, renderer equivalence, and cache ownership.

## Font model

`Font` is a scale-independent logical descriptor. Its size is a logical text size.
It does not store screen density or image scale. The same `Font` can be drawn into
a scale-1 image and a scale-2 window simultaneously.

The effective logical size is:

    Font.size * Graphics.fontScale

The physical raster size is:

    Font.size * Graphics.fontScale * Graphics.contentScale

Use `double` in Java APIs and calculations. Skia may require `SkScalar` internally,
but do not expose new TotalCross `float` APIs or round before the renderer boundary.

## Shaping and logical metrics

For general text, width must come from shaping rather than summing independent
character widths. Shaping resolves glyph selection, ligatures, kerning, script,
direction, language, clusters, and fallback fonts.

With Skia, create fonts at effective logical size and shape with logical width
constraints. Keep linear or fractional metrics so device hinting does not alter
layout. Store glyph positions and run advances in logical coordinates. Apply
`contentScale` through the canvas base transform at draw time.

For multiline text, use the pinned Skia paragraph or shaping facilities already
available in the repository. Do not add a new external text engine without a
separate decision.

The layout width is shaped advance, not painted bounds. Painted or ink bounds are
used for clipping, damage, and diagnostics, not ordinary preferred width.

Vertical line metrics use the most demanding run in the line:

    ascent = positive distance above baseline
    descent = positive distance below baseline
    leading = recommended extra line spacing
    lineHeight = ascent + descent + leading

The text `"ABC"` and `"gjpq"` normally produce the same line height for the same
font runs even though their painted bounds differ.

## FontMetrics compatibility

Keep current integer fields and methods where binary and source compatibility
requires them. They return logical values using a documented conservative
rounding policy. Add `double` accessors, for example:

    double getAscentD();
    double getDescentD();
    double getLeadingD();
    double getHeightD();
    double charWidthD(char value);
    double stringWidthD(String value);

Names may follow repository conventions, but all fractional public additions use
`double`.

A safe compatibility rule is to round positive extents upward so preferred sizes
do not clip. Preserve signed semantics where existing fields require them. Add
tests that freeze the chosen rule.

`font.fm` may remain as the logical metric object associated with a `Font`.
It must not represent one physical surface. Any device-pixel helper belongs to
`Graphics` or an internal renderer context.

## Control preferred sizes

`fm.height`, `fm.stringWidth`, and `fmH` become logical. Code that computes
preferred size may continue to use them directly:

    preferredWidth = fm.stringWidth(text) + logicalPadding;
    preferredHeight = fm.height + logicalPadding;

Do not mix logical metrics with physical control bounds. In the new complete
logical drawing model, `Control.width`, `Control.height`, and coordinates exposed
to `onPaint` should be logical, while the backend owns physical bounds. If an
incremental bridge temporarily stores physical bounds, introduce explicit
logical accessors and device conversion helpers; do not silently mix units.

Audit every use of:

    fmH
    fm.height
    fm.ascent
    fm.descent
    fm.stringWidth
    fm.charWidth

Classify each use as layout, drawing, clipping, cursor placement, selection,
scrolling, cache sizing, or raw pixel allocation. Migrate according to unit.

## Measurement and drawing reuse

Measurement and drawing must use the same shaped result or equivalent cache entry.
Do not call a simplistic width function for layout and a separate shaper for
drawing.

An internal text layout object may contain:

    logical advance
    ascent, descent, leading, line height
    line boundaries
    glyph IDs and logical positions
    font runs and fallback typefaces
    clusters for hit testing
    logical ink bounds
    optional SkTextBlob or paragraph object

The public API need not expose this object in the minimal issue fix, but internal
reuse must prevent measurement/drawing drift.

## Cache separation

Logical layout cache keys include values that change shaping:

    typeface and fallback set
    logical font size
    fontScale
    text
    language, script, direction
    OpenType features
    letter spacing
    logical width constraint

They do not include `contentScale`.

Raster and glyph-atlas cache keys include values that change device pixels:

    typeface and glyph ID
    effective physical size
    contentScale
    hinting and subpixel mode
    backend and pixel format

A content-scale change invalidates raster caches, not logical shaping. A font-scale
change invalidates both logical metrics and raster output.

## Skia path

Use `SkFont` at effective logical size for metrics and shaping. Enable linear
metrics and subpixel positions where supported. Do not multiply the font size by
screen density before shaping.

When drawing a stored logical layout, draw it under the surface base transform.
Avoid an additional text-only scale.

Use actual font metrics for baseline and damage; do not approximate baseline as
`y + fontSize` or dirty height as `fontSize`.

## Java renderer

The Java path uses the same logical size and width constraints. Java font APIs may
produce slightly different fractional values from Skia. Equivalence requires:

- equal integer compatibility metrics where the approved rounding permits;
- equal component preferred sizes;
- equal line count and wrapping points for the test fonts and fixtures;
- equal baseline ordering and containment;
- double metrics within a documented tolerance;
- no dependency on display scale for default-image layout.

Do not require identical antialiasing pixels across engines.

## Non-Skia native path

The legacy native rasterizer may quantize physical sizes. Preserve logical metrics
as the API source of truth and include resolved physical size in raster cache
keys. If the backend cannot support fractional raster size, document and test the
rounding at the final physical boundary.

Do not let a bitmap font cache keyed only by `Font` reuse scale-1 glyphs at scale
2 or vice versa.

## Required tests

Cover:

- plain, bold, and italic styles;
- digits and punctuation used by DANFE;
- accented Portuguese text;
- ascenders and descenders;
- kerning or ligature examples;
- fallback fonts;
- empty string and whitespace;
- single-line preferred sizes;
- multiline wrapping and ellipsis where supported;
- baseline alignment between adjacent controls;
- logical metrics across scales `1`, `1.5`, `2`, and `3`;
- fontScale changes;
- simultaneous drawing of one `Font` on surfaces with different scales;
- cache invalidation on fontScale and contentScale changes;
- equivalence between measurement and painted placement.

For the DANFE, also assert approved minimum and maximum glyph-height and advance
ranges. Containment alone is insufficient because an implementation could
incorrectly shrink all text.
