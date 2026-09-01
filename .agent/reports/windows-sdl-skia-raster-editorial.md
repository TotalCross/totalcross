<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Windows SDL2 + Skia raster migration — editorial handoff

## Outcome

The migration history was rebuilt from the branch base into focused logical
commits. The protected pre-rebuild backup remains unchanged. The final
production tree matches the backup except for the intentional removal of the
two plan-execution helper scripts and the required Android.mk invariant.

The standalone SDL lifecycle fix was removed. Its SDL event polling and Apple
touch behavior now belong to the SDL event/input commit, while startup
`appTczAttr` propagation belongs to the desktop startup integration commit.

## Logical commit list

- docs(plan): plan Windows SDL2 Skia migration
- build(windows): prepare SDL2 dependency toolchain
- build(cmake): define desktop backend matrix
- refactor(windowing): extract shared SDL surface state
- refactor(graphics): route software backend by windowing
- refactor(event): select dispatch by windowing backend
- refactor(windowing,windows): bridge SDL window services
- feat(renderer): integrate software Skia presentation
- refactor(window): separate backend platform services
- feat(event): consolidate SDL desktop input ownership
- fix(windowing): harden display and screen lifecycle
- fix(runtime): filter desktop command line options
- fix(windowing): finalize SDL rotation ownership
- refactor(windowing): centralize desktop startup policy
- test(windowing): retain desktop contract coverage
- docs(report): report Windows SDL2 Skia migration

The two Python desktop-contract helpers are not in this history. They are
plan-execution tools and are restored only in the worktree after this report.

## Final architecture

Windows defaults to SDL + Skia + Software. Native + Legacy + Software remains
available as a fallback, SDL + Legacy + Software remains useful for
diagnostics, Windows Native + Skia is rejected, and WinCE remains Native +
Legacy + Software.

SDL owns the desktop window and event path. SysWM is limited to platform
services and does not create a second Win32 event pump. The SDL event/input
commit owns SDL polling, text input, modifiers, special keys, mouse events,
touch handling, lifecycle events, and the supported Windows hotkey bridge.

The software path presents a CPU framebuffer through a fixed ARGB8888
streaming texture. Skia uses the matching BGRA raster interpretation and
deterministic channel mapping. Window, renderer, backbuffer, and Skia resource
lifetimes remain explicit across resize and screen recreation.

Desktop startup policy is centralized in `WindowStartup.c/.h`. It resolves
TCZ attributes, environment dimensions, `/scr`, position, fullscreen,
maximized, and resizable state once, then lets SDL and Native Windows apply
their platform-specific outer-window behavior.

## Provenance decision

The provenance-audit stage was explicitly waived for this rerun. No provenance
classifications were activated and no provenance-driven header repairs were
applied. Ordinary repository header validation passed for the rebuilt
functional tree.

## Validation

- Passed: ordinary copyright-header validation for the rebuilt functional
  range.
- Passed: final whitespace validation from the branch base.
- Passed: `TotalCrossVM/src/jni/Android.mk` comparison against the branch base.
- Passed: production-tree comparison against the protected backup after
  excluding migration documents, legal audit artifacts, Android.mk, and the
  intentional helper removal; no unexplained production difference remains.
- Passed: all newly created source files stayed below 20 KiB and 600 lines.
- Assessed: no active repository source-size validator exists. A separate
  untracked draft reports two changed legacy files crossing its draft limit;
  no production code was changed to satisfy that non-active policy.
- Passed: native `window_startup_native_test` executable.
- Passed: macOS Release SDL + Skia + Software builds for `tcvm` and `Launcher`.
- Passed: SDK distribution build with `TotalCrossSDK/gradlew-agent dist -x test`.
- Passed: `ctest`; no tests are registered in the native build directory.

## Merge Flow

The new exact-HEAD Merge Flow passed: [run 33574454125](https://github.com/TotalCross/totalcross/actions/runs/33574454125).
Checkout, Windows SDL, Windows Native + Legacy, macOS, Linux amd64, Linux
arm64, Linux arm32v7, Android, iOS, and SDK jobs passed. The policy-disabled
Linux arm32v7 cross job was skipped.

## Deferred runtime coverage

The developer host has no Windows compiler/runtime or interactive Windows
desktop. Windows runtime smoke, mixed-DPI monitor movement, manual text and
modifier interaction, native hotkeys, and visual comparison remain for a
Windows-capable environment. The enabled Windows CI jobs provide build
coverage, not interactive desktop smoke.
