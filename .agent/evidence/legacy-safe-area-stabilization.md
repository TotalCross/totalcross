<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Legacy safe-area stabilization evidence

## Milestone 0 — baseline

- Timestamp: 2026-08-10T17:39:06Z
- Local branch: `feat/logical-ui-scaling2`
- Baseline HEAD and requested remote branch:
  `09dc39143c7edf0363ad3c1670bdd16e141b4572`
- Target tracked paths were clean; unrelated untracked paths are recorded in
  `artifacts/legacy-safe-area-stabilization/logs/milestone-0-baseline.log`
- `git diff --check`: passed; tests intentionally not run

## Milestone 1 — legacy fade infrastructure

- Timestamp: 2026-08-10T17:44:27Z
- Commit: `d74000089`
- Restored `91616c934...` behavior in `ControlAnimation`/`FadeAnimation`
  and only the checkpoint's screenshot changes in `Control`
- Removed repair-only screenshot/fade tests and presentation fade smoke
- `SafeAreaLayoutTest`: passed; focused headers and diff check passed

## Milestone 2 — SlidingWindow and MaterialWindow

- Timestamp: 2026-08-10T17:49:52Z
- Commit: `efb9b7ff6`
- Restored both Window implementations from `8dc8af69...`; final geometry and
  child layout precede the temporary animation origin
- Replaced presentation-state coverage with `SlidingWindowSafeAreaTest`
- Directional/resize cases passed; CENTER passed on focused rerun after adding
  missing MainWindow harness setup
- Focused headers/diff check passed; test is 3,932 bytes/129 lines

## Milestone 3 — TopMenu and SideMenuContainer

- Timestamp: 2026-08-10T17:56:14Z
- Commits: `82b2ffff5`, `9e430ce30`
- Restored TopMenu from `54a7bd5...` and SideMenuContainer from `f0a918d...`
- Automatic horizontal width is `max(1, min(320, safeWidth - 56))`; later
  explicit caller width assignments remain untouched
- SideMenu/shared tests passed; TopMenu passed after correcting test
  expectations from presentation viewport to legacy Window safe client
- Focused headers/diff check passed; tests are 4,844/3,567 bytes

## Milestone 4 — deferred presentation isolation

- Timestamp: 2026-08-10T18:02:41Z
- Commit: `a44665341`
- Removed Window coupling; moved eight sources to
  `totalcross.ui.presentation` with 86–97% Git rename similarity
- Adapted three package-only dependencies without widening legacy APIs;
  deferred host compilation/test passed in 14 seconds
- Deferred limitations: no viewport clip setter, no package-private app-quit
  check, and content uses public fill layout
- Ten files passed headers; all new files passed size limits
- Audit `2026-08-10-9e430ce304f4-a44665341061-min24` is pending human review;
  seven material moves are inherited/high confidence with preserved headers

## Milestone 5 — release validation

- Timestamp: 2026-08-10T18:23:54Z
- Commit: `b3fcb3111`
- JavaSE preview visual test passed after the injected Escape code was corrected
  to `SpecialKeys.ESCAPE`; the expanded reopen run passed in 30 seconds
- Ten inspected PNGs cover portrait/landscape, asymmetric horizontal insets,
  TopMenu transition/endpoints/reopen, SlidingWindow BOTTOM/LEFT/reopen,
  SideMenu LEFT/RIGHT, outside dismissal, Back/Escape, and background dimming
- All PNGs are below 10 KiB; visual artifacts are under
  `artifacts/legacy-safe-area-stabilization/visual/`
- Final focused set passed 18 tests: SlidingWindow 3, TopMenu 4, SideMenu 4,
  and shared safe-area 7
- Non-clean `dist -x test`: passed in 50 seconds; full log is
  `artifacts/legacy-safe-area-stabilization/logs/sdk-dist.log`
- Replacement smoke compilation: passed in 6 seconds
- Native macOS jar deploy and executable run: passed with expected geometry and
  `final=PASS`; direct single-class deploy was rejected by the deployer's
  reserved-package rule and was replaced with the required jar flow
- Android/iOS builds were not run, as required by the plan
- Final `git diff --check`: passed
- Header validator: 27 files passed with zero changes
- New tracked files: 9; maximum 20,410 bytes/479 lines
- Pending audit: 16 files; maximum 19,751 bytes/560 lines
- No visual PNG exceeds 10 KiB

## Milestone 6 — deterministic/visual coverage separation

- Timestamp: 2026-08-12T20:02:07Z
- Initial lifecycle regression failed as intended because the second preview
  retained the first MainWindow; it did not reproduce `NullPointerException:
  target`. Full log: `TotalCrossSDK/agent-logs/20260812-165642-test-full.log`
- After the narrow preview reset, the lifecycle regression passed. Full log:
  `TotalCrossSDK/agent-logs/20260812-165738-test-full.log`
- Focused safe-area and lifecycle lane passed 22 tests in 8 seconds. Full log:
  `TotalCrossSDK/agent-logs/20260812-165948-test-full.log`
- Optional `LegacySafeAreaVisualSmokeTest` passed in 15 seconds and generated
  the existing portrait/landscape PNG artifact set. Full log:
  `TotalCrossSDK/agent-logs/20260812-170251-smokeTest-full.log`
- Nine changed first-party files passed the focused header validator with zero
  changes; `git diff --check` passed.
- Full SDK distribution, Android/iOS builds, packaging, deployment, and
  publishing were intentionally not run.
