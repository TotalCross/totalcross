<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# State: fix-sdl-f9-rotation

- Active milestone: implementation and validation complete.
- Last functional commit: `b27b7df91 fix(windowing,sdl): confirm rotation
  from resize event`.
- Active paths: Window backend headers and `Window.c`, graphics screen-change
  hooks, native Windows F9 event paths, SDL event/tcsdl paths, and focused SDL
  contract tests.
- Next action: hand off the completed milestone; no further source work is
  required.
- Focused validation: `python3 scripts/test-sdl-desktop-contracts.py` passed
  11 tests; scoped `git diff --check` and copyright validation passed for the
  implementation, tests, and plan files; final macOS Release `tcvm` and
  `Launcher` build passed after the final SDL confirmation fix.
- Commit-message note: `ece6ee5f7` was intentionally not amended after the
  repository mirror check found an overlong body line; future commits use
  wrapped bodies.
- Deferred validation: Windows and other platform builds; final macOS VM/
  Launcher build completed; native smoke is `SMOKE_FIXTURE_UNAVAILABLE`
  because `ctest -N` reports zero tests and no runnable desktop fixture exists.
- CI: remote `Merge flow` run `33542462499` passed its full enabled matrix for
  `036e8ff9e`, including Windows SDL and Windows Native+Legacy. The
  `linux-arm32v7-cross` job was intentionally skipped by workflow policy.
- Unrelated local files intentionally left alone: all pre-existing modified or
  untracked `.agent` files, `.vscode/`, generated plist/tcz files, dependency
  checkout contents, and any other paths outside the active set.
- Blockers: none.
- Resume command: `sed -n '1,180p' .agent/state/fix-sdl-f9-rotation.md`
