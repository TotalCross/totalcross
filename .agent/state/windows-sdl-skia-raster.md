<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Windows SDL + Skia migration state

- Plan key: `windows-sdl-skia-raster`
- Active milestone: 1 — backend selection contract
- Last commit: `6d03ffd59 docs(build): document depot SDL2 consumption`
- Active paths: `TotalCrossVM/cmake/TCGraphics.cmake`, then the SDL/event,
  graphics, windowing, and Skia sources named by the active milestone.
- Starting prerequisite: confirmed the pinned depot checkout is at
  `118ff8925b165c79de87cd2d69f562b570b1ebd5`, `deps.yml` pins SDL
  `sdl2-2.32.8`, and the CMake path consumes `SDL2::SDL2` and `Skia::Skia`.
- Baseline limitation: this macOS host has no Windows compiler/runtime and no
  staged Windows SDL artifacts, so Native + Legacy runtime/binary baseline is
  deferred to a Windows-capable lane. Existing macOS build caches are kept
  untouched.
- Focused validation: prerequisite inspection completed; implementation
  contract validation is pending for the current `TCGraphics.cmake` change.
- Deferred validation: Windows x86/x64/ARM64 configure/build and runtime smoke;
  perform when a Windows-capable lane is available.
- Blockers: none for source implementation; Windows runtime proof is deferred.
- Deliberate out-of-scope local files: existing untracked dependency/generated
  trees and helper scripts outside this plan.
- Next action: validate the backend selection contract, then commit the
  CMake-only milestone and advance to shared SDL surface state.
- Resume command: `sed -n '1,220p' .agent/state/windows-sdl-skia-raster.md`
