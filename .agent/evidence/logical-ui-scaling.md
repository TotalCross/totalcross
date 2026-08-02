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

## Milestone 1R: parent layout-unit placement

- Timestamp: 2026-08-01T20:36:01Z
- Commit: `a629ec0d8`
- Command: `TotalCrossSDK/gradlew-agent test --tests
  totalcross.ui.LogicalLayoutUnitTest --warning-mode=none --console=plain`
- Renderer/platform: Java SDK test runtime on macOS
- Status: passed (7 tests)
- Result: `Control.setRect` now resolves child rectangles in a PIXEL parent's
  physical coordinates, then converts rounded edges once into public logical
  geometry. The behavioral tests cover parent ownership, child override for
  descendants, `AFTER` offsets, and shared edges at 1.5, 2, and 3.
- Limitation: deployed native macOS migration-fixture validation is intentionally
  deferred to the native runtime milestone; this Java result is not native proof.

## Milestone 2R: native SDL content-scale initialization

- Timestamp: 2026-08-01T20:48:00Z
- Commit: `8bba4c732`
- Command: `ninja -C build-logical-ui tcvm`
- Renderer/platform: native macOS compile
- Status: passed
- Result: the SDL macOS window requests high-DPI backing, obtains its physical
  drawable dimensions, stores the resulting scale on `ScreenSurface`, reports
  logical dimensions through `Settings`, and assigns that scale to native screen
  `Graphics` instances. Image Graphics retain their image-owned scale.
- Limitation: a freshly deployed native macOS application must still report and
  exercise this value on a real Retina screen before it is runtime proof.

## Milestone 2R: scaled Skia primitive fixture

- Timestamp: 2026-08-01T20:52:00Z
- Commit: `765a1352d`
- Command: compiled `skia_surface_test.cpp` against
  `build-logical-ui/libtcvm.dylib` and ran `/tmp/logical-ui-m2-skia-test`
- Renderer/platform: native macOS Skia helper runtime
- Status: passed
- Result: the fixture now verifies that the base scale applies to primitive
  destinations and clips while raw pixel access remains physical.

## Milestone 2R: deployable fixture task

- Timestamp: 2026-08-01T20:54:00Z
- Commit: `876e0735b`
- Command: `TotalCrossSDK/gradlew-agent compileLogicalUiScalingSmoke
  --warning-mode=none --console=plain`
- Renderer/platform: Java SDK fixture compilation on macOS
- Status: passed
- Result: the build now has focused compile and macOS deploy tasks for the
  logical UI fixture, following the repository's native-runtime smoke pattern.
- Limitation: deployment and direct native execution require the matching SDK
  distribution and freshly built dylib; they remain the next slice.

## Milestone 2R: deployed native macOS startup

- Timestamp: 2026-08-01T20:56:00Z
- Commit: `93ee35201`
- Commands: `TotalCrossSDK/gradlew-agent dist -x test --no-daemon`; focused
  `deployLogicalUiScalingSmokeMacOS` with the freshly built dylib and a local
  macOS launcher; direct execution of `DanfeScalingApp`.
- Renderer/platform: deployed native macOS SDL/Skia application
- Status: startup passed; scale assertion not yet implemented
- Result: the generated executable selected SDL Cocoa and software rendering;
  the deployed `libtcvm.dylib` SHA-256 exactly matched
  `build-logical-ui/libtcvm.dylib`.
- Limitation: this confirms the native launch lane and runtime identity but does
  not yet emit the required logical/physical scale assertion.

## Milestone 2R: deployed native Retina scale assertion

- Timestamp: 2026-08-01T20:58:00Z
- Commit: `bce0f6865`
- Commands: focused `deployLogicalUiScalingSmokeMacOS` with
  `-PtcvmDylib=/Users/flsobral/repos/totalcross-logical-ui/build-logical-ui/libtcvm.dylib`,
  then direct `DanfeScalingApp /logical-ui-assert` execution.
- Renderer/platform: deployed native macOS SDL/Skia application
- Status: passed
- Result: the native process exited 0 and reported `logical=1728x976`,
  `physical=3456x1952`, and `contentScale=2.0`; source and deployed dylib
  SHA-256 values matched exactly.

## Milestone 2R: Skia logical dirty bounds

- Timestamp: 2026-08-01T21:02:00Z
- Commit: `d58ebeef3`
- Command: `ninja -C build-logical-ui tcvm`
- Renderer/platform: native macOS Skia compile
- Status: passed
- Result: logical drawing damage now converts rounded rectangle edges through
  the destination content scale before it is accumulated for the physical
  framebuffer. Raw `setPixel` damage remains physical.

