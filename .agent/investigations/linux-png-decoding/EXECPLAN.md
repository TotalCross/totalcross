<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Close Linux PNG failure reports

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`. The current user
authorization narrows the remaining work to regression coverage and report
closure after the successful Milestone 1 baseline.

## Purpose / Big Picture

Close the Linux x86_64 issue review with independent classifications for the
historical RGBA decode failure, the original PNG drawing failure, and indexed
PNG construction rejection. Add reusable Linux regression coverage for the two
non-reproduced failures. Do not infer or pursue causes for non-reproduced
failures, and keep the indexed-PNG proposal separate from this branch's code.

## Working Set and Resume Protocol

- Read `CHECKPOINT.md` first after a pause. It records the active milestone,
  exact branch and source revision, last commit, current CI run, and next action.
- `FINDINGS.md` records confirmed observations and rejected hypotheses.
- `DECISIONS.md` records scope and review-gate decisions.
- `TEST-MATRIX.md` tracks each manifestation and platform result.
- `evidence/index.md` identifies retained fixtures, source revisions, artifacts,
  and run records. Do not reread CI logs unless the index points to a needed
  detail.
- `TotalCrossSDK/src/test/resources/native-startup/png-regression/fixtures/`
  contains the original and control inputs with hashes and decoded reference
  properties. Verify from the repository root with
  `cd TotalCrossSDK/src/test/resources/native-startup/png-regression/fixtures && sha256sum -c SHA256SUMS`.
- `PROPOSED-ISSUE-564-UPDATE.md` and `PROPOSED-INDEXED-PNG-ISSUE.md` are
  self-contained drafts; do not publish them or edit GitHub issues.
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
- [x] Closure milestone authorized: classify both historical failures as
  `NOT_REPRODUCED`, preserve the original fixtures, add a focused Linux runtime
  regression probe, and prepare issue proposals without publishing them.
- [x] Hosted Linux-only regression run `46071207197`: the repository-owned
  probe passed all three cases without a shim; non-Linux jobs were skipped.
- [ ] Commit and push the completed evidence/report/checkpoint, then verify the
  remote branch head and pause.

## Current Architecture and Scope

The historical runtime path used `Image(Stream)`/`Image(byte[])` capture,
materialization, and the native drawing pipeline. The current baseline no longer
reproduces the reported RGBA decode or drawing failures, so those paths are not
being traced for speculative causes.

In scope: repository-owned Linux x86_64 regression assertions for the 36×36
RGBA file/byte-array paths and the original 2×2 PNG drawing/ownership sequence;
the existing hosted Linux startup workflow; issue-update drafts and evidence.
Out of scope: decoder or dependency changes, symbol isolation, allocator
changes, historical bisecting, indexed-PNG diagnosis or correction, other
platform builds, benchmarks, and GitHub issue edits.

## Plan of Work

1. **Baseline classification (complete).** Run the exact current-master source
   without a startup shim; classify the ADLER32 and drawing failures separately
   from indexed PNG construction.
2. **Permanent regression integration.** Keep fixture bytes in test resources,
   add a focused deployed Linux probe, and call it from the existing Linux
   startup workflow. Use a Linux-only canonical build for the branch push.
3. **Issue proposals.** Prepare a self-contained update for issue #564 and a
   separate indexed-PNG issue draft. Do not publish either draft.
4. **Closure.** Record the hosted result and test integration, run focused
   static/deploy validation plus the hosted Linux job, commit and push, verify
   the remote branch head, and pause.

The original Milestones 2–6 are superseded for this closure. The two failed
symptoms are not reproducible, and the user explicitly ruled out decoder,
dependency, symbol, allocator, and historical-bisect experiments. No production
correction or indexed-PNG investigation is authorized by this closure.

## Surprises & Discoveries

- The I02 report records a shimmed pass for the historical RGBA fixture on a
  frozen master runtime, but does not explain the earlier checksum failure.
- The I06 application embeds its original 2×2 red PNG in source; it is not the
  later JPEG replacement fixture.
- The indexed PNG is rejected by image inspection before pixel decoding in the
  captured run. Treat it as independent unless evidence changes that boundary.
- The unshimmed current-master run completes both previously failing PNG
  scenarios. Their causes remain undetermined; the indexed rejection persists.

## Decision Log

- The two historical Linux failures are `NOT_REPRODUCED`; their root causes are
  undetermined.
- Do not use a startup shim for baseline or corrected functional runs.
- Record I06 ownership validation as `PASS` with the original PNG and no shim.
- Keep the indexed-PNG rejection as a separate issue proposal. Do not investigate
  or correct it in this branch.
- Do not modify image production code or dependencies, isolate symbols, alter
  allocation, or bisect historical revisions.

## Validation and Acceptance

Hosted GitHub Actions on Linux x86_64 is the only native runtime validation for
this closure. Require clean startup, all focused case markers, exact fixture
hashes, expected dimensions/pixel values, successful drawing, and a successful
process exit. Do not run native desktop runtime tests locally or rebuild other
platforms. Static checks and Java compile/deploy checks are appropriate locally.

## Risks and Open Questions

- The causes of the non-reproduced ADLER32 and drawing failures remain unknown.
- The indexed constructor rejection is reproducible but remains outside this
  branch's investigation and implementation scope.
- The closure regression job must identify the branch probe revision and the
  canonical Linux artifact revision separately.

## Idempotence and Recovery

All branch work occurs in the isolated worktree. Never edit or clean the shared
checkout, its untracked artifacts, or other worktrees. Fetch and compare remote
state before pushes; push only `fix/linux-png-decoding`. Do not rewrite or
force-push history. If CI is still running at a pause, record its run ID,
revision, artifacts, and next retrieval command in `CHECKPOINT.md`.

## Outcomes & Retrospective

The closure is complete. The historical RGBA and original drawing failures did
not reproduce on the clean master baseline; their root causes remain
undetermined. I06 ownership validation passed with the original PNG and no
shim. Indexed PNG construction remains separately rejected and was not
investigated. The repository-owned probe passed all three focused cases without
changing production code. Run and artifact identities are recorded in
`CHECKPOINT.md`, `TEST-MATRIX.md`, and `evidence/index.md`.

## Revision Note

Revised after Milestone 1 to reflect the user's closure scope and explicit
prohibition on speculative causes, indexed-PNG work, and unnecessary platform
builds. Earlier plan history remains available in Git.
