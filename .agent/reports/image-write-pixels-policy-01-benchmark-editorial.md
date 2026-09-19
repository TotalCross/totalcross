<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Focused writePixels policy benchmark preparation

## Purpose / Big Picture

Plan 1 adds same-frame active-work diagnostics and a reproducible focused
profile for the later device-copy policy experiment. It does not change
rendering behavior or enable any native optimization policy.

## Working Set and Resume Protocol

The benchmark app is
`TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageScrollRealWorkloadBenchmarkApp.java`.
The runner and its user-facing contract are
`scripts/run-image-scroll-distributed-benchmark.py` and
`scripts/README-image-benchmarks.md`. State and append-only evidence are in
`.agent/state/image-write-pixels-policy-01-benchmark.md` and
`.agent/evidence/image-write-pixels-policy-01-benchmark.md`. Plan 2 is the
preserved handoff at `.agent/plans/image-write-pixels-policy-02-device-copy.md`.

## Progress

All Plan 1 milestones are complete. The next safe action is Plan 2 execution
of the focused profile after its device-copy policy changes.

## Current Architecture and Scope

Paced `frame_time_ns` remains the interval between frame starts. The additive
active region reports `scroll_work_ns`, `paint_work_ns`, and `work_time_ns`,
with JPEG counter snapshots around that region. Run and distributed summaries
carry P50/P95/P99/MAX work and paint values.

The default `full` profile retains 21 masks, two prefetch modes, three rounds,
and 126 processes. `write-pixels-policy` uses masks `0, 4, 2, 6, 32795, 32799`,
two prefetch modes, two rounds, and 24 processes. It writes a 12-row pairwise
artifact for the three controlled mask pairs.

## Plan of Work

The bootstrap artifacts were committed first. The app instrumentation landed in
`11042035e`, with timer-boundary correction in `28c942ef7`. The runner profile,
pairwise aggregation, and README landed in `b97e68583`.

## Decision Log

- Preserve paced frame timing for compatibility and use active work for the
  later warm-path comparison.
- Keep the historical full matrix and manifest contract unchanged.
- Use exactly two focused rounds and report contradictory work-P50 directions
  as `INCONCLUSIVE_VARIANCE`; do not expand the matrix automatically.

## Validation and Acceptance

Passed: focused copyright/header checks, whitespace checks, package-script
syntax, runner bytecode compilation, deterministic planning/parser fixture,
smoke-source compilation, and SDK `dist -x test`. The fixture proved full=126,
focused=24, two runs per focused key, complete controlled pairs, 24 aggregate
rows, 12 pairwise rows, and work-time invariants.

The focused 24-process benchmark and native macOS build/smoke were not run;
they belong to Plan 2 and are outside this measurement-only preparation.

## Risks and Open Questions

Two rounds are intentionally shallow and cannot establish statistical
significance. Device-space copy policy behavior and its measured effect remain
unknown until Plan 2 runs on the focused profile.

## Idempotence and Recovery

The focused profile can be rerun from an existing validated bundle with
`--profile write-pixels-policy --phase matrix`. Temporary parser fixtures and
SDK logs were kept outside tracked source. Existing benchmark logs, plans,
caches, and generated fixtures were preserved.

## Outcomes & Retrospective

The raw frame schema, JSON summaries, pass records, distributed CSV, and
pairwise comparison contract are ready for Plan 2. No source behavior beyond
measurement and runner aggregation changed. One commit-message check reported
an overlong body line in `b97e68583` because the shell preserved literal
`\\n`; history was not rewritten, and the deviation is recorded in state and
evidence.

## Revision Note

Plan 1 closed on 2026-09-18 after the second SDK gate necessitated by the
timer-boundary correction. Plan 2 owns focused execution and device-copy
policy changes.
