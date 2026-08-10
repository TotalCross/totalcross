<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Separate Java Launcher logical geometry from physical backing pixels

This ExecPlan is a living document. The sections `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` must be kept up to date as work proceeds. Maintain it in accordance with `.agent/PLANS.md` from the repository root.

## Purpose / Big Picture

The Java desktop simulator currently treats `/density` partly as a device density and partly as a request to enlarge every public screen coordinate. This makes a simulated 320 by 480 device report 640 by 960 at density 2, scales safe-area values, and lets the host monitor's AWT/HiDPI transform replace the requested simulated density. After this change, `/scr 320x480 /density 2` must expose a 320 by 480 logical screen and logical input, layout, safe-area, and font values while rendering into a 640 by 960 physical framebuffer. Running the same application at density 1 and density 2 must preserve its logical geometry and visual proportions; only raster resolution and the preview frame's physical pixel dimensions change.

## Progress

- [x] (2026-08-10 18:46Z) Read the repository plan rules, header-validation skill, prior logical-scaling design, launcher implementation, and focused tests.
- [x] (2026-08-10 18:46Z) Run the existing focused launcher tests as a green baseline.
- [x] (2026-08-10 18:50Z) Add regression tests that define logical parser values, physical framebuffer dimensions, scaled raster coverage, logical input, density-independent AWT presentation scaling, and host-scale isolation.
- [x] (2026-08-10 18:57Z) Implement the logical/physical boundary in the parser, launcher surface configuration, Java Graphics backing, frame renderer, input conversion, and AWT presentation backend.
- [x] (2026-08-10 19:03Z) Run focused tests, relevant broader tests, header validation, and whitespace validation. The complete SDK test task has one existing order-dependent visual failure that passes in isolation.
- [x] (2026-08-10 19:03Z) Record final evidence and outcomes; superseded by the reopened audit below.
- [x] (2026-08-10 19:22Z) Reopen the renderer boundary audit after identifying that text, region copies, raw RGB access, dithering, and screenshots still assume a logical-pixel backing in at least one path.
- [x] (2026-08-10 19:45Z) Convert every remaining Control/Window raster path and add a density 1/2/3 fixture that exercises the complete boundary.
- [x] (2026-08-10 19:48Z) Re-run focused and broader validation, perform a static `Settings.screenDensity` architecture audit, and record final evidence only after every acceptance item is proven.

## Surprises & Discoveries

- Observation: `CommandLineParser.parse` currently multiplies parsed width, height, and both safe-area inset sets by density after all arguments are parsed.
  Evidence: the final statements in `TotalCrossSDK/src/main/java/tc/simulator/CommandLineParser.java` mutate those logical values.

- Observation: `RuntimeState.updateContentScale` reads the host frame's `GraphicsConfiguration` transform and writes it into both `Settings.screenDensity` and the main-window `Graphics` content scale.
  Evidence: the method is called when the AWT window starts and on every AWT component resize.

- Observation: the Java renderer already knows how to rasterize scaled image-backed logical surfaces, but its shared control framebuffer still allocates and indexes using `Settings.screenWidth` and `Settings.screenHeight` directly.
  Evidence: `Graphics.create` and `Graphics.refresh` allocate the shared `mainWindowPixels` at logical dimensions, while scaled backing conversions are guarded by `surface instanceof Image`.

- Observation: TotalCross initializes parts of the Java runtime lazily through `Convert`, and tests that create controls before setting screen state can cause `fillSettings` to publish unresolved `-1` dimensions.
  Evidence: the initial control-backing test exposed this initialization order; production surface configuration now occurs only after non-negative parsed dimensions exist, and the test initializes the control before installing its explicit screen fixture.

- Observation: the complete `test` task has an existing shared-static-state ordering failure between `LauncherNestedEventPumpTest` and `LegacySafeAreaVisualTest`.
  Evidence: the complete task log `20260810-160214-test-full.log` reports that the visual test received the nested-pump test's `MainWindow`; `LegacySafeAreaVisualTest` passes by itself in `20260810-160250-test-full.log`.

- Observation: the initial scaled-Control implementation did not constitute an end-to-end raster boundary. Text was painted into a scale-1 temporary image and enlarged, `drawSurface` only handled a scaled destination, `getRGB`, `setRGB`, and `dither` indexed logical coordinates with a physical pitch, and screenshots created scale-1 images.
  Evidence: direct framebuffer/pitch audit of `Graphics.java`, `Control.takeScreenShot`, and `MainWindow.getScreenShot` after the first focused test pass.

- Observation: public `getRGB` and `setRGB` must remain logical-region APIs even though their backing is physical.
  Evidence: `Window.drawHighlight` and `NinePatch` allocate exactly one entry per requested logical pixel and use the methods' returned logical count as the next buffer offset. The implementation now samples one physical pixel per logical cell on read and expands one logical value over the cell's physical coverage on write.

