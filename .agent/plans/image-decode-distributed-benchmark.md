<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Add distributed image decode measurements

This ExecPlan follows `AGENTS.md`, `.agent/PLANS.md`, and
`.agents/skills/logical-commits/SKILL.md`.

## Purpose / Big Picture

Extend the existing distributed image-scroll bundle with six corpus variants,
six SDK-format library TCZs, and a fresh-process image decode suite. Running the
same `run-benchmark.py --phase full` must finish the unchanged 126-process
scroll suite and its aggregation before the 90-process decode suite, then create
one ZIP containing both result sets.

## Working Set and Resume Protocol

Read `.agent/state/image-decode-distributed-benchmark.md` first when resuming;
it names the active slice, last commit, validation, and next action.
`.agent/evidence/image-decode-distributed-benchmark.md` stores compact command
results and artifact paths. This plan records stable design choices and
acceptance behavior; update it at functional-family checkpoints.

Active paths are `scripts/package-image-scroll-benchmark.sh`,
`scripts/run-image-scroll-distributed-benchmark.py`, a separate decode runner
and aggregator under `scripts/`, and test-only Java sources under
`TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/`. Documentation belongs
with the existing image-scroll benchmark docs. Do not edit decoder/VM or
production image behavior.

## Progress

- [x] (2026-09-15T19:13:00Z) Created the feature branch from the requested base,
  read the commit skill and plan policy, inspected the current package/runner,
  verified the corpus name sets, and found the requested Actions artifact.
- [x] (2026-09-15T19:18:00Z) Commit `37cfd87d7` packages the six selected
  image sets and deterministic SDK library TCZs; bundle self-test and all four
  existing scroll smokes passed on macOS ARM64.
- [x] (2026-09-15T19:25:00Z) Commit `d2a2626f1` adds the test-only full/half
  app and its deployed macOS executable/TCZ; it compiles against the Actions
  artifact SDK and passes package self-test.
- [x] (2026-09-15T19:35:00Z) Commit `b6525af5d` adds the deterministic plan,
  per-image validation, process logs/CSVs, required smokes, and progress estimates.
  Planner fixture passed with 90 unique jobs and three shared random orders.
- [x] (2026-09-15T19:40:00Z) Commit `91ef824aa` writes the combined image CSV,
  per-process summaries, and 30 scenario rows. A synthetic 59,670-row fixture
  verified size, percentile, and speedup aggregation.
- [x] (2026-09-15) Commit `56427147b` copies both decode helpers into each
  bundle and runs the 90-process decode suite after scroll aggregation and
  before one combined ZIP. A packaged macOS artifact passed four scroll smokes,
  five decode smokes, both self-tests, and a stubbed phase-order assertion.
- [x] Document bundle creation, full/smoke phases, and result files in
  `scripts/README-image-benchmarks.md`; final focused checks passed without
  running either full matrix. This documentation and evidence form commit six.

## Current Architecture and Scope

The current packager compiles `ImageScrollRealWorkloadBenchmarkApp` against the
SDK from an input ZIP, deploys it with `tc.Deploy`, copies a single
`corpus/` tree, and emits a root manifest. The scroll runner validates that
tree, executes its 126 processes, aggregates, and writes the final ZIP. Keep
the scroll app's workload, masks, rounds, prefetch profiles, and CSV schema
unchanged; point it at `corpus/imag` and validate/hash only that 663-file set.

The local source root has 663 `.jpg` paths in each of `imag`, `lossless`,
`decode-baseline`, `decode-fast`, `aggressive-480`, and `aggressive-540`, with
identical relative names. It also has four `.png` side images per folder and a
CSV in `lossless`; these are outside the 663-entry benchmark set. The 663
`.jpg` entries include files whose content is PNG, so record format by content
signature. Map local `aggressive-*` directory names to the requested stable
bundle variant IDs `aggresive-*`.

Build resources through `totalcross.util.zip.TCZ` from the packaged SDK JAR,
with `TCZ.ATTR_LIBRARY` and lexically ordered `decode/<variant>/<path>` names.
Files ending `Lib.tcz` are autoloadable and make `Vm.getFile` the only TCZ
acquisition path. Both acquisition modes pass the resulting bytes to the same
`Image(byte[], length)` constructor. The decode app lives in `totalcross.ui.image`
so it can use package-private `resolveForDrawing(1)` without `getPixels()`.

The specified package artifact is Actions run `34912920763`; use its
`TotalCross-7.2.2.zip` when compiling and deploying the macOS ARM64 bundle.

## Plan of Work