## Milestone 2R: offscreen scale matrix

- Timestamp: 2026-08-01T21:05:00Z
- Commit: `510f30540`
- Command: compiled and ran `skia_surface_test.cpp` against the current
  `build-logical-ui/libtcvm.dylib`
- Renderer/platform: native macOS Skia helper runtime
- Status: passed
- Result: the fixture covers logical primitive destination mapping at scales 1,
  1.5, 2, and 3, as well as scaled clipping, image fallback, and raw pixels.

## Milestone 3R: fractional native vertical metrics

- Timestamp: 2026-08-01T21:14:00Z
- Commits: `061fb98f5`, `4d90cc929`
- Commands: `ninja -C build-logical-ui tcvm`; rebuilt SDK distribution,
  deployed `DanfeScalingApp`, and directly ran `/logical-ui-assert`.
- Renderer/platform: deployed native macOS SDL/Skia application
- Status: passed
- Result: the native fixture reports fractional Skia metrics
  `ascentD=12.98828125`, `descentD=3.41796875`, and `heightD=16.40625`.
  Integer compatibility fields use conservative upward rounding.

## Milestone 3R: fractional native text advances

- Timestamp: 2026-08-01T21:19:00Z
- Commit: `8e32f006a`
- Commands: rebuilt `build-logical-ui/libtcvm.dylib`; focused fixture deploy and
  direct native `/logical-ui-assert` execution.
- Renderer/platform: deployed native macOS SDL/Skia application
- Status: passed
- Result: `FontMetrics.stringWidthD("DANFE 25,00")` resolved through the VM
  native-method table and returned the fractional advance `81.6484375`.

## Milestone 3R: native font-scale drawing path

- Timestamp: 2026-08-01T21:23:00Z
- Commit: `f6655680a`
- Command: `ninja -C build-logical-ui tcvm`
- Renderer/platform: native macOS Skia compile
- Status: passed
- Result: text uses `Font.size * Graphics.fontScale` for the Skia font and
  actual baseline/damage metrics; canvas contentScale is not reapplied.
- Limitation: deployed preferred-size and fontScale invalidation assertions are
  the next slice.

## Reconciliation R1: PIXEL edges and destination text metrics

- Timestamp: 2026-08-01T22:00:00Z
- Commits: `4f7f66e82`, `3363461fc`, `472430042`, `2011c07aa`, `9bdf26a9c`
- Java validation: `LogicalLayoutUnitTest` passed nonzero-inset PIXEL client
  edges at scales 1.5, 2, and 3; `LogicalTextScaleTest` passed destination
  effective-size control and wrapping assertions.
- Native macOS compile: `ninja -C build-logical-ui tcvm` passed after adding
  effective-size `SkFont::measureText`, line-height, and descent bindings.
- Deployed native macOS runtime: direct `DanfeScalingApp /logical-ui-assert`
  passed with the current dylib. It reported fractional metrics, scale-stable
  Label/Edit/MultiEdit results, and root PIXEL conversion
  `pixelChild=10,5,50,20` from physical input `20,10,100,40` at Retina scale 2.
- Logs: `pixel-client-origin-test.log`, `effective-font-measurement-native-retry.log`,
  `effective-wrap-native.log`, `effective-multiedit-wrap-native.log`, and
  `root-pixel-fixture-native-final.log` under
  `artifacts/logical-ui-scaling/logs/`.

## Milestone 3R: effective text-control geometry

- Timestamp: 2026-08-01T23:00:00Z
- Commands: `TotalCrossSDK/gradlew-agent test --tests
  totalcross.ui.LogicalTextScaleTest`; `ninja -C build-logical-ui tcvm`;
  `TotalCrossSDK/gradlew-agent dist -x test`; focused
  `deployLogicalUiScalingSmokeMacOS` with the current dylib; direct
  `DanfeScalingApp /logical-ui-assert` execution.
- Renderer/platform: Java tests and deployed native macOS SDL/Skia application.
- Status: passed.
- Result: Button's destination-dependent ellipsis, preferred bounds, placement,
  alignment, and underline use effective-size metrics. MultiEdit selection and
  dotted-baseline geometry use effective line-height and ascent. Native effective
  ascent comes from `SkFont::getMetrics` at `Font.size * fontScale`; the fixture
  passed with Retina `contentScale=2.0` and all existing text and PIXEL
  assertions.
