<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda
SPDX-License-Identifier: LGPL-2.1-only
-->

# Repair fade lifecycle and restore presentation dimming

This ExecPlan is a living document maintained according to `.agent/PLANS.md`.
Keep `Progress`, `Surprises & Discoveries`, `Decision Log`, and
`Outcomes & Retrospective` current.

It builds on `.agent/ui-presentation-safe-area-execplan.md`; do not redo that
migration. Work on the current local `feat/logical-ui-scaling` checkout, preserve
unrelated work, commit logical slices frequently, never push, and avoid destructive
Git operations.

Every new file created by this work must stay below 20 KiB and approximately
600 lines. Existing files do not need refactoring solely for that limit.

## Purpose / Big Picture

The presentation refactor moved `SlidingWindow`, `TopMenu`, and
`SideMenuContainer` into `PresentationHost`. Fade behavior now exposes defects
inherited from `master` plus regressions specific to the new container-based path.

Two effects are independent:

    FadeAnimation       fades a control subtree
    fadeOtherWindows    dims content behind TopMenu

`SideMenuContainer` intentionally uses:

    topMenu.fadeOtherWindows = true;
    topMenu.setFadeOnPopAndUnpop(false);

After this plan, direct fade, slide-plus-fade, interruption cleanup, nested
screenshots, logical density/font scale, and background dimming must work.
Real `Window.fadeOtherWindows` must remain unchanged.

## Progress

- [x] (2026-08-08 20:18Z) Recorded baseline at `91616c934fe547c7b1b01c4a2990cb17ac865241` on the actual current branch `feat/logical-ui-scaling2`; preserved the unrelated dirty worktree.
- [x] (2026-08-08 20:18Z) Classified `ControlAnimation` and `FadeAnimation` lifecycle defects as inherited unchanged from `master`; classified screenshot scaling and presentation integration as branch-exposed work because `Control.java` differs from `master`.
- [x] (2026-08-08 20:42Z) Added and ran focused pre-fix tests for nonzero standalone/composite fades, immediate abort, caller-owned snapshots, nested target-local painting, transparency, and logical scales. The suite failed in the expected lifecycle, ownership, coordinate, and scale areas; final pre-fix log: `TotalCrossSDK/agent-logs/20260808-004228-test-full.log`.
- [x] (2026-08-08 20:44Z) Repaired `ControlAnimation` composition, per-animation screenshot ownership, slave cleanup, immediate initial alpha, and bounded exception-safe update suppression. `ControlAnimationFadeTest` passed; full log: `TotalCrossSDK/agent-logs/20260808-004424-test-full.log`.
- [x] (2026-08-08 20:46Z) Repaired target-local screenshot traversal and preserved destination content/font scales with logical image backing. Screenshot, fade, graphics-scale, and logical-text-scale tests passed; full log: `TotalCrossSDK/agent-logs/20260808-004551-test-full.log`.
- [x] (2026-08-08 20:51Z) Made the presentation frame transparent and verified it as the common direct/composite fade target with owned snapshot cleanup.
- [x] (2026-08-08 20:51Z) Restored `fadeOtherWindows` through a full-host translucent `PresentationBarrier`; `Window.fadeValue` pixel mapping and SideMenu full-opacity behavior pass.
- [x] (2026-08-08 20:51Z) Added nonzero-duration presentation fade coverage for directional, centered, relayout-abort, repeated, and SideMenu transitions; full log: `TotalCrossSDK/agent-logs/20260808-005111-test-full.log`.
- [x] (2026-08-08 20:54Z) Audited TopMenu, SlidingWindow, Toast, VirtualKeyboard, TabbedContainer, and ScrollContainer. No consumer-specific production change was needed; added a generic nonzero partial-fade reuse assertion for Toast's `maxFade` contract.
- [ ] Run focused Java validation, one non-clean SDK build, JavaSE smoke, and one macOS native smoke if the existing path remains usable.
- [ ] Complete evidence, static checks, file-size checks, and retrospective.

Use UTC timestamps when updating Progress.

## Context and Orientation

Relevant code is under `TotalCrossSDK/src/main/java/totalcross/ui/`, especially
`Control`, `Window`, `TopMenu`, `SideMenuContainer`, the `Presentation*`
classes, `FadePresentationTransition`, `SlidePresentationTransition`,
`anim/ControlAnimation`, `anim/FadeAnimation`, `anim/PathAnimation`, and
`image/Image`.

