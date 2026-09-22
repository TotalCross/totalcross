<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Scroll raster reuse POC editorial handoff

Status: `STOP / REVIEW`

Classification: `PRESENTATION-BOUND`

The previous result set remains invalidated. The corrected source/results
revision is `d9930c603`. The M2 OFF/ON correctness pair and the complete six
process M3 matrix passed the corrected measurement contract: repaint state is
preserved, endpoint rows are excluded from timing distributions, moved bytes
use the target's four-byte BGRA8888 format, direct visible `bag0` overlays
fall back, and accounting/diagnostics are genuinely disabled in M3.

ON achieved 71/71 local hits per measured pass with zero fallback or recovery;
OFF achieved zero hits. Average row/image paints fell from `6.000/18.000` to
`1.775/5.324` cold and from `6.014/18.042` to `1.784/5.352` warm. Measured
work P50 improved 33.8% cold and 35.2% warm, while screen-update P50 stayed
near 1.5 ms and paced pass totals stayed effectively flat. Full-frame
presentation therefore limits the end-to-end gain.

The current compact evidence is in
`.agent/benchmarks/scroll-raster-reuse-poc/final/`. No SDL upload changes,
default enablement, or follow-up optimization is authorized by this plan.
