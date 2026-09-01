<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Windows SDL + Skia raster — factual handoff

## Delivered

- Windows desktop defaults to `TC_WINDOWING_SDL=ON`,
  `TC_RENDERER_SKIA=ON`, and `TC_GRAPHICS_SOFTWARE=ON`.
- Native + Legacy + Software remains selectable with:
  `-DTC_WINDOWING_SDL=OFF -DTC_RENDERER_SKIA=OFF -DTC_GRAPHICS_SOFTWARE=ON`.
- SDL window state and events are shared in `src/nm/ui/sdl` and
  `src/event/sdl`; Linux DirectFB and Native Windows paths remain separate.
- Windows SDL obtains `mainHWnd` through SDL SysWM for platform services only.
- SDL uses a physical ARGB8888 CPU framebuffer, streaming texture, and Skia's
  BGRA raster interpretation. Window-owned SDL surfaces are not used or freed.
- SDL logical/physical metric queries drive content scale and screen recreation;
  SDL window/renderer lifetime is separate from backbuffer and Skia binding
  lifetime.
- SDL mouse dispatch uses the correct event union members, and committed UTF-8
  text is decoded into TotalCross UTF-16 units, including surrogate pairs.
- SDL event initialization explicitly starts text input and teardown stops it;
  `SDL_KEYDOWN` remains the special/non-text path while `SDL_TEXTINPUT` remains
  the printable Unicode path.
- SDL initialization activates text input again after creating the desktop
  window because event initialization precedes graphics/window creation.
- SDL special-key translation is selected before OS-specific branches and
  includes navigation/editing keys, Shift/Ctrl/Alt, screen change, and the
  desktop F-key emulation mappings.
- Startup parses reserved VM options across the full composite launcher line,
  including after `/cmd`, while `MainWindow.getCommandLine()` receives only
  the filtered application payload and never the `/cmd` separator.
- `/scr` values with internal spaces are consumed through the exact `%n`
  endpoint, so `App.tcz /cmd /admin W DEBUG /scr -2, -2, 480, 720` produces
  `defScrX=-2`, `defScrY=-2`, `defScrW=480`, `defScrH=720`, and the exact
  application command line `/admin W DEBUG`.
- The non-desktop startup path keeps its historical exact `" /cmd "` lookup;
  only the desktop path uses the near-match-aware parser and compaction logic.
- SDL modifiers remain raw at the SDL boundary and are translated once by the
  shared event layer; Windows native hotkey registration remains on Win32 VK
  values independently of SDL event translation. SDL’s supported Windows
  message hook forwards `WM_HOTKEY` events back through the portable special-
  key path, and Ctrl+A/C/P/V/X/Space use the SDL keydown path without changing
  ordinary text-input ownership.
- No TotalCross GPU backend or context was introduced.

## Desktop startup sizing follow-up

- Delivered: `Window.c` owns the shared size precedence `/scr` > valid
  environment dimensions > TCZ size attributes > half-display defaults.
- Delivered: SDL consumes logical display bounds for its default, preserves
  physical renderer output metrics for HiDPI, supports all three TCZ sizes, and
  creates a resizable window only for non-fullscreen startup.
- Delivered: Native Windows uses the same logical client-area result and adds
  borders/caption only for the outer Win32 window. Its existing work-area cap
  for the 600x800 attribute remains in force.
- Delivered: explicit `/scr` presence is tracked independently of parsed
  dimensions, so `/scr ...,-1` retains precedence and resolves missing
  dimensions to half-display values. SDL fullscreen uses full-display defaults
  only when no `/scr`, environment, or TCZ size source is present.
- Delivered: focused source contracts and executable resolver-table tests cover
  precedence, `/scr -1`, all TCZ sizes, windowed/fullscreen defaults, SDL
  state flags, Native client geometry, SDL attribute wiring, and the shared
  C/C++ linkage boundary.

## Final desktop startup contract

- Delivered: `WindowStartup.h/.c` centralizes desktop startup options,
  environment dimensions, deterministic size/position resolution, and initial
  fullscreen/maximized/resizable state.
- Delivered: SDL and Native Windows query their own display/work-area metrics,
  pass local options to the shared resolver, and translate its one resolved
  configuration into native window APIs. SDL no longer owns `TC_WIDTH` or
  `TC_HEIGHT`, and Native Win32 keeps resolved dimensions as client-area
  dimensions before adding its outer frame.
- Delivered: the old desktop globals, long `windowResolveStartupSize()` API,
  and `tczSizeApplied` flag were removed; WinCE remains on its existing native
  startup path.

## Validation

- Passed: CMake configure for SDL + Skia + Software on macOS ARM64.
- Passed: `tcvm` and full default target build for SDL + Skia + Software on
  macOS ARM64.
- Passed: CMake configure and full target build for SDL + Legacy + Software on
  macOS ARM64.
- Passed: Windows selector checks for default, fallback, diagnostic, and
  unsupported Native + Skia configurations. The checks reached the intended
  backend summaries; dependency/build completion was unavailable locally.
- Passed: focused copyright-header validation for all fourteen affected files
  and `git diff --check` for the completed commit range and working tree.
