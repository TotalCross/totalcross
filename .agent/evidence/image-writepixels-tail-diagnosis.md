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
- 2026-09-21: M1 instrumentation committed as signed commit `bc4a207e9`.
  It adds frame-scope writePixels attribution and scroll/paint segment deltas
  without changing the rendering policy or optimization defaults.
- 2026-09-21: SDK distribution, macOS ARM64 `tcvm`/`Launcher` Release build,
  native `skia_surface_test`, package self-test, and six packaged macOS smoke
  cases passed. The first package smoke exposed the expected native method
  name truncation rule and a trailing CSV delimiter; both were fixed before
  the passing gate and are included in `bc4a207e9`.
- 2026-09-21: fresh package hashes: SDK ZIP
  `ee5f6be1f97424a70598c35cb701d3daec8b67418d774ef5d851cfd20570834a`, SDK
  JAR `e39698d7f2c1524e90aa66d46b239e6d130f6ea682298cca9d4bb6ebfb2ed22a`,
  runtime
  `ca59b0a0436ada76fd34a4ec1dcdafd37dcf418acb5e89f3c00d36b5beb7d975`.
- 2026-09-21: the post-commit message checker reported one over-80-character
  body line in `bc4a207e9`; the signed commit remains unchanged.
- 2026-09-21: the profile-aware reuse comparator fix was committed as signed
  `e6d6651db`; the corrected reuse matrix passed 16 processes, 48 pass
  summaries, and 24 pairwise rows for `32795->32799` and `32827->32831`.
- 2026-09-21: accounting-off validator fixes were committed as signed
  `8dff4345a`; the final timing controls passed 12 cold and 4 reuse processes.
  Earlier failed attempts are preserved in their task-specific build output;
  no raw result directory was deleted.
- 2026-09-21: final raw ZIPs were hashed as diagnostic
  `33c4cbb7860f71d2f8f4909716f48dc56487f2d43fbef6dacfa1c63a472bdd7f`, reuse
  `4141a252629bb7232aaa69d7f53a0a4aa1cbacbdf71f1c9783fe55dc53a2cd05`, and
  timing
  `e94de16e3319bddc5e276adbcc1aab96798b88600f0e7c8ec6d34eb63e1051d8`.
- 2026-09-21: analyzer commit `df2504b0e` persisted 48 paired percentile
  rows and 108 P95/P99/MAX outlier rows. Every enabled selected outlier had
  writePixels hits; all observed hits were full, not clipped. Copy bytes were
  bounded at 7,732,800 per frame, and prefetch-on cold copy/work correlations
  were 0.011915 and 0.012663 for the two enabled masks.
- 2026-09-21: the paired artifact review selected Outcome 2. Cold outliers
  commonly contain decode/materialization activity; warm outliers can persist
  without new materialization. The report records identity-match limits and
  the absence of a separate copy-duration timer.
- 2026-09-21: correction reopened the STOP / REVIEW gate because the prior
  Outcome 2 was not justified without direct writePixels duration. Signed
  source commit `58ce284aa` adds accounting-gated total-path, pre-copy
  preparation, actual `targetCanvas->writePixels`, and RGB565 conversion
  timers, plus scroll/paint CSV fields and focused runner validation.
- 2026-09-21: corrected native `tcvm` Release target and SDK `dist -x test`
  passed. The post-commit checker reported one over-80-character body line in
  `58ce284aa`; history is preserved without amendment.
- 2026-09-21: the first corrected package attempt failed before benchmark
  execution because it used an older SDK ZIP missing the existing diagnostic
  bridge. The task-specific failed output was preserved; rebuilding from
  `build/TotalCross/dist/totalcross-sdk.jar` produced package v3.
- 2026-09-21: corrected package v3 passed self-test and six macOS smoke cases.
  The focused fresh matrix passed 8 accounting-on cold, 4 accounting-on
  three-pass reuse, and 4 accounting-off cold processes on `32795 -> 32799`.
  The direct analyzer retained 10 paired rows and 30 P95/P99/MAX rows under
  `timing-correction/`.
- 2026-09-21: direct timing found the largest prefetch-on cold tail at
  39,112,500 ns with 513,917 ns total writePixels, 507,500 ns actual copy,
  and 5,627 ns preparation. The largest prefetch-off tail was 373,441,250 ns
  with 1,064,040 ns total writePixels and 0.278% copy/work. Reuse showed the
  same pattern: 30,140,875 ns with 299,541 ns total writePixels on and
  380,697,625 ns with 1,155,623 ns total off.
- 2026-09-21: correction evidence supports Outcome 2. The accounting-off
  control marked all new timing fields unavailable as `-1`; RGB565 conversion
  timing was available but zero in the 32-bit primary pair. Historical M1–M4
  compact artifacts were not rewritten.
- 2026-09-21: timing-correction plan, state, report, analyzer, summary, paired
  rows, and outliers were committed as signed `240f43303`. The post-commit
  checker reported over-80-character body lines; the commit is preserved
  without amendment.
