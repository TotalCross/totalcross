<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Checkpoint

## Current State

- Milestone: closure authorized; focused test integration prepared, hosted run and final synchronization pending.
- Execution interval: closure authorization received at 2026-11-27 21:08 UTC; maximum 40 minutes.
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
- Closure classification: ADLER32 and original I06 drawing are
  `NOT_REPRODUCED`, root causes undetermined; I06 Linux ownership validation is
  `PASS` with the original PNG and no startup shim.
- Closure regression probe is being integrated into the existing Linux startup
  workflow. Its three cases cover RGBA file, RGBA bytes, and original PNG
  drawing/owner cleanup. The hosted result is pending.
- Both issue texts are local proposals only; no GitHub issue has been changed.
- No indexed-PNG diagnosis or correction, production image change, dependency
  change, symbol isolation, allocator change, or historical bisect is in scope.
- No non-Linux build is authorized unless a shared regression-test change makes
  it strictly necessary.
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
- Moved the unchanged PNG fixtures into SDK native-startup test resources,
  added a focused deployed Linux probe, and wired it into the existing runtime
  job rather than adding another workflow.
- Prepared a self-contained issue #564 update and a separate indexed-PNG issue
  proposal; both remain unpublished.

## Next Action

Run focused static checks, commit and push the closure integration, inspect the
hosted Linux-only result, complete the evidence/report/matrix, commit and push
the closure record, then verify that the remote branch HEAD matches local HEAD.

## Active Decisions

- No production code changes are authorized in this closure.
- Baseline runtime must run without a preload shim.
- Keep ADLER32, drawing, and indexed-inspection outcomes separate.
- Preserve exact source and artifact identities for both the clean-master
  baseline and the branch regression run.
- Do not publish proposals or create a PR.

## Deferred Validation

- No local native runtime was executed; native desktop execution is prohibited
  locally.
- Failure localization, indexed-PNG diagnosis, and causal experiments are out
  of scope; both historical root causes remain undetermined.
- Windows, macOS, Android, sanitizer, and benchmark validation are skipped.
- The hosted closure run is limited to Linux x86_64 and SDK build/runtime
  integration; no local native runtime is planned.

## Resume Command

```sh
cd /Users/flsobral/.codex/worktrees/fix-linux-png-decoding
```
