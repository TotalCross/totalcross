<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Logical text and FontMetrics design — SkFont-only scope

Read this file before Milestone 3R.

## Scope decision

Logical text scaling uses only text facilities already present in the pinned
Skia core build:

- `SkFont::getMetrics`;
- `SkFont::measureText`;
- `SkTextBlob::MakeFromText`;
- `SkCanvas` drawing and its base transform.

This task must not add, build, package, or require:

- SkShaper;
- HarfBuzz;
- ICU;
- SkParagraph;
- another text-layout engine.

The task does not transfer line breaking or multiline layout responsibility out
of TotalCross.

## Font model

`Font` is a scale-independent descriptor. `Font.size` is a logical size.

For one destination:

    effectiveLogicalFontSize = Font.size * Graphics.fontScale

Physical rasterization is produced by:

    effectiveLogicalFontSize * Graphics.contentScale

through the canvas base transform. Do not multiply the SkFont size or measured
advance by `contentScale`.

## Measurement order

The destination-aware path must create or configure `SkFont` at:

    Font.size * Graphics.fontScale

before calling `measureText` or `getMetrics`.

Do not implement destination measurement as:

    measureText(Font.size) * fontScale

even when the result appears approximately linear. Measurement and drawing must
receive the same effective size.

## Public FontMetrics compatibility

The existing `FontMetrics` object may remain associated with `Font` and represent
scale-one logical metrics.

Public compatibility fields and methods remain source and binary compatible:

    ascent
    descent
    height
    charWidth
    stringWidth
    sbWidth

Integer extents use conservative upward rounding where positive size is being
reported, so preferred sizes do not clip.

Double accessors expose the renderer's available logical precision. A renderer
that cannot supply fractional metrics may return an integer-valued double, but
this limitation must be labeled and tested.

## Destination-aware internal metrics

Controls and drawing code require an internal path that accepts both the Font and
the destination Graphics, or equivalently the effective logical size.

It returns at least:

    ascent
    descent
    leading
    lineHeight
    string advance
    character advance where required by existing controls

On Skia, all values come from an SkFont configured with the destination's
effective logical size.

## Measurement and drawing equivalence

For this task, equivalence means:

- same typeface;
- same effective logical SkFont size;
- same UTF-16 code-unit input;
- same SkFont text-to-glyph behavior;
- same rounding policy at the public integer boundary.

Measurement uses `SkFont::measureText`. Drawing uses
`SkTextBlob::MakeFromText` or the equivalent existing SkFont path.

The task does not guarantee advanced shaping, bidi, fallback, ligatures, or
cluster behavior beyond what the current core SkFont path already provides.

## TotalCross line breaking and multiline layout

TotalCross remains responsible for:

- explicit newline handling;
- automatic line breaking;
- multiline line construction;
- alignment and justification;
- line spacing;
- ellipsis where currently supported;
- cursor and selection placement;
- preferred width and height.

The line-breaking algorithm must receive a destination-aware measurement
function. It must not rely only on a scale-one `FontMetrics` when
`Graphics.fontScale != 1`.

Do not replace `Convert.insertLineBreak` with SkParagraph. Either adapt it or add
an internal overload/measurement adapter while preserving its TotalCross
behavior.

## Control preferred sizes

A control's preferred text dimensions use destination-aware metrics:

    preferredWidth = ceil(measureAtEffectiveSize(text)) + logicalPadding
    preferredHeight = ceil(lineHeightAtEffectiveSize) + logicalPadding

Changing only `contentScale` must not change preferred size, wrapping, cursor
positions, or line count.

Changing `fontScale` must update metrics, preferred bounds, wrapping where
applicable, cursor positions, selection geometry, and cached line layout.

## Baseline and damage

Use actual effective-size ascent, descent, and leading.

Draw at the logical baseline under the canvas transform. Compute logical damage
from the same effective-size metrics and advance, then convert damage edges to
physical bounds once.

Do not approximate baseline or dirty height from `Font.size`.

## Caches

Logical text cache keys include values that affect the current simple text path:

    typeface
    Font.size
    fontScale
    text
    width constraint
    relevant TotalCross layout options

They do not include `contentScale`.

Raster or glyph caches include effective physical size and renderer-specific
state. A content-scale change invalidates physical raster state but not
TotalCross logical wrapping. A font-scale change invalidates both logical text
layout and physical raster state.

## Java renderer

The Java lane follows the same order:

1. create or derive the Java font at `Font.size * fontScale`;
2. measure with the rendering context used for drawing;
3. keep results in logical units;
4. let the Java backing transform apply contentScale.

Where the Java implementation cannot return fractional values, document the
rounding boundary and compare semantic results rather than requiring identical
fractions to Skia.

## Non-Skia native renderer

The legacy renderer may quantize the effective logical or physical font size.
Apply fontScale before selecting or resizing the font. Keep the rounding at the
final backend boundary and test the documented result.

## Required tests

Cover:

- plain, bold, and italic styles already supported;
- digits, punctuation, and accented Portuguese used by DANFE;
- ascenders and descenders;
- empty string and whitespace;
- preferred sizes for Label, Button, Edit, and MultiEdit;
- TotalCross explicit and automatic multiline layout;
- wrapping at fontScale 1 and 1.5;
- content scales 1, 1.5, 2, and 3;
- simultaneous destinations with different scales;
- cursor, selection, alignment, and baseline;
- cache invalidation on fontScale;
- physical raster invalidation on contentScale;
- equality between the effective-size measurement used for layout and the one
  used for drawing.

Do not add acceptance tests that require SkShaper, HarfBuzz, ICU, SkParagraph,
bidi, complex-script shaping, engine-level fallback, or guaranteed ligatures.
