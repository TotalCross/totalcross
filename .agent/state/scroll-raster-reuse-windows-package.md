<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Windows scroll raster reuse package state

## Active slice

The Windows scroll raster reuse benchmark completed successfully with the
rebuilt hotfix package. Both correctness preflights and all six measured
processes passed.

## Last logical commit

`7af05d79f7a74a7e3ace2aad9e5bafea2cc2ccba` contains the signed app/native,
package/runner, and threshold-metrics commits. It is pushed to
`origin/feat/frame-pacing-scheduling-diagnostics`. Base was
`1c6306b0861c589b8c6abe5f494c5c623db346f9`.

Hotfix source commit `1e8d7c03f120356316603f6a32ca929d7423748c` fixes the
Windows ON assertion and PowerShell resume path. It was built against the
original SDK artifact attested by `7af05d79f7a74a7e3ace2aad9e5bafea2cc2ccba`.

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

## Operator result

The successful result is
`/Users/flsobral/Downloads/scroll-raster-reuse-windows-results-20260926-222513-115.zip`.
Its metadata reports PASS, two preflights, six measured processes in OFF × 3
then ON × 3 order, and the expected corpus hash. The final run used the rebuilt
source commit `1e8d7c03f120356316603f6a32ca929d7423748c`.

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
- The hotfix package was rebuilt from the same full SDK ZIP and image corpus;
  the deployed Windows launcher and `tcvm.dll` hashes match the original package.
- Hotfix package build passed and produced
  `/tmp/scroll-raster-reuse-windows/hotfix-package/image-scroll-raster-reuse-windows-x64.zip`.
  The replacement app TCZ SHA-256 is
  `bac0a5a835896af9e8cc8f8c6b95677366b58321c93aad6181bea70f526ee5c5`.
- The small replacement patch is
  `/tmp/scroll-raster-reuse-windows/hotfix-package/scroll-raster-reuse-windows-preflight-fix.zip`
  (56,711 bytes; SHA-256
  `50de92eae3ef5e0c7c36a828f12167192a4e8edc04fc51d14e48f31b204d2666`).
- `git diff --check`, focused copyright validation, and patch manifest hash
  checks passed. The successful Windows run also exercised the PowerShell
  parser and benchmark runner.
- Operator archive
  `/Users/flsobral/Downloads/scroll-raster-reuse-windows-results-20260926-174457-904.zip`
  contains both `cold` and `warm` OFF-preflight records and a successful app
  summary. Its execution metadata reports zero accepted preflights and zero
  measured processes because PowerShell rejected the record order afterward.
- Second operator archive
  `/Users/flsobral/Downloads/scroll-raster-reuse-windows-results-20260926-185338-152.zip`
  reports OFF preflight passed, ON app exit code 1, and no measured processes.
  Its app summary identifies `cold recorded hits with raster reuse disabled`;
  the process ran with reuse ON and SDL `ARGB8888`.
- Final operator archive reports both preflights passed, ten matching
  waypoints, final displacement 4095, and all six measured processes passed.
  Every ON sample recorded 142/142 reuse hits and zero fallbacks. Median frame
  interval P95 changed by +0.24% cold and -2.04% warm; active-work P95 changed
  by -40.21% cold and -36.77% warm. Screen-update P95 changed by +6.07% cold
  and -9.05% warm, so the frame-time result is mixed rather than a blanket
  end-to-end speedup.
