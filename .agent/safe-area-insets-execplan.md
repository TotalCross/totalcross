<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Implement dynamic safe-area layout and menu insets

This ExecPlan is a living document; keep its progress and decision sections current.

This plan follows `AGENTS.md`, `.agent/PLANS.md`, and the resumable-plan policy in
`TotalCross/totalcross-depot-tools/.agent/PLANS.md`. Execute it autonomously without asking for routine next steps. Stop only to avoid
risking unrelated work or data.

## Purpose / Big Picture

A default `MainWindow` will keep normal controls inside the current safe area
without application code reacting to rotation or system-bar changes. Applications
can still use full-bleed controls, add safe area as internal padding, or cancel the
default protection with negative window insets.

`ScrollContainer` will gain content insets that preserve viewport size. Menus will
use them for:

- ChatGPT: scroll behind the top bar, reserve the bottom bar;
- Reddit: reserve both bars;
- Gmail: reserve the top bar, scroll behind the bottom bar.

Android and iOS will push safe-area changes through a dedicated internal API. A
safe-area change triggers layout and repaint, not a graphics-surface resize.

Panel-shape and clipping work is out of scope; physical display cutouts remain a
safe-area input.

## Working Set and Resume Protocol

Base the work on `feat/logical-ui-scaling`.

Maintain:

- `.agent/safe-area-insets-execplan.md` — active plan;
- `.agent/state/safe-area-insets.md` — first read when resuming; rewrite it;
- `.agent/evidence/safe-area-insets.md` — append-only compact evidence;
- `.agent/archive/safe-area-insets-history.md` — retired milestone detail;
- `.agent/reports/safe-area-insets-editorial.md` — final factual report.

The state file records the active slice, protected changes, completed validation,
next exact action, blockers, and a resume command.

On first execution, read `AGENTS.md`, `.agent/PLANS.md`, this plan, and only the
paths named below. On resume, read state first and only the active milestone.
Avoid rereading plans, broad diffs, or old logs.

Store verbose logs under `build/safe-area-insets/`; report only status, relevant
errors, and a short tail.

Keep every new file within 20 KB and about 600 lines; split before either limit.
Do not resize existing files through unrelated refactoring.

## Progress

- [x] (2026-08-05T05:14:00Z) Author this plan from the current architecture and
  agreed API model.
- [x] (2026-08-05T05:42:31Z) Milestone 0: recorded the scoped baseline without
  building; initialized state, evidence, history, and editorial files.
- [x] (2026-08-05T05:48:20Z) Milestone 1: implemented the core SDK safe-area
  model and passed all 7 focused layout tests.
- [x] (2026-08-05T05:53:20Z) Milestone 2: implemented scroll content insets
  with preserved anchors and passed all 5 focused tests.
- [ ] Milestone 3: adapt menus and run the only SDK checkpoint build.
- [ ] Milestone 4: implement Android/iOS updates and run the only Android build.
- [ ] Milestone 5: final focused validation, optional smoke tests, and reporting.

## Current Architecture and Scope

`Container.java` stores one protected `Insets insets`; `getClientRect` and some
resize paths read it directly. `Control.java` normally positions through
`parent.getClientRect`; legacy `ignoreInsets` instead ignores all parent insets.
The new full-bleed behavior must ignore only safe-area exclusion.

`Window.java` owns a shared `safeAreaInsets`. Deployed code currently queries the
platform on demand. Preserve `getSafeAreaInsets()` and launcher portrait/landscape
presets, but make pushed values the steady-state source of truth.

`ScrollContainer.java` has a clipped viewport (`bag0`) and scrolling content
(`bag`), but no separate content inset. Its ordinary insets affect the viewport.

`TopMenu.java` has one `ScrollContainer`; its public `header` scrolls with content
and `scInsets` is a manual offset. Preserve both. `SideMenuContainer.java` creates
and opens the `TopMenu` and must expose the new fixed-bar configuration.

Android input is around `Launcher4A.java` and `android/Window_c.h`. iOS reports
from `mainview.m`; `event/darwin/event.m` transfers events to the VM thread.

Store safe-area values in logical units; platform callbacks may provide physical
units.

In scope are only four public concepts, their platform delivery, and menu
integration:

1. `Window.safeAreaMode`;
2. `Control.safeAreaLayout`;
3. `Container.safeAreaPaddingEdges`;
4. `ScrollContainer.contentInsets`.

IME insets, gesture exclusion, bar colors/visibility, CSS parsing, panel shapes,
packaging, broad layout rewrites, and unrelated refactors are out of scope.

## Interfaces and Behavioral Contracts

