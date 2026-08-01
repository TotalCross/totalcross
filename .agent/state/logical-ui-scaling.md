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
- Last logical commit: `5d0e66ed12e74b96ffb8909d98b25fe0dfd21749`.

## Active Milestone

Milestone 7: run end-to-end DANFE and platform validation.

## Active Slice

Run required SDK/native/platform validations and determine which external
platform evidence is available locally. The SDK deployer compatibility slice
for `Double.isFinite` is complete.

## Next Concrete Action

Read the Milestone 7 plan section and its explicitly required validation guides.

## Files to Read Now

- `.agent/logical-ui-scaling-execplan.md`, Milestone 7 only.
- `.agent/guides/logical-ui-scaling-validation.md`, final acceptance section.
- `.agent/guides/logical-ui-scaling-danfe.md`.

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

## Deferred Validation

All implementation, renderer, platform, and DANFE validation remains deferred
until its corresponding milestone.

## Active Decisions

- Start from current `origin/master`.
- Ignore source changes from earlier plans in this session.
- Use `double` for fractional API and implementation calculations.
- Do not modify the deployer.
- Keep every new file below 20 KiB and approximately 600 lines.

## Blockers

None recorded.

## Deliberately Out of Scope

- unrelated renderer refactors;
- packaging or release changes not required by this API;
- importing earlier implementation patches;
- closing or updating issue #433 without explicit user instruction.

## Resume Command

cd /Users/flsobral/repos/totalcross-logical-ui
cmake -S TotalCrossVM -B build-logical-ui -DCMAKE_BUILD_TYPE=Release -G Ninja
