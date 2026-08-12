<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Safe presentation execution state

## Current position

- Status: complete
- Active milestone: none; implementation and handoff are complete
- Design section: all implementation-guide sections completed
- Next action: none; retain this file as the resumable final state
- Base commit: `62c9c728cd0570c1e1a8219b42dfd72c6fedd355`
- Current branch: `feat/logical-ui-scaling2`
- Planning commit: `9a3b22ae1`
- Clipping commits: `536a7984c`, `c6e2f90bc`
- Presentation foundation commit: `cd5082a1d`
- Sliding/material commit: `631badefd`
- TopMenu commit: `565b89e37`
- TopMenu sample compile fix: `f0a918d97`
- SideMenu commit: `56544d833`
- Menu milestone record: `b843b64f0`
- Smoke commit: `f1601b2e6`

## Preserved baseline

The checkout already contains the opt-in `PathAnimation` orthogonal-position
behavior, safe-area-aware animation staging in `SlidingWindow` and `TopMenu`,
and explicit `FILL` width for the `MaterialWindow` bar. These changes are local
source-of-truth work and must not be reconstructed from another worktree.

Unrelated untracked work exists under `.agent/` for image-text rendering and
other plans. It is outside this task and must remain unstaged.

## Completed validation

- `git diff --check` for the plan and design: passed.
- focused copyright validation for the plan and design: passed, 2 files.
- plan/design file-size check: passed; 20,309/463 and 18,779/496 bytes/lines.
- `ContainerClippingTest`: passed; log `m1-container-clipping.log`.
- `ClippedContainerTest`: passed; log `m1-clipped-container.log`.
- focused Milestone 1 copyright and static checks: passed.
- `PresentationHostTest` plus `SafeAreaLayoutTest`: passed; log
  `m2-presentation-host.log`.
- focused Milestone 2 copyright, static, and new-file size checks: passed.
- `SlidingWindowPresentationTest` plus `PresentationHostTest`: passed; log
  `m3-sliding-window.log`.
- focused Milestone 3 copyright, static, and new-file size checks: passed.
- `TopMenuSafeAreaTest`: passed; log `m4-top-menu.log`.
- `SideMenuPresentationTest` plus `TopMenuSafeAreaTest`: passed; log
  `m4-side-menu.log`.
- focused Milestone 4 copyright, static, and new-file size checks: passed.
- final focused suite: passed, 16 tests; log `final-focused-tests.log`.
- non-clean `dist -x test`: passed; log `sdk-dist.log`.
- smoke compilation: passed; log `smoke-compile.log`.
- JavaSE smoke: passed with safe viewport `20,10,260,600`; log
  `javase-smoke.log`.
- macOS CMake configure and `tcvm` build: passed; logs `macos-cmake.log` and
  `macos-tcvm.log`.
- macOS deploy and direct native smoke: passed with safe viewport
  `20,10,1668,941`; logs `native-deploy.log` and `native-macos-smoke.log`.
- source and deployed `libtcvm.dylib` SHA-256 matched:
  `fccd8da2a253d409611b11822606e9521f92d5771e762f1613c0fe0c38986db5`.
- final changed-file copyright, static, compatibility, and new-file size
  checks: passed; see the evidence ledger.

## Deferred validation

Android and iOS builds and deployments were intentionally not run. The request
excluded both platforms. No clean/full distribution was run because the
non-clean SDK distribution plus focused and native smoke validation was
sufficient under the repository escalation policy.

## New files

- `.agent/ui-presentation-safe-area-execplan.md`
- `.agent/design/ui-presentation-safe-area-implementation.md`
- `.agent/state/ui-presentation-safe-area.md`
- `.agent/evidence/ui-presentation-safe-area.md`
- `.agent/archive/ui-presentation-safe-area-history.md`
- `TotalCrossSDK/src/test/java/totalcross/ui/ContainerClippingTest.java`
- `TotalCrossSDK/src/test/java/totalcross/ui/ClippedContainerTest.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/Presentation*.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/SlidePresentationTransition.java`
- `TotalCrossSDK/src/test/java/totalcross/ui/PresentationHostTest.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/FadePresentationTransition.java`
- `TotalCrossSDK/src/test/java/totalcross/ui/SlidingWindowPresentationTest.java`
- `TotalCrossSDK/src/test/java/totalcross/ui/SideMenuPresentationTest.java`
- `TotalCrossSDK/src/main/java/totalcross/sample/components/ui/TopMenuSample.java`
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/PresentationSafeAreaSmoke.java`
- `.agent/reports/ui-presentation-safe-area-editorial.md`

## Final limitations

- `SlidingWindow` and `TopMenu` are no longer assignable to `Window`; their
  repository-used source APIs compile and the affected sample was updated.
- TopMenu title/border styling has focused state/layout tests but no screenshot
  equivalence test.
- `fadeOtherWindows` remains as compatibility state, while the new presentation
  barrier is transparent; equivalent visual dimming remains future work.
- Native macOS smoke uses programmatically injected nonzero insets and proves
  runtime behavior, not physical-notch integration.

## Resume rule

Read this file, then the active milestone and named design section only. Inspect
scoped diffs before edits or staging.
