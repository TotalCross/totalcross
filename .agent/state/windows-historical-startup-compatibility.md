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
- Base: local diagnostic commit `32593ba3a4ee80f3295bc9ca27bd44ca8e5fab68`
  (`test(windows): validate historical Win32 startup ownership`), based on
  `2717c47e772862f44201df1c0656c31ae5d1d2b0`; pushed to the dedicated
  TotalCross remote branch.
- User root checkout at
  `/Users/flsobral/repos/totalcross-github` remains untouched and dirty state is
  preserved. Existing AMD64 diagnostic worktree/report remains untouched.
- Existing performance-lab worktree for this diagnostic change:
  `/Users/flsobral/.codex/worktrees/totalcross-performance-lab-windows-i386-ownership-smoke`.
  Its dedicated branch is pushed through commits `ea1c64eb781e0dd96a3b22f6055f0dda342f04ab`
  and `c4ba2f568df374f19a8bbc2a74dcfb598f2ac48e`; follow-up
  `bf691c14d7c5fe807c62de98c7285f65d38de87e` fixes Windows marker/result
  discovery for the appPath-without-separator layout. It adds I386 runtime
  checks, same-runtime hash checks, and I386 crash-runner selection to the
  existing smoke tool. The Java ownership fixture is unchanged.
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
- Focused local validation: initial TotalCross diagnostic contracts (11
  passed), performance-lab ownership contracts (15 passed), header validator
  (8 files, 0 changes), Ruby YAML parse, and staged `git diff --check` passed.
  Follow-up diagnostic contracts pass (12 tests); marker-fix ownership
  contracts pass (17 tests); changed-file header check and working-tree
  `git diff --check` pass. No Windows work ran locally.
- Initial Actions run `45901886592` built no corrected runtime because the
  PowerShell helper called `.Trim()` on the empty output from a clean Git
  status. Historical A and B each launched and exited 0 with `tcdiag-result`
  `PASS`, but the summary failed because the harness omitted `import re`; the
  static initializer stage also reached `record` as null. Details and artifact
  references are in the evidence index. This attempt does not establish the
  corrected Win32 ownership result.
- Current follow-up changes: handle empty PowerShell status output, import the
  marker regex module, and emit the class-initialization marker from the
  constructor after class initialization completes. The static contract suite
  passes. These changes are in pushed commit `44b15b469d60ecfde714b08d58bcc332151842b7`.
- Actions run `45902867357`: historical A, historical B, and corrected Win32
  startup all PASS with exit 0, all durable markers, and process session 2.
  The corrected production build is PE I386 and uses the required Skia/SDL
  Release configuration. The ownership fixture also emitted a durable PASS
  record after all seven stages, but the helper marked the run FAIL because it
  searched for stage files only inside the app directory. The files were
  persisted in its sibling path. It verified that Launcher/tcvm hashes matched
  the corrected startup runtime. No native crash or dump was produced.
- Marker lookup is fixed and covered by 17 passing performance-lab contracts in
  pushed commit `bf691c14d7c5fe807c62de98c7285f65d38de87e`. The TotalCross
  workflow is being pinned to this commit for the final rerun.
- Next action: commit/push the updated pin, run the final isolated Windows job,
  then complete both reports and the evidence index from its artifact.
- Resume protocol: read this state first, then inspect the active workflow,
  build/run scripts, minimal probe, and evidence index. Do not repeat artifact
  downloads or reopen AMD64 analysis unless the corrected I386 run requires it.
