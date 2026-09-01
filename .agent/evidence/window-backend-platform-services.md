<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Evidence: window-backend-platform-services

- 2026-09-01 | `8afc9cf8d` | Milestones 1-2 | focused implementation
  validation | PASS | `python3 scripts/test-window-backend-platform-contracts.py`
  (5 tests), focused copyright validation (12 files), and scoped
  `git diff --check` passed. New service adapters and focused contract test are
  all below the plan size limits.
- 2026-09-01 | `8afc9cf8d` | Milestone 1 | macOS build | PASS |
  `cmake --build build-macos-window-event --target tcvm Launcher`; compatible
  Release SDL + Skia + software configuration; full log at
  `/tmp/window-backend-macos-m1-build.log`.
- 2026-09-01 | `8afc9cf8d` | Milestone 2 | ownership search | PASS |
  SDL text-input calls remain only in `TotalCrossVM/src/event/sdl/event_c.h`;
  Window backend/service headers contain none.
- 2026-09-01 | `8afc9cf8d` | Milestone 3 | final release validation | PASS |
  final focused contracts, header validation, range/worktree diff checks, and
  new-file size checks passed. Final macOS incremental build passed with log
  `/tmp/window-backend-macos-final-build.log`; SDK `dist` passed with log
  `/tmp/window-backend-sdk-dist.log`.
- 2026-09-01 | `ffdab187f` | closure fix | header policy | PASS |
  Android and Darwin new service headers now use only the 2026 Amalgam header
  and LGPL-2.1-only line; focused header validation passed.
- 2026-09-01 | `183fc28bb` | closure fix | safe-area contract | PASS |
  macOS, Linux, and Windows/WinCE no-op adapters explicitly assign all four
  insets to zero. The focused contract suite passed with 7 tests.
- 2026-09-01 | `183fc28bb` | final validation | PASS |
  all touched/new header checks, range/worktree diff checks, size checks, final
  macOS build (`/tmp/window-backend-macos-closure.log`), and SDK `dist`
  (`/tmp/window-backend-sdk-closure.log`) passed.
- 2026-09-01 | `183fc28bb` | CI gate | NOT AVAILABLE |
  GitHub API returned `No commit found for SHA`; `gh run list --commit`
  returned no runs. The full matrix cannot be considered green without a push
  or another external CI run.
