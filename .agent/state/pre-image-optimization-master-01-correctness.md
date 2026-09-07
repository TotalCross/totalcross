<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Pre-image optimization master 01 correctness state

- Active milestone/slice: Plan 1 complete; handoff to plan 2.
- Branch: `fix/pre-image-optimization-master`.
- BASE_SHA: `1898014784b2fba5716cc033e49520740b05f0dd`.
- Final production implementation/test commit:
  `8156f62f9cdf41b6d2cd2e18b7ba4b4704ad98b2` (`fix(image): publish targeted
  jpeg metadata transactionally`); bootstrap commit: `bee546112`.
- Active paths: `TotalCrossSDK/src/main/java/totalcross/ui/image/`; focused
  image tests and smoke registration; targeted native JPEG/image paths named by
  the plan.
- Next exact action: execute `.agent/plans/pre-image-optimization-master-02-native-swap.md`
  from the current tip. First verify `PLAN1_IMPLEMENTATION_HEAD` is an
  ancestor, audit later commits against plan 2's handoff-only path allowlist,
  and record the actual tip as `PLAN2_BASE`; do not rebase or recreate the
  branch.
- Focused validation completed after the implementation commit:
  `totalcross.ui.image.*` SDK tests passed; `dist -x test` passed; fresh
  Release macOS software-Skia CMake/Ninja build passed; exact-dylib
  `runImageJpegPinchSmokeMacOS` passed with `overallPass=true`; focused header
  validation and `git diff --check` remain required for the handoff commit.
- Deferred validation: Android, iOS, Windows, Linux, packaging, and full
  platform matrix remain intentionally skipped by the plan's restrictions.
- Decisions still active: preserve adaptive smooth denominators; repair scale
  metadata at fresh and cached materialization boundaries; keep nearest,
  rotate-scale, and explicit JPEG factories eager/full-decode.
- PLAN1_IMPLEMENTATION_HEAD:
  `8156f62f9cdf41b6d2cd2e18b7ba4b4704ad98b2`.
- CURRENT_BRANCH_HEAD: this handoff documentation commit; plan 2 must resolve
  it with `git rev-parse HEAD` and record that value as `PLAN2_BASE`.
- Commit-message note: the implementation commit has one 84-character body
  line; the user forbade amending it, so the history is preserved as-is.
- Blocker: none.
- Deliberate out-of-scope local files: unrelated untracked `.agent/evidence/`,
  `.agent/plans/`, `.agent/reports/`, and `.agent/state/` artifacts listed by
  scoped status; do not stage them.
- Resume command: read this file, then execute
  `.agent/plans/pre-image-optimization-master-02-native-swap.md` from the
  current branch tip.
