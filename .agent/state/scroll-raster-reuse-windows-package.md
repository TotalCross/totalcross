<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Windows scroll raster reuse package state

## Active slice

Implementation, focused validation, final-source workflow, package creation, and
provenance are complete. The Windows benchmark itself remains unexecuted.

## Last logical commit

`7af05d79f7a74a7e3ace2aad9e5bafea2cc2ccba` contains the signed app/native,
package/runner, and threshold-metrics commits. It is pushed to
`origin/feat/frame-pacing-scheduling-diagnostics`. Base was
`1c6306b0861c589b8c6abe5f494c5c623db346f9`.

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

No benchmark run remains in this task. The operator ZIP and provenance report
are ready at the recorded paths.

## Validation and deferrals

- Current implementation includes the dedicated 663-image app profile, gated
  SDL format reporting, dedicated package mode, two correctness preflights,
  six-process runner, periodic process progress, and cold/warm summaries.
- Final exact-source `package.yml` run `36183652625` succeeded for
  `7af05d79f7a74a7e3ace2aad9e5bafea2cc2ccba`; full SDK artifact ID is
  `10885666470`, downloaded ZIP SHA-256 is
  `bb0f66a0a15fb6bf9eb5b8988140e712b39a17f00541102a2943074bd4bfe1b8`.
- Package ZIP is `/tmp/scroll-raster-reuse-windows/package-final/image-scroll-raster-reuse-windows-x64.zip`,
  SHA-256 `97709db4cd69a326f5a3232a99981c73ad49ff53dda45b09a84421d643774235`.
- Passed after the threshold correction: four new runner/package contract groups,
  existing frame-pacing contracts (13 checks), existing image-scroll
  distributed-benchmark suite, `bash -n scripts/package-image-scroll-benchmark.sh`,
  `git diff --check`, and focused copyright-header validation.
- ZIP integrity, package manifest, runtime/app/runner hashes, corpus digest and
  file counts all passed. The packager compiled and deployed the app using the
  action-built SDK ZIP.
- Local PowerShell parsing was unavailable on this macOS host. No local Windows
  build or benchmark execution occurred; benchmark process count is zero.
