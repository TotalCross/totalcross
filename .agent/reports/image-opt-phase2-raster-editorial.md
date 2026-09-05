<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization phase 2 handoff

This report is the factual handoff for the lossless raster optimization phase
on `perf/image-opt-phase2-raster`, based on phase-1 parent
`9545c18207fab74d81340b24825c5a82ddbda7fd`.

## Delivered

- Direct opt-in PNG/JPEG decode into the final native RGBA backing, preserving
  retry, adaptive JPEG denominator, channel order, and parity behavior.
- Cached proof-based opacity metadata with conservative invalidation.
- A guarded software-Skia opaque 1:1 `writePixels()` path with fallback
  accounting and semantic parity coverage.
- Row-bounded native readback and direct row-based APPLY_COLOR2 materialization.
  The final row bridge performs one native call per image, uses one RGBA row
  buffer, and writes ARGB directly into the caller output.

The historical detailed benchmark reports and their raw CSV/summary artifacts
are committed under:

- `.agent/benchmarks/image-opt-phase2-raster/zero-copy/`
- `.agent/benchmarks/image-opt-phase2-raster/opacity/`
- `.agent/benchmarks/image-opt-phase2-raster/opaque-draw/`
- `.agent/benchmarks/image-opt-phase2-raster/readback-color/`

The corrected post-review recapture is committed under
`.agent/benchmarks/image-opt-phase2-raster/corrections/`. It preserves the
historical directories and uses separate decode, opacity, opaque-draw, and
readback/color reports.

## Measured results

The shared protocol used explicit S1/S2/S3 toggle control, three warmups, 60
samples, and 50 ms RSS sampling. The final readback slice is representative:
S1 `pixels` was 23/23 ms median/P95 at 177,856 KiB RSS; S2 remained 23/23 ms;
S3 was 22/22 ms at 147,216 KiB with 7,840-byte row scratch. All S2/S3
checksums matched S1. S3 encoding remained 462/464 ms, and direct color
materialization recorded 63 hits with matching output parity.

The zero-copy, opacity, and opaque-writePixels results, including counter
proofs and fallback behavior, are recorded in their item reports. No confirmed
disabled-path regression remains.

## Validation

Passed on macOS with Release software Skia:

- focused `totalcross.ui.image.*` tests;
- SDK `dist -x test`;
- final `ninja -C build`;
- native Image smoke family covering modifier/color, geometry,
  materialization, zero-copy decode, presentation state, deferred frame/fade,
  and modifier RSS memory behavior;
- final six-run readback S2/S3 matrix and one-sample semantic smoke.

All phase-2 optimization toggles remain disabled by default. Android, iOS,
Windows, Linux, and GPU validation were intentionally not run because they are
outside this phase contract.

The exact phase-3 base revision is recorded in the final state checkpoint.

## Corrective closeout

The review-driven closeout fixed the remaining semantic and measurement gaps:

- direct APPLY_COLOR2 alpha for `0xAAxxxxxx` inputs now matches the legacy
  original-RGB calculation exactly;
- native-backed Graphics mutations materialize mutable targets, advance the
  backing generation, and invalidate cached opacity to `UNKNOWN`;
- zero-copy decode allocation failure is safe across longjmp, releases once,
  and leaves retry behavior intact;
- decode optimization masks are stable process-global state updated by
  settings rather than per-decode toggles;
- opaque-copy dispatch is independent from opacity metadata and uses one
  cached full scan when metadata is disabled and opacity is unknown;
- ordinary native draws and trivial draw-plan writes share one conservative
  eligibility helper.

The corrected 60-sample S1/S2/S3 matrix used batched workloads with full
pixel/byte hashes outside timing. Every sample cleared the 30 ms floor; hashes
were stable and matched across scenarios. The opaque-draw report also records
the intentional guard fallbacks and the single cached metadata-disabled scan
per backing generation. All phase toggles remain disabled by default, and
Android, iOS, Windows, Linux, and GPU validation remain outside this phase.
