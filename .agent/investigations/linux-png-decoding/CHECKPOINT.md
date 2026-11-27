<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Checkpoint

## Current State

- Milestone: 1 complete — pause for review before Milestone 2.
- Execution interval: first authorization; Milestone 1 complete, awaiting review.
- Branch: `fix/linux-png-decoding`.
- Worktree: `/Users/flsobral/.codex/worktrees/fix-linux-png-decoding`.
- Selected master revision: `6beef491f28c63aa364d3661dd5c37ccff01fb0d`.
- PR #568 correction: verified as an ancestor of selected master.
- Preparation commits: `e4e6ac449`, `291fba75e`, `e12bda6d6`, `94917ff6d`,
  and `2472e596f`; they follow selected master.
- Results commit: `f5582ba36`, pushed to the remote branch.
- Baseline run head: `94917ff6d51f7dd442aacbec838717e5aef48fa4`.
- Remote branch: published as `origin/fix/linux-png-decoding`.
- Current-master results: run `46061400453` completed successfully; compact
  classifications and artifact identities are in `evidence/index.md`.
- Shared checkout: remains on older `master` with unrelated untracked work; no
  files were changed there.
- Native runtime tests: none run locally.
- Hosted source revision: `6beef491f28c63aa364d3661dd5c37ccff01fb0d`; SDK and
  Linux runtime provenance match its clean source tree.

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
- Ran the hosted Linux startup matrix, I02 PNG matrix, and original I06 drawing
  sequence without a startup shim. Startup passed; the historical RGBA and I06
  failures did not reproduce; indexed PNG construction remains rejected.
- Committed and pushed the Milestone 1 evidence to
  `origin/fix/linux-png-decoding`.

## Next Action

Wait for explicit authorization before starting Milestone 2 failure
localization. Do not infer a shared cause from the two non-reproduced failures
or from the separate indexed-PNG rejection.

## Active Decisions

- No production code changes before Milestone 3 causal review and explicit
  approval.
- Baseline runtime must run without a preload shim.
- Keep ADLER32, drawing, and indexed-inspection outcomes separate.
- Preserve the canonical source revision in build provenance when the reusable
  workflow checks out the selected baseline commit.
- Do not begin Milestone 2 until the user explicitly authorizes it.

## Deferred Validation

- No local native runtime was executed; native desktop execution is prohibited
  locally.
- Failure localization and causal experiments are deferred to authorized
  Milestone 2 and later.
- Windows, macOS PNG controls, and Android emulator controls belong to later
  authorized milestones. The canonical reusable build also ran its existing
  macOS ARM64 smoke job as a side-effect; it passed and does not expand scope.
- No sanitizers or benchmarks are needed for this baseline slice.

## Resume Command

```sh
cd /Users/flsobral/.codex/worktrees/fix-linux-png-decoding
```
