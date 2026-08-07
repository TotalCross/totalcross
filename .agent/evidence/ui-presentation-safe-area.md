<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Safe presentation evidence

## Baseline

- Branch: `feat/logical-ui-scaling2`
- Base commit: `62c9c728cd0570c1e1a8219b42dfd72c6fedd355`
- Planning commit: `9a3b22ae1`
- Prior commits preserved: `d848b627b` safe-area animation layout and
  `62c9c728c` explicit top-bar width.
- `PathAnimation.create(..., preserveOrthogonalPosition)` is present for legacy
  callers; the new presentation foundation will use explicit local coordinates.

## Validation ledger

| Milestone | Command or evidence | Result |
| --- | --- | --- |
| 0 | scoped plan/design `git diff --check` | PASS |
| 0 | copyright validator for plan/design | PASS (2 files) |
| 0 | `wc -lc` for plan/design | PASS; both below 20 KiB and 600 lines |
| 1 | `ContainerClippingTest` | PASS; 1 test |
| 1 | `ClippedContainerTest` | PASS; 2 tests |
| 1 | focused header/static checks | PASS |
| 2 | `PresentationHostTest`, `SafeAreaLayoutTest` | PASS; 8 tests |
| 2 | focused header/static/new-file size checks | PASS |
| 3 | `SlidingWindowPresentationTest`, `PresentationHostTest` | PASS; 2 tests |
| 3 | focused header/static/new-file size checks | PASS |
| 4 | `TopMenuSafeAreaTest` | PASS; 2 tests |
| 4 | `SideMenuPresentationTest`, `TopMenuSafeAreaTest` | PASS; 4 tests |
| 4 | focused header/static/new-file size checks | PASS |
| 5 | final focused Java suite | PASS; 16 tests |
| 5 | non-clean `dist -x test` | PASS |
| 5 | `smokeTestClasses` | PASS |
| 5 | JavaSE smoke | PASS; `final=PASS` |
| 5 | macOS CMake configure and `tcvm` build | PASS |
| 5 | macOS jar deploy and direct native run | PASS; `final=PASS` |
| 5 | source/deployed dylib SHA-256 comparison | PASS; hashes identical |
| 5 | final copyright, static, compatibility, and file-size checks | PASS |

Milestone 1 commits are `536a7984c` (explicit default clipping) and
`c6e2f90bc` (visibility search and sentinel correctness). The initial two test
runs failed only because their fixtures lacked deterministic launcher/screen
initialization; the corrected fixtures passed without production-code changes.

Milestone 2 commit `cd5082a1d` proves a safe viewport of
`20,10,260,600`, bottom outside origin `0,600,260,600`, unchanged owner/z-stack,
dynamic relayout to `24,12,252,596`, retained content identity, and clean
idempotent dismissal. The first run exposed the parent-bounds requirement and
the corrected deferred layout passed.

Milestone 3 commit `631badefd` proves LEFT/RIGHT/TOP/BOTTOM origins from the
safe viewport, 7-unit relative slack, one provider creation across relayout,
ordered popup/unpop callbacks, idempotent dismissal, and unchanged z-stack.

Milestone 4 commits `565b89e37`, `f0a918d97`, and `56544d833` prove local bar
geometry without repeated safe insets, retained body identity, outside
dismissal, 204-unit safe drawer sizing, 180-unit explicit override, local
gesture propagation, and successful compilation after the superclass change.

Milestone 5 commit `f1601b2e6` adds the smoke fixture. The final focused command
ran `ContainerClippingTest`, `ClippedContainerTest`, `PresentationHostTest`,
`SlidingWindowPresentationTest`, `TopMenuSafeAreaTest`,
`SideMenuPresentationTest`, and `SafeAreaLayoutTest`; all 16 tests passed.

The JavaSE smoke reported:

    ownerWindowUnchanged=true
    zStackDelta=0
    safeViewport=20,10,260,600
    slidingFinal=0,0,260,600
    topMenuFinal=0,0,204,600
    clippingPass=true
    final=PASS

The macOS smoke was packaged as a jar because `tc.Deploy` correctly rejected a
single class in the reserved `totalcross.*` package. The jar retry deployed
successfully. The current and deployed dylib SHA-256 values were both
`fccd8da2a253d409611b11822606e9521f92d5771e762f1613c0fe0c38986db5`.
The direct native run reported:

    ownerWindowUnchanged=true
    zStackDelta=0
    safeViewport=20,10,1668,941
    slidingFinal=0,0,1668,941
    topMenuFinal=0,0,320,941
    clippingPass=true
    final=PASS

Verbose Gradle, CMake, deploy, and smoke logs are stored under
`artifacts/ui-presentation-safe-area/logs/`. Wrapper logs are under
`TotalCrossSDK/agent-logs/`.

The final compatibility evidence is the successful SDK distribution, which
compiles repository consumers after the superclass changes. The existing
`TopMenuSample.java` local source had to become tracked so its close action
could call `topMenu.unpop()` without an invalid `Window` cast.
