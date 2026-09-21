<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Evidence: writePixels tail diagnosis

- 2026-09-21: created branch `perf/writepixels-tail-diagnosis` from the
  checked-out `perf/image-decode-distributed-benchmark` branch. Existing
  untracked local artifacts were preserved and are deliberately out of scope.
-  2026-09-21: prior M1 evidence shows fresh five-round `32795 -> 32799`
  accounting-on work deltas of -18.66% at P50, +1.83% at P95, +5.54% at P99,
  and +7.13% at MAX; this is context only, not fresh evidence for this plan.
-  2026-09-21: current `frames.csv` contains timing and JPEG deltas but no
  per-frame writePixels/copy/materialization/backing attribution. The native
  path already has aggregate copied bytes, clipped-hit, backing-format, JPEG,
  materialization, target-color, and physical-variant counters.
