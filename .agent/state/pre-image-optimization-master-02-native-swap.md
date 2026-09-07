<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Pre-image optimization master 02 native swap state

- Active milestone/slice: Complete — latest four-way measurement handoff.
- Branch: `fix/pre-image-optimization-master`.
- PLAN1_IMPLEMENTATION_HEAD:
  `8156f62f9cdf41b6d2cd2e18b7ba4b4704ad98b2`.
- PLAN2_BASE: `f918fd4a6ff32c051231657ae58148a040edc6df`.
- Latest benchmark source commit:
  `ef44795adabe6bd33fb41e7687cbc581960be0ab`.
- Latest artifact/evidence commit: `1f84208d2`
  (`test(skia): record four-way swap results`).
- Latest documentation commit: `c3f2e8b95`
  (`docs(image): update swap benchmark evidence`).
- Corrected benchmark artifacts and compact evidence are committed under
  `.agent/benchmarks/pre-image-optimization-master/native-swap/` and
  `.agent/evidence/pre-image-optimization-master-02-native-swap.jsonl`.
- Active implementation paths:
  `TotalCrossVM/src/nm/ui/skia/benchmarks/native_swap_benchmark.cpp`,
  `.github/workflows/native-swap-benchmark.yml`.
- Next exact action: verify the final local HEAD, scoped worktree state, and
  four-way artifact audit.

## Latest benchmark collection

- Local host: macOS arm64; compiler: Clang Apple LLVM 21.0.0
  (`clang-2100.1.1.101`).
- Workflow run: `34075737240`; native Linux x86-64/ARM64 and Windows
  x86-64/ARM64 jobs all succeeded.
- Dimensions: 512x512, 1920x1080, and 3840x2160.
- Checkpoints: three warmups, then 60 and 200 samples per dimension.
- Variants: `SWAP32_PORTABLE`, `SWAP32_FORCED`, `swap32_forced_impl`, and
  `builtinSwap32`.
- Collection validation: 30 JSON summaries and 30 CSV files; all four timing
  columns, medians, p95s, ratios, metadata, rotating-order records, checksum
  agreement, and under-20-KiB file limits passed.
- The prior two-way evidence is explicitly superseded. The latest continuation
  is measurement-only and makes no production policy recommendation.

## Scope and validation

- No changes in this continuation to `SWAP32`, `SWAP32_FORCED`,
  `USE_NATIVE_SWAP`, `skia_internal.h`, or `xtypes.h`.
- Four-way optimized macOS smoke passed with checksum parity and rotating
  order; local full matrix passed; corrected native workflow passed.
- Structural benchmark check passed: four separate direct buffer-level
  `NATIVE_SWAP_NOINLINE` loops and no `SwapFunction` dispatch.
- Focused copyright validation and `git diff --check` passed for the
  benchmark source and artifact/evidence commit.
- No Linux/Windows TotalCross build, SDK rebuild, packaging, QEMU, or
  emulation was used. Existing earlier macOS production validation is not
  repeated because this continuation changes measurement artifacts only.

- Blockers: none.
- Deliberate out-of-scope local files: unrelated untracked repository artifacts
  and generated outputs shown by scoped status; do not stage them.
- Resume command: run the final scoped audit, record the resulting
  `git rev-parse HEAD`, and leave the branch unmerged and unrebased.
