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

`0a0c62ab7` — signed macOS visible-output-directory correction. Signature and
commit-message checks passed. Earlier signed commits `992ec1ccc` and
`de7ad8077` had post-commit message-length findings; history was not rewritten
and both limitations are recorded in the evidence index.
`fab38b8e0` is the signed Stage 1 implementation and test commit.

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

Two launcher failures were caused by unsupported output paths and were fixed.
The next matrix reached its second pacing profile but the native 256-byte app
command-line buffer truncated `--synthetic-pacing=synthetic-60hz`, so validation
stopped before canonical evidence writing. The runner now omits default and
manifest-verified options to fit all Part 1 configurations. Rerun focused
contracts and the Stage 1 preflight/matrix.

## Validation and deferrals

- Activation header validation and staged diff checks passed. The signature is
  valid; the commit-message body-length check failed as recorded above.
- Python frame-pacing contract tests pass (6 cases after the output-path fixes);
  shared image-scroll distributed benchmark tests pass; focused headers and
  diff checks pass. Both runner-correction signatures pass; the latest commit
  message check passes. `SyntheticPacingTest`, SDK packaging, and macOS ARM64
  CMake configure/build passed. The Stage 1 process matrix is pending. The
  shared test log is `/tmp/frame-pacing-stage-1-distributed-tests.log`.
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
