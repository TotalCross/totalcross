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
authorized stage, and the diagnostic-only audit below has now completed. No
optimization policy, cache identity, materialization timing, default mask, or
shared slot has been changed.

## 2026-09-19 — M2 identity and eligibility audit

The M2 package passed self-test and the exact four-process
`raster-policy-audit` profile: masks `8192`, `16384`, `32768`, and `57344`,
all with prefetch and accounting enabled. Corpus hash remained
`588a7e0f4019424a`; SDK JAR hash remained
`4c1d1a70069dae3c8cc5e11b77eb55d69c300793a896726dfb27bf3b8538e711`. The
bundle ZIP hash is
`a9fe55d035fd99fe768b801c6f619e1b37741ab3999f8bfc91960edc5a370120`; the
result ZIP hash is
`95e4d7c97bb95a12b7ee911a350572bc967f25d6001a7464834e2f1d01df9971`.

Aggregate diagnostic evidence:

- target-color: `6` attempts, `6` fallbacks, `0` full keys, `0` unique
  sources, `0` hits, and `0` materializations; all six classified rejects
  were mapping geometry;
- physical variant: `6` lookups and misses, `2` full keys, `2` keys without
  surface size, `0` hits, `0` stores, `0` evictions, and no rejection;
- physical identity: `6` attempts, `0` hits, `6` fallbacks, all six rejects
  classified as mapping geometry;
- shared slot/pending transitions: all four cross-kind counters are `0`;
- canvas/save-state rejects: `0`, with save-count buckets all `0`.

The physical ratio is `2 / 2 = 1.00`; target key ratios are undefined because
no target key reached acquisition. Physical misses were first pending
observations, so this run does not demonstrate a second-observation
materialization. The evidence supports eligibility and delayed observation as
the measured reasons for zero hits; it does not support destination-position,
surface-dimension, cross-kind replacement, or save-state fragmentation as the
cause in this workload. Detailed rows and calculations are in
`.agent/benchmarks/image-raster-policy-03/m2-variant-audit.md`.

The runner, package, and result archive were generated after adding
accounting-gated diagnostics only. The first attempted package exposed a
`NoSuchMethodError` when a new Java wrapper was used without a corresponding
`TCUI.tcz` class; that wrapper was removed in favor of the existing native
metric method, the package was rebuilt, and the authoritative rerun passed.

`STOP / REVIEW 2` is now ready. M3 remains unauthorized; no structural fix
was inferred or applied from this audit.

## 2026-09-19 — M2 review correction rerun

Implementation commit: `ca71cbe0b` (`fix(benchmark): refine M2 raster diagnostics`).
The corrected run passed the same self-test and exact four-process
`raster-policy-audit` profile. Each process completed 189 frames and reached
the automatic-scroll endpoint in approximately 3.01 seconds; no timeout was
observed. Corpus hash remained `588a7e0f4019424a`; SDK JAR hash remained
`4c1d1a70069dae3c8cc5e11b77eb55d69c300793a896726dfb27bf3b8538e711`.
Native runtime hash is
`ead97b59f0a3839e549f76a0afdf770be070ceee70f831145bbbc4f00483254f`, bundle
ZIP hash is `e2b97f8d8f7d125f8937d2339007661f6653aa7f654eb4d9aa55787b632c3f7f`,
and result ZIP hash is
`b94034944425e7a0e83b307062c36e48cc960407fbe7dba7505419b4d0515433`.

The correction did not change the active cache key or eligibility. It made
`targetColorUniqueIntrinsicKeys` hash exactly source generation, source decode
generation, and target color type; separated save-count buckets for target,
physical-variant, and identity diagnostics; classified mapping geometry into
ten accounting-gated subreasons; removed `PARTIAL_INTERSECTION` from the
published output because no real condition was classified; and moved target
fallback accounting to the attempt terminal paths so each non-handled attempt
contributes exactly one fallback.

Aggregate corrected counters are target attempts/fallbacks/hits/materializations
`6/6/0/0`, physical lookups/misses/hits/stores/evictions `6/6/0/0/0`, and
identity attempts/hits/fallbacks `6/0/6`. Target and identity mapping totals
are `root-to-device=6` each; physical-variant mapping totals are zero. The
target, physical-variant, and identity save-count bucket vectors are all
`[0,0,0,0,0,0]`. Physical full/no-surface-size keys remain `2/2`; all shared
slot and pending cross-kind counters remain zero; and target fallback equals
target attempts in the applicable masks (`3/3` for each target-enabled run).

`PARTIAL_INTERSECTION` remains a legacy native enum value only; it is not part
of the M2 JSON or runner contract. `STOP / REVIEW 2` remains in force and M3
is still unauthorized.