- Logs: `effective-text-controls-test.log`,
  `effective-text-controls-native-build.log`, `effective-text-controls-dist.log`,
  `effective-text-controls-deploy.log`, and
  `effective-text-controls-native.log` under
  `artifacts/logical-ui-scaling/logs/`.

## Milestone 3R: remaining multiline consumers

- Timestamp: 2026-08-01T23:10:00Z
- Commands: focused `LogicalTextScaleTest`; rebuilt SDK distribution; focused
  `deployLogicalUiScalingSmokeMacOS` with the current dylib; direct
  `DanfeScalingApp /logical-ui-assert` execution.
- Renderer/platform: Java tests and deployed native macOS SDL/Skia application.
- Status: passed.
- Result: live Window, dialog, popup, list, tooltip, Check, Radio, and Grid
  multiline paths route candidate-line measurement through the destination
  effective-size adapter. The fixture retained its Retina scale and existing
  text and PIXEL assertions.
- Logs: `effective-multiline-consumers-test.log`,
  `effective-multiline-consumers-dist.log`,
  `effective-multiline-consumers-deploy.log`, and
  `effective-multiline-consumers-native.log` under
  `artifacts/logical-ui-scaling/logs/`.

## Milestone 3R: Check and Radio scale caches

- Timestamp: 2026-08-01T23:20:00Z
- Command: focused `TotalCrossSDK/gradlew-agent test --tests
  totalcross.ui.LogicalTextScaleTest`.
- Renderer/platform: Java test lane.
- Status: passed.
- Result: Check cached line widths stay invariant across contentScale and refresh
  at fontScale 1.5; Radio preferred geometry measures at the destination
  effective size.
- Native macOS validation: rebuilt SDK distribution, deployed the fixture with
  the matching current dylib, and directly ran `DanfeScalingApp /logical-ui-assert`.
  It passed with Retina `contentScale=2.0`; logs: `m3-choice-dist.log`,
  `m3-choice-deploy.log`, and `m3-choice-native.log`.

## Milestone 3R: DANFE text-control matrix

- Timestamp: 2026-08-01T23:30:00Z
- Commands: rebuilt SDK distribution, focused macOS fixture deployment with the
  matching dylib, and direct `DanfeScalingApp /logical-ui-assert` execution.
- Renderer/platform: deployed native macOS SDL/Skia application.
- Status: passed.
- Result: the fixture verifies content-scale invariance and font-scale growth
  for Label, Button, Edit, MultiEdit, Check, and Radio. It reports
  `buttonWidths=86,86,127`, `checkWidths=49,49,53`, and
  `radioWidths=103,103,152` without any advanced-typography claim.
- Logs: `m3-danfe-dist.log`, `m3-danfe-deploy.log`, and
  `m3-danfe-native.log` under `artifacts/logical-ui-scaling/logs/`.

## Milestone 3R: Edit effective caption geometry

- Timestamp: 2026-08-01T23:40:00Z
- Commands: rebuilt SDK distribution, focused macOS fixture deployment with the
  matching dylib, and direct `DanfeScalingApp /logical-ui-assert` execution.
- Renderer/platform: deployed native macOS SDL/Skia application.
- Status: passed.
- Result: Edit caption-icon spacing, material preferred height, cursor descent,
  and horizontal cursor scrolling use destination effective metrics. The DANFE
  fixture retained all text-control and PIXEL assertions at Retina scale 2.
- Logs: `m3-edit-dist.log`, `m3-edit-deploy.log`, and `m3-edit-native.log`
  under `artifacts/logical-ui-scaling/logs/`.

## Milestone 3R: final SkFont-only text gate

- Timestamp: 2026-08-01T23:50:00Z
- Commands: rebuilt SDK distribution, focused macOS fixture deployment with the
  matching dylib, and direct `DanfeScalingApp /logical-ui-assert` execution.
- Renderer/platform: deployed native macOS SDL/Skia application.
- Status: passed.
- Result: final primary-control geometry audit passed. The deployed DANFE
  fixture confirms the SkFont-only effective-size text contract and TotalCross
  wrapping without an advanced-typography claim.
- Logs: `m3-final-dist.log`, `m3-final-deploy.log`, and `m3-final-native.log`
  under `artifacts/logical-ui-scaling/logs/`.

## Milestone 4R: logical image graphics surface

