<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image-scroll runner hardening state

## Active milestone

Checkpoint 4: aggregation/final-status handling and tests.

## Last completed checkpoint

Checkpoint 1 is committed as `e639ee2c1`: results preflight, access probing,
and clean/resume/partial execution-state diagnostics.

Checkpoint 2 is committed as `319c59f8c`: dynamic physical-target baseline
capture, 540x960 logical validation, and physical fields in summaries.

Checkpoint 3 is implemented and staged for commit: non-fatal validation
recording/continuation for regular processes with fatal execution failures.

## Active paths

- `scripts/run-image-scroll-distributed-benchmark.py`
- `scripts/test-image-scroll-distributed-benchmark.py`
- `.agent/exec-plan-image-scroll-runner-hardening.md`

## Next concrete action

Make regular/reuse aggregation tolerate invalid planned runs, mark incomplete
pairwise comparisons, and write final PASS/PASS_WITH_VALIDATION_FAILURES/
INCOMPLETE summaries. Add focused aggregation and status tests.

## Validation and evidence

Focused matrix/runner tests and Python compilation pass through checkpoint 3.
Full benchmark and native builds are explicitly deferred by the user.

## Decisions still active

- Logical target remains 540x960; physical target is captured and compared at
  runtime without a DPI assumption.
- Post-exit validation failures are non-fatal; execution failures are fatal.
- Existing results directories without the new state marker are invalid rather
  than successful resumes.

## Deliberate out-of-scope files

Generated package ZIPs, existing benchmark result directories, native build
outputs, and unrelated untracked files remain untouched.

## Resume command

`sed -n '1,220p' .agent/state/image-scroll-runner-hardening.md`