- Passed: `python3 scripts/test-sdl-desktop-contracts.py` with seven focused SDL
  keyboard, modifier, special-key, Windows hotkey, backend-ownership, and
  command-line contract tests.
- Passed: the existing native test-suite registry contains a command-line
  regression covering reserved options before, between, and after `/cmd`,
  exact compacted TCZ/application strings, and near-match preservation.
- Passed: the test-enabled `startup.c` syntax check for the existing native
  regression.
- Passed: final macOS SDL + Skia Release configure/build and
  `TotalCrossSDK/gradlew-agent dist`.
- Passed: final macOS SDL + Skia + Software Release rebuild after the complete
  keyboard, hotkey, and command-line closure; log:
  `/tmp/totalcross-sdl-desktop-final-macos-build-followup2.log`.
- Passed: final `TotalCrossSDK/gradlew-agent dist`; log:
  `/tmp/totalcross-sdk-final-dist-followup2.log`.
- Passed: the final macOS SDL + Skia + Software `tcvm`/`Launcher` build after
  the `/scr` payload fix; log:
  `/tmp/totalcross-sdl-desktop-final-macos-build-scr-payload.log`.
- Passed: exact-HEAD Merge Flow run [33547921138](https://github.com/TotalCross/totalcross/actions/runs/33547921138)
  for commit `16a0577a9`; all enabled jobs passed and the intentionally
  disabled Linux ARM32 cross job was skipped. The commit validator reported
  only a non-blocking body line-length warning.
- Passed: CI run [33450370159](https://github.com/TotalCross/totalcross/actions/runs/33450370159)
  at `51dde0f43` passed Android and iOS compilation, plus macOS, Windows,
  Linux, and SDK jobs. This confirms the non-desktop startup fix in the pushed
  branch.
- Attempted: local macOS sample launch. It reached the SDL application loop,
  but the available sample setup could not complete interactive input smoke
  because runtime-state creation failed in the temporary launch directory.
- Passed: source audit found no `SDL_GetWindowSurface`, `SDL_FreeSurface`,
  `SDL_GetWindowPixelFormat`, `SDL_PollEvent(NULL)`, or obsolete resize API.
- Passed: `python3 scripts/test-sdl-desktop-contracts.py` with 15 focused
  startup and existing SDL desktop contract tests.
- Passed: focused copyright-header validation for the seven changed source and
  test files, plus `git diff --check --cached` for each logical commit.
- Passed: final macOS SDL + Skia + Software Release `tcvm`/`Launcher` build;
  log: `/tmp/desktop-window-startup-sizing-final-macos-build-rerun.log`.
- Passed: exact-HEAD [Merge Flow run 33553035556](https://github.com/TotalCross/totalcross/actions/runs/33553035556)
  for `9572636b4`; all enabled jobs passed and the intentional Linux ARM32
  cross job was skipped.
- Passed: `python3 scripts/test-sdl-desktop-contracts.py` with 16 focused
  startup and SDL desktop contract tests, including explicit `/scr` tracking
  and shared resolver assertions.
- Passed: executable native test-suite registration for the resolver table and
  `/scr` marker regression; focused headers, commit messages, and cached diff
  checks passed for both final logical commits.
- Passed: final macOS SDL + Skia + Software Release `tcvm`/`Launcher` build;
  log: `/tmp/desktop-window-startup-sizing-correctness-macos-build.log`.
- Passed: exact-HEAD [Merge Flow run 33555763139](https://github.com/TotalCross/totalcross/actions/runs/33555763139)
  for `6501430ad5f13ce5c696f98f785a2173366ff981`; all enabled jobs passed and
  the intentionally disabled Linux ARM32 cross job was skipped.
- Passed: `python3 scripts/test-sdl-desktop-contracts.py` with 16 focused
  startup and SDL desktop contract tests after the final resolver migration.
- Passed: macOS SDL + Skia + Software Release `tcvm`/`Launcher` build after
  the final resolver migration; log:
  `/tmp/desktop-window-startup-refactor-macos-build.log`.
- Passed: focused header validation for 13 affected files and cached diff
  checks for commits `7a8a11ffd` and `e562e0501`.

## Limitations and deferred proof

This macOS host has no Windows compiler/runtime and no staged Windows SDL
artifacts. Windows x86/x64/ARM64 configure/build/link, Native + Legacy runtime
smoke, SDL + Legacy runtime smoke, SDL + Skia rendering/input smoke, mixed-DPI
monitor movement, and native-library event-hook compatibility require the
Windows CI lane. The available macOS builds validate the shared SDL source
contract and macOS behavior, not Windows runtime behavior. Manual keyboard
smoke for normal text, modifiers, arrows, Enter/Tab/Backspace/Escape,
representative function keys, and Ctrl+A/C/V is therefore deferred with the
Windows runtime proof. No temporary diagnostics remain in the tree. No
TotalCross GPU backend or context was added; Skia’s existing GLES code remains
conditional on `TC_GRAPHICS_GLES`.

The host still has no Windows toolchain/runtime for architecture builds or
interactive smoke. The first refactor commit retains a commit-message mirror
warning caused by literal escaped newlines because the requested non-amended
history was preserved; the compatibility follow-up commit passes the mirror.
