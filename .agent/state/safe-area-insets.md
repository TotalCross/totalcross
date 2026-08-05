<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Safe-area insets execution state

Rewrite this file at each milestone boundary. Read it first when resuming.

## Active checkpoint

- Base: `e73443c44` after the milestone-3 commit.
- Branch: `feat/logical-ui-scaling`.
- Completed milestone: 4, Android/iOS delivery, focused tests, and Android checkpoint.
- Active milestone: 5, final validation, smoke attempts, and reporting.
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
- Milestone-4 focused subset: 14 tests passed in
  `TotalCrossSDK/agent-logs/20260805-030820-test-full.log`.
- Sole Android checkpoint: `:app:assembleStandardDebug` passed in 28 seconds;
  full log is `build/safe-area-insets/android-build.log`.
- Generated JNI header contains `nativeSafeAreaInsetsChanged`; it remains a
  generated build file and is not staged.

## Next exact action

Run the complete focused Java set once, focused copyright validation, scoped
diff/static checks, and smoke-test availability checks using existing artifacts
only. Finish all plan, state, evidence, history, and editorial outcomes without
rerunning either checkpoint build.

## Blockers

None.

## Resume command

    sed -n '1,220p' .agent/state/safe-area-insets.md
