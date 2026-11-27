<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Checkpoint

## Current State

- Milestone: 1 — reproduce on current master.
- Execution interval: first authorization; hosted baseline is running.
- Branch: `fix/linux-png-decoding`.
- Worktree: `/Users/flsobral/.codex/worktrees/fix-linux-png-decoding`.
- Selected master revision: `6beef491f28c63aa364d3661dd5c37ccff01fb0d`.
- PR #568 correction: verified as an ancestor of selected master.
- Last workflow implementation commit: `e12bda6d6` (three focused setup commits
  follow selected master).
- Baseline run head: `94917ff6d51f7dd442aacbec838717e5aef48fa4`.
- Remote branch: published as `origin/fix/linux-png-decoding`.
- Shared checkout: remains on older `master` with unrelated untracked work; no
  files were changed there.
- Native runtime tests: none run locally.
- Hosted baseline run: `46061400453`, in progress; source-selection and helper
  contract jobs have passed. The canonical native build is starting.

## Completed

- Read repository `AGENTS.md`, `.agent/PLANS.md`, and the logical-commits skill.
- Inspected registered worktrees and uncommitted files; chose a new isolated
  worktree and left existing checkouts untouched.
- Read issue #564, reports I02/I06, run metadata, and retained CI artifact
  identities. Exact fixture records are in `evidence/index.md`.
- Recovered the original 74-byte I06 PNG from the RED_PNG array at performance
  lab commit `d084949d106f9606a7639d851ad66b97734df1ec`.
- Independently validated all retained PNG fixtures with Pillow 13.0.0,
  including dimensions, samples, alpha, indexed palette transparency, inflate
  lengths, and chunk CRCs.

## Next Action

Wait for run `46061400453`, capture the selected source from build provenance,
then classify the PNG and ownership probes separately. The source selector
passed; the branch changes only investigation records, fixtures, and workflow
code, so the canonical build is expected to use
`6beef491f28c63aa364d3661dd5c37ccff01fb0d`.

## Active Decisions

- No production code changes before Milestone 3 causal review and explicit
  approval.
- Baseline runtime must run without a preload shim.
- Keep ADLER32, drawing, and indexed-inspection outcomes separate.
- Preserve the canonical source revision in build provenance when the reusable
  workflow checks out the selected baseline commit.

## Deferred Validation

- Hosted Linux baseline is pending; native desktop execution is prohibited
  locally.
- Windows, macOS PNG controls, and Android emulator controls belong to later
  authorized milestones.
- No sanitizers or benchmarks are needed for this baseline slice.

## Resume Command

```sh
cd /Users/flsobral/.codex/worktrees/fix-linux-png-decoding
```
