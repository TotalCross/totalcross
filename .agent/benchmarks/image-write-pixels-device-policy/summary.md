<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Device-space writePixels policy

## Artifact

- Implementation HEAD: `5265f38a6`.
- Target: macOS ARM64, Skia, Release.
- SDK JAR SHA-256: `5fe1876a875232de6e39aa59e26f0067d1c9aac9a1a975c9d709877378962d9f`.
- Native `libtcvm.dylib` SHA-256: `76d10a90851b450c0c8759deadfb6f01547562c3912c4e3953b819acc3f50163`.
- Corpus: 663 JPEGs; self-test hash `588a7e0f4019424a`.
- Raw results: `build/image-write-pixels-policy-02-package/image-scroll-benchmark-macos-arm64/results/`.
- Result ZIP: `results/totalcross-image-benchmark-results-1789781883290430000.zip`.
- Result ZIP SHA-256: `860a0bfdd9d093bd331f9519a26ac264065d1b4d6d1232f6534cb81154353570`.
- Bundle ZIP: `build/image-write-pixels-policy-02-package/image-scroll-benchmark-macos-arm64.zip`.
- Bundle ZIP SHA-256: `e6ee75d24942968308a828d231ad603c2ec3e57aec4a199d2e40cf5586831e2e`.

The result ZIP contains the complete bundle and the raw run artifacts. The
runner's self-test hash is relative to the bundle corpus root; the package
manifest also records the staged `imag`-relative hash `af39fea695191a27`.

## Matrix status

Exactly 24/24 benchmark processes passed: six masks, two prefetch profiles, and
two rounds. The active metric is `work_time_ns`; values below are milliseconds.
Each `R` cell is control→enabled for P50/P95. Median deltas are computed over
the two rounds. Paint values are also medians over the two rounds.

| Pair | Prefetch | R1 work P50/P95 | R2 work P50/P95 | Median work P50 | Median work P95 | Median paint P50 | Median paint P95 | Variance |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| 0→4 | off | 379.106→370.597 / 382.918→431.062 | 370.245→369.412 / 374.351→375.769 | -4.671 ms (-1.25%) | +24.781 ms (+6.54%) | -4.670 ms (-1.25%) | +24.783 ms (+6.55%) | consistent |
| 0→4 | on | 5.854→3.661 / 6.365→4.852 | 5.825→3.658 / 6.488→8.645 | -2.180 ms (-37.33%) | +0.322 ms (+5.01%) | -2.178 ms (-37.45%) | +0.167 ms (+2.61%) | consistent |
| 2→6 | off | 356.976→357.588 / 364.291→364.557 | 356.945→354.915 / 360.926→361.636 | -0.709 ms (-0.20%) | +0.488 ms (+0.13%) | -0.695 ms (-0.19%) | +0.489 ms (+0.13%) | inconclusive |
| 2→6 | on | 4.082→3.295 / 4.966→8.307 | 4.112→3.367 / 5.136→7.776 | -0.766 ms (-18.69%) | +2.991 ms (+59.21%) | -0.765 ms (-18.77%) | +2.965 ms (+58.84%) | consistent |
| 32795→32799 | off | 350.207→356.660 / 356.895→362.061 | 352.167→358.076 / 361.676→364.399 | +6.181 ms (+1.76%) | +3.944 ms (+1.10%) | +6.184 ms (+1.76%) | +3.947 ms (+1.10%) | consistent |
| 32795→32799 | on | 4.113→3.398 / 5.268→8.650 | 4.246→3.328 / 5.359→7.666 | -0.817 ms (-19.54%) | +2.845 ms (+53.54%) | -0.816 ms (-19.61%) | +2.824 ms (+53.30%) | consistent |

The `2→6` cold pair is `INCONCLUSIVE_VARIANCE` because its two P50 rounds
disagree in direction. No third matrix was run.

## Device-copy accounting

The table reports medians across the two matrix rounds for each enabled mask
and prefetch profile. Candidate-to-hit conversion is `regular hits / device
1:1 candidates`.

| Mask | Prefetch | Regular attempts | Hits | Fallbacks | Hit rate | Candidates | Known opaque | Conversion | Clipped hits |
| ---: | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 4 | off | 231.5 | 208.5 | 23 | 90.1% | 208.5 | 42 | 100% | 0 |
| 4 | on | 3411 | 3411 | 0 | 100% | 3411 | 2766 | 100% | 0 |
| 6 | off | 240 | 216 | 24 | 90.0% | 216 | 216 | 100% | 0 |
| 6 | on | 3406.5 | 3406.5 | 0 | 100% | 3406.5 | 3406.5 | 100% | 0 |
| 32799 | off | 240 | 216 | 24 | 90.0% | 216 | 216 | 100% | 0 |
| 32799 | on | 3405 | 3405 | 0 | 100% | 3405 | 3405 | 100% | 0 |

All matrix rows preserved `regular attempts = hits + fallbacks` and
`writePixelsRejectSaveCount = 0`. Real scrolling produced zero clipped hits;
the native correctness fixture remains the proof for partial device clips and
recorded two clipped hits. Median JPEG decode time is included in the raw
`summary.csv`; it was unchanged in scope and is context only, not the primary
metric.

## Scope and conclusion

The delivered policy is a safe positive device-space scale/translate fast path
with explicit device clipping and source-subset copying. Skew, rotation,
perspective, fractional physical boundaries, alpha, and non-opaque cases fall
back. Physical-variant caching, JPEG/storage/resampling policies, and prefetch
semantics were not changed.
