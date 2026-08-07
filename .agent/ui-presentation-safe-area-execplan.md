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

- [ ] Record the local baseline, local-only changes, and earlier animation fixes
      that must be preserved.
- [ ] Make ancestor clipping explicit without changing its default behavior.
- [ ] Correct `ClippedContainer` visibility-search edge cases.
- [ ] Add the internal presentation host, entry/handle, viewport/frame, and
      transition foundation.
- [ ] Move `SlidingWindow` and `MaterialWindow` off top-level Window
      presentation.
- [ ] Move `TopMenu` off top-level Window presentation and simplify its internal
      safe-area handling.
- [ ] Update `SideMenuContainer` to use local gesture/presentation behavior and
      safe-viewport sizing.
- [ ] Add a focused smoke fixture if existing repository smoke/deploy machinery
      can be reused economically.
- [ ] Run final focused Java validation, one non-clean SDK distribution build,
      and, when feasible, one deployed native macOS smoke.
- [ ] Complete file-size, copyright, compatibility, evidence, state, and
      editorial checks.

## Current Architecture and Scope

`Window` remains the real top-level surface abstraction. Its blocking and
nonblocking popup paths modify `Window.topMost` and `Window.zStack`, disable the
previous window for events, and restore focus on close. Real dialogs and other
true windows keep that behavior.

`SlidingWindow` and `TopMenu` currently inherit from `Window`.
`SideMenuContainer` is already a `Container`, but creates a `TopMenu`, invokes
its popup lifecycle, uses physical screen width for drawer sizing, and registers
gesture handling through its parent window. `MaterialWindow` inherits
`SlidingWindow` and composes a `Bar` with provider content.

The owner window's `getClientRect()` is the authoritative safe client rectangle
for presentations. The internal presentation host may cover the owner window's
full usable surface, but the animated presentation viewport must equal that safe
client rectangle.

A source audit performed while writing this plan found that
`Control.getGraphics()` already delegates to `Control.refreshGraphics()`, which
clips a control against every ancestor container bound before refreshing the
graphics. Therefore the new clipping policy must default to clipping enabled.
Do not implement a default-disabled clipping model; that would change existing
rendering semantics.

`ClippedContainer` adds paint culling on top of normal ancestor clipping. Its
current search accepts `ini` but starts at zero, uses zero both as a valid index
and as a not-found result, and leaves `lastMid` initialized as zero even though
the algorithm treats `-1` as the empty sentinel. Correct these defects only.
Do not rename the class or redesign list virtualization.

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

Record:

    git branch --show-current
    git rev-parse HEAD
    git log -8 --oneline

Use scoped status/diff commands for active paths. Confirm the current branch is
the intended local `logical-ui-scaling` checkout or its existing
`feat/logical-ui-scaling` name. Do not switch branches just to normalize naming.

Inspect local `PathAnimation`, `SlidingWindow`, `TopMenu`,
`SideMenuContainer`, `Control`, `Container`, and `ClippedContainer`. Record in
state which previous safe-area/animation fixes already exist. Preserve the
opt-in orthogonal-position behavior in `PathAnimation` if present. Capture the
starting commit as `BASE_COMMIT` and record unrelated dirty files.

If this plan is newly added and uncommitted, commit the plan/supporting design
separately before source work:

    docs(sdk): add safe presentation exec plan

Do not amend unrelated commits and do not push.

### Milestone 1: Clipping contract and ClippedContainer correctness

Follow the implementation design section `Clipping and ClippedContainer`.

Make clipping an internal Container policy whose default matches today's
ancestor clipping. Update `Control.refreshGraphics` to consult that policy while
preserving translation, `topParent`, offscreen, scale, and font behavior.

Add focused `ContainerClippingTest`.

Fix `ClippedContainer` search/sentinel handling without renaming it or changing
its public role. Add focused `ClippedContainerTest`.

Run only these two tests and static diff checks.

Preferred commits:

    refactor(sdk): make container child clipping explicit
    fix(sdk): correct clipped container visibility search

### Milestone 2: Internal presentation foundation

Follow `Internal presentation foundation` in the design file.