Current shape:

    real Window
      PresentationHost
        PresentationBarrier
        safe viewport
          frame
            content

`Control.offscreen` is the snapshot used by `FadeAnimation`.
`ControlAnimation.with(...)` composes animations; the root owns the update clock.

## Known Defects

Expected inherited defects from `master`:

- slave FadeAnimation can skip offscreen preparation;
- screenshot cleanup follows root/slave status instead of ownership;
- abort before first tick can leave `Control.enableUpdateScreen == false`;
- `Control.paint2shot()` mixes root-local and absolute coordinates.

Specific to, or exposed by, `logical-ui-scaling`:

- fade screenshots are created at scale 1;
- snapshot Graphics may use the wrong font scale;
- an opaque `PresentationHandle.frame` can bake a background into the screenshot;
- the presentation barrier intercepts input but does not visually implement
  `fadeOtherWindows`;
- current presentation tests use duration zero.

## Scope Boundaries

Keep screenshot-based FadeAnimation.

Do not add a public opacity/compositor API.

Do not change real `Window.fadeOtherWindows` or use `Graphics.fadeScreen` inside
`PresentationHost`.

Do not redesign Toast, VirtualKeyboard, TabbedContainer, ScrollContainer, or other
consumers unless focused tests prove a regression caused by these fixes.

Keep existing public signatures source-compatible.

## Plan of Work

### Milestone 0: Establish the baseline

Record:

    git rev-parse HEAD
    git branch --show-current
    git status --short
    git diff --check

Compare generic animation files with master:

    git diff master --       TotalCrossSDK/src/main/java/totalcross/ui/anim/ControlAnimation.java       TotalCrossSDK/src/main/java/totalcross/ui/anim/FadeAnimation.java

If no diff exists, record those defects as inherited unchanged from master.

Compare `Control.java` separately:

    git diff master -- TotalCrossSDK/src/main/java/totalcross/ui/Control.java

Inspect only:

    Control.takeScreenShot
    Control.paint2shot
    Control.refreshGraphics
    ControlAnimation.start/stop/with
    FadeAnimation.start/animate
    PresentationHandle
    SlidePresentationTransition
    FadePresentationTransition
    PresentationBarrier
    TopMenu.createPresentationEntry

Do not edit production code before Milestone 1 tests exist.

### Milestone 1: Reproduce generic failures

Create:

    TotalCrossSDK/src/test/java/totalcross/ui/anim/ControlAnimationFadeTest.java
    TotalCrossSDK/src/test/java/totalcross/ui/ControlScreenshotTest.java

`ControlAnimationFadeTest` covers:

    standalone nonzero-duration fade
    PathAnimation.with(FadeAnimation)
    immediate abort before first tick
    preexisting caller-owned offscreen
    preexisting offscreen0

At an intermediate composite frame assert movement plus:

    0 < offscreen.alphaMask < FadeAnimation.maxFade

The abort test must prove `Control.enableUpdateScreen` returns to its original
value.

`ControlScreenshotTest` proves:

    nested target at nonzero parent x/y uses target-local coordinates
    transparent root keeps untouched pixels transparent
    contentScale=3 preserves logical size and creates 3x physical backing
    screenshot Graphics contentScale/fontScale match the target

Run:

    cd TotalCrossSDK
    ./gradlew-agent test       --tests 'totalcross.ui.anim.ControlAnimationFadeTest'       --tests 'totalcross.ui.ControlScreenshotTest'       --no-daemon --console=plain

Record expected pre-fix failures. Do not weaken tests.

Suggested commit: `test(ui): cover fade animation lifecycle`.

### Milestone 2: Repair composite lifecycle and ownership

Edit:

    TotalCrossSDK/src/main/java/totalcross/ui/anim/ControlAnimation.java
    TotalCrossSDK/src/main/java/totalcross/ui/anim/FadeAnimation.java
    TotalCrossSDK/src/main/java/totalcross/ui/Control.java

Keep `ControlAnimation.with(...)` source-compatible.

Separate:

    driven by another animation
    initialized/cleaned independently

A slave must not register another MainWindow listener, but must initialize and
clean its own resources.

Track screenshot ownership. An animation owns `offscreen` only when it found it
null and created it.