Create under `TotalCrossSDK/src/main/java/totalcross/ui/`:

    public enum SafeAreaMode { AUTO, ENABLED, DISABLED }
    public enum SafeAreaLayout { INHERIT, SAFE, FULL_BLEED }

    public final class SafeAreaEdges {
        public static final int NONE = 0;
        public static final int LEFT = 1;
        public static final int TOP = 2;
        public static final int RIGHT = 4;
        public static final int BOTTOM = 8;
        public static final int ALL = LEFT | TOP | RIGHT | BOTTOM;
    }

Reject unknown edge bits through one package-visible helper.

Add to `Window`:

    public void setSafeAreaMode(SafeAreaMode mode)
    public SafeAreaMode getSafeAreaMode()
    public void setSafeAreaEdges(int edges)
    public int getSafeAreaEdges()
    static boolean _updateSafeAreaInsets(int top, int left, int bottom, int right)
    protected void safeAreaInsetsChanged(Insets previous, Insets current)

`AUTO` protects `MainWindow` and touched screen edges. `ENABLED` applies selected
edges; `DISABLED` applies none. `TopMenu` distributes safe area internally.

`Window.getInsets` continues returning declared container/border insets. Safe-area
updates never overwrite them. Effective values are additive and are not clamped;
negative user insets can cancel safe-area values.

Add to `Control`:

    public void setSafeAreaLayout(SafeAreaLayout layout)
    public SafeAreaLayout getSafeAreaLayout()

`INHERIT` follows the parent window. `SAFE` forces the safe client rectangle.
`FULL_BLEED` uses the base client rectangle while still respecting border and
user insets. It is not `ignoreInsets`.

Add to `Container`:

    public void setSafeAreaPaddingEdges(int edges)
    public int getSafeAreaPaddingEdges()
    protected void getEffectiveInsets(Insets copyInto)

Safe-area padding adds only unconsumed selected edges; a safe-positioned
container must not receive the same edge twice.

Add to `ScrollContainer`:

    public void setContentInsets(int left, int right, int top, int bottom)
    public void getContentInsets(Insets copyInto)

Content insets are non-negative. They change scrollable extent, not viewport size.
Changing them preserves the visible anchor; a bottom-anchored container remains
bottom-anchored.

Add to `TopMenu`:

    public enum BarLayoutMode { RESERVE_SPACE, OVERLAY }
    public enum ScrollUnderMode { NONE, TOP, BOTTOM, BOTH }

    public void setTopBar(Control bar)
    public Control getTopBar()
    public void setBottomBar(Control bar)
    public Control getBottomBar()
    public void setTopBarLayoutMode(BarLayoutMode mode)
    public void setBottomBarLayoutMode(BarLayoutMode mode)
    public void setScrollUnderMode(ScrollUnderMode mode)

`RESERVE_SPACE` removes the full bar height, including safe-area padding, from the
viewport. `OVERLAY` keeps the full viewport and adds that height to the matching
content inset. Keep the legacy scrolling `header` unchanged.

Add forwarding methods to `SideMenuContainer`. The presets are `TOP` for ChatGPT,
`NONE` for Reddit, `BOTTOM` for Gmail, and `BOTH` for the same mechanism on both
edges.

## Plan of Work

### Milestone 0: Baseline and state

Do not build, switch branches, reset files, delete caches, or edit generated JNI
headers.

From the repository root:

    git branch --show-current
    git status --short -- \
      .agent \
      TotalCrossSDK/src/main/java/totalcross/ui \
      TotalCrossSDK/src/test/java/totalcross/ui \
      TotalCrossVM/android/app/src/main/java/totalcross \
      TotalCrossVM/src/event \
      TotalCrossVM/src/nm/ui

Record unrelated changes as protected. Run one targeted inventory:

    rg -n \
      "getSafeAreaInsets|safeInsets|viewSafeAreaInsetsDidChange|ignoreInsets|\
getClientRect|class ScrollContainer|class TopMenu|class SideMenuContainer" \
      TotalCrossSDK/src/main/java/totalcross/ui \
      TotalCrossVM/android/app/src/main/java/totalcross \
      TotalCrossVM/src/event TotalCrossVM/src/nm/ui

Confirm logical scaling and the current surface lifecycle are present. Adapt names
to the actual working tree without reintroducing old sentinel lifecycle commands.
Create state/evidence files and run `git diff --check` only.

### Milestone 1: Core SDK model

Create the three public types with Javadocs defining logical units and defaults.

In `Control.java`, default to `INHERIT`. Change the central parent-client-rectangle
selection so direct children can request safe or full-bleed bounds without using
`ignoreInsets`.

