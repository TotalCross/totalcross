<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Windows SDL + Skia migration state

- Plan key: `windows-sdl-skia-raster`
- Active milestone: complete; Windows interactive smoke remains deferred
- Last implementation commit: `f1dbb7827 fix(event,sdl): preserve special-key dispatch semantics`
- Active paths: `TotalCrossVM/src/event/sdl/event_c.h`,
  `TotalCrossVM/src/event/sdl/specialkeys_c.h`,
  `TotalCrossVM/src/nm/sys/win/Vm_c.h`, `TotalCrossVM/src/init/startup.c`,
  `TotalCrossVM/src/init/startup_test.h`,
  `TotalCrossVM/src/tests/tc_tests.c`,
  `TotalCrossVM/src/init/tcsdl.h`, and
  `scripts/test-sdl-desktop-contracts.py`.
- Starting prerequisite: confirmed the pinned depot checkout is at
  `118ff8925b165c79de87cd2d69f562b570b1ebd5`, `deps.yml` pins SDL
  `sdl2-2.32.8`, and the CMake path consumes `SDL2::SDL2` and `Skia::Skia`.
- Baseline limitation: this macOS host has no Windows compiler/runtime and no
  staged Windows SDL artifacts, so Native + Legacy runtime/binary baseline is
  deferred to a Windows-capable lane. Existing macOS build caches are kept
  untouched.
- Focused validation: `python3 scripts/test-sdl-desktop-contracts.py` passed
  seven focused contract tests; the test-enabled `startup.c` syntax check passed;
  copyright headers passed for all fourteen affected files; the completed
  range and working-tree `git diff --check` passed; and the native regression
  is wired into the existing `ENABLE_TEST_SUITE` registry. New files remain
  below the 20 KB / approximately 600-line limit.
- Hotkey bridge validation: SDL’s Windows message-hook install/remove points,
  `WM_HOTKEY` filtering, Win32 reverse mapping, and separation from SDL key
  translation are covered by the focused contract suite.
- Command-line validation: the existing native regression now compares exact
  compacted TCZ and application strings, exercises reserved options before,
  between, and after `/cmd`, and preserves exact near-matches.
- Shortcut validation: Ctrl+A, Ctrl+C, Ctrl+P, Ctrl+V, Ctrl+X, and Ctrl+Space
  use one SDL keydown-to-key path with raw Ctrl modifiers; ordinary printable
  text remains on `SDL_TEXTINPUT`, with no general printable-keydown fallback.
- Final local validation: the SDL + Skia + Software macOS Release build passed
  with log `/tmp/totalcross-sdl-desktop-final-macos-build-followup2.log`, and
  `TotalCrossSDK/gradlew-agent dist` passed with log
  `/tmp/totalcross-sdk-final-dist-followup2.log`.
- Deferred validation: Windows x86/x64/ARM64 configure/build/link and runtime
  smoke for both default and fallback, including mixed-DPI monitors and
  native-library event hooks, because this host has no Windows toolchain,
  runtime, or staged Windows artifacts.
- Blockers: none for the source implementation or required CI compilation.
  CI run `33450370159` at `51dde0f43` passed Android and iOS, as well as the
  macOS, Windows, Linux, and SDK jobs. Windows interactive keyboard smoke and
  native event-hook runtime behavior remain deferred because this host has no
  Windows runtime. A local macOS sample launch reached the SDL loop but did not
  complete because its runtime-state write failed in the available smoke setup.
- Deliberate out-of-scope local files: existing untracked dependency/generated
  trees and helper scripts outside this plan.
- Next action: none. The migration is complete; retain the documented Windows
  runtime limitation and do not amend or push from this task.
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