Cleanup must:

    release only owned snapshots
    honor releaseScreenShot
    preserve caller-owned offscreen
    preserve unrelated offscreen0
    not depend on the PathAnimation root

Add a package-private Control helper if needed to release one screenshot slot while
leaving public `releaseScreenShot()` unchanged.

Make update suppression exception-safe and bounded:

    save previous Control.enableUpdateScreen
    suppress only while screenshot + initial alpha are prepared
    restore previous value in finally

Do not wait for a future animation tick.

Initialize FadeAnimation's initial alpha immediately after its screenshot exists,
preferably through a protected/package-private startup hook.

Do not redesign `then(...)` unless a focused test proves it necessary.

Audit `.with(...)` users, especially TopMenu and VirtualKeyboard.

Run:

    ./gradlew-agent test       --tests 'totalcross.ui.anim.ControlAnimationFadeTest'       --no-daemon --console=plain

Suggested commit: `fix(ui): correct composite fade lifecycle`. Keep it generic
where practical because the defect exists in master.

### Milestone 3: Repair screenshot geometry and scaling

Edit `Control.java`.

A screenshot of `C` represents:

    (0, 0, C.width, C.height)

Paint descendants with coordinates accumulated relative to `C`. Do not compare
the root's parent-relative rect with descendant absolute rects.

Do not clip the snapshot to current ancestor position. A presentation frame may
start outside the safe viewport but still needs a complete snapshot; the live
viewport clips it later.

Preserve special handling required when the root itself is a Window.

Create the image using target destination scale:

    Image.createLogical(width, height, gfx.getContentScale())

Configure screenshot Graphics with target contentScale and fontScale.

Do not derive scale from `Settings.screenDensity`.

Transparent screenshot roots must stay transparent and must not be automatically
filled with `parent.backColor`.

Run:

    ./gradlew-agent test       --tests 'totalcross.ui.ControlScreenshotTest'       --tests 'totalcross.ui.anim.ControlAnimationFadeTest'       --tests 'totalcross.ui.gfx.GraphicsScaleTest'       --tests 'totalcross.ui.LogicalTextScaleTest'       --no-daemon --console=plain

Suggested commit: `fix(ui): preserve fade snapshot geometry and scale`.

### Milestone 4: Make the presentation frame fade-safe

Edit:

    TotalCrossSDK/src/main/java/totalcross/ui/PresentationHandle.java
    TotalCrossSDK/src/main/java/totalcross/ui/FadePresentationTransition.java
    TotalCrossSDK/src/main/java/totalcross/ui/SlidePresentationTransition.java

Set:

    frame.transparentBackground = true;

before screenshots can be created.

Keep direct fade targeting the frame. Keep slide and fade targeting the same frame.
Generic `.with(...)` must handle the composite without presentation-specific
screenshot setup.

Add a nonzero-duration transition test. At an intermediate frame prove:

    position is intermediate
    frame.offscreen exists
    alphaMask is intermediate

At completion prove final bounds, owned offscreen release, and restored
`Control.enableUpdateScreen`.

Preserve legacy dismissal behavior: user dismiss while `PRESENTING` should not
reverse the transition. Ignore or defer it.

Safe-area/owner relayout may abort and stabilize at final geometry, as decided by
the previous plan; that path must clean fade resources and restore rendering state.

Do not implement reversible transitions.

### Milestone 5: Restore fadeOtherWindows through PresentationBarrier

Edit:

    TotalCrossSDK/src/main/java/totalcross/ui/PresentationEntry.java
    TotalCrossSDK/src/main/java/totalcross/ui/PresentationBarrier.java
    TotalCrossSDK/src/main/java/totalcross/ui/TopMenu.java

Legacy dimming multiplies RGB by:

    Window.fadeValue / 255

Equivalent black-overlay alpha:

    barrierAlpha = 255 - clamp(Window.fadeValue, 0, 255)

With default `Window.fadeValue == 128`, expected alpha is 127.

Make package-private PresentationEntry carry barrier color and alpha separately.
Input interception remains independent from visual transparency.

`PresentationBarrier` must paint the translucent color using existing Control
translucent drawing; do not call `Graphics.fadeScreen`.

The barrier fills the complete PresentationHost area. The menu remains inside the
safe viewport.

