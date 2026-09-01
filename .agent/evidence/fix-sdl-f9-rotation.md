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
