<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda
SPDX-License-Identifier: LGPL-2.1-only
-->

# Repair fade lifecycle and restore presentation dimming

This ExecPlan is a living document maintained according to `.agent/PLANS.md`.
It builds on `.agent/ui-presentation-safe-area-execplan.md`; the completed
safe-area presentation architecture is preserved rather than repeated here.

## Purpose / Big Picture

The presentation refactor moved `SlidingWindow`, `TopMenu`, and
`SideMenuContainer` into a container-based `PresentationHost`. It exposed old
fade lifecycle bugs and added presentation-specific screenshot and dimming
regressions. This work repairs shared animation and screenshot infrastructure,
then restores visual parity for presentations without changing public APIs or
real `Window` fade behavior.

After the repair, direct fade, slide-plus-fade, interrupted transitions, nested
screenshots, logical content/font scales, and repeated presentation cycles work.
SideMenu slides at full opacity while a `PresentationBarrier` dims the content
behind it. JavaSE pixel tests provide deterministic alpha evidence and a macOS
native smoke proves completion without a rendering freeze.

## Progress

- [x] (2026-08-08 03:38Z) Recorded baseline `91616c934fe547c7b1b01c4a2990cb17ac865241` on actual branch `feat/logical-ui-scaling2` and preserved the unrelated dirty worktree.
- [x] (2026-08-08 03:38Z) Classified `ControlAnimation` and `FadeAnimation` defects as inherited unchanged from `master`; `Control.java` screenshot scale/geometry and presentation integration were branch-exposed.
- [x] (2026-08-08 03:42Z) Added and ran focused pre-fix tests. Five lifecycle/ownership cases and two screenshot cases failed as intended; final pre-fix log: `TotalCrossSDK/agent-logs/20260808-004228-test-full.log`.
- [x] (2026-08-08 03:44Z) Repaired composite initialization, per-animation screenshot ownership, slave cleanup, immediate initial alpha, and exception-safe update suppression; focused log: `TotalCrossSDK/agent-logs/20260808-004424-test-full.log`.
- [x] (2026-08-08 03:46Z) Repaired target-local screenshot traversal and destination content/font scaling; focused log: `TotalCrossSDK/agent-logs/20260808-004551-test-full.log`.
- [x] (2026-08-08 03:51Z) Made presentation frames transparent fade targets and restored full-host barrier dimming with independent self-fade intent.
- [x] (2026-08-08 03:51Z) Added nonzero directional, centered, SideMenu, relayout-abort, repeated-presentation, and pixel coverage; focused log: `TotalCrossSDK/agent-logs/20260808-005111-test-full.log`.
- [x] (2026-08-08 03:54Z) Audited TopMenu, SlidingWindow, Toast, VirtualKeyboard, TabbedContainer, and ScrollContainer; no consumer-specific production patch was needed.
- [x] (2026-08-08 03:55Z) Corrected retained partial-fade final alpha after the Toast-style regression exposed `88` instead of `128`; focused log: `TotalCrossSDK/agent-logs/20260808-005436-test-full.log`.
- [x] (2026-08-08 04:00Z) Passed final focused Java, non-clean SDK distribution, smoke compilation, JavaSE smoke, and reused-path native macOS smoke.
- [x] (2026-08-08 04:03Z) Passed 14-file copyright validation, scoped static checks, unchanged-Window verification, and all new-file size limits; recorded final commits and retrospective.

## Context and Orientation

Relevant code is under `TotalCrossSDK/src/main/java/totalcross/ui/`.
`Control.offscreen` is the image used by `FadeAnimation`; `offscreen0` is a
separate initial snapshot used by cross-fade consumers. `ControlAnimation.with`
composes animations while only the root owns the `MainWindow` update listener.

The presentation tree is:

    real Window
      PresentationHost
        PresentationBarrier
        safe viewport
          transparent frame
            content

`fadeOnPopAndUnpop` controls the frame's own fade. `fadeOtherWindows` controls
only the barrier dimming behind TopMenu. SideMenu intentionally sets the former
false and the latter true.

## Baseline and Defect Classification

At baseline, `git diff master` was empty for `ControlAnimation.java` and
`FadeAnimation.java`, proving the skipped slave initialization, root-dependent
cleanup, and update-screen leak were inherited. `Control.java` differed from
master by 121 insertions and 41 deletions; its scale-1 snapshots and mixed
coordinate comparison were therefore handled as branch-exposed infrastructure.

The pre-fix suite proved:

- composite FadeAnimation did not create an offscreen image;
- completion cleared caller-owned `offscreen` and unrelated `offscreen0`;
- immediate abort left `Control.enableUpdateScreen` false;
- a nested target at nonzero ancestor offsets lost its child pixel;
- content scale 3 allocated a 12-pixel rather than 36-pixel backing;
- transparent-root untouched pixels were already correct and stayed unchanged.

## Completed Implementation

