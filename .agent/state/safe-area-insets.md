<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Safe-area insets execution state

Rewrite this file at each milestone boundary. Read it first when resuming.

## Active checkpoint

- Base: `2677c8ffc` after the milestone-4 commit.
- Branch: `feat/logical-ui-scaling`.
- Completed milestone: 5; every implementation and available validation gate is complete.
- Active milestone: none.
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
- Final suite: 19 tests passed in
  `TotalCrossSDK/agent-logs/20260805-031147-test-full.log`.
- Final changed-file copyright validation passed for 22 supported files;
  Objective-C headers were reconciled separately to the required 2026 chain.
- Committed task diff from `0ec107e0b9e3` contains 24 files and passes
  `git diff --check`.
- Every newly created source/test/state/evidence/history/editorial file is below
  20 KB and 600 lines. The supplied authoritative ExecPlan is excluded from the
  new-file limit and remains self-contained.

## Next exact action

No implementation action remains. When a configured Android device and a
launchable safe-area demo become available, run the prescribed portrait and
landscape smoke observations. Run iOS smoke only with a pre-existing runnable
artifact; do not infer a need for another build from this completed plan.

## Blockers

None. Device smoke is an environment limitation, not an implementation blocker.

## Resume command

    sed -n '1,220p' .agent/state/safe-area-insets.md
