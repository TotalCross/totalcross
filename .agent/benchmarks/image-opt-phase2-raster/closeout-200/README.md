<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Phase-2 raster 200-sample closeout

This directory contains the closeout measurement regime requested after the
corrective 60-sample matrix. Each run used three warmups, 200 measured
samples, Release macOS software Skia, and external RSS sampling every 50 ms.
Raw CSV files are stored as deterministic `gzip -n -9` streams; the matching
runner summaries remain plain text. Local verbose runner logs are under
`logs/` and are intentionally not committed.

`true-base-s1/` uses runtime
`9545c18207fab74d81340b24825c5a82ddbda7fd` and the versioned adapter at
`../true-base-harness/`, digest
`cbfada1ba5f95c27005c473d1f7f128e644adf8f8496d8573bf8beeb1dc08a19`.
`final-s2/` and `final-s3/` use final runtime/harness revision
`f66561b99c7b06ee1f7e7d627583de7066c39f0a`.

The canonical requested comparisons are in `report.md`. The `repeat-*` and
`diagnostic-final-pre/` directories are additional RSS-boundary controls: they
are retained because the integrated S2/S1 comparison crossed the 5% boundary.
