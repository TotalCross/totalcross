<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization phase 3 handoff

This factual handoff is updated at Phase-3 milestone completion. It will
distinguish delivered compact backing behavior, measured benchmark results,
supported validation scope, limitations, and disabled defaults.

## Bootstrap

Phase 3 starts from the accepted Phase-2 runtime at
`86bfeafe388ce866236c3ae58eecb144664895e2` on branch
`perf/image-opt-phase3-formats`. The implementation checkpoint is
`fb5718cb2`; final harness corrections are `94519f0b8`, `bbd364b32`, and
`119ab421c`.

## Delivered behavior

- Opaque non-grayscale JPEG/PNG sources can use RGB565.
- Structurally grayscale, non-alpha JPEG/PNG sources can use GRAY8.
- Alpha-bearing PNG sources can use premultiplied ARGB4444.
- Precedence is GRAY8 > RGB565 > ARGB4444 > RGBA8888.
- Compact backing is source-only; mutation promotes transactionally to RGBA8888.
- Compact observers use row-wise RGBA conversion; direct decode reports zero
  temporary full-size RGBA staging bytes.
- Decode and promotion allocation-failure retries preserve ownership and the
  original compact backing.

## Measured results

All final matrices contain 60 samples with stable full input/output hashes and
sample CV below 5%. Exact Phase-2 S1 artifacts are retained for each isolated
format, promotion, and combined workload. Because the isolated GRAY8 and
ARGB4444 workloads were doubled after the first baseline to satisfy the frozen
30 ms floor, matched final-harness `s1-matched` controls are also retained and
used for S2 regression comparisons.

| Workload | Matched S1 median | S2 median | S3 median | S3 result |
| --- | ---: | ---: | ---: | --- |
| RGB565 | 62 ms | 63 ms | 35 ms | RGB565, 2 B/px |
| GRAY8 | 111 ms | 110 ms | 48 ms | GRAY8, 1 B/px |
| ARGB4444 | 61 ms | 61 ms | 46 ms | ARGB4444, 2 B/px |
| Promotion | 63 ms | 63 ms | 60 ms | all three promote once |
| Combined, Phase 2 off | 264 ms | 264 ms | 185 ms | all three compact formats |
| Combined, Phase 2 on | 160 ms | 160 ms | 169 ms | 128 write-pixel hits + 32 ARGB fallbacks |

RGB565, GRAY8, and ARGB4444 S3 runs report zero temporary RGBA decode bytes and
zero promotions. RGB565 quality is model max 1; GRAY8 output is exact; ARGB4444
model max/RMSE is 0 with black/white composite max error 16.

## Validation scope and limitations

Passed: Release software-Skia native build, SDK `dist -x test`, compact format
smoke, observer non-promotion, transactional promotion failure/retry, decode
failure/retry, exact RGB565 write-pixels parity, isolated matrices, promotion,
and both combined matrices. Android, iOS, Windows, Linux, GPU, and full
cross-platform packaging remain out of scope for this phase.

The detached true-base CMake configure was deferred because the pinned qrcodegen
asset returned HTTP 404. The native source tree was byte-equivalent to the
current branch, and its existing Release macOS dylib was used for exact-base
S1 capture.

Compact formats remain internal opt-in features and are disabled by default.
