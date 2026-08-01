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

Reconciliation checkpoint R1, then resume Milestone 3R.

## Active Slice

Correct destination text measurement so `fontScale` is applied to the SkFont
size before measurement, and make TotalCross wrapping use the same
destination-aware path.

## Next Concrete Actions

1. Commit this plan update.
2. Correct the PIXEL client-rectangle conversion using original edges and add a
   nonzero-inset regression test.
3. Append verified evidence for commits after the current evidence endpoint.
4. Replace `fm.metric * fontScale` helpers with destination-aware measurement at
   `Font.size * fontScale`.
5. Route Label autoSplit and other TotalCross multiline users through that path.
6. Re-run focused Java tests and the deployed native macOS fixture.
7. Continue through the rest of M3R without text-engine dependency work.

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

- Current control helpers multiply scale-one measured results by fontScale rather
  than measuring at the effective SkFont size.
- TotalCross line breaking still receives scale-one FontMetrics in Label paths.
- PIXEL layout conversion mishandles a nonzero client origin.
- State, outcomes, and append-only evidence are not synchronized.
- `Graphics.setScales` public lifecycle is not yet an approved final API.
- Existing `"kerning"` terminology overstates what the core SkFont path proves.
- Java double text metrics remain integer-valued and need an approved renderer
  treatment.

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
