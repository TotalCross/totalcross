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

- Timestamp: 2026-08-01T22:50:00Z
- Commits: `b76ef5f70522b4a70a2797d2892fd3598c1ba8d5`, `3e868044cf1d203f86aef6a439faceeb3c7b8b2f`, `c577d9ff65228dc023458b974b6e3536cfb90a03`
- Command: `ninja -C build-logical-ui`; focused GraphicsScaleTest
- Status: passed
- Result: native ABI field offsets preserve existing image fields; Java pixel
  changes recreate the Skia bitmap before dirty state is cleared; copies retain
  physical dimensions and backing metadata.

## Milestone 5: Java logical image drawing slice

- Timestamp: 2026-08-01T23:00:00Z
- Commits: `01628a56e6b55f6490c833b0af3491c78ba54a2e`, `b18dde1d7bedfdf5c519a8b90ac3078d0002ec0c`
- Command: `TotalCrossSDK/gradlew-agent test --tests totalcross.ui.gfx.GraphicsScaleTest`
- Status: passed, 4 tests
- Result: Java Graphics renders a scale-2 physical backing at its logical
  natural size; all destination pixels in the fixture receive the expected color.

## Milestone 7: deployable finite-scale validation

- Timestamp: 2026-08-01T19:13:12Z
- Commit: `5d0e66ed12e74b96ffb8909d98b25fe0dfd21749`
- Commands: `TotalCrossSDK/gradlew-agent test --tests totalcross.lang.Double4DTest
  --tests totalcross.ui.gfx.GraphicsScaleTest`; `TotalCrossSDK/gradlew-agent
  dist -x test --warning-mode=none --console=plain`
- Renderer/platform: Java SDK test runtime and deployed SDK distribution
- Status: passed (5 tests; distribution 29 seconds)
- Result: `Double4D.isFinite` accepts finite values and rejects NaN and both
  infinities. The distribution deploy resolved the Java `Double.isFinite` calls
  in the logical scale checks through `Double4D`; this also corrected the
  existing equality-based `Double4D.isNaN` behavior.

## Milestone 7: headless DANFE image assertions

- Timestamp: 2026-08-01T19:20:33Z
- Commit: `21d6958a3a250dd435655319b7cd24071d7af1f3`
- Command: `TotalCrossSDK/gradlew-agent test --tests
  totalcross.ui.image.DanfeScalingTest --tests totalcross.ui.gfx.GraphicsScaleTest`
- Renderer/platform: Java SDK test runtime
- Status: passed (5 tests total)
- Result: a deterministic synthetic DANFE image has logical dimensions 360x540;
  its default PNG is 360x540 and scale-2 PNG is 720x1080. Both backing scales
  retain exactly 31 dark barcode runs. The test exposed and the implementation
  fixed Java `fillRect` using logical dimensions as physical pixel bounds.
- Limitation: deployed text, renderer equivalence, synchronization, macOS, and
  Android proof are not covered by this headless assertion.

## Milestone 7: macOS fixture launch

- Timestamp: 2026-08-01T19:26:00Z
- Command: compiled `DanfeScalingApp` against the generated SDK, then launched
  it through `totalcross.Launcher` with `/scale 1` and `/scale 2`.
- Renderer/platform: macOS Java launcher, deterministic synthetic DANFE fixture
- Status: passed for launch at both scales
- Result: the application initialized a 480x620 logical screen at the requested
  scale and rendered the synthetic fixture without startup exceptions.
- Capture result: no artifact accepted. The process-specific Computer Use
  capture path could not target the Java application; a full-desktop fallback
  was deliberately not used.
- Scope decision: per user direction, macOS is the platform proof for this
  milestone. Android and iOS workspace execution are deferred to final
  validation.

## Milestone 7: Java image synchronization assertions

- Timestamp: 2026-08-01T19:29:24Z
- Commit: `8f8e11bf2`
- Commands: `TotalCrossSDK/gradlew-agent test --tests
  totalcross.ui.image.DanfeScalingTest --tests totalcross.ui.gfx.GraphicsScaleTest`;
  `ninja -C build-logical-ui`
- Renderer/platform: Java SDK test runtime; macOS native build tree
- Status: passed (6 SDK tests; native tree required no rebuild)
- Result: the scale-2 fixture preserves an alpha-128 pixel at an odd physical
  row across `applyChanges` and paints a logical 1x1 rectangle into the expected
  2x2 physical pixels.
- Limitation: this proves the Java-side ownership boundary only; native-to-Java
  readback still requires a native runtime fixture.

## Milestone 8: control density audit slice

- Timestamp: 2026-08-01T19:32:06Z
- Commit: `3c119d17b`
- Command: `TotalCrossSDK/gradlew-agent test --tests
  totalcross.ui.LogicalLayoutUnitTest --tests totalcross.ui.gfx.GraphicsScaleTest
  --tests totalcross.ui.image.DanfeScalingTest`
- Status: passed (6 focused tests)
- Result: removed global-density layout and rendering calculations from Button,
  TopMenu, Edit, ListContainer, Toast, and SideMenuContainer. Their values are
  now logical values; platform initialization continues to mirror the deprecated
  compatibility setting without being used by this slice.

## Milestone 8: final SDK distribution validation

- Timestamp: 2026-08-01T19:35:21Z
- Command: `TotalCrossSDK/gradlew-agent dist -x test --warning-mode=none
  --console=plain`
- Renderer/platform: SDK distribution build
- Status: passed (35 seconds)
- Result: final SDK packaging and deployment completed after the control density
  audit. Agent summary: `TotalCrossSDK/agent-logs/20260801-163521-dist-agent.log`.

## Milestone 8: final static audit

- Timestamp: 2026-08-01T19:36:00Z
- Commands: scoped `rg` audits for `Settings.screenDensity` and DP comparisons;
  size audit against the recorded base commit.
- Status: passed
- Result: only the two deprecated compatibility assignments in `Launcher` retain
  `Settings.screenDensity`; no control code reads it. No old DP-marker comparison
  was found. All added files are below 20 KiB and approximately 600 lines.

## Milestone 8: repeated safe-capture attempt

- Timestamp: 2026-08-01T19:37:00Z
- Command: launched the deterministic fixture at simulated macOS scale 2 and
  requested a process-targeted Computer Use window state.
- Status: external capture blocker repeated
- Result: the fixture launched successfully, but the installed integration again
  rejected the Java process application identity as an unsupported target. No
  fallback capture containing the desktop was attempted or retained.

## Corrective checkpoint R0: guarded Skia direct writes

- Timestamp: 2026-08-01T20:00:00Z
- Commits: `2a27fa5f5`, `7d300c9da`
- Commands: `ninja -C build-logical-ui`; configured and built
  `build-logical-ui-no-write` with `-DUSE_WRITE_PIXELS=0`; configured and built
  `build-logical-ui-write-opaque` with `-DUSE_WRITE_PIXELS=1
  -DUSE_COMPUTE_OPAQUE=1`; compiled and ran `skia_surface_test.cpp` against all
  three freshly built dylibs.
- Renderer/platform: native macOS compile and native Skia helper runtime
- Status: passed
- Result: direct writes are eligible only for an opaque, complete, same-size,
  alpha-255 copy at an identity matrix with no saved clip and integral, in-bounds
  physical destination coordinates. The fixture verifies identity copy, scaled
  fallback, clipped fallback, and physical raw pixels across enabled and disabled
  variants.