- Timestamp: 2026-08-02T00:00:00Z
- Command: focused `GraphicsScaleTest` and `DanfeScalingTest` through
  `TotalCrossSDK/gradlew-agent test`.
- Renderer/platform: Java test lane.
- Status: passed.
- Result: `Image.getGraphics()` now retains logical surface dimensions while the
  physical backing remains selected by contentScale.
- Log: `m4-image-graphics-test.log` under
  `artifacts/logical-ui-scaling/logs/`.

## Milestone 4R: native logical-image ABI

- Timestamp: 2026-08-02T00:10:00Z
- Commands: rebuilt `tcvm`; redeployed `TCUI.tcz`, SDK distribution, and the
  focused macOS fixture with the matching dylib; directly ran
  `DanfeScalingApp /logical-ui-assert`.
- Renderer/platform: deployed native macOS SDL/Skia application.
- Status: passed.
- Result: native `Image4D.createLogical(3, 2, 2)` reports logical `3x2` and
  physical `6x4`. The VM instance-field map preserves legacy image fields and
  places logical dimensions after `lastAccess` and `textureId`.
- Logs: `m4-image4d-abi-build.log`, `m4-image4d-tcui.log`,
  `m4-image4d-dist.log`, `m4-image4d-deploy.log`, and
  `m4-image4d-native.log` under `artifacts/logical-ui-scaling/logs/`.

## Milestone 4R: native image source sampling and readback

- Timestamp: 2026-08-02T00:20:00Z
- Commands: focused `GraphicsScaleTest` and `DanfeScalingTest`; rebuilt `tcvm`;
  redeployed and directly ran `DanfeScalingApp /logical-ui-assert` on macOS.
- Renderer/platform: Java test lane and deployed native macOS SDL/Skia app.
- Status: passed.
- Result: source rectangles convert logical image coordinates to physical backing
  coordinates. The native row readback now returns physical Skia pixels
  correctly, preserving a scale-2 four-color source, alpha mask, and natural
  logical destination. Multiframe visible width remains physical internally and
  logical publicly.
- Deployed dylib SHA-256: `39eea86827ed02162de5355f0a3ce88e245a19577bf02ca862d7e93dc09f0fe7`.
- Logs: `m4-image-ownership-test.log`, `m4-image-ownership-build.log`,
  `m4-image-ownership-deploy.log`, `m4-image-ownership-native.log`, and
  `m4-image-ownership-hashes.log` under `artifacts/logical-ui-scaling/logs/`.

## Milestone 4R: corrective native image readback

- Timestamp: 2026-08-02T00:30:00Z
- Commands: focused Java image/graphics tests; macOS `tcvm` build; TCUI and SDK
  deployment; smoke deployment; SHA-256 comparison; direct native fixture.
- Renderer/platform: Java test lane and deployed native macOS SDL/Skia app.
- Status: passed.
- Result: the prior defect was incorrect byte access to a temporary RGBA_8888
  bitmap. One `readPixels` call now fills a row bitmap and `getColor(x, 0)`
  supplies RGBA bytes in order. The macOS fixture proves red/green and
  blue/white rows, partial source origin, alpha-mask 128 compositing tolerance,
  and multiframe visible dimensions. Direct Image ABI field offsets and the
  fast/strict source-rect policy are restored.
- Deployed dylib SHA-256: `b4e7c140717fb4bf6e0f1eada365f5c1aea97067907ad280fb99430bedb58a5a`.
- Logs: `m4-readback-corrective-test.log`, `m4-readback-corrective-build.log`,
  `m4-readback-corrective-tcui.log`, `m4-readback-corrective-dist.log`,
  `m4-readback-corrective-deploy.log`, `m4-readback-corrective-hashes.log`,
  and `m4-readback-corrective-native.log` under
  `artifacts/logical-ui-scaling/logs/`.

## Milestone 4R: image transform and PNG dimensions

- Timestamp: 2026-08-02T00:40:00Z
- Commands: focused Java image/graphics tests; TCUI/SDK deployment; deployed
  macOS fixture and direct assertion execution.
- Status: passed.
- Result: a scale-2 `3x2` image encodes a physical `6x4` PNG. Existing scaling
  transforms retain their fixed-pixel result contract: `6x4`, scale `1`.
- Deployed dylib SHA-256: `b4e7c140717fb4bf6e0f1eada365f5c1aea97067907ad280fb99430bedb58a5a`.
- Logs: `m4-image-transform-test.log`, `m4-image-transform-tcui.log`,
  `m4-image-transform-dist.log`, `m4-image-transform-deploy.log`,
  `m4-image-transform-hashes.log`, and `m4-image-transform-native.log`.

