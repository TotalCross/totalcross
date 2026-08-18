<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Safe presentation implementation design

This file supports `.agent/ui-presentation-safe-area-execplan.md`. Read it when
implementing Milestones 1-4. It is implementation guidance, not a public API
proposal. Keep all names package-private unless an existing public class must
expose a member to preserve its own current API.

## Clipping and ClippedContainer

Current clipping is implemented in `Control.refreshGraphics`: it accumulates
ancestor translation and intersects the child graphics rectangle with each
ancestor container before calling `Graphics.refresh`. Preserve this as the
default.

Add a small internal clipping policy to `Container`, conceptually:

    private boolean clipChildrenToBounds = true;

    boolean clipsChildrenToBounds() {
      return clipChildrenToBounds;
    }

    void setClipChildrenToBounds(boolean clip) {
      clipChildrenToBounds = clip;
    }

The exact member names may follow local conventions. Do not expose a public API
unless a current caller requires it.

Refactor only the ancestor intersection portion of `Control.refreshGraphics`.
For every ancestor, always accumulate x/y translation exactly as today. Perform
the "before" and "after" rectangle intersection only when that ancestor's
clipping policy is enabled. Preserve `expand`, `topParent`, offscreen rendering,
content/font scales, and final `Graphics.refresh` arguments.

Default-enabled clipping must make existing controls render identically before
and after this change. Add a focused test with nested containers and a child
whose drawing extends beyond an ancestor. Prove default clipping prevents pixels
outside that ancestor. Then disable clipping on one intermediate ancestor and
prove drawing may cross it but remains clipped by the next enabled ancestor.
Keep this test deterministic and small.

Correct `ClippedContainer.findOneVisible` as an ordinary search over
`[ini, end)`. It should return an intersecting index or `-1`; it must not use zero
as a not-found sentinel. A straightforward implementation can use:

    low = ini
    high = end - 1

and compare each midpoint child's vertical interval against `[y0, yf]`. Do not
change the assumption that the searched items are vertically ordered.

Initialize:

    protected int lastMid = -1;

In `paintChildren`, handle `n == 0` and `first < 0` by setting `lastMid = -1`
and returning without painting. When children are painted, keep the existing
backward scan to locate the first visible sibling and forward scan until the
visible run ends. Compute a cached midpoint only from a valid painted range.

Do not rename `ClippedContainer`, change `verticalOnly`, replace `tabOrder`, or
redesign scrolling/list virtualization in this plan.

## Internal presentation foundation

Place the new internal foundation in:

    TotalCrossSDK/src/main/java/totalcross/ui/

Keep it in the `totalcross.ui` package so it can use package-private integration
without turning provisional names into public SDK API.

Prefer responsibility-sized files equivalent to:

    PresentationHost.java
    PresentationEntry.java
    PresentationHandle.java
    PresentationController.java
    PresentationTransition.java
    SlidePresentationTransition.java

A tiny `PresentationViewport` or `PresentationFrame` can be a separate
package-private file or a nested type. Check every new file against the
20 KiB/~600-line limit before committing.

### PresentationEntry

`PresentationEntry` is immutable configuration for one presentation. It should
describe:

    enum Layer { ROUTE, OVERLAY }

    Control content
    Layer layer
    BoundsResolver bounds
    PresentationTransition transition
    boolean blocksInput
    boolean dismissOnOutsidePress
    boolean dismissOnBack
    int barrierColor or equivalent alpha/color state

`BoundsResolver` is internal and receives the safe viewport rectangle in local
coordinates, writing the final frame rectangle. It must not read
`Settings.screenWidth` or `Settings.screenHeight`.

The foundation does not need public route names, restoration IDs, navigation
arguments, scaffold slots, or generalized overlay anchoring.

### Window integration and PresentationHost

Add a package-private field/getter to `Window` so each real Window lazily owns at
most one `PresentationHost`. Do not change Window popup behavior for real
windows.

The host is added as a direct child of the owner and configured with:

    SafeAreaLayout.FULL_BLEED

It must be brought to front whenever a presentation is added. It does not enter
the real Window z-stack.

The host can have two internal layers to keep future direction clear:

    routeLayer
    overlayLayer

Current `SlidingWindow` uses route-like presentation. Current `TopMenu` uses
overlay-like presentation. Avoid public APIs around these layers.

