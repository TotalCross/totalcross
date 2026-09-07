<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Pre-image optimization master 02 native swap handoff

Plan 2 is complete on `fix/pre-image-optimization-master`.

Handoff identifiers:

- Plan-1 BASE_SHA: `1898014784b2fba5716cc033e49520740b05f0dd`.
- PLAN1_IMPLEMENTATION_HEAD: `8156f62f9cdf41b6d2cd2e18b7ba4b4704ad98b2`.
- PLAN2_BASE: `f918fd4a6ff32c051231657ae58148a040edc6df`.
- Validated branch head before this final handoff commit:
  `f77b1ecbf`.

## Native-swap matrix

The ratio is native median divided by portable median; values below 1.0 mean
the native expression was faster. Each cell is `60-pair / 200-pair`, and every
run used three warmups and checksum agreement. Full medians, p95 values, raw
rows, compiler strings, and pixel counts are in the per-size JSON/CSV artifacts.

| Platform / architecture | Compiler | 512x512 | 1920x1080 | 3840x2160 |
| --- | --- | ---: | ---: | ---: |
| macOS arm64 | Clang Apple LLVM 21.0.0 | 0.999715 / 1.000140 | 1.001070 / 1.000540 | 0.996271 / 1.000740 |
| Linux x86-64 | GCC 11.4.0 | 1.512110 / 1.488210 | 1.497580 / 0.988315 | 1.493490 / 1.494100 |
| Linux ARM64 | GCC 11.4.0 | 0.989927 / 1.000140 | 1.000730 / 1.000130 | 1.000830 / 0.999821 |
| Windows x86-64 | MSVC 1944.35228 | 1.182130 / 1.000410 | 1.115990 / 1.001430 | 1.168890 / 1.042060 |
| Windows ARM64 | MSVC 1944.35228 | 0.928144 / 0.891643 | 0.919328 / 0.954739 | 0.967859 / 0.952637 |

The fixed rule selects retention. Windows ARM64 has a stable greater-than-
5-percent native win at 512x512 in both checkpoints, while Linux x86-64 has
stable greater-than-5-percent regressions at 512x512 and 3840x2160 in both
checkpoints. Rule 4 therefore requires retaining the current cross-platform
defaults; no per-architecture policy was introduced. macOS corroborates that
the two expressions are effectively equal there and does not override the
native Windows/Linux result.

## Production policy

`TotalCrossVM/src/nm/ui/skia/skia_internal.h` remains the sole default site:

- `USE_NATIVE_SWAP` is `0` on Apple/Android and `1` elsewhere.
- `USE_COMPUTE_OPAQUE` and `USE_COLORTYPE_CONVERSION` keep their existing
  Apple/Android `0`, other-platform `1` defaults.
- `USE_WRITE_PIXELS` remains `1`.
- Both native and portable `SWAP32` implementations remain unchanged.
- The duplicate default blocks were removed from `skia.cpp`.

Runtime opacity/writePixels flags, physical-transform folding, materialization
caching, GPU backing, compact formats, color-type specialization, and target
color-type conversion remain deferred to the image-optimization branches.

## Validation and limitations

Passed validations:

- Optimized macOS arm64 benchmark smoke: 3 warmups, 10 pairs, valid CSV/JSON,
  checksum parity, and `uname -m=arm64`.
- Local macOS 60/200 checkpoints for all three sizes.
- GitHub Actions run [34070711950](https://github.com/TotalCross/totalcross/actions/runs/34070711950): native Linux x86-64/ARM64 and Windows x86-64/ARM64 jobs all passed.
- 30 summary/raw artifact checks, checksum parity, and raw-file size limits.
- Focused copyright-header validation and `git diff --check` for each staged
  commit and the final working tree.
- Release macOS software-Skia reconfiguration, `ninja -C build-preopt-macos
  skia_surface_test`, and the resulting `skia_surface_test` executable.

No Windows/Linux TotalCross, SDK, packaging, or cross-platform runtime build
was run. The plan explicitly limits those runners to the standalone benchmark,
and no SDK production source changed after plan 1. The timings are isolated
microbenchmark measurements and should not be read as whole-image performance
claims.

Three early plan-2 commits (`63fc89a48`, `298f51d00`, and `9c6ad7743`) contain
overlong body lines reported by the local commit-message mirror. They were not
amended because the logical-commits instructions prohibit history rewriting;
all later plan-2 commit-message checks passed. The final response records the
exact resulting branch HEAD.

This final HEAD is the intended master merge candidate. Do not merge it to
`master` or rebase optimization branches here. Later rebase order remains:

    perf/image-opt-phase1-controls
      -> perf/image-opt-phase2-raster
      -> subsequent image-optimization branches
