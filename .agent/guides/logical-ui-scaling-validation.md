<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Logical UI scaling validation guide

## Proof categories

Every result must be labeled as exactly one of:

- static source audit;
- Java unit or integration test;
- JavaSE/AWT runtime;
- native macOS compile;
- native macOS deployed runtime;
- final Android runtime;
- optional final iOS or embedded runtime;
- manual visual review.

Do not promote one category into another.

A CMake/Ninja pass is native compile proof, not runtime proof. A
`totalcross.Launcher` run is JavaSE/AWT proof, even when the host OS is macOS.

## Escalation order

Within the active milestone:

1. diff and static checks;
2. focused Java tests;
3. focused native helper or unit test;
4. SDK module build;
5. native macOS compile;
6. deployed native macOS fixture;
7. full SDK/native build at milestone closure.

Do not run Android or iOS during implementation milestones.

At final cross-platform validation:

1. rerun the complete Java matrix;
2. rerun deployed native macOS Skia;
3. rerun deployed native macOS non-Skia when supported;
4. run Android;
5. run optional iOS or embedded target validation.

## Corrective USE_WRITE_PIXELS gate

Preserve the build path. Locate its actual preprocessor and build configuration.

Compile with the path disabled and enabled. Test:

- eligible opaque full-source same-size alpha-255 copy at identity scale;
- the same visual output through the fallback;
- contentScale greater than one rejects direct write;
- destination translation or non-identity transform rejects direct write;
- filtered or alpha copies use canvas drawing;
- raw pixel methods still address physical pixels.

No Linux embedded execution is required until the final platform checkpoint.

## Logical layout gate

Tests must call real layout APIs and inspect resulting bounds. Resolver-only tests
are insufficient.

Cover root DP, root PIXEL, nested inheritance, child override, semantic constants,
preferred sizes, shared edges, events, and hit testing at scales 1, 1.5, 2, and 3.

Run the migration fixture in Java and in a deployed native macOS application.

## Renderer matrix

Use the same semantic fixture for:

- Java renderer;
- native macOS Skia renderer;
- native macOS non-Skia renderer when supported.

Require equivalent logical bounds, integer compatibility metrics, wrapping,
baseline ordering, containment, image dimensions, and barcode structure.

Allow documented fractional metric and antialiasing differences. Do not allow a
renderer to skip fontScale, source/destination unit rules, or synchronization.

## Text gate

Require actual double values, not integer values returned as double.

Cover ascent, descent, leading, line height, shaped advance, fallback, accents,
kerning or ligature behavior, multiline wrapping, preferred sizes, and baseline.

Verify contentScale does not change logical text layout. Verify fontScale does.

## Image and synchronization gate

Test constructors, loaders, codecs, export, frames, transforms, caches, natural
drawing size, physical source rectangles, row pitch, alpha, and odd widths.

In native macOS runtime, test both directions:

- Java pixels uploaded to native/Skia;
- native/Skia drawing read back into Java pixels.

Test repeated and alternating ownership. A Java-only `applyChanges` test is not
native synchronization proof.

## DANFE gate

Follow `.agent/guides/logical-ui-scaling-danfe.md`.

The final fixture includes text and automated text assertions. A rectangle and
barcode-only test remains low-level coverage, not DANFE acceptance.

## Native macOS gate

Follow `.agent/guides/macos-native-runtime-validation.md`.

Require matching SDK, deployed application, matching dylib, native execution,
machine-readable assertions, output PNGs, and target-window screenshot.

## Screenshot gate

Follow `.agent/guides/private-screenshot-capture.md`.

Use a CoreGraphics window ID owned by the launched PID and pass it to:

    /usr/sbin/screencapture -x -l "$WINDOW_ID" "$OUTPUT_PNG"

Computer Use targeting is not the primary path. Full-desktop fallback is
forbidden.

## Final Android gate

Run only after native macOS milestones pass. Use at least one Android device or
emulator with density greater than one.

Deploy and run the complete fixture. Require:

- default image remains 360 by 540 physical pixels;
- scale-two logical image is 720 by 1080;
- text metrics and containment pass;
- 31 barcode runs remain;
- upload and readback assertions pass;
- no global screen-density dependency returns.

iOS is optional unless explicitly requested. Do not let an unavailable iOS
environment replace or block the Android issue proof.

## Build and audit commands

Use the smallest current task names. Typical commands are:

    cd TotalCrossSDK
    ./gradlew-agent test --tests '<focused test>'
    ./gradlew-agent dist -x test

    cd ..
    cmake -S TotalCrossVM -B build-logical-ui \
      -DCMAKE_BUILD_TYPE=Release -G Ninja
    ninja -C build-logical-ui tcvm

At each milestone closure:

    git diff --check
    git diff --stat
    git status --short -- <active paths>
    wc -c <new files>
    wc -l <new files>

Redirect verbose output to `artifacts/logical-ui-scaling/logs/`.

## Final acceptance

Completion requires:

- corrected embedded fast path;
- real layout behavior;
- Java and native macOS proof kept separate;
- native backing scale;
- complete text and image contracts;
- bidirectional native synchronization;
- complete DANFE in Java and native macOS;
- deterministic window screenshots;
- final Android proof;
- privacy, file-size, deprecation, and source audits.
