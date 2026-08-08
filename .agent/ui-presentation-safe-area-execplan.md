<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Move transient UI presentations into safe-area container layers

This ExecPlan is a living execution document for the local `logical-ui-scaling`
checkout. It follows `AGENTS.md` and the ExecPlan policy from
`TotalCross/totalcross-depot-tools/.agent/PLANS.md`. Keep `Progress`,
`Surprises & Discoveries`, `Decision Log`, `Outcomes & Retrospective`, and the
state file current while executing it.

Make frequent logical commits and never push. New files must remain below 20 KiB
and approximately 600 lines; existing files need not be split for this limit.

## Purpose / Big Picture

`SlidingWindow`, `TopMenu`, and `SideMenuContainer` currently use top-level
`Window` behavior for UI that conceptually belongs inside an existing
application window. This causes their animation and safe-area behavior to depend
on moving an entire second window rather than moving content inside the owner's
safe client area.

After this plan, `SlidingWindow` and `TopMenu` are presented through an internal
container-based presentation host owned by the current real `Window`;
`SideMenuContainer` opens its `TopMenu` through the same foundation. The current
classes remain the user-facing API. Do not deprecate them and do not introduce or
recommend public `Navigator`, `Route`, `Overlay`, or `Scaffold` APIs yet.

A nonzero safe-area test must show that a sliding page or menu starts just
outside the safe presentation viewport, finishes inside it, is clipped by that
viewport during animation, and never becomes a new `Window.zStack` entry.

This plan also formalizes container clipping without changing its current
default behavior and fixes correctness defects in `ClippedContainer`. Keep the
name `ClippedContainer`; naming cleanup is out of scope.

## Working Set and Resume Protocol

The local checkout is the source of truth. Preserve its earlier `PathAnimation`,
`SlidingWindow`, and `TopMenu` fixes; do not fetch, reset, or reconstruct them
from the remote branch.

Read first on initial execution:

    AGENTS.md
    .agent/ui-presentation-safe-area-execplan.md
    .agent/design/ui-presentation-safe-area-implementation.md

Create and maintain:

    .agent/state/ui-presentation-safe-area.md
    .agent/evidence/ui-presentation-safe-area.md
    .agent/archive/ui-presentation-safe-area-history.md
    .agent/reports/ui-presentation-safe-area-editorial.md

On resume, read the state file, active milestone, and named design section only.

Store verbose command output under:

    artifacts/ui-presentation-safe-area/logs/

Read these guides only when their validation lane is reached:

    .agent/guides/logical-ui-scaling-validation.md
    .agent/guides/macos-native-runtime-validation.md

Expected production working set:

    TotalCrossSDK/src/main/java/totalcross/ui/Control.java
    TotalCrossSDK/src/main/java/totalcross/ui/Container.java
    TotalCrossSDK/src/main/java/totalcross/ui/ClippedContainer.java
    TotalCrossSDK/src/main/java/totalcross/ui/Window.java
    TotalCrossSDK/src/main/java/totalcross/ui/SlidingWindow.java
    TotalCrossSDK/src/main/java/totalcross/ui/MaterialWindow.java
    TotalCrossSDK/src/main/java/totalcross/ui/TopMenu.java
    TotalCrossSDK/src/main/java/totalcross/ui/SideMenuContainer.java
    TotalCrossSDK/src/main/java/totalcross/ui/anim/PathAnimation.java
    TotalCrossSDK/src/test/java/totalcross/ui/

Do not broaden this set without a compile error or focused test proving it is
necessary.

## Progress

- [x] (2026-08-07 15:10Z) Recorded baseline `62c9c728c`, branch
      `feat/logical-ui-scaling2`, unrelated local files, and the earlier
      animation/safe-area fixes that must be preserved.
- [x] (2026-08-07 15:25Z) Made ancestor clipping explicit without changing its
      default behavior (`536a7984c`).
- [x] (2026-08-07 15:26Z) Corrected `ClippedContainer` visibility-search edge
      cases (`c6e2f90bc`).
- [x] (2026-08-07 15:35Z) Added the internal presentation host, entry/handle,
      viewport/frame, controller, and transition foundation (`cd5082a1d`).
- [x] (2026-08-07 15:39Z) Moved `SlidingWindow` and `MaterialWindow` off
      top-level Window presentation (`631badefd`).
- [x] (2026-08-07 15:47Z) Moved `TopMenu` to a safe overlay and removed duplicate
      safe-area compensation (`565b89e37`).
- [x] (2026-08-07 15:48Z) Made SideMenu gestures local and drawer sizing
      safe-viewport-relative (`56544d833`).
