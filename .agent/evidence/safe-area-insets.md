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
