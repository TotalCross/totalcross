<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Diagnose the writePixels performance tail

This ExecPlan follows `AGENTS.md`, `.agent/PLANS.md`, and
`.agents/skills/logical-commits/SKILL.md`.

## Purpose / Big Picture

Close the remaining performance diagnosis for the real 663-image macOS
scroll workload. The result must explain, with fresh paired evidence, why
enabling `RASTER_OPAQUE_WRITE_PIXELS` improves common-case/P50 work while the
P95/P99/MAX tail is mixed. The diagnosis must compare the same workload with
the bit disabled and enabled, preserve all other optimization bits, and retain
the raw per-frame evidence needed to inspect each expensive frame.

This plan is diagnostic-first. It does not redesign `writePixels`, change
defaults, add GPU work, or start dirty-region/scroll-reuse work. A source or
instrumentation change is allowed only when it adds a narrowly scoped
measurement or corrects a demonstrated benchmark defect. The completion gate
is a factual outcome, not an optimization recommendation.

## Working Set and Resume Protocol

Read `.agent/state/image-writepixels-tail-diagnosis.md` first when resuming.
It records the active milestone, last logical commit, active paths, focused
validation, deferrals, and the next command. The append-only evidence index
`.agent/evidence/image-writepixels-tail-diagnosis.md` records compact command
results, raw-artifact paths, hashes when needed, and limitations. The final
factual handoff is
`.agent/reports/image-writepixels-tail-diagnosis-editorial.md`.

Reference context, not new evidence:

- `.agent/state/image-raster-policy-03-audit-and-reuse.md` records the M1–M4
  gates and confirms that M4 is complete at the branch base.
- `.agent/benchmarks/image-raster-policy-03/m1-write-pixels-accounting.md`
  records the earlier `32795` versus `32799` timing observation.
- `.agent/benchmarks/image-raster-policy-03/m4-reuse-rgb565-target-color.md`
  records the warm/reuse workload and target/backing context.
