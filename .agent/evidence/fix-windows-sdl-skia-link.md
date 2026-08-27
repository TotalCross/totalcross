<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Windows SDL + Skia link-fix evidence

- Baseline: TotalCross HEAD `6625e1c965e7016f4565d0bf28095c7bcb38507c`; depot
  pin `118ff8925b165c79de87cd2d69f562b570b1ebd5`; no existing MSVC runtime
  policy found in `TotalCrossVM/CMakeLists.txt` or `TotalCrossVM/cmake`.
- CRT correction: commit `4fdb6b044`; `git diff --check` and focused copyright
  validation passed for `TotalCrossVM/CMakeLists.txt`.
- Depot release: `sdl2-2.32.8-r2` is an annotated tag peeling to
  `e9ce760e3a4ba23f3af926a0c0260f4b175009a8`. Clean source verification passed:
  `sdl2/CMakeLists.txt` contains Windows-only `SDL_LIBC=ON`, and
  `sdl2/manifest.yml` names the same release.
- Published artifacts: release assets include `sdl2-windows-x86.tar.gz`,
  `sdl2-windows-x64.tar.gz`, and `sdl2-windows-arm64.tar.gz`. The downloaded
  x86 artifact manifest reports static `SDL2-static.lib`, `msvc_runtime=MultiThreaded`,
  and `sdl2main=OFF`; LLVM symbol inspection found no SDL `dlmalloc` family
  definitions.
- Pin: `TotalCrossVM/deps/totalcross-depot-tools.ref` now contains only the
  verified 40-hex peeled commit. A clean consumer bootstrap resolved exactly
  `e9ce760e3a4ba23f3af926a0c0260f4b175009a8`. Pin commit: `044515ca0`.
- Runtime probe: a temporary Windows-targeted Ninja Multi-Config CMake probe
  using `clang-cl` configured and built successfully; its Debug compile command
  used `-MTd` and its Release compile command used `-MT`. This is supplemental
  evidence for the policy, not a substitute for the Visual Studio generator.
- CI run `33036772677` / Windows job `98401396846` reached the Visual Studio
  Win32 configure and compile stages. It selected SDL + Skia + software and
  did not emit the original SDL `LNK2005` or Skia `LNK2038` failures.
- The same CI link failed with `LNK4098` and unresolved dynamic-CRT symbols
  from `minizip.lib`, `sqlite3-see.obj`, and `axtls.lib`, including
  `__imp___localtime64_s`, `__imp___mktime64`, `__imp__strncat`,
  `__imp__strdup`, `__imp__rand_s`, `__imp___ctime64`, `__imp__open`, and
  `__imp___ftime64`. The failure ended with `LNK1120: 10 unresolved externals`.
- COFF directive inspection of the prepared CI artifact confirms `MSVCRT`
  directives in the published Windows x86 `axtls` and `minizip-ng` libraries.
  The prepared SQLite marker resolves to the private `TotalCross/totalcross-
  sqlite3-see-build` override and its `sqlite3-see.obj` appears in the same
  dynamic-CRT failure. The default public `sqlite3-3.32.3-r2` artifact was
  separately inspected and contains `LIBCMT`, so the override is material.
- This is a new external dependency-release blocker after the two planned
  fixes: SDL2 `sdl2-2.32.8-r2` is allocator-safe and Skia/SDL/VM are aligned,
  but the complete Windows static dependency closure is not uniformly `/MT`.

## Final execution evidence

- Depot-tools published static-runtime releases for AxTLS
  `axtls-2.1.5-tc.1-r2`, Minizip-ng `minizip-ng-4.2.2-r3`, and qrcodegen
  `qrcodegen-20250123-r2`; the consumer pin now resolves to
  `0ebff1d7202fab6e61758344219f60fa757fe6ce`.
- The private SQLite SEE build was corrected and its full CI matrix passed in
  run `33038753491`; release `sqlite3-see-3.32.3-r1` contains all 11 expected
  platform artifacts, including the three Windows archives.
- Consumer CI run `33040181465` passed all jobs. Its Windows job
  `98411983584` passed the default Win32 SDL + Skia + software build and its
  generated-project assertions for `MultiThreaded` and `MultiThreadedDebug`.
- The same run's Windows Native + Legacy job `98411983588` passed with
  `TC_WINDOWING_NATIVE=ON`, `TC_WINDOWING_SDL=OFF`,
  `TC_RENDERER_LEGACY=ON`, `TC_RENDERER_SKIA=OFF`, and software graphics;
  it also produced `Release/tcvm.dll` and passed the same CRT assertions.
- The final CI logs contain no `LNK2005`, `LNK2038`, `LNK4098`, or `LNK1169`.