`ControlAnimation.start()` now initializes every composite member, while the
root alone registers an update listener. It saves `Control.enableUpdateScreen`,
suppresses updates only during snapshot creation, and restores the exact prior
value in `finally`. A protected startup hook lets `FadeAnimation` write its
initial alpha immediately after the snapshot exists.

Each animation stores the exact `Image` it created. Cleanup removes only that
identity from `Control.offscreen`, only when `releaseScreenShot` is true, and
never clears `offscreen0`. Root abort propagates to composite members so a fade
cannot retain owned state. Partial fades transfer their retained snapshot to the
control for reuse. `FadeAnimation.animate()` writes its final alpha before
stopping, which fixes partial fade-in/out without changing Toast.

`Control.takeScreenShot()` now uses
`Image.createLogical(width, height, gfx.getContentScale())` and configures the
image graphics with the target content and font scales. Descendant traversal no
longer compares a parent-relative root rectangle with absolute child rectangles;
the image clip and target-local `refreshGraphics` offsets bound the capture.
Transparent roots remain unfilled. A frame outside the live safe viewport is
therefore captured completely and clipped only when painted live.

`PresentationHandle.frame.transparentBackground` is set before transitions can
capture it. Direct fade and slide-plus-fade continue to target that same frame.
Relayout aborts and stabilizes at final geometry; dismissal while entering is
ignored to preserve non-reversing legacy behavior.

`PresentationEntry` carries barrier color and alpha independently. TopMenu maps
legacy brightness to a black overlay using:

    barrierAlpha = fadeOtherWindows
        ? 255 - clamp(Window.fadeValue, 0, 255)
        : 0

`PresentationBarrier` uses the existing translucent-control drawing path and
fills the entire `PresentationHost`; it never calls `Graphics.fadeScreen`.
Input interception remains independent from visual transparency. `Window.java`
has no diff from baseline, so real Window fade behavior is unchanged.

## Focused Tests and Consumer Audit

`ControlAnimationFadeTest` covers standalone and composite nonzero fades,
immediate abort, caller-owned `offscreen`, unrelated `offscreen0`, and retained
partial fade reuse. `ControlScreenshotTest` covers target-local descendants,
transparent untouched pixels, and scale-3 content/font destinations.

`PresentationFadeTest` drives real 100 ms transitions through intermediate and
final frames. It covers directional TopMenu slide-plus-fade, centered direct
fade, SideMenu's opaque drawer plus dim barrier, brightness at `fadeValue` 255,
128, and 0, relayout abort, ignored dismissal during entry, and a fresh second
presentation. Test cleanup restores update state, fade globals, screen geometry,
safe insets, `Window.topMost`, and the original z-stack.

The consumer audit found three ownership patterns. TopMenu, SlidingWindow, and
VirtualKeyboard use animation-owned composite snapshots. TabbedContainer and
ScrollContainer explicitly create and release caller-owned snapshots. Toast
sets `FadeAnimation.maxFade < 255` and reuses the retained image for fade-out.
The shared contract covers all three, so none of those consumers changed.

## Concrete Validation

From `TotalCrossSDK`, the final focused command is:

    ./gradlew-agent test \
      --tests 'totalcross.ui.anim.ControlAnimationFadeTest' \
      --tests 'totalcross.ui.ControlScreenshotTest' \
      --tests 'totalcross.ui.PresentationFadeTest' \
      --tests 'totalcross.ui.PresentationHostTest' \
      --tests 'totalcross.ui.TopMenuSafeAreaTest' \
      --tests 'totalcross.ui.SideMenuPresentationTest' \
      --tests 'totalcross.ui.SlidingWindowPresentationTest' \
      --tests 'totalcross.ui.gfx.GraphicsScaleTest' \
      --tests 'totalcross.ui.LogicalTextScaleTest' \
      --no-daemon --console=plain

It passed; summary log is `artifacts/fade-presentation/logs/final-focused-tests.log`
and wrapper log is `TotalCrossSDK/agent-logs/20260808-005713-test-full.log`.

The non-clean SDK build and smoke compilation passed:

    ./gradlew-agent dist -x test --no-daemon --console=plain
    ./gradlew-agent smokeTestClasses --no-daemon --console=plain

Logs are `artifacts/fade-presentation/logs/sdk-dist.log` and
`artifacts/fade-presentation/logs/smoke-compile.log`.

`PresentationFadeSmoke` ran through the JavaSE launcher and a jar deployed only
for macOS. The existing `build-ui-presentation-safe-area/libtcvm.dylib` path was
reused; native code was not rebuilt because this repair is Java-only. Both runs
reported:

    compositeFadeStarted=true
    compositeFadeCompleted=true
    fadeOtherWindowsBarrier=true
    updateScreenEnabled=true
    offscreenReleased=true
    final=PASS

Logs are `artifacts/fade-presentation/logs/javase-smoke.log`,
`native-deploy.log`, and `native-macos-smoke.log`. Android and iOS were not
built or deployed because focused Java and macOS behavior showed no discrepancy.

