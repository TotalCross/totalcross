<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Plan 1 of 2 — add active-work timing and a focused writePixels benchmark profile

This ExecPlan follows `AGENTS.md`, the ExecPlan rules in
`TotalCross/totalcross-depot-tools:.agent/PLANS.md`, and
`.agents/skills/logical-commits/SKILL.md`.

Execute only on branch `perf/image-decode-distributed-benchmark`. Do not create,
switch, rebase, reset, amend, or rewrite branches or commits.

## Purpose / Big Picture

Prepare a faster and more accurate benchmark for the next
`RASTER_OPAQUE_WRITE_PIXELS` policy experiment.

This plan must:

1. add same-frame active-work timing so warm-path improvements are not hidden by
   the existing ~60 Hz pacing;
2. add a focused scroll profile with six masks and two rounds;
3. preserve the historical full benchmark behavior unchanged.

Do not change rendering policy, `OPAQUE_WRITE_PIXELS`,
`PHYSICAL_VARIANT_CACHE`, JPEG decode/tier selection, resampling, storage
formats, or prefetch behavior.

Plan 2 will implement the writePixels policy and run this focused profile.

## Working Set and Resume Protocol

Create and commit:

- `.agent/plans/image-write-pixels-policy-01-benchmark.md`
- `.agent/state/image-write-pixels-policy-01-benchmark.md`
- `.agent/evidence/image-write-pixels-policy-01-benchmark.md`
- `.agent/reports/image-write-pixels-policy-01-benchmark-editorial.md`

Read state first on resume, then only the active milestone and active paths.

Primary paths:

- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageScrollRealWorkloadBenchmarkApp.java`
- `scripts/run-image-scroll-distributed-benchmark.py`
- `scripts/README-image-benchmarks.md`

Read prior diagnostic artifacts only when a specific contract is unclear.

## Execution Constraints

- Operate economically with tokens and output.
- Prefer `rg`, narrow reads, and scoped Git commands.
- Preserve unrelated local changes; stage only task paths.
- Build only SDK and macOS, and only at related milestone boundaries.
- Native smokes may run only at milestone boundaries/final validation.
- Do not run builds between logical commits inside a milestone.
- Redirect verbose output to task-specific logs outside tracked source.
- Commit every deliberate repository artifact created by this plan.
- Build outputs, logs, raw benchmark directories, and benchmark ZIPs are not
  committed.
- Every new tracked file must remain <=20 KiB and approximately <=600 lines.
- Existing large files must not be refactored merely to reduce size.
- Do not push.

Before editing:

    test "$(git branch --show-current)" = "perf/image-decode-distributed-benchmark"
    git rev-parse HEAD

Record starting HEAD and scoped pre-existing changes in state.

## Progress

- [x] Bootstrap plan/state/evidence and commit them.
- [x] Milestone 1: add same-frame active-work metrics.
- [x] Milestone 2: add the focused two-round runner profile.
- [x] Close Plan 1 and hand off to Plan 2.

## Current Architecture and Scope

The current benchmark stores `frame_time_ns` as the interval between frame
starts. On warm runs this is intentionally paced near 16 ms, so a rendering
optimization can reduce CPU/render work without materially changing
`frame_time_ns`.

JPEG counters are already captured before and after the actual scroll+paint work,
but the paced frame metric is not the right denominator for same-row attribution.

The current distributed runner hardcodes:

- 21 masks;
- prefetch off/on;
- 3 rounds;
- 126 scroll matrix processes;
- a separate standalone decode matrix when `--phase full` is used.

This plan preserves that historical contract and adds a separate focused profile.

## Fixed Decisions

### A. Active-work timing

Keep `frame_time_ns` unchanged for compatibility.

For each measured frame add:

- `scroll_work_ns`: elapsed time spent in `scroll.scrollContent(...)`; zero when
  no movement is requested;
- `paint_work_ns`: elapsed time spent in `scroll.repaintNow()`;
- `work_time_ns`: elapsed time from immediately before `scrollContent` through
  the return from `repaintNow()`.

JPEG before/after snapshots must bracket exactly this `work_time_ns` region, so
JPEG deltas on a frame row refer to the same work.

Add aggregate P50/P95/P99/MAX values for both `work_time_ns` and
`paint_work_ns` to the run summary and distributed summary.

Validation invariants:

- every work metric is non-negative;
- `work_time_ns >= scroll_work_ns`;
- `work_time_ns >= paint_work_ns`;
- existing JPEG frame/aggregate invariants continue to pass.

Do not alter scroll duration, path, pacing, or prefetch behavior.

### B. Focused policy profile

Extend the runner with:

    --profile full
    --profile write-pixels-policy

`full` remains the default and must retain the existing matrix semantics:
21 masks × 2 prefetch profiles × 3 rounds = 126 processes.

`write-pixels-policy` uses exactly:

    masks = 0, 4, 2, 6, 32795, 32799
    prefetch = off, on
    rounds = 2

This is exactly 24 matrix processes.

The controlled pairs are:

- `0 -> 4`: add writePixels alone;
- `2 -> 6`: add writePixels with opacity metadata already enabled;
- `32795 -> 32799`: add writePixels to the current default composite mask.

The focused profile is scroll-only. Plan 2 will execute it with
`--phase matrix`; it must not execute the standalone decode matrix.

Do not modify the bundle manifest's historical full matrix contract. The profile
selection belongs to the runner and aggregation layer.

### C. Focused pairwise output

For the focused profile, produce a compact pairwise comparison artifact
containing per run:

- control mask;
- enabled mask;
- prefetch profile;
- `work` P50/P95;
- `paint` P50/P95;
- enabled-minus-control delta in ns and percent.

Do not attempt statistical extrapolation from two runs.

If the two rounds for one pair disagree in the direction of `work_time_p50`,
the later report must mark that pair `INCONCLUSIVE_VARIANCE`. Do not
automatically expand the matrix.

## Plan of Work

### Bootstrap checkpoint

Create plan/state/evidence files, record starting HEAD, validate new-file size,
headers, and staged whitespace, and commit only these artifacts.

Suggested commit:

    docs(benchmark): plan focused write pixels benchmark

No build or native smoke is allowed here.

### Milestone 1 — same-frame active-work measurement

Modify `ImageScrollRealWorkloadBenchmarkApp`:

- start the work timer immediately before the optional `scrollContent` call;
- measure `scroll_work_ns` around that call only;
- measure `paint_work_ns` around `repaintNow()`;
- end `work_time_ns` immediately after paint returns;
- place JPEG snapshots around that same work region;
- append the three work metrics to `frames.csv`;
- add aggregate work/paint percentiles to `summary.json`;
- retain existing paced frame metrics.

Update output validation for the new columns and invariants.

Suggested commit:

    perf(benchmark): measure active scroll work

Before commit run only focused static/header/whitespace checks.

Milestone 1 gate:

1. Run the smallest focused SDK benchmark/image tests.
2. Build SDK.
3. Do not build macOS native; production native behavior did not change.
4. Run any Java-side/parser fixture needed to prove the new schema.
5. Record concise evidence and commit state updates if needed.

### Milestone 2 — focused runner profile

Refactor only the runner's matrix-planning inputs so functions receive the
selected profile's masks/rounds instead of relying on the full globals.

Requirements:

- `--profile` defaults to `full`;
- full profile still plans exactly 126 processes;
- focused profile plans exactly 24;
- the focused profile uses the exact mask order declared above;
- keep existing deterministic seed/permutation behavior;
- aggregation validates against the active profile;
- focused aggregation writes the pairwise comparison artifact from Contract C;
- existing full `summary.csv` semantics remain compatible;
- add work/paint aggregate fields to distributed summary;
- update README with:
  - paced `frame_time_ns` meaning;
  - active `work_time_ns` meaning;
  - focused profile masks/rounds;
  - statement that focused profile is intended for writePixels policy work.

Suggested commit:

    perf(benchmark): add focused write pixels profile

Do not build between runner implementation and its focused parser tests.

Milestone 2 gate:

1. Run:

       bash -n scripts/package-image-scroll-benchmark.sh
       PYTHONDONTWRITEBYTECODE=1 python3 -m py_compile \
         scripts/run-image-scroll-distributed-benchmark.py

2. Run deterministic planning/parser validation proving:
   - full profile = 126 processes;
   - focused profile = 24 processes;
   - exactly two runs exist per focused `(mask,prefetch)`;
   - controlled pairs are complete;
   - new work fields parse and aggregate correctly.
3. Build SDK only if Java benchmark code changed since the Milestone 1 build.
4. Do not run the 24-process benchmark yet.
5. Commit state/evidence closeout.

## Surprises & Discoveries

Record only observations that alter remaining work.

Examples:

- paced frame time and active work time diverge significantly;
- a parser assumption depends on the historical fixed process count;
- focused profile requires a runner-local compatibility adjustment.

Do not change rendering policy in response to these observations.

## Decision Log

- Keep paced frame metrics for compatibility.
- Use active work time as the primary metric for the next warm-path comparison.
- Preserve the full 126-process benchmark unchanged.
- Use six masks and two rounds for the focused experiment.
- Do not auto-run a third round; contradictory rounds are reported as
  inconclusive.

## Validation and Acceptance

Before every logical commit:

    python3 scripts/validate-copyright-headers.sh --files <changed tracked files>
    git diff --check --cached

Run the logical-commits message checker before committing and verify the actual
commit afterward.

For every new tracked file:

    wc -c <file>
    wc -l <file>

Require <=20480 bytes and approximately <=600 lines.

Plan 1 completes only when:

- active work timing is same-frame with JPEG deltas;
- work/paint metrics appear in raw and aggregated output;
- full profile still plans 126 processes;
- focused profile plans 24 processes;
- no rendering/native optimization behavior changed;
- Plan 2 can consume the focused profile without further architecture choices.

## Risks and Open Questions

- Two rounds intentionally trade statistical depth for execution speed.
- Existing environment metadata inaccuracies are outside this plan.
- Memory/RSS remains outside this measurement change unless already available.
- Do not use paced `frame_time_ns` as the warm optimization verdict.

## Idempotence and Recovery

Use task-specific logs and temporary output directories.

Do not delete prior benchmark results or unrelated caches.

If validation fails, fix only the measurement/profile contract in a focused
follow-up commit. Do not amend history.

## Logical Commit Policy

Follow `.agents/skills/logical-commits/SKILL.md`.

Expected commits:

- `docs(benchmark): plan focused write pixels benchmark`
- `perf(benchmark): measure active scroll work`
- `perf(benchmark): add focused write pixels profile`

Add focused fix commits only when validation discovers a real defect.

## Outcomes & Retrospective

The active-work schema is delivered in commit `11042035e`, with the timer
alignment correction in `28c942ef7`. The runner and documentation are in
`b97e68583`; the bootstrap/state validation commit is `f16301dd9`.

`frames.csv`, `summary.json`, pass records, and distributed `summary.csv` now
carry work/paint timing. The deterministic fixture proved full planning at
126 processes, focused planning at 24, two runs per focused key, complete
controlled pairs, and pairwise aggregation with variance marking.

SDK smoke-source compilation and `dist -x test` passed at both milestone gates;
the focused 24-process benchmark and all native macOS validation remain
deferred to Plan 2. No rendering or native optimization policy changed.

The profile commit and closeout commit used literal `\\n` in their shell `-m`
arguments, so the local commit checker reported one overlong body line after
each commit. History was not rewritten; both deviations are recorded in state
and evidence.

The editorial report must use the section structure required by `.agent/PLANS.md`.

## Revision Note

Initial Plan 1: prepare same-frame active-work timing and a 24-process focused
profile for the device-space writePixels experiment.
