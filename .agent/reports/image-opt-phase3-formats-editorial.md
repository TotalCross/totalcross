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
`fb5718cb2`; the corrective runtime and correctness checkpoints are
`37746781b` and `6fcb50a37`. The exact true-base dylib SHA-256 is
`32926d24c475ca3b6f04134ce4d6556c37d926862d6877d122cdec517213c4ca`; the final
dylib SHA-256 is `2864d0ee3ace6d52729bcaccad727902088327caa2bafe0769e66cbc2c0a9caa`.

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
| Combined, Phase 2 on | 160 ms | 161 ms | 162 ms | 128 write-pixel hits + 32 ARGB fallbacks |

RGB565, GRAY8, and ARGB4444 S3 runs report zero temporary RGBA decode bytes and
zero promotions. RGB565 quality is model max 1; GRAY8 output is exact; ARGB4444
model max/RMSE is 0 with black/white composite max error 16. The final
combined-enabled S3 median improved from 169 ms to 162 ms after the direct
row-conversion correction.

## Corrective 200-sample RSS gate

The matched final-harness peak RSS pairs were:

| Workload | S1 KiB | S2 KiB | Delta |
| --- | ---: | ---: | ---: |
| ARGB4444 | 127088 | 139456 | +9.7% |
| Promotion | 153216 | 153744 | +0.3% |
| Combined, Phase 2 off | 147904 | 151504 | +2.4% |
| Combined, Phase 2 on | 157568 | 152496 | -3.2% |

ARGB4444 exceeded the raw threshold and therefore has required `vmmap -summary`
and `ps` captures at samples 100 and 150. S2 current physical footprint was
higher at those snapshots, but its peak physical footprint was lower (81.3M vs
84.4M) and the difference was concentrated in allocator residency; backing
formats, output hashes, and counters were identical. This is documented as an
unconfirmed sampled-RSS signal, not a confirmed live compact-backing leak.

## Validation scope and limitations

Passed: Release software-Skia native build, SDK `dist -x test`, compact format
smoke, observer non-promotion, transactional promotion failure/retry across all
three formats, decode failure/retry across all five fixtures, exact RGB565 and
GRAY8 draw parity, ARGB4444 translucent fallback, isolated matrices, promotion,
both combined matrices, and the 200-sample RSS gate. Android, iOS, Windows,
Linux, GPU, and full cross-platform packaging remain out of scope for this
phase.

The detached true-base CMake configure was deferred because the pinned qrcodegen
asset returned HTTP 404. The native source tree was byte-equivalent to the
current branch, and its existing Release macOS dylib was used for exact-base
S1 capture.

Compact formats remain internal opt-in features and are disabled by default.
The implementation was largely delivered in one runtime slice before the
per-format S1 captures; exact-base and matched-control evidence remain separate
and are not conflated.
