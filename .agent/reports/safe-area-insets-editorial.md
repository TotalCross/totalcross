<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Safe-area insets editorial report

## Editorial Summary

Execution is complete on `feat/logical-ui-scaling`. All five implementation and
validation milestones are delivered in focused local commits; nothing was
pushed or published.

## Scope

This work covers window safe-area policy, per-control safe/full-bleed layout,
container safe padding, scroll content insets, menu integration, and dynamic
Android/iOS delivery. Display-cutout visuals, panel clipping and shapes, IME
insets, packaging, and unrelated layout changes remain excluded.

## Original Plan versus Current Outcome

The outcome matches the plan. Window policy, control safe/full-bleed override,
container safe padding, and dynamic SDK cache transition are implemented.
`ScrollContainer` content insets now extend scrollable content independently of
the viewport and preserve origin, visible-content, and trailing-edge anchors.
`TopMenu` supports fixed safe-padded bars with Reddit (`NONE`), ChatGPT (`TOP`),
Gmail (`BOTTOM`), and dual-overlay (`BOTH`) behavior; `SideMenuContainer`
forwards those APIs.
Android and iOS now deliver physical safe-area changes through dedicated event
paths to one logical native helper; neither path reports a graphics-surface
change.

Compatibility is retained for declared and negative insets, `ignoreInsets`,
launcher portrait/landscape presets, `TopMenu.header`, `scInsets`, constructors,
animations, selection, and auto-close behavior. Platform pull queries remain a
one-time startup fallback.

## Decisions and Trade-offs

Declared user insets remain distinct from effective safe-area insets. A
consumed-edge mask prevents nested safe padding from duplicating already applied
window exclusion. Scroll anchors distinguish origin, middle, and trailing
positions. Fixed bars use full-bleed hosts with edge-specific safe padding.
Android retains the latest values across Java and native startup boundaries.
Missing device smoke does not justify an extra build.

## Validation and Measurable Results

`SafeAreaLayoutTest` passed 7 tests with no failures. No build was run, matching
the milestone-1 gate. `ScrollContainerContentInsetsTest` passed 5 tests with no
failures. The combined milestone-3 suite passed 19 tests, and the sole SDK
distribution checkpoint passed in 25 seconds.
The milestone-4 focused subset passed 14 tests, and the sole Android
`assembleStandardDebug` checkpoint passed in 28 seconds. iOS was statically
reconciled and not built, as required.

The final complete focused run passed 19 tests in 2 seconds. Focused copyright
validation passed for 22 supported changed files, Objective-C headers were
reconciled to the required 2026 chain, and the 24-file committed task diff passed
`git diff --check`. New execution-created source, test, and support files satisfy
the size limit.

## Limitations and Remaining Work

Android device smoke was unavailable because no device was attached and the
generic checkpoint APK contains no launchable safe-area demo fixture. iOS device
smoke was unavailable because no pre-existing `.app` artifact exists. Full test
suites, repeated checkpoint builds, other platform builds, clean tasks,
packaging, and publishing were intentionally skipped by the plan.

## Claims Requiring Human Review

Human/device follow-up may visually confirm rotation, negative cancellation,
the three menu presets plus `BOTH`, and touch alignment on Android and iOS when
devices and runnable fixtures are available. No code acceptance item remains
open.
