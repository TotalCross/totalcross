<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Safe-area insets execution state

Rewrite this file at each milestone boundary. Read it first when resuming.

## Active checkpoint

- Base: `9ae29ec2e` after the milestone-0 commit.
- Branch: `feat/logical-ui-scaling`.
- Completed milestone: 1, core SDK safe-area model and focused tests.
- Active milestone: 2, `ScrollContainer` content insets.
- Protected task input: `.agent/safe-area-insets-execplan.md`; preserve its
  intent and living history while committing milestone updates locally.

## Validated evidence

- Scoped source paths were clean before implementation.
- Logical platform scaling exists through `screen.contentScale`.
- Current Android code pulls cached `safeInsets`; current iOS callback emits
  `screenChanged`, which must be replaced by the dedicated safe-area event.
- Milestone-0 `git diff --check` is the next boundary check after these support
  files are written.
- `SafeAreaLayoutTest`: 7 tests passed in
  `TotalCrossSDK/agent-logs/20260805-024752-test-full.log`.
- New public types are 20, 21, and 31 lines; the new test is 175 lines.

## Next exact action

Inspect only `ScrollContainer` viewport/content extent and scroll-position
methods, then implement non-negative content insets with anchor preservation and
`ScrollContainerContentInsetsTest`.

## Blockers

None.

## Resume command

    sed -n '1,220p' .agent/state/safe-area-insets.md
