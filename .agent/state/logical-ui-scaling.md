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
  `ef3b3dec8f15e409597b552ee13db09fd98f3fca`
- Worktree:
  `/Users/flsobral/repos/totalcross-logical-ui`
- History policy: preserve existing commits; correct with new commits.

## Active Milestone

Corrective checkpoint R0: reconcile reviewed implementation and restore
`USE_WRITE_PIXELS`.

## Active Slice

Restore the embedded direct-write specialization removed by
`fd7e5d358cd3e19ef4c57e1a4a7ba5622ee0ea33`, adding scale-aware eligibility
rather than reverting the commit wholesale.

## Next Concrete Action

1. Read `.agent/reviews/logical-ui-scaling-branch-review.md`.
2. Read Corrective checkpoint R0 in the main ExecPlan.
3. Inspect the original and current `skia_drawSurface`.
4. Locate the real build definition for `USE_WRITE_PIXELS`.
5. Restore the guarded path and add focused enabled/disabled validation.
6. Keep `skia_setPixel` physical-coordinate handling unless a focused test
   disproves it.

## Files to Read Now

- `.agent/reviews/logical-ui-scaling-branch-review.md`
- `.agent/logical-ui-scaling-execplan.md`, R0 only
- `.agent/guides/logical-ui-scaling-validation.md`, USE_WRITE_PIXELS section
- `TotalCrossVM/src/nm/ui/skia/skia_surface.cpp`
- the build file that defines `USE_WRITE_PIXELS`

Do not read all design files yet.

## Verified Foundations

- Branch starts from the recorded master.
- DP is zero and `UnitsConverter.toPixels` is identity.
- `LayoutUnit`, Graphics scale fields, and logical Image fields exist.
- Focused Java API/image tests pass.
- Current native macOS target compiles and links.

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

The previous execution used a Computer Use targeting attempt. It did not use the
required CoreGraphics window-ID plus `/usr/sbin/screencapture -l` workflow.

There is no established screenshot blocker until the direct workflow in
`.agent/guides/private-screenshot-capture.md` has been attempted. Screen
Recording permission may become a genuine external blocker.

## Resume Command

    cd /Users/flsobral/repos/totalcross-logical-ui
    git status --short -- \
      TotalCrossVM/src/nm/ui/skia \
      .agent
