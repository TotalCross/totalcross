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

The corrected individual matrices are under
`.agent/benchmarks/image-opt-phase2-raster/corrections/true-baseline/`. Their
S1 captures run the actual Phase-1 runtime at
`9545c18207fab74d81340b24825c5a82ddbda7fd`; S2/S3 run the final
Phase-2 runtime with all non-target features explicitly disabled. All 33
individual scenario files contain 60 samples, every sample is at least 30 ms,
and every full output hash matches across S1/S2/S3. The per-item reports record
the runtime SHA, harness SHA/digest, peak RSS, and counters.

The integrated matrix is under
`.agent/benchmarks/image-opt-phase2-raster/corrections/combined/`. Its median
elapsed time is 877 ms (S1), 878 ms (S2), and 56 ms (S3); P95 is 881 ms,
884 ms, and 62 ms; peak RSS is 122,688 KiB, 131,296 KiB, and 140,352 KiB.
All three output hashes are identical in every scenario. S3 records 120
zero-copy decodes, 60 source and 60 decode opacity proofs, 61,440/61,440
writePixels attempts/hits, 92,160 row reads, zero full readbacks, and 60
direct color materializations.

## 200-sample reproducibility closeout

The versioned true-base adapter is
`.agent/benchmarks/image-opt-phase2-raster/true-base-harness/prepare-image-opt-phase2-true-base.sh`.
It adapts only benchmark compatibility/counter calls and deployment inputs to
the Phase-1 runtime. Its digest is
`cbfada1ba5f95c27005c473d1f7f128e644adf8f8496d8573bf8beeb1dc08a19`; the S1
200-sample recapture uses that adapter and the exact Phase-1 runtime SHA
`9545c18207fab74d81340b24825c5a82ddbda7fd`.

The new closeout is under
`.agent/benchmarks/image-opt-phase2-raster/closeout-200/`. Opacity JPEG S2
RSS is -1.87% versus S1; readback pixels, encode, and color are -4.14%,
-8.09%, and -1.82%. Their medians/P95 remain equal or within one millisecond,
and output hashes match exactly.

The integrated canonical S1/S2/S3 results are 878/874/56 ms median,
883/879.05/59.05 ms P95, and 116,176/122,288/126,560 KiB peak RSS. Input
JPEG and generated opaque-PNG full-content hashes are identical in all three
scenarios. S3 counters prove 400 zero-copy decodes, 200 source and decode
opacity proofs, 204,800 writePixels hits, 307,200 row reads, no full
readbacks, and 200 direct color materializations.

The integrated S2/S1 RSS delta is above 5% in the canonical run and in its
repeat, but final-runtime `pre` versus `post-disabled` controls differ by
only 0.95% in the repeated pair with identical medians. This distinguishes a
cross-runtime revision RSS shift from demonstrated disabled-feature overhead;
no runtime fix is claimed or applied. The full diagnosis and repeat controls
are in the closeout report. Historical, corrected-60-sample, true-base, and
200-sample evidence are intentionally kept separate.

## Validation

Passed on macOS with Release software Skia:

- focused `totalcross.ui.image.*` tests;
- SDK `dist -x test`;
- final `ninja -C build`;
- native Image smoke family covering modifier/color, geometry,
  materialization, zero-copy decode, presentation state, deferred frame/fade,
  and modifier RSS memory behavior;
- final six-run readback S2/S3 matrix and one-sample semantic smoke.
- true-base S1 plus final-runtime S2/S3 matrices for decode, opacity,
  opaque-draw, and readback/color;
- integrated five-optimization S1/S2/S3 matrix with exact output parity.
- true-base and final-runtime 200-sample closeout runs for opacity JPEG,
  readback pixels/encode/color, and the integrated workload.
- final focused `totalcross.ui.image.*` tests, SDK distribution, incremental
  Release software-Skia native build, smoke-test compilation, and related
  native Image smokes after the harness changes.

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

The corrected 60-sample S1/S2/S3 matrices and the new 200-sample closeout use
a true Phase-1 S1 rather than a final-runtime disabled-feature proxy. The
integrated workload enables all five Phase-2 features only in S3 and proves
each with native counters. Hashes are stable and match across scenarios. The
opaque-draw report also records the intentional guard fallbacks and the single
cached metadata-disabled scan per backing generation. All phase toggles remain
disabled by default, and Android, iOS, Windows, Linux, and GPU validation
remain outside this phase.
