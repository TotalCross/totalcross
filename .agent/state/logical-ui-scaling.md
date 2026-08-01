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
- Last logical commit: `85e19a7090de493db5bf78582b14986ea83f7500`.

## Active Milestone

Milestone 4: implement scaled-image semantics.

## Active Slice

Read the image design and implement explicit logical versus physical image
dimensions without changing ordinary scale-1 image behavior.

## Next Concrete Action

Read the Milestone 4 section and the image design, then inspect Image and its
native synchronization path.

## Files to Read Now

- `.agent/logical-ui-scaling-execplan.md`, Milestone 4 only.
- `.agent/design/logical-ui-scaling-images.md`.

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