In `Container.java`, combine declared insets and optional safe-area padding in
`getEffectiveInsets`. Update `getClientRect` and only direct inset reads that must
observe effective values. Avoid unrelated cleanup.

In `Window.java`, derive base and safe client rectangles. Resolve `INHERIT`,
`SAFE`, and `FULL_BLEED` for child placement.

Make `_updateSafeAreaInsets` the single SDK transition. It compares all values,
returns `false` when unchanged, snapshots old/new values, updates the shared cache
on the VM thread, notifies active windows once, repositions, and repaints. It must
not call screen-resize or graphics-recreation paths.

Add `SafeAreaLayoutTest.java` covering modes, edge selection, full bleed, declared
inset preservation, negative cancellation, unchanged deduplication, and one
callback for a real change.

Run only:

    cd TotalCrossSDK
    ./gradlew-agent test --tests 'totalcross.ui.SafeAreaLayoutTest'

Do not run an SDK distribution build yet.

### Milestone 2: Scroll content insets

Add a private `Insets contentInsets` to `ScrollContainer`. Keep viewport,
scrollbars, and `getClientRect` based on existing control bounds.

Apply content insets to the scrolling coordinate system: left/top precede the
first child; right/bottom extend the maximum; resize and scrollbar calculations
count them once. Preserve position when values change. Preserve the visible item
when not edge-anchored and the bottom edge when bottom-anchored.

Add `ScrollContainerContentInsetsTest.java` covering unchanged viewport, changed
content extent, reachable first/last content, idempotence, negative-value
rejection, and bottom anchoring.

Run only:

    cd TotalCrossSDK
    ./gradlew-agent test --tests \
      'totalcross.ui.ScrollContainerContentInsetsTest'

Do not run a distribution build yet.

### Milestone 3: Menu integration

Refactor only `TopMenu` internal layout. Preserve animation, selection, auto-close,
constructors, `header`, and `scInsets`.

Add one body scroller and optional fixed top/bottom controls. Add the body first so
bars paint above overlay content. Set the menu window to `SafeAreaMode.DISABLED`.
Derive attached edges from `animDir`: left uses top/left/bottom; right uses
top/right/bottom; top uses top/left/right; bottom uses bottom/left/right.

Fixed bars use full-bleed bounds plus safe-area padding on attached edges.
`RESERVE_SPACE` shrinks the body viewport. `OVERLAY` leaves it full and updates
content insets. Recalculate on bounds or safe-area changes without recreating items
or losing scroll position.

Keep `header` scrolling. Forward fixed bars and `ScrollUnderMode` through
`SideMenuContainer`. Preserve its title behavior; a small package-private title bar
may be added if needed, within the new-file limit.

Add `TopMenuSafeAreaTest.java` for attached edges, bar/viewport bounds, content
insets, dynamic safe-area changes, and all four scroll-under modes. Add a separate
forwarding test only if needed and keep each test below the size limit.

Run focused tests and then the only SDK checkpoint build:

    cd TotalCrossSDK
    ./gradlew-agent test --tests 'totalcross.ui.SafeAreaLayoutTest' \
      --tests 'totalcross.ui.ScrollContainerContentInsetsTest' \
      --tests 'totalcross.ui.TopMenuSafeAreaTest'

    mkdir -p ../build/safe-area-insets
    ./gradlew-agent dist -x test --warning-mode=none --console=plain \
      > ../build/safe-area-insets/sdk-dist.log 2>&1
    status=$?; tail -80 ../build/safe-area-insets/sdk-dist.log; exit $status

Do not run `clean`, packaging, full tests, or another SDK build for later
comments/documentation changes.

### Milestone 4: Dynamic Android and iOS updates

Add one common native helper near the Window native contract:

    void windowUpdateSafeAreaInsetsPhysical(
        Context currentContext,
        int32 top,
        int32 left,
        int32 bottom,
        int32 right);

It converts physical values using current content scale and invokes
`Window._updateSafeAreaInsets(int,int,int,int)` on the VM event thread. Keep
`tuW_getSafeAreaInsets` source-compatible, but use the SDK cache after initial
startup.

On Android, install or reuse a `WindowInsetsCompat` listener on the actual root.
Read system bars plus physical display cutout, exclude IME, and never call
`View.setPadding`. Cache and deduplicate physical values. Queue this dedicated
method on the TotalCross event thread:

    private native void nativeSafeAreaInsetsChanged(
        int top, int left, int bottom, int right);

Retain the latest values before VM startup and flush them when ready. Generate
JNI headers through Gradle, never manually, and delegate JNI to the common helper.

