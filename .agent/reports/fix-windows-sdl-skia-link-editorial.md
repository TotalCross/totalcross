<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Windows SDL + Skia link fix — factual handoff

## Delivered

- Restored centralized MSVC static CRT selection in `TotalCrossVM/CMakeLists.txt`:
  `/MT` for Release-like configurations and `/MTd` for Debug, retaining the
  CMake 3.11-compatible flag fallback.
- Consumed allocator-safe SDL2 `sdl2-2.32.8-r2` and pinned depot-tools to the
  immutable commit `0ebff1d7202fab6e61758344219f60fa757fe6ce`.
- Rebuilt or republished the remaining Windows static dependencies as `/MT`:
  AxTLS, Minizip-ng, qrcodegen, and the private SQLite SEE override.
- Added a CI Windows fallback job for Native + Legacy + software and explicit
  assertions for generated Visual Studio CRT settings.
- Preserved TotalCross ownership of `dlmalloc`; no linker suppression or
  allocator-semantic change was introduced.

## Measurements and validation

- CRT correction commit: `4fdb6b044`.
- Consumer dependency and CI commits: `044515ca0`, `4efb13e0d`, `cae516c90`,
  `ee697c09d`, `a9c153c53`, and `e2fe6c2a4`.
- Private SQLite SEE release: `sqlite3-see-3.32.3-r1`; its full build matrix
  passed in run `33038753491`.
- Consumer run `33040181465` passed all jobs. Windows job `98411983584`
  passed the default Win32 SDL + Skia + software build and produced
  `tcvm.dll`; generated projects contained `MultiThreaded` and
  `MultiThreadedDebug`.
- Windows job `98411983588` passed Native + Legacy + software with the exact
  fallback flags and produced `Release/tcvm.dll`.
- The final CI logs contain no `LNK2005`, `LNK2038`, `LNK4098`, or `LNK1169`.
- Local Visual Studio validation was skipped as requested; the authorized CI
  runners performed the generator, configuration, runtime, and link checks.

## Supported platforms and limitations

The requested Windows default and fallback configurations are validated. The
same consumer CI run also passed Android, iOS, macOS ARM64, Linux x86_64,
Linux ARM64, Linux ARM32v7, SDK, and source preparation jobs.

No known limitations remain for this plan. The consumer branch still requires
normal review and merge.

## Human review points

Review the focused consumer commits and the depot-tools/private SQLite SEE
release provenance before merging. The final functional acceptance is backed
by CI run `33040181465`.