- [x] (2026-08-07 15:50Z) Added and compiled a focused safe-presentation smoke
      fixture (`f1601b2e6`).
- [x] (2026-08-07 15:53Z) Passed final focused Java, one non-clean SDK dist,
      JavaSE smoke, and deployed native macOS smoke.
- [x] (2026-08-07 16:04Z) Completed file-size, copyright, compatibility,
      evidence, state, retrospective, and editorial handoff checks.

## Current Architecture and Scope

`Window` remains the real top-level surface abstraction. Its blocking and
nonblocking popup paths modify `Window.topMost` and `Window.zStack`, disable the
previous window for events, and restore focus on close. Real dialogs and other
true windows keep that behavior.

`SlidingWindow` and `TopMenu` now inherit `Container` and compose an internal
controller. `MaterialWindow` retains SlidingWindow and its Bar/provider layout.
SideMenu retains its public TopMenu relationship but uses local gestures and
safe-viewport drawer sizing.

The owner window's `getClientRect()` is the safe presentation rectangle. The
host may cover the full window, but its animated viewport must equal that client
rectangle.

Ancestor clipping is now an explicit default-enabled internal Container policy.
`ClippedContainer` remains a paint-culling optimization and now uses correct
search ranges and `-1` empty/not-found sentinels.

A "presentation viewport" is a child container whose bounds equal the owner
window's safe client rectangle. A "presentation frame" is the child moved
temporarily during animation. Layout uses final frame geometry; animation changes
only transient frame x/y. A "barrier" is a full-host transparent or translucent
control that blocks pointer input to content behind an active presentation and
may dismiss an overlay on outside press.

Detailed type responsibilities and migration rules are in
`.agent/design/ui-presentation-safe-area-implementation.md`.

## Plan of Work

### Milestone 0: Reconcile the local checkout

Completed at baseline `62c9c728c` on `feat/logical-ui-scaling2`. The state and
archive record preserved fixes and unrelated local files; planning commit is
`9a3b22ae1`. Do not switch, fetch, overwrite local work, amend, or push.

### Milestone 1: Clipping contract and ClippedContainer correctness

Completed in `536a7984c` and `c6e2f90bc`. Default ancestor clipping is explicit,
one ancestor may opt out internally, and `ClippedContainer` uses correct ranges
and sentinels. Both focused tests and static checks passed.

### Milestone 2: Internal presentation foundation

Completed in `cd5082a1d`. Package-private host/entry/handle/controller and slide
transition types use the owner's safe client rectangle, a clipped viewport, a
barrier, and transient frame movement without changing the real window stack.
`PresentationHostTest` and `SafeAreaLayoutTest` pass.

### Milestone 3: SlidingWindow and MaterialWindow migration

Completed in `631badefd`. `SlidingWindow` now inherits `Container`, composes the
controller, retains its own popup, provider, slack, gesture, resize, and key
behavior, and uses local slide/fade transitions. `MaterialWindow` retains its
bar/provider layout. Its focused test and `PresentationHostTest` pass.

### Milestone 4: TopMenu and SideMenuContainer migration

Completed in `565b89e37` and `56544d833`. TopMenu now composes the overlay
controller, consumes safe area once, and retains its own menu/bar/title/sizing
APIs. SideMenu gestures are local and drawer width is resolved from the safe
viewport. Focused TopMenu and SideMenu tests pass. `f0a918d97` updates the local
sample required by the superclass change.

### Milestone 5: Smoke, final validation, and handoff

No platform build or deployment is allowed before Milestones 1-4 are complete
and focused tests pass.

Build the SDK once, without `clean`:

    cd TotalCrossSDK
    ./gradlew-agent dist -x test --no-daemon --console=plain \
      > ../artifacts/ui-presentation-safe-area/logs/sdk-dist.log 2>&1

Compile smoke sources against the generated SDK:

    ./gradlew-agent smokeTestClasses --no-daemon --console=plain \
      > ../artifacts/ui-presentation-safe-area/logs/smoke-compile.log 2>&1

If existing smoke/deploy machinery can be reused with a small fixture, add
`TotalCrossSDK/src/smokeTest/java/totalcross/ui/PresentationSafeAreaSmoke.java`.
It should self-assert one sliding presentation and one side/top menu, report safe
viewport/final bounds, verify the real window stack is unchanged, and exit
nonzero on failure. Prefer programmatic safe insets or existing Launcher
safe-area arguments for the JavaSE lane.

