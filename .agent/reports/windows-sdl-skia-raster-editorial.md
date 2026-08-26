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
- Passed: focused copyright-header validation and `git diff --check`.
- Passed: source audit found no `SDL_GetWindowSurface`, `SDL_FreeSurface`,
  `SDL_GetWindowPixelFormat`, `SDL_PollEvent(NULL)`, or obsolete resize API.

## Limitations and deferred proof

This macOS host has no Windows compiler/runtime and no staged Windows SDL
artifacts. Windows x86/x64/ARM64 configure/build/link, Native + Legacy runtime
smoke, SDL + Legacy runtime smoke, SDL + Skia rendering/input smoke, mixed-DPI
monitor movement, and native-library event-hook compatibility require the
Windows CI lane. The available macOS builds validate the shared SDL source
contract, not Windows runtime behavior.
