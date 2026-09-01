<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Windows SDL + Skia migration state

- Plan key: `windows-sdl-skia-raster`
- Active milestone: complete; final desktop startup correctness follow-up
  complete; Windows interactive smoke remains deferred
- Last logical commit: `204306bdf fix(windowing): correct TCZ startup
  centering`; preceding startup-policy commits remain
  `7a8a11ffd`, `e562e0501`, and `a47d1d1a`.
- Active paths: `TotalCrossVM/src/nm/ui/WindowStartup.h`,
  `TotalCrossVM/src/nm/ui/WindowStartup.c`, `TotalCrossVM/src/init/globals.c`,
  `TotalCrossVM/src/init/globals.h`, `TotalCrossVM/src/init/startup.c`,
  `TotalCrossVM/src/init/tcsdl.cpp`,
  `TotalCrossVM/src/nm/ui/win/gfx_Graphics_c.h`, startup tests, CMake/Android
  source lists, `TotalCrossVM/src/tests/window_startup_native_test.c`, and
  `scripts/test-sdl-desktop-contracts.py`.
- Starting prerequisite: confirmed the pinned depot checkout is at
  `118ff8925b165c79de87cd2d69f562b570b1ebd5`, `deps.yml` pins SDL
  `sdl2-2.32.8`, and the CMake path consumes `SDL2::SDL2` and `Skia::Skia`.
- Baseline limitation: this macOS host has no Windows compiler/runtime and no
  staged Windows SDL artifacts, so Native + Legacy runtime/binary baseline is
  deferred to a Windows-capable lane. Existing macOS build caches are kept
  untouched.
- Focused validation: `python3 scripts/test-sdl-desktop-contracts.py` passed
  16 focused contract tests; the opt-in `window_startup_native_test`
  executable built and ran `test_windowResolveStartupConfiguration`; the
  macOS SDL + Skia + Software `tcvm`/`Launcher` build passed; source/test
  copyright headers passed; and staged diff checks passed.
- Hotkey bridge validation: SDL’s Windows message-hook install/remove points,
  `WM_HOTKEY` filtering, Win32 reverse mapping, and separation from SDL key
  translation are covered by the focused contract suite.
- Command-line validation: the existing native regression now compares exact
  compacted TCZ and application strings, exercises reserved options before,
  between, and after `/cmd`, and preserves exact near-matches.
- Shortcut validation: Ctrl+A, Ctrl+C, Ctrl+P, Ctrl+V, Ctrl+X, and Ctrl+Space
  use one SDL keydown-to-key path with raw Ctrl modifiers; ordinary printable
  text remains on `SDL_TEXTINPUT`, with no general printable-keydown fallback.
- Final local validation: the SDL + Skia + Software macOS Release `tcvm` and
  `Launcher` build passed with log
  `/tmp/desktop-startup-final-macos-build.log`; the native resolver executable
  passed with log `/tmp/desktop-startup-native-test-exec-run.log`.
- Deferred validation: Windows x86/x64/ARM64 configure/build/link and runtime
  smoke for both default and fallback, including mixed-DPI monitors and
  native-library event hooks, because this host has no Windows toolchain,
  runtime, or staged Windows artifacts.
- Blockers: none for the source implementation or required CI compilation.
  Exact-HEAD Merge Flow run `33563499168` at
  `204306bdf99556e75f098b9d2796292e9a6d8710` passed Android, iOS, macOS,
  Windows, Linux, and SDK jobs; the intentionally disabled Linux ARM32 cross
  job was skipped. Windows interactive keyboard smoke and native event-hook
  runtime behavior remain deferred because this host has no Windows runtime.
- Deliberate out-of-scope local files: existing untracked dependency/generated
  trees and helper scripts outside this plan.
- Next action: none. The final desktop startup correctness follow-up is
  complete; retain the documented Windows runtime limitation and do not amend
  or rewrite the logical commits.
- Resume command: `sed -n '1,220p' .agent/state/windows-sdl-skia-raster.md`

## Closure record

- `cb5f06b6d fix(event,sdl): enable desktop keyboard input` starts/stops SDL
  text input explicitly while retaining separate keydown/text-input dispatch.
- `19efed8a0 refactor(event,sdl): own SDL special key mappings` owns SDL
  navigation, editing, modifier, and desktop function-key translation in the
  SDL backend.
- `947cfec9b fix(runtime): filter VM options from application arguments` was
  superseded by `1f0512734 refactor(runtime): separate VM and application
  arguments`, which centralizes desktop payload filtering and adds regression
  coverage for mixed options after `/cmd` and near-matches.
- `0e0b1b527 test(runtime): preserve command-line near matches` corrects the
  regression assertion to distinguish the exact `/cmd` separator from the
  required `/cmdlike` application argument.
- `c1041cb9d fix(event,sdl): activate text input after window setup` closes
  the startup-order gap by activating SDL text input after window creation.
- `11fa2cae7 fix(event,sdl): preserve raw event modifiers` keeps SDL modifier
  values raw until the shared event layer translates them and preserves the
  `-1` no-modifier convention for mouse events.
- `7f16a2a83 fix(vm,windows): keep native hotkey keycodes` keeps Win32 native
  hotkey registration on VK values while SDL owns event translation.
- Final macOS SDL + Skia build and SDK distribution build passed. The plan is
  from the prior closure is superseded by this follow-up; Windows architecture/
  runtime and full interactive keyboard smoke remain deferred to CI.
- `195e218fc fix(event,windows): bridge SDL native hotkeys` installs SDL’s
  supported Windows message hook with the SDL window lifecycle and converts
  registered Win32 hotkey VK values back to portable keys before posting.
- `008c91ff3 fix(runtime): compact filtered command line` filters every
  reserved VM option on the composite line before splitting and guarantees
  canonical single-space output for the TCZ and application payloads.
- `02eb42a8c fix(event,sdl): forward control shortcuts` forwards Ctrl+A/C/V
  from SDL keydown without changing ordinary text-input ownership.
- `6d01ab5d1 fix(event,windows): guard SDL hotkey hook setup` installs the
  bridge only after SDL returns a valid native Windows window handle.
- `fdab5c7a7 fix(runtime): preserve mobile command separator` keeps the exact
  desktop separator parser while restoring the historical non-desktop lookup
  semantics so Android/iOS compilation no longer sees a desktop-only helper.
- `f1dbb7827 fix(event,sdl): preserve special-key dispatch semantics` adds one
  SDL special-key dispatcher for navigation, rotation, and native hotkeys, and
  forwards the existing Edit/MultiEdit Ctrl+A/C/P/V/X/Space contract without
  adding a printable-text fallback.
- CI run `33450370159` at `51dde0f43` passed Android and iOS compilation and
  all other enabled build jobs, confirming the non-desktop startup fix.
- The final source audit and allowed macOS/SDK builds passed. The local sample
  reached the SDL loop but could not complete interactive input smoke because
  runtime-state creation failed in the temporary launch setup. Windows runtime
  and interactive keyboard proof remain explicitly deferred to CI.
- `204306bdf fix(windowing): correct TCZ startup centering` fixes TCZ position
  modes after environment overrides and adds the executable resolver test
  target. Its focused checks, macOS build, and exact-head Merge Flow
  `33563499168` passed. The broad native suite was not enabled because of
  unrelated pre-existing `objectmemorymanager_test.h` compilation errors.
