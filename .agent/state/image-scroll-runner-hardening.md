<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image-scroll runner hardening state

## Active milestone

Checkpoint 1: preflight/results-state diagnostics.

## Last completed checkpoint

None. Branch starts at `d01bcb180628370cbdde7c14f9b836aa840d0a7d` with the
50-process matrix and package metadata already committed.

## Active paths

- `scripts/run-image-scroll-distributed-benchmark.py`
- `scripts/test-image-scroll-distributed-benchmark.py`
- `.agent/exec-plan-image-scroll-runner-hardening.md`

## Next concrete action

Implement preflight bundle/results validation, the create/read/write/delete
probe, and execution-state classification. Add tests for clean, valid-resume,
partial, and access-failure results states.

## Validation and evidence

No new validation has run for this plan yet. Full benchmark and native builds
are explicitly deferred by the user.

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
