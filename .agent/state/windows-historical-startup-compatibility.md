<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Completed state: Windows historical I386 compatibility

- Milestone: run historical A/B and corrected production Win32 startup on one
  Windows runner, then run the existing ownership smoke only if corrected
  startup passes.
- Branch/worktree: `codex/windows-historical-startup-compatibility` at
  `/Users/flsobral/.codex/worktrees/windows-historical-startup-compatibility`.
- Final diagnostic commit: `cf3487e76147be4ee272cf435f1789c567e10e13`
  (`ci(windows): pin corrected I386 ownership smoke helper`), pushed to
  `codex/windows-historical-startup-compatibility`.
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
- Local validation passed: 12 TotalCross diagnostic contract tests, 17
  performance-lab ownership contract tests, changed-file copyright
  validation, workflow YAML parsing, JSONL evidence parsing, `git diff --check`,
  and commit-message validation.
- Initial Actions run `45901886592` built no corrected runtime because the
  PowerShell helper called `.Trim()` on the empty output from a clean Git
  status. Historical A and B each launched and exited 0 with `tcdiag-result`
  `PASS`, but the summary failed because the harness omitted `import re`; the
  static initializer stage also reached `record` as null. Details and artifact
  references are in the evidence index. This attempt does not establish the
  corrected Win32 ownership result.
- Final Actions run `45907759163`
  (https://github.com/TotalCross/totalcross/actions/runs/45907759163) passed
  the production Win32 build, historical A/B and corrected I386 startups, the
  ownership smoke, and final gate. All processes were in Windows session 2,
  exited 0, and produced every required durable marker. No crash dump was
  needed.
- Corrected build source: `94af2b8b653cad509994b5ccf3dbb60ac28ef920`,
  tree `539f5b424b6f4593c7d96074d69ff077a38ed361`; Visual Studio 17 2022,
  Win32, Release; Skia and SDL enabled, legacy renderer/native windowing/GLES
  disabled, software surface enabled. Launcher SHA-256
  `9ffe796f6cfa5ceaf33beaccde5f158cb9e47304256642913894849485fc8e31`;
  tcvm SHA-256
  `000d9d2ae734659383db9266b7e5c2db0e056de78ebbee68383142f7d797af52`.
- Ownership status: PASS on the same hashes. Two screens of 1,510 controls,
  zero mismatches, all seven stages, red pixel `16711680`, owner count zero
  after detach, cache/reparent/shared-owner checks all passed, and no
  underflow diagnostic was present.
- Final artifacts: runtime `15158467793` and results `15158866549`; run,
  digest, source, and artifact details are in
  `.agent/evidence/windows-historical-startup-compatibility.jsonl`. Report
  handoff is in `.agent/reports/windows-historical-startup-compatibility.md`.
- Dedicated TotalCross and performance-lab diagnostic branches were pushed.
  No production branch, PR, merge, release, or runtime/ownership implementation
  change was made. Root checkout remains untouched.
- Objective complete. No further work is pending; AMD64-only follow-up remains
  separate and outside the priority correction.
