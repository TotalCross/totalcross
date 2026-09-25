<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Windows scroll raster reuse package state

## Active slice

M0: adapt the existing raster-reuse smoke app for a dedicated full-corpus
Windows comparison, then package from the exact action-built SDK artifact.

## Last logical commit

None. Initial base is `1c6306b0861c589b8c6abe5f494c5c623db346f9`, the verified
HEAD of `origin/feat/frame-pacing-scheduling-diagnostics`.

## Active paths

- `.agent/plans/scroll-raster-reuse-windows-package.md`
- `.agent/state/scroll-raster-reuse-windows-package.md`
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageScrollRealWorkloadBenchmarkApp.java`
- `TotalCrossVM/src/init/tcsdl.cpp`
- `scripts/package-image-scroll-benchmark.sh`
- `scripts/run-scroll-raster-reuse-windows.ps1`
- `scripts/scroll-raster-reuse-windows-functions.ps1`
- `scripts/test-scroll-raster-reuse-windows.py`
- `scripts/README-image-benchmarks.md`
- `.agent/evidence/scroll-raster-reuse-windows-package.json`
- `.agent/reports/scroll-raster-reuse-windows-package.md`

## Preservation

The original worktree contains uncommitted frame-pacing runner/helper changes
and unrelated untracked plans/cache files. Work in the isolated task worktree;
do not stage or alter those paths.

## Next concrete action

Add the dedicated 663-image profile without changing the existing 120-image
POC or full-corpus release profiles. Confirm benchmark-only SDL selected-format
reporting and then implement runner/package support.

## Validation and deferrals

- Current local HEAD equals remote target HEAD. No source changes or tests have
  been made in the isolated worktree yet.
- Windows build and Windows benchmark execution are explicitly prohibited.
- Successful workflow dispatch, full SDK artifact, package SHA, and final
  provenance remain required.
