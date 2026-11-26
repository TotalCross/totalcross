<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Active state: Windows historical I386 compatibility

- Milestone: run historical A/B and corrected production Win32 startup on one
  Windows runner, then run the existing ownership smoke only if corrected
  startup passes.
- Branch/worktree: `codex/windows-historical-startup-compatibility` at
  `/Users/flsobral/.codex/worktrees/windows-historical-startup-compatibility`.
- Base: local completed diagnostic commit `2717c47e772862f44201df1c0656c31ae5d1d2b0`;
  dedicated TotalCross remote branch has not yet been pushed.
- User root checkout at
  `/Users/flsobral/repos/totalcross-github` remains untouched and dirty state is
  preserved. Existing AMD64 diagnostic worktree/report remains untouched.
- Existing performance-lab worktree for this diagnostic change:
  `/Users/flsobral/.codex/worktrees/totalcross-performance-lab-windows-i386-ownership-smoke`.
  Its dedicated branch is pushed through commits `ea1c64eb781e0dd96a3b22f6055f0dda342f04ab`
  and `c4ba2f568df374f19a8bbc2a74dcfb598f2ac48e`; it adds I386 runtime checks,
  same-runtime hash checks, and I386 crash-runner selection to the existing
  smoke tool. The Java ownership fixture is unchanged.
- Exact historical A/B SDK/runtime artifacts and fixed SDK are downloaded in
  the external artifact cache. IDs, digests, local hashes, source trees, and PE
  findings remain indexed in `.agent/evidence/windows-historical-startup-compatibility.jsonl`.
- Fixed Win32 build inputs from run `45884551862`: SDK artifact
  `15148184230`; prepared dependencies `15148893237` with digest
  `sha256:d2b007713e7bc374e4b190585efb7156893bf2ab15ae08b121ea865f2f216d1c`;
  native bridges `15147749510` with digest
  `sha256:a977aac5cbf59c355e5c9f178453952113eaf8588579c31a31d9a95e7d7643c8`.
- Current implementation edits: marker path/error behavior in
  `WindowsNativeStartupProbe.java`; I386-aware preparation, PE checks, TCZ and
  process evidence in `windows-native-crash-diagnostics.py`; generic x86 dump
  analysis in `windows-native-crash-runner.ps1`; new Win32 build/run scripts and
  isolated workflow; the plan/state/evidence and both report updates are now
  present in this worktree.
- Focused local validation already run: TotalCross diagnostic Python contracts
  (11 passed), performance-lab ownership contracts (15 passed), header
  validator (4 files, 0 changes), `git diff --check`, Ruby YAML parse (passed).
  No Windows build, deploy, binary execution, or dump analysis has run locally.
- No Actions run for this I386 objective has started. No TotalCross branch push
  has occurred. The only remote writes so far are two diagnostic commits on the
  dedicated performance-lab branch above.
- Next action: stage the focused files and check the staged diff, validate the
  commit title, create one focused TotalCross diagnostic commit, push the
  dedicated branch to trigger its single Windows Actions run, then inspect the
  uploaded result artifact and complete both reports/evidence.
- Resume protocol: read this state first, then inspect the active workflow,
  build/run scripts, minimal probe, and evidence index. Do not repeat artifact
  downloads or reopen AMD64 analysis unless the corrected I386 run requires it.
