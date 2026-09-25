<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Frame pacing and scheduling evidence index

Append compact records here. Canonical stage rows belong in the stage CSV/JSON
files; raw process output and temporary run directories stay outside Git.

## Records

- 2026-09-25: Activation started on
  `feat/frame-pacing-scheduling-diagnostics`; both `HEAD` and
  `feat/png-prefetch` resolved to `d7a9ec93faf9f2da0724611de115649176d0b033`.
  Existing local paths are inventoried in
  `.agent/state/frame-pacing-scheduling.md` and excluded from staging.
- 2026-09-25: Signed activation commit `de7ad8077` created and signature
  verified. Header validation and staged diff checks passed. The prescribed
  commit-message check found a body line longer than 80 characters; history was
  not rewritten per plan policy. The corpus root was resolved from the existing
  evidence record `.agent/evidence/image-scroll-diagnostics-macos-02-benchmark.md`
  and validated at 663 files (660 JPEG, 3 PNG).
- 2026-09-25: Stage 1 correctness preflight attempt exited with signal `-5`
  before emitting app output. The retained log was empty at
  `/var/folders/k8/02b7wfkd7fn32vtm3t5mwxwr0000gn/T/frame-pacing-stage-1-l46l2s78/logs/00-synthetic-current-16ms.log`;
  the complete execution directory remains at
  `/var/folders/k8/02b7wfkd7fn32vtm3t5mwxwr0000gn/T/frame-pacing-stage-1-l46l2s78`.
  No canonical evidence files were written. Diagnosis found the runner passed an
  absolute output path although the established macOS runner uses a path
  relative to the bundle working directory. The runner now uses a unique,
  bundle-relative output directory; rerun pending.
- 2026-09-25: Signed correction commit `992ec1ccc` adds the relative output
  argument and contract test; all six frame-pacing contract tests and focused
  header/diff checks pass. The signature is valid. The post-commit message
  validator found one body line over 80 characters; history was not rewritten.
- 2026-09-25: The bundle-relative retry also exited `-5` because its output
  directory began with a dot. A direct invocation with `run=0` and a normal
  bundle-relative output directory completed successfully and produced one
  summary. The runner now uses a visible, invocation-unique directory; matrix
  retry pending. The failed execution log and run directories remain under
  `/var/folders/k8/02b7wfkd7fn32vtm3t5mwxwr0000gn/T/` and the `/tmp` bundle.
- 2026-09-25: The direct `run=0` output passed the full correctness validator:
  663 prefetch requests, 663 READY, and 189 frame rows. Signed correction
  commit `0a0c62ab7` uses a visible output directory. Its signature and
  post-commit message validation pass; full Stage 1 matrix retry pending.
- 2026-09-25: The runner's longer nested visible output path exited `-5`, even
  though a direct run with the shorter relative path
  `frame-pacing-probe-run0/results` passed. The runner now uses a short relative
  path with a unique invocation token and sample number. Matrix retry pending.
- 2026-09-25: With the short output path, the Stage 1 runner completed its
  correctness preflight and three `synthetic-current-16ms` samples, then stopped
  when the `synthetic-60hz` summary reported the 16 ms default. The native VM
  stores app command-line arguments in a 256-byte buffer
  (`TotalCrossVM/src/init/globals.c`). A compact direct command honored 60 Hz.
  The runner now omits default options and the bundle-verified dataset hash, and
  tests that each Part 1 command fits 255 characters. Canonical files were not
  written; retry pending.
- 2026-09-25: Signed command-line correction `7b5013bfd` removes redundant
  defaults, the manifest-verified dataset-hash argument, and the explicit
  semaphore sleep default so all Part 1 profile arguments fit. Six contract
  tests, focused headers, diff check, signature, and commit-message validation
  pass. A compact direct probe recorded `synthetic-60hz` at 16,666,667 ns.
  Stage 1 matrix retry pending.
