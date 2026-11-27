<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Active state: production Windows Win32/I386 ownership validation

- Active milestone: validate the customer Win32/I386 target; the earlier AMD64
  crash investigation remains separate historical evidence.
- Branch/worktree: `codex/image-ownership-windows-crash-diagnostics` at
  `/Users/flsobral/.codex/worktrees/image-ownership-windows-crash-diagnostics`.
  The existing dedicated branch may be pushed to start the authorized Actions
  run; do not create a PR or modify production runtime code.
- Workflow implementation commit: `c932f8b743f100d4703d27601401d5bc1e10e519`
  (`test(windows): validate production Win32 ownership`); commit-message
  validation passed. It changes only the diagnostic workflow, harness,
  contracts, and plan/evidence records.
- Corrected source: `94af2b8b653cad509994b5ccf3dbb60ac28ef920`, built with the
  canonical Windows Release workflow and CMake `-A Win32`.
- Historical artifacts: runs `44414042826` and `44565183218` were downloaded,
  checked for exact clean source commit/tree, producer run ID, SDK/Windows build
  ID and PE I386 architecture. Both `Launcher.exe` and `tcvm.dll` are `0x014c`.
  Neither artifact pair includes matching PDB/MAP symbols. Full hashes are in
  `.agent/evidence/windows-image-ownership-crash-diagnostics.jsonl`.
- New workflow: `.github/workflows/windows-win32-image-ownership-validation.yml`
  builds the corrected source, runs two historical and one corrected startup
  probe on one Windows runner, then gates the pinned 1,510-control fixture on
  corrected startup passing. The exact same corrected SDK/runtime artifacts
  feed startup and ownership jobs.
- Current status: workflow and I386 debugger/marker contracts are implemented
  but not yet run. Production Win32 ownership status is INCOMPLETE. No claim of
  Windows ownership, rendering, or pixel correctness is permitted until all
  fixture assertions pass on I386.
- Local validation already passed: 15 crash-diagnostics Python tests, 17
  performance-lab I386 fixture contract tests, Ruby parsing of the three
  workflow YAML files, Python compile check, and `git diff --check`. Hosted
  PowerShell/CMake/runtime execution is pending GitHub Actions; do not build or
  execute Windows binaries locally.
- Next action: push the reviewed commits on this existing branch, then monitor
  the new workflow through startup and the gated ownership job. Record all run
  evidence and update the report/state after completion or a documented
  failure. The push path filter excludes later docs/evidence-only updates.
- Preserve the original root worktree, other branches, dependency caches,
  untracked artifacts, historical AMD64 evidence, and all existing reports.
