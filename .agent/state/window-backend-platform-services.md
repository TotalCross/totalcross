<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# State: window-backend-platform-services

- Prerequisite commit: `29333efcf fix(event,macos): preserve event loop throttling`.
- Active milestone: complete.
- Active slice: none; plan execution and handoff are complete.
- Last functional commit: `8afc9cf8d refactor(window): separate backend and platform services`.
- Last documentation commit: pending closure commit.
- Active paths: `TotalCrossVM/src/nm/ui/Window.c`, the five backend headers,
  the five `WindowServices_c.h` adapters, Darwin service implementation, and
  `scripts/test-window-backend-platform-contracts.py`.
- Focused validation: five Window contract tests passed; production searches
  found SDL text-input ownership only in Plan 1's SDL event adapter;
  copyright validation passed for code and closure records; completed-range
  and worktree `git diff --check` passed; all new files are within limits;
  macOS Release Ninja and SDK `dist` builds passed.
- Commit-message validation: the commit title passed, but the body contains a
  literal escaped newline and exceeds the 80-character body-line limit. The
  plan forbids amending or rewriting history, so this remains recorded for
  handoff; later commits must use valid wrapped bodies.
- Blockers: none.
- Deferred validation: Windows, Linux, Android, iOS, and WinCE builds were not
  run per plan restrictions; no runnable Windows artifact or macOS keyboard
  sample was available for interactive smoke; CI was not run because the
  branch was not pushed.
- Unrelated local files intentionally left alone: existing untracked plans,
  prior-plan state/evidence/reports, and `TotalCrossSDK/IOSDateFixture.tcz`.
- Next concrete action: commit the closure plan/state/evidence/report, then
  leave the repository ready for handoff.
- Resume command: `sed -n '1,180p'
  .agent/state/window-backend-platform-services.md`
