<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Milestone 1 — writePixels accounting A/B

Date: 2026-09-19

The packaged macOS ARM64 bundle used the required 663-file corpus. Bundle
self-test passed with dataset hash `588a7e0f4019424a`. The 20-process matrix
used five interleaved rounds of masks `32795` and `32799`, prefetch `on`, and
accounting `on`/`off`.

## Build and contract gate

- `skia_surface_test`: passed.
- Bundle self-test: passed.
- Six accounting-on smokes passed: masks `0`, `4`, and `32799`, each with
  prefetch `off` and `on`.
- The diagnostic `4/off` smoke recorded intrinsic JPEG opacity `157`, source
  metadata opacity `0`, and fallback scans `2`, with bit 1 disabled.
- The diagnostic `32799/off` smoke recorded intrinsic JPEG opacity `170` and
  fallback scans `0`, with bit 1 disabled.
- The prefetch-on `4/on` smoke produced `3411` writePixels hits and `3408`
  known-opaque 1:1 candidates. Its opacity counters are phase-local because
  the benchmark resets them before the scroll pass; the three initial
  non-intrinsic candidates were resolved by the existing fallback path.
- `environment.json` reported the active software target as `56x896`, row
  bytes `4320`, color type `6` (`BGRA8888`), alpha type `2`, and `kN32` type
  `4`. Renderer backend was `software`.

## Raw scroll samples

All timing values are nanoseconds. `work_*` is the primary metric; paint
values are included as context. Accounting-off counters are intentionally
unavailable and are represented by `--`.

| accounting | mask | round | work P50 | work P95 | work P99 | work MAX | paint P50 | paint P95 |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| on | 32795 | 1 | 4334792 | 5499083 | 26270041 | 31160167 | 4314000 | 5477042 |
| on | 32795 | 2 | 4375041 | 7520375 | 27152792 | 30890333 | 4353709 | 7494125 |
| on | 32795 | 3 | 4258292 | 6850000 | 27457292 | 30740750 | 4235250 | 6847208 |
| on | 32795 | 4 | 4230209 | 5955333 | 30708833 | 35261375 | 4208458 | 5928375 |
| on | 32795 | 5 | 4320417 | 11682875 | 27006875 | 31726625 | 4294791 | 11662917 |
| on | 32799 | 1 | 3471667 | 4785500 | 29238958 | 36128125 | 3448042 | 4761583 |
| on | 32799 | 2 | 3477667 | 5092750 | 26832500 | 33722750 | 3455750 | 4970583 |
| on | 32799 | 3 | 3477083 | 11916834 | 33056709 | 33235333 | 3456333 | 11898208 |
| on | 32799 | 4 | 3460417 | 5513250 | 30078083 | 40490000 | 3438125 | 5496750 |
| on | 32799 | 5 | 3485916 | 6205417 | 32961542 | 39802666 | 3460917 | 6183417 |
| off | 32795 | 1 | 4331125 | 6686916 | 26772041 | 32186792 | 4309125 | 6667375 |
| off | 32795 | 2 | 4276250 | 6658375 | 26460334 | 34973834 | 4253666 | 6630042 |
| off | 32795 | 3 | 4282542 | 7544500 | 28882000 | 32687916 | 4258958 | 7524875 |
| off | 32795 | 4 | 4287584 | 5805083 | 26422084 | 31835625 | 4266250 | 5784208 |
| off | 32795 | 5 | 4263792 | 6322500 | 30532041 | 30722208 | 4242458 | 6304959 |
| off | 32799 | 1 | 3507333 | 6376083 | 27391375 | 30613458 | 3486042 | 6372667 |
| off | 32799 | 2 | 3517000 | 8846917 | 25603417 | 32571709 | 3493709 | 8821041 |
| off | 32799 | 3 | 3443334 | 8403292 | 32891375 | 40044375 | 3417834 | 8382833 |
| off | 32799 | 4 | 3444625 | 6587042 | 27726292 | 29925083 | 3423583 | 6561584 |
| off | 32799 | 5 | 3461833 | 7720083 | 34132416 | 37121916 | 3438292 | 7694708 |

## Five-round summaries

The summaries below are medians of the five per-process percentile values.
The delta is `32799 - 32795`; negative is faster.

| accounting | metric | 32795 | 32799 | delta | delta % |
| --- | --- | ---: | ---: | ---: | ---: |
| on | work P50 | 4320417 | 3477083 | -843334 | -19.52% |
| on | work P95 | 6850000 | 5513250 | -1336750 | -19.51% |
| on | work P99 | 27152792 | 30078083 | 2925291 | +10.77% |
| on | work MAX | 31160167 | 36128125 | 4967958 | +15.94% |
| on | paint P50 | 4294791 | 3455750 | -839041 | -19.54% |
| on | paint P95 | 6847208 | 5496750 | -1350458 | -19.72% |
| off | work P50 | 4282542 | 3461833 | -820709 | -19.16% |
| off | work P95 | 6658375 | 7720083 | 1061708 | +15.95% |
| off | work P99 | 26772041 | 27726292 | 954251 | +3.56% |
| off | work MAX | 32186792 | 32571709 | 384917 | +1.20% |
| off | paint P50 | 4258958 | 3438292 | -820666 | -19.27% |
| off | paint P95 | 6630042 | 7694708 | 1064666 | +16.06% |

Bit 2 is disabled in `32795` and enabled in `32799`. The median work P50
improvement is approximately 19% in both accounting modes. The five-round
P95 and tail distributions do not support treating the P95 increase as a
single-run artifact or as a settled regression; accounting-on and accounting-
off show different tail behavior and require later review.

With accounting on, `32795` had no writePixels attempts. `32799` had
`3402–3408` attempts per run, all hits and zero fallbacks. With accounting off,
the run JSON explicitly marked diagnostics unavailable while retaining all
work/paint timing fields.

Stable artifacts:

- self-test log: `/tmp/image-raster-policy-03-self-test-accounting-fix.log`
- smoke log: `/tmp/image-raster-policy-03-smokes-accounting-fix.log`
- matrix log: `/tmp/image-raster-policy-03-accounting-ab-final.log`
- result summary: `build/image-raster-policy-03-package/image-scroll-benchmark-macos-arm64/results/summary.csv`
- result archive: `build/image-raster-policy-03-package/image-scroll-benchmark-macos-arm64/results/totalcross-image-benchmark-results-1789789544107088000.zip`
