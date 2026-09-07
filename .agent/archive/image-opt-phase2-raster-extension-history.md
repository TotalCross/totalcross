<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Phase 2 raster extension history

## 2026-09-07 — Bootstrap

The extension starts from Phase 2 tip
`a225d10165b8b60c4bf7bf2f95f5bf3b395f3a92`, with frozen Phase 1 base
`a8a9480bd61aa510de423569af494d8dde69e8f2` in its ancestry. Historical Phase 2
state, reports, and benchmark artifacts are preserved and will not be rewritten.

## 2026-09-07 — Milestone 1 rebaseline

The three additive benchmark workloads were committed before any ID 13-15
runtime implementation. The benchmark-source production revision for this
milestone is
`25a43c0d55bd8550fef3e992e212da9fd2d57d15`.

The frozen Phase 1 S1 used runtime
`a8a9480bd61aa510de423569af494d8dde69e8f2`, the current benchmark harness
revision above, and true-base adapter digest
`69383c1689963b04e1a94e2a0403a024a6c249105683924fab5b8572c496edf9`.
S2 disabled every optimization feature; S3 enabled only controls 0-4. All
integrated workload hashes were stable. S2 median elapsed time was 877 ms
versus 876 ms for S1, and peak RSS was 128352 KiB versus 124016 KiB; the
observed deltas remain below the 5% regression threshold. S3 was escalated to
200 samples after the 60-sample CV was 5.56%; the authoritative 200-sample CV
was 4.22%, with median elapsed time 59 ms and peak RSS 132816 KiB.

The physical-identity disabled control was also captured at the current
production revision: 60 samples, median 5629 ms, peak RSS 114208 KiB. The
initial stale installed dylib was detected by symbol/timestamp inspection and
replaced with the exact macOS Release build before measurements; no historical
artifact was changed.

## 2026-09-07 — Milestone 2 physical identity folding

ID 15 was committed in `31c3d0fa40d955806df18e1dad1ba79fa301a606`. The native
path derives an ephemeral `RasterPhysicalPlan` from the existing compiled
geometry, requires exact physical integer identity on software raster targets,
and falls back without rotation or near-identity approximation. It preserves
the existing writePixels helper as an independent eligibility path and records
package-private identity counters.

Focused smokes passed for smooth and nearest identity, 400x400 to 200x200
non-identity fallback, alpha/save and dynamic-hwScale guards, and exact crop and
frame selection. SDK tests/dist, macOS Release software-Skia build, and related
geometry/writePixels smokes passed. Identity S2/S3 used the committed runtime
above and 60 samples: S2 median 5483 ms, P95 5516 ms, CV 0.62%, peak RSS 114080
KiB, counters zero; S3 median 1060 ms, P95 1062 ms, CV 0.21%, peak RSS 115728
KiB, 1024 identity hits and avoided resamples per batch. Both hashes were
`000000D600000165`, and no 200-sample escalation was required.

## 2026-09-07 — Extension 02 handoff and S1 baselines

Extension 01's final handoff HEAD was `de5ad089e68e39a1224a87247aad026e6d31baab`.
The ID 15 runtime remained `31c3d0fa40d955806df18e1dad1ba79fa301a606`; three
follow-up commits corrected the test-raster factory registration and its
32-character native signature lookup. The final pre-ID13/14 production and
harness revision for both S1 baselines is
`07912a0f2d5d48705a5b7caa65f82a71ed7d3b57`.

The target-color RGBA control used 60 samples: median 5477 ms, P95 5493 ms,
CV 0.22%, peak RSS 115296 KiB, and hash `000000D600000165`. Focused S1 probes
for BGRA, RGB565, and translucent targets passed with hashes
`000000D600000165`, `0000AD0400003765`, and `0000F55E00000165`.
The physical-variant repeat control used 60 samples: median 1326 ms, P95
1328 ms, CV 0.13%, peak RSS 114160 KiB, and hash `000000D600000165`.
First-use, mutation, and size-replacement probes each passed with one sample.
All raw captures are additive under the extension benchmark directory.

## 2026-09-07 — Extension 02 target-color conversion