A native smoke is optional only when the current checkout already has a
reusable deployment path. If feasible, deploy and execute it only on macOS,
only after implementation completion, and at most once successfully unless a
failure requires a focused retry. Follow
`.agent/guides/macos-native-runtime-validation.md`. Do not build or deploy
Android or iOS in this plan.

If native macOS has zero real safe insets, use native smoke for lifecycle,
clipping, and z-stack proof and JavaSE/simulator for nonzero-inset geometry.
Record the limitation accurately.

Run the final focused Java set once, including only tests that exist:

    ./gradlew-agent test \
      --tests 'totalcross.ui.ContainerClippingTest' \
      --tests 'totalcross.ui.ClippedContainerTest' \
      --tests 'totalcross.ui.PresentationHostTest' \
      --tests 'totalcross.ui.SlidingWindowPresentationTest' \
      --tests 'totalcross.ui.TopMenuSafeAreaTest'

Include `SideMenuPresentationTest` and `SafeAreaLayoutTest` when applicable.

Finish with:

    git diff --check
    git diff --stat
    git status --short -- <active paths>
    python3 scripts/validate-copyright-headers.sh --files <changed source/test files>

For every file created by this plan, run `wc -c` and `wc -l`. Split by
responsibility rather than compressing unreadable code if any new file approaches
20 KiB or 600 lines.

A final test-only commit may be:

    test(sdk): add safe presentation regression coverage

Do not squash the logical commits and do not push.

## Surprises & Discoveries

- Observation: `Control.refreshGraphics` already clips control graphics against
  ancestor container bounds.
  Evidence: the current method intersects the child rectangle with each parent
  before `Graphics.refresh`.
  Consequence: clipping remains enabled by default; the new Container policy is
  an explicit/internal control over existing behavior, not a default change.

- Observation: `ClippedContainer` is a paint-culling optimization in addition to
  normal clipping.
  Consequence: keep the current name and scope its fixes to search/culling
  correctness.

- Observation: the remote branch may lag the local checkout.
  Consequence: preserve local earlier animation fixes and never overwrite them
  from remote source.

- Observation: TotalCross rejects `FILL` child layout before a parent has bounds.
  Evidence: the first host test failed at `Container.add`; deferring internal
  `FILL` layout until attachment made the focused test pass.

- Observation: `MaterialWindow` previously started two delayed provider loads.
  Evidence: its override called `super.postPopup()` and then launched a second
  provider thread. A placement hook now performs one load below the bar.

- Observation: the previously untracked TopMenu sample became uncompilable when
  TopMenu stopped extending Window.
  Evidence: Java rejected its `getParentWindow()` cast; using the enclosing
  TopMenu reference restored compilation in `f0a918d97`.

Add only discoveries that materially alter remaining work.

## Decision Log

- Decision: Build only an internal presentation foundation now.
  Rationale: current APIs need correct implementation before future public
  navigation/overlay names and contracts are frozen.
  Date/Author: 2026-08-07 / plan author.

- Decision: Keep current `SlidingWindow`, `MaterialWindow`, `TopMenu`, and
  `SideMenuContainer` as the supported user-facing API; do not deprecate them.
  Rationale: this phase changes implementation, not product guidance.
  Date/Author: 2026-08-07 / user requirement.

- Decision: Preserve default ancestor clipping.
  Rationale: it already exists in `Control.refreshGraphics`.
  Date/Author: 2026-08-07 / plan author.

- Decision: Keep the `ClippedContainer` name.
  Rationale: naming cleanup is explicitly out of scope.
  Date/Author: 2026-08-07 / user requirement.

- Decision: Native smoke, if feasible, runs only on macOS and only after the
  implementation is complete.
  Rationale: minimize expensive platform validation and honor the requested
  platform boundary.
  Date/Author: 2026-08-07 / user requirement.

- Decision: A safe-area change ends an active transition at a stable state
  before relayout.
  Rationale: it avoids carrying fractional progress across viewport geometries
  and matches the design's preferred deterministic behavior.
  Date/Author: 2026-08-07 / Codex.

- Decision: Keep delayed provider loading in `SlidingWindow` and specialize only
  insertion position in `MaterialWindow`.
  Rationale: one shared load preserves behavior and prevents duplicate views.
  Date/Author: 2026-08-07 / Codex.

## Validation and Acceptance

Use the repository escalation order and stop at the first sufficient level:
static check, focused unit test, focused integration test, module build, smoke
deploy, full distribution, clean full distribution.

Acceptance requires:

1. Presenting `SlidingWindow`, `MaterialWindow`, or `TopMenu` does not add it to
   `Window.zStack` or replace the real top-most Window.
