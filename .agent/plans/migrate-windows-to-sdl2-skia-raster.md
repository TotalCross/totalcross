<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Migrate Windows desktop to SDL2 windowing and Skia raster rendering

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`. It is written for autonomous execution by a Luna agent on a branch created after the separate migration that makes TotalCross consume static SDL2 from `TotalCross/totalcross-depot-tools`.

## Purpose / Big Picture

Migrate TotalCross Windows desktop from the current coupled Win32 implementation to an explicit backend architecture where windowing, event delivery, rendering, and graphics storage are selected independently enough to support both the existing implementation and the new SDL2 + Skia path.

At the end of the plan:

- the existing Windows implementation is explicitly the `Native` windowing backend with the `Legacy` renderer;
- the SDL2 implementation is a shared `SDL` windowing backend usable on Windows;
- event dispatch follows the selected windowing backend: Windows Native uses the existing Win32 event implementation, while SDL windowing uses the shared SDL event implementation;
- Windows SDL uses the software framebuffer graphics path;
- Skia on Windows uses its raster/software path over that framebuffer;
- no TotalCross GPU graphics backend is added or enabled by this plan;
- `Native + Legacy` remains a supported explicit fallback configuration;
- after `SDL + Skia` is independently functional and validated, Windows defaults change to `SDL + Skia`;
- after that default switch, the plan resolves the known SDL/Skia correctness problems, including HiDPI/per-monitor scaling, resize/display transitions, screen/backbuffer/Skia lifecycle, deterministic pixel format, SDL surface ownership, text/input correctness, and other defects discovered while exercising the new default;
- the final Windows default is SDL2 windowing + Skia raster rendering with a physical-pixel framebuffer and logical TotalCross coordinates.

The migration is complete only after the post-default hardening milestones pass. The intermediate commit that changes the default is a deliberate checkpoint, not a release-ready stopping point.

## Execution Goal

Refactor TotalCross Windows desktop so the existing Win32 implementation is preserved as `TC_WINDOWING_NATIVE + TC_RENDERER_LEGACY`, add a fully functional `TC_WINDOWING_SDL + TC_RENDERER_SKIA` Windows path using only the software framebuffer graphics backend, route events according to the selected windowing backend, promote SDL + Skia to the Windows default only after the alternate path works, then harden the new default by fixing HiDPI, resize/display-change, lifecycle, pixel-format, SDL ownership, input, and related SDL/Skia defects, while keeping Native + Legacy buildable as a fallback and delivering the work in frequent logical Conventional Commits.

## Luna Agent Execution Contract

Execute this plan as an implementation plan, not as a design-only document.

Before editing:

1. Read `AGENTS.md` and `.agent/PLANS.md` in the active TotalCross branch.
2. Inspect the current branch, commit, and path-scoped worktree state.
3. Preserve all unrelated local changes.
4. Never use destructive Git cleanup, reset, checkout, rebase, amend, force-push, or history rewriting.
5. Use narrow searches and path-scoped reads. Do not repeatedly dump large files, plans, logs, generated projects, or dependency trees.
6. Redirect full build/test output to ignored task-specific logs and surface only concise summaries or the first relevant failure.
7. Create frequent logical commits at the checkpoints below. Do not create micro-commits for individual lines, and do not combine unrelated architecture layers.
8. Use the English Conventional Commit format required by the repository, including scope and an explanatory body for every non-trivial commit.
9. After each logical commit, rewrite the state file with the last commit, validation performed, remaining risk, and next concrete action.
10. Do not create evidence, screenshots, reports, or benchmark artifacts unless they materially help execution or resumption. Working code and focused validation take priority.

When a platform or architecture cannot be executed on the current Luna host, continue with the strongest available focused validation and record the deferred runtime proof. Do not pretend a compile-only result proves runtime behavior.

## Working Set and Resume Protocol

Use the plan key `windows-sdl-skia-raster`.

Maintain:

- `.agent/state/windows-sdl-skia-raster.md` as the first read after interruption. Rewrite it after each logical commit. Record active milestone/slice, last commit, active paths, focused validation, deferred validation, blockers, deliberate out-of-scope files, and the next concrete action.
- `.agent/evidence/windows-sdl-skia-raster.md` only for concise append-only validation records that would otherwise need to be reconstructed.
- `.agent/archive/windows-sdl-skia-raster-history.md` only if completed milestone detail starts bloating this active plan.
- `.agent/reports/windows-sdl-skia-raster-editorial.md` at important milestone closure and final completion, following `.agent/PLANS.md`.

On resume, read state first, inspect only active paths and the focused diff, then continue from the recorded next action. Do not routinely reread the full plan or evidence archive.

## Progress

- [x] (2026-08-26) Verified the pinned depot-tools SDL 2.32.8 contract and
  recorded the Windows baseline as deferred because this macOS host has no
  Windows toolchain/runtime or staged Windows SDL artifacts.
- [x] (2026-08-26) Made graphics, renderer, and windowing selections explicit
  cache contracts with derived inverse macros; preserved current platform
  defaults, accepted legacy inverse inputs, and rejected unsupported WinCE
  and Windows Native + Skia combinations. Evidence is in the migration state
  file and configure logs.
- [x] (2026-08-26) Extracted the SDL `TScreenSurfaceEx` definition into the
  shared `nm/ui/sdl/gfx_ex.h` backend and left Linux `gfx_ex.h` DirectFB-only;
  `GraphicsPrimitives.h` now selects SDL state before desktop platform identity.
- [x] (2026-08-26) Routed graphics implementation by backend selection: WinCE
  and Windows Native retain the Win32 DIB/BitBlt implementation, while Windows
  SDL can reach the generic software graphics path.
- [x] (2026-08-26) Added a shared SDL event backend and made event dispatch
  follow windowing selection; Native Windows remains exclusively on Win32,
  while Linux `event_c.h` now contains only DirectFB handling. Moved the
  Windows DLL module capture into the platform startup source so SDL builds do
  not need the Win32 event header.
- [x] (2026-08-26) Added the SDL Windows windowing adapter for text-input and
  UTF-8 title updates, routed Windows SDL title/SIP calls through it, and
  bridged the SDL window to `mainHWnd` with SDL SysWM without adding a Win32
  event pump.
- [x] (2026-08-26) Configured and built the SDL + Legacy + Software diagnostic
  path on the available macOS SDL lane, proving the shared SDL event/windowing
  and generic software graphics sources compile together. Windows interactive
  smoke remains deferred to a Windows-capable lane.
- [x] (2026-08-26) Configured and built SDL + Skia + Software on the available
  macOS SDL lane; the Skia software branch compiled without enabling the GLES
  path. Windows runtime exercise remains deferred.
- [x] (2026-08-26) Validated the two SDL configurations available on this host
  and recorded the Windows Native + Legacy and runtime comparison as deferred
  because no Windows toolchain/runtime is installed. A macOS Native proxy was
  rejected by the existing platform model and is not treated as Windows proof.
- [x] (2026-08-26) Changed Windows desktop defaults to SDL + Skia + Software;
  explicit Native + Legacy remains available through the cache selectors and
  WinCE stays fixed to Native + Legacy + Software.
- [x] (2026-08-26) Added SDL logical/physical metric querying, per-monitor scale
  handling, SDL Windows DPI awareness, central screen-change dispatch, and
  constrained the legacy 0.75 density override to non-SDL Windows.
- [x] (2026-08-26) Separated SDL window/renderer lifetime from backbuffer
  lifetime and added explicit Skia screen teardown/rebind around recreation.
- [x] (2026-08-26) Replaced window-surface/format discovery with a fixed
  ARGB8888 streaming texture and deterministic Skia BGRA mapping.
- [x] (2026-08-26) Hardened SDL event dispatch, mouse-union handling, UTF-8
  committed text including surrogate pairs, composition behavior, lifecycle
  events, fullscreen/resizable initialization, and SDL title/input behavior.
- [x] (2026-08-26) Ran the available final selector matrix and affected macOS
  SDL builds, audited ownership/GPU conditions, and recorded Windows
  architecture/runtime validation as deferred because this host has neither a
  Windows toolchain nor staged Windows artifacts.
- [x] (2026-08-26) Reconciled implementation state, validation limitations,
  outcomes, and the factual editorial report.
- [x] (2026-08-31) Enabled SDL text input lifecycle, moved special-key
  translation to the SDL backend, and filtered reserved VM options from the
  application command line while preserving parsing after `/cmd`.
- [x] (2026-08-31) Passed focused SDL desktop contract tests, copyright/header
  validation, `git diff --check`, macOS SDL + Skia Release native build, and
  SDK distribution build. Windows interactive smoke remains deferred because
  this host has no Windows toolchain/runtime.
- [x] (2026-08-31) Closed the SDL startup-order gap by activating text input
  after SDL window creation and passed the incremental macOS native rebuild.
- [x] (2026-08-31) Preserved raw SDL event modifiers through the shared event
  layer and kept Windows native hotkey registration on Win32 VK values.
- [x] (2026-08-31) Separated the composite VM command line from the filtered
  application payload, removed reserved options after `/cmd`, preserved
  unrelated arguments and near-matches, and added existing-suite regression
  coverage.
- [x] (2026-08-31) Re-ran focused contracts, test-enabled startup syntax,
  fourteen-file header validation, diff checks, file-size limits, the final
  macOS SDL + Skia + Software build, and the SDK distribution build. The SDL
  Windows hotkey bridge and Ctrl+A/C/V path are source-validated; Windows
  interactive proof is explicitly deferred to CI.
- [x] (2026-08-31) Restored the historical non-desktop `" /cmd "` lookup while
  keeping the exact desktop separator and compaction behavior unchanged.
- [x] (2026-08-31) Centralized SDL special-key dispatch, restored historical
  `SK_SCREEN_CHANGE` rotation/minimum-dimension behavior, and covered the
  existing Ctrl+A/C/P/V/X/Space editing shortcuts without duplicate text
  events.
- [x] (2026-08-31) CI run `33450370159` at `51dde0f43` passed Android and iOS
  compilation and all other enabled build jobs. The migration is complete;
  Windows interactive keyboard smoke remains explicitly deferred because this
  host has no Windows runtime.
- [x] (2026-09-01) Fixed `/scr` filtering for comma-separated payloads with
  internal spaces by returning the existing `%n` endpoint from
  `parseScreenBounds()` and consuming it directly in the generic filter.
  Compact `/scr`, near-match preservation, exact application payloads, header
  validation, the macOS VM/Launcher build, and exact-HEAD Merge Flow passed.
- [x] (2026-09-01) Centralized SDL and Native Windows startup sizing in the
  Window layer. `/scr` takes precedence over `TC_WIDTH`/`TC_HEIGHT`, TCZ sizes,
  and half-display defaults; SDL now consumes all three TCZ sizes and gates
  resizable windows off in fullscreen. Focused contracts, the macOS build, and
  exact-HEAD Merge Flow passed at `9572636b4`.
- [x] (2026-09-01) Corrected explicit `/scr` tracking so `-1` dimensions keep
  command-line precedence, use half-display defaults, and cannot be replaced
  by environment or TCZ sizing. Fullscreen SDL startup now uses the full
  display only when no size source is supplied. Executable table coverage,
  focused contracts, headers, macOS VM/Launcher build, and exact-HEAD Merge
  Flow passed at `6501430ad`.
- [x] (2026-09-01) Completed the final desktop startup-policy refactor in
  `7a8a11ffd` and `e562e0501`: `WindowStartup` now owns the shared options,
  environment loading, size/position precedence, and fullscreen/maximized/
  resizable resolution. SDL and Native Windows translate one resolved
  configuration, and WinCE remains on its existing native path.

## Current Architecture and Scope

### Required starting state

This plan will run on a branch after the separate SDL dependency migration.

Before implementation, verify that the active branch already:

- obtains SDL2 from the pinned `totalcross-depot-tools` checkout;
- resolves static SDL 2.32.8 through the depot CMake modules;
- links through `SDL2::SDL2` rather than `SDL2_LIBRARY`;
- does not depend on a host/system SDL installation for supported SDL builds;
- has Windows SDL static artifacts available for the Windows architectures targeted by the build.

If this prerequisite is not true, stop and report a prerequisite mismatch. Do not silently fold the earlier dependency migration into this plan.

Skia must likewise resolve through the existing depot-tools `Skia::Skia` contract. Do not create a second Skia distribution mechanism.

### Existing backend dimensions

`TotalCrossVM/cmake/TCGraphics.cmake` already names three architectural dimensions:

    Graphics:
        TC_GRAPHICS_GLES
        TC_GRAPHICS_SOFTWARE

    Renderer:
        TC_RENDERER_SKIA
        TC_RENDERER_LEGACY

    Windowing:
        TC_WINDOWING_SDL
        TC_WINDOWING_NATIVE

At plan creation, several source dispatchers still select Windows code from `WIN32` before considering the configured backend.

The target Windows matrix for this plan is:

    Native + Legacy + Software
        supported fallback
        existing Windows behavior
        default until the explicit promotion milestone

    SDL + Legacy + Software
        supported diagnostic path when practical
        useful to isolate windowing/presentation defects from Skia defects

    SDL + Skia + Software
        supported target path
        becomes the Windows default after migration validation

    Native + Skia
        not required by this plan
        reject at CMake configuration time unless it becomes valid naturally
        without Windows-specific implementation work

    any GPU graphics backend
        out of scope

WinCE remains Native + Legacy. Do not migrate WinCE to SDL or Skia.

### Current couplings that must be separated

`TotalCrossVM/src/event/Event.c` currently selects `event/win/event_c.h` whenever `WIN32` is defined, so Windows cannot use the SDL event pump by windowing selection alone.

`TotalCrossVM/src/nm/ui/gfx_Graphics.c` currently selects `nm/ui/win/gfx_Graphics_c.h` before the graphics backend macros. That file owns both native Win32 window creation and DIB/BitBlt presentation, preventing Windows SDL from reaching the generic software backend.

`TotalCrossVM/src/nm/ui/GraphicsPrimitives.h` selects `nm/ui/win/gfx_ex.h` before the existing SDL-capable Linux extension. SDL window/renderer/texture state is currently hidden inside a Linux-specific type.

`TotalCrossVM/src/nm/ui/Window.c` selects `nm/ui/win/Window_c.h` whenever `WIN32` is defined. Some functions are true windowing operations, while others are Windows platform integrations.

`TotalCrossVM/src/event/linux/event_c.h` mixes SDL event handling and DirectFB fallback. Shared SDL events must no longer be owned by a Linux-named file.

`TotalCrossVM/src/init/tcsdl.cpp` currently creates the SDL window, renderer, texture/surface, backing pixels, and teardown in one lifetime model that is not compatible with TotalCross screen recreation.

`TotalCrossVM/src/nm/ui/backend/graphics/software/gfx_Graphics_c.h` already has the right high-level SDL + Skia shape, but `graphicsDestroy(screen, true)` currently tears down all SDL state.

`TotalCrossVM/src/nm/ui/GraphicsPrimitives_c.h` already centralizes `TScreenConfiguration`, `screenApplyConfiguration()`, and `screenChangeCommitted()`. SDL must feed this mechanism instead of directly mutating screen metrics.

### Windows Native implementation that must remain

Preserve `TotalCrossVM/src/event/win/event_c.h` as the Native event backend.

Preserve `TotalCrossVM/src/nm/ui/win/gfx_Graphics_c.h` as the Native window + DIB/BitBlt implementation. Refactor only platform-only responsibilities that must no longer live inside an event or renderer boundary.

Preserve the ability to build Windows with Native + Legacy throughout the plan and after the final default changes.

### Windows platform identity must remain Windows

Selecting SDL does not mean selecting Linux behavior.

Windows-specific filesystem, process, registry, clipboard, SIP/TabTip, platform identity, native-library loading, and other OS services continue to use Windows implementations unless they are truly windowing/event concerns.

Where Windows platform integrations require `mainHWnd`, obtain the native HWND from the SDL window through SDL SysWM and populate the existing handle. Do not start a parallel Win32 message pump. SDL is the sole window-event owner when `TC_WINDOWING_SDL` is selected.

### Software-only graphics contract

For Windows SDL + Skia:

    TC_GRAPHICS_SOFTWARE = ON
    TC_RENDERER_SKIA     = ON
    TC_WINDOWING_SDL     = ON

Skia must use its raster path:

    SkBitmap / raster SkCanvas
        -> CPU physical framebuffer
        -> SDL streaming texture
        -> SDL_Renderer presentation

Do not create an OpenGL context. Do not add `TC_GRAPHICS_GL`, Direct3D, Vulkan, Metal, ANGLE, Ganesh setup, Graphite setup, or any other TotalCross GPU backend.

The depot Skia binary may contain GPU-capable code or transitive platform dependencies. That does not authorize using a GPU graphics path in TotalCross.

## Plan of Work

### Milestone 0 — Verify prerequisite and capture Native + Legacy baseline

Goal: prove the branch starts after the static SDL migration and record the fallback behavior.

Inspect only the current relevant architecture files and verify `SDL2::SDL2`/`Skia::Skia` consumption. If the SDL prerequisite is absent, stop.

Build and, when possible, run the current Windows Native + Legacy configuration before changing dispatch. Record concise baseline facts for startup, initial dimensions, resize, mouse, keyboard/text, minimize/restore, close, and title change. Do not fix legacy defects here.

Acceptance: Native + Legacy baseline is reproducible and static depot SDL is confirmed.

Validation level: 1. No implementation commit required.

### Milestone 1 — Make backend selection a real Windows build contract

Goal: allow Windows to select SDL and Skia explicitly without changing the default yet.

Refactor `TotalCrossVM/cmake/TCGraphics.cmake` so command-line/cache choices are not overwritten by unconditional normal-variable assignments.

Preserve the macro names consumed by C/C++:

    TC_WINDOWING_SDL
    TC_WINDOWING_NATIVE
    TC_RENDERER_SKIA
    TC_RENDERER_LEGACY
    TC_GRAPHICS_SOFTWARE
    TC_GRAPHICS_GLES

Prefer one user-facing boolean per pair and derive the inverse, so users do not have to set contradictory flags manually.

Keep Windows defaults in this milestone:

    TC_WINDOWING_SDL=OFF
    TC_WINDOWING_NATIVE=ON
    TC_RENDERER_SKIA=OFF
    TC_RENDERER_LEGACY=ON
    TC_GRAPHICS_SOFTWARE=ON

Keep WinCE fixed to Native + Legacy and fail clearly if SDL/Skia is requested for WinCE.

Allow `SDL + Legacy` and `SDL + Skia`. `Native + Skia` may be rejected because adapting Skia to the Win32 DIB backend is outside scope.

Acceptance: the Windows backend matrix is representable and default behavior is unchanged.

Validation level: 2.

Commit:

    build(cmake,windows): expose windowing and renderer backends

### Milestone 2 — Extract shared SDL windowing state from Linux

Goal: make SDL a backend rather than a Linux platform detail.

Create a shared SDL-specific location consistent with the existing layout, preferably:

    TotalCrossVM/src/nm/ui/sdl/gfx_ex.h

Move only SDL `TScreenSurfaceEx` state out of `nm/ui/linux/gfx_ex.h`. Leave DirectFB state in Linux.

Change `GraphicsPrimitives.h` selection so WinCE remains Win32-specific, `TC_WINDOWING_SDL` selects the shared SDL type before desktop platform identity, Native Windows selects `win/gfx_ex.h`, and other platforms retain their current path.

Do not duplicate SDL structs in Windows and Linux.

Acceptance: Linux/macOS SDL and Windows SDL compile against the same SDL screen-extension type; Native Windows remains on the Win32 type.

Validation level: 2.

Commit:

    refactor(windowing): extract shared SDL surface state

### Milestone 3 — Route graphics implementation by backend, not WIN32 identity

Goal: let Windows SDL reach the generic software graphics implementation while Native Windows stays on DIB/BitBlt.

Refactor `TotalCrossVM/src/nm/ui/gfx_Graphics.c` so semantic selection is equivalent to:

    WinCE -> win/gfx_Graphics_c.h
    Windows + TC_WINDOWING_NATIVE -> win/gfx_Graphics_c.h
    TC_GRAPHICS_GLES -> GLES backend
    TC_GRAPHICS_SOFTWARE -> generic software backend

Do not rewrite the Native Win32 implementation into SDL abstractions.

Acceptance: Native Windows uses the Win32 backend; Windows SDL uses the generic software backend; Linux/macOS behavior is unchanged; no GPU backend is added.

Validation level: 2.

Commit:

    refactor(graphics,windows): route software backend by windowing

### Milestone 4 — Make event ownership follow windowing

Goal: Native keeps Win32 events; SDL uses a shared SDL event implementation on Windows and existing SDL desktops.

Create:

    TotalCrossVM/src/event/sdl/event_c.h

Extract SDL event handling from `src/event/linux/event_c.h`; leave DirectFB-only handling there.

Change `Event.c` so `TC_WINDOWING_SDL` selects the shared SDL event backend before desktop Windows/Linux platform dispatch. Do not include both Win32 and SDL event pumps in one build.

Audit `event/win/event_c.h` for Windows platform responsibilities that are not Native-event responsibilities. In particular, `DllMain` currently lives in the Win32 event backend. Move platform-only initialization such as DLL attach/module capture to an appropriate Windows init/platform source compiled regardless of windowing. Preserve ordering and behavior.

Audit the native-library `HandleEvent(void*)` contract. Do not run the Win32 pump in SDL mode just to preserve raw Win32 event pointers. Preserve/document backend-specific raw-event behavior rather than inventing a cross-backend ABI.

Acceptance: Native Windows events are exclusively Win32; SDL Windows events are exclusively SDL; DLL/platform initialization still works under both modes; Linux DirectFB remains isolated.

Validation level: 2.

Commit:

    refactor(event): select event handling by windowing

### Milestone 5 — Preserve Windows platform integrations under SDL

Goal: SDL owns the Windows window while Windows OS services retain the native handle they need.

After `SDL_CreateWindow`, use SDL2 SysWM on Windows to obtain the HWND and set the existing `mainHWnd`. Clear it on final SDL window destruction.

Treat `mainHWnd` as a platform integration bridge only. Its presence must not activate the Native event pump.

Audit Windows desktop code that consumes `mainHWnd` and preserve services that do not conflict with SDL ownership.

Split true windowing operations in `Window.c` where necessary. Prefer a shared SDL implementation such as:

    TotalCrossVM/src/nm/ui/sdl/Window_c.h

At minimum, SDL window title changes must use `SDL_SetWindowTitle`. SDL text input activation/deactivation should use SDL APIs where the existing TotalCross contract maps to text input. Windows TabTip integration may still use the HWND if required.

Acceptance: Windows SDL has a valid HWND for platform services, uses SDL for window ownership, and has no duplicate message loop.

Validation level: 2.

Commit:

    refactor(windowing,windows): bridge SDL window to Win32 services

### Milestone 6 — Bring up Windows SDL + Legacy over software graphics

Goal: establish SDL windowing/presentation independently of Skia.

Build:

    TC_WINDOWING_SDL=ON
    TC_RENDERER_SKIA=OFF
    TC_RENDERER_LEGACY=ON
    TC_GRAPHICS_SOFTWARE=ON

Use depot `SDL2::SDL2`; do not add host SDL lookup or DLL copying.

Make only the minimum `tcsdl`/software-backend changes required for an SDL window, CPU framebuffer, SDL presentation, and SDL event handling on Windows. Do not prematurely fold the later comprehensive HiDPI/lifecycle/pixel redesign into this bring-up unless a defect blocks basic operation.

Runtime smoke: window creation, first frame, mouse click/move/drag, special keys, ASCII committed text, wheel, title, minimize/restore, close, and basic resize if the current path can survive it.

Acceptance: Windows SDL + Legacy reaches an interactive rendered application without Win32 window creation or Win32 event pumping.

Validation level: 3.

Commit:

    feat(windowing,windows): enable SDL software presentation

### Milestone 7 — Enable Windows SDL + Skia raster as an alternate configuration

Goal: render Windows SDL through Skia CPU raster while the default is still Native + Legacy.

Build:

    TC_WINDOWING_SDL=ON
    TC_RENDERER_SKIA=ON
    TC_RENDERER_LEGACY=OFF
    TC_GRAPHICS_SOFTWARE=ON

Use `Skia::Skia`. Ensure Windows remains in the `TC_GRAPHICS_SOFTWARE` branch of Skia; never create a GL/GPU context.

Intended path:

    TotalCross logical drawing
        -> Skia raster canvas
        -> CPU physical framebuffer
        -> SDL streaming texture
        -> SDL presentation

Exercise fills/strokes, text, images, clipping, alpha/transparency, anti-aliased shapes, and interactive updates. Compare against Native + Legacy and, when useful, SDL + Legacy to localize defects.

Record known HiDPI/resize/lifecycle/pixel issues for mandatory later hardening rather than hiding them.

Acceptance: Windows SDL + Skia raster is usable as an explicit alternate configuration and uses no TotalCross GPU backend.

Validation level: 3.

Commit:

    feat(renderer,windows): enable Skia raster with SDL

### Milestone 8 — Validate migration before promotion

Goal: prove the new backend is functional enough to become default while fallback remains trustworthy.

Run:

    Native + Legacy + Software
    SDL + Legacy + Software
    SDL + Skia + Software

Validate configure + compile + link for the Windows architectures currently built/distributed by the repository. Prefer x86 and x64 when both are supported; include ARM64 when an existing TotalCross lane supports it.

For runtime-capable targets, use the same smoke scenario. Confirm SDL + Skia has no TotalCross GL/GLES context setup and does not incorrectly mark `Settings.isOpenGL` merely because SDL internally chooses an accelerated presentation driver.

Validate inexpensive Windows platform services that depend on `mainHWnd`.

Do not promote if SDL + Skia cannot start, render, receive basic input, close, or recover from ordinary activation.

Acceptance: SDL + Skia is a functioning Windows configuration; Native + Legacy still works.

Validation level: 3.

Commit only if durable tests/build checks were added:

    test(windows): cover native and SDL backend matrix

### Milestone 9 — Promote SDL + Skia to the Windows default

Goal: change only the default selection after the alternate path is proven.

Update `TCGraphics.cmake` so Windows desktop defaults to:

    TC_GRAPHICS_SOFTWARE=ON
    TC_WINDOWING_SDL=ON
    TC_WINDOWING_NATIVE=OFF
    TC_RENDERER_SKIA=ON
    TC_RENDERER_LEGACY=OFF

WinCE remains Native + Legacy.

Keep explicit fallback through backend flags so Native + Legacy requires no source edits.

Update normal Windows CI/build configuration as needed so the default lane exercises SDL + Skia. Retain at least one focused fallback configuration/build check so Native + Legacy cannot silently rot.

Do not remove Legacy or Native.

Acceptance: normal Windows desktop build selects SDL + Skia + Software; explicit Native + Legacy remains buildable.

Validation level: 2 for this policy commit, with immediate continuation into hardening. This intermediate revision is not final plan completion.

Commit:

    build(windows): make SDL and Skia default

### Milestone 10 — Fix Windows HiDPI and per-monitor logical scaling

Goal: make logical TotalCross dimensions independent of physical framebuffer density and survive monitor transitions correctly.

Before `SDL_Init(SDL_INIT_VIDEO)` on Windows SDL, request SDL Windows DPI-scaled coordinates using the SDL 2.32.8 Windows DPI scaling hint and create the window with `SDL_WINDOW_ALLOW_HIGHDPI`.

Treat SDL window/input coordinates as logical units.

Create one shared metrics query, conceptually `TCSDL_QueryWindowMetrics()`, that reads:

    logical width/height  = SDL_GetWindowSize()
    physical width/height = SDL_GetRendererOutputSize()

Derive scale from physical/logical dimensions. Use physical dimensions for `screen.screenW/H`, CPU framebuffer, Skia bitmap/canvas, and SDL texture. Let the central TotalCross settings flow expose logical `Settings.screenWidth/Height` and `Settings.screenDensity`.

Do not derive content scale from `SDL_GetDisplayDPI()`.

TotalCross stores one scalar density. Verify X/Y scale agreement with a tolerance and handle unexpected non-uniform results explicitly.

Populate hRes/vRes through an appropriate reliable Windows mechanism only where required; do not make those values authoritative for content scale.

Constrain the legacy Windows small-screen 0.75 density override so it cannot overwrite measured SDL content scale. Preserve WinCE compatibility where required.

For `SIZE_CHANGED`, `DISPLAY_CHANGED`, and relevant `MOVED` transitions, re-query metrics and create a complete `TScreenConfiguration`. Feed it through `screenApplyConfiguration()`, `screenConsumePendingChanges()`, and `screenChangeCommitted()`. Do not directly mutate `screen.screenW/H/contentScale` in the event backend.

Keep pointer coordinates logical. Normalized touch coordinates should use logical window size.

Runtime scenarios: 100%, 125%/150%, maximize/restore, resize, and mixed-DPI monitor movement when hardware is available. Logical size must remain stable across DPI-only transitions while physical backing size and density change.

Acceptance: drawing scale, logical dimensions, physical backing dimensions, density, and input alignment are correct at non-100% DPI and during monitor transitions.

Validation level: 3.

Commit:

    fix(windowing,windows): apply per-monitor HiDPI metrics

### Milestone 11 — Fix SDL and Skia lifecycle for resize and density changes

Goal: screen recreation destroys/recreates only resources whose lifetime changed.

Split SDL ownership into:

Long-lived window lifetime:

    SDL_Window
    SDL_Renderer
    Windows HWND bridge

Recreatable framebuffer lifetime:

    CPU pixel buffer
    SDL streaming texture
    Skia raster bitmap/canvas binding
    physical width/height/pitch/pixel format

Do not let `graphicsDestroy(screen, true)` destroy the SDL window, renderer, HWND bridge, or SDL subsystem.

Refactor `tcsdl` around responsibilities equivalent to:

    TCSDL_InitWindow(...)
    TCSDL_QueryWindowMetrics(...)
    TCSDL_CreateBackBuffer(...)
    TCSDL_DestroyBackBuffer(...)
    TCSDL_DestroyWindow(...)

Initial startup creates window/renderer and metrics. `graphicsCreateScreenSurface()` creates the physical backbuffer before Skia binds it.

On `SCREEN_CHANGE_RECREATE_SURFACE`:

1. stop using the current Skia screen canvas/bitmap;
2. reset/destroy the Skia screen binding;
3. destroy SDL texture;
4. free old CPU framebuffer;
5. let TotalCross recreate its screen pixel array as designed;
6. allocate new physical framebuffer;
7. create new SDL texture;
8. bind a fresh Skia raster bitmap/canvas;
9. repaint.

Final shutdown additionally destroys SDL renderer/window and calls SDL shutdown exactly once.

Add explicit Skia screen-surface teardown/rebind. Do not leave `new SkCanvas(bitmap)` without matching cleanup. Reset any screen Skia object before the pixel memory it references is freed. Keep image-surface lifetime separate.

Never call `SDL_FreeSurface()` on an `SDL_GetWindowSurface()` result.

Exercise repeated resize, maximize/restore, DPI changes, minimize/restore, and shutdown.

Acceptance: no stale pointer, double free, destroyed-window-on-resize, leaked screen canvas, or obsolete texture dimensions remain.

Validation level: 3.

Prefer two commits if independently buildable:

    refactor(windowing): separate SDL window and backbuffer lifetime
    fix(renderer): rebind Skia after screen recreation

Otherwise use one focused lifecycle commit with an explanatory body.

### Milestone 12 — Make SDL/Skia pixel path deterministic

Goal: remove dependence on native window pixel format and eliminate unsafe window-surface presentation.

Do not use `SDL_GetWindowPixelFormat()` as the authoritative TotalCross framebuffer/Skia/texture format.

Choose an explicit 32-bit pixel contract compatible with TotalCross ARGB values, Windows little-endian memory, Skia raster `SkColorType`, and SDL streaming textures.

Prefer `SDL_PIXELFORMAT_ARGB8888` when a focused channel-order test confirms the mapping. On little-endian Windows it is expected to map to the appropriate Skia BGRA memory interpretation, but prove this rather than assuming it.

If fallback is needed, inspect `SDL_RendererInfo.texture_formats` and accept only formats with explicit tested Skia mappings. Never continue with `kUnknown_SkColorType`.

Set `screen.bpp`, `screen.pitch`, and `screen.pixelformat` from the chosen framebuffer contract, not the window.

Use an SDL streaming texture regardless of whether SDL's renderer driver is internally accelerated or software. Remove the `usesTexture`/`SDL_GetWindowSurface()` presentation split unless a proven platform requirement exists.

Normal path:

    CPU pixels
      -> SDL_UpdateTexture
      -> SDL_RenderCopy
      -> SDL_RenderPresent

Add the smallest useful deterministic color/channel test or existing fixture. Verify opaque red/green/blue, white/black, semi-transparent color, and a representative image/readback path when practical.

Acceptance: colors/channels/alpha are correct and independent of window format; no SDL-owned window surface is manually freed.

Validation level: 3.

Commit:

    fix(renderer): use deterministic SDL Skia pixel format

### Milestone 13 — Harden SDL event semantics and window behavior

Goal: finish correctness work exposed by making SDL the Windows default.

Use explicit SDL event-type dispatch rather than broad numeric comparisons.

Fix mouse union usage: motion reads `event.motion`, button events read `event.button`.

Preserve logical coordinates under HiDPI.

Fix committed text input: `SDL_TEXTINPUT` is UTF-8. Decode code points and emit TotalCross/Java UTF-16 `JChar` units correctly, including surrogate pairs above U+FFFF. Do not post raw UTF-8 bytes as individual characters.

Handle `SDL_TEXTEDITING` deliberately. If TotalCross has no active-composition API, keep composition non-destructive and forward only committed `SDL_TEXTINPUT`; do not invent a new public Java API without an existing architectural requirement.

Map window lifecycle events needed for parity:

- minimize -> `postOnMinimizeOrRestore(true)`;
- restore -> `postOnMinimizeOrRestore(false)`;
- exposed -> mark/repaint rather than present stale data;
- close/SDL_QUIT -> normal shutdown;
- size/display/move -> Milestone 10 metric refresh;
- focus changes where TotalCross behavior depends on them.

Review wheel direction/pointer position and SDL text-input start/stop integration.

Fix fullscreen/window initialization discovered during migration. The `fullScreen` argument and TotalCross settings must be authoritative; do not retain behavior where absence of `TC_FULLSCREEN` unexpectedly forces fullscreen. Honor the existing resizable-window contract with SDL flags.

Ensure `windowSetDeviceTitle()` follows the selected windowing backend.

Re-run the native-library event-hook audit after SDL becomes default. Do not revive a parallel Win32 pump.

Acceptance: ASCII/non-ASCII committed text, mouse, drag, wheel, keys, minimize/restore, focus, resize/display transition, close, title, fullscreen, and resizable behavior work through SDL on Windows.

Validation level: 3.

Split commits by concern when independently reviewable:

    fix(event): decode SDL text input as UTF-8
    fix(event): harden SDL pointer and window events
    fix(windowing,windows): align SDL window state behavior

### Milestone 14 — Final backend matrix and regression closure

Goal: prove the new default and preserved fallback are healthy after hardening.

Required Windows configurations:

    default: SDL + Skia + Software
    fallback: Native + Legacy + Software

Recommended diagnostic compile:

    SDL + Legacy + Software

Unsupported combinations must fail clearly rather than accidentally mix ownership.

For each supported Windows architecture available in the repository family, perform the strongest feasible configure/build/link validation.

For runtime-capable builds exercise startup/shutdown, normal/fullscreen window, repeated resize, maximize/restore, minimize/restore, mouse/drag/wheel, special keys, ASCII text, accented text, a non-BMP character when possible, title change, Skia text/shapes/clipping/images/transparency, 100% DPI and at least one scaled DPI setting, and mixed-DPI monitor transition when available.

Check scaling invariants directly:

    logical window dimensions
    physical renderer output dimensions
    screen.contentScale
    Settings.screenWidth/Height
    Settings.screenDensity
    input coordinates

Use screenshots/video only when a visual defect cannot be characterized cheaply by runtime values.

Confirm no TotalCross GPU context/backend was introduced. Confirm Native + Legacy still uses Win32 events and DIB/BitBlt.

Run affected Linux/macOS SDL builds after shared SDL refactors to prevent regressions. Use the smallest relevant lanes, not a full release matrix unless required by `AGENTS.md`.

Update focused build/architecture documentation only as needed to describe the Windows default, fallback flags, software-only scope, and event ownership.

Validation level: 4 only here because the default Windows architecture changed.

Final documentation commit when needed:

    docs(build,windows): document SDL Skia default

## Surprises & Discoveries

Initial known observations:

- `Event.c` selects Win32 events by platform identity before windowing selection.
- SDL events are embedded in `event/linux/event_c.h`.
- `gfx_Graphics.c` selects Windows native graphics before the generic software backend.
- `GraphicsPrimitives.h` selects the Win32 extension before the SDL extension.
- SDL screen state lives in `nm/ui/linux/gfx_ex.h`.
- the generic software backend already has the essential SDL + Skia shape, but screen-change destruction currently tears down all SDL state.
- `screenChangeCommitted()` already centralizes logical settings and surface recreation; SDL should feed it rather than bypass it.
- `tcsdl.cpp` currently derives texture format from the window, has a special `SDL_GetWindowSurface()` path, and frees a window-owned surface during destruction.
- `tcsdl.cpp` currently destroys the SDL window/renderer/subsystem through the same operation used during screen recreation.
- Skia raster binds a global bitmap/canvas to supplied pixel memory without explicit matching screen teardown.
- current SDL resize handling mutates `screen` directly and bypasses the centralized screen-change flow.
- current SDL text input forwards UTF-8 bytes individually instead of decoding Unicode.
- current SDL mouse handling reads event-union fields too broadly.
- Windows `DllMain` currently resides in the Win32 event backend, a hidden platform/event coupling.
- Windows platform services use `mainHWnd`; SDL must expose its HWND without reactivating native window ownership.
- `updateScreenSettings()` has a legacy Windows small-screen density override that must not replace measured SDL HiDPI scale.

Add only discoveries that materially change remaining work. Move resolved history to the archive instead of growing this section indefinitely.

## Decision Log

- Decision: Preserve Native + Legacy as a supported Windows fallback after SDL + Skia becomes default.
  Rationale: compatibility, diagnostics, and rollback require a trustworthy old path.
  Date: 2026-08-26.

- Decision: Event backend follows windowing backend.
  Rationale: SDL must own both window and event pump; mixed ownership is unsafe.
  Date: 2026-08-26.

- Decision: Windows SDL + Skia uses `TC_GRAPHICS_SOFTWARE`.
  Rationale: GPU integration is explicitly outside this plan.
  Date: 2026-08-26.

- Decision: Support SDL + Legacy as a diagnostic combination when it requires no separate Windows renderer implementation.
  Rationale: it separates windowing/presentation defects from Skia defects.
  Date: 2026-08-26.

- Decision: Native + Skia is not required.
  Rationale: adapting Skia to native DIB presentation adds scope without helping the target architecture.
  Date: 2026-08-26.

- Decision: Change the Windows default only after functional SDL + Skia bring-up and fallback validation.
  Rationale: the old path must remain trustworthy throughout migration.
  Date: 2026-08-26.

- Decision: Make default promotion a separate logical commit before mandatory hardening.
  Rationale: this follows the requested execution sequence and makes policy change independently reviewable; final completion still requires all hardening milestones.
  Date: 2026-08-26.

- Decision: Logical/physical scale comes from SDL logical window size versus renderer output size, not reported hardware DPI.
  Rationale: this directly models the coordinate-to-framebuffer mapping TotalCross needs.
  Date: 2026-08-26.

- Decision: Prefer one canonical streaming-texture pixel contract.
  Rationale: window pixel format is not a stable Skia framebuffer contract.
  Date: 2026-08-26.

## Validation and Acceptance

The plan is complete only when:

1. Windows Native + Legacy remains explicitly selectable and buildable.
2. Windows SDL + Skia + Software is selectable and is the final default.
3. SDL + Legacy builds when retained as the diagnostic path.
4. WinCE remains Native + Legacy.
5. unsupported combinations fail clearly.
6. no TotalCross GPU backend or context was added.
7. SDL builds create/destroy their window through SDL.
8. Native builds create/destroy their window through Win32.
9. Windows SDL obtains HWND only for platform services.
10. no SDL build runs the Win32 event pump and no Native build runs SDL events.
11. SDL event code is shared, not Linux-owned.
12. mouse, drag, wheel, keys, Unicode text, minimize/restore, close, and resize/display events work through SDL.
13. Windows SDL + Skia uses Skia raster/software.
14. CPU framebuffer is physical-pixel sized.
15. SDL/Skia agree on channel order, alpha, width, pitch, and height.
16. no window-owned SDL surface is manually freed.
17. logical and physical SDL sizes are queried separately.
18. `screen.contentScale` is physical/logical scale.
19. `Settings.screenWidth/Height` remain logical and `Settings.screenDensity` follows monitor scale.
20. pointer coordinates align at non-100% scale.
21. different-DPI monitor transitions recreate required physical resources without changing logical behavior unexpectedly.
22. window/renderer lifetime is separate from framebuffer/texture/Skia binding lifetime.
23. resize/content-scale change does not destroy SDL window.
24. Skia releases its screen binding before old framebuffer memory is freed.
25. repeated resize/maximize/restore/DPI changes do not crash or retain stale dimensions.
26. final shutdown destroys SDL resources exactly once.
27. default Windows build uses SDL + Skia without overrides.
28. explicit Native + Legacy passes a focused smoke after the default change.
29. affected Linux/macOS SDL builds remain healthy.
30. unrelated local changes and generated dependency artifacts are not committed.

Use validation levels proportionally: Level 1 for prerequisite/config investigation, Level 2 for focused refactors, Level 3 for functional Windows backend/hardening families, and Level 4 only at final default-architecture closure.

Do not run the full matrix after every commit.

## Risks and Open Questions

The execution branch will differ from the snapshot used to create this plan because it will already contain the static SDL depot-tools migration. Treat the branch as authoritative and adapt variable names without reintroducing legacy SDL discovery.

The current Win32 implementation couples native window creation and DIB presentation in one file. Preserve it for Native + Legacy rather than forcing a complete internal decomposition before SDL can land.

Moving `DllMain` or Windows module initialization out of Native event code may affect startup ordering. Validate `hModuleTCVM` and native-library loading under both windowing modes.

Native libraries may receive backend-specific raw event pointers. Do not claim a cross-backend raw-event ABI that does not exist; audit actual desktop consumers before default promotion.

The Windows Skia prebuilt may link GPU-related system libraries internally. That is acceptable as a prebuilt property only; TotalCross runtime remains raster/software.

SDL presentation may internally use Direct3D/OpenGL. That does not violate this plan: the restriction is against a TotalCross/Skia GPU graphics backend. Do not force SDL's renderer to software merely for naming purity unless behavior requires it.

`SDL_PIXELFORMAT_ARGB8888` is a preferred candidate, not an unverified assumption. Prove channel order on supported Windows architectures.

Per-monitor DPI testing may require mixed-scale monitors. If unavailable, test multiple scale settings separately and record mixed-monitor movement as deferred runtime evidence.

The legacy 0.75 Windows small-screen density rule may have WinCE history. Narrow it rather than deleting it globally without evidence.

Do not expand this work into SDL3, a general renderer rewrite, GPU backend, font-engine replacement, or deprecation/removal of Native/Legacy.

## Idempotence and Recovery

Use separate build directories for backend configurations, for example:

    build-win-native-legacy
    build-win-sdl-legacy
    build-win-sdl-skia

Prefer repository-native Windows build scripts/generator options when available. Do not delete user build trees to clear stale cache; create a fresh task-specific directory.

SDL/Skia depot artifacts are generated dependencies. Do not edit or commit their local staged files.

Before every commit:

    git status --short -- <changed paths>
    git diff --stat
    git diff -- <changed paths>
    git diff --check -- <changed paths>

Run the focused copyright-header validator for changed/new first-party files as required by `AGENTS.md`. Stage only intended files.

Do not amend earlier commits. Fix later discoveries in new logical commits and update state.

If a shared SDL refactor breaks Linux/macOS, fix the shared contract instead of forking a Windows copy unless behavior is genuinely platform-specific.

If the SDL default fails late in hardening, use the preserved Native + Legacy path to isolate the regression; do not delete the new path or silently restore the default as a substitute for fixing it.

## Suggested Logical Commit Sequence

1. `build(cmake,windows): expose windowing and renderer backends`
2. `refactor(windowing): extract shared SDL surface state`
3. `refactor(graphics,windows): route software backend by windowing`
4. `refactor(event): select event handling by windowing`
5. `refactor(windowing,windows): bridge SDL window to Win32 services`
6. `feat(windowing,windows): enable SDL software presentation`
7. `feat(renderer,windows): enable Skia raster with SDL`
8. `test(windows): cover native and SDL backend matrix` when durable tests/checks are added
9. `build(windows): make SDL and Skia default`
10. `fix(windowing,windows): apply per-monitor HiDPI metrics`
11. `refactor(windowing): separate SDL window and backbuffer lifetime` when independently reviewable
12. `fix(renderer): rebind Skia after screen recreation`
13. `fix(renderer): use deterministic SDL Skia pixel format`
14. `fix(event): decode SDL text input as UTF-8`
15. `fix(event): harden SDL pointer and window events`
16. `fix(windowing,windows): align SDL window state behavior`
17. `docs(build,windows): document SDL Skia default` when documentation remains

Do not create commits merely to satisfy numbering. A logical, reviewable, preferably buildable history matters more than exact count.

Every non-trivial commit body should state motivation, ownership boundary changed, compatibility/fallback impact, and focused validation actually run.

## Outcomes & Retrospective

The Windows desktop default is now SDL + Skia + Software. Native + Legacy +
Software remains explicitly selectable with
`-DTC_WINDOWING_SDL=OFF -DTC_RENDERER_SKIA=OFF -DTC_GRAPHICS_SOFTWARE=ON`, and
WinCE remains fixed to that native configuration. SDL state and events are
shared under `src/nm/ui/sdl` and `src/event/sdl`; Windows SDL exposes its HWND
only as a platform-services bridge and never runs the Win32 event pump.

SDL + Legacy remains supported as a diagnostic configuration. SDL/Skia uses a
physical ARGB8888 CPU framebuffer, SDL streaming texture, and Skia BGRA raster
mapping. Resize/DPI events use logical SDL window size versus physical renderer
output size, and screen recreation keeps the SDL window/renderer while
rebinding the backbuffer and Skia screen canvas. UTF-8 committed text is
decoded to UTF-16 units, including surrogate pairs.

The final SDL input closure explicitly starts and stops SDL text input, keeps
`SDL_KEYDOWN` for special/non-text keys, and keeps `SDL_TEXTINPUT` for printable
Unicode. SDL special-key ownership now includes the desktop function-key
emulation mappings. Desktop startup parses reserved VM options throughout the
composite launcher command line but exposes only the filtered payload after
`/cmd` through `MainWindow.getCommandLine()`.

The follow-up `/scr` regression now consumes the complete spaced payload
`-2, -2, 480, 720`, preserving the exact application command line
`/admin W DEBUG` while retaining compact forms and near-match behavior.

The final desktop startup contract is implemented in
`src/nm/ui/WindowStartup.h` and `WindowStartup.c`. The parser stores command
line state in one desktop options object; shared environment parsing accepts
only the existing strictly positive `TC_WIDTH`/`TC_HEIGHT` values; and both
SDL and Native Windows pass local options plus native display metrics to the
same deterministic resolver. The old `defScr*`, `initialWindowState`,
`windowResolveStartupSize()`, and `tczSizeApplied` ownership paths are gone.

The default and diagnostic SDL configurations built successfully on macOS
ARM64, including the affected shared SDL paths. Windows selector checks showed
the intended default, fallback, diagnostic, and unsupported Native + Skia
behavior, but Windows x86/x64/ARM64 compilation and all runtime scenarios
(including mixed-DPI monitors and native-library event hooks) remain deferred
to a Windows-capable CI lane. No TotalCross GPU backend or context was added;
Skia's existing GLES code remains conditional on `TC_GRAPHICS_GLES`.

See `.agent/reports/windows-sdl-skia-raster-editorial.md` for the factual
handoff and the exact validation records.

At final completion record:

- final Windows backend defaults;
- exact flags for Native + Legacy fallback;
- files moved/created for shared SDL ownership;
- final event dispatch ownership;
- whether SDL + Legacy remains supported;
- Windows architectures compiled and runtime-tested;
- HiDPI scenarios actually exercised;
- lifecycle defects corrected;
- final canonical pixel format and Skia mapping;
- text/IME behavior delivered;
- native-library event-hook compatibility findings;
- affected Linux/macOS SDL validation;
- deferred validation and why;
- confirmation that no TotalCross GPU backend was introduced;
- remaining separate follow-up work, especially any future GPU renderer work.

Create/update `.agent/reports/windows-sdl-skia-raster-editorial.md` with the factual sections required by `.agent/PLANS.md`.

## Revision Note

2026-08-26: Initial Luna-oriented ExecPlan. It assumes static SDL2 consumption from totalcross-depot-tools is already complete, preserves Windows Native + Legacy as a fallback, adds SDL + Skia only through the software graphics path, makes event ownership follow windowing, changes the Windows default only after functional bring-up, and then requires HiDPI/lifecycle/pixel/input hardening before the migration is considered complete.
2026-08-26: Completed the implementation on the available host. Windows runtime and architecture validation remains explicitly deferred rather than inferred from macOS builds.
2026-08-31: Closed the remaining SDL keyboard, SDL special-key ownership,
modifier propagation, native Windows hotkey, and command-line exposure
findings in logical commits. Final focused checks, macOS SDL + Skia build, and
SDK distribution build passed; Windows interactive proof remains deferred to
CI.
2026-08-31: Added the SDL Windows message-hook bridge for native registered
hotkeys, deterministic full-line VM-option filtering with exact application
payload regression coverage, and the focused Ctrl+A/C/V keydown path. Final
macOS SDL + Skia + Software and SDK builds, fourteen-file header validation,
test-enabled startup syntax, focused contracts, and diff checks passed. The
local sample reached the SDL loop but could not complete input smoke because
runtime-state creation failed; Windows runtime proof remains deferred to CI.
2026-09-01: Completed the `/scr` spaced-payload follow-up in `16a0577a9`.
The focused desktop contracts, three-file header validation, cached diff check,
macOS `tcvm`/`Launcher` build, and Merge Flow `33547921138` passed for the
exact HEAD. The authoritative commit validator emitted only its existing body
line-length warning; the intentionally disabled Linux ARM32 cross job remained
skipped. No further CI-chasing documentation commit is required.
2026-09-01: Added the desktop startup sizing follow-up in `c2e433c01`,
`5f581a8a5`, `d5c5233cb`, `52f075840`, and `9572636b4`. The Window layer now
owns `/scr` > environment > TCZ > half-display sizing precedence for SDL and
Native Windows, including logical SDL sizes, the Native client-area contract,
all three TCZ size attributes, and SDL resizable/fullscreen behavior. The
focused suite passed 15 tests, the final macOS VM/Launcher build passed, and
exact-HEAD Merge Flow `33553035556` passed. Windows interactive smoke remains
deferred because this host has no Windows runtime.
2026-09-01: Corrected the sizing edge cases in `14fdce749` and added executable
table coverage in `6501430ad`. The parser now records whether `/scr` was
explicitly supplied, so `/scr ...,-1` cannot be overridden by environment or
TCZ sizing; SDL fullscreen uses full-display defaults only with no explicit
size source. The 16-test focused suite, header checks, macOS VM/Launcher
build, and exact-HEAD Merge Flow `33555763139` passed for
`6501430ad5f13ce5c696f98f785a2173366ff981`. Windows interactive smoke remains
deferred because this host has no Windows runtime.
2026-09-01: Completed the final shared desktop startup-policy refactor in
`7a8a11ffd` and added the WinCE compatibility guard in `e562e0501`. Focused
contracts, 13-file header validation, staged diff checks, and the macOS SDL +
Skia + Software `tcvm`/`Launcher` build passed. The implementation and closure
documentation commits retain overlong body lines because the requested
non-amended history was preserved; the final empty closure checkpoint passes
the message mirror. Windows architecture/runtime and interactive smoke remain
deferred because this host has no Windows toolchain or runtime.
