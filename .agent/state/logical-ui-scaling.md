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
- Last logical commit: `702793faaf65b87af7819efc66760633e8267623`.

## Active Milestone

Milestone 2: implement logical drawing in Skia.

## Active Slice

Complete focused native validation for the Skia base transform, then audit raw
pixel calls so they remain physical before closing the milestone.

## Next Concrete Action

Resume the CMake configuration after the depot Skia artifact is available, then
build the smallest native target.

## Files to Read Now

- `.agent/logical-ui-scaling-execplan.md`, Milestone 2 only.
- `.agent/design/logical-ui-scaling-api.md`, Graphics scale sections.

Do not read all design guides yet.

## Focused Validation Completed

- `git fetch origin master` and isolated worktree creation: passed.
- Static baseline audit: passed. `Launcher.getFont`, Skia text drawing, and
  Skia metric width paths multiply font size by `Settings.screenDensity`.
- `./gradlew-agent test --tests totalcross.ui.LogicalLayoutUnitTest --tests
  totalcross.ui.gfx.GraphicsScaleTest`: passed (5 tests).

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
