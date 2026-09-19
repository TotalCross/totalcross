<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Milestone 1 — writePixels accounting A/B

Date: 2026-09-19

## Correction rerun and gate

The authoritative rerun used implementation HEAD `31bdef4bf` and the
macOS ARM64 bundle
`build/image-raster-policy-03-package-fixed/image-scroll-benchmark-macos-arm64`.
It used the required 663-file corpus with self-test hash
`588a7e0f4019424a`. SDK compile/deploy JAR SHA-256 was
`4c1d1a70069dae3c8cc5e11b77eb55d69c300793a896726dfb27bf3b8538e711`.
The result ZIP SHA-256 was
`9f2d6ee03838b30814f1b76830b1fa14b80dc30929e5b02d4f58263f134c5264`.

The complete build gate passed: SDK distribution, macOS ARM64 `tcvm`,
`Launcher`, `skia_surface_test`, native surface test, package creation,
self-test, six accounting-on smokes, and the exact 20-process A/B matrix.

The native target metrics are now untruncated and structurally coherent:

- physical target: `1080x1920`;
- effective software pitch/rowBytes: `4320`;
- Skia color type: `6`, classification `BGRA8888`;
- alpha type: `2`;
- `kN32_SkColorType`: `4`;
- backend: `software`.

The former `56x896` report was not a physical target. It was the lower
10-bit portion of the packed `1080x1920` dimensions. The correction bridge
reports scalar metrics without truncation and uses the supplied software pitch.

Six smokes passed for masks `0`, `4`, and `32799`, each with prefetch
off/on and accounting on. Mask `4` is the no-bit-1 intrinsic JPEG proof.
Mask `32799` includes `RASTER_OPACITY_METADATA` (bit 1), so it is reported
separately and is not described as bit 1 disabled.

The `4/off` smoke recorded 175 intrinsic-known opacity results and 2
fallback scans overall; the bit-1-disabled JPEG candidates therefore do not
depend on metadata. The `32799/off` smoke recorded 175 intrinsic-known,
177 metadata-known, and 0 fallback scans. The `4/on` smoke produced 3411
writePixels attempts/hits, zero fallbacks, and 3408 known-opaque candidates.

## Raw scroll samples

The exact `write-pixels-accounting-ab` profile used masks `32795` and
`32799`, prefetch on, accounting on/off, and five interleaved rounds.
All values are nanoseconds; `work_*` is primary and paint is context.

| accounting | mask | round | work P50 | work P95 | work P99 | work MAX | paint P50 | paint P95 |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| off | 32795 | 1 | 4313417 | 5429958 | 25868209 | 31373834 | 4288208 | 5407958 |
| off | 32799 | 1 | 3557084 | 6453292 | 26900375 | 35177375 | 3531166 | 6432875 |
| off | 32795 | 2 | 4253458 | 6307583 | 32448583 | 39941750 | 4232834 | 6286833 |
| off | 32799 | 2 | 3464666 | 8542625 | 31262875 | 53036959 | 3431833 | 8490500 |
| off | 32795 | 3 | 4245750 | 5743041 | 27290125 | 31281208 | 4226875 | 5740417 |
| off | 32799 | 3 | 3520208 | 5971750 | 29654042 | 32904375 | 3499250 | 5950792 |
| off | 32795 | 4 | 4290708 | 8356834 | 26212250 | 30619208 | 4265459 | 8334292 |
| off | 32799 | 4 | 3413750 | 6755708 | 27905042 | 30776125 | 3388667 | 6734833 |
| off | 32795 | 5 | 4227875 | 5856083 | 26478916 | 29855250 | 4205625 | 5832291 |
| off | 32799 | 5 | 3384417 | 4582250 | 28147333 | 29203000 | 3358083 | 4560208 |
| on | 32795 | 1 | 4210000 | 4796500 | 26707916 | 29574833 | 4186708 | 4775333 |
| on | 32799 | 1 | 3491000 | 4982042 | 27140000 | 33984084 | 3470000 | 4958250 |
| on | 32795 | 2 | 4275208 | 6296750 | 25494042 | 29974834 | 4252917 | 6255208 |
| on | 32799 | 2 | 3457375 | 6122042 | 30169792 | 30592959 | 3433166 | 6120042 |
| on | 32795 | 3 | 4299959 | 8111583 | 31031667 | 45339292 | 4277333 | 8087125 |
| on | 32799 | 3 | 3398500 | 4663000 | 34216125 | 43135875 | 3377583 | 4647375 |
| on | 32795 | 4 | 4243208 | 6011834 | 25401333 | 31227000 | 4210667 | 5993750 |
| on | 32799 | 4 | 3473291 | 7701375 | 26967708 | 33452250 | 3449750 | 7676958 |
| on | 32795 | 5 | 4250584 | 4924250 | 25715667 | 31395333 | 4221834 | 4899000 |
| on | 32799 | 5 | 3431250 | 7369042 | 25817375 | 32189833 | 3408208 | 7323417 |

