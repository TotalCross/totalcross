<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Logical UI scaling API contract

Read this file before implementing Milestones 1 and 2. It defines the public and
internal contracts that tests must freeze before renderer-specific work begins.

## Terms

A logical unit is the coordinate unit visible to TotalCross UI code. A physical
pixel is an element of a screen framebuffer or image backing buffer.
`contentScale` is the number of physical pixels per logical unit for a surface.
`fontScale` is a user or application text scale that changes how much logical
space text occupies.

The conversion is:

    physical coordinate = logical coordinate * contentScale

The effective physical font size is:

    Font.size * fontScale * contentScale

`fontScale` participates in layout. `contentScale` does not change logical layout.

## Layout units

Add:

    public enum LayoutUnit {
      INHERIT,
      DP,
      PIXEL
    }

Place it in the smallest package consistent with current UI API conventions.
Document that DP means logical device-independent units, not the old encoded
integer marker.

`MainWindow` has effective `LayoutUnit.DP` by default. `Container` has configured
`LayoutUnit.INHERIT` by default. A container inherits from its nearest ancestor
with an explicit unit. A detached container with no explicit ancestor may resolve
to DP, but the implementation must use one documented rule and test it.

The container's effective unit governs the numeric arguments used to position its
children. The unit configured on a child container governs only that child's own
children. Therefore:

    parent.add(child, 10, 10, 100, 40);

is interpreted according to the parent, while:

    child.setLayoutUnit(LayoutUnit.PIXEL);

changes how later descendants are placed inside `child`.

Provide:

    public void setLayoutUnit(LayoutUnit unit);
    public LayoutUnit getLayoutUnit();

Use an internal effective resolver so detached state and inheritance are tested in
one place.

A legacy application preserves pixel layout with one root change:

    public MyApp() {
      setLayoutUnit(LayoutUnit.PIXEL);
    }

Do not add TCZ-version switching or automatic legacy mode. DP is the new default.

## Public component geometry

Public component positions, dimensions, insets, padding, preferred sizes, font
sizes, event coordinates, and hit-testing coordinates are logical values. Preserve
existing integer signatures unless a separate API is required. Use `double`
internally for fractional calculations and conversions. Do not introduce a new
public `float` API.

It is acceptable for stored compatibility fields to remain integer logical
values. Backend bounds and buffers are physical and should use clearly named
internal fields or helpers.

Semantic layout constants such as `PREFERRED`, `AFTER`, `CENTER`, `FILL`, and
`FIT` remain encoded constants. Their numeric offsets are interpreted in the
effective layout unit. Resolve semantic meaning first, then apply logical-to-
physical conversion at the layout boundary.

Convert rectangle edges rather than converting width independently:

    pixelLeft = round(logicalLeft * scale)
    pixelRight = round(logicalRight * scale)
    pixelWidth = pixelRight - pixelLeft

Use the same rule vertically. Define and test the rounding method centrally.
Preferred sizes should use conservative rounding when converting fractional text
metrics to integer logical compatibility values.

## DP and UnitsConverter migration

Change:

    @Deprecated
    public static final int DP = 0;

A recompiled expression such as `DP + 16` becomes `16`. Remove every branch that
detects values near `DP` or subtracts an encoded marker.

Deprecate:

    UnitsConverter.toPixels(int)

and make it return the exact argument. Its Javadoc must state that component
layout and drawing now use logical values and the destination `Graphics` performs
the scale conversion.

Do not modify the deployer. `DP` is a compile-time constant, so consumer
applications and libraries must be recompiled with the new SDK. Document this as
a migration requirement.

Because `DP` is now zero, it can no longer override a pixel container for an
individual value. Mixed-mode layouts use a logical subcontainer or an explicit
logical placement API only if a real use case justifies one. Do not preserve the
old marker machinery for this edge case.

## Graphics scale ownership

Add public read access:

    public double getContentScale();
    public double getFontScale();

The implementation may have package-private or native setters used by surface
creation and accessibility integration. Validate that both values are finite and
greater than zero. Defaults are `1`.

Scale belongs to the actual destination:

- a window `Graphics` uses that window's current backing scale;
- an image `Graphics` uses the image's immutable scale;
- a headless or printer surface uses its configured scale;
- a cache image uses the scale explicitly selected at creation.

Do not infer scale from a global screen field after surface creation. The same
`Font` may be drawn into a scale-1 image and a scale-2 window at the same time.

Application transforms compose after the base logical-to-physical transform.
Resetting application transforms must retain the base transform. Save/restore,
translation, clipping, and dirty-region calculations must respect this order.

## Font scale

`fontScale` defaults to `1`. It changes effective logical font size and therefore
invalidates font metrics, preferred sizes, wrapping, and layout. It may be
initialized from a future accessibility setting, but this plan does not require a
new user preference UI.

A `contentScale` change invalidates physical caches and buffers, but normally does
not invalidate logical layout. A `fontScale` change does.

## Settings.screenDensity

Deprecate `Settings.screenDensity`. Keep the field readable for source and binary
compatibility. It may mirror the primary or main-window scale as a best-effort
legacy value, but no internal layout, font metric, image rendering, primitive
drawing, or cache decision may depend on it.

Search all reads after implementation. An intentional compatibility read must be
documented and must not affect the new rendering model.

## Events, hit testing, clips, and dirty regions

Input events exposed to controls use logical coordinates. Convert platform
physical coordinates at the window boundary exactly once. Do not reapply scale in
controls.

Control clipping and hit testing use logical geometry. Backends convert final
clip and dirty rectangles to physical edges. Expand conservative raster damage
when antialiasing or stroke width requires it; do not use logical dimensions as
raw pixel extents.

## Compatibility policy

The plan intentionally changes the default layout unit and image dimension
semantics for newly recompiled applications. Compatibility is provided through:

- existing integer method signatures where practical;
- integer logical `FontMetrics` results;
- one root `LayoutUnit.PIXEL` setting;
- explicit physical image getters and pixel APIs;
- deprecated symbols that compile but no longer perform scaling.

Do not add hidden version modes, deployer bytecode rewriting, or global screen
density decisions.

## Focused tests

Before renderer work, add tests that prove:

- `MainWindow` defaults to DP;
- nested containers inherit and override units correctly;
- a child's unit does not affect how its parent places it;
- root pixel mode preserves the migration fixture;
- `PREFERRED` and semantic offsets stay coherent;
- fractional scales convert shared edges without gaps;
- `DP == 0` and `DP + 16 == 16`;
- `UnitsConverter.toPixels(16) == 16`;
- invalid scale values are rejected;
- event conversion occurs once;
- no focused test reads `Settings.screenDensity` for layout or drawing.

Record exact test names and commands in evidence when they are created.