In `TopMenu.createPresentationEntry()`:

    fadeOtherWindows:
        barrierColor = black
        barrierAlpha = 255 - clamp(Window.fadeValue, 0, 255)

    otherwise:
        visually transparent barrier

Do not connect this to `fadeOnPopAndUnpop`.

Required SideMenu behavior:

    drawer slides fully opaque
    background is dimmed
    barrier blocks input
    existing outside-dismiss behavior remains

Do not animate barrier opacity and do not modify real Window fade behavior.

Suggested commit: `fix(ui): restore presentation background dimming`.

### Milestone 6: Add presentation fade regressions

Prefer one new file:

    TotalCrossSDK/src/test/java/totalcross/ui/PresentationFadeTest.java

Cover:

    SideMenu defaults -> self-fade=false, dim barrier=true
    Window.fadeValue=128 -> barrierAlpha=127
    barrier pixels -> 255 unchanged, 128 about half, 0 black
    directional TopMenu -> intermediate slide + alpha
    SideMenu -> opaque frame + dim barrier
    centered presentation -> direct FadePresentationTransition
    relayout during fade -> stable state, new geometry, no owned offscreen,
                            updateScreen restored
    present/dismiss/present -> fresh fade state

Restore in test cleanup:

    Control.enableUpdateScreen
    FadeAnimation.maxFade
    Window.fadeValue
    Settings.screenWidth/Height
    safe-area insets
    Window.topMost
    Window.zStack

Run:

    ./gradlew-agent test       --tests 'totalcross.ui.anim.ControlAnimationFadeTest'       --tests 'totalcross.ui.ControlScreenshotTest'       --tests 'totalcross.ui.PresentationFadeTest'       --tests 'totalcross.ui.PresentationHostTest'       --tests 'totalcross.ui.TopMenuSafeAreaTest'       --tests 'totalcross.ui.SideMenuPresentationTest'       --tests 'totalcross.ui.SlidingWindowPresentationTest'       --tests 'totalcross.ui.gfx.GraphicsScaleTest'       --tests 'totalcross.ui.LogicalTextScaleTest'       --no-daemon --console=plain

Fade assertions must use nonzero-duration transitions.

Suggested commit: `test(ui): cover presentation fade behavior`.

### Milestone 7: Audit existing consumers

