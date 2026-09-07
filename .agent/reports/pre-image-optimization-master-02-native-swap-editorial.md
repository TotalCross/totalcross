<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Pre-image optimization master 02 native swap handoff

Plan 2 is complete on `fix/pre-image-optimization-master` after correcting the
benchmark harness and replacing its superseded evidence.

Handoff identifiers:

- Plan-1 BASE_SHA: `1898014784b2fba5716cc033e49520740b05f0dd`.
- PLAN1_IMPLEMENTATION_HEAD: `8156f62f9cdf41b6d2cd2e18b7ba4b4704ad98b2`.
- PLAN2_BASE: `f918fd4a6ff32c051231657ae58148a040edc6df`.
- Corrected benchmark source: `8e0b7f5b3b528efb6f1bebc6e675d2ffce215610`.
- Corrected workflow run: [34072184561](https://github.com/TotalCross/totalcross/actions/runs/34072184561).

## Benchmark correction

The original result set is superseded. Its per-pixel `SwapFunction` pointer
measured indirect dispatch rather than the production loop. The corrected
source has two direct NOINLINE buffer-level loops: one using
`__builtin_bswap32`/`_byteswap_ulong`, and one using the exact `SWAP32`
shift/mask expression. The compiler can therefore optimize each complete loop
as it can in `skia_makeBitmap()`.

The fixed executable interface, deterministic buffers, checksum sink, timing
boundaries, A/B then B/A order, 3 warmups, and 60/200 sample checkpoints are
unchanged. The corrected local macOS arm64 run and all four native workflow
jobs produced checksum-agreeing results for 512x512, 1920x1080, and 3840x2160.
Corrected per-size JSON summaries and raw CSV rows are committed under
`.agent/benchmarks/pre-image-optimization-master/native-swap/`.

## Corrected native-swap matrix

The ratio is native median divided by portable median; values below 1.0 mean
the native loop was faster. Each cell is `60-pair / 200-pair`. Compiler, OS,
architecture, p95, pixel count, and checksum metadata remain in each JSON file.

| Platform / architecture | Compiler | 512x512 | 1920x1080 | 3840x2160 |
| --- | --- | ---: | ---: | ---: |
| macOS arm64 | Clang Apple LLVM 21.0.0 | 1.042900 / 1.002290 | 0.968895 / 1.013570 | 0.967123 / 0.995903 |
| Linux x86-64 | GCC 11.4.0 | 1.000190 / 1.000180 | 1.001390 / 0.999454 | 1.001380 / 0.995974 |
| Linux ARM64 | GCC 11.4.0 | 1.000350 / 0.998794 | 0.996209 / 1.001480 | 1.011290 / 1.003910 |
| Windows x86-64 | MSVC 1944.35228 | 0.987469 / 1.005090 | 0.996642 / 1.002440 | 1.009320 / 1.015380 |
| Windows ARM64 | MSVC 1944.35228 | 0.238587 / 0.252329 | 0.393846 / 0.378473 | 0.483902 / 0.480236 |

Every Windows/Linux target/size has checksum agreement. Windows ARM64 has a
stable >=5-percent native win at every size. No Windows/Linux target/size has
a stable >5-percent regression: all other 60/200 ratios stay within 5 percent
of parity. Under the corrected fixed rule, retain the current policy. macOS is
corroborating evidence only.

## Production policy

The measured policy is unchanged and is now supported by the corrected full
matrix:

- `USE_NATIVE_SWAP` remains `0` on Apple/Android and `1` elsewhere.
- `skia_internal.h` is the sole default-definition site.
- `USE_COMPUTE_OPAQUE`, `USE_COLORTYPE_CONVERSION`, and `USE_WRITE_PIXELS`
  retain their existing defaults and behavior.
- Native and portable `SWAP32` implementations remain unchanged.
- Duplicate default blocks remain removed from `skia.cpp`.

No differentiated runtime or architecture dispatch was added. Runtime
opacity/writePixels flags, physical-transform folding, materialization caching,
GPU backing, compact formats, color-type specialization, target color-type
conversion, and all other image-optimization work remain deferred.

## Validation and limitations

Passed validations:

- Corrected optimized macOS arm64 benchmark smoke: 3 warmups, 10 pairs,
  checksum parity, valid CSV/JSON, and actual `uname -m=arm64`.
- Corrected local macOS 60/200 checkpoints for all three sizes.
- Corrected workflow run 34072184561: native Linux x86-64/ARM64 and Windows
  x86-64/ARM64 jobs all passed; no emulation was used.
- Replacement artifact validation: 30 summaries/raw files, checksum parity,
  required metadata, and every raw file below 20 KiB.
- No indirect `SwapFunction` or scalar swap helper remains in the benchmark.
- Focused copyright validation and `git diff --check` before each correction
  commit.
- Existing final macOS Release software-Skia `skia_surface_test` passed after
  the duplicate macro cleanup; corrected evidence requires no new production
  macro change.

No Windows/Linux TotalCross build, SDK rebuild, packaging build, master merge,
or optimization-branch rebase was performed. The timings are isolated
microbenchmark measurements, not end-to-end image-performance claims.

This corrected final HEAD is the intended master merge candidate. The exact
local HEAD is recorded in the final execution response and state. Later rebase
order remains:

    perf/image-opt-phase1-controls
      -> perf/image-opt-phase2-raster
      -> subsequent image-optimization branches
