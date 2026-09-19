<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# State — device-space writePixels policy

## Active slice

- Milestone 1 implementation is complete; Milestone 2 is active.
- Required branch: `perf/image-decode-distributed-benchmark`.
- Plan 1 closure revision: `8ab1c6b28a6238c2e2aea53afcd6103bfc46d45b`.
- Plan 2 specification revision: `9594720fa`.
- Next action: add the native correctness matrix for regular device-space
  copies, then build and run the macOS ARM64 native test gate.

## Working set

- Plan: `.agent/plans/image-write-pixels-policy-02-device-copy.md`
- State: this file; rewrite on resume.
- Evidence: `.agent/evidence/image-write-pixels-policy-02-device-copy.md`
- Editorial report:
  `.agent/reports/image-write-pixels-policy-02-device-copy-editorial.md`
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
- [ ] Milestone 2: add native clipping/transform tests and run focused smokes.
- [ ] Milestone 3: run the focused macOS matrix and close the plan.

## Validation and deferrals

- Bootstrap checks: branch, Plan 1 closure, Plan 2 specification, headers,
  size, and staged whitespace.
- Milestone 1 commit: `5e9540d70`.
- Milestone 1 checks: focused copyright-header validation, `git diff --check`,
  staged whitespace validation, Python AST parsing for the benchmark runner,
  and commit-message validation.
- No SDK/native build, native smoke, package, or 24-process benchmark has run
  for Plan 2; native validation begins at the Milestone 2 gate.
- Preserve verbose validation logs outside tracked source and record compact
  paths/results here and in the append-only evidence file.

## Deliberate local files

Preserve all unrelated benchmark logs/directories, optimization plans, generated
SDK fixtures/launchers, caches, and any partial Plan 2 artifacts. Do not delete
prior results or generated dependency outputs.

## Resume command

Read this state first, then the active Plan 2 milestone and only the named
native source/test paths. Use focused header and staged whitespace checks before
each logical commit.
