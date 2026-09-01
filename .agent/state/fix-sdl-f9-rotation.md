<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# State: fix-sdl-f9-rotation

- Active milestone: implementation complete; exact-HEAD CI pending.
- Last functional commit: `500119506 fix(event): snapshot rotation
  dimensions`.
- Active paths: Window backend headers and `Window.c`, graphics screen-change
  hooks, native Windows F9 event paths, SDL event/tcsdl paths, and focused SDL
  contract tests.
- Next action: obtain CI for exact final SHA `500119506` through the remote
  workflow; do not push from this task.
- Focused validation: `python3 scripts/test-sdl-desktop-contracts.py` passed
  11 tests; scoped `git diff --check` and copyright validation passed for the
  implementation, tests, and plan files; final macOS Release `tcvm` and
  `Launcher` build passed after the final correctness fix.
- Commit-message note: `ece6ee5f7` was intentionally not amended after the
  repository mirror check found an overlong body line; future commits use
  wrapped bodies.
- Deferred validation: Windows and other platform builds; final macOS VM/
  Launcher build completed; native smoke is `SMOKE_FIXTURE_UNAVAILABLE`
  because `ctest -N` reports zero tests and no runnable desktop fixture exists.
- CI: remote `Merge flow` run `33539502398` passed its full enabled matrix for
  `6971e996a`, including Windows SDL and Windows Native+Legacy. No run exists
  for exact final SHA `500119506`; the branch was not pushed as instructed.
- Unrelated local files intentionally left alone: all pre-existing modified or
  untracked `.agent` files, `.vscode/`, generated plist/tcz files, dependency
  checkout contents, and any other paths outside the active set.
- Blockers: exact-HEAD CI cannot be created without publishing the final SHA;
  preserve the no-push instruction and leave this as an external gate.
- Resume command: `sed -n '1,180p' .agent/state/fix-sdl-f9-rotation.md`
