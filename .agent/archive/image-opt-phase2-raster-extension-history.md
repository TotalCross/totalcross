<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Phase 2 raster extension history

## 2026-09-07 — Bootstrap

The extension starts from Phase 2 tip
`a225d10165b8b60c4bf7bf2f95f5bf3b395f3a92`, with frozen Phase 1 base
`a8a9480bd61aa510de423569af494d8dde69e8f2` in its ancestry. Historical Phase 2
state, reports, and benchmark artifacts are preserved and will not be rewritten.

## 2026-09-07 — Milestone 1 rebaseline

The three additive benchmark workloads were committed before any ID 13-15
runtime implementation. The benchmark-source production revision for this
milestone is
`25a43c0d55bd8550fef3e992e212da9fd2d57d15`.

The frozen Phase 1 S1 used runtime
`a8a9480bd61aa510de423569af494d8dde69e8f2`, the current benchmark harness
revision above, and true-base adapter digest
`69383c1689963b04e1a94e2a0403a024a6c249105683924fab5b8572c496edf9`.
S2 disabled every optimization feature; S3 enabled only controls 0-4. All
integrated workload hashes were stable. S2 median elapsed time was 877 ms
versus 876 ms for S1, and peak RSS was 128352 KiB versus 124016 KiB; the
observed deltas remain below the 5% regression threshold. S3 was escalated to
200 samples after the 60-sample CV was 5.56%; the authoritative 200-sample CV
was 4.22%, with median elapsed time 59 ms and peak RSS 132816 KiB.

The physical-identity disabled control was also captured at the current
production revision: 60 samples, median 5629 ms, peak RSS 114208 KiB. The
initial stale installed dylib was detected by symbol/timestamp inspection and
replaced with the exact macOS Release build before measurements; no historical
artifact was changed.
