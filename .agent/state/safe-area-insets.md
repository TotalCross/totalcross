<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Safe-area insets execution state

Rewrite this file at each milestone boundary. Read it first when resuming.

## Active checkpoint

- Base: `0ec107e0b9e3`.
- Branch: `feat/logical-ui-scaling`.
- Completed milestone: 0, scoped baseline and support-file initialization.
- Active milestone: 1, core SDK safe-area model.
- Protected task input: `.agent/safe-area-insets-execplan.md`; preserve its
  intent and living history while committing milestone updates locally.

## Validated evidence

- Scoped source paths were clean before implementation.
- Logical platform scaling exists through `screen.contentScale`.
- Current Android code pulls cached `safeInsets`; current iOS callback emits
  `screenChanged`, which must be replaced by the dedicated safe-area event.
- Milestone-0 `git diff --check` is the next boundary check after these support
  files are written.

## Next exact action

Inspect the narrow layout methods in `Control.java`, `Container.java`,
`Window.java`, and `MainWindow.java`, then implement the three public safe-area
types and the window/control/container policy with `SafeAreaLayoutTest`.

## Blockers

None.

## Resume command

    sed -n '1,220p' .agent/state/safe-area-insets.md
