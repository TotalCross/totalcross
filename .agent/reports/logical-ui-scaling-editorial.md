<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Logical UI scaling editorial report

## Editorial Summary

Implementation is in progress on `feat/logical-ui-scaling`. Useful native and
control foundations are present. No final completion claim is made.

## Scope

Logical text scaling uses core SkFont measurement and metrics. TotalCross retains
line breaking and multiline layout.

SkShaper, HarfBuzz, ICU, SkParagraph, bidi, complex shaping, guaranteed
ligatures, engine-level fallback, and paragraph-engine migration are outside this
task.

## Original Plan versus Current Outcome

Completed foundations include:

- logical API scaffolding;
- guarded embedded direct writes;
- native macOS high-DPI backing;
- deployed Retina runtime identity;
- fractional native Skia metrics;
- selected Label, Button, Edit, and MultiEdit scaling work.

Remaining corrections include effective-size measurement before layout,
destination-aware TotalCross wrapping, nonzero PIXEL client origins, synchronized
evidence, images, renderer equivalence, full DANFE, screenshots, and Android.

## Decisions and Trade-offs

- Existing branch history is preserved.
- Core SkFont is sufficient for logical scaling.
- TotalCross text-layout ownership remains unchanged.
- Advanced typography is not silently promised by this feature.
- Java and native macOS validation remain distinct.
- Android is final required platform proof.

## Validation and Measurable Results

Populate from append-only evidence only. Separate:

- Java tests;
- JavaSE/AWT runtime;
- native macOS compile;
- deployed native macOS runtime;
- final Android runtime;
- manual visual review.

Do not describe a representative `"AV"` width as proof of kerning or shaping.

## Limitations and Remaining Work

Use the current state and branch review. Do not state that the work is complete
until all final acceptance items pass.

## Claims Requiring Human Review

- public scale mutation API;
- visual equivalence;
- non-Skia support status;
- compatibility of logical Image dimensions;
- optional iOS result;
- embedded fast-path performance.
