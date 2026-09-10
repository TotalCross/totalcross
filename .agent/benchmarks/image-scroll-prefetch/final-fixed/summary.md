<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Corrected image-scroll prefetch matrix

- Corpus: 663 JPEGs from `/Users/flsobral/Downloads/win32`.
- Matrix: 16 fresh macOS processes, 48 pass records, two resolutions, four
  target-color/variant-cache combinations, and disabled/all prefetch profiles.
- Native runtime SHA-256:
  `6ad937b552e9305a39b17c363b655fc50f93d977181cd3435a4fdd770797d72c`.

## Corrected prefetch results

- Every all-prefetch cold run accounted for 663 requests: 660 ready, zero
  failures, and three not-prefetchable images.
- Cold scrolling performed zero targeted JPEG decodes; the only remaining
  cold work was the three non-prefetchable/full-decode cases.
- Cold final raster and native geometry materializations were both at most 3.
- Cold p95 was at most 6 ms; frames at or above 34 ms were at most 1.
- Warm targeted/full JPEG decodes, final materializations, and native geometry
  materializations were all zero.
- Warm all-prefetch p95 was at most 5 ms versus at most 9 ms for disabled
  prefetch in this run; the integer-ms comparison uses the rounded 10% ceiling.
- Prefetch elapsed time was 11,511--14,195 ms. Live backing memory after the
  corrected COPY_READY lifecycle was 282,405,544--356,793,048 bytes.

## Corrected versus previous implementation

The earlier `final/` implementation removed first-use JPEG decode from the
scroll but left roughly 645--648 final image/native-geometry materializations
in the cold scroll. The corrected implementation uses `COPY_READY` for the
default `ImageControl`, materializes and caches the final raster before the
scroll, and releases only the intermediate decoded source backing once no
cached final variant references it. The corrected `final-fixed/` matrix has at
most three cold materializations and zero warm materializations.

Raw process logs and `results.csv` in this directory are the committed evidence
for the corrected matrix.
