<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Definitive image-scroll prefetch matrix

- Date: 2026-09-10
- Corpus: `/Users/flsobral/Downloads/win32`, exactly 663 JPEGs.
- Matrix: 16 fresh macOS processes and 48 pass records; every process reported
  `overallPass=true`.
- Runtime SHA-256:
  `6ad937b552e9305a39b17c363b655fc50f93d977181cd3435a4fdd770797d72c`.
- Raw records: `results.csv` in this directory.

The all-prefetch profile accounted for 663 requests in every process: 660
ready, zero failed, and three not-prefetchable. Cold scrolling performed zero
targeted JPEG decodes, three full JPEG decodes, at most three final raster
materializations, and at most three native geometry materializations. Cold p95
was at most 5 ms and frames at or above 34 ms were at most 2.

Warm and warm2 performed zero targeted/full JPEG decodes, zero final raster
materializations, and zero native geometry materializations in every
prefetch-enabled record. `physical_variant_bytes` and
`target_color_converted_bytes` were zero throughout the matrix. The warm p95
was at most 5 ms; the worst prefetch-enabled warm ratio against its matching
disabled baseline was 55.6%, within the 10% regression gate.

Because accounting is reset after the UI tree is built and immediately before
`prepareForDisplay`, prefetch backing ranges are directly attributable to the
prefetch phase: live `371794316..452295356` bytes and peak
`379123836..452295356` bytes.

A preliminary full matrix in the sibling `final-definitive/` directory had
one transient warm2 p95 sample at 13 ms versus a 9 ms baseline. Three fresh
reruns of that combination were 5--6 ms, and the definitive full matrix above
passed without changing any gate.

Validation was limited to the focused SDK tests, Release SDK distribution,
Release macOS tcvm build, and the macOS ImagePreparation smoke as required by
the plan. Android, Linux, Windows, and iOS builds remain deferred.
