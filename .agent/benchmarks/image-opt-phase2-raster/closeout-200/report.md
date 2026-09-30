<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Phase-2 raster 200-sample closeout report

## Regime and identity

All runs used macOS Release software Skia with three warmups, 200 measured
samples, and RSS sampling every 50 ms. The host was Darwin 25.5.0 / macOS
26.5.2, arm64, with 17,179,869,184 bytes of RAM. Every benchmark process
exited successfully and recorded exactly 200 samples.

S1 uses the true Phase-1 runtime
`9545c18207fab74d81340b24825c5a82ddbda7fd`. Its benchmark source was adapted
by the committed script in `../true-base-harness/` from source revision
`f66561b99c7b06ee1f7e7d627583de7066c39f0a`; the adapter digest is
`cbfada1ba5f95c27005c473d1f7f128e644adf8f8496d8573bf8beeb1dc08a19`.
The adapter adds only benchmark compatibility/counter shims and deployment
inputs; it does not copy Phase-2 runtime code. S2 explicitly disables all
five Phase-2 raster features. S3 enables exactly those five features.

The integrated fixture is generated once before scenario configuration. The
full-content FNV-1a 64-bit input hashes are identical in all three canonical
scenarios:

- JPEG `lena512.jpg`: `0000F4870000A84E` (`90,385` bytes; source SHA-256
  `df0035534c5c12d024763dce5f515023ad7e16139f4278b360291b2f0261fb51`)
- generated opaque 512x512 PNG: `0000900B0000BBA9`

This ordering prevents optimization state from affecting PNG generation and
the recorded per-sample fields prove byte identity across S1/S2/S3.

## Canonical S1/S2 results

| Workload | S1 median / P95 | S2 median / P95 | S1 RSS | S2 RSS | S2 vs S1 RSS |
| --- | ---: | ---: | ---: | ---: | ---: |
| Opacity JPEG | 37 / 38 ms | 37 / 38 ms | 151,344 KiB | 148,512 KiB | -1.87% |
| Readback pixels | 45 / 46 ms | 45 / 46 ms | 201,008 KiB | 192,688 KiB | -4.14% |
| Readback encode | 462 / 464 ms | 463 / 465 ms | 199,488 KiB | 183,344 KiB | -8.09% |
| Readback color | 36 / 37 ms | 36 / 37 ms | 187,552 KiB | 184,144 KiB | -1.82% |
| Integrated | 878 / 883 ms | 874 / 879.05 ms | 116,176 KiB | 122,288 KiB | +5.26% |

The opacity and readback output hashes are equal between S1 and S2 for every
corresponding workload. The compressed raw samples and runner summaries are
under `true-base-s1/` and `final-s2/`.

## Integrated S1/S2/S3 result

The canonical integrated medians/P95s/RSS are:

| Scenario | Median | P95 | Peak RSS |
| --- | ---: | ---: | ---: |
| S1 true base | 878 ms | 883 ms | 116,176 KiB |
| S2 all disabled | 874 ms | 879.05 ms | 122,288 KiB |
| S3 all enabled | 56 ms | 59.05 ms | 126,560 KiB |

S2 is -0.46% in median and -0.45% in P95 versus S1. S3 is -93.62% in median
and -93.31% in P95 versus S1. S3 is +3.49% RSS versus S2 and +8.94% versus
S1; enabled-feature memory is reported separately and is not a disabled-path
acceptance gate.

The output hashes are identical in all three canonical scenarios:

| Output | Hash |
| --- | --- |
| Target pixels | `00006C3E000043B0` |
| Encoded PNG | `0000675B00005B57` |
| Color materialization | `00002DBA0000C894` |

The final S3 counter row proves the enabled workload executed every target:
400 zero-copy decodes, 200 source opacity proofs, 200 decode opacity proofs,
204,800 writePixels attempts/hits with zero fallbacks, 307,200 row readbacks,
zero full readbacks, and 200 direct color materializations.

## RSS boundary diagnosis

The integrated S2/S1 RSS comparison is above 5% in the canonical run and was
repeated. The repeat true-base S1 was 118,416 KiB and the repeat final S2 was
125,504 KiB. This confirms a cross-runtime-revision RSS shift for this
integrated workload, but does not identify a disabled-feature-only overhead.

As a control, the final runtime with its default `pre` settings measured
118,704 KiB in one run and 124,320 KiB in the repeated run; explicit
`post-disabled` measured 122,288 KiB and 125,504 KiB. The repeated final
`pre`/`post-disabled` pair differs by only 0.95%, with identical 874 ms
medians. The +5–6% S2/S1 delta is therefore not reproducibly attributable to
the explicit disabled mask. No runtime fix was applied because no disabled
path allocation or timing overhead was demonstrated. The extra diagnostic
runs are retained in `diagnostic-final-pre/`, `repeat-final-pre/`,
`repeat-final-s2/`, and `repeat-true-base-s1/`.

## Artifact map and limitations

Canonical compressed samples are in `true-base-s1/`, `final-s2/`, and
`final-s3/`; runner summaries are adjacent. The previous historical 60-sample
artifacts and the prior adapter digest remain untouched under
`../corrections/`. This closeout does not recapture decode or opaque-draw S3;
the requested 200-sample gate covered opacity JPEG, readback pixels/encode/
color, and the integrated workload. Android, iOS, Windows, Linux, and GPU
validation remain outside the phase contract.