1. Extend packaging to validate all six 663-name sets before deployment,
   copy only selected workload files to six `corpus/<variant>` folders, build
   six SDK library TCZs, and include the decode app executable and manifest
   metadata. Checkpoint: package self-test proves paths, counts, TCZ entries,
   and deterministic entry names.
2. Add the test-only decode app. It reads a shared order file, records one CSV
   row per image, uses `System.nanoTime()` longs for acquire/decode/total, and
   materializes full or half-size images through `resolveForDrawing(1)`.
   Checkpoint: compile against the artifact SDK and smoke both byte-acquisition
   paths through the native runtime.
3. Add deterministic process planning for the five official source/order/mode
   scenarios × six variants × three rounds. A process handles all 663 images;
   no per-image processes. Print progress and estimate mean process seconds and
   remaining seconds after initial samples. Checkpoint: plan has 90 unique rows,
   three per scenario, and identical per-round random orders across variants.
4. Aggregate per-image CSVs into process summaries and `decode-summary.csv`,
   retaining ns through percentile/median/sum calculations and computing
   encoded size, baseline deltas, throughput, and speedups. Checkpoint: focused
   aggregation fixture produces all fields and compares each scenario to
   `imag`.
5. Integrate the helper into the existing full runner after scroll aggregation
   and before one final combined ZIP. Keep existing standalone phases intact;
   decode development phases may be separate helper CLI options. Checkpoint:
   orchestration inspection/test confirms order and one archive boundary.
6. Update workflow documentation and finish focused validation. Do not run the
   complete 126-process scroll or 90-process decode matrix.

## Decision Log

- Decision: the measured dataset is the 663 `.jpg`-named path set used by the
  existing scroll suite; four `.png` siblings and one CSV are excluded.
  Rationale: the official workload count and existing scroll input are both
  663, and every variant has the same 663 `.jpg` names. Detect bytes' actual
  format per row because suffix is not authoritative.
- Decision: retain the requested misspelled bundle IDs `aggresive-480` and
  `aggresive-540` while mapping from source directory names `aggressive-*`.
  Rationale: those IDs define resource paths and file names in the requested
  bundle contract.
- Decision: create TCZs with the SDK's public TCZ API and the library attribute;
  do not add a TCZ encoder or production runtime helpers.
- Decision: smoke only the five requested decode combinations and necessary
  scroll smokes; never run either official complete matrix during development.

## Validation and Acceptance

For each commit, use the focused checks that prove that slice, validate headers
per the local skill, stage only intended files, and run
`git diff --check --cached`. At final acceptance run the user-specified
`git diff --check`, shell syntax check, and Python compilation check.

Prove: six 663-file selected folders and identical paths; six `*Lib.tcz` files
with deterministic `decode/<variant>/...` resources readable via `Vm.getFile`;
scroll uses only `corpus/imag`; the dry-run plan has 90 processes, three runs
per scenario, and shared deterministic random order; Java timing stays in ns;
and `decode-summary.csv` includes size and `imag` comparisons. Run package
self-test, the existing scroll smokes needed for the path change, and at least
the five requested decode smokes. Do not run full scroll or decode matrices.

## Risks and Open Questions

- Confirm the artifact's SDK JAR and macOS ARM64 deployment layout before
  relying on the SDK TCZ writer from a host JVM.
- Confirm autoloaded `*Lib.tcz` resources can be retrieved by `Vm.getFile` in
  the deployed macOS app; the native smoke is the acceptance test.
- Native app time for each 663-image smoke can approach the existing timeout;
  preserve diagnostic logs and only tune the timeout if measured evidence needs
  it.

## Idempotence and Recovery

Build bundles only under a task-specific `/tmp` directory. The packager may
replace only its output bundle directory and must validate source structure
before doing so. Preserve corpus files, downloaded Actions artifact,
uncommitted local files, and unrelated benchmark outputs. Retrying a build
should reproduce TCZ entry order, plan files, random permutations, and output
schemas. Use the state file for the next safe command.

## Outcomes & Retrospective

Delivered six 663-image variants, six SDK library TCZs, the separate full/half
decode app, a deterministic 90-process matrix, and size/timing aggregation.
`run-benchmark.py --phase full` now runs the existing scroll suite, then decode,
and writes one combined ZIP. Bundle usage and result locations are documented in
`scripts/README-image-benchmarks.md`. Native package self-tests, four scroll
smokes, five decode smokes, and orchestration-order validation passed. The full
126-process scroll and 90-process decode matrices were intentionally deferred;
see `.agent/evidence/image-decode-distributed-benchmark.md` for artifacts and
commit history.
