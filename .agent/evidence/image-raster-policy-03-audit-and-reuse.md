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

## 2026-09-19 — M3 approved structural corrections

Implementation commit: `0129316af` (`fix(vm,benchmark): apply approved raster
reuse corrections`). The commit implements exactly the approved M3 changes:
canonical intrinsic target-color identity, removal of physical target surface
dimensions from the real key, and a conservative rectangular saved-clip proof.
Delayed materialization, default masks, the single shared slot, and fallback
behavior were not changed.

The macOS ARM64 build passed for `tcvm`, `Launcher`, and `skia_surface_test`.
The native surface test passed with coverage for position changes, compatible
surface-size changes, saved rectangular clips, outside-clip protection,
translated positive-scale matrices, and non-rectangular/rotation/skew
fallbacks.

The final bundle is
`build/image-raster-policy-03-package-m3-native/image-scroll-benchmark-macos-arm64`.
It passed self-test and the exact `raster-structural-smoke` profile: masks
`0`, `8192`, `16384`, `32768`, and `57344`; prefetch on; accounting on; two
rounds; ten processes. Every process passed, completed 189 frames, and reached
scroll endpoint `39091`; no timeout occurred. The corpus remained 663 JPEGs
with hash `588a7e0f4019424a`; SDK JAR SHA-256 remained
`4c1d1a70069dae3c8cc5e11b77eb55d69c300793a896726dfb27bf3b8538e711`.

The software target remained physical `1080x1920`, rowBytes `4320`, color type
`6` (`BGRA8888`), alpha `2`, and backend `software`. Aggregate counters over
the ten processes were target attempts/fallbacks/hits/materializations
`12/12/0/0` with all rejects classified as `root-to-device`; physical
lookups/misses/hits/stores `12/12/0/0` with `12` full and no-surface-size keys;
and identity attempts/hits/fallbacks `12/0/12` with all rejects classified as
`root-to-device`. Disabled paths remained zero and writePixels accounting was
consistent. The workload's zero hits are explained by eligibility and delayed
observation; native tests directly prove the approved key reuse and clip
behavior. This is structural evidence, not a performance-promotion result.

Bundle ZIP SHA-256 is
`70fe022ebb57991f0aa69d53e3c894994ce9ea49bb501605635a17b3eaf879f`;
runtime SHA-256 is
`a7fd330a1fc983d4a0e63e6a53a91edc766995b6cf35e06f6f4218448ad838d3`;
result ZIP SHA-256 is
`da92c7308be840f83da66ca0b361abac55e4eef4a82583255419307fb76666ca`.
Detailed rows and logs are recorded in
`.agent/benchmarks/image-raster-policy-03/m3-structural-validation.md`.

`STOP / REVIEW 3` is ready. M4 remains unauthorized.

## 2026-09-19 — M3 scaled-mapping corrective rerun

The prior M3 package was superseded after the real 2× logical-to-physical
mapping requirement was made explicit. Implementation HEAD is `367fc887c`,
with native mapping/test correction `0528eb62b` and structural runner gate
`367fc887c`. Historical commits `0129316af` and `11c7349a1` were preserved;
their over-80-character body lines were recorded rather than rewritten.

The macOS ARM64 Release rebuild passed for `tcvm`, `Launcher`, and
`skia_surface_test`. Native coverage now includes final pixels after target
position changes, final pixels across compatible surface sizes, a real 2×
draw, rectangular clip/outside pixels, `saveLayer` fallback, and
non-rectangular/rotation/skew/perspective/invalid-mapping fallbacks.

The new bundle
`build/image-raster-policy-03-package-m3-scaled2/image-scroll-benchmark-macos-arm64`
passed self-test and the exact ten-process structural smoke: masks `0`,
`8192`, `16384`, `32768`, `57344`; prefetch on; accounting on; two rounds;
10/10 pass. Every process completed 189 frames, reached `scroll_end=39091`,
and reported `overallPass=true`. The corpus remained 663 JPEGs with hash
`588a7e0f4019424a`; target metrics remained physical `1080x1920`, rowBytes
`4320`, BGRA8888, alpha `2`, and software backend.

The strengthened gate observed target-color attempts/fallbacks/hits/material-
izations `12/12/0/0` and identity attempts/hits/fallbacks `12/0/12`; both
had `RootToDevice=0` and later `SourceMapping=12`. Physical variants recorded
lookups/misses/hits/stores `12/12/0/0`, with `12` full and `12`
no-surface-size keys and no `RootToDevice` rejects. Disabled paths remained
zero, writePixels accounting was consistent, and no clip/pixel corruption was
observed. The workload did not reach the later observations needed for target
materialization or physical stores; this is structural evidence without a
performance conclusion.

Artifacts: SDK JAR
`4c1d1a70069dae3c8cc5e11b77eb55d69c300793a896726dfb27bf3b8538e711`; SDK ZIP
`d1bbd5301db88a2eea4362732c09ed320311e53d6adc0b4d5c45c68f209e46d7`; runtime
`33f50a0cc910947fe2b102c4e1c65aed37ca4d830734ad2c9daedd965bf3c2e0`; Launcher
`ef6f924f3beda71e6615badf94dd93d5dd167a332250aa22e6dc3855cbcf384a`; bundle
ZIP `86b7239434a6771c21a1d1460a7d84d3af9cddfd78854b18b46de3867328740f`; and
result ZIP `2622321e0a1178fc368a11e359c5085c17f38e0b44b430e2680fe94f787fafd4`.
Detailed rows and logs remain in
`.agent/benchmarks/image-raster-policy-03/m3-structural-validation.md`.

## 2026-09-19 — M2 source-scoped key correction

