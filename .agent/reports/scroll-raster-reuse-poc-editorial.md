<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Scroll raster reuse POC editorial handoff

Status: `STOP / REVIEW`

Classification: `PRESENTATION-BOUND`

The previous M3 result set is invalidated because the diagnostic timer reads
were still executed when diagnostics were disabled. The current source
revision is `f7e662508`, based on
`perf/writepixels-tail-diagnosis@c6cc3bcbf9e28ead3edabb69c12e5c31926a55d0`.
The timer-only change preserves rendering, counters, eligibility, repaint
semantics, local hit metrics, and screen-update benchmark timing.

M2 correctness remains retained and was not rerun. The final timer-gating M3
matrix completed exactly three OFF and three ON processes with accounting and
rendering diagnostics disabled. All waypoint hashes matched; ON achieved
71/71 local hits per pass with zero fallback or recovery, and all diagnostic
decision/move/dirty timer deltas were zero.

Median work improved 30.2% cold and 33.9% warm, with 70.38% and 70.60% row /
image paint reduction. However, ON work P95/P99/MAX materially regressed due
to screen-update tails. The no-tail-regression condition for `PROMISING` is
therefore not met; unchanged full-frame presentation is the next bottleneck,
so the strict existing classification is `PRESENTATION-BOUND`. Fixed-duration
pass totals were not used.

The current compact evidence is in
`.agent/benchmarks/scroll-raster-reuse-poc/final/`. No SDL upload changes,
default enablement, or follow-up optimization is authorized by this plan.