## Milestone 4R: ordinary image loading

- Timestamp: 2026-08-02T00:50:00Z
- Commands: focused `GraphicsScaleTest` and `DanfeScalingTest`; TCUI and SDK
  deployment; deployed macOS fixture and direct assertion execution.
- Renderer/platform: Java test lane and deployed native macOS SDL/Skia app.
- Status: passed.
- Result: a PNG encoded from a scale-2 `3x2` image retains its `6x4` physical
  dimensions when decoded. Decoding creates the established fixed-pixel,
  scale-1 image in both Java and the macOS runtime fixture.
- Deployed dylib SHA-256: `b4e7c140717fb4bf6e0f1eada365f5c1aea97067907ad280fb99430bedb58a5a`.
- Logs: `m4-image-loader-java.log`, `m4-image-loader-tcui.log`,
  `m4-image-loader-dist.log`, `m4-image-loader-deploy.log`,
  `m4-image-loader-hashes.log`, and `m4-image-loader-native.log` under
  `artifacts/logical-ui-scaling/logs/`.

## Milestone 4R: texture refresh after backing writes

- Timestamp: 2026-08-02T01:00:00Z
- Commands: macOS `tcvm` build; focused `GraphicsScaleTest` and
  `DanfeScalingTest`; TCUI and SDK deployment; smoke deployment; SHA-256
  comparison; direct native fixture.
- Renderer/platform: Java test lane and deployed native macOS SDL/Skia app.
- Status: passed.
- Result: when Java-side image drawing marks a cached Skia texture changed, the
  native painter deletes and recreates that texture before drawing. The native
  fixture proves the updated black/green pixels after a prior texture upload.
- Deployed dylib SHA-256: `ef6b9ec5f10ed1ff79f2a5dc59efe3cc45685422bc7dac9530e4abe831624e7e`.
- Logs: `m4-image-texture-build.log`, `m4-image-texture-java.log`,
  `m4-image-texture-tcui.log`, `m4-image-texture-dist.log`,
  `m4-image-texture-deploy.log`, `m4-image-texture-hashes.log`, and
  `m4-image-texture-native.log` under `artifacts/logical-ui-scaling/logs/`.

## Milestone 4R: texture-refresh correction

- Timestamp: 2026-08-02T01:10:00Z
- Status: superseded before milestone completion.
- Result: the shared `changed` flag also follows native canvas writes, while a
  Skia image surface owns a copied bitmap. Recreating a texture solely from
  that flag could discard native-canvas pixels, so the provisional invalidation
  is removed pending an explicit ownership boundary and executable transition
  proof. The preceding loader/readback evidence remains valid.

## Milestone 4R: image ownership and cache audit

- Timestamp: 2026-08-02T01:20:00Z
- Status: passed.
- Result: no further scale-specific cache or copy defect was found. Frame-copy
  and transform results use fresh physical backing under their fixed-pixel
  scale-1 contract. Legacy shared texture lifetime has no logical-dimension
  conversion. Java backing tests and the deployed macOS native-canvas/readback
  fixture jointly cover the documented ownership boundaries.

## Milestone 5R: Java primitive backing edges

- Timestamp: 2026-08-02T01:30:00Z
- Command: focused `GraphicsScaleTest` through `TotalCrossSDK/gradlew-agent`.
- Renderer/platform: Java renderer test lane.
- Status: passed.
- Result: Java image-backed points and line/rectangle primitives preserve
  logical clip/translation then convert each raster edge once. The scale-1.5
  regression proves expected physical coverage without fractional edge gaps.
- Log: `m5-java-primitives-test.log` under
  `artifacts/logical-ui-scaling/logs/`.

## Milestone 5R: Java image blits into scaled backing

- Timestamp: 2026-08-02T01:40:00Z
- Command: focused `GraphicsScaleTest` through `TotalCrossSDK/gradlew-agent`.
- Renderer/platform: Java renderer test lane.
- Status: passed.
- Result: Java `drawImage` and `copyImageRect` clip in logical coordinates then
  rasterize their source at the destination image backing scale. The regression
  covers natural drawing and a partial source rectangle into scale-2 backing.
- Log: `m5-java-blit-test.log` under `artifacts/logical-ui-scaling/logs/`.

## Milestone 5R: Java effective text font selection

