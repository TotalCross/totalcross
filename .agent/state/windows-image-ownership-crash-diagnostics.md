<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Active state: Windows image ownership crash diagnostics

- Active milestone: complete; the baseline-versus-fixed Windows crash
  investigation and handoff report are recorded.
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
- Latest diagnostic run: 45884551862 on branch commit
  9ca20fa3c3de9bc97c5ec7f0b130db4ff1b7e6d0. Both exact builds/deploys passed
  on the same win22 image 20261116.370.1; both probes exited 0xC0000005 before
  Java markers and CDB captured 57 MB dumps with the matching private PDBs.
  The same CRT write instruction at tcvm RVA 0xB93A52 resolves through
  `privateHeapSetJump` (`mem.c:353`, with the `xstrcpy` at line 350),
  `tczNewInstance` (`tcz.c:177`), `tczFindName`, `tczLoad`'s ConstantPool path,
  and `startVM` (`startup.c:653`). The baseline/master and fixed paths match.
  The error is classified A: a preexisting native TCVM startup defect; the
  invalid heap destination's upstream corruption remains unknown. Artifacts
  and logs are under
  /Users/flsobral/.codex/windows-crash-diagnostics-new-runs/45884551862/.
- Handoff report: `.agent/reports/windows-image-ownership-crash-diagnostics.md`
  is committed locally at `231eb8deb`. Copyright validation, staged diff
  checks, and the report commit-message validator passed. That report-only
  commit was not pushed, to avoid triggering a redundant Windows build.
- Do not modify production runtime code. The progressive image workload remains
  gated because neither minimal probe passes.
- Next action: none for this investigation. Windows ownership acceptance stays
  blocked on a separately reviewed fix for the preexisting TCVM heap/TCZ
  startup failure.
- Validation completed: downloaded full logs/artifacts for all four prior
  runs; the perf-lab instrumentation contract suite passes 14 tests. The first
  prior run failed during deploy; later runs lacked app markers, and the final
  process exit was 0xC0000005.
- Focused validation passed eight diagnostic Python tests and 14 fixture
  contract tests. Ruby parsed both workflow YAML files; focused copyright
  validation passed; both worktrees pass `git diff --check`. The approved
  depot-tools dependency fetch completed successfully;
  no tracked dependency files changed.
- Deferred validation: the root reason the heap exception-field destination is
  invalid remains unresolved; the required next diagnostic is documented in
  the handoff report. No Windows binary was built or run locally.
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
