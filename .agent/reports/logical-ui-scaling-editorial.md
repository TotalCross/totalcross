<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Logical UI scaling editorial report

## Editorial Summary

The branch contains useful implementation scaffolding but is under corrective
review. No final outcome is claimed.

## Original Plan versus Actual Outcome

The original plan expected complete layout behavior, three-renderer equivalence,
native macOS and Android proof, native synchronization, and a complete
text-bearing DANFE.

The reviewed branch currently proves API scaffolding, selected Java image
behavior, PNG dimensions, barcode runs, native compilation, and Java Launcher
startup. It does not yet prove the complete behavioral acceptance.

## What Changed

Update after corrective milestones. Keep JavaSE/AWT and native macOS changes in
separate subsections.

## Decisions and Trade-offs

- Existing branch history is preserved.
- `USE_WRITE_PIXELS` must be restored with scale-aware eligibility.
- Native implementation validation uses macOS until final platform validation.
- Java Launcher execution is not native evidence.
- Screenshots use CoreGraphics window IDs and `screencapture -l`.

## Unexpected Problems and Discoveries

- Prior progress reporting confused compilation and Java host execution with
  native runtime validation.
- The embedded direct-write branch was removed unnecessarily.
- Layout-unit metadata was not connected to layout behavior.

## Validation and Measurable Results

Do not promote existing Java-only or compile-only results into native proof.
Populate this section as corrected milestones pass.

## Useful Evidence and Examples

Use repository-relative paths under:

    artifacts/logical-ui-scaling/

## Limitations, Remaining Work, and Open Questions

See `.agent/reviews/logical-ui-scaling-branch-review.md` and the current state.

## Possible Article Angles

Defer until implementation is complete.

## Suggested Narrative

Explain the original mixed-unit bug, the difference between Java and native
rendering, the surface-owned scale model, preservation of embedded
specializations, and the final cross-platform proof.

## Claims Requiring Human Review

- visual equivalence;
- public compatibility of Image dimension semantics;
- non-Skia macOS support status;
- embedded fast-path performance;
- optional iOS support conclusion.