When presenting, resolve:

    Rect safe = owner.getClientRect();

The safe rectangle is owner-local. Convert it to host-local coordinates if the
host itself begins at a nonzero base client origin. Do not assume `(0, 0)` for
window border/title cases.

For each active entry create an entry host that fills the presentation host. Its
children are ordered:

    barrier
    presentationViewport

The barrier always fills the entry host. It can be visually transparent while
still receiving input. For outside-dismiss overlays, a press on the barrier
dismisses only the top entry that owns it.

`presentationViewport` is a normal `Container` with child clipping explicitly
enabled. Its bounds equal the safe rectangle. It contains one
`PresentationFrame`.

### PresentationFrame

The frame receives final layout before animation:

    frame.setRect(finalX, finalY, finalWidth, finalHeight)

Then add/preserve the presented content inside the frame according to its normal
layout. Do not lay out provider content at an offscreen coordinate.

For slide animation, move only the frame's transient `x` and `y`. Reuse
`PathAnimation.SetPosition` or an equivalent small callback:

    setPos(x, y) {
      frame.x = x;
      frame.y = y;
      Window.needsPaint = true;
    }

Do not call the frame/content declarative `setRect(KEEP, KEEP, ...)` on each
animation tick. Do not modify recorded `setX/setY` anchors merely to animate.

At animation completion, the frame must have its resolved final bounds and no
temporary animation state that would confuse later resize/reposition.

### PresentationTransition

`PresentationTransition` should create or run enter/exit animation from the
viewport and final frame geometry. It must use only local values.

For a full-safe-viewport frame whose final origin is `(0, 0)`:

    BOTTOM enter: (0, viewport.height) -> (0, 0)
    TOP enter:    (0, -frame.height)   -> (0, 0)
    RIGHT enter:  (viewport.width, 0)  -> (0, 0)
    LEFT enter:   (-frame.width, 0)    -> (0, 0)

For non-full frames such as TopMenu drawers, keep the orthogonal final coordinate
and derive only the animated axis from the viewport edge. Exit reverses the
path.

A centered/fade presentation can use `FadeAnimation` without screen-relative
positioning.

Do not use the directional `PathAnimation.create(control, direction, ...)`
overload inside the new foundation. The earlier PathAnimation correction remains
for legacy callers; the foundation should use explicit local coordinates so safe
area is not inferred by the animation utility.

### PresentationHandle

A handle represents one active entry and knows whether it is presenting,
presented, dismissing, or dismissed. It owns the entry host/frame references and
the current animation.

`dismiss()` must be idempotent. If dismissal begins while enter animation is
active, either stop/replace that animation in a deterministic way or refuse the
second transition until the first completes; choose the smallest behavior that
matches existing `SlidingWindow`/`TopMenu` expectations and record it.

On final dismissal remove the entry host from its layer and release references
that would retain provider views.

### PresentationController and legacy lifecycle

`SlidingWindow` and `TopMenu` should each own a `PresentationController`.
The controller resolves the current real owner window, creates the entry, asks
the owner's host to present it, and maps legacy lifecycle methods to the handle.

Owner selection should preserve current popup intent: use the current real
top-most Window when available, otherwise `MainWindow.getMainWindow()`. The
presented control itself must never become `Window.topMost`.

The controller supports both:

    popupNonBlocking()
    popup()

`popupNonBlocking()` presents and returns. `popup()` presents and pumps the
existing event queue until the handle is dismissed, preserving the legacy
blocking call shape without using the Window popup/z-stack mechanism.

`unpop()` starts entry dismissal. Repeated unpop is safe.

Preserve the current class-specific observable ordering of `onPopup`,
`postPopup`, animation, `onUnpop`, and `postUnpop` as closely as practical. Add
a focused lifecycle assertion if local subclasses/tests depend on it. Do not
invent a general public lifecycle contract.

The host owns Back/Escape dispatch for the top dismissible presentation. Save and
restore focus only if tests show the underlying focused control continues to
receive keyboard interaction incorrectly. Keep focus work local to the host;
general `FocusScope` design is out of scope.

### Resize and safe-area changes

When the owner Window is repositioned or its safe-area client rectangle changes,
the host must update its own bounds and recalculate each entry viewport.

