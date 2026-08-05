<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Safe-area insets editorial report

## Editorial Summary

Execution is in progress on `feat/logical-ui-scaling`. Milestones 1 and 2
delivered the core SDK and scrolling models; menu integration is active.

## Scope

This work covers window safe-area policy, per-control safe/full-bleed layout,
container safe padding, scroll content insets, menu integration, and dynamic
Android/iOS delivery. Display-cutout visuals, panel clipping and shapes, IME
insets, packaging, and unrelated layout changes remain excluded.

## Original Plan versus Current Outcome

The baseline matched the plan. Window policy, control safe/full-bleed override,
container safe padding, and dynamic SDK cache transition are implemented.
`ScrollContainer` content insets now extend scrollable content independently of
the viewport and preserve origin, visible-content, and trailing-edge anchors.

## Decisions and Trade-offs

Declared user insets remain distinct from effective safe-area insets. A
consumed-edge mask prevents nested safe padding from duplicating already applied
window exclusion. Platform work will call the dedicated transition rather than
surface resize events.

## Validation and Measurable Results

`SafeAreaLayoutTest` passed 7 tests with no failures. No build was run, matching
the milestone-1 gate. `ScrollContainerContentInsetsTest` passed 5 tests with no
failures; the SDK checkpoint remains reserved for milestone 3.

## Limitations and Remaining Work

Milestones 3 through 5 remain.

## Claims Requiring Human Review

None at this stage.
