<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Logical UI scaling execution state

Rewrite this file instead of appending. Read it first when resuming.

## Base and Branch

- Base:
  `d480df074e7fb6f5a32dfcc2f1f30c3949095e73`
- Branch:
  `feat/logical-ui-scaling`
- Reviewed head before this plan update:
  `c8a3152c482d6f3ac511e8295cc400b555aeecae`
- Branch distance from base at review: 82 commits ahead, 0 behind.
- Preserve history; correct with new focused commits.

## Maintainer Scope Decision

Logical text scaling uses the pinned core Skia APIs only:

- `SkFont::getMetrics`;
- `SkFont::measureText`;
- `SkTextBlob::MakeFromText`;
- canvas `contentScale`.

Do not add or require SkShaper, HarfBuzz, ICU, SkParagraph, or another text
engine.

TotalCross retains line breaking and multiline layout.

The external shaping blocker recorded by `c8a3152c4` is resolved by this scope
decision and must be removed from the living plan.

## Active Milestone

Milestone 3R: SkFont-only logical text.

## Active Slice

Complete the remaining M3R text-control matrix, cache behavior, and native
runtime assertions under the SkFont-only scope.

## Next Concrete Actions

1. Audit remaining text controls and effective-size baseline/cursor/selection
   uses.
2. Validate content-scale and font-scale cache behavior in Java and deployed
   native macOS lanes.
3. Complete the text-bearing DANFE M3R assertions without claiming unsupported
   typography behavior.

## Files to Read Now

- `.agent/reviews/logical-ui-scaling-current-review.md`
- `.agent/logical-ui-scaling-execplan.md`, R1 and M3R only
- `.agent/design/logical-ui-scaling-text.md`
- `.agent/guides/logical-ui-scaling-validation.md`, R1 and text gates
- `TotalCrossSDK/src/main/java/totalcross/ui/Control.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/Label.java`
- `totalcross.sys.Convert` line-breaking implementation
- `TotalCrossVM/src/nm/ui/font_FontMetrics.c`
- `TotalCrossVM/src/nm/ui/skia/skia.cpp`
- `TotalCrossVM/src/nm/ui/skia/skia_primitives.cpp`

## Verified Foundations

- The branch starts directly from the recorded base.
- DP is zero and `UnitsConverter.toPixels` is identity.
- Destination contentScale and fontScale fields exist.
- Logical and physical Image dimensions exist.
- `USE_WRITE_PIXELS` was restored with guarded eligibility and focused native
  validation.
- Native SDL/macOS records physical backing and logical screen dimensions.
- A deployed native macOS fixture reported a real Retina content scale of 2 with
  a matching current dylib.
- Native Skia exposes fractional scale-one font metrics and advances.
- Native drawing uses `Font.size * Graphics.fontScale`, while canvas
  contentScale remains the physical transform.
- Label, Button, Edit, and MultiEdit contain useful font-scale layout changes and
  focused tests.
- Integer compatibility widths use upward rounding on the native Skia path.

## Corrections Required Before Closing M3R

- State, outcomes, and append-only evidence are not synchronized.
- Existing `"kerning"` terminology overstates what the core SkFont path proves.
- Java double text metrics remain integer-valued and need an approved renderer
  treatment.

## R1 Evidence

- PIXEL client-rectangle conversion preserves original logical left, top, right,
  and bottom before converting each edge. `LogicalLayoutUnitTest` verifies
  nonzero insets at content scales 1.5, 2, and 3; Java unit validation passed
  in `artifacts/logical-ui-scaling/logs/pixel-client-origin-test.log`.
- The deployed native macOS fixture exercises a PIXEL root container at Retina
  scale and reports `pixelChild=10,5,50,20` for physical input
  `20,10,100,40`; log: `root-pixel-fixture-native-final.log`.
- Destination-aware `FontMetrics` methods now pass the effective logical size
  directly to native Skia measurement and metrics. The deployed macOS fixture
  passed after symbol registration correction; logs:
  `effective-font-measurement-build.log`,
  `effective-font-measurement-java-test.log`,
  `effective-font-measurement-native-retry.log`.
- `Label` routes TotalCross automatic and explicit line breaking through the
  effective-size measurement path. Focused Java wrapping validation confirms
  content-scale invariance and font-scale-dependent break count.
- The freshly deployed native macOS fixture also passed after the wrapping
  change; logs: `effective-wrap-dist.log`, `effective-wrap-deploy.log`, and
  `effective-wrap-native.log`.

R1 is complete: plan/state/evidence are synchronized, the PIXEL regression and
deployed native root fixture pass, and no external text-engine blocker remains.
- `Graphics.setScales` remains the documented destination lifecycle API used by
  native runtime initialization, Image surfaces, and fixtures. It preserves
  logical layout for content-scale changes, repositions controls for font-scale
  changes, and does not expose a global density setting.
- `Edit` cursor descent now comes from effective-size SkFont metrics rather
  than scaling a scale-one value; native compile and focused Java test pass.
- The freshly deployed macOS fixture also passed the effective descent binding;
  logs: `effective-descent-dist.log`, `effective-descent-deploy.log`, and
  `effective-descent-native.log`.
- `MultiEdit` now passes its destination effective font size to both wrapping
  and cursor break-position calls; focused Java validation passed.
- The deployed native macOS fixture passed after the MultiEdit wrapping change;
  logs: `effective-multiedit-wrap-dist.log`,
  `effective-multiedit-wrap-deploy.log`, and
  `effective-multiedit-wrap-native.log`.
- `Label` cached line widths exclude contentScale and refresh when fontScale
  changes; focused Java cache validation passed.

## Remaining Major Work

- complete M3R under the SkFont-only contract;
- complete image codecs, transformations, and bidirectional native sync;
- complete Java renderer semantics;
- complete supported non-Skia native semantics;
- complete DANFE Java and native macOS lanes;
- capture deterministic target-window screenshots;
- run final Android validation;
- complete audits, docs, evidence, and final report.

## Platform Policy

During implementation:

- Java tests may run on macOS but remain Java proof;
- native compile/deploy/runtime validation uses macOS only;
- do not run Android or iOS.

At final validation:

- Android is required;
- iOS is optional unless separately requested;
- embedded validation is optional when hardware is available.

## Screenshot Status

No accepted screenshot exists. Use the CoreGraphics owner-PID window ID and:

    /usr/sbin/screencapture -x -l "$WINDOW_ID" "$OUTPUT_PNG"

in Milestone 7R.

## Resume Command

    cd /Users/flsobral/repos/totalcross-logical-ui
    git status --short
    git rev-parse HEAD
