<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Safe presentation editorial handoff

## Editorial Summary

This work replaces fake top-level windows used by sliding panels and menus with
an internal presentation host owned by the real `Window`. Transient UI now moves
inside a clipped viewport matching the owner's safe client rectangle, so it no
longer stages itself against physical-screen coordinates or mutates the global
window stack. Focused tests, a non-clean SDK distribution, JavaSE smoke, and a
deployed macOS native smoke all passed.

## Original Plan versus Actual Outcome

The planned architecture was implemented: explicit default clipping, corrected
`ClippedContainer` culling, package-private presentation primitives, and
migration of `SlidingWindow`, `MaterialWindow`, `TopMenu`, and
`SideMenuContainer`. Validation followed the intended escalation order and the
existing macOS deploy path proved economical enough to use.

Two details differed from the simplest reading of the plan. A safe-area change
during motion finishes the transition at a stable endpoint instead of
restarting it, which keeps relayout deterministic. Also, the previously
untracked TopMenu sample became part of the change because the superclass
migration made its old `Window` cast fail the SDK distribution compile.

## What Changed

`Container` now exposes an internal, default-enabled child-clipping policy, and
`Control.refreshGraphics` honors it while preserving coordinate translation.
`ClippedContainer` now uses an unambiguous `-1` sentinel and correctly respects
half-open child ranges when no child is visible.

`Window` lazily creates a `PresentationHost`. The host owns route and overlay
layers, a clipped safe viewport, modal barriers, presentation frames, handles,
and explicit slide/fade transitions. Presentation and dismissal are idempotent,
and the host removes itself when empty.

`SlidingWindow` and `TopMenu` now extend `Container`. `MaterialWindow` retains
its bar/provider behavior without starting duplicate delayed loads. TopMenu
computes its drawer and bar layout within the safe viewport, while
`SideMenuContainer` handles gestures locally instead of attaching a global
listener to its parent window.

## Decisions and Trade-offs

The presentation API is package-private to limit the compatibility surface
until more components need it. Existing real `Window` popup semantics were left
unchanged for dialogs and other genuine top-level surfaces.

Default clipping behavior remains enabled; the new policy makes an existing
contract explicit rather than changing rendering globally. Safe-area changes
complete active motion before relayout, favoring a stable deterministic state
over animation continuity.

The superclass changes intentionally remove `Window` assignability. Repository
call sites compile and the affected sample was updated, but external source or
binary consumers that treated these classes as `Window` must adapt.

## Unexpected Problems and Discoveries

TotalCross rejects `FILL` layout before a parent has nonzero bounds, so the host
test and implementation must establish parent geometry before deferred child
layout. `MaterialWindow` also revealed two paths that could start delayed
provider loading; the migration consolidated this into one insertion hook.

The first native deploy attempt used a single class file. The deployer correctly
rejected a class in the reserved `totalcross.*` package, so the same generated
class was packaged into a jar and deployed successfully. The source and deployed
native libraries were then verified by matching SHA-256 hashes.

## Validation and Measurable Results

The final focused suite ran seven test classes and passed all 16 tests. It covers
ancestor clipping, offscreen culling, presentation lifecycle and cleanup, four
slide directions, slack, retained provider/body identity, TopMenu and SideMenu
safe geometry, outside dismissal, local gestures, and existing safe-area layout.

One non-clean `dist -x test` SDK build passed, followed by smoke compilation.
JavaSE smoke reported safe viewport `20,10,260,600`, sliding final bounds
`0,0,260,600`, TopMenu final bounds `0,0,204,600`, zero window-stack delta,
unchanged owner, passing clipping, and `final=PASS`.

The macOS CMake configuration and `tcvm` build passed. The direct deployed
native smoke reported safe viewport `20,10,1668,941`, sliding final bounds
`0,0,1668,941`, TopMenu final bounds `0,0,320,941`, zero window-stack delta,
unchanged owner, passing clipping, and `final=PASS`. Both dylib hashes were
`fccd8da2a253d409611b11822606e9521f92d5771e762f1613c0fe0c38986db5`.

Copyright validation, `git diff --check`, repository compilation compatibility,
and byte/line checks for every new file passed. No new file reaches 20 KiB or
approximately 600 lines.

## Useful Evidence and Examples

The compact command ledger and exact smoke output are in
`.agent/evidence/ui-presentation-safe-area.md`. Full logs are under
`artifacts/ui-presentation-safe-area/logs/`, with Gradle wrapper logs under
`TotalCrossSDK/agent-logs/`.

The most useful before/after claim is measurable: presentation previously used
top-level `Window` machinery, while smoke now observes `zStackDelta=0` and an
unchanged owner across both JavaSE and native macOS execution. The safe viewport
values also demonstrate that the content uses nonzero insets rather than a raw
screen rectangle.

## Limitations, Remaining Work, and Open Questions

No Android or iOS build or deploy was run, as explicitly required. The native
macOS fixture injects nonzero safe insets programmatically; it does not prove
hardware-notch event integration.

TopMenu title and border mappings have state/layout coverage but no screenshot
equivalence comparison. `fadeOtherWindows` remains available as compatibility
state, while the current modal barrier is transparent and does not reproduce the
old dimming effect. Broad focus scopes, a public navigation API, and a public
clipping API remain outside this implementation.

External consumers compiled against the old `SlidingWindow extends Window` or
`TopMenu extends Window` relationship may require source changes and cannot be
claimed binary-compatible from this repository-only validation.

## Possible Article Angles

- Why transient UI should be hosted inside a real window instead of pretending
  to be another top-level surface.
- Safe-area correctness as a coordinate-system problem, not an inset-patching
  problem.
- Turning an implicit clipping rule into a focused, testable internal contract.
- Combining deterministic unit geometry with a hash-verified native smoke lane.

## Suggested Narrative

Begin with the failure mode: sliding menus staged against physical-screen
coordinates and participated in the global window stack. Explain the invariant
chosen to replace it: one real owner, one clipped safe viewport, explicit local
transition coordinates. Then follow the migrations from shared host foundation
to SlidingWindow/MaterialWindow and TopMenu/SideMenu. Close with the two-lane
proof: deterministic nonzero-inset Java tests and smoke, followed by a deployed
native macOS executable whose runtime library matches the one just built.

## Claims Requiring Human Review

- Whether removing `Window` assignability is acceptable for external SDK users
  and whether a migration note is required.
- Whether TopMenu title and border rendering is visually close enough to the
  previous platform/UI-style variants.
- Whether `fadeOtherWindows` must regain visible dimming before release.
- Whether a later hardware or simulator run should supplement the injected
  nonzero-inset native smoke for product-level release confidence.
