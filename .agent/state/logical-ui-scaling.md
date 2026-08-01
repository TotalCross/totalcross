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
  `7d300c9da`
- Worktree:
  `/Users/flsobral/repos/totalcross-logical-ui`
- History policy: preserve existing commits; correct with new commits.

## Active Milestone

Milestone 1R: complete actual logical layout behavior.

## Active Slice

Connect effective `LayoutUnit` values to actual child placement and prove the
root PIXEL migration behavior.

## Next Concrete Action

Read Milestone 1R and the API design, then identify the existing layout boundary
where parent unit and surface scale can convert child placement.

## Files to Read Now

- `.agent/logical-ui-scaling-execplan.md`, Milestone 1R only
- `.agent/design/logical-ui-scaling-api.md`
- `.agent/guides/logical-ui-scaling-validation.md`, logical layout gate

Do not read later renderer or text guides yet.

## Verified Foundations

- Branch starts from the recorded master.
- DP is zero and `UnitsConverter.toPixels` is identity.
- `LayoutUnit`, Graphics scale fields, and logical Image fields exist.
- Focused Java API/image tests pass.
- Current native macOS target compiles and links.
- R0 restored guarded direct writes. Default, disabled, and opaque-enabled macro
  builds compile; the native Skia fixture passes identity, scaled fallback,
  clipping, and physical raw-pixel assertions.

These are foundations, not completion of their behavioral milestones.

## Incomplete or Unproven

- `LayoutUnit` is not connected to real placement.
- native macOS backing scale is not proven;
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
    git status --short -- \
      TotalCrossVM/src/nm/ui/skia \
      .agent
