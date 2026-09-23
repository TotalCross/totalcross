<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image-scroll runner hardening state

## Active milestone

STOP / REVIEW after the runner-regression closeout.

## Last completed checkpoint

Checkpoint 1 is committed as `e639ee2c1`: results preflight, access probing,
and clean/resume/partial execution-state diagnostics.

Checkpoint 2 is committed as `319c59f8c`: dynamic physical-target baseline
capture, 540x960 logical validation, and physical fields in summaries.

Checkpoint 3 is committed as `9d27d71f8`: non-fatal validation recording and
continuation for regular processes with fatal execution failures.

Checkpoint 4 is committed as `87d94eafc`: invalid aggregate rows, incomplete
pairwise status, final execution status/counts, and focused tests.

A follow-up fatal-diagnostics fix is committed as `9d95da261`: fatal
launch/timeout/exit failures are recorded with `fatal: true` before immediate
termination.

The clean-full, comparison-continuation, and final-console regressions are
committed as `248c6667f`, `4b2a937f5`, and `fe185aca8`, respectively.

## Active paths

- `scripts/run-image-scroll-distributed-benchmark.py`
- `scripts/test-image-scroll-distributed-benchmark.py`
- `.agent/exec-plan-image-scroll-runner-hardening.md`

## Next concrete action

Report the implementation commits plus this plan closeout, focused test
results, deferred full benchmark/native validation, and the no-push status.

## Validation and evidence

Focused matrix/runner, aggregation, continuation, and Python compilation tests
pass through the regression closeout. The package script syntax check and
copyright-header validation pass. GPG signatures are required and verified for
the three continuation commits. Full benchmark and native builds are
explicitly deferred by the user.

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