## Validation and Acceptance

Acceptance requires all of the following, and the named tests/smokes above prove
them: intermediate and final alpha for standalone and composite fades; movement
and alpha in the same nonzero frame; target-local and complete offscreen capture;
logical scale-3 backing with retained font scale; transparent frame pixels;
abort and relayout cleanup; independent SideMenu self-fade and background dim;
legacy-equivalent brightness; repeated fresh state; unchanged real Window fade;
green safe-area, clipping, z-stack, graphics-scale, and logical-text regressions;
a non-clean SDK build; JavaSE and macOS native completion without freeze.

## Validation Evidence and Commits

Local commits, in order, are:

    1d1957c56  test(ui): cover fade animation lifecycle
    8669e03cd  fix(ui): correct composite fade lifecycle
    d9cc9e9f5  fix(ui): preserve fade snapshot geometry and scale
    87ec52e21  fix(ui): restore presentation background dimming
    d3a4950f9  test(ui): cover presentation fade behavior
    95972a66c  fix(ui): preserve final partial fade alpha
    982971481  test(ui): add fade presentation smoke coverage

No commit was pushed and no remote state was modified.

## Surprises & Discoveries

- The actual branch is `feat/logical-ui-scaling2`, not the unsuffixed name in the request. Work continued on the current checkout without switching or resetting.
- The transparent-root screenshot guard passed before the fix; the repair preserved it rather than broadening background fill behavior.
- The retained Toast-style partial fade found a second inherited ordering bug: the animation's internal alpha reached 128 only after the image had been left at 88.
- JavaSE pixel blending mapped black overlay alpha 127 over white to brightness 128, confirming the algebraic replacement for legacy `Graphics.fadeScreen(128)`.
- The existing native macOS build/deploy path remained usable, so rebuilding native code would have added cost without evidence value.

## Decision Log

- Decision: Keep screenshot-based FadeAnimation and public APIs. Rationale: renderer-level subtree opacity is outside this repair. Date/Author: 2026-08-08 / plan author.
- Decision: Fix shared lifecycle before presentation behavior. Rationale: the core implementation is unchanged from master. Date/Author: 2026-08-08 / plan author.
- Decision: Track the exact owned `Image`. Rationale: identity prevents clearing a caller replacement or `offscreen0` without new public API. Date/Author: 2026-08-08 / Codex.
- Decision: Bound update suppression with `finally`. Rationale: restoration cannot depend on a future tick or successful allocation. Date/Author: 2026-08-08 / plan author.
- Decision: Use destination content/font scales and target-local traversal. Rationale: snapshot rendering must match live logical rendering and ignore live ancestor position. Date/Author: 2026-08-08 / plan author.
- Decision: Use black barrier alpha `255 - clamp(Window.fadeValue)`. Rationale: it is equivalent to legacy brightness without mutating the rendered screen. Date/Author: 2026-08-08 / plan author.
- Decision: Ignore dismissal during entry and allow relayout to abort/stabilize. Rationale: preserve legacy non-reversing semantics and the prior safe-area decision. Date/Author: 2026-08-08 / Codex.
- Decision: Leave real Window fade and all audited consumers unchanged. Rationale: the shared ownership contract fixes the evidence without consumer workarounds. Date/Author: 2026-08-08 / Codex.

## Idempotence and Recovery

No destructive Git operations, clean build, branch switch, push, publish, or
remote mutation is part of this work. Validation is repeatable. Generated smoke
output is isolated under `TotalCrossSDK/build/presentation-fade-smoke` and logs
under `artifacts/fade-presentation/logs`. Unrelated tracked and untracked work
present at baseline remains untouched.

## Outcomes & Retrospective

The inherited composite lifecycle, ownership, abort, and partial-final-alpha
defects are corrected independently of PresentationHost. Branch-exposed
screenshot geometry/scaling, transparent presentation frames, and missing
barrier dimming are corrected on top. Deterministic JavaSE pixels and real
nonzero transition assertions cover visual behavior; JavaSE and native macOS
smoke cover event-driven completion, cleanup, and successful reuse.

No legacy consumer required modification. Android/iOS validation remains
intentionally skipped because no Java/native discrepancy justified it. The only
remaining limitation is platform breadth: Android and iOS were not built, by
design. The focused validator passed all 14 changed plan/source/test files with
no stale audit, and the largest new file is this plan at 14,838 bytes and 255
lines. Scoped `git diff --check` passed, and `Window.java` matches the baseline.

## Revision Note

2026-08-08: Initial plan separated generic fade/screenshot repair from
PresentationHost parity work.

2026-08-08 04:02Z: Compacted the completed living plan below the requested
new-file size limit while preserving baseline classification, implementation,
validation commands and logs, decisions, commits, deviations, and remaining
final-audit work.

2026-08-08 04:03Z: Completed final header/static/size checks, recorded the smoke
commit, corrected progress timestamps to UTC, and closed the retrospective.
