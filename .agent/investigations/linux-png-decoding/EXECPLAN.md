<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Investigate Linux x86_64 PNG decoding

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`. The user's task file
defines the full investigation requirements and six milestone acceptance gates.

## Purpose / Big Picture

Classify the three reported PNG failures independently on current master,
identify causes only from reproducible evidence, and correct only a demonstrated
Linux x86_64 decoding defect. Keep the indexed-PNG inspection defect separate
unless a shared cause is established and approved.

## Working Set and Resume Protocol

- Read `CHECKPOINT.md` first after a pause. It records the active milestone,
  exact branch and source revision, last commit, current CI run, and next action.
- `FINDINGS.md` records confirmed observations and rejected hypotheses.
- `DECISIONS.md` records scope and review-gate decisions.
- `TEST-MATRIX.md` tracks each manifestation and platform result.
- `evidence/index.md` identifies retained fixtures, source revisions, artifacts,
  and run records. Do not reread CI logs unless the index points to a needed
  detail.
- `fixtures/` contains the original and control inputs with hashes and decoded
  reference properties. Verify with `cd fixtures && sha256sum -c SHA256SUMS`.
- `diagnostic-patches/` is reserved for temporary experiments. Keep temporary
  instrumentation out of production paths.
- Update `FINAL-REPORT.md` at milestone 6 only.

## Progress

- [x] Verified current `origin/master` is
  `6beef491f28c63aa364d3661dd5c37ccff01fb0d`, and PR #568 is an ancestor.
- [x] Created isolated worktree `/Users/flsobral/.codex/worktrees/fix-linux-png-decoding`
  on `fix/linux-png-decoding`; the shared dirty checkout remains untouched.
- [x] Milestone 1: run the current-master Linux baseline without a startup shim,
  then commit and push its classifications and compact evidence. The baseline
  completed; both reported decode/drawing failures were not reproduced, while
  indexed PNG construction remains rejected. No cause or production change is
  justified by this result.
- [ ] Milestones 2–6 remain gated on explicit authorization after each pause.

## Current Architecture and Scope

The PNG constructor and encoded-image inspection paths are in
`TotalCrossVM/src/nm/ui/image_Image.c` and
`TotalCrossVM/src/nm/ui/image/ImageEncodedBag.c`. PNG materialization and drawing
use the native decoder and graphics pipeline. Milestone 2 will trace those
boundaries only after Milestone 1 establishes current behavior.

In scope: Linux x86_64 PNG decode/materialization and the exact failed image
drawing sequence. Out of scope: startup behavior from PR #568, indexed image
inspection changes, ownership implementation, Image(String) fallback changes,
other image refactors, benchmarks, and non-Linux production fixes.

## Plan of Work

1. **Current-master reproduction.** Pin master; validate original fixture
   identities; verify ordinary startup; run I02 and the original I06 PNG drawing
   probe on hosted Linux without `LD_PRELOAD`; classify each result.
2. **Failure localization.** Trace each reproduced failure to a concrete image
   processing stage with minimal diagnostic instrumentation.
3. **Causal investigation.** Compare exact dependency and binary provenance,
   runtime symbol bindings, and an isolated decoder control. Stop at the smallest
   causal experiment. Pause for review before any production edit.
4. **Production correction.** Only after explicit approval, make the smallest
   evidence-backed Linux PNG correction and add permanent tests for the original
   fixtures.
5. **Functional regression validation.** Run the focused Linux matrix, then the
   authorized Windows I386, macOS ARM64, and local Android emulator controls.
6. **Final consolidation.** Reconcile the branch, findings, reports, evidence,
   and remote history. Do not open a pull request unless requested.

## Surprises & Discoveries

- The I02 report records a shimmed pass for the historical RGBA fixture on a
  frozen master runtime, but does not explain the earlier checksum failure.
- The I06 application embeds its original 2×2 red PNG in source; it is not the
  later JPEG replacement fixture.
- The indexed PNG is rejected by image inspection before pixel decoding in the
  captured run. Treat it as independent unless evidence changes that boundary.

## Decision Log

- No production correction is justified before a current unmodified-master
  reproduction and reviewed causal findings.
- Do not use a startup shim for baseline or corrected functional runs.
- Preserve the indexed-PNG case as a control; do not expand implementation to
  its inspector rejection without a separate approval.

## Validation and Acceptance

Milestone 1 uses hosted GitHub Actions on Linux x86_64 for native runtime work.
Require normal startup markers, successful process completion for captured
probes, materialized pixel counts and values, and the exact original fixtures.
The probe records expected decoder failures without treating a captured defect
as a workflow-harness failure. Do not run native desktop runtime tests locally.

Later milestones use the smallest validation that proves the current change,
following the validation levels in `AGENTS.md`. Cross-platform runtime tests and
sanitizers are deferred until the milestone that requires them.

## Risks and Open Questions

- The original RGBA failure may remain non-reproducible on current master.
- The rendering failure may share or differ from the ADLER32 failure.
- Prior indexed-PNG source and runtime behavior disagree; current rejection
  boundary must be re-established without converting that into scope expansion.
- Native dependency and symbol-binding evidence must match the exact tested
  Linux artifact.

## Idempotence and Recovery

All branch work occurs in the isolated worktree. Never edit or clean the shared
checkout, its untracked artifacts, or other worktrees. Fetch and compare remote
state before pushes; push only `fix/linux-png-decoding`. Do not rewrite or
force-push history. If CI is still running at a pause, record its run ID,
revision, artifacts, and next retrieval command in `CHECKPOINT.md`.

## Outcomes & Retrospective

No milestone is complete yet. Record each outcome once at its acceptance
checkpoint and point to the evidence index.

## Revision Note

Created for the six-milestone investigation; it summarizes the user's detailed
plan while preserving the required pause and review gates.