## Five-round summaries

The delta is `32799 - 32795`; negative is faster. Each value is the median
of five per-process percentile values.

| accounting | metric | 32795 | 32799 | delta | delta % |
| --- | --- | ---: | ---: | ---: | ---: |
| on | work P50 | 4250584 | 3457375 | -793209 | -18.66% |
| on | work P95 | 6011834 | 6122042 | 110208 | +1.83% |
| on | work P99 | 25715667 | 27140000 | 1424333 | +5.54% |
| on | work MAX | 31227000 | 33452250 | 2225250 | +7.13% |
| on | paint P50 | 4221834 | 3433166 | -788668 | -18.68% |
| on | paint P95 | 5993750 | 6120042 | 126292 | +2.11% |
| off | work P50 | 4253458 | 3464666 | -788792 | -18.54% |
| off | work P95 | 5856083 | 6453292 | 597209 | +10.20% |
| off | work P99 | 27290125 | 28147333 | 857208 | +3.14% |
| off | work MAX | 31281208 | 32904375 | 1623167 | +5.19% |
| off | paint P50 | 4232834 | 3431833 | -801001 | -18.92% |
| off | paint P95 | 5832291 | 6432875 | 600584 | +10.30% |

The accounting-on P50 improvement is 18.66%; accounting-off is 18.54%.
Accounting-on P95 is effectively flat in this rerun (+1.83%), while
accounting-off increases 10.20%. Tails increase in both modes. These are
five-round distributions, not a single-process causal claim.

With accounting on, `32795` had no writePixels attempts and `32799` had
3402–3408 attempts per run, all hits and zero fallbacks. Accounting-off
artifacts contain timing but explicitly mark diagnostics unavailable.

## Historical superseded record

The earlier package reported `56x896`, with the same rowBytes `4320`.
That result is preserved as a real historical observation but superseded as
target-format evidence because both dimensions were packed into 10-bit fields.
Its earlier five-round P50 deltas were -19.52% (accounting on) and -19.16%
(accounting off). It must not be combined with this correction rerun.

## Stable artifacts

- SDK log: `/tmp/image-raster-policy-03-package-sdk-fixed.log`
- native build log: `/tmp/image-raster-policy-03-target-metrics-native-build.log`
- self-test log: `/tmp/image-raster-policy-03-self-test-fixed.log`
- smoke log: `/tmp/image-raster-policy-03-smokes-fixed.log`
- matrix log: `/tmp/image-raster-policy-03-accounting-ab-fixed.log`
- summary: `build/image-raster-policy-03-package-fixed/image-scroll-benchmark-macos-arm64/results/summary.csv`
- result archive: `build/image-raster-policy-03-package-fixed/image-scroll-benchmark-macos-arm64/results/totalcross-image-benchmark-results-1789791566290091000.zip`