- `scripts/run-image-scroll-distributed-benchmark.py` is the existing process
  planner, runner, validator, and aggregator.
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageScrollRealWorkloadBenchmarkApp.java`
  owns the 663-image workload, nanosecond timing, pass lifecycle, and raw
  `frames.csv` output.

Do not modify completed M1–M4 evidence. Append only this plan's follow-up
records to the new evidence file. Raw result directories, ZIPs, full build
logs, caches, and runtime bundles remain outside Git. Compact summaries,
analysis scripts, plan/state/evidence/report files, and intentional generated
analysis artifacts for this plan are tracked and committed.

## Progress

- [x] (2026-09-21) Created `perf/writepixels-tail-diagnosis` from
  `perf/image-decode-distributed-benchmark`, preserving unrelated untracked
  local artifacts.
- [x] (2026-09-21) Read `AGENTS.md`, `.agent/PLANS.md`, and the repository
  `logical-commits` guidance; inspected the existing M1–M4 evidence and the
  current runner, workload, and native writePixels counters.
- [ ] Bootstrap this plan with a focused state/evidence commit and confirm the
  exact base revision and corpus/package prerequisites.
- [ ] Milestone 1: add only the missing bounded per-frame attribution and
  validate its schema and accounting invariants.
- [ ] Milestone 2: build/package macOS at the end of instrumentation work and
  collect fresh paired cold and warm/reuse samples.
- [ ] Milestone 3: persist outliers, analyze paired controls, and prepare the
  evidence-based outcome.
- [ ] STOP / REVIEW: freeze the diagnosis before any follow-up optimization is
  designed or implemented.

## Current Architecture and Scope

The benchmark runs `ImageScrollRealWorkloadBenchmarkApp` at logical
`540x960` over exactly 663 sorted JPEG-named corpus files. It records separate
`scroll_work_ns`, `paint_work_ns`, and `work_time_ns` values. `work_time_ns`
is the primary performance measure; `frame_time_ns` is the paced inter-frame
context. The app already emits one `frames.csv`, `summary.json`, and
`counters.json` per process/pass and has accounting-gated counters for JPEG
decode, image/native materialization, backing bytes by format, target-color and
physical variants, and writePixels attempts/hits/fallbacks/copied bytes.

The current per-frame row contains timing and JPEG deltas but not per-frame
writePixels, materialization/cache, backing-format, or control-identity data.
Aggregate counters therefore cannot establish whether an outlier paid for a
large copy, a clipped/full copy, materialization/decode, a cache miss, or
unrelated work. The native regular writePixels path already computes visible
source/destination rectangles and knows whether a hit was clipped; the native
accounting layer already distinguishes 32-bit and RGB565 backing bytes.

The measurement extension will be additive and accounting-gated:

- snapshot existing cumulative counters at measured segment boundaries;
- add a small native frame-scope writePixels record for copy width/height,
  copied bytes, hit/fallback counts, full versus clipped hits, and source
  backing format, without changing the existing global counters;
- emit scroll-segment and paint-segment deltas so an expensive frame can be
  attributed to scroll work, paint work, or both;
- emit a deterministic visible-control range derived from the existing sorted
  image order, scrollbar position, tile size, and viewport; use control index
  plus corpus-relative path hash rather than duplicating image bytes or paths;
- retain the existing aggregate schema and invariants, extending only the
  frame attribution schema and focused runner validation.

No production policy, default mask, target identity, delayed materialization,
shared raster-variant slot, or rendering result changes are in scope.

## Experimental Matrix

Use the smallest matrix that covers the required causal comparisons. The
writePixels A/B pair is always identical except for bit 2:

- 32-bit backing pair: disabled `32795`, enabled `32799`;
- RGB565 backing pair: disabled `32827`, enabled `32831` (the same pair with
  bit 5, `STORAGE_RGB565`, enabled in both cells).

The RGB565 pair is a stratified comparison, not a claim that the macOS target
surface is RGB565. The environment file must continue to report the physical
target and target color type separately from source backing format.

Run two focused profiles:

1. `write-pixels-tail-diagnostic`: the four masks × prefetch off/on ×
   accounting on × three interleaved rounds, one cold pass per process. This
   provides fresh P50/P95/P99/MAX timing and per-frame evidence for cold and
   prefetch paths.
2. `write-pixels-tail-reuse`: the same four masks × prefetch off/on ×
   accounting on × two interleaved rounds, with the existing three passes
   (`cold-forward`, `warm-reverse`, `warm-forward`). This tests whether an
   outlier belongs to first materialization or persists after reuse, and keeps
   prefetch-on and prefetch-off reuse comparable.

For timing overhead control, repeat only the 32-bit pair in accounting-off
mode with three cold rounds and one reuse round after the diagnostic schema is
validated. Accounting-off preserves timing while marking diagnostic fields
unavailable; it is not used to infer attribution. Do not rerun broad
historical profiles. Interleave masks and rounds using the runner's existing
deterministic planner and keep the same corpus hash, resolution, runtime, and
process timeout for every cell.

## Plan of Work

### Milestone 0 — bootstrap and plan checkpoint

Record the new branch, parent/base SHA, current HEAD, corpus path/hash if
available, and the exact existing macOS package/runtime that will be rebuilt.
Verify that the M1–M4 state records the validated 663-file corpus and that no
tracked implementation changes are present on the new branch. Run only
focused plan/header/whitespace checks. Commit the plan, state, and evidence
bootstrap as one documentation slice.

PASS means the branch ancestry, corpus prerequisite, raw-output policy, and
active paths are explicit. FAIL means stop before source edits and report the
missing corpus or ancestry problem.

### Milestone 1 — bounded per-frame attribution

Extend the existing benchmark app and native test bridge only enough to
capture the following for each frame and for its scroll and paint segments:

- writePixels attempts, hits, fallbacks, copied bytes, regular-path counts,
  full versus clipped hit counts/bytes, and the last/max copy width/height
  evidence needed to identify large transfers;
- JPEG decode count/ns by bucket, image materializations, native-geometry
  materializations, and existing physical/target variant lookup, hit, miss,
  fallback, materialization, eviction, and byte deltas;
- current and peak backing bytes, plus 32-bit/RGB565/other backing-byte
  deltas from the existing format counters;
- target-color and physical-variant activity, including attempts, hits,
  fallbacks/misses, materializations, bytes, and the existing policy rejection
  fields at pass scope;
- deterministic scroll position, direction, visible control index range, and
  bounded corpus-relative identity/hash fields;
- the existing `scroll_work_ns`, `paint_work_ns`, and `work_time_ns` values.

Prefer one compact frame-metric snapshot/delta structure and the existing
counter APIs. Do not add duplicate global counters. If a new native counter is
needed for copy extents or full/clipped bytes, reset only frame-scope fields at
the segment boundary and leave pass/global accounting untouched. Keep all
diagnostic writes behind accounting-on as current instrumentation does.

Extend `frames.csv` and runner validation with stable field names, nonnegative
numeric checks, segment-sum checks, and an explicit unavailable marker for
accounting-off. Preserve the existing JPEG sum and writePixels aggregate
invariants. Add a focused fixture for frame-delta aggregation and alignment by
`mask/prefetch/accounting/run/pass/direction/frame_index/scroll_value`.

PASS means source compilation/schema fixtures prove that frame sums reconcile
to pass counters, disabled masks have zero writePixels activity, RGB565 and
32-bit format fields are distinguishable, and accounting-off does not update
diagnostic counters. FAIL means no package or long benchmark is started.

### Milestone 2 — macOS package and paired samples

At the end of the related instrumentation milestone, build only the SDK and
macOS ARM64 native targets, package the existing benchmark bundle, and run the
small native/package smoke gate. Do not run Android, iOS, Windows, Linux, or
the full historical matrix. Use a task-specific output directory and save
verbose build output outside Git.

Run both focused profiles from the bundle. The runner must persist every raw
process/pass `frames.csv`, `summary.json`, `counters.json`, `environment.json`,
and process log. The runner must also write a compact per-cell outlier index
containing the top work-time frames for P95/P99/MAX, their matching control
frame keys, and absolute paths to the raw files. Raw files stay outside Git;
the index and final report record their paths and hashes.

PASS means all planned cells reach the scroll endpoint, have valid frame
trajectories, and preserve the A/B mask pair, corpus hash, target metrics, and
accounting invariants. FAIL means preserve partial results, record the exact
failed process/phase, retry only a clearly transient environmental failure,
and stop without changing policy.

### Milestone 3 — paired outlier analysis and final evidence

Add or use a focused analyzer that reads the raw frames and produces compact
tracked artifacts under `.agent/benchmarks/image-writepixels-tail-diagnosis/`:

- `summary.md` with package/corpus/runtime hashes, cell counts, percentile
  tables, copy-size scaling, and the evidence-based outcome;
- `outliers.csv` with each selected P95/P99/MAX frame, its configuration,
  direction/pass/position/control identity, work/scroll/paint timings, the
  corresponding writePixels and pipeline deltas, and the paired disabled
  frame values;
- `pairwise.csv` with enabled-minus-disabled deltas for matched cold and warm
  frames and per-cell P50/P95/P99/MAX summaries.

Rank outliers from `work_time_ns` and retain all ties needed for P95/P99/MAX;
also report paint and scroll components. Match enabled frames to equivalent
disabled frames using the same planned cell identity, pass/direction, frame
index, and scroll value, allowing a documented nearest-position match only
when pacing produces a different frame count. Never call a timing-only match
causal.

For every selected outlier, answer whether the frame contains:

- writePixels attempts/hits/fallbacks and whether copy bytes/width/height are
  unusually large;
- full versus clipped copies and the fraction of work accounted for by the
  copy;
- image/native materialization, JPEG decode, or cache miss activity;
- 32-bit versus RGB565 backing bytes and target color/physical-variant work;
- a useful control/image identity and scroll versus paint dominance;
- the same activity and cost in the disabled equivalent frame.

Compute correlations and grouped summaries for copied pixels/bytes versus
`work_time_ns`, but report them as association. A large-copy explanation is
accepted only if paired disabled frames and per-frame copy evidence support
it. Otherwise classify the cost as pre-eligibility work, materialization/decode
or cache miss, unrelated same-frame work, or insufficient evidence.

PASS means the compact artifacts retain inspectable outlier rows and every
claim cites matched raw fields. FAIL means state the exact missing measurement
and end with outcome 3 rather than recommending a fix.

### STOP / REVIEW gate

Freeze source and policy changes after the paired analysis. The final report
must end with exactly one evidence-based outcome:

1. a specific localized writePixels tail-cost cause was identified and is
   suitable for a follow-up fix;
2. writePixels is behaving as an opportunistic fast path and expensive frames
   are dominated by other pipeline work;
3. evidence remains insufficient, naming the exact missing measurement.

No follow-up optimization, default change, GPU work, dirty-region work, or
scroll-reuse design may begin before this gate is reviewed. If a benchmark or
instrumentation defect was corrected, include before/after measurements from
the same workload/configuration and still report P95/P99/MAX plus outlier
attribution.

## Decision Log

- Decision: use `32795/32799` as the primary A/B pair and `32827/32831` as a
  source-backing stratum.
  Rationale: each pair differs only by writePixels, while the second pair
  keeps RGB565 enabled in both cells and tests the format-specific path.
  Date: 2026-09-21.
- Decision: keep accounting-on for attribution and add a narrow accounting-off
  timing confirmation only after schema validation.
  Rationale: existing diagnostics are accounting-gated; comparing within one
  accounting mode avoids mixing instrumentation overhead with policy cost.
  Date: 2026-09-21.
- Decision: use `work_time_ns` as the primary timing and `scroll_work_ns` /
  `paint_work_ns` as attribution context.
  Rationale: the existing plan identifies frame pacing as context, while the
  requested diagnosis is about work performed by the pipeline.
  Date: 2026-09-21.
- Decision: do not reinterpret existing M1–M4 timing as fresh proof.
  Rationale: those artifacts establish the question and validated environment;
  new performance conclusions require fresh samples with per-frame evidence.
  Date: 2026-09-21.

## Validation and Acceptance

Validation follows the smallest sufficient level at each milestone:

- bootstrap: `git diff --check`, focused header validation, plan/state size
  checks, and commit-message validation;
- instrumentation: Java/Python syntax/fixture checks, focused native compile
  or test as applicable, and `git diff --check`;
- package gate: SDK and macOS ARM64 build/package only, benchmark self-test,
  native smoke, and the four relevant mask/prefetch smoke combinations;
- matrix gate: focused profiles, runner invariant validation, per-frame sum
  checks, and analyzer fixture plus raw-result analysis;
- closeout: final `git diff --check`, focused headers, staged-diff inspection,
  new-file size checks, and validated signed Conventional Commits.

Use `python3 scripts/validate-copyright-headers.sh --files <changed files>`
for every changed first-party file before staging. Before every commit run
`git diff --check --cached`, inspect the staged diff, and run the
`logical-commits` message checker. Do not push, amend, rebase, squash, or
rewrite history.

The final acceptance requires: fresh paired enabled/disabled samples; cold and
warm/reuse coverage; raw per-frame outlier retention; all requested pipeline,
format, identity, and timing fields; an explicit copied-byte scaling result;
and one of the three STOP / REVIEW outcomes. Full historical matrices and
non-macOS builds remain deferred because they are outside the requested
diagnosis.

## Risks and Open Questions

- The native bridge may not expose copy extents without a small frame-scope
  counter. If it cannot do so without changing the measured path, report the
  exact limitation and use outcome 3.
- RGB565 source backing may be absent or may fall back before writePixels. The
  matrix must report that fact rather than treating zero RGB565 hits as a
  failure.
- Pacing can produce different frame counts between A/B processes. The
  analyzer must preserve exact matches first and document any nearest-position
  match; unmatched tail frames remain evidence gaps.
- Accounting-on adds measurement work. Accounting-off confirmation is required
  before attributing a small timing delta to the instrumentation itself.
- The existing control range is deterministic but may over-approximate actual
  visible controls. Report it as workload identity context, not as a proof of
  individual draw participation.

## Idempotence and Recovery

All builds and benchmark outputs use task-specific paths under `build/` or
`/tmp`; never delete prior results, caches, dependency checkouts, or unrelated
untracked files. A rerun uses a new output directory and a new raw-result
hash/index record. The packager and runner are expected to be repeatable for
the same SDK/runtime/corpus and deterministic planner seed.

If instrumentation validation fails, keep the failed source changes only if
they are a focused diagnostic correction, record the failure in state/evidence,
and create a new logical fix commit after revalidation. If a matrix process
fails, preserve partial output, retry once only for a transient environmental
failure, and do not change optimization policy. Resume from the state file,
not from raw logs.

## Outcomes & Retrospective

This section remains intentionally open until Milestone 3. At completion it
will summarize the accepted measurement changes, fresh matrix coverage, raw
artifact locations, paired outlier findings, limitations, and the selected
STOP / REVIEW outcome. It must distinguish direct measurements from
interpretation and must not turn a P50 improvement into a promotion claim.

## Revision Note

Initial plan created on 2026-09-21 from the requested objective. It narrows
the work to paired writePixels tail attribution and explicitly carries the
RGB565 source-backing stratum, cold/warm reuse coverage, segment timing, raw
outlier retention, and final STOP / REVIEW gate.
