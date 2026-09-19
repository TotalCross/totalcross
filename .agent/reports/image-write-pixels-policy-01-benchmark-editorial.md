<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Focused writePixels policy benchmark preparation

## Editorial Summary

Plan 1 completed the measurement and profile preparation for the device-space
writePixels experiment. It added active-work timing without changing rendering
or native optimization policy, and it left Plan 2 ready to implement and
measure the policy on macOS.

## Original Plan versus Actual Outcome

The planned active-work schema, preserved full matrix, focused profile, and
pairwise comparison contract were delivered. The full profile remains 126
processes. The focused profile is exactly 24 processes: masks `0, 4, 2, 6,
32795, 32799`, prefetch off/on, and two rounds. The 24-process execution was
intentionally deferred to Plan 2, as specified.

## What Changed

`frame_time_ns` remains the paced, compatibility-preserving interval between
frame starts. Each frame now also records same-frame `scroll_work_ns`,
`paint_work_ns`, and `work_time_ns`; JPEG snapshots bracket that active region.
Run JSON, pass records, frames CSV, distributed summary CSV, and the focused
pairwise artifact carry the resulting work and paint aggregates.

The committed Plan 2 specification at
`.agent/plans/image-write-pixels-policy-02-device-copy.md` fixes the next
scope: safe device-space scale+translate mapping, explicit device
clip/source-subset handling, no `saveCount == 1` eligibility rule, preserved
opacity/fallback behavior, regular-path counters, native clipping/transform
tests, and the 24-process focused macOS run. It explicitly excludes physical
variant cache, JPEG, storage, resampling, and prefetch policy changes.

## Decisions and Trade-offs

The historical full matrix and manifest contract were kept unchanged. Two
focused rounds provide economical controlled comparisons rather than
publication-grade statistics. Controlled pairs are `0 -> 4`, `2 -> 6`, and
`32795 -> 32799`; contradictory work-P50 directions are reported as
`INCONCLUSIVE_VARIANCE` without adding a third round.

## Unexpected Problems and Discoveries

No implementation or rendering defect was discovered in Plan 1. The main
measurement finding was that paced frame timing is not the right denominator
for active warm-path work, which motivated the additive schema.

Two closeout commits, `b97e68583` and `e3ec4778d`, retained one overlong body
line because the shell passed literal `\\n` text to `git commit -m`. The
checker deviations were recorded factually; existing commits were not amended
or rewritten.

## Validation and Measurable Results

Focused copyright/header checks, whitespace checks, package-script syntax,
runner bytecode compilation, and the deterministic planning/parser fixture
passed. The fixture proved full planning at 126, focused planning at 24, two
runs per focused key, complete controlled pairs, 24 aggregate rows, 12
pairwise rows, and work-time invariants.

SDK smoke compilation and `dist -x test` passed at both Plan 1 gates. No native
build, native smoke, or 24-process benchmark was run, because those validations
belong to Plan 2.

## Useful Evidence and Examples

- `11042035e`: active-work timing and summary schema.
- `28c942ef7`: alignment of the active timer boundary.
- `b97e68583`: focused profile, aggregation, pairwise output, and README.
- `9594720fa`: committed Plan 2 device-copy specification.
- `/tmp/image-write-pixels-policy-01-compile-smoke.log` and
  `/tmp/image-write-pixels-policy-01-sdk-dist.log`: first SDK gate logs.
- `/tmp/image-write-pixels-policy-01-milestone2-compile-smoke.log` and
  `/tmp/image-write-pixels-policy-01-milestone2-sdk-dist.log`: repeated SDK
  gate logs after the timer correction.

## Limitations, Remaining Work, and Open Questions

Plan 1 contains no performance result for the writePixels policy. Plan 2 must
implement and test the device-space planner, prove clip and transform safety,
run the focused macOS smokes, and execute all 24 matrix processes. The two-run
design remains intentionally limited, and existing environment metadata
limitations remain outside scope.

## Possible Article Angles

- Why paced frame intervals can hide improvements in a warm rendering path.
- How explicit device-space clipping makes a low-level pixel shortcut safe.
- Why a small controlled mask matrix is more useful than an unbounded benchmark
  expansion for an optimization policy decision.

## Suggested Narrative

Start with the mismatch between 60 Hz pacing and actual scroll/paint work.
Introduce the additive same-frame metrics and preserved JPEG attribution, then
show how the 126-process historical suite coexists with a 24-process policy
profile. End with the Plan 2 handoff: prove device-space clipping and
scale+translate correctness before treating any active-work delta as evidence.

## Claims Requiring Human Review

The completed work supports claims about instrumentation, planning counts, and
validation only. It does not support a claim that writePixels is faster, safer
in production, or beneficial on macOS until Plan 2 completes its native tests,
focused smokes, and 24-process measurement.
