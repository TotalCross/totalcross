<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Safe-area insets evidence

This file is append-only. Store verbose logs under `build/safe-area-insets/`.

## Milestone 0: scoped baseline

- Timestamp: 2026-08-05T05:42:31Z
- Base: `0ec107e0b9e3`
- Branch: `feat/logical-ui-scaling`
- Commands: scoped `git status`, targeted safe-area/layout `rg` inventory, and
  logical-scale/surface-lifecycle `rg` inventory.
- Status: passed without building.
- Result: only the supplied untracked ExecPlan was present in scope. Android
  already converts physical input with `screen.contentScale`; iOS currently
  maps `viewSafeAreaInsetsDidChange` to `screenChanged`/`SK_SCREEN_CHANGE`.
- Objective update: focused local milestone commits are explicitly required;
  remote publication remains prohibited.

## Milestone 1: core SDK safe-area model

- Timestamp: 2026-08-05T05:48:20Z
- Command: `TotalCrossSDK/gradlew-agent test --tests
  'totalcross.ui.SafeAreaLayoutTest'`
- Status: passed, 7 tests, 0 failures, 0 errors.
- Full log: `TotalCrossSDK/agent-logs/20260805-024752-test-full.log`.
- Result: window modes and selected/touched edges, forced safe/full-bleed child
  placement, declared inset preservation and negative cancellation, safe-padding
  deduplication, invalid-input rejection, and one callback per changed update are
  covered. Identical updates return false and do not notify.
- Build status: not run, as required before the milestone-3 SDK checkpoint.

## Milestone 3: fixed menu bars and layout models

- Timestamp: 2026-08-05T06:01:15Z
- Focused command: `TotalCrossSDK/gradlew-agent test --tests
  'totalcross.ui.SafeAreaLayoutTest' --tests
  'totalcross.ui.ScrollContainerContentInsetsTest' --tests
  'totalcross.ui.TopMenuSafeAreaTest'`
- Status: passed, 19 tests, 0 failures, 0 errors.
- Full test log: `TotalCrossSDK/agent-logs/20260805-030018-test-full.log`.
- SDK checkpoint command: `TotalCrossSDK/gradlew-agent dist -x test
  --warning-mode=none --console=plain`.
- SDK checkpoint status: passed in 25 seconds.
- Checkpoint log: `build/safe-area-insets/sdk-dist.log`; wrapper logs:
  `TotalCrossSDK/agent-logs/20260805-030050-dist-agent.log` and
  `TotalCrossSDK/agent-logs/20260805-030050-dist-full.log`.
- Result: attached-edge selection, fixed safe-padded bars, reserve/overlay
  viewport bounds, content insets, all four presets, dynamic updates without
  body recreation, and side-menu forwarding are covered.
- Corrected preliminary failures: test-only timer backend initialization,
  zero-sized hosts before `FILL`, and stale saved bounds during dynamic
  reposition. The final suite is clean.

## Milestone 2: scroll content insets

- Timestamp: 2026-08-05T05:53:20Z
- Command: `TotalCrossSDK/gradlew-agent test --tests
  'totalcross.ui.ScrollContainerContentInsetsTest'`
- Status: passed, 5 tests, 0 failures, 0 errors.
- Full log: `TotalCrossSDK/agent-logs/20260805-025313-test-full.log`.
- Result: viewport bounds remain unchanged; maxima grow by leading plus trailing
  insets exactly once; first and last content are reachable; origin, middle, and
  trailing anchors survive changes; identical values are inert and negatives
  are rejected.
- Build status: not run, as required before the milestone-3 SDK checkpoint.
