<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Harden the distributed image-scroll runner

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`.

## Purpose / Big Picture

Make `scripts/run-image-scroll-distributed-benchmark.py` safe on Windows and
resilient after a process exits successfully with invalid benchmark artifacts.
The benchmark command line, corpus, process matrix, and measurement fields stay
unchanged. The runner will instead validate results access before launch,
capture the first successful physical target as a same-machine baseline, mark
post-run validation failures without stopping the suite, and emit explicit
final status and diagnostic artifacts.

## Working Set and Resume Protocol

The primary implementation is the distributed runner and its focused test:

- `scripts/run-image-scroll-distributed-benchmark.py` — preflight, execution,
  physical-target baseline, failure recording, aggregation, and final status.
- `scripts/test-image-scroll-distributed-benchmark.py` — deterministic tests
  using temporary bundle/results fixtures and fake process outcomes.

This plan is the design record. The active state is
`.agent/state/image-scroll-runner-hardening.md`; read that state first when
resuming. Focused validation logs remain under `/tmp` unless a command needs a
stable artifact path. Do not inspect or modify benchmark result directories
outside the temporary fixtures created by the focused tests.

## Progress

- [x] Read repository, ExecPlan, and logical-commit instructions.
- [x] Add results preflight and state diagnostics; commit checkpoint 1.
- [x] Add dynamic physical-target baseline and summary fields; commit checkpoint 2.
- [ ] Make post-run validation failures non-fatal and record exact errors;
  commit checkpoint 3.
- [ ] Make aggregation/final status resilient; add focused tests and commit
  checkpoint 4.
- [ ] Run final focused validation, diff checks, and stop at `STOP / REVIEW`.

## Current Architecture and Scope

The runner currently validates a bundle and writes `results/self-test.json`,
then launches processes. It assumes a fixed 1080x1920 Skia surface, treats any
post-exit artifact validation error as fatal, and aggregates only all-valid
summaries. The benchmark application already writes logical dimensions and
physical target fields in `environment.json`, so this change is runner-side.

The existing successful-run checks in `validate_run_artifacts` and
`validate_scroll_reuse_artifacts` remain authoritative. New code wraps those
checks, supplies a persisted physical-target baseline, and classifies errors;
it does not relax counters, hashes, timing, accounting, or reuse invariants.

## Plan of Work

### Checkpoint 1 — preflight and results state

Add a preflight path that validates the bundle before any process launch,
creates or verifies `results`, and performs a create/write/read/delete probe
inside it. Report the path, operation, errno, and Windows error code for access
failures. Persist an execution state marker that distinguishes a clean start,
valid self-test resume, active/partial execution, and invalid previous state.
Reject an existing partial or malformed results directory instead of treating
it as a successful self-test.

### Checkpoint 2 — physical target baseline

Replace the fixed 1080x1920 constants with a physical-target record assembled
from the first successful process environment. Persist it as
`results/physical-target-baseline.json`, including logical 540x960 dimensions,
physical width/height, row bytes, pixel bytes, color/alpha types, color
classification, and renderer backend. Require later processes to match the
baseline exactly while keeping the logical resolution requirement. Add the
logical and physical target records to final summaries and aggregate rows.

### Checkpoint 3 — non-fatal post-run validation

Separate process execution failures from artifact validation failures. A zero
exit code followed by a validation error writes a JSONL record under
`results/validation-failures.jsonl`, prints a concise warning, preserves the
process artifacts, returns `VALIDATION_FAILED`, and continues. Launch errors,
timeouts, non-zero exits, invalid bundles, and results preservation failures
remain fatal. Apply the same behavior to normal and raster-reuse profiles.

### Checkpoint 4 — aggregation, status, and tests

Teach regular and reuse aggregation to include invalid planned runs as explicit
`VALIDATION_FAILED` rows and mark missing pairwise comparisons as incomplete
instead of indexing missing valid samples. Write final status as `PASS`,
`PASS_WITH_VALIDATION_FAILURES`, or `INCOMPLETE`, with planned/completed/
validation-failed counts and failure records. Extend focused tests for 1x/2x
targets, target changes, clean/resume/partial results, continuation after a
validation failure, invalid aggregation, and fatal execution stopping.

## Decision Log

- Decision: Keep `540x960` as the only logical-resolution requirement and use
  exact physical-target equality for one execution.
  Rationale: Windows DPI and display scaling are host-dependent; a fixed
  physical size would reject valid 1x and 2x machines or mix incomparable runs.

- Decision: Record post-exit artifact failures as JSONL records rather than
  deleting or rewriting their output directories.
  Rationale: JSONL is append-safe during continuation and preserves exact
  per-process errors and paths for later audit.

- Decision: Use `INCOMPLETE` for fatal execution termination and reserve
  `PASS_WITH_VALIDATION_FAILURES` for suites that launched all planned processes
  but contain invalid post-run artifacts.
  Rationale: Consumers can distinguish an unfinished suite from a complete but
  partially unusable measurement set.

## Validation and Acceptance

Validation is Level 2 focused functional validation. Run after each checkpoint:

- `python3 scripts/test-image-scroll-distributed-benchmark.py`
- `python3 -m py_compile scripts/run-image-scroll-distributed-benchmark.py scripts/test-image-scroll-distributed-benchmark.py`
- `git diff --check`
- focused copyright-header validation for changed first-party files
- staged diff checks and the logical-commit message checker before each signed
  commit.

At completion, also run `bash -n scripts/package-image-scroll-benchmark.sh`
when package-script paths remain unchanged, and run the final focused test
suite. Do not run the 50-process benchmark, rebuild native targets, or push.

## Risks and Open Questions

- Existing results directories produced by older runner versions have no new
  state marker and will be rejected as partial/invalid; this is intentional and
  prevents false self-test resumes.
- A process that exits zero but lacks enough physical-target fields cannot
  establish the baseline; it is recorded as a validation failure and later
  processes may establish the baseline only if they provide a complete target
  record. The final status remains non-pass.
- Pairwise comparisons with one invalid side cannot produce deltas; they must
  remain explicitly incomplete and excluded from valid comparisons.

## Idempotence and Recovery

The runner writes state and validation records only under the bundle's
`results` directory. The preflight probe uses a unique temporary filename and
deletes it after a successful read. It never deletes an existing results
directory or benchmark artifacts. A fatal or interrupted run leaves a state
marker that the next invocation reports as partial rather than silently
resuming.

## Outcomes & Retrospective

Pending implementation. Final outcomes will record the four logical commits,
focused test results, deferred full-benchmark/native validation, and any
platform limitations in the final response.

## Revision Note

Created for the Windows-hardening and resilient post-run validation work.
