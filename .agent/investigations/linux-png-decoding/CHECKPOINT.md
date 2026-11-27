<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Checkpoint

## Current State

- Milestone: 1 — reproduce on current master.
- Execution interval: first authorization; baseline capture is being prepared.
- Branch: `fix/linux-png-decoding`.
- Worktree: `/Users/flsobral/.codex/worktrees/fix-linux-png-decoding`.
- Selected master revision: `6beef491f28c63aa364d3661dd5c37ccff01fb0d`.
- PR #568 correction: verified as an ancestor of selected master.
- Current branch HEAD: selected master; initial investigation commit not yet made.
- Remote branch: not yet published; publish the first complete milestone commit.
- Shared checkout: remains on older `master` with unrelated untracked work; no
  files were changed there.
- Native runtime tests: none run locally.
- Hosted baseline workflow: not yet started.

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

Finish and inspect the branch-scoped hosted baseline workflow, validate the
workflow and fixture manifest locally, create focused commits, push the branch,
and capture the resulting run ID. Wait for that run before classifying the
current-master behavior.

## Active Decisions

- No production code changes before Milestone 3 causal review and explicit
  approval.
- Baseline runtime must run without a preload shim.
- Keep ADLER32, drawing, and indexed-inspection outcomes separate.

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
