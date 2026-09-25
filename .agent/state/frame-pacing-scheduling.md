<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Frame pacing and scheduling execution state

## Active slice

Part 1 (Stages 1-3) is complete on branch
`feat/frame-pacing-scheduling-diagnostics`, based on immutable commit
`d7a9ec93faf9f2da0724611de115649176d0b033`. Part 2 is active and Stage 4 is
complete: relative/absolute native deadline scheduling passed its 12-process
macOS matrix, and its evidence is committed. Stage 5 is complete: SDL wait and
native yield commits passed the nine-process macOS matrix; its evidence is the
current checkpoint.

## Last logical commit

`0ee9086f7` adds the native yield selector; `703045cf9` adds the SDL
event-driven wait mode. `92f5e94e6` skips empty
benchmark Flick scrolls after rounded frames;
`85ff9a7b4` keeps benchmark Flick active through the rounded-frame plateau.
`4c32898d7` — add headroom for the terminal Flick frame.
`593bea07b` preserves failed Flick frame diagnostics.
`feddea120` adds timer deadline and event/yield benchmark matrices;
`2beb02475` adds absolute native deadlines with deterministic coverage. Stage 3
evidence JSON change is `de3a6af1a`; Stage 3 clock code is `fb9dda02b`; Stage 2
code commits are
`6ead9cd9d` (shared Flick advancement), `90bdb5f41` (TimerEvent/UpdateListener
drivers and tests), and `697d460ec` (benchmark harness and packaging). Stage 1
implementation is `fab38b8e0`. Earlier signed commits `992ec1ccc` and
`de7ad8077` had post-commit message-length findings; history was not rewritten
and both are recorded in the evidence index.

## Active paths

- `.agent/plans/frame-pacing-scheduling-part-1.md`
- `.agent/plans/frame-pacing-scheduling-part-2.md` (active)
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
- `TotalCrossVM/src/event/Event.c`
- `TotalCrossVM/src/init/globals.c/.h`
- `TotalCrossVM/src/nm/ui/MainWindow.c`
- `TotalCrossVM/src/nm/ui/MainWindow_test.h`
- `.agent/evidence/frame-pacing-stage-1-macos.csv`
- `.agent/evidence/frame-pacing-stage-1-macos.json`
- `.agent/evidence/frame-pacing-stage-2-macos.csv`
- `.agent/evidence/frame-pacing-stage-2-macos.json`
- `.agent/evidence/frame-pacing-stage-3-macos.csv`
- `.agent/evidence/frame-pacing-stage-3-macos.json`
- `.agent/evidence/frame-pacing-stage-4-macos.csv`
- `.agent/evidence/frame-pacing-stage-4-macos.json`

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

Implement the pure PowerShell Windows runner and package integration from the
Part 2 plan. Polling and legacy `Sleep(1)` remain the defaults.

## Validation and deferrals

- Activation header validation and staged diff checks passed. The signature is
  valid; the commit-message body-length check failed as recorded above.
- Stage 1 passed with six measured processes; Stage 2 passed with nine, all at
  -22,440 px; Stage 3 passed with twelve, all at -22,440 px. Every stage passed
  its preflight. Stage 3 rows explicitly identify `millis` or `nano`. Canonical
  CSV/JSON files for all stages are below 20 KiB.
- The first Stage 3 matrix validated all processes but the writer rejected the
  JSON size. The append-only evidence index records that attempt. The writer now
  omits null-only row fields and enforces both file-size caps before replacing
  evidence; the nine-case contract suite passes.
- Stage 4 native scheduling code and deterministic cases are committed at
  `2beb024753087a87adf58a2b736d81559990bdf3`; copyright and diff checks passed.
  The Stage 4/5 matrices, summary mode fields, 48-process static contract, and
  argument-size checks are committed at
  `feddea120980cf805d176d5a078d94d18fecd49d`. The 11-case benchmark contract
  suite passed. The CMake macOS ARM64 Release build and SDK packaging passed.
  Stage 4 needed two benchmark-only Flick fixes: continue through the rounded
  zero-motion plateau, then skip the zero-delta `scrollContent` call that the
  real ScrollContainer treats as an end condition. The deterministic Flick
  smoke test and focused native `setTimerInterval` test pass. Stage 4 then
  passed all 12 measured processes and preflight. Evidence files are 7,188 and
  18,758 bytes; their SHA-256 digests and the runtime identity are in the
  evidence index.
- Stage 5 SDL wait-loop commit `703045cf9` and native yield commit
  `0ee9086f7` passed a focused macOS native build and three native tests
  covering timer scheduling, seven event-loop helper checks, and yield-mode
  parsing. The nine-process macOS matrix and preflight passed; canonical
  evidence hashes and metric ranges are recorded in the evidence index. The
  harness is temporary under `/tmp`.
- `FlickDriverTest` and `SyntheticPacingTest` pass. SDK package builds and
  macOS ARM64 benchmark packaging passed for Stages 2 and 3. The Stage 3 bundle
  reused runtime SHA-256
  `ac48fc121de338951824d645f5c7d5e37090e33d6cc6a085bf9d4553906895e0`.
  Windows, Linux, Android, and iOS builds remain out of scope.

## Active decisions and blockers

- Preserve production defaults: TimerEvent at 40 fps and legacy millisecond
  animation time.
- Nano clock selection is benchmark-only; gesture sampling and the native
  timer scheduler retain their existing millisecond behavior.
- Branch base and current starting commit matched the plan's immutable SHA.
- `TC_IMAGE_CORPUS` is unset. Use the explicitly recorded
  `/Users/flsobral/Downloads/win32/win32` root from
  `.agent/evidence/image-scroll-diagnostics-macos-02-benchmark.md`; its `imag`
  directory was checked and contains 663 names (660 JPEG, 3 PNG).

## Resume command

Read this file first, then `.agent/plans/frame-pacing-scheduling-part-2.md` for
the next sequenced plan.
