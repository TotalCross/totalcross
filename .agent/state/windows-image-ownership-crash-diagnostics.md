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
  this checkpoint: 04041664bc798bcca51cc89c78f8972906f9ff38.
- Exact source revisions: baseline 6b915883dd202f6db891990f6c3af26fb645ecc7;
  fixed 94af2b8b653cad509994b5ccf3dbb60ac28ef920.
- Instrumented fixture: TotalCross/totalcross-performance-lab commit
  804803a243db61a2ed54756e1f63cfc42ff4412b, on the dedicated diagnostic
  branch and pinned by commit in the workflow.
- Prior runs: 45853745093, 45856087270, 45857806319, 45858785421. Logs and all
  artifacts are under /Users/flsobral/.codex/windows-crash-diagnostics-prior-runs/.
- Latest diagnostic run: 45879820691 on branch commit
  1728e6fae10d7e52779bdb89c23a2d3323fa4c4a. Exact baseline/fixed builds and
  deploys passed, and both sequential probes used the same win22 image
  20261123.376.1. Both exited 0xC0000005 before Java markers. Live CDB caught
  second-chance invalid pointer writes in tcvm.dll at the same RVA 0xB93A52
  with the same relative stack, but the dump command's backslash path was
  parsed as control escapes and the local PDB directory was relative to CDB's
  temporary working directory. Thus no dump or source-level symbol was
  produced. Artifacts and logs are under
  /Users/flsobral/.codex/windows-crash-diagnostics-new-runs/45879820691/.
- Current local slice converts CDB symbol and dump paths to absolute
  forward-slash paths and adds a contract assertion. Next: run focused local
  checks, commit and push the diagnostic-only correction, then analyze the
  CDB dump and matching symbols from the rerun. Progressive image work remains
  gated on both minimal probes passing.
- Validation completed: downloaded full logs/artifacts for all four prior
  runs; the perf-lab instrumentation contract suite passes 14 tests. The first
  prior run failed during deploy; later runs lacked app markers, and the final
  process exit was 0xC0000005.
- Focused validation passed eight diagnostic Python tests and 14 fixture
  contract tests. Ruby parsed both workflow YAML files; focused copyright
  validation passed; both worktrees pass `git diff --check`. The approved
  depot-tools dependency fetch completed successfully;
  no tracked dependency files changed.
- Deferred validation: Windows execution/debugging awaits the diagnostic
  workflow rerun after correcting CDB paths. No Windows binary was built or
  run locally.
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