On iOS, make `viewSafeAreaInsetsDidChange` enqueue `safeAreaChanged` with physical
values. UIKit provides points, so multiply by native screen scale first. Ensure one
initial event after valid layout. Handle it in `event/darwin/event.m` and call the
same common helper. Do not reuse `screenChanged`, post `SK_SCREEN_CHANGE`, or
resize graphics resources.

Keep platform query functions only as a bounded startup fallback if initialization
requires them. Remove duplicate steady-state ownership after the push path works.

Run focused Java tests, then the only Android checkpoint build:

    cd TotalCrossSDK
    ./gradlew-agent test --tests 'totalcross.ui.SafeAreaLayoutTest' \
      --tests 'totalcross.ui.TopMenuSafeAreaTest'

    cd ../TotalCrossVM/android
    mkdir -p ../../build/safe-area-insets
    ./gradlew :app:assembleStandardDebug \
      --warning-mode=none --console=plain \
      > ../../build/safe-area-insets/android-build.log 2>&1
    status=$?; tail -100 ../../build/safe-area-insets/android-build.log; exit $status

Do not run Android clean tasks or any desktop, macOS, Linux, Windows, or iOS build.

### Milestone 5: Final validation and handoff

Run the complete focused Java set once, copyright validation for actual new/changed
files, `git diff --check`, and `git diff --stat`. Inspect only task-related diffs.
Do not rerun either checkpoint build.

Device smoke tests are last and use existing artifacts only. When an Android device
and launchable demo are available, verify portrait and landscape:

- normal controls remain safe after rotation;
- negative window insets cancel safe area as configured;
- ChatGPT, Reddit, and Gmail menu modes match their contracts;
- `BOTH` scrolls behind both bars while first/last items remain reachable;
- touch coordinates still match drawn controls.

If no device or fixture is available, record the exact reason and continue. Do not
create another build system or ask the user. Run iOS smoke only with a pre-existing
runnable artifact; never build iOS under this plan.

Complete `.agent/reports/safe-area-insets-editorial.md` with the factual sections
required by `.agent/PLANS.md`, including validation and limitations.

## Surprises & Discoveries

- Observation: The branch already has logical `screen.contentScale` conversion
  helpers in Android event code, while iOS safe-area changes currently reuse the
  unrelated `screenChanged` event.
  Evidence: the milestone-0 inventory found conversion in
  `TotalCrossVM/src/event/android/event_c.h` and `SK_SCREEN_CHANGE` dispatch in
  `TotalCrossVM/src/event/darwin/event.m`.

- Observation: `Window.getClientRect` did not include its inherited declared
  `Container.insets`, even though the compatibility contract exposes them.
  Evidence: the original override computed only title/border gaps; the focused
  negative-cancellation test now proves declared values remain separate and
  additive with safe exclusion.

- Observation: `ScrollContainer.resize` reset scrollbar tracking values when
  re-adding bars, while bag coordinates retained the prior visual offset.
  Evidence: the original method assigned `lastH = 0` and `lastV = 0`; content
  inset resizing now restores clamped scrollbar values and positions the bag
  from one source of truth.

Record only findings that materially change remaining work; keep raw output in the
log/evidence paths.

## Decision Log

- Decision: Keep declared and effective insets separate.
  Rationale: Dynamic updates must not erase user padding, and negative values must
  cancel safe area.
  Date: 2026-08-05

- Decision: Use window policy, child override, container safe padding, and scroll
  content insets as separate concepts.
  Rationale: Each controls a different layout boundary and avoids overloading
  `ignoreInsets` or ordinary padding.
  Date: 2026-08-05

- Decision: Apply safe padding only to unconsumed edges.
  Rationale: Full-bleed bars need safe internal content without double padding.
  Date: 2026-08-05

- Decision: Push safe-area updates through a dedicated internal API.
  Rationale: Safe area can change without surface size and must not recreate
  graphics resources.
  Date: 2026-08-05

- Decision: Preserve the scrolling `TopMenu.header` and add fixed bars.
  Rationale: Silent semantic changes would break existing layouts.
  Date: 2026-08-05

- Decision: Permit one SDK build and one Android build only at milestone closure.
  Rationale: Focused tests cover logic while builds protect integration with low
  cost.
  Date: 2026-08-05

- Decision: Preserve the supplied ExecPlan as protected task input and create a
  focused local commit at every completed milestone.
  Rationale: The updated goal objective explicitly authorizes and requires local
  milestone commits while still prohibiting pushes or remote-branch changes.
  Date: 2026-08-05

