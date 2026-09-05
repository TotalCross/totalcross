<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization phase 1 history

This append-only file records completed milestone summaries and stable evidence
references for `.agent/plans/exec-plan-image-opt-phase1-controls.md`.

## Bootstrap

The phase branch was created from the authored master SHA. The detailed state
and initial evidence record are in:

- `.agent/state/image-opt-phase1-controls.md`
- `.agent/evidence/image-opt-phase1-controls.jsonl`

## Milestone 1 — benchmark harness and S1

The deterministic control workload and reusable RSS runner were added and
deployed through the existing macOS smoke registration. The exact pre-settings
baseline is commit `d00d7c1dfec4bddc14bdfbb8293b30dfe8b3a3c6`; compact samples and
machine metadata are in
`.agent/benchmarks/image-opt-phase1-controls/control-plumbing/`.
