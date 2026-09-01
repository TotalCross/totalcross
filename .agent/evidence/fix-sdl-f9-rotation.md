<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Evidence: fix-sdl-f9-rotation

Append compact records here at logical-commit and milestone checkpoints.

- 2026-09-01 | `ece6ee5f7` | ownership slice | focused SDL contracts passed;
  headers and scoped diff check passed | Win32 resize moved to Window backend;
  graphics hook is graphics-only.
- 2026-09-01 | `8ea9cd486` | SDL rotation slice | 11 SDL desktop contract tests,
  headers, staged diff check, and commit-message validation passed | F9 now
  requests logical SDL resize; resize event owns HiDPI commit/recreation.
- 2026-09-01 | `40d127c46` | compile correction | focused contracts and
  touched-file headers passed; commit-message validation passed; final macOS
  Release `tcvm`/`Launcher` build passed | SDL setter is void; wrapper verifies
  the resulting logical size.
- 2026-09-01 | `40d127c46` | milestone closure | `ctest --test-dir
  build-macos-sdl-contracts -N` passed with zero tests | native smoke recorded
  as `SMOKE_FIXTURE_UNAVAILABLE`; Windows and other platforms not built.
- 2026-09-01 | `500119506` | final correctness | 11 focused contracts,
  three-file header validation, cached diff check, and commit-message
  validation passed; macOS Release `tcvm`/`Launcher` build passed | native
  Windows values are snapshotted before resize; SDL gate uses queried logical
  size and swaps minimums only after resize success.
- 2026-09-01 | `33539502398` | remote CI inspection | full enabled matrix passed
  for remote SHA `6971e996a` | included Windows SDL and Windows Native+Legacy;
  exact final SHA `500119506` has no run because no push was permitted.
- 2026-09-01 | `b27b7df91` | final SDL confirmation | 11 focused contracts,
  four-file header validation, cached diff check, and commit-message validation
  passed; macOS Release `tcvm`/`Launcher` build passed | generic SDL resize no
  longer reads back requested size; pending orientation resolves on SIZE_CHANGED.
- 2026-09-01 | `33542462499` | exact-HEAD closure | full enabled Merge Flow
  matrix passed for `036e8ff9e` | Windows SDL and Windows Native+Legacy passed;
  Linux arm32 cross remained intentionally skipped.