Search for:

    FadeAnimation.create
    .with(
    takeScreenShot()
    offscreen
    offscreen0

Inspect at least TopMenu, SlidingWindow, Toast, VirtualKeyboard, TabbedContainer,
and ScrollContainer.

Do not refactor merely because a consumer appears.

Toast is important because it uses partial fade through `FadeAnimation.maxFade`
and chains fade-in/out with `then(...)`. Preserve partial-fade behavior and do not
force-release an offscreen when `releaseScreenShot == false`.

Add a consumer-specific regression only if the generic fix changes existing
behavior.

### Milestone 8: Final validation and smoke

Run the complete focused suite from Milestone 6 first.

Then build once without `clean`:

    cd TotalCrossSDK
    ./gradlew-agent dist -x test --no-daemon --console=plain       > ../artifacts/fade-presentation/logs/sdk-dist.log 2>&1

Compile smoke sources:

    ./gradlew-agent smokeTestClasses --no-daemon --console=plain       > ../artifacts/fade-presentation/logs/smoke-compile.log 2>&1

Reuse the previous presentation smoke infrastructure. Add
`PresentationFadeSmoke.java` only if extending the existing smoke would mix
unrelated concerns.

Use a nonzero-duration fade and report:

    compositeFadeStarted=true
    compositeFadeCompleted=true
    fadeOtherWindowsBarrier=true
    updateScreenEnabled=true
    offscreenReleased=true
    final=PASS

JavaSE pixel tests are the deterministic alpha proof.

If the previous native macOS deployment path remains usable, run one native macOS
smoke after Java validation. Do not build/deploy Android or iOS unless a Java/native
discrepancy proves a platform-specific issue.

Native smoke should prove fade completion, no rendering freeze, cleanup, and a
successful second presentation. Do not repeat a successful native build
unnecessarily.

Finish with:

    git diff --check
    git diff --stat
    git status --short

Run the copyright validator on changed source/test files and check every new
file with `wc -c` and `wc -l`; keep it below 20 KiB and approximately 600 lines.

## Validation and Acceptance

Complete only when:

- standalone FadeAnimation reaches intermediate/final alpha without stale owned state;
- `PathAnimation.with(FadeAnimation)` changes position and alpha together;
- nested controls fade correctly regardless of ancestor offsets;
- an offscreen presentation frame is captured completely;
- content scale 3 produces a 3x physical snapshot with correct logical size/font scale;
- transparent frames do not introduce an opaque rectangle;
- immediate abort and relayout-abort cannot leave updates disabled;
- SideMenu slides fully opaque while content behind it is dimmed;
- `Window.fadeValue == 128` produces approximately legacy-equivalent brightness;
- `fadeOtherWindows=false` affects visual dimming only;
- `setFadeOnPopAndUnpop(false)` affects self-fade only;
- real Window fade behavior remains unchanged;
- safe-area, clipping, z-stack, and logical-scaling regressions remain green.

## Validation Evidence and Commits

The complete Milestone 6 focused suite passed on 2026-08-08 at
`TotalCrossSDK/agent-logs/20260808-005210-test-full.log`. Local commits created so
far, in order, are:

    1d1957c56  test(ui): cover fade animation lifecycle
    8669e03cd  fix(ui): correct composite fade lifecycle
    d9cc9e9f5  fix(ui): preserve fade snapshot geometry and scale
    87ec52e21  fix(ui): restore presentation background dimming
    d3a4950f9  test(ui): cover presentation fade behavior

## Suggested Commit Boundaries

Prefer:

    test(ui): cover fade animation lifecycle
    fix(ui): correct composite fade lifecycle
    fix(ui): preserve fade snapshot geometry and scale
    fix(ui): restore presentation background dimming
    test(ui): cover presentation fade behavior
    test(ui): add fade presentation smoke coverage

Do not squash into previous presentation commits and do not push.

## Idempotence and Recovery

Do not use destructive Git operations or reset the branch to master.

Before each commit, inspect the active diff and stage only that logical slice.

If generic changes break Toast or VirtualKeyboard, verify the shared ownership and
sequencing contract first; fix the shared mechanism rather than patching consumers
independently.

Screenshot allocation failure must always restore the previous
`Control.enableUpdateScreen` value.

Repeated present/dismiss cycles must not retain stale handles, animations, or
screenshots.

## Surprises & Discoveries

- Observation: The current checkout is named `feat/logical-ui-scaling2`, while the request and initial plan name `feat/logical-ui-scaling`.
  Evidence: `git branch --show-current` returned `feat/logical-ui-scaling2` at baseline. Work continues on the current checkout as requested, without switching or resetting branches.

- Observation: The generic animation lifecycle implementation is byte-for-byte unchanged from `master`, but `Control.java` has 121 insertions and 41 deletions relative to `master`.
  Evidence: `git diff master -- .../ControlAnimation.java .../FadeAnimation.java` was empty; `git diff --stat master -- .../Control.java` reported one changed file.

- Observation: The first focused execution produced five animation lifecycle/ownership failures and two screenshot failures, while the transparent-root guard already passed.
  Evidence: `ControlAnimationFadeTest` failed all five cases; `ControlScreenshotTest` failed target-local red-pixel capture (`expected 0xFF0000, was 0`) and physical scale (`expected 36, was 12`).

- Observation: Existing consumers split into animation-owned snapshots (TopMenu, SlidingWindow, VirtualKeyboard), explicit caller-owned snapshots (TabbedContainer, ScrollContainer), and retained partial-fade snapshots (Toast).
  Evidence: only animation code and presentation transitions use `.with(FadeAnimation...)`; TabbedContainer and ScrollContainer explicitly pair screenshot creation/release; Toast sets `maxFade < 255` and intentionally reuses the retained image for fade-out.

- Observation: The Toast-style retained partial fade exposed a final-frame ordering bug: `FadeAnimation` assigned `a = af` only after writing the screenshot alpha.
  Evidence: the new nonzero partial-fade test retained `alphaMask=88` when the expected final `maxFade` was 128. The fix remains generic by writing the final value before either screenshot slot is updated.

- Observation: `.with(...)` can prevent slave FadeAnimation offscreen preparation.
  Consequence: fix generic animation lifecycle, not PresentationHost.

- Observation: TopMenu self-fade intent survived migration.
  Consequence: missing self-fade is mainly infrastructure/lifecycle.

- Observation: SideMenu intentionally combines dimming with self-fade disabled.
  Consequence: keep these properties independent.

- Observation: the previous presentation plan already recorded
  `fadeOtherWindows` visual parity as remaining work.

- Observation: screenshot correctness is target-local; ancestor position must not
  truncate an offscreen animation frame.

Add only discoveries that materially change remaining work.

## Decision Log

- Decision: Store the exact `Image` created by each animation rather than a control-wide ownership flag.
  Rationale: identity checking prevents an animation from clearing a caller replacement and permits `offscreen0` to remain entirely independent without adding public API.
  Date/Author: 2026-08-08 / Codex.

- Decision: Remove absolute-rectangle prefiltering from screenshot descendant traversal and rely on the screenshot graphics clip plus descendant-local `refreshGraphics` offsets.
  Rationale: absolute prefiltering mixes coordinate spaces and clips frames based on their live ancestor position; the image surface already bounds painting to the target-local snapshot.
  Date/Author: 2026-08-08 / Codex.

- Decision: Ignore dismissal while a presentation is actively entering.
  Rationale: this preserves the legacy non-reversing behavior required by the plan; relayout remains the explicit abort-and-stabilize path.
  Date/Author: 2026-08-08 / Codex.

- Decision: Keep screenshot-based FadeAnimation.
  Rationale: renderer-level subtree opacity is outside this repair.
  Date/Author: 2026-08-08 / plan author.

- Decision: Fix generic composite lifecycle before PresentationHost behavior.
  Rationale: the core defect exists in master.
  Date/Author: 2026-08-08 / plan author.

- Decision: Track screenshot ownership explicitly.
  Rationale: only the creator may safely release it.
  Date/Author: 2026-08-08 / plan author.

- Decision: Bound update suppression to screenshot initialization.
  Rationale: restoration must not depend on a future tick.
  Date/Author: 2026-08-08 / plan author.

- Decision: Capture in root-local coordinates and preserve destination
  contentScale/fontScale.
  Rationale: nested/logical-scale fades must match live rendering.
  Date/Author: 2026-08-08 / plan author.

- Decision: Implement presentation dimming as a black translucent barrier with
  alpha `255 - Window.fadeValue`.
  Rationale: equivalent to legacy brightness without mutating the rendered screen.
  Date/Author: 2026-08-08 / plan author.

- Decision: Leave real Window fade unchanged and keep dimming independent from
  self-fade.
  Rationale: SideMenu depends on that separation.
  Date/Author: 2026-08-08 / plan author.

- Decision: Do not implement reversible transitions.
  Rationale: preserve legacy no-dismiss-while-entering semantics.
  Date/Author: 2026-08-08 / plan author.

## Outcomes & Retrospective

At completion record which defects were inherited from master versus exposed by
presentation migration, which fixes are independently cherry-pickable, affected
legacy consumers, JavaSE pixel evidence, native macOS smoke, and remaining limits.

Success means safe-area presentation remains intact while fade behavior becomes
more correct than both master and the first container-based implementation.

## Revision Note

2026-08-08: Initial plan. It separates generic fade/screenshot repairs from
PresentationHost visual-parity work, restores SideMenu dimming, preserves logical
scaling during fade, requires nonzero-duration tests, and limits native smoke to
macOS after focused Java validation.

2026-08-08 20:18Z: Began execution, recorded the exact baseline and dirty-worktree
constraints, documented the branch-name deviation, classified inherited versus
branch-exposed defects, and added the first focused pre-fix tests.

2026-08-08 20:42Z: Finished Milestone 1 reproduction with nonzero-duration
tests and recorded exact expected failure evidence before production changes.

2026-08-08 20:44Z: Completed and focused-tested the generic composite lifecycle
and screenshot ownership repair, including immediate-abort cleanup.

2026-08-08 20:46Z: Completed and focused-tested target-local, transparent,
logical-scale screenshot capture without clipping to live ancestor position.

2026-08-08 20:51Z: Completed presentation-frame transparency and barrier dimming,
then passed the first nonzero presentation lifecycle and pixel suite.

2026-08-08 20:54Z: Audited legacy fade and screenshot consumers and added a
generic retained-partial-fade regression without changing consumer code.

2026-08-08 20:55Z: The retained-partial-fade regression exposed and verified a
generic final-alpha ordering repair; focused log:
`TotalCrossSDK/agent-logs/20260808-005436-test-full.log`.
