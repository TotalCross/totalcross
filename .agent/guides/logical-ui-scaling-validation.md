<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Logical UI scaling validation guide

## Proof categories

Label every result as one of:

- static source audit;
- Java unit or integration test;
- JavaSE/AWT runtime;
- native macOS compile;
- native macOS deployed runtime;
- final Android runtime;
- optional final iOS or embedded runtime;
- manual visual review.

A native build is not runtime proof. A Java Launcher run on macOS is not native
macOS proof.

## Escalation order

Within an implementation milestone:

1. diff and static checks;
2. focused Java tests;
3. focused native helper;
4. SDK build;
5. native macOS build;
6. deployed native macOS fixture;
7. milestone audit.

Run Android only in the final cross-platform milestone. iOS remains optional.

## R1 reconciliation gate

Before resuming M3R:

- update state to the actual branch head;
- remove the shaping-engine blocker;
- append only verified missing evidence;
- correct PIXEL conversion with a nonzero client origin;
- add a deployed native PIXEL migration assertion;
- record the intended visibility/lifecycle of scale mutation.

## USE_WRITE_PIXELS gate

Preserve both enabled and disabled configurations.

Require direct writes only for eligible identity-transform copies. Scaled,
clipped, filtered, translucent, or transformed copies use canvas drawing. Raw
pixel APIs remain physical.

## Logical layout gate

Exercise actual layout APIs at scales 1, 1.5, 2, and 3.

Cover:

- root DP and root PIXEL;
- inheritance and child override;
- semantic constants;
- shared edges;
- nonzero insets/client origin;
- events and hit testing;
- root PIXEL migration in a deployed native macOS app.

## SkFont-only text gate

No test or build may require SkShaper, HarfBuzz, ICU, or SkParagraph.

For each destination, prove:

    effective size = Font.size * fontScale

is passed to both measurement and drawing before either operation.

Require:

- `SkFont::measureText` at the effective logical size;
- `SkFont::getMetrics` at the effective logical size;
- drawing with the same typeface, size, and UTF-16 input;
- contentScale applied only by the canvas;
- integer rounding after effective-size measurement;
- content-scale invariance of logical metrics;
- font-scale changes reflected in metrics and layout;
- actual double values on the Skia path.

Do not accept a test that only proves:

    measureAtScaleOne * fontScale

because it does not prove equal measurement and drawing configuration.

## TotalCross wrapping gate

TotalCross must continue to perform line breaking and multiline layout.

Test:

- `Convert.insertLineBreak` or its replacement/overload uses a
  destination-aware measurement path;
- explicit newlines remain unchanged;
- only fontScale changes wrap points;
- contentScale does not change wrap points;
- accepted lines fit when drawn;
- Label autoSplit, MultiEdit, and other current multiline controls remain under
  TotalCross ownership;
- no SkParagraph call or dependency exists.

## Typography scope boundary

Accented Portuguese, `"AV"`, digits, punctuation, and long DANFE strings are
representative text inputs.

Do not claim these tests prove:

- HarfBuzz shaping;
- ligatures;
- bidi;
- complex scripts;
- fallback;
- Unicode cluster semantics.

## Renderer matrix

Use common semantic fixtures for Java, native macOS Skia, and the supported
native non-Skia path.

Compare logical bounds, integer compatibility metrics, TotalCross wrapping,
baseline ordering, containment, image dimensions, and barcode structure.

Do not require identical antialiasing pixels or identical fractional values from
a renderer that documents integer-only metrics.

## Image and synchronization gate

Test constructors, loaders, codecs, export, frames, transforms, cache ownership,
natural drawing size, physical source rectangles, row pitch, alpha, and odd
widths.

The deployed native macOS fixture must prove Java-to-native upload and
native-to-Java readback, including alternating ownership.

## DANFE gate

Follow `.agent/guides/logical-ui-scaling-danfe.md`.

The complete fixture includes text and automated containment and size assertions.
The rectangle/barcode-only test remains low-level coverage.

## Native macOS gate

Follow `.agent/guides/macos-native-runtime-validation.md`.

Require matching SDK, deployed app, matching dylib hash, native execution,
machine-readable assertions, PNGs, and target-window screenshot.

## Screenshot gate

Resolve a CoreGraphics window ID owned by the launched PID and execute:

    /usr/sbin/screencapture -x -l "$WINDOW_ID" "$OUTPUT_PNG"

Do not use full-desktop fallback.

## Final Android gate

After Java and native macOS pass, run the same fixture on a high-density Android
target.

Require logical text stability, correct physical image sizes, TotalCross
wrapping, barcode structure, synchronization, and absence of global-density
layout decisions.

## Audit commands

Use focused tasks and redirect verbose logs:

    cd TotalCrossSDK
    ./gradlew-agent test --tests '<focused test>'
    ./gradlew-agent dist -x test

    cd ..
    cmake -S TotalCrossVM -B build-logical-ui \
      -DCMAKE_BUILD_TYPE=Release -G Ninja
    ninja -C build-logical-ui tcvm

At each milestone closure:

    git diff --check
    git status --short -- <active paths>
    git diff --stat
    wc -c <new files>
    wc -l <new files>

## Final acceptance

Completion requires:

- preserved embedded fast path;
- correct DP and PIXEL layout, including nonzero client origins;
- native backing scale;
- SkFont-only destination-aware text metrics;
- TotalCross-owned line breaking and multiline layout;
- complete image ownership and synchronization;
- Java, Skia, and supported non-Skia semantic results;
- complete DANFE in Java and native macOS;
- deterministic window screenshots;
- final Android proof;
- privacy, compatibility, file-size, and source audits.
