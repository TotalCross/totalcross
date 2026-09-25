<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Frame pacing and scheduling execution state

## Active slice

Part 1 activation is in progress on branch
`feat/frame-pacing-scheduling-diagnostics`, based on immutable commit
`d7a9ec93faf9f2da0724611de115649176d0b033`. The user requested execution of
`.agent/plans/frame-pacing-scheduling-part-1.md`; Part 2 remains out of scope
until Part 1 closes.

## Last logical commit

The activation scaffold is the first logical commit for this plan. No
implementation commit exists yet; record the activation commit ID in the first
state update after creation.

## Active paths

- `.agent/plans/frame-pacing-scheduling-part-1.md`
- `.agent/plans/frame-pacing-scheduling-part-2.md` (sequenced follow-on only)
- `.agent/state/frame-pacing-scheduling.md`
- `.agent/evidence/frame-pacing-scheduling.md`
- `.agent/archive/frame-pacing-scheduling-history.md`
- `.agent/reports/frame-pacing-scheduling-editorial.md`

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

Complete and sign the activation commit with only the two pacing plan files and
the four initial supporting files. Then inspect the Stage 1 paths, check for an
explicitly recorded `TC_IMAGE_CORPUS` location, and implement the synthetic
pacer measurement slice.

## Validation and deferrals

- Activation: check staged scope, `git diff --check --cached`, focused header
  validation, and the logical commit message check.
- No implementation validation has run.
- SDK/macOS benchmark builds are deferred to Part 1 milestone closures as the
  plan requires. No Windows or other platform build is in scope.

## Active decisions and blockers

- Preserve production defaults: TimerEvent at 40 fps and legacy millisecond
  animation time.
- Branch base and current starting commit matched the plan's immutable SHA.
- Corpus path is not yet established in this state; inspect existing pacing or
  PNG benchmark state/evidence. If none explicitly records it, stop at the
  plan's corpus blocker rather than searching user directories.

## Resume command

Read this file first, then the active Stage heading in
`.agent/plans/frame-pacing-scheduling-part-1.md`; search the evidence index only
for that stage.
