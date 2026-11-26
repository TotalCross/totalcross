<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Active state: Windows image ownership crash diagnostics

- Active milestone: validate and execute the isolated baseline-versus-fixed
  Windows diagnostics workflow.
- Branch/worktree: codex/image-ownership-windows-crash-diagnostics at
  /Users/flsobral/.codex/worktrees/image-ownership-windows-crash-diagnostics.
- Base: f417e2513ffdf024d83885498cc68e9ed9fb60d5, the existing dedicated
  Windows validation branch. Current remote diagnostic branch head before
  this checkpoint: e2fb0f1375364e56015e1ed50bc41f2ec104208c.
- Exact source revisions: baseline 6b915883dd202f6db891990f6c3af26fb645ecc7;
  fixed 94af2b8b653cad509994b5ccf3dbb60ac28ef920.
- Instrumented fixture: TotalCross/totalcross-performance-lab commit
  804803a243db61a2ed54756e1f63cfc42ff4412b, on the dedicated diagnostic
  branch and pinned by commit in the workflow.
- Prior runs: 45853745093, 45856087270, 45857806319, 45858785421. Logs and all
  artifacts are under /Users/flsobral/.codex/windows-crash-diagnostics-prior-runs/.
- Latest diagnostic run: 45871774434. Both exact source builds and deploys
  passed; both minimal probes exited 0xC0000005 before app markers. Their
  module snapshots do not contain tcvm.dll. The runner had WinSta0\Default,
  an interactive session, Explorer/DWM, and a 1024x768 Hyper-V display. The
  ProcDump wait monitor missed both short-lived processes; no dump or matching
  Launcher/tcvm PDB was retained. Full artifacts are under
  /Users/flsobral/.codex/windows-crash-diagnostics-new-runs/45871774434/.
- Next action: finish focused checks, commit/push the revised diagnostic
  workflow and capture tools, then inspect the next Actions dump and CDB stack.
- Validation completed: downloaded full logs/artifacts for all four prior
  runs; the perf-lab instrumentation contract suite passes 14 tests. The first
  prior run failed during deploy; later runs lacked app markers, and the final
  process exit was 0xC0000005.
- Focused validation now passes seven diagnostic Python tests and 14 fixture
  contract tests. Ruby parsed both workflow YAML files; focused copyright
  validation passed six applicable files; both worktrees pass `git diff --check`.
  The approved depot-tools dependency fetch completed successfully;
  no tracked dependency files changed.
- Deferred validation: Windows execution/debugging awaits the diagnostic
  workflow. No Windows binary was built or run locally.
- Decisions: diagnostic-only source/workflow changes; the caller triggers on
  pushes to this dedicated branch and supports manual reruns. Use two calls to the
  canonical reusable build in one Actions run, with baseline/fixed artifact
  suffixes; gate the functional matrix on both minimal probes; do not modify
  production runtime code. The next iteration uses ProcDump's AeDebug
  postmortem capture with WER LocalDumps fallback, installs CDB from the
  Windows SDK when absent, emits Release linker PDBs, and preserves only PDBs
  matching Launcher.exe/tcvm.dll. The functional runner accepts absent dump
  tools and still records process status.
- Preserve: all existing implementation, benchmark, smoke, and local
  worktree evidence. Do not edit the original root worktree.
- Resume: read this file, then the matching plan's active task. Prior run
  evidence root: /Users/flsobral/.codex/windows-crash-diagnostics-prior-runs.
