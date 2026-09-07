<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Pre-image optimization master 02 native swap handoff

This is the latest measurement-only handoff for
`fix/pre-image-optimization-master`. It supersedes the earlier two-way
benchmark evidence. No production swap expression, macro default, or runtime
policy is changed by this four-way rerun.

## Handoff identifiers

- Plan-1 BASE_SHA: `1898014784b2fba5716cc033e49520740b05f0dd`.
- PLAN1_IMPLEMENTATION_HEAD:
  `8156f62f9cdf41b6d2cd2e18b7ba4b4704ad98b2`.
- PLAN2_BASE: `f918fd4a6ff32c051231657ae58148a040edc6df`.
- Corrected benchmark source:
  `ef44795adabe6bd33fb41e7687cbc581960be0ab`.
- Four-way artifact/evidence checkpoint: `1f84208d2`.
- Corrected workflow run: [34075737240](https://github.com/TotalCross/totalcross/actions/runs/34075737240).
- Local host: macOS arm64, Clang Apple LLVM 21.0.0
  (`clang-2100.1.1.101`).

The exact final branch HEAD is verified after the documentation commit and is
reported in the execution handoff and state file.

## Benchmark contract

The standalone benchmark has one separate `NATIVE_SWAP_NOINLINE` buffer-level
function and direct loop for each variant:

1. `SWAP32_PORTABLE`: the portable shift/mask expression from
   `skia_internal.h`.
2. `SWAP32_FORCED`: the existing `xtypes.h` expression, including its
   `unsigned long` casts.
3. `swap32_forced_impl`: a single-evaluation 32-bit shift/mask helper.
4. `builtinSwap32`: `__builtin_bswap32` on GCC/Clang and
   `_byteswap_ulong` on MSVC.

The benchmark does not use per-pixel function dispatch. Each timed operation
executes the complete direct buffer loop, allowing the compiler to optimize
or vectorize it. Allocation, deterministic initialization, checksum work,
file I/O, and formatting are outside timing. The four variants process the
same source buffer, all checksums must agree, and the four-way order rotates
deterministically across samples. The clock is monotonic.

Each size uses three warmups, then 60 and 200 samples. Raw CSV records contain
one timing column per variant; JSON summaries contain each variant's median,
p95, ratio to the portable median, plus compiler, OS, architecture, warmups,
sample count, pixel count, and checksum agreement.

## Complete ratio matrix

Each tuple is ordered `[SWAP32_PORTABLE, SWAP32_FORCED, swap32_forced_impl,
builtinSwap32]`. Values are the variant median divided by the portable median;
values below `1.0` are faster than the portable baseline. Cells show
`60-sample / 200-sample` tuples. Exact medians and p95s are in the committed
JSON summaries.

| Platform / architecture | Compiler | Size | 60-sample ratios | 200-sample ratios |
| --- | --- | --- | --- | --- |
| macOS arm64 | Clang Apple LLVM 21.0.0 | 512x512 | [1, 1.011560, 1.009280, 1] | [1, 0.997674, 0.997617, 1] |
| macOS arm64 | Clang Apple LLVM 21.0.0 | 1920x1080 | [1, 1.004940, 1.049550, 1.046900] | [1, 1.001160, 0.997110, 1.003080] |
| macOS arm64 | Clang Apple LLVM 21.0.0 | 3840x2160 | [1, 0.946174, 0.941983, 0.950048] | [1, 1.002980, 0.998015, 1.005390] |
| Linux x86-64 | GCC 11.4.0 | 512x512 | [1, 0.999897, 0.999897, 1.717980] | [1, 1, 1.000100, 1.718500] |
| Linux x86-64 | GCC 11.4.0 | 1920x1080 | [1, 0.999391, 1.000280, 1.715810] | [1, 0.999354, 1.000210, 1.719790] |
| Linux x86-64 | GCC 11.4.0 | 3840x2160 | [1, 0.998822, 0.999915, 1.706210] | [1, 0.999680, 0.999416, 1.706290] |
| Linux ARM64 | GCC 11.4.0 | 512x512 | [1, 0.998190, 0.999397, 0.996683] | [1, 1.001240, 0.999378, 1] |
| Linux ARM64 | GCC 11.4.0 | 1920x1080 | [1, 1.001200, 1.003250, 1.001900] | [1, 1.000240, 1.002140, 1.002170] |
| Linux ARM64 | GCC 11.4.0 | 3840x2160 | [1, 0.999040, 0.995558, 1.001390] | [1, 0.999256, 1.003480, 0.999225] |
| Windows x86-64 | MSVC 1944.35228 | 512x512 | [1, 0.328719, 0.0795331, 0.0765472] | [1, 0.328812, 0.079490, 0.0770483] |
| Windows x86-64 | MSVC 1944.35228 | 1920x1080 | [1, 0.328680, 0.0809865, 0.0807121] | [1, 0.328636, 0.0797929, 0.0791071] |
| Windows x86-64 | MSVC 1944.35228 | 3840x2160 | [1, 0.327833, 0.130426, 0.129155] | [1, 0.328341, 0.129826, 0.128999] |
| Windows ARM64 | MSVC 1944.35228 | 512x512 | [1, 0.529741, 0.502796, 0.144382] | [1, 0.526477, 0.496945, 0.139511] |
| Windows ARM64 | MSVC 1944.35228 | 1920x1080 | [1, 0.559841, 0.535010, 0.265078] | [1, 0.557900, 0.533262, 0.251753] |
| Windows ARM64 | MSVC 1944.35228 | 3840x2160 | [1, 0.568290, 0.552481, 0.306488] | [1, 0.573226, 0.554718, 0.304800] |

All 30 JSON/CSV pairs report checksum agreement. The four native CI jobs were
successful on the requested native Linux x86-64/ARM64 and Windows x86-64/ARM64
runners; no QEMU, emulation, or TotalCross build was used.

## Production scope

This rerun leaves `SWAP32`, `SWAP32_FORCED`, `USE_NATIVE_SWAP`,
`skia_internal.h`, and `xtypes.h` unchanged. It makes no recommendation or
change to production defaults. Opacity, writePixels, color-type behavior,
physical-transform folding, materialization caching, GPU backing, compact
formats, and the remaining image-optimization work stay outside this slice.

## Validation and limitations

Passed validations:

- macOS arm64 four-way optimized smoke with rotating order, checksum parity,
  and valid raw/summary output.
- Local macOS arm64 60/200 checkpoints for all three dimensions.
- Workflow run 34075737240 with all four native architecture jobs successful.
- Replacement validation of 30 summaries and 30 raw CSV files, including all
  four timing columns, medians, p95s, ratios, metadata, checksum agreement,
  and the under-20-KiB artifact limit.
- Structural check for four direct buffer-level loops and no `SwapFunction`
  dispatch in the benchmark.
- Focused copyright-header validation and `git diff --check` before commits.

Skipped expensive validations are Linux/Windows TotalCross builds, SDK
rebuilds, packaging, and end-to-end image benchmarks because this slice is
measurement-only. The existing macOS Release software-Skia validation from
the earlier production cleanup remains unchanged; no new production C/C++
change required a rerun here.

The branch remains unmerged and unrebased. The intended later rebase order is:

    perf/image-opt-phase1-controls
      -> perf/image-opt-phase2-raster
      -> subsequent image-optimization branches