Create small package-private presentation types in `totalcross.ui`; do not create
a public widget package. Add only the smallest package-private integration hook
needed in `Window` to own a presentation host.

The host must use the owner safe client rectangle for each presentation
viewport, keep a full-host barrier, clip the viewport, animate only a frame's
transient position, and leave `Window.zStack`/`Window.topMost` unchanged.

Add focused `PresentationHostTest` with nonzero safe insets. Run it plus the
existing `SafeAreaLayoutTest`, then commit:

    refactor(sdk): add internal presentation host foundation

### Milestone 3: SlidingWindow and MaterialWindow migration

Follow `SlidingWindow and MaterialWindow migration` in the design file.

`SlidingWindow` must stop being presented as a top-level Window. Prefer direct
`Container` inheritance plus internal presentation composition rather than
making a new public presentation superclass. Keep its own current constructors,
popup/unpop methods, slack, direction, delayed provider behavior, gestures, and
Back/Escape behavior.

`MaterialWindow` keeps its current Bar/provider composition but makes Bar width
explicit with `FILL`.

Add focused deterministic `SlidingWindowPresentationTest`. Run only that test,
`PresentationHostTest`, and `PathAnimation` focused tests if `PathAnimation`
itself changed.

Commit:

    refactor(sdk): present sliding windows inside safe viewport

### Milestone 4: TopMenu and SideMenuContainer migration

Follow `TopMenu and SideMenuContainer migration` in the design file.

`TopMenu` must stop being presented as a top-level Window. Keep its own current
constructors, item/selection API, popup/unpop behavior, sizing controls, fixed
bars, scroll-under modes, header/background/separator/elevation intent, and
animation listener. Safe area is consumed by the presentation viewport once;
remove the old second layer of TopMenu safe-inset compensation.

`SideMenuContainer` keeps its public API, Bar/content composition, and
`topMenu` relationship. Its gestures become local and its drawer sizing is
resolved from safe viewport width at presentation time.

Rewrite existing `TopMenuSafeAreaTest` for the new model and add a separate
SideMenu test only if needed for clarity/file-size limits.

Preferred commits:

    refactor(sdk): present top menu as safe overlay
    refactor(sdk): detach side menu from window presentation

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

Removing `Window` inheritance from `SlidingWindow` and `TopMenu` changes Java
type assignability. Do not deprecate the classes, but record actual compile/API
impact. Preserve their own documented and repository-used API; do not recreate
arbitrary inherited Window API solely to hide the superclass change.

TopMenu previously obtained title/border behavior from Window. Preserve the
TopMenu title presence, alignment/color intent, and border intent required by
current repository callers using existing Container/Label/border primitives.
Avoid copying the large Window title-painting implementation. If focused visual
evidence finds a material regression, make the smallest TopMenu-specific fix and
record it.

If focus or Back dispatch escapes to underlying content after presentations stop
being real Windows, add the smallest host-level focus save/restore or key
dispatch needed. Do not start a general FocusScope framework.

Keep the new clipping switch internal. Public clipping API design belongs to a
later UI API phase.

## Idempotence and Recovery

Never use `git reset --hard`, `git checkout -- <path>`, broad clean commands, or
deletion of unrelated generated directories as recovery. Preserve unrelated
local changes.

Before each commit, inspect `git diff --stat` and only active diffs. Stage only
the current slice. After each logical commit, update state with the commit hash,
focused validation, deferred validation, new files, and next action.

If a focused test fails, fix and rerun only that slice. Do not escalate to SDK
distribution or native smoke for a unit-level failure. If a platform smoke fails,
capture concise evidence and retry only the failed deployment/runtime step after
a targeted fix.

Repeated present/dismiss cycles must not leak entry layers. A second dismiss is
a no-op. An aborted animation must leave an entry fully attached at a stable
position or fully detached, never half registered.

No task in this plan pushes, opens a pull request, publishes, or modifies remote
state.

## Outcomes & Retrospective

At each completed milestone, add one short factual paragraph with delivered
behavior, logical commit, focused validation, and deferred expensive validation.
At completion, compare delivered behavior with the purpose and distinguish unit,
JavaSE, and native macOS proof.

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
