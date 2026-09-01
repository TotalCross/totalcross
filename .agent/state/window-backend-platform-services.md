<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# State: window-backend-platform-services

- Prerequisite commit: `29333efcf fix(event,macos): preserve event loop throttling`.
- Active milestone: Milestone 3 complete.
- Active slice: closed after final-HEAD CI validation.
- Last functional commit: `b63a6b64e fix(headers): align SIP constant provenance`.
- Previous header fix: `ffdab187f fix(headers): align new window service copyrights`.
- Previous rewritten functional/docs commits: `ab85fa051`, `ed78a0d10`.
- Last documentation record: `0770a193a docs(plan): record window closure validation`.
- Active paths: `TotalCrossVM/src/nm/ui/Window.c`, the five backend headers,
  the five `WindowServices_c.h` adapters, Darwin service implementation, and
  `scripts/test-window-backend-platform-contracts.py`.
- Focused validation: eight Window and eight SDL/event contract tests passed;
  production searches
  found SDL text-input ownership only in Plan 1's SDL event adapter;
  copyright validation passed for all touched/new files after applying the
  extracted-code provenance chain to `WindowSIP.h`; completed-range and
  worktree `git diff --check` passed; all new files are within limits; final
  macOS Release Ninja and SDK `dist` builds passed.
- Commit-message validation: rewritten commits `ab85fa051` and `ed78a0d10`
  pass with no literal `\\n` text. Closure commits `ffdab187f`,
  `183fc28bb`, `0770a193a`, and `b2a872553` have valid titles but body lines
  over 80 characters; the current objective forbids rewriting existing
  commits.
- Blockers: none. GitHub `Merge flow` run `33533439023` for final HEAD
  `b63a6b64e` passed checkout/copyright validation and every supported build
  job, including iOS, Windows SDL, and Windows Native+Legacy. The intentionally
  disabled `linux-arm32v7-cross` job was skipped.
- Deferred validation: Windows, Linux, Android, iOS, and WinCE builds were not
  run per plan restrictions; no runnable Windows artifact or macOS keyboard
  sample was available for interactive smoke.
- Unrelated local files intentionally left alone: existing untracked plans,
  prior-plan state/evidence/reports, and `TotalCrossSDK/IOSDateFixture.tcz`.
- Next concrete action: none; retain the final CI URL and local evidence for
  handoff.
- Resume command: `sed -n '1,180p'
  .agent/state/window-backend-platform-services.md`
