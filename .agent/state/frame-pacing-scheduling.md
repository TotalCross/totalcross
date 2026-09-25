<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Frame pacing and scheduling execution state

## Active slice

Stage 1 synthetic pacing instrumentation is in progress on branch
`feat/frame-pacing-scheduling-diagnostics`, based on immutable commit
`d7a9ec93faf9f2da0724611de115649176d0b033`. The user requested execution of
`.agent/plans/frame-pacing-scheduling-part-1.md`; Part 2 remains out of scope
until Part 1 closes.

## Last logical commit

`fab38b8e0` — signed Stage 1 implementation and test commit. Its commit-message
and signature checks passed. Activation commit `de7ad8077` was signed; its body
length issue is recorded in the evidence index and was not rewritten.

## Active paths

- `.agent/plans/frame-pacing-scheduling-part-1.md`
- `.agent/plans/frame-pacing-scheduling-part-2.md` (sequenced follow-on only)
- `.agent/state/frame-pacing-scheduling.md`
- `.agent/evidence/frame-pacing-scheduling.md`
- `.agent/archive/frame-pacing-scheduling-history.md`
- `.agent/reports/frame-pacing-scheduling-editorial.md`
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageScrollRealWorkloadBenchmarkApp.java`
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/SyntheticPacingTest.java`
- `scripts/run-frame-pacing-benchmark.py`
- `scripts/frame_pacing_contract.py`
- `scripts/frame_pacing_results.py`
- `scripts/test-frame-pacing-benchmark.py`
- `scripts/README-image-benchmarks.md`

## Preservation inventory

These pre-existing worktree paths were present during activation. Do not stage,
modify, or remove them as part of this plan:

- `totalcross.code-workspace` (modified)
- `.agent/artifacts/`
- `.agent/benchmarks/image-scroll-prefetch/final-definitive-pass/` (16 logs)
- `.agent/benchmarks/image-scroll-prefetch/final-definitive/`
- `.agent/benchmarks/image-scroll-prefetch/final-fixed/fixture-deploy.log`
- `.agent/benchmarks/image-scroll-prefetch/final-fixed/fixture-jar.log`
- `.agent/benchmarks/image-scroll-prefetch/final-fixed/sdk-dist.log`
- `.agent/benchmarks/image-scroll-prefetch/worker-semaphore-milestone2/`
- `.agent/benchmarks/scroll-raster-reuse-poc/m2/`
- `.agent/plans/image-optimization-mask-01-diagnose.md`
- `.agent/plans/image-optimization-mask-02-fix.md`
- `.agent/plans/image-scroll-prefetch-reduction.md`
- `ImageScrollRasterFastPathBenchmarkApp.log`
- `ImageScrollRealWorkloadBenchmarkApp.log`
- `TotalCrossSDK/IOSDateFixture.tcz`
- `TotalCrossSDK/ImageScrollRealWorkloadBenchmarkApp.log`
- `TotalCrossSDK/etc/launchers/`
- `TotalCrossVM/xcode/generated/`
- `scripts/__pycache__/`

## Next concrete action

The initial Stage 1 correctness preflight exited with signal `-5` before app
output; its log and execution directory are retained under
`/var/folders/k8/02b7wfkd7fn32vtm3t5mwxwr0000gn/T/frame-pacing-stage-1-l46l2s78`.
The existing macOS runner uses a bundle-relative output path, while the new
runner passed an absolute temporary path. The runner now uses a unique,
bundle-relative output directory per invocation. Re-run its focused contract
checks and the Stage 1 preflight/matrix; commit canonical CSV/JSON only after
every process validates.

## Validation and deferrals

- Activation header validation and staged diff checks passed. The signature is
  valid; the commit-message body-length check failed as recorded above.
- Python frame-pacing contract tests pass (5 cases before output-path fix); shared image-scroll
  distributed benchmark tests pass; focused headers and diff checks pass.
  `SyntheticPacingTest` passed. The Stage 1 process matrix is pending. The shared
  test log is `/tmp/frame-pacing-stage-1-distributed-tests.log`.
- SDK and macOS ARM64 builds are now in scope at this Stage 1 milestone
  closure. No Windows or other platform build is in scope.

## Active decisions and blockers

- Preserve production defaults: TimerEvent at 40 fps and legacy millisecond
  animation time.
- Branch base and current starting commit matched the plan's immutable SHA.
- `TC_IMAGE_CORPUS` is unset. Use the explicitly recorded
  `/Users/flsobral/Downloads/win32/win32` root from
  `.agent/evidence/image-scroll-diagnostics-macos-02-benchmark.md`; its `imag`
  directory was checked and contains 663 names (660 JPEG, 3 PNG).

## Resume command

Read this file first, then the active Stage heading in
`.agent/plans/frame-pacing-scheduling-part-1.md`; search the evidence index only
for that stage.
