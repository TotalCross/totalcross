<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Logical UI scaling execution state

Rewrite this file instead of appending. It is the first read when resuming.

## Base and Branch

- Observed master at plan authoring:
  `d480df074e7fb6f5a32dfcc2f1f30c3949095e73`
- Actual fetched base: `d480df074e7fb6f5a32dfcc2f1f30c3949095e73`.
- Worktree: `/Users/flsobral/repos/totalcross-logical-ui`.
- Branch: `feat/logical-ui-scaling`.
- Last logical commit: `8f5f60089`.

## Active Milestone

Milestone 8: finalize documentation and handoff.

## Active Slice

Complete the final acceptance audit. The source audit has no remaining
control-level global-density reads or old DP-marker comparisons.

## Next Concrete Action

Attempt the remaining safe macOS window capture only through a process-targeted
path; preserve the current external blocker if the installed integration cannot
target the Java process.

## Files to Read Now

- `.agent/logical-ui-scaling-execplan.md`, Milestone 8 only.
- `.agent/guides/logical-ui-scaling-validation.md`, final acceptance section.

Do not read all design guides yet.

## Focused Validation Completed

- `git fetch origin master` and isolated worktree creation: passed.
- Static baseline audit: passed. `Launcher.getFont`, Skia text drawing, and
  Skia metric width paths multiply font size by `Settings.screenDensity`.
- `./gradlew-agent test --tests totalcross.ui.LogicalLayoutUnitTest --tests
  totalcross.ui.gfx.GraphicsScaleTest`: passed (5 tests).
- `cmake -S TotalCrossVM -B build-logical-ui -DCMAKE_BUILD_TYPE=Release -G Ninja`
  and `ninja -C build-logical-ui`: passed.
- `TotalCrossSDK/gradlew-agent test --tests totalcross.ui.LogicalLayoutUnitTest
  --tests totalcross.ui.gfx.GraphicsScaleTest`: passed (5 tests).
- `ninja -C build-logical-ui`: passed after logical Skia font sizing.
- `TotalCrossSDK/gradlew-agent test --tests totalcross.ui.gfx.GraphicsScaleTest`:
  passed (3 tests), including scale-1.5 logical-image rounding.
- `TotalCrossSDK/gradlew-agent test --tests totalcross.lang.Double4DTest --tests
  totalcross.ui.gfx.GraphicsScaleTest`: passed (5 tests). `Double4D.isFinite`
  rejects NaN and both infinities; the test also exposed and the slice fixed
  the preexisting invalid equality-based `Double4D.isNaN` implementation.
- `TotalCrossSDK/gradlew-agent dist -x test --warning-mode=none --console=plain`:
  passed (29 seconds; agent log
  `TotalCrossSDK/agent-logs/20260801-161312-dist-agent.log`). This confirms
  deployed code resolves `Double.isFinite` through `Double4D`.
- `TotalCrossSDK/gradlew-agent test --tests totalcross.ui.image.DanfeScalingTest
  --tests totalcross.ui.gfx.GraphicsScaleTest`: passed. The fixture checks 360x540
  and 720x1080 PNG dimensions and 31 physical barcode runs at scale 1 and 2.
- `TotalCrossSDK/gradlew-agent test --tests totalcross.ui.image.DanfeScalingTest
  --tests totalcross.ui.gfx.GraphicsScaleTest`: passed after the added alpha-128,
  odd-row raw-pixel preservation assertion and logical-to-physical fill check.
- `ninja -C build-logical-ui`: passed (no work required after the Java-only
  synchronization fixture update).
- `TotalCrossSDK/gradlew-agent test --tests totalcross.ui.LogicalLayoutUnitTest
  --tests totalcross.ui.gfx.GraphicsScaleTest --tests
  totalcross.ui.image.DanfeScalingTest`: passed after removing six initial
  control-level density dependency groups (6 focused tests).
- `TotalCrossSDK/gradlew-agent dist -x test --warning-mode=none --console=plain`:
  passed after the final density audit slice (35 seconds; agent log
  `TotalCrossSDK/agent-logs/20260801-163521-dist-agent.log`).
- Final static audits: no `Settings.screenDensity` reads remain outside the two
  deprecated compatibility assignments in `Launcher`; no old DP-marker
  comparisons were found; every added file is below 20 KiB and 600 lines.

## Deferred Validation

Deployed text containment, Java/Skia and non-Skia equivalence, native-to-Java
pixel readback, and a sanitized window-only screenshot are not independently
proven. The user explicitly directed macOS-only platform validation; Android and
iOS workspace validation are deferred to final validation rather than treated
as Milestone 7 gates.

## Audit Observations

- Milestone 8's scoped audit found remaining control-level global-density reads
  in `Button`, `TopMenu`, `Edit`, `ListContainer`, `Toast`, and
  `SideMenuContainer`. These must not remain as hidden logical-layout or
  rendering dependencies. Platform initialization reads in `Launcher` and
  native settings remain compatibility mirrors and are intentionally out of
  this focused control slice.
- `Button`, `TopMenu`, `Edit`, `ListContainer`, `Toast`, and
  `SideMenuContainer` were removed from the audit set in `3c119d17b`.
- The final source scan leaves only `Launcher` assignments to the deprecated
  compatibility mirror; it is not a rendering or layout read.

## Active Decisions

- Start from current `origin/master`.
- Ignore source changes from earlier plans in this session.
- Use `double` for fractional API and implementation calculations.
- Do not modify the deployer.
- Keep every new file below 20 KiB and approximately 600 lines.

## Blockers

- Safe macOS window capture is currently unavailable through the installed
  Computer Use integration: it cannot target the launched Java process by its
  process-specific application identity. A second process-targeted attempt at
  simulated scale 2 returned the same unsupported-target result. No desktop-wide
  fallback was used.

## Deliberately Out of Scope

- unrelated renderer refactors;
- packaging or release changes not required by this API;
- importing earlier implementation patches;
- closing or updating issue #433 without explicit user instruction.

## Resume Command

cd /Users/flsobral/repos/totalcross-logical-ui
cmake -S TotalCrossVM -B build-logical-ui -DCMAKE_BUILD_TYPE=Release -G Ninja
