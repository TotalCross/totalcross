<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Frame pacing and scheduling execution state

## Active slice

Stages 1 and 2 are complete on branch `feat/frame-pacing-scheduling-diagnostics`,
based on immutable commit `d7a9ec93faf9f2da0724611de115649176d0b033`. Stage 3
clock comparison is active. The user requested
`.agent/plans/frame-pacing-scheduling-part-1.md`; Part 2 remains out of scope
until Part 1 closes.

## Last logical commit

`b7b7e13fb` — compact frame-pacing evidence and enforce the 20 KiB cap;
signature and focused validations passed. Stage 2 code commits are
`6ead9cd9d` (shared Flick advancement), `90bdb5f41` (TimerEvent/UpdateListener
drivers and tests), and `697d460ec` (benchmark harness and packaging). Stage 1
implementation is `fab38b8e0`. Earlier signed commits `992ec1ccc` and
`de7ad8077` had post-commit message-length findings; history was not rewritten
and both are recorded in the evidence index.

## Active paths

- `.agent/plans/frame-pacing-scheduling-part-1.md`
- `.agent/plans/frame-pacing-scheduling-part-2.md` (sequenced follow-on only)
- `.agent/state/frame-pacing-scheduling.md`
- `.agent/evidence/frame-pacing-scheduling.md`
- `.agent/archive/frame-pacing-scheduling-history.md`
- `.agent/reports/frame-pacing-scheduling-editorial.md`
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageScrollRealWorkloadBenchmarkApp.java`
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/SyntheticPacingTest.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/Flick.java`
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/FlickBenchmarkSupport.java`
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/FlickDriverTest.java`
- `scripts/run-frame-pacing-benchmark.py`
- `scripts/frame_pacing_contract.py`
- `scripts/frame_pacing_results.py`
- `scripts/test-frame-pacing-benchmark.py`
- `scripts/README-image-benchmarks.md`
- `.agent/evidence/frame-pacing-stage-1-macos.csv`
- `.agent/evidence/frame-pacing-stage-1-macos.json`
- `.agent/evidence/frame-pacing-stage-2-macos.csv`
- `.agent/evidence/frame-pacing-stage-2-macos.json`

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

Implement the Stage 3 benchmark-only animation clock selector. Keep gesture
sampling on the millisecond clock, test exact integer-millisecond equivalence
and sub-millisecond nano progress, then package and run the Stage 3 matrix.

## Validation and deferrals

- Activation header validation and staged diff checks passed. The signature is
  valid; the commit-message body-length check failed as recorded above.
- Stage 1 passed with six measured processes and a successful preflight. Stage
  2 passed with nine measured processes and a successful preflight; all nine
  completed at -22,440 px. The latest frame-pacing contract suite passes eight
  cases, `FlickDriverTest` and `SyntheticPacingTest` pass, and shared image-scroll
  distributed benchmark tests pass.
- `./gradlew-agent dist -x test`, `(cd scripts && ./package-sdk.sh)`, and the
  macOS ARM64 benchmark package step passed. The Stage 2 bundle reused runtime
  SHA-256 `ac48fc121de338951824d645f5c7d5e37090e33d6cc6a085bf9d4553906895e0`;
  the unrelated preexisting package-directory dylib was left untouched.
- Stage 2 canonical files are below the 20 KiB limit. The compact JSON writer
  checks both file sizes before replacing existing evidence. No Windows or
  other platform build is in scope.

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
