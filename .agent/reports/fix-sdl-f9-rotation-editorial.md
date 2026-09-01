<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Editorial handoff: fix SDL F9 rotation

## Delivered

SDL F9 now swaps the logical SDL window width and height through
`TCSDL_SetWindowSize()`. The resulting `SDL_WINDOWEVENT_SIZE_CHANGED` is the
single path that queries logical and renderer-output metrics, applies the
screen configuration, commits settings, and recreates the physical framebuffer
resources. A second F9 is ignored while that confirmation is pending. Win32
full-window rotation remains border-aware and is owned by the Window backend.
Graphics only reacts to committed configuration changes.

## Proof

- Commits: `ece6ee5f7`, `8ea9cd486`, `40d127c46`, `500119506`, final SDL
  confirmation fix `b27b7df91`, and pending-request fix `c97f7dc78`.
- `python3 scripts/test-sdl-desktop-contracts.py`: 11 tests passed.
- Focused copyright validation and staged/working-tree diff checks passed.
- macOS Release `tcvm` and `Launcher` build passed; log:
  `/tmp/fix-sdl-f9-rotation-macos-build-pending-guard.log`.
- Native smoke inventory passed but reported zero configured tests; native
  smoke is `SMOKE_FIXTURE_UNAVAILABLE`.
- Remote `Merge flow` run [33545202074](https://github.com/TotalCross/totalcross/actions/runs/33545202074)
  passed the full enabled matrix for exact HEAD `c97f7dc78`, including Windows
  SDL and Windows Native+Legacy. The Linux arm32 cross job was intentionally
  skipped by workflow policy.

## Limitations

Windows, Linux, Android, iOS, and other platform builds were not run locally,
per the task restriction; they were covered by the exact-HEAD remote matrix.
The first ownership commit was not amended after its body-line check found an
overlong line; subsequent commit messages pass the local repository validator.
