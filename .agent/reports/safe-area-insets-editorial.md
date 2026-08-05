<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Safe-area insets editorial report

## Editorial Summary

Execution is in progress on `feat/logical-ui-scaling`. Milestone 1 delivered
the core SDK model; scroll content insets are the active slice.

## Scope

This work covers window safe-area policy, per-control safe/full-bleed layout,
container safe padding, scroll content insets, menu integration, and dynamic
Android/iOS delivery. Display-cutout visuals, panel clipping and shapes, IME
insets, packaging, and unrelated layout changes remain excluded.

## Original Plan versus Current Outcome

The baseline matched the plan. Window policy, control safe/full-bleed override,
container safe padding, and dynamic SDK cache transition are implemented.

## Decisions and Trade-offs

Declared user insets remain distinct from effective safe-area insets. A
consumed-edge mask prevents nested safe padding from duplicating already applied
window exclusion. Platform work will call the dedicated transition rather than
surface resize events.

## Validation and Measurable Results

`SafeAreaLayoutTest` passed 7 tests with no failures. No build was run, matching
the milestone gate.

## Limitations and Remaining Work

Milestones 2 through 5 remain.

## Claims Requiring Human Review

None at this stage.
