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

Run the remaining full DANFE assertions in a deployed application, then record
the platform evidence available locally. The headless fixture now proves logical
and physical image dimensions, PNG dimensions, and scaled barcode structure.

## Next Concrete Action

Build the deployed DANFE fixture required for text, synchronization, and safe
window-capture assertions; do not treat the headless barcode check as platform
proof.

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
- `TotalCrossSDK/gradlew-agent test --tests totalcross.ui.image.DanfeScalingTest
  --tests totalcross.ui.gfx.GraphicsScaleTest`: passed. The fixture checks 360x540
  and 720x1080 PNG dimensions and 31 physical barcode runs at scale 1 and 2.

## Deferred Validation

Deployed text containment, Java/Skia and non-Skia equivalence, two-way pixel
synchronization, safe macOS window capture, and Android export remain required.

## Active Decisions

- Start from current `origin/master`.
- Ignore source changes from earlier plans in this session.
- Use `double` for fractional API and implementation calculations.
- Do not modify the deployer.
- Keep every new file below 20 KiB and approximately 600 lines.

## Blockers

- Android validation is unavailable locally: `adb devices -l` reports no device
  or emulator and `ANDROID_HOME` is unset, so the Android Gradle module cannot
  locate an SDK. This is external platform evidence only; source/test work can
  still continue.

## Deliberately Out of Scope

- unrelated renderer refactors;
- packaging or release changes not required by this API;
- importing earlier implementation patches;
- closing or updating issue #433 without explicit user instruction.

## Resume Command

cd /Users/flsobral/repos/totalcross-logical-ui
cmake -S TotalCrossVM -B build-logical-ui -DCMAKE_BUILD_TYPE=Release -G Ninja
