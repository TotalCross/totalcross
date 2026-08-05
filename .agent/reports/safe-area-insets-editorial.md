<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Safe-area insets editorial report

## Editorial Summary

Execution is in progress on `feat/logical-ui-scaling`. Milestone 0 established
the clean scoped baseline; implementation begins with the core SDK model.

## Scope

This work covers window safe-area policy, per-control safe/full-bleed layout,
container safe padding, scroll content insets, menu integration, and dynamic
Android/iOS delivery. Display-cutout visuals, panel clipping and shapes, IME
insets, packaging, and unrelated layout changes remain excluded.

## Original Plan versus Current Outcome

The baseline matches the plan. No feature implementation has completed yet.

## Decisions and Trade-offs

Declared user insets will remain distinct from effective safe-area insets, and
platform changes will use a dedicated internal transition rather than surface
resize events.

## Validation and Measurable Results

Milestone 0 used scoped status and source inventories only. No build was run.

## Limitations and Remaining Work

Milestones 1 through 5 remain.

## Claims Requiring Human Review

None at this stage.
