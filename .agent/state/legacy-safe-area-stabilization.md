<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Legacy safe-area stabilization state

- Updated: 2026-08-12T20:02:07Z
- Active milestone: none — milestone 6 complete
- Branch: `feat/logical-ui-scaling2`
- Baseline HEAD: `09dc39143c7edf0363ad3c1670bdd16e141b4572`
- Last implementation commit: this follow-up commit
- Last validation: 22 focused mandatory tests and one visual smoke passed;
  focused headers and diff check passed
- Blockers: none

## Baseline

The local branch name differs from the requested checkout name, but HEAD is
exactly `origin/feat/logical-ui-scaling`. Target tracked paths are clean. The
repository contains many unrelated untracked files; they are outside this task
and must remain untouched. The full baseline is stored in
`artifacts/legacy-safe-area-stabilization/logs/milestone-0-baseline.log`.

Historical reconstruction checkpoints:

- fade boundary: `91616c934fe547c7b1b01c4a2990cb17ac865241`
- SlidingWindow/MaterialWindow: `8dc8af69bddeaa4b4341386a886268ad14e8443e`
- TopMenu: `54a7bd5b49369737b5d17292597d7acbe7d95d31`
- SideMenuContainer: `f0a918d97e57b0e4868019256701d6f349904c87`
- Window coupling boundary: `b4002cdbae736fcd3f17d48db7ad70cc087b41e3`

## Milestone 1 outcome

Restored checkpoint behavior in `Control`, `ControlAnimation`, and
`FadeAnimation` while retaining current headers. Removed repair-only screenshot,
animation-fade, presentation-fade tests, and the presentation fade smoke. Full
test output is in `milestone-1-safe-area-test-full.log` under the task logs.

## Milestone 2 outcome

Restored both Window-based classes from `8dc8af69...`, kept current provenance
headers, and extracted package-private popup preparation for direct geometry
coverage. Final safe geometry precedes the temporary animation origin.

## Milestone 3 outcome

Commits `82b2ffff5` and `9e430ce30` restored TopMenu and SideMenu to their
Window-based path. Horizontal automatic drawer width is
`max(1, min(320, safeWidth - 56))`; later explicit caller assignments win.

## Milestone 4 outcome

Removed Window coupling and moved eight package-private sources. Deferred code
uses existing public APIs; clipping was dropped, blocking quit detection was
simplified, and content is always filled because runtime parity is deferred.
Audit `2026-08-10-9e430ce304f4-a44665341061-min24` found high-confidence lineage for
seven material files and remains pending human review.

## Milestone 5 validation

- JavaSE preview: passed; ten inspected PNGs cover transitions, endpoints,
  reopen, resize, outside dismissal, Back/Escape, and background dimming
- Focused safe-area set: 18 tests passed
- Non-clean SDK distribution: passed in 50 seconds
- Smoke compilation: passed; native macOS jar deploy/run ended `final=PASS`
- Android/iOS: not run by plan

## Milestone 6 outcome

- Deterministic TopMenu portrait/landscape geometry, SideMenu widths 208/320,
  SlidingWindow safe relayout/reuse, and synchronous z-stack behavior now run
  under mandatory JUnit.
- Preview rendering, real animations, input injection, polling, and PNG output
  moved to `LegacySafeAreaVisualSmokeTest` under the existing smoke source set.
- The minimal repeated-preview test did not reproduce `NullPointerException:
  target`; it reproduced stale MainWindow/window-stack state on session two.
  `Launcher` now calls the existing preview reset before creating each preview
  application, while EventLoop continues to require a non-null MainClass.
- Focused mandatory and visual smoke validation passed. Full SDK distribution,
  platform builds, packaging, deployment, and publishing were not run.

## Active paths

- None.

## Next command

None. Preserve the local commit and do not push.