- Observation: the software font registry can construct a destination-only font at `logical font size * contentScale` without changing the application-visible `Font` or its metrics.
  Evidence: the density matrix observes stable logical font and preferred-control sizes while the rendered glyph bounds and painted-pixel count increase at densities 2 and 3 and contain sub-logical-pixel detail.

## Decision Log

- Decision: Keep `/scr`, safe-area values, `Settings.screenWidth`, `Settings.screenHeight`, layout, fonts, and dispatched input in logical units.
  Rationale: This is the native runtime contract and makes density a raster property rather than a UI zoom.
  Date/Author: 2026-08-10 / Codex

- Decision: Interpret `/scale` and automatic fit as host-presentation scaling of the logical viewport, independent of `/density`.
  Rationale: AWT window size is expressed in host user-space coordinates. Dividing presentation scale by simulated density couples two independent transforms and makes density change apparent UI size.
  Date/Author: 2026-08-10 / Codex

- Decision: Derive each physical framebuffer edge with `ceil(logicalSize * contentScale)` and map logical edges with rounding.
  Rationale: This matches the repository's logical-image contract and ensures a fractional-density backing covers the full logical extent.
  Date/Author: 2026-08-10 / Codex

- Decision: Retain the host AWT default transform only as an observable presentation property; never copy it into simulated settings or renderer content scale.
  Rationale: Java/AWT already manages the host device transform when presenting a component. It is not a simulated-device input.
  Date/Author: 2026-08-10 / Codex

- Decision: Remove simulated density from `WindowConfiguration` entirely and expose the AWT transform only as `AwtWindow.getHostPresentationScale()`.
  Rationale: A host-window configuration that carries device density leaves an unnecessary path for future coupling. The renderer receives content scale from the configured screen surface instead.
  Date/Author: 2026-08-10 / Codex

## Outcomes & Retrospective

The reopened renderer audit is complete. `/density` now configures the shared Control/Window surface scale and physical pitch, while screen dimensions, safe areas, control geometry, font metrics, and dispatched input remain logical. Text is generated with a physical-size destination font and written directly into the physical screen backing; it is no longer rendered into a logical temporary image. Image and screen copies resample directly between the source and destination backings, preserve source/destination rectangles and alpha, and snapshot overlapping screen regions before writing. Logical RGB access maps through physical pitch, dithering and fading traverse physical pixels, and Control screenshots are logical images with density-scaled physical backing.

The final focused architecture set passed in `TotalCrossSDK/agent-logs/20260810-164556-test-full.log`. The complete SDK test task ran in `20260810-163914-test-full.log`; its only failure is the previously documented order-dependent `LegacySafeAreaVisualTest`, which received `LauncherNestedEventPumpTest`'s retained static `MainWindow`. The visual test passed alone in `20260810-164223-test-full.log`. The static audit found no `Settings.screenDensity` reference in `totalcross/ui` (including font/layout code), and no simulator expression couples AWT's default transform to `Settings`. Focused header validation covered all 20 changed first-party files with zero changes, and `git diff --check` passed.

## Context and Orientation

`TotalCrossSDK/src/main/java/tc/simulator/CommandLineParser.java` parses `/scr`, `/density`, `/safeAreaPortrait`, and `/safeAreaLandscape` into `LaunchOptions`. Those options flow through `tc.simulator.Launcher` and `ApplicationLoader` into `SettingsBridge.fillSettings`, which publishes the screen configuration to TotalCross code.

`TotalCrossSDK/src/main/java/totalcross/ui/gfx/Graphics.java` owns the Java renderer's shared `mainWindowPixels` array. A logical unit is a coordinate visible to application and UI code. A physical pixel is an element in this backing array. `contentScale` is the number of physical pixels per logical unit. The launcher must configure the shared screen destination with its simulated density before application UI paints.

`TotalCrossSDK/src/main/java/tc/simulator/FrameRenderer.java` wraps the shared physical pixels in a `BufferedImage` and emits `PreviewFrame` snapshots. A preview frame's width, height, and stride are physical; its density lets consumers map those pixels to logical size. `TotalCrossSDK/src/main/java/tc/simulator/InputDispatcher.java` receives AWT component coordinates, removes only host presentation scale, and must dispatch the resulting logical coordinates.

`TotalCrossSDK/src/main/java/tc/simulator/awt/AwtWindow.java` sizes the host window and exposes its AWT graphics transform. The host transform is the operating system's HiDPI presentation transform. It is distinct from simulated density and must not configure the TotalCross renderer.

## Plan of Work

First extend `CommandLineParserTest` to prove density leaves screen and safe-area values logical, and update `AwtWindowTest` to prove presentation scale and rendered viewport size are density-independent. Add focused launcher/render tests that configure a logical screen at density 2, initialize the shared destination, draw at logical coordinates, and assert both the physical frame dimensions and physical pixel coverage. Add a narrow input-conversion test that proves an AWT coordinate is divided by presentation scale exactly once and never by content scale.

