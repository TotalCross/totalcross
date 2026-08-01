<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Complete DANFE validation

The existing rectangle and barcode test is retained as low-level coverage. It is
not the complete DANFE acceptance test because it does not draw or measure text.

## Common fixture

Use deterministic synthetic data and one logical document specification for all
renderers. It contains:

- 360 by 540 logical document;
- title and document identifier;
- accented Portuguese issuer and recipient data;
- address and tax-like synthetic fields;
- product table with long descriptions;
- quantities, prices, totals, bold and regular text;
- lines, rectangles, filled headers;
- footer near the lower boundary;
- barcode with exactly 31 dark runs.

No real customer, company, tax, address, email, or phone data is allowed.

Split data, renderer, assertions, and native application files when needed to
remain below the file-size limit.

## Required image variants

Default image:

    logical: 360 x 540
    physical: 360 x 540
    contentScale: 1

Explicit scale-two logical image:

    logical: 360 x 540
    physical: 720 x 1080
    contentScale: 2

Use the same logical drawing commands.

## Java lane

Run the fixture through JavaSE/AWT. This proves the Java renderer only.

Generate:

    danfe-java-default.png
    danfe-java-scale2.png
    danfe-java-assertions.json

Capture the Java application window separately using its Java PID and the
screenshot guide.

## Native macOS lane

Compile the fixture against the current SDK, deploy it for macOS, and execute the
generated native application with the exact current `libtcvm.dylib`.

Generate:

    danfe-native-default.png
    danfe-native-scale2.png
    danfe-native-assertions.json

The native executable must perform or emit enough data for assertions without
depending on the Java Launcher.

Capture the native application window using the native process PID.

## Text assertions

For title, issuer, recipient, long product description, totals, and footer,
record and assert:

- shaped logical advance;
- ascent, descent, leading, and line height;
- expected line count or approved range;
- baseline positions;
- logical ink bounds;
- containment within the assigned rectangle.

Containment alone is insufficient. Add approved minimum and maximum ranges for:

- title glyph height;
- body glyph height;
- known string advance;
- line height;
- bold versus regular distinction.

A change that shrinks all text to fit must fail.

## Geometry assertions

Assert:

- exact logical and physical dimensions;
- shared rules align with text and table cells;
- footer remains inside the document;
- scale-two physical geometry is twice the default while logical geometry is
  unchanged;
- no primitive or text is scaled twice.

## Barcode assertions

Analyze a selected physical scan line and require:

- exactly 31 dark runs;
- expected quiet zones;
- no clipped first or last run;
- equal logical structure at scale one and scale two.

## Density independence

For one renderer and font set, changing only the host or window content scale must
not change a default scale-one image.

Require equal dimensions, logical metrics, and PNG hash when raster inputs are
otherwise identical.

Do not use a simulated Java `/scale` result as native Retina proof. Native Retina
proof comes from the deployed native application and native backing scale.

## Synchronization assertions

In the native app:

- modify an alpha-128 Java pixel at an odd row width;
- upload and verify it in native output;
- draw a known native mark;
- read back and verify the Java pixel;
- alternate ownership and repeat;
- preserve failure state when a native copy fails.

## Renderer equivalence

Require equivalent logical semantics between Java, native Skia, and supported
native non-Skia paths:

- integer compatibility metrics;
- line counts and approved wraps;
- baselines and containment;
- logical and physical image dimensions;
- barcode structure.

Do not require cross-engine antialiasing pixel identity.

## Final Android lane

Run only after Java and native macOS pass. Deploy the same fixture on a
high-density Android target and require the same semantic and synchronization
assertions.

iOS is optional final validation unless explicitly required.

## Evidence package

Store under:

    artifacts/logical-ui-scaling/danfe/

Include assertion JSON, PNGs, logs, runtime identity, dylib hash for native
macOS, Android metadata, screenshots, comparison images, and privacy review.

Label every artifact with its lane. Never call a Java Launcher PNG or screenshot
native macOS evidence.
