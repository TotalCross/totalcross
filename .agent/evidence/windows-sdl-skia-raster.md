<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Windows SDL + Skia raster evidence

- 2026-09-01 | `16a0577a9` | `/scr` spaced-payload follow-up | 11 focused
  desktop contract tests, three-file header validation, authoritative commit
  validation, and `git diff --check --cached` passed. The authoritative
  validator emitted one non-blocking body line-length warning.
- 2026-09-01 | `16a0577a9` | final macOS validation | SDL + Skia + Software
  Release `tcvm` and `Launcher` build passed | log:
  `/tmp/totalcross-sdl-desktop-final-macos-build-scr-payload.log`.
- 2026-09-01 | `33547921138` | exact-HEAD Merge Flow | passed for
  `16a0577a9fa2812d4765a67e35fdde9410fe55e1` | Android, iOS, macOS, Windows,
  Linux, and SDK jobs passed; the intentionally disabled Linux ARM32 cross job
  was skipped.
- 2026-09-01 | `c2e433c01` | startup sizing implementation | shared Window-layer
  precedence, SDL TCZ attributes, Native client-area sizing, half-display
  defaults, and SDL fullscreen/resizable gating committed. Focused source and
  header checks passed.
- 2026-09-01 | `5f581a8a5`, `d5c5233cb`, `52f075840`, `9572636b4` | regression
  contracts and linkage correction | focused SDL desktop suite passed 15 tests;
  staged diff checks passed; latest commit-message validation passed. Two
  earlier commit bodies retain non-blocking line-length warnings.
- 2026-09-01 | `9572636b4` | final macOS validation | SDL + Skia + Software
  Release `tcvm` and `Launcher` build passed | log:
  `/tmp/desktop-window-startup-sizing-final-macos-build-rerun.log`.
- 2026-09-01 | `33553035556` | exact-HEAD Merge Flow | passed for
  `9572636b469b0407fd9d6cd688a3998b8ab71303` | Android, iOS, macOS, Windows,
  Linux, and SDK jobs passed; the intentionally disabled Linux ARM32 cross job
  was skipped.
- 2026-09-01 | `14fdce749`, `6501430ad` | sizing correctness follow-up |
  explicit `/scr` tracking, fullscreen default correction, and executable
  resolver-table coverage committed. Focused contracts passed 16 tests;
  headers and commit messages validated; cached diff checks passed.
- 2026-09-01 | `6501430ad5f13ce5c696f98f785a2173366ff981` | final macOS validation |
  SDL + Skia + Software Release `tcvm` and `Launcher` build passed | log:
  `/tmp/desktop-window-startup-sizing-correctness-macos-build.log`.
- 2026-09-01 | `33555763139` | exact-HEAD Merge Flow | passed for
  `6501430ad5f13ce5c696f98f785a2173366ff981` | Android, iOS, macOS, Windows,
  Linux, and SDK jobs passed; the intentionally disabled Linux ARM32 cross job
  was skipped.
- 2026-09-01 | `204306bdf` | TCZ centering correctness | resolver now centers
  only when a TCZ dimension remains in use after environment precedence;
  executable coverage includes environment width/height overrides, `/scr`
  partial dimensions, all TCZ sizes, fullscreen defaults, and resizable states.
- 2026-09-01 | `204306bdf` | local validation | 16 focused desktop contract tests,
  source/test copyright headers, staged diff checks, the opt-in native
  `window_startup_native_test` build and execution, and the macOS SDL + Skia +
  Software Release `tcvm`/`Launcher` build passed. Broad native-suite
  enablement was deferred after unrelated existing
  `objectmemorymanager_test.h` errors.
- 2026-09-01 | `33563499168` | exact-HEAD Merge Flow | passed for
  `204306bdf99556e75f098b9d2796292e9a6d8710` | Android, iOS, macOS, Windows,
  Linux, and SDK jobs passed; the intentionally disabled Linux ARM32 cross job
  was skipped.
