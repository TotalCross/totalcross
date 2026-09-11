<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization phase 3 handoff

This factual handoff is updated at Phase-3 milestone completion. It will
distinguish delivered compact backing behavior, measured benchmark results,
supported validation scope, limitations, and disabled defaults.

## Prior documented handoff (historical)

The prior history correction replayed Phase 3 directly on the then-accepted
Phase-2 runtime at
`4d3177801a29752bc3e7b17754400001fef6270f` on branch
`perf/image-opt-phase3-formats`. The rebased implementation checkpoint is
`5dbadf3bb7ea44ee14472ba25ab2b27b2fb3a2b9`; the corrective runtime and
correctness checkpoints are `a212f76e64c5415af8d6a3843e87ce82030db94e` and
`4e52067003b899566de67f01a9f50000326cbd98`. The final rebased Phase-3 HEAD is
the tip of `perf/image-opt-phase3-formats` after the documentation-only
history correction. The exact true-base dylib SHA-256 is
`32926d24c475ca3b6f04134ce4d6556c37d926862d6877d122cdec517213c4ca`; the final
dylib SHA-256 is `2864d0ee3ace6d52729bcaccad727902088327caa2bafe0769e66cbc2c0a9caa`.

The benchmark results in this handoff are historical pre-rebase evidence:
their exact-base controls ran on `86bfeafe388ce866236c3ae58eecb144664895e2`
and their final runtime measurements used the original pre-rebase Phase-3
checkpoints. The production and permanent-test tree was replayed without
rerunning the full benchmark matrix or changing those measurements.

## Exact-base rebase handoff

On 2026-09-07, the branch was rebased from old HEAD
`9d5c6133318f97baeb88d382cb7837c3f585f122` onto the frozen Phase-2 SHA
`6d1c95f77fcb9c74d19b4e9393dba7c82cd37aee`, verified from
`origin/perf/image-opt-phase2-raster`. The new Phase-3 implementation HEAD is
`15ab7d72e28f860f67106796bdd4cd7329075e56`; all 21 Phase-3 commits remain in
the original order. Rebase conflicts were resolved by retaining the finalized
Phase-2 raster architecture and replaying the genuine compact-format changes.

Focused `totalcross.ui.image.*` tests, SDK `dist -x test`, the Release macOS
software-Skia build, and `runImageCompactFormatsSmokeMacOS` passed against the
rebased tree. No Phase-3 benchmarks were run for this handoff.

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

ARGB4444 exceeded the raw threshold in the historical single pair and therefore
received the required `vmmap -summary` and `ps` captures. The authoritative
corrected-harness recheck is under
`argb4444/rss-200-corrective-3pairs-matched-harness/`: alternating S2/S1 peak
RSS deltas were `-0.7%`, `+7.1%`, and `-4.3%`; matched S2 peak physical-footprint
deltas were `-4.9%`, `-2.6%`, and `+2.0%`. Current physical/private writable
residency changed with run order and allocator/page state, so the frozen rule
rejects a reproducible disabled-path regression. Runtime source was unchanged.

## Validation scope and limitations

Passed: Release software-Skia native build, SDK `dist -x test`, compact format
smoke, observer non-promotion, transactional promotion failure/retry across all
three formats, decode failure/retry across all five fixtures, exact RGB565 and
GRAY8 draw parity, ARGB4444 translucent fallback, isolated matrices, promotion,
both combined matrices, and the 200-sample RSS gate. Milestone 9B additionally
passed the physical Android GPU smoke and the existing iOS, Windows, and Linux
platform build jobs. Hosted Windows/Linux performance remains unavailable.

The detached true-base CMake configure was deferred because the pinned qrcodegen
asset returned HTTP 404. The native source tree was byte-equivalent to the
current branch, and its existing Release macOS dylib was used for exact-base
S1 capture.

Compact formats remain internal opt-in features and are disabled by default.
The implementation was largely delivered in one runtime slice before the
per-format S1 captures; exact-base and matched-control evidence remain separate
and are not conflated. Phase 4 starts from the final Phase-3 branch tip and was
not started here.

## Milestone-9 final closeout

### Delivered behavior and supported validation

The final runtime candidate is `d2f8195b9faf828f4ee04154c93a93e1136c5bd4`,
with frozen Phase-2 parent `6d1c95f77fcb9c74d19b4e9393dba7c82cd37aee`, last
9A runtime correction `8b455f2ad`, and adapter digest
`f2292c73893fb1568c3ba5e56803f4fb0b37d3ff5b7f193f9a992a3e91472f42`. The
existing GitHub PR matrix passed at exact head in run
`34190415679`, including SDK, macOS ARM64, Android, iOS archive, Windows,
Windows native/legacy, Linux amd64, and Linux ARM64.

The physical Android smoke passed on Xiaomi 2312DRA50G / Android 13 / MIUI
V14.0.9.0.TNRMIXM with Adreno 710 OpenGL ES 3.2. It selected all expected
compact formats, decoded directly into `2097152` compact bytes, preserved
observer hashes and quality, completed a real MainWindow screen draw, and kept
raster, promotion, and temporary-RGBA counters at zero. The diagnostic package
was removed after validation.

### Final measured delta

Fresh Matrix A isolated medians were `266/264/184` ms for S1/S2/S3, with S2
elapsed/RSS deltas `-0.75%/+0.81%`. Fresh Matrix B full-stack medians were
`162/159/163` ms, with first S2 deltas `-1.85%/+9.65%`. The required 200-sample
Matrix B escalation produced `162/161` ms and `-0.62%/+8.60%`; matched
`vmmap -summary`/`ps` checkpoints did not reproduce monotonic private growth,
so the RSS signal remains unconfirmed allocator/residency variation and no
runtime change was made.

### Limitations and human review

Hosted Windows/Linux S1/S2/S3 performance is `NOT AVAILABLE`, not failed; the
existing platform builds provide portability evidence only. Android provides
GPU correctness evidence, not timing or RSS evidence. S3 has no speedup
requirement, and live compact byte counters are GC-sensitive. Human review
should confirm the intended opt-in/default-off release policy and accept the
separate runtime-candidate and final-docs-tip provenance before Phase 4 starts.
