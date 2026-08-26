<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Windows SDL + Skia migration state

- Plan key: `windows-sdl-skia-raster`
- Active milestone: 14 — final matrix and regression closure
- Last implementation commit: `eb19c863e fix(windowing,renderer): harden SDL Skia presentation`
- Active paths: `TotalCrossVM/src/event/sdl/event_c.h`,
  `TotalCrossVM/src/init/tcsdl.cpp`, `TotalCrossVM/src/nm/ui/skia`,
  `TotalCrossVM/cmake/TCGraphics.cmake`, and the final documentation/report.
- Starting prerequisite: confirmed the pinned depot checkout is at
  `118ff8925b165c79de87cd2d69f562b570b1ebd5`, `deps.yml` pins SDL
  `sdl2-2.32.8`, and the CMake path consumes `SDL2::SDL2` and `Skia::Skia`.
- Baseline limitation: this macOS host has no Windows compiler/runtime and no
  staged Windows SDL artifacts, so Native + Legacy runtime/binary baseline is
  deferred to a Windows-capable lane. Existing macOS build caches are kept
  untouched.
- Focused validation: SDL + Skia and SDL + Legacy configure/full builds passed
  on macOS ARM64; Windows selector summaries and unsupported Native + Skia
  rejection passed; ownership and forbidden-path audits passed; headers and
  diff checks passed.
- Deferred validation: Windows x86/x64/ARM64 configure/build/link and runtime
  smoke for both default and fallback, including mixed-DPI monitors and
  native-library event hooks, because this host has no Windows toolchain,
  runtime, or staged Windows artifacts.
- Blockers: none for source implementation; Windows runtime proof is deferred
  to the Windows CI lane.
- Deliberate out-of-scope local files: existing untracked dependency/generated
  trees and helper scripts outside this plan.
- Next action: finalize the documentation commit, then leave the state/report
  reconciled with the final commit and deferred Windows proof.
- Resume command: `sed -n '1,220p' .agent/state/windows-sdl-skia-raster.md`
