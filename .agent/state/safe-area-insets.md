<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Safe-area insets execution state

Rewrite this file at each milestone boundary. Read it first when resuming.

## Active checkpoint

- Base: `24414c985` after the milestone-2 commit.
- Branch: `feat/logical-ui-scaling`.
- Completed milestone: 3, menu integration, focused tests, and SDK checkpoint.
- Active milestone: 4, dynamic Android/iOS delivery and Android checkpoint.
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
- `ScrollContainerContentInsetsTest`: 5 tests passed in
  `TotalCrossSDK/agent-logs/20260805-025313-test-full.log`.
- Its new test file is 120 lines; content insets preserve the viewport and
  origin/middle/trailing anchors.
- Combined milestone-3 suite: 19 tests passed in
  `TotalCrossSDK/agent-logs/20260805-030018-test-full.log`.
- Sole SDK distribution checkpoint: passed in 25 seconds; concise/full logs are
  `TotalCrossSDK/agent-logs/20260805-030050-dist-agent.log` and
  `TotalCrossSDK/agent-logs/20260805-030050-dist-full.log`.

## Next exact action

Inspect the Android root-insets listener/JNI path and iOS main-view/event queue,
then add the common native physical-to-logical helper and dedicated push events.
Run only the prescribed focused SDK subset and the sole Android checkpoint.

## Blockers

None.

## Resume command

    sed -n '1,220p' .agent/state/safe-area-insets.md
