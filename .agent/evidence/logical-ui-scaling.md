<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Logical UI scaling evidence

This file is append-only. Add compact records; keep raw logs and artifacts under
`artifacts/logical-ui-scaling/`.

## Plan authoring

- Timestamp: 2026-08-01T18:07:00Z
- Revision: plan bundle only
- Status: created
- Observed upstream master:
  `d480df074e7fb6f5a32dfcc2f1f30c3949095e73`
- Limitation: execution must fetch and record the actual current master.

## Milestone 0: source identity

- Timestamp: 2026-08-01T21:00:00Z
- Base and tested commit: `d480df074e7fb6f5a32dfcc2f1f30c3949095e73`
- Branch/worktree: `feat/logical-ui-scaling` at `totalcross-logical-ui`
- Command: `git fetch origin master`; `git worktree add -b feat/logical-ui-scaling …`
- Status: passed
- Result: worktree starts directly at the fetched `origin/master`; only the
  supplied plan-support files are untracked and no previous-plan source changes
  are present.

## Milestone 0: issue #433 baseline

- Timestamp: 2026-08-01T21:05:00Z
- Base and tested commit: `d480df074e7fb6f5a32dfcc2f1f30c3949095e73`
- Milestone and slice: 0, baseline source-path reproduction
- Command: focused `rg` audit saved to
  `artifacts/logical-ui-scaling/logs/m0-density-coupling.txt`
- Renderer/platform: JavaSE source path and Skia native source path
- Status: passed (static reproduction)
- Result: both `Launcher.getFont` and Skia `drawText`/string-width calculations
  multiply font size by global `Settings.screenDensity`; a normal Image has no
  independent content scale. This proves the density-dependent text-image path
  on the untouched base. Device execution is deferred to the Android milestone.

## Milestone 1: logical API and layout contract

- Timestamp: 2026-08-01T21:35:00Z
- Base and tested commit: `d480df074e7fb6f5a32dfcc2f1f30c3949095e73`
- Command: `TotalCrossSDK/gradlew-agent test --tests
  totalcross.ui.LogicalLayoutUnitTest --tests totalcross.ui.gfx.GraphicsScaleTest`
- Renderer/platform: Java SDK test runtime
- Status: passed, 5 tests
- Result: `DP == 0`, `UnitsConverter.toPixels` is identity, inheritance resolves
  from explicit ancestor units, and Graphics accepts only finite positive scales.
- Full log: `TotalCrossSDK/agent-logs/20260801-153028-test-full.log`

## Milestone 2: Skia base transform slice

- Timestamp: 2026-08-01T21:50:00Z
- Commit: `702793faaf65b87af7819efc66760633e8267623`
- Status: implementation committed; native build pending
- Result: every Graphics-selected Skia canvas resets to, then receives, its
  finite positive `contentScale` base transform. The CMake configuration began
  fetching the required Skia artifact and did not reach generation; no native
  build result is recorded yet.

- Timestamp: 2026-08-01T22:05:00Z
- Command: `cmake -S TotalCrossVM -B build-logical-ui -DCMAKE_BUILD_TYPE=Release -G Ninja`; `ninja -C build-logical-ui`
- Renderer/platform: Skia, macOS arm64
- Status: passed
- Result: generated and linked `libtcvm.dylib`; the Skia transform and raw-pixel
  boundary changes compile successfully. Logs: `/tmp/logical-ui-m2-cmake-resume.log`,
  `/tmp/logical-ui-m2-ninja-raw.log`.

## Milestone 3: AWT backing scale and logical metrics slice

- Timestamp: 2026-08-01T22:20:00Z
- Commit: `28b67e719f76338e5cbec71804b6bdeb1398d94f`
- Commands: focused SDK tests and `ninja -C build-logical-ui`
- Status: passed
- Result: the visible AWT `LauncherFrame` obtains the backing scale from its
  `GraphicsConfiguration` after peer creation and on moves/resizes. Skia font
  size no longer reads global density; FontMetrics exposes logical double APIs.

## Milestone 4: logical image dimensions slice

- Timestamp: 2026-08-01T22:40:00Z
- Commits: `3e106909da3baab44d0c0220f08c84ab0371b008`, `0ea5f820b6cebd2cf7ca22852f0e7a19ca88f512`
- Command: `TotalCrossSDK/gradlew-agent test --tests totalcross.ui.gfx.GraphicsScaleTest`
- Status: passed, 3 tests
- Result: default images remain scale 1; `Image.createLogical(3, 7, 1.5)` has
  logical dimensions 3x7 and a 5x11 physical buffer. Graphics uses physical
  pitch for image buffers.
