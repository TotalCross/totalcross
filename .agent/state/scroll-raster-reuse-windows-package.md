<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Windows scroll raster reuse package state

## Active slice

M1 implementation is complete. M2: create signed logical commits, push the
requested branch, dispatch the exact SHA, then package from its action-built SDK.

## Last logical commit

`75b0fb97a` adds the app profile and benchmark-gated SDL pixel-format evidence.
It is signed. `8617a0a35` records the initial plan and state. The implementation
base is `1c6306b0861c589b8c6abe5f494c5c623db346f9`, the verified HEAD of
`origin/feat/frame-pacing-scheduling-diagnostics`.

## Active paths

- `.agent/plans/scroll-raster-reuse-windows-package.md`
- `.agent/state/scroll-raster-reuse-windows-package.md`
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageScrollRealWorkloadBenchmarkApp.java`
- `TotalCrossVM/src/init/tcsdl.cpp`
- `scripts/package-image-scroll-benchmark.sh`
- `scripts/run-scroll-raster-reuse-windows.ps1`
- `scripts/scroll-raster-reuse-windows-functions.ps1`
- `scripts/scroll-raster-reuse-windows-analysis.ps1`
- `scripts/test-scroll-raster-reuse-windows.py`
- `scripts/README-image-benchmarks.md`
- `.agent/evidence/scroll-raster-reuse-windows-package.json`
- `.agent/reports/scroll-raster-reuse-windows-package.md`

## Preservation

The original worktree contains uncommitted frame-pacing runner/helper changes
and unrelated untracked plans/cache files. Work in the isolated task worktree;
do not stage or alter those paths.

## Next concrete action

Review and commit the package, runner, tests, and README as a signed logical
slice, then push only the requested branch and start its SDK package workflow.

## Validation and deferrals

- Current implementation includes the dedicated 663-image app profile, gated
  SDL format reporting, dedicated package mode, two correctness preflights,
  six-process runner, periodic process progress, and cold/warm summaries.
- Passed after the final edits: four new runner/package contract groups,
  existing frame-pacing contracts (13 checks), existing image-scroll
  distributed-benchmark suite, `bash -n scripts/package-image-scroll-benchmark.sh`,
  `git diff --check`, and focused copyright-header validation.
- Local PowerShell parsing and SDK/native compilation were unavailable: this
  host has no PowerShell runtime, SDK JAR, or configured native build tree.
  The exact-source package workflow will perform artifact builds; no local
  Windows build or benchmark has been run.
- Windows build and Windows benchmark execution are explicitly prohibited.
- Successful workflow dispatch, full SDK artifact, package SHA, and final
  provenance remain required.
