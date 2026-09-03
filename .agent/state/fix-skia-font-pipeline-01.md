<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# State — fix-skia-font-pipeline-01

- Active milestone/slice: Plan 1 complete; both implementation milestones and
  permitted macOS validation are finished.
- Last logical commits: `bfac1883c` (`fix(font): separate skia and legacy
  loading`) and `7f4f11d2a` (`fix(font): make skia ttf resolution
  deterministic`); baseline `868294ba1` on
  `fix/windowing-clickthrough-fontname`.
- Modified paths: committed `TotalCrossVM/src/nm/ui/font_Font.c`,
  `TotalCrossVM/src/nm/ui/PalmFont_c.h`,
  `TotalCrossVM/src/nm/ui/GraphicsPrimitivesText_c.h`, and
  `TotalCrossVM/src/nm/ui/font_Font_test.h`; this state file is local support
  state; unrelated untracked worktree content remains out of scope.
- Next concrete action: none for Plan 1; proceed to Plan 2 only as a separate
  user-requested execution.
- Focused checks completed: scoped diff/status; header validation; staged and
  committed diff checks; Milestone 1 and Milestone 2 macOS Skia Ninja builds;
  and the available `window_startup_native_test` smoke after each milestone.
  Logs: `/tmp/fix-skia-font-pipeline-01-m1-build.log`,
  `/tmp/fix-skia-font-pipeline-01-m1-smoke.log`,
  `/tmp/fix-skia-font-pipeline-01-m2-build.log`, and
  `/tmp/fix-skia-font-pipeline-01-final-smoke.log`. Both checkpoint message
  checks found an overlong body line; no amend is allowed by repository policy.
- Milestone-end build/smoke: passed. No font-specific native test target is
  configured in the available macOS CMake build, so that check was not run.
- Blocker: none; message-format exception recorded above.
- Resume command: `cd /Users/flsobral/repos/totalcross-github && sed -n '1,160p' .agent/state/fix-skia-font-pipeline-01.md`
