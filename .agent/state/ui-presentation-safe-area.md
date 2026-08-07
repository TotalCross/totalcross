<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Safe presentation execution state

## Current position

- Status: active
- Active milestone: Milestone 2, internal presentation foundation
- Design section: `Internal presentation foundation`
- Next action: add the package-private host, entry, handle, controller, and transition types
- Base commit: `62c9c728cd0570c1e1a8219b42dfd72c6fedd355`
- Current branch: `feat/logical-ui-scaling2`
- Planning commit: `9a3b22ae1`
- Clipping commits: `536a7984c`, `c6e2f90bc`

## Preserved baseline

The checkout already contains the opt-in `PathAnimation` orthogonal-position
behavior, safe-area-aware animation staging in `SlidingWindow` and `TopMenu`,
and explicit `FILL` width for the `MaterialWindow` bar. These changes are local
source-of-truth work and must not be reconstructed from another worktree.

Unrelated untracked work exists under `.agent/` for image-text rendering and
other plans. It is outside this task and must remain unstaged.

## Completed validation

- `git diff --check` for the plan and design: passed.
- focused copyright validation for the plan and design: passed, 2 files.
- plan/design file-size check: passed; 20,309/463 and 18,779/496 bytes/lines.
- `ContainerClippingTest`: passed; log `m1-container-clipping.log`.
- `ClippedContainerTest`: passed; log `m1-clipped-container.log`.
- focused Milestone 1 copyright and static checks: passed.

## Deferred validation

SDK distribution, smoke compilation, and any macOS native smoke remain
prohibited until Milestones 1-4 are complete.

## New files

- `.agent/ui-presentation-safe-area-execplan.md`
- `.agent/design/ui-presentation-safe-area-implementation.md`
- `.agent/state/ui-presentation-safe-area.md`
- `.agent/evidence/ui-presentation-safe-area.md`
- `.agent/archive/ui-presentation-safe-area-history.md`
- `TotalCrossSDK/src/test/java/totalcross/ui/ContainerClippingTest.java`
- `TotalCrossSDK/src/test/java/totalcross/ui/ClippedContainerTest.java`

## Resume rule

Read this file, then the active milestone and named design section only. Inspect
scoped diffs before edits or staging.