ID 13 was committed in `37f489600449c81c4ead3eb677a01ec35d12112f`. The one-slot
derived raster is target-aware, CPU-accessible, and owned by the source backing;
the primary source remains authoritative for canonical ARGB readback. Skia
performs BGRA8888 and RGB565 conversion only after the second eligible draw.
RGBA remains the control path, translucent content falls back, and source-root
mutation clears the slot before the next draw.

The exact RGBA S1/S2/S3 comparison used 60 samples and three warmups. S1 at
`07912a0f2d5d48705a5b7caa65f82a71ed7d3b57` measured 5477 ms median, 5493 ms
P95, 0.221% CV, and 115296 KiB peak RSS. S2 at the ID 13 revision measured
5629 ms median, 5646 ms P95, 0.178% CV, and 113280 KiB peak RSS. S3 measured
5642 ms median, 5648 ms P95, 0.133% CV, and 115744 KiB peak RSS. All RGBA
hashes were `000000D600000165`; S2 and S3 target-color counters were zero.

The BGRA conversion S2/S3 pair also used 60 samples. S2 measured 5667 ms
median, 5677 ms P95, 0.128% CV, and 114240 KiB peak RSS with zero counters.
S3 measured 1083 ms median, 1089 ms P95, 0.329% CV, and 109184 KiB peak RSS;
the final counters were 64512 attempts, one materialization, 64510 hits, one
fallback, and 160000 converted bytes. The stable output hash was
`000000D600000165`.

Focused macOS smokes passed for RGB565 conversion (80000 converted bytes),
translucent fallback, source-root mutation invalidation, BGRA-to-RGB565 slot
replacement (two materializations and 240000 converted bytes), disabled BGRA
parity, and canonical source readback. No 200-sample escalation was required;
all CVs were below 5% and exact RGBA S2 elapsed/RSS deltas stayed below 5%.

## 2026-09-07 — Extension 02 physical variant cache

ID 14 was committed in `c6515a8f0`. The native one-slot derived raster cache
supports only frame/crop/scale/smooth-scale geometry, uses exact scalar
signatures and source/decode generations, materializes on the second
consecutive key, and clears on mutation. ID 15 identity folding remains
first; with ID 13 enabled the physical materializer writes directly to final
BGRA/RGB565 when opacity permits, otherwise RGBA.

The exact physical-variant S1/S2/S3 workload used 60 samples and the unchanged
`ImageRasterVariantBenchmarkApp` repeat case: S1 median1326ms, S2 1323ms,
S3 263ms; P95 1328/1334/265ms; CV 0.129/0.329/0.280%; peak RSS
114160/116608/116320KiB. All hashes were `000000D600000165`. S2/S3 elapsed
deltas versus S1 were -0.226%/-80.166%, RSS deltas +2.144%/+1.892%, no
escalation.

Focused smokes passed for repeat/materialization/hits, identity precedence,
one-slot replacement/eviction, mutation invalidation, crop, alpha, rotation,
hardware-scale, disabled parity, and ID13 BGRA/translucent compatibility.
The combined ID13+ID14 BGRA smoke passed with one physical materialization,
one hit, no target-color materialization, and the stable output hash. Counters
were repeat 3 lookups/2 misses/1 materialization/1 hit; replacement
6 lookups/4 misses/2 materializations/1 eviction; mutation 2 lookups/2
misses/1 materialization; the encoded-root decode-generation smoke recorded
two misses, one fresh materialization, and zero stale hits; identity zero
physical variants. Raw captures and compact results are under the physical-
variant S2/S3 directories.

## 2026-09-07 — Extension 02 handoff to integrated closeout

Extension 02 completed with native runtime code frozen at `c6515a8f0`. The
combined-target and encoded-root decode-generation smoke follow-ups are
`4b5de2931` and `2ff711ffe`; the physical-variant evidence commit is
`8db53eab3`. The exact next-plan starting tip is `2ff711ffe` before this
documentation handoff, with no native runtime changes after `c6515a8f0`.

IDs 13 and 14 remain independently switchable and default-disabled, ID 15
still wins before physical variant admission, and all additive S1/S2/S3
captures remain immutable. Extension 03 is responsible for the combined
all-eight macOS closeout, conditional Android GPU check without an Android
build, and final Phase 3 compact-source handoff.
