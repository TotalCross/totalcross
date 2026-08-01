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
  `510f30540`
- Worktree:
  `/Users/flsobral/repos/totalcross-logical-ui`
- History policy: preserve existing commits; correct with new commits.

## Active Milestone

Milestone 3R: complete logical text and FontMetrics.

## Active Slice

Validate per-destination `fontScale` in the deployed fixture, then connect the
logical metrics used by preferred-size layout to the same effective scale.

## Next Concrete Action

Run the native fontScale fixture and inspect preferred-size consumers before
making fontScale invalidate their logical metrics.

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

These are foundations, not completion of their behavioral milestones.

## Incomplete or Unproven

- fontScale and true double metrics are incomplete;
- Java renderer coverage is partial;
- native-to-Java image readback is unproven;
- non-Skia native equivalence is unproven;
- complete text-bearing DANFE assertions are absent;
- Java and native macOS runtime proof were conflated;
- no accepted screenshot exists.

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
    cd /Users/flsobral/repos/totalcross-logical-ui
    rg -n "fontScale|double.*Width|FontMetrics" TotalCrossSDK/src/main/java