Implementation commit: `f4dab5e23` (`fix(benchmark): scope raster identity diagnostics by backing`).
The exact M2 gate passed again: corpus `588a7e0f4019424a`, masks `8192`,
`16384`, `32768`, and `57344`, prefetch on, accounting on, one round, and
exactly four processes. The self-test still reported 663 JPEGs and the same
corpus hash. Every process completed 189 frames and reached the automatic
scroll endpoint. SDK JAR hash remained
`4c1d1a70069dae3c8cc5e11b77eb55d69c300793a896726dfb27bf3b8538e711`;
runtime hash is
`7730aff3bf03272396bdd67d4f752d70e00ec9993851fa1685c2109de5182845`, bundle
ZIP hash is `a261d3f6f60a2b4074bb8c6f6979d201b03cc2aa443145d7c979a28381663b89`,
and result ZIP hash is
`72f5602092befc2aae0d4937dc77939ecc9db3587ed3fac24d45a88cdca332eb`.

Unique-key diagnostics now use the `NativeImageBackingRecord` identity only as
a diagnostic namespace. The real `RasterVariantKey`, equality, slot, lookup,
eligibility, and materialization policy are unchanged. The intrinsic target
key remains exactly `sourceGeneration + sourceDecodeGeneration +
targetColorType` before source namespacing. Target sources are recorded at the
start of each target-color attempt, so rejected requests are included once.

Corrected aggregate counters are target attempts/fallbacks/unique
sources/acquisition sources `6/6/6/0`, target full/no-destination/intrinsic
keys `0/0/0`, physical lookups/misses/unique sources/full/no-surface-size keys
`6/6/6/6/6`, physical materializations/hits/evictions `0/0/0`, and identity
attempts/hits/fallbacks `6/0/6`. Each target-enabled run has 3 unique target
sources; each physical-enabled run has 3 unique sources and 3 scoped full and
no-surface-size keys. The prior aggregate physical `2/2` was a diagnostic
collapse and is superseded by `6/6`.

The runner now enforces scoped-key invariants: target key counts cannot be
below sources that reached acquisition, and physical full/no-surface-size key
counts cannot be below observed physical sources. All invariants passed with
accounting enabled. `root-to-device` remains the only nonzero target and
identity mapping subreason (`6` each); save-count buckets and shared-slot
transitions remain zero. The source-scoping correction changes only the
diagnostic counts, not the prior eligibility or delayed-observation
conclusions. `STOP / REVIEW 2` remains in force; M3 is not authorized.

## 2026-09-19 — M3 final lifecycle and mapping correction

Implementation commits `922ce2921` and `3c7864115` preserve the validated M3
behavior while closing the remaining structural gap. The canvas registry is
cleared before bitmap replacement, backing release, screen shutdown, and
color-mutation surface replacement. Native tests cover reused bitmap/canvas
addresses and backing release after a known rectangular clip.

The old real-workload `SourceMapping=12/12` was caused by two numeric effects:
an exact integer test rejected source boundaries such as `999.999971`, and an
inverted `SkMatrix` accumulated translation error near large device positions.
The fix computes source boundaries from double-precision geometry and accepts
only bounded float-rounding error. A real source 1000x1000, output 179x179,
output scale 2, and physical 358x358 target now passes source mapping proof.
Fractional visible clipping remains safe: target-color uses the converted
raster with the original smooth shader after full-source and bounds proof,
while identity folding still falls back on `VisibleMapping`.

Native validation passed for macOS ARM64 `tcvm`, `Launcher`, and
`skia_surface_test`. The test asserts final pixels for the 2x smooth target
path and fractional target-color clip, and for an inconsistent fractional
identity transform asserts generic fallback plus final pixels. Existing
rotation, skew, perspective, saveLayer, non-rectangular clip, and lifecycle
coverage also passed.

The final package is
`build/image-raster-policy-03-package-m3-final/image-scroll-benchmark-macos-arm64`.
Self-test passed with corpus hash `588a7e0f4019424a`, and the exact matrix
passed 10/10: masks `0,8192,16384,32768,57344`, prefetch on, accounting on,
two rounds, 189 frames per process, and automatic-scroll endpoint reached.
Aggregate counters are:

- target color attempts/fallbacks/hits/materializations/acquisition sources:
  `12/12/0/0/12`;
- target mapping RootToDevice/SourceMapping/VisibleMapping: `0/0/0`;
- physical identity attempts/hits/fallbacks: `12/0/12`;
- physical identity mapping RootToDevice/SourceMapping/VisibleMapping:
  `0/0/12`;
- physical variant lookups/misses/hits/stores: `12/12/0/0`;
- disabled paths: zero; `writePixels` accounting: consistent.

Target metrics remained physical `1080x1920`, rowBytes `4320`, BGRA8888,
alpha 2, software. Runtime SHA-256 is
`62318f5b09172aa2518c1bd9dc2d0013c7fdff548af71cc85d95b8145d584ee2`;
Launcher SHA-256 is
`ef6f924f3beda71e6615badf94dd93d5dd167a332250aa22e6dc3855cbcf384a`;
bundle ZIP SHA-256 is
`6ee02bc362dcb7e8578044eab2b0834b0271c70ed73b1fac6330f251d87fcef5`;
result ZIP SHA-256 is
`8067a4a6b4b55a49afc49956a127b7b6cd7b65905b97376d52c8f893a1c80886`.
Logs: `/tmp/image-raster-policy-03-final-native-build-2.log`,
`/tmp/image-raster-policy-03-skia-surface-test-final.log`,
`/tmp/image-raster-m3-final-self-test-2.log`, and
`/tmp/image-raster-m3-final-structural-matrix-2.log`.

`STOP / REVIEW 3` is ready. M4 remains unauthorized and was not started.