- Decision: Propagate a consumed-edge mask through the central parent client
  rectangle selection used by `Control.setRect`.
  Rationale: This keeps safe/full-bleed selection at the direct window-child
  boundary while allowing nested container safe padding to avoid adding the
  same edge twice.
  Date: 2026-08-05

- Decision: Model leading content insets as the scrolling bag origin and model
  both leading and trailing values in scrollbar extent.
  Rationale: Existing child coordinates and viewport bounds remain unchanged,
  while the first and last content edges become reachable with exactly one
  addition to each axis maximum.
  Date: 2026-08-05

## Validation and Acceptance

Use the escalation order in `AGENTS.md`. Acceptance requires:

- default main-window children use current selected safe edges;
- declared insets are unchanged by platform updates;
- `FULL_BLEED` retains border/user insets but ignores safe exclusion;
- safe padding is not applied twice;
- negative user insets are not clamped;
- unchanged updates do nothing and changed updates relayout once;
- safe-area updates do not invoke screen-surface change;
- content insets do not change scroll viewport size;
- the three requested menu modes pass focused tests;
- focused Java tests, one SDK build, and one Android build pass;
- iOS is statically reconciled and smoke-tested only when possible without build.

Missing device access is a reported limitation, not a feature failure.

## Risks and Open Questions

Change only direct inset reads required by the new semantics, preserving zero
content-inset behavior.

`TopMenu` animates off-screen; use intended open bounds for edge classification or
keep its window mode explicitly disabled as prescribed.

Android values may arrive before VM startup; cache the latest set and never invoke
VM code from the UI thread. iOS callbacks must use the existing event queue.

`getSafeAreaInsets` returns a shared mutable object for compatibility. Copy before
comparison or callback.

Scroll maximum/value conventions are delicate. Add tests before changing them and
preserve bottom anchoring.

## Idempotence and Recovery

Compare setter/event values before relayout. Repeated identical platform events are
harmless. Never use destructive git commands, broad cleanup, or cache deletion.
Remove a generated directory only when a specific stale path blocks the permitted
Android build.

If local commits are authorized, use one path-scoped milestone commit; never push,
tag, open a PR, or rewrite history. Otherwise record logical checkpoints.

On focused-test failure, fix and rerun only that test. On permitted build failure,
inspect its saved log and rerun only that checkpoint after the relevant fix. Do
not escalate to other builds. Trust preserved successful evidence when resuming.

## Outcomes & Retrospective

Milestone 0 completed without a build. The scoped source paths were clean, the
branch was confirmed as `feat/logical-ui-scaling` at `0ec107e0b9e3`, and the
only pre-existing scoped change was this untracked ExecPlan. The baseline
confirmed that logical scaling and platform safe-area queries exist, but dynamic
safe-area ownership is not yet separated from surface-change behavior.

Milestone 1 delivered `SafeAreaMode`, `SafeAreaLayout`, `SafeAreaEdges`, window
edge policy, direct-child safe/full-bleed placement, additive container safe
padding, and the deduplicating SDK update transition. Seven focused tests pass,
including selected modes, touched edges, declared inset preservation, negative
cancellation, padding deduplication, update deduplication, and callbacks. No
distribution build ran.

Milestone 2 delivered non-negative `ScrollContainer.contentInsets`. Five focused
tests prove viewport stability, exact extent growth, reachable first/last
content, idempotence, invalid-value rejection, middle-anchor preservation, and
trailing-edge anchoring. No distribution build ran.

## Revision Note

Initial plan created on 2026-08-05. It limits work to safe-area and inset
semantics, defines the four requested concepts, adds dynamic Android/iOS delivery,
adapts menus for reserve/overlay layouts, excludes panel-shape work, permits only
one SDK and one Android checkpoint build, and requires token-efficient autonomous
execution.

Milestone 0 update on 2026-08-05 recorded the clean scoped baseline, protected
the supplied plan, initialized the required support files, and captured the
existing Android logical conversion and iOS `screenChanged` coupling that guide
the next implementation slice.

Objective-reconciliation update on 2026-08-05 records the newly explicit local
commit requirement. It changes checkpoint mechanics only; feature scope,
validation gates, and platform-build limits are unchanged.

Milestone 1 update on 2026-08-05 records the implemented core APIs, the
consumed-edge propagation design, the declared-window-inset compatibility fix,
and the passing focused validation. Milestone 2 is now active.

Milestone 2 update on 2026-08-05 records the bag-origin/extent representation,
the scrollbar-state correction discovered during implementation, and the five
passing focused tests. Milestone 3 menu integration is now active.
