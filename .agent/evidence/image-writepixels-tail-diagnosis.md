<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Evidence: writePixels tail diagnosis

- 2026-09-21: created branch `perf/writepixels-tail-diagnosis` from the
  checked-out `perf/image-decode-distributed-benchmark` branch. Existing
  untracked local artifacts were preserved and are deliberately out of scope.
- 2026-09-21: bootstrap started at base/HEAD
  `d5a682e0d1a32928f1e96bc62be083f6f2d813df`; the corpus candidate
  `/Users/flsobral/Downloads/win32/win32/imag` contains 663 JPEG-named files.
- 2026-09-21: plan/state/evidence bootstrap was committed as signed commit
  `a3c1c8820`. Its message checker later reported over-80-character body
  lines; the commit remains preserved and is not rewritten.
- 2026-09-21: bootstrap checkpoint update was committed as signed commit
  `67310f920`; its message checker also reported over-80-character body lines.
  Both historical messages remain unchanged per the no-rewrite rule.
-  2026-09-21: prior M1 evidence shows fresh five-round `32795 -> 32799`
  accounting-on work deltas of -18.66% at P50, +1.83% at P95, +5.54% at P99,
  and +7.13% at MAX; this is context only, not fresh evidence for this plan.
-  2026-09-21: current `frames.csv` contains timing and JPEG deltas but no
  per-frame writePixels/copy/materialization/backing attribution. The native
  path already has aggregate copied bytes, clipped-hit, backing-format, JPEG,
  materialization, target-color, and physical-variant counters.