- Timestamp: 2026-08-02T01:50:00Z
- Command: focused `GraphicsScaleTest` through `TotalCrossSDK/gradlew-agent`.
- Renderer/platform: Java renderer test lane.
- Status: passed.
- Result: Java text uses a local integer-rounded effective font for glyph
  selection, justification, and fallback. This preserves public `FontMetrics`
  compatibility; raster-size proof remains pending an initialized Java font
  fixture because raw image graphics intentionally has no default font.
- Log: `m5-java-text-test.log` under `artifacts/logical-ui-scaling/logs/`.

## Milestone 5R: Java scaled text backing

- Timestamp: 2026-08-02T02:00:00Z
- Command: focused `GraphicsScaleTest` through `TotalCrossSDK/gradlew-agent`.
- Renderer/platform: Java renderer test lane.
- Status: passed.
- Result: scaled image text keeps logical clipping and translation while its
  temporary logical raster is composed through the destination-scaled blit path.
  Visual raster-size proof remains pending an initialized Java font fixture.
- Log: `m5-java-text-backing-test.log` under
  `artifacts/logical-ui-scaling/logs/`.

## Milestone 5R: Java Launcher DANFE semantic lane

- Timestamp: 2026-08-02T02:10:00Z
- Commands: focused `GraphicsScaleTest`; compiled the smoke fixture; launched
  `totalcross.Launcher` with `/logical-ui-assert`.
- Renderer/platform: Java Launcher renderer lane.
- Status: passed.
- Result: the initialized Java fixture passes logical image, alpha composition,
  text metric/control, and destination-relative PIXEL assertions at
  `contentScale=1`. The fixture reports its physical backing separately from
  native macOS evidence.
- Logs: `m5-java-final-test.log`, `m5-java-final-compile.log`, and
  `m5-java-final-launcher.log` under `artifacts/logical-ui-scaling/logs/`.

## Milestone 5R: Java renderer completion audit

- Timestamp: 2026-08-02T02:20:00Z
- Status: passed.
- Result: primitive, clip/translation, image/source-rectangle, effective text,
  scaled text backing, and Java Launcher DANFE lanes pass. Java dirty state is a
  repaint boolean and has no coordinate conversion. The Java renderer retains
  integer font-raster metrics as its documented rounding boundary.

## Milestone 4R: corrective transform identity guard

- Timestamp: 2026-08-01T22:10:00Z
- Commands: focused `GraphicsScaleTest` and `DanfeScalingTest`; macOS `tcvm`
  build; SDK/smoke deployment; SHA-256 comparison; direct fixture execution.
- Renderer/platform: Java test lane and deployed native macOS SDL/Skia app.
- Status: passed.
- Result: the native `Image4D` identity shortcut incorrectly treated physical
  `6x4` arguments as an identity for a logical `3x2` scale-two image. The
  shortcut now applies only to scale-one backing, so transforms retain the
  established fixed-pixel scale-one `6x4` result. The fixture also passed its
  four-color row readback, alpha-128 partial source rectangle, and frame-width
  assertions.
- Deployed dylib SHA-256: `b4e7c140717fb4bf6e0f1eada365f5c1aea97067907ad280fb99430bedb58a5a`.
- Logs: `m4r-transform-java-tests.log`, `m4r-transform-native-build.log`,
  `m4r-transform-sdk-dist.log`, `m4r-transform-deploy.log`, and
  `m4r-transform-native-fixture.log` under `artifacts/logical-ui-scaling/logs/`.

## Milestone 6R: non-Skia macOS audit

- Timestamp: 2026-08-01T22:15:00Z
- Commands: configured/built `build-logical-ui-nonskia` with `-DUSE_SKIA=OFF`;
  redeployed the smoke app; compared SHA-256; directly ran the fixture.
- Renderer/platform: deployed native macOS SDL non-Skia app.
- Status: unsupported configuration recorded.
- Result: the build and hash check pass, but a scale-two image-backed primitive
  paints only the first physical pixel (`red, black` instead of the expected
  replicated logical backing). Its generic pixel renderer has no destination
  content-scale mapping, so it is not semantically equivalent to Skia.
- Deployed dylib SHA-256: `a3d6ecc41d612fbe5ba942325aa7b89cbf4bfc52c4f7a24739cec5916fab4898`.
- Logs: `m6-nonskia-build.log`, `m6-nonskia-deploy-after-transform.log`, and
  `m6-nonskia-native-after-transform.log` under
  `artifacts/logical-ui-scaling/logs/`.
