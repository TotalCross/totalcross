<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Evidence — raster policy audit and reuse

## 2026-09-18 — bootstrap

- Branch: `perf/image-decode-distributed-benchmark`.
- Plan 2 ancestry and required bootstrap artifacts were verified.
- Unrelated local files remain unstaged.
- No native build or benchmark ran at bootstrap.

## 2026-09-19 — M1 correction rerun

Implementation HEAD: `31bdef4bf`.

Implementation commits for the correction are `feea98e56`
(`fix(benchmark): report complete Skia target metrics`) and `31bdef4bf`
(`fix(benchmark): validate full software target dimensions`). The plan was
compacted in `5d4b3a300`.

The complete gate passed: SDK distribution, macOS ARM64 `tcvm`,
`Launcher`, `skia_surface_test`, native surface test, bundle packaging,
self-test, six accounting-on smokes, and the exact 20-process
`write-pixels-accounting-ab` matrix. Corpus self-test hash:
`588a7e0f4019424a`. SDK compile/deploy JAR hash:
`4c1d1a70069dae3c8cc5e11b77eb55d69c300793a896726dfb27bf3b8538e711`.
Result ZIP hash:
`9f2d6ee03838b30814f1b76830b1fa14b80dc30929e5b02d4f58263f134c5264`.

The corrected environment reports software `1080x1920`, effective rowBytes
`4320`, Skia color type `6` (`BGRA8888`), alpha `2`, `kN32=4`, and
backend `software`. The previous `56x896` was the result of truncating the
real `1080x1920` dimensions into 10-bit packed fields; it is not combined
with the corrected evidence.

Five-round median work P50 is `4250584 -> 3457375` on
(`-18.66%`) and `4253458 -> 3464666` off (`-18.54%`). Work P95 is
`6011834 -> 6122042` on (`+1.83%`) and
`5856083 -> 6453292` off (`+10.20%`). Work P99/MAX increase in both
modes; corrected off-mode work P99 is `26478916 -> 28147333`, delta
`+1668417` (`+6.30%`). Accounting-off retains timing and reports diagnostics
unavailable.

Mask `4` is the no-bit-1 intrinsic JPEG proof: its off smoke recorded 175
intrinsic-known results and 2 fallback scans overall. Mask `32799` includes
`RASTER_OPACITY_METADATA` (bit 1), so its off smoke is separate: 175
intrinsic-known, 177 metadata-known, and 0 fallback scans. The six smokes and
all 20 A/B processes passed validation.

## Historical evidence preserved

The prior M1 package and result archive recorded physical metrics as `56x896`
and earlier P50 deltas of -19.52% on and -19.16% off. Those are real prior
results, but their target dimensions were transport-truncated. The correction
rerun is authoritative for target metrics and timing after the source fix.

## Historical commit-message findings

All Plan 3 commits with post-commit lines over 80 characters are recorded
without history rewriting: `82f4af600` (266),
`ed09cf657` (141), `135050818` (102), `1f5bdbbea` (90 and 122),
`f02f9dcff` (273), and `1fb0b1d35` (86). New commits are signed and
message-line compliant.

## Review boundary

`STOP / REVIEW 1` is technically approved and closed. M2 is the next
authorized stage, but was not started in this documentation closure. No
optimization policy, cache identity, materialization timing, default mask, or
shared slot has been changed.
