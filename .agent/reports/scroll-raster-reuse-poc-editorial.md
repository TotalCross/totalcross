<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Scroll raster reuse POC editorial handoff

Status: `INVALIDATED — CORRECTION RERUN IN PROGRESS`

Classification: `INVALIDATED`

The previously recorded result set is not valid for the corrected measurement
contract. The old harness manually cleared repaint state, relied on diagnostic
hit counters in accounting-off mode, measured endpoint samples, and reported
pixel counts as bytes.

The correction implementation is at `5fcd2ab95`. Fresh M2 correctness and the
complete six-process M3 matrix are required before classification.

Existing compact result files must be replaced rather than mixed with fresh
samples. No SDL upload changes, default enablement, or follow-up optimization
is authorized by this plan.
