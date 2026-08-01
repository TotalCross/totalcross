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
- Last logical commit: `21d6958a3a250dd435655319b7cd24071d7af1f3`.

## Active Milestone

Milestone 7: run end-to-end DANFE and platform validation.

## Active Slice

Perform the final source and documentation audits. The headless fixture covers
logical/physical image dimensions, PNG dimensions, scaled barcode structure, and
Java-side raw-pixel preservation; the deployed fixture starts at simulated
macOS scales 1 and 2.

## Next Concrete Action

Read Milestone 8 and its explicitly required supporting material, then perform
the scoped deprecation, Javadoc, file-size, and validation audits.

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

## Deferred Validation

Deployed text containment, Java/Skia and non-Skia equivalence, native-to-Java
pixel readback, and a sanitized window-only screenshot are not independently
proven. The user explicitly directed macOS-only platform validation; Android and
iOS workspace validation are deferred to final validation rather than treated
as Milestone 7 gates.

## Active Decisions

- Start from current `origin/master`.
- Ignore source changes from earlier plans in this session.
- Use `double` for fractional API and implementation calculations.
- Do not modify the deployer.
- Keep every new file below 20 KiB and approximately 600 lines.

## Blockers

- Safe macOS window capture is currently unavailable through the installed
  Computer Use integration: it cannot target the launched Java process by its
  process-specific application identity. No desktop-wide fallback was used.

## Deliberately Out of Scope

- unrelated renderer refactors;
- packaging or release changes not required by this API;
- importing earlier implementation patches;
- closing or updating issue #433 without explicit user instruction.

## Resume Command

cd /Users/flsobral/repos/totalcross-logical-ui
cmake -S TotalCrossVM -B build-logical-ui -DCMAKE_BUILD_TYPE=Release -G Ninja
