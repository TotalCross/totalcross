<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Stabilize legacy safe-area presentations for release

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`.

It is a release-stabilization plan for the current local
`feat/logical-ui-scaling` checkout. Its priority is to restore the proven
Window-based behavior of `TopMenu`, `SlidingWindow`, and `SideMenuContainer`
while keeping the safe-area fixes needed for the next release. The newer
presentation architecture is retained for future work but removed from the
release-critical runtime path and moved to `totalcross.ui.presentation`.

Do not push, publish, open a pull request, rewrite remote history, or perform
destructive Git operations while executing this plan.

Every new file created by this work must remain below 20 KiB and approximately
600 lines. Existing files that already exceed those limits must not be split or
refactored merely to satisfy them.

## Purpose / Big Picture

The observable release outcome is intentionally narrow:

- `SlidingWindow` is again a `Window`, with its existing popup, focus, event,
  fade, and z-stack behavior, while its children are laid out against the correct
  safe area before directional animation moves the window offscreen.
- `TopMenu` is again a `Window`, with legacy popup/fade/dismiss behavior and the
  safe-area compensation already implemented before the PresentationHost
  migration.
- `SideMenuContainer` again relies on the legacy `TopMenu` path and computes a
  horizontal drawer width from usable safe width rather than raw screen width.
- the recent broad fade/screenshot repair changes are removed from the release
  path so stabilization does not depend on a partially redesigned generic
  animation subsystem.
- the `Presentation*` infrastructure is retained, moved under
  `totalcross.ui.presentation`, isolated from the three legacy components, and
  compile-clean for future work. Runtime parity of that deferred infrastructure
  is not a release acceptance criterion.
- logical UI scaling, safe-area APIs, container clipping, and unrelated work in
  `feat/logical-ui-scaling` remain intact.

The result should match the pre-presentation Window behavior while respecting
current safe-area insets.

## Working Set and Resume Protocol

Use these support files:

    .agent/state/legacy-safe-area-stabilization.md
    .agent/evidence/legacy-safe-area-stabilization.md
    .agent/archive/legacy-safe-area-stabilization-history.md
    .agent/reports/legacy-safe-area-stabilization-editorial.md

Store verbose command output under:

    artifacts/legacy-safe-area-stabilization/logs/

On first execution, read this plan and only the relevant portions of `AGENTS.md`.
Create the state file before the first implementation milestone.

On resume, read the state file first. It records the active milestone, last
commit, active paths, validation, blockers, and next command. Avoid rereading the
complete plan or large history unless the state identifies a concrete reason.
Keep evidence concise and logs under `artifacts/`.

Important historical checkpoints:

    91616c934fe547c7b1b01c4a2990cb17ac865241
        state immediately before the later fade-repair plan

    8dc8af69bddeaa4b4341386a886268ad14e8443e
        SlidingWindow/MaterialWindow immediately before PresentationHost migration

    54a7bd5b49369737b5d17292597d7acbe7d95d31
        TopMenu immediately before PresentationHost migration

    f0a918d97e57b0e4868019256701d6f349904c87
        SideMenuContainer immediately before presentation detachment

    b4002cdbae736fcd3f17d48db7ad70cc087b41e3
        state immediately before PresentationHost was connected to Window

These revisions are narrow reconstruction references. Do not reset the working
tree or branch to any of them.

## Progress

- [x] 2026-08-10T17:39:06Z Record the current branch, HEAD,
      selected-path status, and checkpoints.
- [x] 2026-08-10T17:44:27Z Remove later generic fade/screenshot repairs not
      required by the safe-area release.
- [x] 2026-08-10T17:49:52Z Restore Window-based `SlidingWindow` and
      `MaterialWindow`.
- [x] 2026-08-10T17:56:14Z Restore Window-based `TopMenu` and legacy
      `SideMenuContainer`, with the minimal safe-width correction.
- [x] 2026-08-10T18:02:41Z Remove active PresentationHost coupling from
      `Window`.
- [x] 2026-08-10T18:02:41Z Move deferred presentation sources to
      `totalcross.ui.presentation` and compile without widening legacy APIs.
- [x] 2026-08-10T18:02:41Z Replace presentation-oriented coverage with focused
      legacy safe-area coverage.
- [x] 2026-08-10T18:30:02Z Complete release-focused visual/smoke validation,
      one non-clean SDK build, provenance/header checks, and final handoff.

Use UTC timestamps when updating Progress.

## Current Architecture and Scope

The branch migrated `SlidingWindow` and `TopMenu` from `Window` into
PresentationHost. Restore the legacy Window z-stack model with SideMenu using
TopMenu, while preserving safe-area APIs, scaling, and unrelated clipping.

The pre-migration SlidingWindow already lays out at final bounds before moving
only x/y to the animation origin. The pre-migration TopMenu already compensates
safe edges explicitly and preserves the orthogonal animation position.

Move the eight current presentation classes (`PresentationHost`,
`PresentationHandle`, `PresentationEntry`, `PresentationController`,
`PresentationBarrier`, `PresentationTransition`, `SlidePresentationTransition`,
and `FadePresentationTransition`) to
`TotalCrossSDK/src/main/java/totalcross/ui/presentation/` with package
`totalcross.ui.presentation`. The three release-critical components must not use
them afterward.

## Plan of Work

### Milestone 0: Establish a reversible stabilization baseline

Goal: make later work resumable and prove exactly which local state is modified.

Record:

    git branch --show-current
    git rev-parse HEAD
    git status --short
    git diff --check

The remote branch reference at plan-writing time is
`09dc39143c7edf0363ad3c1670bdd16e141b4572`; local HEAD remains the source of
truth. Inspect only target components, Window/Control, animation files,
presentation sources, and related tests. Record unrelated dirty paths and leave
them untouched.

Do not run tests in this milestone. Its final step is only `git diff --check`.

If the plan/support files are committed separately, use:

    docs(sdk): add legacy safe area stabilization plan

### Milestone 1: Restore the pre-fade-repair generic animation baseline

Goal: remove risky generic fade/screenshot work added after `91616c934...`
without reverting logical scaling or unrelated UI work.

Compare:

    git diff 91616c934fe547c7b1b01c4a2990cb17ac865241 --       TotalCrossSDK/src/main/java/totalcross/ui/anim/ControlAnimation.java       TotalCrossSDK/src/main/java/totalcross/ui/anim/FadeAnimation.java       TotalCrossSDK/src/main/java/totalcross/ui/Control.java

Restore the intended pre-repair behavior of `ControlAnimation.java` and
`FadeAnimation.java`.

For `Control.java`, do not restore the whole file. Revert only screenshot/fade
changes introduced by the later repair plan. Preserve unrelated logical scaling,
safe-area, clipping, rendering, and branch work.

Use `git show <revision>:<path>` into a temporary file and compare/apply narrow
patches. Do not use broad checkout/restore/reset commands over possibly dirty
source paths.

Remove fade-repair-only tests/smoke such as `ControlAnimationFadeTest`,
`PresentationFadeTest`, and `PresentationFadeSmoke`; keep `ControlScreenshotTest`
only if it still protects an independent supported contract. Do not modify other
animation consumers.

As the final milestone action, run only the smallest animation/UI tests needed
for compile/basic legacy behavior. On failure, fix and rerun only that lane. Do
not run `dist`.

Suggested commit:

    fix(sdk): restore legacy fade infrastructure

Explain in the body that the post-`91616c934` repair path was removed for release
stability and unrelated logical scaling was retained.

### Milestone 2: Restore Window-based SlidingWindow behavior

Goal: restore the proven Window lifecycle while retaining safe-area-aware layout
sequencing.

Reconstruct from `8dc8af69bddeaa4b4341386a886268ad14e8443e`:

    TotalCrossSDK/src/main/java/totalcross/ui/SlidingWindow.java
    TotalCrossSDK/src/main/java/totalcross/ui/MaterialWindow.java

Preserve Window popup/unpop semantics, CENTER FadeAnimation, directional
PathAnimation, drag/Back behavior, and delayed provider UI. In `popup()`, establish
final bounds, reposition children there, move only temporary x/y to the animation
origin, then enter normal Window popup. Never relayout at the offscreen position.
`screenResized()` reconstructs final bounds before child relayout. Do not call
PresentationController/Host.

As the final milestone action, create/adapt `SlidingWindowSafeAreaTest` for
nonzero insets, final content geometry, directional preparation, resize/reopen,
and CENTER-path selection. Run only it plus `SafeAreaLayoutTest` if needed.

Suggested commit:

    fix(sdk): restore safe sliding window lifecycle

### Milestone 3: Restore Window-based TopMenu and SideMenu behavior

Goal: recover legacy TopMenu popup/fade/z-stack/input behavior while keeping safe
area local to the component.

Reconstruct `TopMenu.java` from:

    54a7bd5b49369737b5d17292597d7acbe7d95d31

Preserve `TopMenu extends Window`, disabled automatic safe-area mode, explicit
attached-edge/bar padding, safe-aware bar hosts, safe-area relayout callback,
orthogonal-preserving PathAnimation, optional self-fade, `fadeOtherWindows`, and
legacy outside-click/focus/Back/z-stack behavior. Do not replace this with generic
Window changes.

Reconstruct `SideMenuContainer.java` from:

    f0a918d97e57b0e4868019256701d6f349904c87

For horizontal drawers replace raw screen-width sizing with
`safeWidth = screenWidth - safe.left - safe.right` and
`drawerWidth = min(320, safeWidth - BAR_HEIGHT_IN_DP)`, clamped validly.
Preserve explicit caller width precedence. Keep TOP/BOTTOM percentage behavior
unless focused evidence proves a defect, and keep legacy Window input handling.

Testing is the final milestone action. Use `TopMenuSafeAreaTest` and
`SideMenuSafeAreaTest`; assert safe bounds, edge mapping, bar padding, and safe
drawer width with asymmetric insets, never Presentation state. Run those tests
and `SafeAreaLayoutTest` only if needed.

Prefer separate validated commits:

    fix(sdk): restore safe top menu lifecycle
    fix(sdk): make side menu width safe area aware

A combined commit is acceptable only if the implementation cannot be validated
meaningfully in independent slices.

### Milestone 4: Isolate the deferred presentation subsystem

Goal: retain the newer architecture for future work without letting it affect the
release path.

Remove only active PresentationHost coupling from `Window.java`:

    private PresentationHost presentationHost
    presentationHost()
    repositionChildren() forwarding to presentationHost.ownerLayoutChanged()

Use `b4002cdbae736fcd3f17d48db7ad70cc087b41e3` only as a comparison reference.
Do not restore the whole `Window.java`; preserve subsequent safe-area and
logical-scaling work.

Verify `TopMenu`, `SlidingWindow`, `MaterialWindow`, and `SideMenuContainer`
contain no PresentationController/Host/Handle dependency.

Move:

    FadePresentationTransition.java
    PresentationBarrier.java
    PresentationController.java
    PresentationEntry.java
    PresentationHandle.java
    PresentationHost.java
    PresentationTransition.java
    SlidePresentationTransition.java

to `TotalCrossSDK/src/main/java/totalcross/ui/presentation/` and update package
declarations/imports.

Keep classes package-private where practical. Do not make legacy `Window`,
`Control`, or `Container` methods public merely to preserve dormant presentation
code.

Package relocation will expose package-private dependencies. Resolve them inside
presentation code with existing public APIs (for example getters/`setRect`) rather
than widening legacy APIs. If exact deferred behavior would require new public
access, simplify/document it instead. Acceptance is isolation plus compilation,
not runtime parity. Keep only cheap presentation tests that remain useful.

Treat the source move as provenance-sensitive: follow repository header/audit
rules and generate required move evidence, but do not approve audits or invoke
commit-creating approval tools without explicit authorization.

Compilation/testing is the final milestone action. Run the smallest Gradle compile
or focused test task that proves the new package compiles. Do not run full `dist`.

Suggested commit:

    refactor(sdk): isolate deferred presentation infrastructure

State in the body that the package is intentionally unused by the legacy release
path.

### Milestone 5: Release-focused safe-area validation and final integration

Goal: prove actual release behavior, especially visual behavior that prior
internal-state tests did not represent.

Do not add architecture work in response to visual failure. Fix only local
safe-area behavior in the three target components.

Before final automation, exercise a real sample/launcher with portrait
top/bottom insets, landscape asymmetric left/right insets, and edge-to-edge.
Cover TopMenu LEFT/RIGHT (plus other directions when cheap), SlidingWindow
BOTTOM/CENTER plus one horizontal direction, SideMenu drawer, reopen, and one
resize. Check safe content/bars, animation endpoints, legacy fades,
outside-click, Back/Escape, and absence of one-frame safe-area jumps.

Reuse an existing JavaSE visual/screenshot smoke if it represents the real tree.
Add a small smoke only if it validates release properties without rebuilding
presentation architecture.

After implementation and visual inspection are complete, automated tests are the
last functional action. Run the focused safe-area set once. Then run one non-clean
SDK build:

    cd TotalCrossSDK
    ./gradlew-agent dist -x test --no-daemon --console=plain       > ../artifacts/legacy-safe-area-stabilization/logs/sdk-dist.log 2>&1

Do not run `clean` unless stale outputs are demonstrated.

If the existing macOS native smoke path is cheap, run it once after Java
validation. Do not build/deploy Android or iOS unless focused evidence exposes a
platform-specific discrepancy.

Finish with:

    git diff --check
    git status --short
    python3 scripts/validate-copyright-headers.sh --files <changed-files>

Run `wc -c` and `wc -l` for every new file; each must remain below 20 KiB and
approximately 600 lines.

Update evidence, state, this plan, and:

    .agent/reports/legacy-safe-area-stabilization-editorial.md

The editorial report distinguishes restored legacy behavior, retained scaling,
deferred presentation work, validation, and remaining limits.

## Surprises & Discoveries

- The pre-PresentationHost `SlidingWindow` already establishes final geometry
  before moving to its animation origin. Use it instead of designing a new host.
- The pre-migration `TopMenu` already has attached-edge safe-area handling and
  orthogonal-position preservation. Restore and validate that local mechanism.
- SideMenu legacy width uses raw screen width. Keep the legacy TopMenu model but
  calculate horizontal width from usable safe width.
- Later fade-repair commits changed generic animation/screenshot infrastructure.
  Remove that dependency from the release-critical path.
- Moving presentation classes to a subpackage removes implicit access to
  `totalcross.ui` package-private internals. Adapt deferred code internally; do
  not widen legacy APIs just to keep it unchanged.

Add only discoveries that materially change remaining work.

## Decision Log

- Decision: ship Window-based TopMenu and SlidingWindow for this release.
  Rationale: safe-area correctness is release-critical; presentation architecture
  is not.
  Date: 2026-08-10.
- Decision: restore component implementations from immediate pre-migration
  checkpoints, not master.
  Rationale: those checkpoints already contain relevant safe-area fixes.
  Date: 2026-08-10.
- Decision: use `91616c934...` as the boundary for later generic fade/screenshot
  repair rollback.
  Rationale: that repair is not required for the safe-area objective.
  Date: 2026-08-10.
- Decision: retain presentation classes under `totalcross.ui.presentation` but
  keep them out of the legacy runtime path.
  Rationale: preserve future work without release risk.
  Date: 2026-08-10.
- Decision: do not widen legacy UI APIs for the deferred package move.
  Rationale: dormant architecture must adapt to the stable SDK, not vice versa.
  Date: 2026-08-10.
- Decision: run tests only after relevant milestone implementation is complete.
  Rationale: reduce token/tool cost and repeated validation.
  Date: 2026-08-10.

## Validation and Acceptance

Within each milestone:

1. inspect only required source/history;
2. complete the implementation slice;
3. inspect the scoped diff;
4. run focused tests as the milestone's final implementation step;
5. if needed, fix and rerun only the failing focused lane;
6. commit the validated slice.

Do not use test-first/red-green execution for this stabilization. Do not run broad
tests after every commit. Do not rerun expensive validation after comments, plan,
state, or formatting-only edits; use `git diff --check`.

The release is accepted when:

- `SlidingWindow` is Window-based and its child layout respects safe area before,
  during, and after directional presentation;
- `TopMenu` is Window-based and applies the correct safe edges/bars without
  double-consuming insets;
- `SideMenuContainer` uses safe usable width for horizontal drawers;
- legacy fade, outside-click, Back/Escape, focus, and z-stack behavior are
  visually unchanged from the known Window-based path;
- the later generic fade/screenshot repair is no longer required by these
  components;
- all eight presentation classes compile in `totalcross.ui.presentation` and are
  not called by the three release-critical components;
- unrelated safe-area, scaling, and clipping behavior remains intact;
- focused tests pass;
- one final non-clean SDK distribution succeeds;
- copyright/provenance checks pass;
- all new files meet size limits;
- no changes are pushed.

## Risks and Open Questions

Main risks are overwriting scaling work, subpackage access, and SideMenu width
precedence. Use narrow comparisons, compilation evidence, and preserve explicit
caller width.

If legacy fade is still visually broken after removing the later generic fade
repair and restoring Window-based components, do not expand architecture. Compare
only the affected fade path with its pre-repair checkpoint and make the smallest
local compatibility fix.

## Idempotence and Recovery

Never use `git reset --hard`, broad checkout/restore commands, or destructive
clean operations.

Before historical replacement, compare `git show <sha>:<path>` via a temporary
file and preserve unrelated edits. Before each commit run `git diff --check`,
inspect only active-path diff/status, and stage only the logical slice.

If execution stops, the state file is the restart point. It must name the active
milestone, current paths, last validation, last commit, and one concrete next
command.

A failed focused test may be rerun after a targeted fix. A successful broad build
must not be repeated unless later source changes affect its proof.

Do not push. Commits are local recovery checkpoints only.

## Outcomes & Retrospective

- Removed the later generic fade repair and restored Window-based TopMenu,
  SlidingWindow, MaterialWindow, and SideMenu behavior.
- Horizontal automatic drawer width is
  `max(1, min(320, safeWidth - 56))`; explicit caller width still wins.
- Isolated eight deferred presentation classes without API widening. Runtime
  parity and human review of the pending provenance audit remain future work.
- Passed 18 focused tests, inspected 10 JavaSE preview PNGs, completed the
  replacement native macOS smoke, and built one non-clean SDK distribution.
- Final diff, header, provenance, and size checks passed. Android/iOS were not
  run, and no changes were pushed.

## Revision Note

2026-08-10: Initial Window-based safe-area stabilization plan with deferred
presentation isolation and milestone-final testing.
