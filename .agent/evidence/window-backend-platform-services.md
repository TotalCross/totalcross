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
- 2026-09-01 | `0770a193a` | CI gate | NOT AVAILABLE |
  GitHub API returned `No commit found for SHA`; `gh run list --commit`
  returned no runs. The full matrix cannot be considered green without a push
  or another external CI run.
- 2026-09-01 | `0770a193a` | commit-message validation | PARTIAL |
  Rewritten commits `ab85fa051` and `ed78a0d10` pass. Closure commits
  `ffdab187f`, `183fc28bb`, and `0770a193a` have valid titles but body lines
  over 80 characters; they remain unchanged under the no-rewrite instruction.
- 2026-09-01 | `ed78a0d10` | remote CI context | FAIL |
  The latest remote `Merge flow` run failed in `Validate commit`; all build
  jobs were skipped. Final local `HEAD` `0770a193a` has no GitHub check-run.
- 2026-09-01 | `b2a872553` | SIP constant centralization | PASS |
  `WindowSIP.h` is the single source for all six SIP values; Window and Darwin
  SIP consumers use it, and the focused Window/event suites each pass 8 tests.
  The permitted macOS VM/Launcher and SDK `dist` builds also passed.
- 2026-09-01 | `b2a872553` | commit-message validation | PARTIAL |
  The new commit title is valid, but its body lines exceed 80 characters; it
  remains unchanged under the no-rewrite instruction.
- 2026-09-01 | `b2a872553` | header validation | BLOCKED |
  The requested 2026-only new-file header is present, but the repository
  validator detects the enum's extraction from `Window.h` and requests the
  historical inherited chain. Adding that chain would violate the explicit
  new-file header requirement.
- 2026-09-01 | `b2a872553` | final CI gate | NOT AVAILABLE |
  GitHub run `33533007596` recognizes the commit, but `Validate commit` fails
  on extracted-code copyright lineage for `WindowSIP.h`; iOS and Windows jobs
  are skipped. No push was performed by the agent.
- 2026-09-01 | `b63a6b64e` | header correction | PASS |
  `python3 scripts/validate-copyright-headers.sh --fix --files
  TotalCrossVM/src/nm/ui/WindowSIP.h` added the validator-required
  `2020-2021 TotalCross` and `2022-2026 Amalgam` provenance chain; focused
  header validation, Window contract tests, and staged diff checks passed.
- 2026-09-01 | `b63a6b64e` | final CI gate | PASS |
  GitHub `Merge flow` run `33533439023` passed checkout/copyright validation,
  iOS, Windows SDL, Windows Native+Legacy, Android, Linux, macOS, and SDK.
  The `linux-arm32v7-cross` job was intentionally skipped.
