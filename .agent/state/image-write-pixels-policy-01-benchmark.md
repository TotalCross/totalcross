<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# State — focused writePixels policy benchmark preparation

## Active slice

- Plan 1 complete; Plan 2 is the next active handoff.
- Required branch: `perf/image-decode-distributed-benchmark`.
- Starting HEAD: `02c7e1f528b4d198e6eecb57885313129ee7c294`.
- Milestone 1 commit: `11042035e`.
- Milestone 1 timer-alignment fix: `28c942ef7`.
- Milestone 2 commit: `b97e68583`.
- Next action: Plan 2 may execute the focused 24-process profile on the device
  copy policy.

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
- [x] Add active-work timing and validate its schema.
- [x] Add the 24-process focused runner profile and pairwise output.
- [x] Close Plan 1 and hand off to the device-copy policy plan.

## Validation and deferrals

- The preserved bootstrap commit `fe6eef5ae` has overlong body lines under the
  commit checker; it was not amended because this plan forbids history rewrite.
- Milestone 1 validation passed: `compileSmokeTestJava` and `dist -x test`.
- Full logs: `/tmp/image-write-pixels-policy-01-compile-smoke.log` and
  `/tmp/image-write-pixels-policy-01-sdk-dist.log`.
- Milestone 2 static checks passed: `bash -n` for the package script,
  copyright validation, `git diff --check`, and runner `py_compile`.
- Deterministic parser/planning fixture passed: full=126, focused=24,
  focused rows=24, pairwise rows=12, and work invariants accepted.
- Java changed in the timer-alignment fix after Milestone 1, so the SDK smoke
  compile and `dist -x test` were repeated; both passed. Logs:
  `/tmp/image-write-pixels-policy-01-milestone2-compile-smoke.log` and
  `/tmp/image-write-pixels-policy-01-milestone2-sdk-dist.log`.
- No native macOS build or smoke has run for this plan.
- The final profile commit `b97e68583` has one overlong body line because the
  shell commit command preserved literal `\\n`; the checker failure was
  recorded without rewriting history.
- Active-work schema is present in `frames.csv`, `summary.json`, and pass output;
  the source validates nonnegative work and work >= each component.
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
