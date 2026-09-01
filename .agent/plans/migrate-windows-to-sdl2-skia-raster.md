<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Migrate Windows desktop to SDL2 windowing and Skia raster rendering

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`. It defines the
architecture and acceptance criteria for replacing the experimental Windows
desktop migration history with a concise, reviewable implementation history.

## Purpose / Big Picture

Windows desktop currently couples Win32 window creation, event delivery,
software presentation, and platform services. The migration makes those
responsibilities explicit so Windows can use SDL2 for windowing and events,
Skia for raster drawing, and the existing CPU framebuffer for presentation,
while retaining the native Win32 implementation as a supported fallback.

The observable result is a Windows default of SDL2 + Skia + Software with
physical-pixel presentation and logical TotalCross coordinates. A developer
can still configure Native + Legacy + Software, and WinCE remains on its
existing native path. The implementation must not add a TotalCross GPU
backend or make host/system SDL a prerequisite for supported builds.

## Working Set and Resume Protocol

The implementation worktree is the temporary rebuild branch created from the
requested base commit. The original branch is protected by a backup branch;
the backup is never modified. The two tracked artifacts for this migration
are this plan and the final editorial report at
`.agent/reports/windows-sdl-skia-raster-editorial.md`.

No migration state, evidence, archive, or helper script is part of the
rebuilt history. Validation logs and the old/new revision manifest live in
the developer worktree or temporary artifact directories and are not source
deliverables. If interrupted, inspect the current branch, its last commit,
the scoped diff, and the backup branch before continuing.

## Progress

- [ ] Record the final old/new revision comparison and production-tree audit.
- [ ] Rebuild the functional history in architectural order.
- [ ] Pass focused tests, header checks, source limits, macOS SDL + Skia
  Release validation, SDK validation, and exact-HEAD enabled CI.
- [ ] Add the final factual report only after functional CI succeeds.
- [ ] Update the original branch with `git push --force-with-lease`, leaving
  the backup branch intact.

## Current Architecture and Scope

### Supported configuration matrix

The selectors in `TotalCrossVM/cmake/TCGraphics.cmake` expose three
independent choices:

    Windowing       Renderer       Graphics       Contract
    Native          Legacy         Software       Windows fallback
    SDL             Legacy         Software       diagnostic path
    SDL             Skia           Software       Windows default
    Native          Skia           any            rejected on Windows
    any             any            GLES            outside this migration

WinCE is fixed to Native + Legacy + Software. Android and iOS retain their
existing platform-specific defaults. SDL2 comes from the pinned
`TotalCrossVM/deps/totalcross-depot-tools` checkout and is linked through the
repository's imported dependency target.

### Windowing and event ownership

`TotalCrossVM/src/event/Event.c` selects event delivery by windowing backend.
The SDL event implementation belongs under `src/event/sdl`, while DirectFB
and Native Windows implementations remain in their platform-specific paths.
SDL owns SDL event polling, text input, modifier capture, special-key
translation, mouse unions, lifecycle events, and supported Windows hotkey
bridging. The native Windows event path remains available for Native
windowing.

`TotalCrossVM/src/nm/ui/sdl` owns SDL window state and the renderer-facing
surface contract. `Window.c` separates backend operations from platform
services. Windows SDL may obtain an `HWND` through SDL SysWM only for native
services; it must not reintroduce a second Win32 event pump.

### Raster and lifecycle ownership

The software graphics path owns a CPU framebuffer. SDL presents that memory
through a fixed ARGB8888 streaming texture, and Skia uses the corresponding
BGRA raster interpretation deterministically. Window-owned SDL surfaces are
not borrowed or freed. SDL window/renderer lifetime is separate from
backbuffer and Skia binding lifetime so screen recreation can tear down and
rebind the correct resources.

SDL logical and physical display metrics drive content scale, resize, and
display-change handling. Resize ownership belongs to the selected windowing
backend. Rotation confirmation and F9 behavior must not apply stale or
malformed pending requests.

### Startup and command-line contracts

Desktop startup uses a shared resolver in `WindowStartup.c/.h`. It owns
environment loading, `/scr` presence and values, TCZ dimensions, default
window sizing, position, fullscreen, maximized, and resizable state. The
precedence is explicit `/scr`, valid environment dimensions, TCZ attributes,
then half-display defaults; an explicit `-1` remains an explicit source and
resolves its missing dimension to a default. SDL and Native Windows translate
one resolved configuration into their native APIs, while WinCE keeps its
existing path.

The launcher parses the full composite command line, including options after
`/cmd`, removes reserved VM options, preserves near matches and unrelated
arguments, and passes `MainWindow.getCommandLine()` only the compacted
application payload. The historical non-desktop command-separator behavior is
preserved.

## Plan of Work

1. Write this plan as the first post-base commit.
2. Consolidate Windows SDL dependency preparation, static-runtime settings,
   workflow fallback coverage, and the final depot-tools reference.
3. Make graphics, renderer, and windowing selectors explicit and enforce the
   supported matrix.
4. Extract shared SDL window state, route software graphics by backend, and
   route events by windowing selection.
5. Add the Windows SDL adapter, SysWM platform-service bridge, software
   framebuffer, Skia raster binding, deterministic pixel format, and Skia
   ownership rules.
6. Separate backend operations from platform services, centralize SIP values,
   and move resize ownership to the backend.
7. Consolidate SDL text input, keyboard shortcuts, modifiers, special keys,
   native hotkeys, and their permanent contract coverage.
8. Consolidate desktop command-line filtering and preserve legacy separator
   semantics.
9. Consolidate F9, rotation, resize confirmation, pending-request handling,
   and display-change behavior.
10. Add the final shared `WindowStartup` resolver and its CMake wiring,
    followed by permanent native and source-level tests.
11. After functional validation and exact-HEAD CI, write the final report as
    the last commit and perform the leased branch update.

Each implementation commit will contain one architectural purpose, preserve
the final behavior directly, and omit superseded fixes, reversions, and
execution artifacts. Commit titles and bodies will pass the repository
validator and cached whitespace checks before acceptance.

## Decision Log

- Decision: keep Native + Legacy + Software as an explicit Windows fallback.
  Rationale: the migration must not make SDL runtime or renderer behavior the
  only recovery path.

- Decision: use SDL for shared windowing and events, but use SysWM only for
  Windows platform services. Rationale: this prevents duplicate event pumps
  and keeps platform integrations available without coupling rendering to
  Win32 window creation.

- Decision: use a CPU framebuffer with a fixed SDL texture and Skia raster
  surface. Rationale: the target is software rendering; SDL surface format
  discovery and window-owned surface lifetime are not stable contracts for
  this architecture.

- Decision: centralize startup policy in `WindowStartup` and keep backend
  translations local. Rationale: repeated fixes to `/scr`, environment, TCZ,
  fullscreen, and sizing must resolve once and behave consistently across
  SDL and Native Windows.

- Decision: keep `TotalCrossVM/src/jni/Android.mk` identical to the base.
  Rationale: it is legacy Android wiring outside this migration and must have
  no branch history in the rebuilt result.

## Validation and Acceptance

Validation is a release-gate change because the rewrite crosses CMake
selectors, native source dispatch, event/input behavior, startup ABI, and
Windows platform integration.

Required focused validation includes:

- commit-title/body validation and `git diff --check --cached` before every
  reconstructed commit;
- focused copyright/header validation for every changed first-party source,
  test, script, Markdown, and workflow file;
- permanent SDL desktop contracts, command-line resolver tests, and the
  retained native startup resolver test;
- source-size and source-growth checks required by the repository;
- `git diff --check $BASE..HEAD`, where `$BASE` denotes the branch base;
- macOS Release SDL + Skia + Software `tcvm`/`Launcher` build and the SDK
  validation normally required by this migration;
- selector checks for default, fallback, diagnostic, and rejected matrix
  configurations;
- exact-HEAD Merge Flow with every enabled job, including Windows SDL and
  Windows Native + Legacy. Policy-disabled jobs may remain skipped.

The functional HEAD must pass CI before the final report commit is created.
The report commit is documentation-only and does not trigger a second CI
requirement. Windows interactive behavior can be reported as deferred only if
the enabled Windows CI jobs pass and no local Windows runtime is available.

Acceptance also requires that the production tree matches the old branch's
final implementation except for the intentional Android.mk restoration and
removal of migration execution artifacts. Exactly the plan and report remain
as tracked migration artifacts.

## Risks and Open Questions

- The macOS host may not provide Windows compilers, runtime smoke, or staged
  Windows native artifacts; those claims require the exact-HEAD Windows CI
  jobs.
- SDL logical versus physical metrics can diverge across HiDPI monitors and
  display changes; both the resolver and renderer binding must preserve the
  logical/physical contract.
- SDL text input and keydown ownership can duplicate printable characters or
  lose modifiers if translation occurs in more than one layer.
- Static MSVC runtime and dependency pins must remain consistent between
  local CMake configuration and Windows workflow fallback builds.
- The rewritten history must not accidentally drop a final fix merely because
  its original commit was labeled as a temporary repair; the old/new
  production diff is the final audit.

## Idempotence and Recovery

The backup branch is the authoritative recovery point for the original
history. Re-running reconstruction must use the temporary branch and compare
its tree to the backup; never mutate or delete the backup. The original
worktree's untracked files remain outside the rewrite and are not cleaned.

Before the final update, verify the rebuilt functional HEAD, the exact
Android.mk comparison, tracked artifact inventory, scoped production diff,
and CI result. If any check fails, leave the original branch and backup
untouched, correct the temporary branch, and rerun only the necessary focused
validation.

## Outcomes & Retrospective

This section is completed in the final report commit after the functional
history, validation evidence, production-tree audit, and branch update are
complete. It will distinguish delivered behavior from deferred runtime proof
and document any compatibility limitation that remains.
