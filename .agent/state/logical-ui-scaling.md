<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Logical UI scaling execution state

Rewrite this file instead of appending. Read it first when resuming.

## Base and Branch

- Base commit:
  `d480df074e7fb6f5a32dfcc2f1f30c3949095e73`
- Reviewed branch:
  `feat/logical-ui-scaling`
- Reviewed head:
  `f820d4540`
- Worktree:
  `/Users/flsobral/repos/totalcross-logical-ui`
- History policy: preserve existing commits; correct with new commits.

## Active Milestone

Milestone 3R: complete logical text and FontMetrics.

## Active Slice

Extend the same effective logical font scale through the remaining ordinary text
controls, line layout, and cached layout invalidation. `Label`, `Button`, and
`Edit` preferred sizes are covered. `Label` and `Button` line placement now
uses scaled logical line height; `Edit` vertical text, selection, and cursor
geometry now use it too. Its horizontal measurement and caret placement now
use destination-scaled advances; remaining text drawing equivalence is next.

## Next Concrete Action

Audit measurement/drawing equivalence and the remaining baseline behavior for
text controls against the `Graphics.fontScale` contract.

## Files to Read Now

- `.agent/logical-ui-scaling-execplan.md`, Milestone 3R only
- `.agent/design/logical-ui-scaling-text.md`
- `.agent/guides/logical-ui-scaling-validation.md`, text gate

Do not read later image or renderer guides yet.

## Verified Foundations

- Branch starts from the recorded master.
- DP is zero and `UnitsConverter.toPixels` is identity.
- `LayoutUnit`, Graphics scale fields, and logical Image fields exist.
- Focused Java API/image tests pass.
- Current native macOS target compiles and links.
- R0 restored guarded direct writes. Default, disabled, and opaque-enabled macro
  builds compile; the native Skia fixture passes identity, scaled fallback,
  clipping, and physical raw-pixel assertions.
- The native SDL screen path now records its physical drawable dimensions and
  content scale, exposes logical screen dimensions through Settings, and assigns
  the scale to newly created screen Graphics objects. A deployed native macOS
  fixture reports a real Retina scale of 2 with matching logical/physical sizes.
- Native Skia FontMetrics now exposes actual fractional ascent, descent, and
  height while retaining upward-rounded integer compatibility metrics.
- Native Skia exposes fractional character and string advances through
  `FontMetrics` deployed-method bindings.
- Native Skia text drawing and damage use the destination Graphics.fontScale;
  the base canvas contentScale remains the sole physical scale.
- `Control` exposes logical font measurement helpers and `Label` uses them for
  preferred size and line widths. Its cached widths are recomputed when the
  destination font scale changes. Focused Java tests pass, and the deployed
  native macOS fixture reports equal widths at content scales 2 and 4 with a
  larger width at font scale 1.5.
- `Button` refreshes its cached line widths when the destination font scale
  changes and uses the scaled logical metrics for preferred size. Its focused
  Java preferred-size test passes.
- `Edit` now uses destination-scaled logical text metrics for its non-material
  preferred width and preferred height. The focused Java test covers all three
  ordinary controls against content and font scale changes.
- `Label` pagination/vertical alignment and `Button` multiline placement use
  the same scaled logical line height as their preferred-size calculations.
- `Edit` selection, caption-icon placement, and cursor height now use scaled
  logical vertical metrics.
- `Edit` horizontal alignment, cursor positions, masks, password text, and
  caption-image fitting now use destination-scaled advances. The focused test
  exercises materialized text, content-scale invariance, and font-scale growth.
- The deployed native macOS fixture now verifies the `Edit` preferred-width
  contract alongside `Label`; it reports `editWidths=48,48,67` at content
  scales 2 and 4 followed by font scale 1.5.
- Native integer text widths now round positive fractional advances upward,
  matching the documented compatibility policy; the deployed macOS fixture
  still reports the expected Label and Edit scale triples.
- The deployed fixture rejects invalid vertical metrics, DANFE, accented, or
  typographic-pair advances, and control scale invariants. It passed with
  real fractional metrics and nonzero accents/pair advances.
- A `Graphics.fontScale` transition now repositions control surfaces using their
  recorded layout expressions and requests repaint, so preferred bounds are not
  left at the previous font scale. Focused Java bounds assertions pass.
- The same font-scale transition path passed in the freshly deployed native
  macOS fixture without changing the established metric assertions.
- Native Skia now serves `charWidth`, String, char-array, and StringBuffer
  integer measurement overloads through the same measurement path as drawing
  and fractional String measurement; native compile and deployed runtime pass.

These are foundations, not completion of their behavioral milestones.

## Incomplete or Unproven

- fontScale coverage beyond `Label` and true double metrics in all renderers
  are incomplete;
- Java renderer coverage is partial;
- native-to-Java image readback is unproven;
- non-Skia native equivalence is unproven;
- complete text-bearing DANFE assertions are absent;
- Java and native macOS runtime proof were conflated;
- no accepted screenshot exists.
- The pinned Skia package has no `SkShaper` or `SkParagraph` headers; preserve
  the shared simple-text measurement/drawing path while treating complete
  shaping, fallback, and ligature proof as still open M3R work.

## Platform Policy

Until final validation:

- Java tests may run on macOS and are labeled Java;
- all native compile and runtime validation uses macOS only;
- do not build or run iOS or Android.

At final validation:

- Android is required;
- iOS is optional unless separately requested;
- embedded Linux may validate `USE_WRITE_PIXELS` when available.

## Screenshot Status

No screenshot validation has been attempted under the updated direct CoreGraphics
workflow. It belongs to Milestone 7R, not the active layout slice.

## Resume Command

    cd /Users/flsobral/repos/totalcross-logical-ui
    rg -n "getPreferred(Width|Height)|fm\.stringWidth|fmH" TotalCrossSDK/src/main/java/totalcross/ui