2. Nonzero owner safe insets produce a presentation viewport equal to the owner
   safe client rectangle.
3. Bottom/left/right/top slides start just beyond the corresponding safe viewport
   edge and use no physical-screen dimension.
4. Descendants are clipped at the safe viewport while the frame animates.
5. Safe-area/rotation changes relayout active presentations without unnecessary
   provider/body recreation.
6. TopMenu bars/body consume safe area once, not once in the host and again in
   TopMenu.
7. SideMenu gestures work without registering a global parent-window pen
   listener.
8. Existing `PathAnimation` legacy callers retain previous behavior.
9. `ClippedContainer` paints no false index-zero child when no child is visible.
10. All new files remain below 20 KiB and approximately 600 lines.
11. Focused tests pass, one non-clean SDK dist passes, and smoke claims match only
    lanes actually executed.

## Risks and Open Questions

The superclass change intentionally removes Window assignability while retaining
the classes' own repository-used APIs. TopMenu title/border rendering is a small
Container/Label mapping and has no automated visual-equivalence proof. Host-level
Back dispatch is covered structurally; broad focus-scope work and a public
clipping/navigation API remain out of scope.

The compatibility field `TopMenu.fadeOtherWindows` remains available, but the
new modal barrier is transparent and does not reproduce the old visual dimming.
This is recorded as remaining visual-parity work rather than silently claimed
as equivalent behavior.

## Idempotence and Recovery

Recovery preserves unrelated work and avoids destructive Git/clean operations.
Scoped diffs and staging precede commits. Present/dismiss is idempotent, resize
ends transitions in a stable state, and no task pushes, publishes, or changes
remote state.

## Outcomes & Retrospective

Milestones 0-4 preserved the baseline fixes, made clipping explicit, corrected
ClippedContainer, added the internal host, and migrated SlidingWindow,
MaterialWindow, TopMenu, and SideMenu. Commits and focused proof are recorded in
the state, evidence, and archive. External Window assignability is intentionally
not preserved.

Milestone 5 added smoke `f1601b2e6`. The final focused Java set, non-clean SDK
distribution, smoke compilation, JavaSE run, current native macOS build/deploy,
and direct native run passed. JavaSE proved deterministic nonzero geometry
`20,10,260,600`; native macOS proved lifecycle/clipping/stack behavior with its
runtime viewport `20,10,1668,941`. Android and iOS were not built or deployed.

The implementation meets the structural and measurable acceptance criteria:
presentations remain inside their owner, use its safe client rectangle, clip
transient motion, preserve retained content across relayout, and leave the real
Window stack unchanged. The final audit also confirmed all new files remain
below the requested size thresholds. Remaining uncertainty is visual rather
than architectural: title/border equivalence and `fadeOtherWindows` dimming are
not covered by screenshot comparison.

The final editorial report is:

    .agent/reports/ui-presentation-safe-area-editorial.md

It must contain the headings required by the ExecPlan policy: `Editorial
Summary`, `Original Plan versus Actual Outcome`, `What Changed`, `Decisions and
Trade-offs`, `Unexpected Problems and Discoveries`, `Validation and Measurable
Results`, `Useful Evidence and Examples`, `Limitations, Remaining Work, and Open
Questions`, `Possible Article Angles`, `Suggested Narrative`, and `Claims
Requiring Human Review`.

## Revision Note

2026-08-07: Initial plan. It combines the internal safe-area presentation
foundation, explicit Container clipping contract, focused `ClippedContainer`
fixes, migration of `SlidingWindow`/`MaterialWindow` and
`TopMenu`/`SideMenuContainer`, new-file size limits, token-efficient validation,
frequent local commits with no push, and macOS-only native smoke after
implementation completion.

2026-08-07: Recorded the reconciled local baseline and Milestone 0 outcome so a
resume begins from the verified branch and preserves the existing fixes.

2026-08-07: Recorded Milestone 1 results and compacted completed milestone text
into factual outcomes to keep this living plan within its file-size limit.

2026-08-07: Recorded the completed host foundation, its bounds-initialization
discovery, and deterministic resize decision.

2026-08-07: Recorded the sliding/material migration, compatibility impact, and
the duplicate delayed-provider discovery.

2026-08-07: Recorded the TopMenu/SideMenu migration and the sample compile
compatibility fix; all implementation milestones are now complete.

2026-08-07: Recorded smoke and final validation outcomes and compacted completed
history into the archive to keep this plan below its size limit.

2026-08-07: Closed the plan after final copyright, static, compatibility,
new-file size, evidence, state, retrospective, and editorial checks.
