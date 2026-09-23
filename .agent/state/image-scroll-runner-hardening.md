<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image-scroll runner hardening state

## Active milestone

Checkpoint 3: non-fatal post-run validation.

## Last completed checkpoint

Checkpoint 1 is committed as `e639ee2c1`: results preflight, access probing,
and clean/resume/partial execution-state diagnostics.

Checkpoint 2 is implemented and staged for commit: dynamic physical-target
baseline capture, 540x960 logical validation, and physical fields in summaries.

## Active paths

- `scripts/run-image-scroll-distributed-benchmark.py`
- `scripts/test-image-scroll-distributed-benchmark.py`
- `.agent/exec-plan-image-scroll-runner-hardening.md`

## Next concrete action

Implement non-fatal post-run validation recording and continuation for regular
and raster-reuse processes. Keep execution, timeout, exit-code, and output
preservation failures fatal.

## Validation and evidence

Focused matrix/runner tests and Python compilation pass through checkpoint 2.
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
