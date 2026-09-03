<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# State — fix-skia-font-pipeline-02

- Active milestone/slice: Plan 2 complete; registry hardening, bold
  propagation, SDK documentation, and final validation are finished.
- Plan 1 handoff: verified. Skia `fontInit()` does not initialize PalmFont
  state; Skia `tufF_fontCreate()` does not call `loadFontFile()`; Skia leaves
  `hv_UserFont` null; custom TTF names survive success; missing custom names
  select `TCFont`.
- Logical commits: `62d4afbba` (`fix(skia): harden typeface registry`),
  `bc6085176` (`fix(font): honor bold style with skia`), `d712b2df4`
  (`test(skia): verify bold pixel state`), and `374d55370`
  (`docs(font): clarify renderer font sources`). Plan 1 commits remain
  `bfac1883c` and `7f4f11d2a`.
- Modified paths: Skia registry/interface/primitives, native font metrics and
  text call sites, the existing Skia fixture, `Font.java`, this state file,
  and the editorial report. Unrelated dirty worktree content remains out of
  scope.
- Focused validation: copyright headers and diff checks passed. The direct
  macOS Skia fixture passed invalid-TTF rejection, stable repeated/cache
  indices, 40 unique typefaces, bold rendering, and plain-state restoration.
- Final gates: `./gradlew-agent clean dist`, `ninja -C build-window-startup-macos`,
  `window_startup_native_test`, and the direct Skia fixture all passed.
  Logs: `/tmp/fix-skia-font-pipeline-02-sdk-dist.log`,
  `/tmp/fix-skia-font-pipeline-02-final-native-build.log`,
  `/tmp/fix-skia-font-pipeline-02-final-startup-smoke.log`, and
  `/tmp/fix-skia-font-pipeline-02-final-skia-fixture.log`.
- Validation limitation: the repository CMake configuration has no dedicated
  full native VM test-suite target; the standalone Skia fixture was compiled
  directly against the built macOS VM library.
- Commit-message note: the Plan 1-derived commits `62d4afbba` and
  `374d55370` have body lines over 80 characters due to the no-amend history
  policy; `bc6085176` and `d712b2df4` passed the local message validator.
- Next concrete action: none for Plan 2; proceed only with a separately
  requested follow-up.
- Resume command: `cd /Users/flsobral/repos/totalcross-github && sed -n
  '1,180p' .agent/state/fix-skia-font-pipeline-02.md`
