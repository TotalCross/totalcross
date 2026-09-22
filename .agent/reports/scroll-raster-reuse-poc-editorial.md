<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Scroll raster reuse POC editorial handoff

Status: `STOP / REVIEW`

Classification: `PRESENTATION-BOUND`

The implementation is correct on the fixed macOS software-raster workload.
Three OFF and three ON accounting-OFF processes completed two passes each.
Every cold/warm waypoint hash matched across modes and remained stable across
rounds. ON had 71 hits from 71 attempts per pass, no fallbacks, and no
post-move recoveries. OFF had no hits.

The ON path reduced row and image paint calls by about 67% and reduced median
measured work by 34% in both passes. Full-frame presentation remained active;
screen-update P50 stayed near 1.5 ms and did not improve with the optimization.
The paced pass totals were effectively unchanged cold and slightly worse warm,
so this result supports a later presentation study without authorizing one in
this plan.

Evidence and compact result files are committed under
`.agent/benchmarks/scroll-raster-reuse-poc/final/`. No SDL upload changes,
default enablement, or follow-up optimization were made.
