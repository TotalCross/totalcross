<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Scroll raster reuse POC editorial handoff

Status: `INVALIDATED — TIMER-GATING M3 RERUN IN PROGRESS`

Classification: `INVALIDATED`

The previous M3 result set is invalidated because the diagnostic timer reads
were still executed when diagnostics were disabled. The current source
revision is `f7e662508`, based on
`perf/writepixels-tail-diagnosis@c6cc3bcbf9e28ead3edabb69c12e5c31926a55d0`.
The timer-only change preserves rendering, counters, eligibility, repaint
semantics, local hit metrics, and screen-update benchmark timing.

M2 correctness remains retained and is not rerun. Exactly three OFF and three
ON M3 processes, with accounting and rendering diagnostics disabled, are
required before replacing the final distributions and classification.

The current compact evidence is in
`.agent/benchmarks/scroll-raster-reuse-poc/final/`. No SDL upload changes,
default enablement, or follow-up optimization is authorized by this plan.
