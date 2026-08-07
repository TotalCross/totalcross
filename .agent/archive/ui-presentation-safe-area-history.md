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
