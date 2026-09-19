<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Evidence — raster policy audit and reuse

## 2026-09-18 — bootstrap

- Branch: `perf/image-decode-distributed-benchmark`.
- HEAD: `57d0532409c2163ac390d2f0affd8a0787049b07`.
- Required Plan 2 revision is an ancestor of HEAD.
- Prior Plan 2 state, evidence, and benchmark summary were read.
- Unrelated local files were identified and remain unstaged.
- No native build, package, or benchmark was run.
- Next slice: Milestone 1 correctness and benchmark hygiene.

## 2026-09-19 — Milestone 1 complete; STOP / REVIEW 1

- Logical implementation commits: `ed09cf657`, `06fd3e234`, `135050818`,
  `1f5bdbbea`, `f02f9dcff`, and `1fb0b1d35`.
- The native target-format bridge required a follow-up correction: the
  deployed VM truncates long native symbols, and the original `Image` probe
  was unsafe. Final reporting captures one packed snapshot through the
  established `NativeImageBacking` bridge before the workload.
- Focused validation passed: SDK distribution, macOS ARM64 CMake targets
  `tcvm`, `Launcher`, and `skia_surface_test`, plus the native surface test.
- Package self-test passed. Six accounting-on smokes passed for masks `0`, `4`,
  and `32799`, with prefetch off/on. The 20-process
  `write-pixels-accounting-ab` matrix passed and aggregation produced 20 rows.
- Corpus self-test hash: `588a7e0f4019424a`.
- Environment metrics: software target `56x896`, row bytes `4320`, Skia color
  type `6` (`BGRA8888`), alpha type `2`, and `kN32` type `4`.
- Accounting-off runs retained work/paint timings and emitted
  `accountingEnabled:false` and `diagnosticsAvailable:false`; the runner did
  not require diagnostic fields. The accounting-on `32799` runs recorded
  `3402–3408` writePixels attempts, all hits and zero fallbacks; `32795`
  recorded no writePixels attempts.
- Five-round median work P50 for `32799` versus `32795` improved by 19.52%
  with accounting on and 19.16% with accounting off. P95 improved 19.51%
  with accounting on but increased 15.95% with accounting off; P99/MAX tails
  varied, so no causal P95 claim is made.
- Opacity contract evidence: the bit-1-disabled, prefetch-off diagnostic
  smoke recorded intrinsic-known JPEG opacity (`157`) with only `2` fallback
  scans; the `32799/off` smoke recorded intrinsic `170` and zero scans.
- Detailed raw samples, summaries, and artifact paths are in
  `.agent/benchmarks/image-raster-policy-03/m1-write-pixels-accounting.md`.
- Post-commit checks also recorded body-line formatting defects in
  `82f4af600`, `ed09cf657`, `f02f9dcff`, and `1fb0b1d35`. These commits were
  not amended or rewritten.
- Milestone 2 is intentionally not started. Review approval is required.
