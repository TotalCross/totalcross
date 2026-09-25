<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Windows scroll raster reuse package state

## Active slice

M1 implementation is complete. M2: finalize all requested per-process frame
threshold metrics, commit the runner correction, then rebuild/package from the
exact final source SHA. The Windows benchmark itself remains unexecuted.

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

Commit the added 16.67/20/25 ms process thresholds, then push the exact source
SHA and dispatch `package.yml`; package from that run's full SDK artifact.

## Validation and deferrals

- Current implementation includes the dedicated 663-image app profile, gated
  SDL format reporting, dedicated package mode, two correctness preflights,
  six-process runner, periodic process progress, and cold/warm summaries.
- The previous exact-source workflow run `36182451240` for `35b7a0139` succeeded;
  its full SDK artifact ID is `10884319717`, SDK ZIP SHA-256 is
  `3500878397f787d6cf5dd62c2c98ba44fa6dfab6f5878c867733de6257d69d98`. A final
  workflow run is required after adding all requested process thresholds.
- Passed after the threshold correction: four new runner/package contract groups,
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
