<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Legacy safe-area stabilization state

- Updated: 2026-08-10T17:39:06Z
- Active milestone: 1 — restore the pre-fade-repair animation baseline
- Branch: `feat/logical-ui-scaling2`
- HEAD: `09dc39143c7edf0363ad3c1670bdd16e141b4572`
- Last task commit: none
- Last validation: milestone 0 `git diff --check` passed
- Blockers: none

## Baseline

The local branch name differs from the requested checkout name, but HEAD is
exactly `origin/feat/logical-ui-scaling`. Target tracked paths are clean. The
repository contains many unrelated untracked files; they are outside this task
and must remain untouched. The full baseline is stored in
`artifacts/legacy-safe-area-stabilization/logs/milestone-0-baseline.log`.

Historical reconstruction checkpoints:

- fade boundary: `91616c934fe547c7b1b01c4a2990cb17ac865241`
- SlidingWindow/MaterialWindow: `8dc8af69bddeaa4b4341386a886268ad14e8443e`
- TopMenu: `54a7bd5b49369737b5d17292597d7acbe7d95d31`
- SideMenuContainer: `f0a918d97e57b0e4868019256701d6f349904c87`
- Window coupling boundary: `b4002cdbae736fcd3f17d48db7ad70cc087b41e3`

## Active paths

- `TotalCrossSDK/src/main/java/totalcross/ui/Control.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/anim/ControlAnimation.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/anim/FadeAnimation.java`
- fade-repair-only tests and smoke paths identified from history

## Next command

Compare the three generic UI files and post-boundary test additions against
`91616c934fe547c7b1b01c4a2990cb17ac865241`.
