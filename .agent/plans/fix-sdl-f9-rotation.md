<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Fix SDL F9 rotation and window ownership

This ExecPlan follows `AGENTS.md`, `.agent/PLANS.md`, and
`.agents/skills/logical-commits/SKILL.md`.

## Purpose / Big Picture

F9 on SDL must rotate the application window in logical coordinates. SDL then
reports the resulting physical renderer size, and the existing screen-change
pipeline recreates the framebuffer, texture, and Skia raster resources once.
Native Windows keeps its historical full-window rotation behavior, but the
Win32 resize operation belongs to the Window backend rather than graphics.

## Working Set and Resume Protocol

The active state is `.agent/state/fix-sdl-f9-rotation.md`; read it first when
resuming. `.agent/evidence/fix-sdl-f9-rotation.md` records compact validation
results, and `.agent/reports/fix-sdl-f9-rotation-editorial.md` is the final
factual handoff. Existing `.agent` files and generated local files outside
these paths are unrelated and must remain untouched.

## Progress

- [x] Refactor the window-size ownership boundary while preserving native
  Windows rotation.
- [x] Route SDL F9 through a generic logical SDL resize and retain one event
  driven commit.
- [x] Add focused source-contract regression coverage and run final permitted
  macOS VM/Launcher and native smoke validation.
- [x] Apply final native snapshot and SDL resize-success ordering fixes.

## Current Architecture and Scope

`TotalCrossVM/src/event/sdl/event_c.h` dispatches F9 through
`dispatchPortableSpecialKey()`. Its current path records the queried logical
window orientation, requests a generic SDL resize, and defers minimum-size
orientation updates until `SDL_WINDOWEVENT_SIZE_CHANGED`. That event queries
`TCSDL_QueryWindowMetrics()`, applying the configuration, consuming pending
flags, and calling `screenChangeCommitted()`.

`screenChangeCommitted()` invokes only the graphics configuration hook. The
native Win32 resize operation is exposed by the Window backend, while SDL F9
does not call `screenChange()` directly.

## Plan of Work

### Milestone 1 — Separate window resizing from graphics

Add a Window-level size operation selected by the active Window backend. Move
the existing Win32 border adjustment and `SetWindowPos` behavior into the
native Windows Window backend. Remove `privateScreenChange()` and rename the
remaining GLES projection hook to a graphics-only hook, with explicit no-op
implementations for software/native legacy graphics. Update the native Windows
F9 paths to use the Window operation and then preserve their existing
`screenChange()` semantics.

Focused proof: source contracts verify no Win32 resize remains in the graphics
backend, the Window backend owns it, and native F9 still requests a full-window
resize before committing the swapped configuration.

### Milestone 2 — Route SDL F9 through logical window size

Add generic `TCSDL_SetWindowSize(width, height)` to `tcsdl`. In the SDL F9
dispatcher, read the current logical SDL window size, swap it, and request the
resize. Do not mutate physical `screen` dimensions or call `screenChange()` in
that path. Keep `SDL_WINDOWEVENT_SIZE_CHANGED` as the only path that queries
logical and renderer-output dimensions, applies the configuration, commits it,
and recreates resources.

Focused proof: source contracts verify logical width/height swapping, physical
metrics assignment, and the absence of a second commit/recreation path.

### Milestone 3 — Final focused validation and macOS milestone gate

Run the focused Python contract suite, header validation for changed files,
`git diff --check`, and any available test-enabled syntax check during
implementation. At closure, run the permitted macOS SDL + Skia VM/Launcher
build and native smoke test once. Do not build Windows or other platforms.

## Decision Log

- Decision: F9 obtains dimensions from `TCSDL_GetWindowSize()` and requests
  `TCSDL_SetWindowSize(height, width)`; it does not use `tcSettings` as the
  physical configuration source.
  Rationale: SDL owns the logical window size and the subsequent event query
  is authoritative for both logical/physical separation and HiDPI scaling.
  Date: 2026-09-01.

- Decision: Keep `screenChange()` for native Windows F9 after the Window
  backend resize, but remove its indirect graphics-owned window resize.
  Rationale: native Win32 has no SDL size-change pipeline and must retain its
  historical full-window behavior without expanding the refactor.
  Date: 2026-09-01.

## Validation and Acceptance

Acceptance requires:

1. SDL F9 requests swapped logical SDL window dimensions.
2. SDL resize handling derives physical surface dimensions from queried SDL
   renderer output, not logical `tcSettings` dimensions.
3. One SDL F9 rotation produces one configuration commit and one resource
   recreation path.
4. Native Windows retains border-aware `SetWindowPos` full-window rotation.
5. Graphics no longer owns window resizing.

Implementation validation is Level 1/2: focused contracts, syntax where
available, header validation, and diff checks. The final related-milestone
gate is Level 3 only for the permitted macOS VM/Launcher build and native
smoke test. Windows and other platform builds are explicitly deferred.

## Risks and Open Questions

- A native Win32 `SetWindowPos` can synchronously produce `WM_SIZE`; preserve
  the existing event guard and ordering rather than introducing a new event
  state machine in this focused refactor.
- SDL window-size requests may be ignored or fail in fullscreen; the generic
  operation reports failure, while the existing F9 eligibility and SDL event
  behavior remain unchanged.

## Idempotence and Recovery

All source changes are additive or local replacements. Re-running focused
contracts and header validation is safe. Stage only implementation, test, and
this plan's state/evidence/report paths; never stage existing unrelated `.agent`
  files, generated artifacts, or dependency checkouts. Commits are small and
non-amended. Push only when required for exact-HEAD CI.

## Outcomes & Retrospective

Milestones 1 and 2 are implemented in `ece6ee5f7` and `8ea9cd486`; the SDL
wrapper compile correction is `40d127c46`, the snapshot fix is `500119506`,
and final SDL confirmation is `b27b7df91`. Focused contracts, the permitted
macOS VM/Launcher build, and exact-HEAD Merge Flow run `33542462499` passed.
The native smoke inventory remains unavailable because `ctest -N` reports zero
configured tests.
