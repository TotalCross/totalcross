<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Safe presentation milestone history

## Milestone 0 — local checkout reconciliation

The execution began on `feat/logical-ui-scaling2` at `62c9c728c`. The checkout
contains the earlier safe-area/animation fixes and is ahead of the separate
`feat/logical-ui-scaling` worktree, so execution remains here. Unrelated local
`.agent/` files were recorded and left untouched. The new ExecPlan and design
guide passed focused static, header, and size checks and were committed as
`9a3b22ae1`.

## Milestone 1 — clipping contract and culling correctness

`536a7984c` added a package-private, default-enabled clipping policy and taught
`Control.refreshGraphics` to consult it without changing translation.
`ContainerClippingTest` proves opt-out at one ancestor remains bounded by the
next clipping ancestor. `c6e2f90bc` fixed `ClippedContainer` range handling,
`-1` sentinels, and empty/offscreen cache behavior. Its two focused tests pass.

## Milestone 2 — internal presentation foundation

`cd5082a1d` added package-private route/overlay layers, barriers, clipped safe
viewports, frames, handles, a controller, and explicit-coordinate slide
transitions. `Window` lazily owns the host and relayouts it on safe-area changes.
Focused tests prove geometry, lifecycle cleanup, content retention, and no
top-level window-stack mutation.

## Milestone 3 — SlidingWindow and MaterialWindow

`631badefd` changed `SlidingWindow` from `Window` to `Container` and composed it
with the internal controller. Explicit local slide/fade transitions replace
screen staging. `MaterialWindow` preserves its bar layout and now loads delayed
provider content once through an insertion hook. Focused tests cover four
directions, slack, lifecycle, relayout identity, and stack isolation.

## Milestone 4 — TopMenu and SideMenuContainer

`565b89e37` moved TopMenu into overlay presentation, removed internal safe-area
padding, preserved fixed-bar/scroll modes, and added local title/border state.
`f0a918d97` updated the sample close action required by lost Window assignability.
`56544d833` made SideMenu gesture handling local, deferred drawer width to safe
viewport layout, and relayouted expanding submenus through their TopMenu.

## Milestone 5 — smoke, final validation, and handoff

`f1601b2e6` added a compact programmatic smoke fixture. The seven-class focused
suite passed 16 tests, the one final non-clean SDK distribution passed, and the
fixture compiled and passed under JavaSE with deterministic nonzero safe insets.

The existing macOS path was reusable. CMake configuration and the `tcvm` target
build passed, the fixture deployed from a jar, the deployed dylib matched the
built dylib byte-for-byte by SHA-256, and the direct native executable reported
`final=PASS`. No Android or iOS build/deploy was attempted. Final checks covered
scoped diffs, copyrights, new-file sizes, and repository compilation. Detailed
claims and limitations are preserved in the evidence and editorial report.
