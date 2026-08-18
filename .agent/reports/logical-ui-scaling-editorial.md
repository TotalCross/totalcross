<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Logical UI scaling editorial report

## Editorial Summary

Execution stopped at user request on `feat/logical-ui-scaling`. The branch
contains validated logical-scaling work through Java and deployed native macOS;
final Android semantic acceptance remains unresolved.

## Scope

Logical text scaling uses core SkFont measurement and metrics. TotalCross retains
line breaking and multiline layout.

SkShaper, HarfBuzz, ICU, SkParagraph, bidi, complex shaping, guaranteed
ligatures, engine-level fallback, and paragraph-engine migration are outside this
task.

## Original Plan versus Current Outcome

Validated work includes:

- logical API scaffolding;
- guarded embedded direct writes;
- native macOS high-DPI backing;
- deployed Retina runtime identity;
- fractional native Skia metrics;
- selected Label, Button, Edit, and MultiEdit scaling work;
- scale-aware image primitives, source rectangles, alpha composition, frame
  dimensions, transforms, PNG loading, and texture ownership;
- Java renderer backing-scale primitives, blits, and text composition;
- direct native macOS DANFE assertions with a matching dylib and a
  process-owned screenshot.

The remaining required correction is Android runtime resource lookup. The final
fixture AAB builds and installs on a 440-dpi emulator, but `Resources.multiedit`
is null even though `TCUI.tcz` contains the corresponding PNG. `MultiEdit`
therefore aborts in `NinePatch` before semantic assertions execute.

## Decisions and Trade-offs

- Existing branch history is preserved.
- Core SkFont is sufficient for logical scaling.
- TotalCross text-layout ownership remains unchanged.
- Advanced typography is not silently promised by this feature.
- Java and native macOS validation remain distinct.
- Android is final required platform proof.

## Validation and Measurable Results

Focused Java `GraphicsScaleTest` and `DanfeScalingTest` passed. Java Launcher
passed the deterministic fixture at scale one. Native macOS Skia build, deploy,
hash comparison, and direct fixture passed; deployed dylib SHA-256 was
`b4e7c140717fb4bf6e0f1eada365f5c1aea97067907ad280fb99430bedb58a5a`.
The non-Skia macOS configuration compiles but is recorded unsupported because it
does not map image-backed logical primitives to physical backing. Android native
dependency fetch, standard release build, bundle, AAB deploy, and install pass;
Android semantic runtime does not.

Do not describe a representative `"AV"` width as proof of kerning or shaping.

## Limitations and Remaining Work

Restart at the Android TCZ-resource lookup. Verify why an entry demonstrably
present in `TCUI.tcz` is unavailable through `Vm.getFile`/image loading, then
rerun the installed high-density fixture. iOS remains optional and was not run.

## Claims Requiring Human Review

- Android TCZ resource lookup and final high-density semantics;
- public scale-mutation lifecycle API;
- optional iOS result;
- non-Skia renderer support remains deliberately unsupported.
