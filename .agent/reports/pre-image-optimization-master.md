<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Pre-image optimization master stabilization report

## Technical outcome

This report records the technical outcome of the pre-image-optimization
stabilization work. The branch established the adaptive JPEG metadata
contract, added durable regressions, investigated the legacy Skia byte-swap
policy, measured portable and native implementations, inspected optimized
code generation, and consolidated the endian boundary used by Skia.

The work remains separate from later image-optimization features. Temporary
benchmark workflows, standalone benchmark source, raw CSV/JSON records,
code-generation dumps, state/evidence files, and intermediate execution
documents are not part of the production history.

## Adaptive JPEG correctness

Adaptive JPEG decode remains limited to safe smooth-scaling pipelines. A
targeted decode preserves the decoder's physical raster dimensions while
retaining the encoded source's logical dimensions. Its presentation scale is
derived from the selected JPEG decode denominator, so fresh materialization and
cached backing reconstruction expose equivalent metadata.

Backing publication is transactional: a transient initialization failure does
not install an unusable decoded backing, and a retry can materialize normally.
Nearest scaling and rotate-scaling continue to force a complete decode, and
explicit JPEG factories remain eager. Compatibility aliases retain their
actual smooth-scaling behavior.

Permanent regression coverage includes denominator 2/4/8 behavior, fresh and
cached backing reuse, metadata invariants, smooth-scaling families, the
hardware-scaling alias, alpha, crop, chained smooth scaling, direct drawing,
readback, odd dimensions, denominator boundaries, and full-decode fallbacks.
Visual comparisons use an independently forced complete-decode reference.

## Endian implementation and Skia integration

The shared type boundary now provides fixed-width 16-bit and 32-bit swap
helpers. The public forced-swap macros evaluate their arguments once and pass
`uint16`/`uint32` values to reusable helpers. Compiler capability checks select
supported compiler operations, MSVC uses its guarded byte-swap intrinsics, and
the canonical inline shift/mask implementation remains the portable fallback.

Skia includes the shared type utility and uses the shared forced-swap contract
for surface conversion. Duplicate Skia defaults were removed where the shared
configuration is authoritative. Rendering, windowing, and unrelated Skia
policy defaults remain unchanged; Skia no longer owns a separate byte-swap
implementation or native-swap policy.

## Benchmark and code-generation conclusions

The investigation used direct buffer-level 16-bit and 32-bit loops with
identical work, deterministic measurement order, warmups, sample counts, and
checksum checks. The useful conclusions were:

* Clang on macOS/Android ARM64 and GCC on Linux ARM64 generally generated
  equivalent vectorized byte-reversal instructions for the competing forms,
  producing near-parity results in most workloads.
* GCC on Linux x86-64 produced scalar `bswap` code for most 32-bit loops. The
  canonical helper was the material outlier in the recorded round while the
  compiler operation stayed near parity. The direct-loop design removed the
  earlier indirect-dispatch and code-layout confounder.
* MSVC on Windows x86-64 and ARM64 generated vectorized byte-reversal code for
  the compiler-operation path, explaining its substantial advantage there,
  particularly for 16-bit reversal on Windows.

These observations are workload-specific evidence. They support a shared
compiler-capability boundary and do not justify retaining benchmark machinery
or a second Skia-specific runtime policy in production.

## SDL2/SDL3-informed decision

The SDL2/SDL3 implementation guidance aligns modern compiler selection with
`__builtin_bswap16`/`__builtin_bswap32` for GCC and Clang,
`_byteswap_ushort`/`_byteswap_ulong` for MSVC, and a canonical inline
shift/mask implementation as the portable fallback. This matches the final
shared compiler-capability design: compiler-specific operations are selected
at the common endian boundary, while the portable implementation remains
available when those capabilities are absent.

That alignment supported removing the Skia-specific swap policy without
changing SDL windowing or rendering behavior. The macOS integration path uses
the repository's SDL-backed Skia surface target, while the standalone endian
checks and historical byte-swap measurements remain independent of SDL, TCVM,
and Skia.

## Validation and limitations

The stabilization validation passed with:

* focused `totalcross.ui.image.*` SDK tests;
* the macOS SDL/Skia surface configure, build, and execution path;
* representative C99 and C++17 consumers, including single-evaluation checks;
* focused copyright-header validation and `git diff --check`.

The detailed native benchmark and code-generation evidence was collected
during the technical investigation. This documentation cleanup did not rerun
benchmarks, restore raw samples, or claim a new cross-platform measurement.
The full release and platform matrix remains outside this focused stabilization
handoff; the report preserves conclusions and limitations rather than
presenting historical measurements as fresh results.

## Final outcome

The master stabilization provides a tested adaptive-JPEG contract, durable
image regressions, and one shared endian policy boundary with compiler-aware
implementations and a portable fallback. The production tree contains no
temporary benchmark workflow or source, and later image-optimization branches
can build on the documented contracts without inheriting the discarded
execution noise.