- 2026-09-25: Stage 1 passed. The preflight had 663 requests, 663 READY, zero
  failed or non-prefetchable images, 189 frame rows, and zero scroll JPEG
  decodes or image/native-geometry materializations. Six measured rows passed:
  three each for `synthetic-current-16ms` and `synthetic-60hz`. Across the six
  rows, frame counts were 181-189; sleep requests 506-552; requested sleep
  totals 1.887-2.062 s; actual sleep totals 2.124-2.314 s; sleep overshoot p95
  527,000-530,084 ns and max 538,667-759,208 ns; deadline error p95
  1,391,459-2,076,458 ns and max 4,340,084-72,757,625 ns. Quantiles were
  ordered, sleep totals were populated with actual >= requested, and all
  per-row pacing counters were non-empty. Canonical files are
  `.agent/evidence/frame-pacing-stage-1-macos.csv` (4,006 bytes) and
  `.agent/evidence/frame-pacing-stage-1-macos.json` (14,533 bytes), both below
  20 KiB. Run logs are under
  `/var/folders/k8/02b7wfkd7fn32vtm3t5mwxwr0000gn/T/frame-pacing-stage-1-3p_g2aj9`;
  process outputs remain in the `/tmp/frame-pacing-stage-1.JrzRFt` bundle.
- 2026-09-25: Stage 2 passed. The accounting-enabled preflight recorded 663
  requests and READY images, zero failed/non-prefetchable images, 118 frame
  rows, and zero scroll JPEG decodes or image/native-geometry materializations.
  Nine fresh measured processes passed across `timer-40-millis`,
  `timer-60-millis`, and `update-millis`; all nine finished at the checked
  -22,440 px Flick displacement. Frame counts were 117-119, 145-149, and
  142-147 respectively. Canonical evidence is
  `.agent/evidence/frame-pacing-stage-2-macos.csv` (5,384 bytes) and
  `.agent/evidence/frame-pacing-stage-2-macos.json` (16,701 bytes), both below
  20 KiB. The bundle manifest points to app source commit `697d460ec` and
  runtime SHA-256
  `ac48fc121de338951824d645f5c7d5e37090e33d6cc6a085bf9d4553906895e0`.
  The package SDK output contained a different dylib; the benchmark used a
  temporary SDK copy with the verified Stage 1 runtime. Runner logs are under
  `/var/folders/k8/02b7wfkd7fn32vtm3t5mwxwr0000gn/T/frame-pacing-stage-2-m1heay10`;
  the temporary bundle is under `/tmp/frame-pacing-stage-2.HOkeMU`.
- 2026-09-25: The first Stage 3 run validated all twelve measured processes
  and the preflight, then refused to publish because compact JSON still
  exceeded 20 KiB. No canonical Stage 3 files were written. The attempt is
  preserved under
  `/var/folders/k8/02b7wfkd7fn32vtm3t5mwxwr0000gn/T/frame-pacing-stage-3-75ed7xcf`.
  The JSON writer now omits null-only row fields and tests both caps.
- 2026-09-25: Stage 3 passed after that writer correction. The preflight
  recorded 663 requests and READY images, zero failed/non-prefetchable images,
  148 frame rows, and zero scroll JPEG decodes or image/native-geometry
  materializations. Twelve fresh measured processes passed across
  `timer-60-millis`, `timer-60-nano`, `update-millis`, and `update-nano`; all
  twelve finished at the checked -22,440 px Flick displacement. Frame counts
  were 144-150, 137-151, 139, and 136-142 respectively. Clock labels appear in
  every result row. Canonical evidence is
  `.agent/evidence/frame-pacing-stage-3-macos.csv` (6,813 bytes) and
  `.agent/evidence/frame-pacing-stage-3-macos.json` (17,699 bytes), both below
  20 KiB. The bundle manifest points to app source commit `fb9dda02b` and
  runtime SHA-256
  `ac48fc121de338951824d645f5c7d5e37090e33d6cc6a085bf9d4553906895e0`.
  Runner logs are under
  `/var/folders/k8/02b7wfkd7fn32vtm3t5mwxwr0000gn/T/frame-pacing-stage-3-_2dw0y1j`;
  the temporary bundle is under `/tmp/frame-pacing-stage-3.uEenM3`.
