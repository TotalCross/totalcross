<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Pre-image optimization master 01 correctness state

- Active milestone/slice: Plan 1 complete; handoff to plan 2.
- Branch: `fix/pre-image-optimization-master`.
- BASE_SHA: `1898014784b2fba5716cc033e49520740b05f0dd`.
- Last logical commit: `221c259ab` (`docs(image): complete targeted jpeg
  correction`); implementation commit: `061a1dde9`; bootstrap commit:
  `bee546112`.
- Active paths: `TotalCrossSDK/src/main/java/totalcross/ui/image/`; focused
  image tests and smoke registration; targeted native JPEG/image paths named by
  the plan.
- Next exact action: execute `.agent/plans/pre-image-optimization-master-02-native-swap.md`
  from this exact HEAD without rebasing or recreating the branch.
- Focused validation completed: `totalcross.ui.image.*` SDK tests passed;
  `dist -x test` passed; Release macOS software-Skia CMake/Ninja build passed;
  exact-dylib `runImageJpegPinchSmokeMacOS` passed; focused header validation,
  `git diff --check`, and commit-message validation passed.
- Deferred validation: Android, iOS, Windows, Linux, packaging, and full
  platform matrix remain intentionally skipped by the plan's restrictions.
- Decisions still active: preserve adaptive smooth denominators; repair scale
  metadata at fresh and cached materialization boundaries; keep nearest,
  rotate-scale, and explicit JPEG factories eager/full-decode.
- PLAN1_HEAD: `221c259ab`.
- Blocker: none.
- Deliberate out-of-scope local files: unrelated untracked `.agent/evidence/`,
  `.agent/plans/`, `.agent/reports/`, and `.agent/state/` artifacts listed by
  scoped status; do not stage them.
- Resume command: read this file, then execute
  `.agent/plans/pre-image-optimization-master-02-native-swap.md` from
  `221c259ab`.
