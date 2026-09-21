<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# State — writePixels tail diagnosis

## Active slice

- Milestone 0 bootstrap is active.
- Branch: `perf/writepixels-tail-diagnosis`.
- Requested base: `perf/image-decode-distributed-benchmark`.
- Last logical commit: none for this plan yet.
- Next action: record parent/base SHA and corpus/package prerequisites, then
  commit the plan/state/evidence bootstrap.

## Working set

- Plan: `.agent/plans/image-writepixels-tail-diagnosis.md`
- State: `.agent/state/image-writepixels-tail-diagnosis.md`
- Evidence: `.agent/evidence/image-writepixels-tail-diagnosis.md`
- Final report:
  `.agent/reports/image-writepixels-tail-diagnosis-editorial.md`
- Final compact artifacts:
  `.agent/benchmarks/image-writepixels-tail-diagnosis/`
- Primary app:
  `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageScrollRealWorkloadBenchmarkApp.java`
- Runner: `scripts/run-image-scroll-distributed-benchmark.py`
- Native measurement:
  `TotalCrossVM/src/nm/ui/skia/skia_image_backing.cpp` and its bridge files.

## Validation completed

- Read `AGENTS.md`, `.agent/PLANS.md`, and
  `.agents/skills/logical-commits/SKILL.md` in full.
- Inspected the M1–M4 state/evidence, current runner profiles, app frame schema,
  native writePixels implementation, and existing accounting bridge.
- Created the branch from the checked-out
  `perf/image-decode-distributed-benchmark` branch without touching existing
  untracked artifacts.

## Deferred validation

- No SDK/native build yet; builds are deferred until the end of the related
  instrumentation milestone as required by the objective.
- No benchmark matrix yet; fresh samples wait for schema and package gates.
- No historical M1–M4 evidence was modified.

## Active decisions

- Primary A/B: `32795` versus `32799`.
- Source-backing stratum: `32827` versus `32831`.
- Attribution uses accounting-on; accounting-off is a later timing control.
- `work_time_ns` is primary; scroll and paint timings are attribution fields.
- No policy/default/renderer behavior changes are authorized.

## Deliberate out-of-scope local files

Preserve all pre-existing untracked logs, generated benchmark directories,
`TotalCrossSDK/IOSDateFixture.tcz`, `TotalCrossSDK/etc/launchers/`, and
`scripts/__pycache__/`. Do not stage them.

## Resume command

Read this file, then the active milestone in
`.agent/plans/image-writepixels-tail-diagnosis.md`, and inspect only the paths
listed under the active slice before taking the next action.