If an entry is stable, recalculate final frame bounds and place it at the new
final location. Do not recreate provider content.

If a resize occurs during an animation, prefer ending the current animation at a
valid final geometry and relayouting rather than trying to preserve exact
fractional progress across old/new viewport dimensions. Record this decision if
the local animation framework makes another simple behavior safer.

## SlidingWindow and MaterialWindow migration

Change `SlidingWindow` away from top-level Window presentation. Prefer:

    public class SlidingWindow extends Container
        implements PenListener, KeyListener

Do not add a new public presentation superclass solely for sharing code.
Composition through `PresentationController` keeps the foundation internal.

Preserve the class's own current API and behavior:

    Presenter<Container> provider
    delayInitUI
    animDir
    slackSpace
    totalTime
    delayedUiSpinner
    popup()
    popupNonBlocking()
    unpop()
    getSlackSpace()
    setSlackSpace()
    drag-to-dismiss
    Back/Escape dismissal

Remove screen staging such as `(100000, 100000)`, `SCREENSIZE` presentation
bounds, and animation code that depends on physical screen dimensions.

Create a route-like entry whose safe viewport is the owner client rectangle and
whose normal frame fills that viewport. Preserve slack as a destination offset
relative to the safe viewport. For example, a bottom presentation may end at
`y = slackSpace`; the enter origin is still the safe viewport's bottom edge.

Keep provider layout at final local bounds. For the non-delayed path:

    add(provider.getView(), LEFT, TOP, FILL, FILL)

For delayed UI, keep background provider creation and main-thread insertion, but
do not trigger whole-owner relayout and do not use window staging coordinates.

`screenResized()` can become a compatibility method that requests host/frame
relayout; presentation geometry is owned by the host.

`MaterialWindow` continues to inherit `SlidingWindow`. Keep its title/back button
behavior and provider placement. Make Bar width explicit:

    add(bar, LEFT, TOP, FILL, PREFERRED);

Keep provider content below it with `FILL`.

Unit tests should disable or shorten animation deterministically. Assert all four
directions use safe viewport edges, z-stack is unchanged, slack remains relative
to the viewport, and provider content is not recreated by relayout.

## TopMenu and SideMenuContainer migration

Change `TopMenu` away from top-level Window presentation. Prefer direct
`Container` inheritance plus `PresentationController`.

Preserve TopMenu's own current behavior: constructors, item selection,
`autoClose`, direction, duration, `percWidth`, `widthInPixels`, background image,
scroll insets, separators, elevation intent, fixed top/bottom bars, scroll-under
modes, header, animation listener, popup/unpop methods, and selected index.

### TopMenu compatibility state previously inherited from Window

`SideMenuContainer` and TopMenu currently rely on a small subset of Window state.
Move only the required TopMenu-specific compatibility state into TopMenu itself,
including title text, title gap/alignment/color intent, `canDrag`,
`fadeOtherWindows`, and constructor border-style intent where local compilation
shows they are required.

Do not reproduce the whole Window API and do not copy the large Window
title-painting implementation.

For TopMenu's title/border, use existing Container/Label/border primitives.
Preserve the current caller-visible intent for `Window.NO_BORDER`,
`Window.RECT_BORDER`, and `Window.ROUND_BORDER` values accepted by TopMenu
constructors. The constants can still be supplied by callers; TopMenu no longer
needs to inherit Window to interpret the byte value.

If a current repository test/sample requires another TopMenu border mode, add
only that narrow mapping and record it.

### TopMenu geometry

Resolve explicit width first:

    widthInPixels != 0

Otherwise derive percentage width from `viewport.width`, not screen width.

For left/right menus, the final height normally fills safe viewport height. For
top/bottom menus, resolve their height from viewport height and item content,
preserving the current cap intent but replacing physical screen height with
viewport height.

The transition origin uses the safe viewport edge. A left menu begins at
`-frame.width`; a right menu begins at `viewport.width`; top/bottom follow the
same local rule.

Because the entire TopMenu is already inside the safe viewport, remove internal
safe-area compensation such as:

    getAttachedSafeAreaEdges()
    getBarPaddingEdges()
    adding Window.getSafeAreaInsets() to bar heights
    safe-area padding hosts created solely for the old Window model

