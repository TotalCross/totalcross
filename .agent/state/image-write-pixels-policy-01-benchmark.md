<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# State — focused writePixels policy benchmark preparation

## Active slice

- Plan 1 bootstrap complete; Milestone 1 is next.
- Required branch: `perf/image-decode-distributed-benchmark`.
- Starting HEAD: `02c7e1f528b4d198e6eecb57885313129ee7c294`.
- Next action: add same-frame active-work timing while preserving paced
  `frame_time_ns` and all rendering/policy behavior.

## Working set

- Plan: `.agent/plans/image-write-pixels-policy-01-benchmark.md`
- State: this file; rewrite on resume.
- Evidence: `.agent/evidence/image-write-pixels-policy-01-benchmark.md`
- Editorial report:
  `.agent/reports/image-write-pixels-policy-01-benchmark-editorial.md`
- Primary benchmark app:
  `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageScrollRealWorkloadBenchmarkApp.java`
- Runner: `scripts/run-image-scroll-distributed-benchmark.py`
- README: `scripts/README-image-benchmarks.md`

## Progress

- [x] Bootstrap plan/state/evidence and record scoped pre-existing files.
- [ ] Add active-work timing and validate its schema.
- [ ] Add the 24-process focused runner profile and pairwise output.
- [ ] Close Plan 1 and hand off to the device-copy policy plan.

## Validation and deferrals

- No SDK/native build or smoke has run for this plan.
- No source files have been changed by this plan yet.
- Build only SDK at the Milestone 1 gate; do not build native macOS for this
  measurement-only change.
- Do not run the 24-process focused benchmark; Plan 2 owns execution.

## Deliberate local files

Preserve all pre-existing untracked benchmark logs/directories, the image
optimization plans, `image-write-pixels-policy-02-device-copy.md`,
`scripts/__pycache__/`, and generated SDK fixture/launcher files. They are not
part of this plan's staging scope.

## Resume command

Read this state first, then the active milestone and named source paths in the
Plan 1 file. Use static/header checks between logical commits.