Then remove parser-side multiplication. Introduce one explicit Java-screen surface configuration boundary in the graphics layer. It will retain logical dimensions and content scale, derive physical width, height, stride, and allocation length, and make every control graphics view use the same destination scale. Generalize existing scaled-backing primitive and text paths from image-only backing to the shared screen backing while retaining image-specific source handling.

Update `FrameRenderer` to construct and emit physical-sized images using the surface's physical width and height while continuing to report configured density. Reset the image when logical size or density changes. Update launcher resize and orientation paths to reconfigure the destination without changing logical layout values.

Finally remove the AWT monitor-transform write into `Settings.screenDensity` and renderer scales. Make AWT window sizing and automatic fit use logical viewport dimensions and `/scale` alone. Keep mouse conversion tied only to the AWT presentation scale.

## Concrete Steps

From the repository root, run focused tests with:

    TotalCrossSDK/gradlew-agent test \
      --tests tc.simulator.CommandLineParserTest \
      --tests tc.simulator.SimulatorLauncherTest \
      --tests tc.simulator.awt.AwtWindowTest \
      --tests totalcross.LauncherPreviewSurfaceTest

After implementation, run the relevant graphics and logical-layout suites as named by source inspection, then validate touched headers:

    python3 scripts/validate-copyright-headers.sh --files <changed first-party files>
    git diff --check -- <changed paths>

The full Gradle output remains in `TotalCrossSDK/agent-logs`; the agent summary should report `status=success`.

## Validation and Acceptance

The parser test must prove `/scr 320x480x32 /density 2 /safeAreaPortrait 10,3,7,4` produces logical width 320, logical height 480, unchanged logical insets, and density 2.

The renderer test must prove a 320 by 480 logical screen at density 2 emits a `PreviewFrame` whose physical width, height, and stride are 640, 960, and 640. A logical one-unit fill must cover its expected density-scaled physical region, demonstrating rasterization rather than metadata-only scaling.

The layout/font assertion must compare density 1 and density 2 and observe identical logical `Settings` dimensions, preferred control geometry, and logical font metrics. The safe-area assertion must observe the exact configured logical values. Input assertions must observe identical dispatched coordinates for both densities at the same AWT presentation scale.

The AWT tests must prove explicit `/scale` and automatic fitting operate on logical viewport dimensions, density-1 and density-2 framebuffers paint into the same host viewport, and `WindowConfiguration` has no density input. A host transform must not change `Settings.screenDensity` or `Graphics.contentScale`.

## Idempotence and Recovery

All test and validation commands are repeatable. The implementation changes no persistent user data. Existing unrelated untracked `.agent` artifacts must remain untouched. If a shared static screen configuration leaks between tests, tests must restore it explicitly rather than relying on execution order. Do not use destructive Git commands.

## Artifacts and Notes

The initial baseline passed:

    status=success
    duration_seconds=3
    full_log=agent-logs/20260810-154600-test-full.log

Initial focused and expanded validation passed:

    status=success
    full_log=agent-logs/20260810-155727-test-full.log
    full_log=agent-logs/20260810-160105-test-full.log
    isolated_visual_log=agent-logs/20260810-160250-test-full.log

Final reopened-boundary validation:

    status=success
    focused_architecture_log=agent-logs/20260810-164556-test-full.log
    full_suite_log=agent-logs/20260810-163914-test-full.log (one known order-dependent failure)
    isolated_visual_log=agent-logs/20260810-164223-test-full.log

## Interfaces and Dependencies

The graphics layer now provides `Graphics.configureMainWindowSurface(int logicalWidth, int logicalHeight, double contentScale)` plus physical-dimension and scale getters for the launcher renderer. It uses the existing Java pixel array and `BufferedImage`; no rendering dependency was added.

`Graphics` additionally exposes the destination's physical width, height, and pitch so pitch-dependent code and tests do not infer them from logical dimensions. `PreviewFrame` continues to expose physical `width`, `height`, and `stride` plus density. `Settings.screenWidth` and `Settings.screenHeight` remain logical. `AwtWindow.getScale()` remains host presentation scale. Host `GraphicsConfiguration.getDefaultTransform()` is never a source for simulated density.

Plan revision note (2026-08-10): Created this plan after tracing the current parser, AWT, renderer, input, and Java Graphics paths. The decisions above resolve density/presentation ambiguity before implementation.

Plan revision note (2026-08-10 19:03Z): Marked implementation and validation complete, recorded the explicit AWT/simulated-surface boundary, and documented the isolated evidence for the unrelated full-suite ordering failure.

Plan revision note (2026-08-10 19:22Z): Reopened completion after a full direct-pixel audit found several logical-backing assumptions that the original focused tests missed. The plan now requires one density 1/2/3 end-to-end fixture and a final requirement-by-requirement audit.

Plan revision note (2026-08-10 19:48Z): Completed the reopened raster boundary, recorded the density-matrix evidence, and documented the only full-suite failure and its passing isolated reproduction.
