<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Complete DANFE validation

## Scope

The DANFE fixture validates logical scaling with the existing text engines.

It must not require SkShaper, HarfBuzz, ICU, or SkParagraph. TotalCross remains
responsible for line breaking and multiline layout.

## Common fixture

Use deterministic synthetic data and one logical document specification:

- 360 by 540 logical document;
- accented Portuguese issuer and recipient text;
- title, totals, product descriptions, address, and footer;
- regular and bold styles already supported;
- table rules and filled headers;
- barcode with exactly 31 dark runs.

No real private data is allowed.

## Images

Default image:

    logical 360 x 540
    physical 360 x 540
    contentScale 1

Explicit scale-two image:

    logical 360 x 540
    physical 720 x 1080
    contentScale 2

Use the same logical drawing commands.

## Java lane

Run through JavaSE/AWT and produce:

    danfe-java-default.png
    danfe-java-scale2.png
    danfe-java-assertions.json

This is Java proof only.

## Native macOS lane

Compile against the current SDK, deploy for macOS, and run with the exact freshly
built matching `libtcvm.dylib`.

Produce:

    danfe-native-default.png
    danfe-native-scale2.png
    danfe-native-assertions.json

## Text assertions

For representative strings, record:

- effective logical font size;
- measured logical advance;
- ascent, descent, leading, and line height;
- baseline;
- assigned logical rectangle;
- containment;
- TotalCross line count and break positions where wrapping applies.

Require approved minimum and maximum ranges for title and body height, line
height, and selected string advances so globally shrinking text cannot pass.

Measurement must occur at `Font.size * fontScale`, not by scaling an earlier
measurement.

Do not label an `"AV"` or accented-string check as proof of kerning, shaping,
fallback, ligatures, bidi, or complex-script support.

## Multiline assertions

Use the existing TotalCross line-breaking and multiline implementation.

Require:

- explicit newlines produce the expected lines;
- automatic wrapping is unchanged by contentScale;
- fontScale can change line breaks;
- every accepted line fits its logical width when drawn;
- vertical placement uses effective-size line metrics;
- no SkParagraph dependency is present.

## Geometry and barcode

Assert exact logical and physical dimensions, aligned table edges, contained
footer, no double scaling, 31 barcode runs, quiet zones, and unclipped endpoints.

## Density independence

For the same renderer and font inputs, changing only destination contentScale
must not change logical metrics, TotalCross wrapping, or the default scale-one
image.

Native Retina proof comes from the deployed native application, not a simulated
Java `/scale`.

## Synchronization

In the native app, prove:

- alpha-128 Java pixel upload at an odd row width;
- native drawing visible in Java readback;
- alternating ownership;
- preserved failure state.

## Renderer equivalence

Compare Java, native Skia, and supported native non-Skia behavior for:

- logical bounds;
- integer compatibility metrics;
- TotalCross line breaks;
- baselines and containment;
- image dimensions;
- barcode structure.

Fractional metrics and antialiasing pixels may differ within documented
tolerances.

## Final Android lane

After Java and native macOS pass, deploy the same fixture to a high-density
Android target and run the same semantic and synchronization assertions.

## Evidence

Store under:

    artifacts/logical-ui-scaling/danfe/

Label every result by lane and include assertion JSON, PNGs, logs, runtime
identity, native hashes, screenshots, comparison images, and privacy review.
