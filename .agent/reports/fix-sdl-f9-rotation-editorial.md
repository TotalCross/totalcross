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
resources. Win32 full-window rotation remains border-aware and is owned by the
Window backend. Graphics only reacts to committed configuration changes.

## Proof

- Commits: `ece6ee5f7`, `8ea9cd486`, `40d127c46`, and final correctness fix
  `500119506`.
- `python3 scripts/test-sdl-desktop-contracts.py`: 11 tests passed.
- Focused copyright validation and staged/working-tree diff checks passed.
- macOS Release `tcvm` and `Launcher` build passed; log:
  `/tmp/fix-sdl-f9-rotation-macos-build-final2.log`.
- Native smoke inventory passed but reported zero configured tests; native
  smoke is `SMOKE_FIXTURE_UNAVAILABLE`.
- Remote `Merge flow` run [33539502398](https://github.com/TotalCross/totalcross/actions/runs/33539502398)
  passed the full enabled matrix for SHA `6971e996a`, including Windows SDL
  and Windows Native+Legacy. Exact final SHA `500119506` has no CI run because
  it was not pushed.

## Limitations

Windows, Linux, Android, iOS, and other platform builds were not run locally,
per the task restriction. No push was performed. The first ownership commit was not
amended after its body-line check found an overlong line; subsequent commit
messages pass the local repository validator.