Keep bar layout modes and scroll-under modes. A fixed bar's height is its own
preferred height. `layoutMenu()` works in TopMenu-local client coordinates and
applies only TopMenu's declared `scInsets`, title/border client area, and
fixed-bar policy.

`TopMenu.Item` must find its nearest TopMenu ancestor instead of casting
`getParentWindow()`.

`SideMenuContainer.Sub` expand/collapse must relayout its containing TopMenu (or
nearest relevant container), not call `getParentWindow().reposition()`.

### SideMenuContainer

Keep:

    public TopMenu topMenu
    open()
    close()
    setTopBar()/getTopBar()
    setBottomBar()/getBottomBar()
    setScrollUnderMode()
    item/sub APIs
    Bar + content composition

Register SideMenu's pen listener on the SideMenu/container path rather than
calling:

    content.getParentWindow().addPenListener(this)

Do not set parent Window `callListenersOnAllTargets` just for SideMenu.

Do not freeze drawer width from `Settings.screenWidth` in the constructor.
Introduce only an internal TopMenu sizing policy needed by SideMenu so the
drawer rule is evaluated when the safe viewport is known:

    min(320, viewport.width - 56)

An explicit user-supplied `topMenu.widthInPixels` must continue to take
precedence. Top/bottom SideMenu directions continue to request full safe viewport
width.

Rewrite `TopMenuSafeAreaTest` to assert single safe-area consumption. The menu's
local bars should start at local safe viewport coordinates, not at global
`Window.getSafeAreaInsets()` offsets. Dynamic safe-area changes should resize the
presentation viewport while retaining the same body scroller/provider objects.

Test outside barrier dismissal and ensure it does not depend on Window's
`onClickedOutside`.

## Smoke design and execution boundary

Do not add platform-specific smoke machinery before implementation completion.

If a reusable smoke path exists, add a compact:

    TotalCrossSDK/src/smokeTest/java/totalcross/ui/PresentationSafeAreaSmoke.java

The fixture should emit concise machine-readable assertions such as:

    ownerWindowUnchanged=true
    zStackDelta=0
    safeViewport=x,y,w,h
    slidingFinal=x,y,w,h
    topMenuFinal=x,y,w,h
    clippingPass=true
    final=PASS

For JavaSE/simulator, prefer deterministic nonzero safe insets through existing
Launcher arguments or a test-only package-level update path already used by
safe-area tests.

For native macOS, reuse the repository's existing deploy/run pattern if it can
be done with a small extension. Build/deploy/run only after Milestones 1-4 are
complete. Launch the deployed executable directly, capture exit status and log,
and do not label JavaSE output as native proof.

Do not execute Android or iOS builds or deployments in this plan.

## Focused validation commands

During clipping work:

    cd TotalCrossSDK
    ./gradlew-agent test --tests 'totalcross.ui.ContainerClippingTest'
    ./gradlew-agent test --tests 'totalcross.ui.ClippedContainerTest'

During host work:

    ./gradlew-agent test --tests 'totalcross.ui.PresentationHostTest'
    ./gradlew-agent test --tests 'totalcross.ui.SafeAreaLayoutTest'

During SlidingWindow work:

    ./gradlew-agent test --tests 'totalcross.ui.SlidingWindowPresentationTest'

During TopMenu/SideMenu work:

    ./gradlew-agent test --tests 'totalcross.ui.TopMenuSafeAreaTest'

Add a SideMenu-specific test command only if a separate test file is created.

Redirect verbose output to
`artifacts/ui-presentation-safe-area/logs/` and inspect concise tails/errors.
Do not run `clean` unless stale outputs are demonstrated. Do not run `dist` until
all implementation milestones are complete.

## Commit discipline

Make commits after coherent, focused validation. Prefer this sequence, adjusting
only when local implementation makes two adjacent slices inseparable:

    docs(sdk): add safe presentation exec plan
    refactor(sdk): make container child clipping explicit
    fix(sdk): correct clipped container visibility search
    refactor(sdk): add internal presentation host foundation
    refactor(sdk): present sliding windows inside safe viewport
    refactor(sdk): present top menu as safe overlay
    refactor(sdk): detach side menu from window presentation
    test(sdk): add safe presentation regression coverage

Before each commit inspect scoped diffs, stage only relevant files, and update
the state file. Never push.
