<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# State — device-space writePixels policy

## Active slice

- Plan 2 is complete; all three milestones passed their required gates.
- Required branch: `perf/image-decode-distributed-benchmark`.
- Plan 1 closure revision: `8ab1c6b28a6238c2e2aea53afcd6103bfc46d45b`.
- Plan 2 specification revision: `9594720fa`.
- Implementation/fix revision: `5265f38a6`.
- Final documentation and results revision: `85c1ed5bb`.

## Working set

- Plan: `.agent/plans/image-write-pixels-policy-02-device-copy.md`
- State: this file; rewrite on resume.
- Evidence: `.agent/evidence/image-write-pixels-policy-02-device-copy.md`
- Editorial report:
  `.agent/reports/image-write-pixels-policy-02-device-copy-editorial.md`
- Benchmark summary:
  `.agent/benchmarks/image-write-pixels-device-policy/summary.md`
- Policy source: `TotalCrossVM/src/nm/ui/skia/skia_image_backing.cpp`
- Internal API:
  `TotalCrossVM/src/nm/ui/skia/skia_image_backing_internal.h`
- Native tests: `TotalCrossVM/src/nm/ui/skia/skia_surface_test.cpp`

## Fixed scope

- Use safe device-space positive scale+translate mapping.
- Apply explicit device clip and source-subset handling because
  `writePixels()` ignores matrix and clip.
- Do not use `saveCount == 1` as an eligibility rule.
- Preserve opacity and generic fallback behavior.
- Add regular-path writePixels attempts, hits, fallbacks, copied bytes, and
  clipped-hit counters.
- Do not change physical variant cache, JPEG, storage, resampling, or prefetch
  policies.
- Plan 2's final benchmark is the Plan 1 focused profile: 6 masks, 2 prefetch
  modes, 2 rounds, 24 processes.

## Progress

- [x] Verify Plan 1 closure and commit the Plan 2 specification.
- [x] Create Plan 2 bootstrap state and evidence.
- [x] Milestone 1: implement the device-space policy and regular counters.
- [x] Milestone 2: add native clipping/transform tests and run focused smokes.
- [x] Milestone 3: run the focused macOS matrix and close the plan.

## Validation and deferrals

- Bootstrap checks: branch, Plan 1 closure, Plan 2 specification, headers,
  size, and staged whitespace.
- Milestone 1 commit: `5e9540d70`.
- Milestone 1 checks: focused copyright-header validation, `git diff --check`,
  staged whitespace validation, Python AST parsing for the benchmark runner,
  and commit-message validation.
- Milestone 2 checks: macOS ARM64 CMake/Ninja build of `tcvm`, `Launcher`, and
  `skia_surface_test`; native assertions; SDK distribution; macOS package;
  self-test; standard smokes; and focused smokes for
  `4/6/32795/32799 × off/on` all passed.
- Milestone 3 checks: exactly 24/24 focused matrix processes passed in two
  rounds; aggregation and pairwise comparison passed. See the benchmark
  summary and append-only evidence for paths and metrics.
- Deferred: full platform matrix, standalone decode matrix, and release
  packaging outside macOS ARM64; these are outside Plan 2's focused gate.
- Preserve verbose validation logs outside tracked source and record compact
  paths/results here and in the append-only evidence file.

## Deliberate local files

Preserve all unrelated benchmark logs/directories, optimization plans, generated
SDK fixtures/launchers, caches, and any partial Plan 2 artifacts. Do not delete
prior results or generated dependency outputs.

## Resume command

Plan 2 is closed. For audit, read this state, the editorial report, the final
summary, and the append-only evidence. Do not delete the task-specific package,
raw result ZIP, or validation logs.
