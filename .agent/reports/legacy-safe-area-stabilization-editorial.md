<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Legacy safe-area stabilization editorial report

Status: complete on 2026-08-10.

## Restored legacy behavior

`SlidingWindow`, `MaterialWindow`, and `TopMenu` are Window-based again.
They use the established Window popup/unpop, z-stack, focus, input, outside
dismissal, Back/Escape, path animation, self-fade, and `fadeOtherWindows`
behavior. `SideMenuContainer` again presents its drawer through TopMenu.

SlidingWindow establishes its final bounds and repositions children before
moving only x/y to the temporary animation origin. TopMenu retains disabled
automatic safe-area consumption, explicit attached-edge mapping, orthogonal
animation positioning, and safe-aware fixed-bar hosts.

Horizontal SideMenu automatic width is:

    max(1, min(320, screenWidth - safe.left - safe.right - 56))

A later explicit `topMenu.widthInPixels` assignment remains authoritative.
TOP/BOTTOM percentage sizing is unchanged.

## Retained logical scaling and safe-area work

Historical reconstruction was limited to the named components and fade-specific
changes after `91616c934...`. `Control.java` was not restored wholesale.
Unrelated logical scaling, safe-area APIs, client geometry, clipping, rendering,
and all unrelated untracked work remain untouched.

## Deferred presentation subsystem

Eight package-private classes now live under `totalcross.ui.presentation`.
Window and the release-critical components have no references to them. They
compile against existing public APIs without widening Window, Control, or
Container.

Deferred runtime limitations are viewport clipping, app-quit observation during
blocking presentation, and forced fill layout for content. Runtime parity is
future architecture work and does not affect the legacy release path.

Provenance audit `2026-08-10-9e430ce304f4-a44665341061-min24` reports inherited,
high-confidence lineage with preserved headers for seven material moved files.
It remains pending human review; no approval command was run.

## Validation

- Legacy safe-area focused set: 18 tests passed.
- Deferred PresentationHost compilation/test: passed.
- JavaSE preview visual lane: passed with ten manually inspected PNGs covering
  portrait and asymmetric landscape insets, transition/endpoints, reopen,
  resize, outside dismissal, Back/Escape, and background dimming.
- Non-clean SDK `dist -x test`: passed in 50 seconds.
- Replacement smoke compilation: passed.
- Native macOS deploy/run: passed with expected geometry and `final=PASS`.
- Android/iOS builds: intentionally not run under the release plan.
- Final diff and header checks passed for 27 changed files. All 9 new tracked
  files and 16 pending-audit files meet the plan's size limits.

The distribution emitted existing Javadoc diagnostics and one Gradle
deprecation notice but completed successfully.

## Remaining limitations

Presentation runtime parity and human activation of the pending provenance audit
remain deferred. The native smoke validates runtime geometry and lifecycle; the
release-focused screenshots come from the real JavaSE preview renderer. No
changes were pushed or published.
