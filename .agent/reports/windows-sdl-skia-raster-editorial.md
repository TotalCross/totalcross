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
- SDL modifiers remain raw at the SDL boundary and are translated once by the
  shared event layer; Windows native hotkey registration remains on Win32 VK
  values independently of SDL event translation. SDL’s supported Windows
  message hook forwards `WM_HOTKEY` events back through the portable special-
  key path, and Ctrl+A/C/V use the SDL keydown path without changing ordinary
  text-input ownership.
- No TotalCross GPU backend or context was introduced.

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
- Passed: `python3 scripts/test-sdl-desktop-contracts.py` with six focused SDL
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
- Attempted: local macOS sample launch. It reached the SDL application loop,
  but the available sample setup could not complete interactive input smoke
  because runtime-state creation failed in the temporary launch directory.
- Passed: source audit found no `SDL_GetWindowSurface`, `SDL_FreeSurface`,
  `SDL_GetWindowPixelFormat`, `SDL_PollEvent(NULL)`, or obsolete resize API.

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
