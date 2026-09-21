<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Editorial handoff — raster policy audit and reuse

## Editorial Summary

Milestone 1 correction, the M2 identity audit, and the approved M3 structural
corrections are complete.
The corrected bridge reports the real software target `1080x1920`, rowBytes
`4320`, BGRA8888, alpha `2`, `kN32=4`, and software backend. M2 passed the
four-process audit profile and found no target-key acquisition, no variant
hits/stores, no cross-kind replacement, and no canvas/save-state rejection.
`STOP / REVIEW 1` and `STOP / REVIEW 2` are closed; `STOP / REVIEW 3` is
ready. M4 also passed its diagnostic and primary performance matrices; the
three-pass automatic-scroll workload reached its endpoint without timeout.
`STOP / REVIEW 4` is ready.

## Original Plan versus Actual Outcome

The four-milestone plan remains intact. M1 was rerun after correcting the
target metric transport. M2 added accounting-gated identity and eligibility
diagnostics and ran only its prescribed four-process profile. M3 applied the
approved identity/clip corrections, then required a corrective rerun for
logical-to-physical scale proof and the stricter structural gate. M4 added the
prescribed three-pass reuse workload, ran both matrices on the final HEAD, and
preserved the plan's review boundary.

## What Changed

The benchmark-only target bridge now transports scalar width, height, effective
software pitch/rowBytes, alpha type, Skia color type, `kN32` type, stable
color classification, and backend through a short NativeImageBacking native
name. Structural validation checks minimum rowBytes for each color class.
The old unsafe Image probe and packed dimension word were removed.

M2 exposes diagnostic-only counts for normalized target and physical keys,
pending observations, shared-slot cross-kind transitions, rejection reasons,
feature-specific save-count buckets, and ten mapping subreasons through the
established benchmark metric path. The accounting-off path remains
diagnostics-unavailable. The intrinsic target key diagnostic is exactly
`sourceGeneration + sourceDecodeGeneration + targetColorType`; it is not the
active cache key, and all unique-key sets are namespaced by source backing for
diagnostics. `PARTIAL_INTERSECTION` is not emitted because no real condition
was classified. The runner enforces that scoped key counts cover the sources
that reached acquisition or observation.

The M3 correction maps device-space destinations back to integral source
pixels using finite positive axis-aligned X/Y scales compatible with the
effective content scale. It computes boundaries from double geometry and
accepts only bounded float-rounding error. Target-color conversion may retain
a fractional visible edge after full-source and bounds proof; identity folding
still rejects inconsistent or fractional mappings. Saved states are accepted
only when the repository clip wrapper recorded a rectangular clip; unknown
rectangular states and `saveLayer` remain on the generic path. Lifecycle
cleanup clears clip authorization before tracked canvases are replaced or
destroyed.

M4 adds `cold-forward`, `warm-reverse`, and `warm-forward` pass artifacts while
leaving the single-pass default intact. The runner validates each pass and
aggregates per-pass timing, lifecycle counters, scoped key multiplicity,
target classification, bytes, pending replacements, and shared-slot
transitions. Warm-pass structural diagnostics may be inactive only after the
same run has exercised the enabled path and the counters remain consistent.

## Decisions and Trade-offs

Keep intrinsic JPEG opacity independent of bit 1. Use mask `4` for the
no-bit-1 proof. Mask `32799` includes `RASTER_OPACITY_METADATA` and is
metadata-enabled, not bit-1-disabled. Accounting off retains external timing
but makes native diagnostics unavailable. Delayed materialization, defaults,
and the single raster-variant slot remain unchanged.
On the measured macOS target, classification is BGRA8888. The 8192 path is
therefore a target-color attempt toward BGRA8888, not evidence of an RGB565
surface target; RGB565 is interpreted only as the separate source-storage
comparison prescribed by M4.

## Unexpected Problems and Discoveries

The old `56x896` target was a transport artifact: 10-bit packing converted
the actual `1080x1920` dimensions to their lower bits. The native VM also
requires short native method names, and the previous Image bridge was unsafe
after rendering. A scalar bridge through the established backing path solves
both constraints.

Historical commit-message checks found lines over 80 characters in
`82f4af600`, `ed09cf657`, `135050818`, `1f5bdbbea`, `f02f9dcff`, and
`1fb0b1d35`. These commits were not amended or rewritten.

The first M2 package attempt used a new Java helper absent from the existing
`TCUI.tcz`, producing `NoSuchMethodError`. The helper was removed in favor of
the existing native metric method; the rebuilt package passed all M2 checks.

The first M3 structural rerun still rejected every real target-color and
identity candidate at `root-to-device`: the workload uses logical `540x960`
on a physical `1080x1920` target. The native 2× test exposed the missing
mapping proof. After the fix, candidates passed that stage and reached later
source-mapping fallbacks, while the automatic scroll still reached its
endpoint without timeout.

The first M4 package fixture lacked the official resource JAR and then lacked
the SDK `etc` directory; both packaging issues were fixed in temporary SDK
staging without changing repository sources. The first reuse validation also
treated zero warm-pass activity as a failure and omitted one diagnostic CSV
field. The validator and aggregation were corrected narrowly, then both M4
matrices were rerun on the final HEAD.

## Validation and Measurable Results

Implementation HEAD: `31bdef4bf`; bundle:
`build/image-raster-policy-03-package-fixed/image-scroll-benchmark-macos-arm64`.

Passed: SDK distribution; macOS ARM64 `tcvm`, `Launcher`, and
`skia_surface_test`; native surface test; package; bundle self-test; six
accounting-on smokes; and 20/20 A/B processes across five rounds. Corpus hash
was `588a7e0f4019424a`; SDK JAR hash was
`4c1d1a70069dae3c8cc5e11b77eb55d69c300793a896726dfb27bf3b8538e711`; result
ZIP hash was
`9f2d6ee03838b30814f1b76830b1fa14b80dc30929e5b02d4f58263f134c5264`.

The five-round work P50 delta `32795 -> 32799` is -18.66% with accounting on
and -18.54% off. Work P95 is +1.83% on and +10.20% off; corrected off-mode
work P99 is `26478916 -> 28147333`, delta `+1668417` (`+6.30%`). P99/MAX
also rise.
Mask `4/off` recorded 175 intrinsic-known results and 2 fallback scans
overall. Mask `32799/off` recorded 175 intrinsic-known, 177
metadata-known, and 0 fallback scans.

### M2 identity and eligibility results

The authoritative corrected M2 package is
`build/image-raster-policy-03-package-m2-scoped/image-scroll-benchmark-macos-arm64`.
The source-scoping implementation commit is `f4dab5e23`.
The corpus hash is `588a7e0f4019424a`, SDK JAR hash is
`4c1d1a70069dae3c8cc5e11b77eb55d69c300793a896726dfb27bf3b8538e711`, bundle
hash is `a261d3f6f60a2b4074bb8c6f6979d201b03cc2aa443145d7c979a28381663b89`,
runtime hash is
`7730aff3bf03272396bdd67d4f752d70e00ec9993851fa1685c2109de5182845`, and
result ZIP hash is
`72f5602092befc2aae0d4937dc77939ecc9db3587ed3fac24d45a88cdca332eb`.

The four masks passed. Aggregate target attempts/fallbacks/unique
sources/acquisition sources were `6/6/6/0`, with target full,
no-destination, and intrinsic keys `0/0/0`; physical-variant
lookups/misses/unique sources/full/no-surface-size keys were
`6/6/6/6/6`, with materializations/hits/evictions `0/0/0`; and identity
attempts/hits/fallbacks were `6/0/6`. Target and identity rejects were all
mapping geometry. Physical full and no-surface-size keys versus observed
sources were both `6/6 = 1.00`; target key ratios remain undefined because no
target key reached acquisition. Save-state rejects were `0/6 = 0%`; all
shared-slot and pending cross-kind transitions were zero.

The measured zero-hit causes are eligibility and delayed observation: target
color and identity were rejected by the `root-to-device` mapping subreason,
while physical variants were observed once without a second observation. The
corrected source-scoped diagnostics changed the physical key count from the
prior collapsed `2/2` to `6/6`, and changed target unique sources from zero to
six because rejected requests are now registered at attempt start. These are
diagnostic corrections only; the run still does not support destination-
position fragmentation, surface-dimension fragmentation, cross-kind
eviction, or canvas/save-state rejection as causes. Target fallbacks equal
target attempts in the applicable runs, and all three feature-specific
save-count bucket vectors are zero.

### M3 approved structural results

Implementation commit `0129316af` applied the approved canonical target-color
identity, removed physical surface dimensions from the physical key, and
replaced blanket save-count rejection with a rectangular saved-clip proof.
Delayed materialization, default masks, and the single shared slot remained
unchanged.

The final macOS ARM64 bundle
`build/image-raster-policy-03-package-m3-native/image-scroll-benchmark-macos-arm64`
passed self-test and the exact ten-process `raster-structural-smoke` profile.
Masks were `0,8192,16384,32768,57344`, with prefetch and accounting on, over
two rounds. All ten processes passed, completed 189 frames, and reached scroll
endpoint `39091`; no timeout occurred. The target remained software
`1080x1920`, rowBytes `4320`, BGRA8888, alpha `2`.

Aggregate target attempts/fallbacks/hits/materializations were `12/12/0/0`,
with all rejects classified as `root-to-device`. Physical variant
lookups/misses/hits/stores were `12/12/0/0`, with twelve full and
no-surface-size keys. Identity attempts/hits/fallbacks were `12/0/12`, again
all `root-to-device`. Disabled paths remained at zero and writePixels
accounting stayed consistent. Native tests directly confirmed target position
reuse, physical reuse across compatible surface sizes, saved rectangular clip
protection, translated positive-scale handling, and fallback for non-rectangular
clips, rotation, and skew.

Bundle SHA-256 is
`70fe022ebb57991f0aa69d53e3c894994ce9ea49bb501605635a17b3eaf879f`;
runtime SHA-256 is
`a7fd330a1fc983d4a0e63e6a53a91edc766995b6cf35e06f6f4218448ad838d3`;
result ZIP SHA-256 is
`da92c7308be840f83da66ca0b361abac55e4eef4a82583255419307fb76666ca`.
This is structural evidence, not a performance-promotion result.

### M3 scaled-mapping corrective rerun

Implementation HEAD `367fc887c` includes the native correction `0528eb62b`
and the runner gate `367fc887c`. The new bundle
`build/image-raster-policy-03-package-m3-scaled2/image-scroll-benchmark-macos-arm64`
passed the same exact ten-process profile. All processes completed 189 frames,
reached `scroll_end=39091`, and reported `overallPass=true`; target metrics
remained `1080x1920`, rowBytes `4320`, BGRA8888, alpha `2`, software.

The gate recorded target-color attempts/fallbacks/hits/materializations
`12/12/0/0` and identity attempts/hits/fallbacks `12/0/12`. Both paths had
`RootToDevice=0` and later `SourceMapping=12`, proving that the 2× mapping
stage was traversed. Physical variant lookups/misses/hits/stores were
`12/12/0/0`, with `12` full and no-surface-size keys. Disabled paths and
writePixels accounting remained valid. The zero materializations/stores are
reported as delayed-observation behavior, without a performance conclusion.

The native test added final-pixel checks for target and physical reuse, a real
2× draw, outside-clip protection, and explicit `saveLayer`, perspective,
rotation, skew, non-rectangular, and invalid-mapping fallbacks. The updated
summary and hashes are in
`.agent/benchmarks/image-raster-policy-03/m3-structural-validation.md`.

### M3 final corrective gate

Implementation commits `922ce2921` and `3c7864115` close the remaining
structural failure without changing defaults, delayed materialization, or the
single shared variant slot. The real-workload `SourceMapping=12/12` was
traced to exact-integrality checks applied to float-rounded boundaries and to
translation error from an inverted float matrix. Direct double-precision
mapping now accepts only bounded float error. Target-color keeps the original
smooth shader for a fractional visible edge only after full-source and bounds
proof; inconsistent fractional identity still falls back.

The final package
`build/image-raster-policy-03-package-m3-final/image-scroll-benchmark-macos-arm64`
passed self-test and the exact ten-process matrix. Masks were
`0,8192,16384,32768,57344`, prefetch and accounting were on, two rounds ran,
all 10/10 processes completed 189 frames, and automatic scroll reached
`39091` without timeout. The target remained physical `1080x1920`, rowBytes
`4320`, BGRA8888, alpha `2`, software.

Final aggregate target-color attempts/fallbacks/hits/materializations/
acquisition sources were `12/12/0/0/12`; target mapping
RootToDevice/SourceMapping/VisibleMapping was `0/0/0`. Physical identity
attempts/hits/fallbacks were `12/0/12`, with mapping `0/0/12`. Physical
variant lookups/misses/hits/stores were `12/12/0/0`; disabled paths were zero
and writePixels accounting was consistent. This is structural evidence only;
M4 reuse and RGB565/target-color measurements remain gated.

### M3 final ULP-boundary correction

Implementation HEAD `404424e3c` replaces the magnitude-scaled integral test
with adjacent-float ULP spacing and a `1/1024`-pixel absolute cap. The focused
native cases accept `999.999971` and `1000.000029` as `1000`, while rejecting
`1000.01`, `1000000.1`, and `1000000.4`. This preserves the real 2x source
mapping proof without making large-coordinate subpixels eligible.

The updated macOS ARM64 package passed self-test and the exact structural
matrix: 10/10 processes, 189 frames each, masks
`0,8192,16384,32768,57344`, prefetch/accounting on, and automatic scroll at
`39091` for every fresh log. Counters remained target-color
`12/12/0/0/12`, target mapping `0/0/0`, identity `12/0/12` with mapping
`0/0/12`, physical variants `12/12/0/0`, disabled paths zero, and
writePixels accounting consistent. M4 remains gated.

### M4 reuse and RGB565/target-color measurement

Implementation HEAD `cfdbb480e`; final bundle:
`build/image-raster-policy-04-package-m4-final2/image-scroll-benchmark-macos-arm64`.
The bundle self-test passed with 663 JPEGs, corpus hash `588a7e0f4019424a`,
and physical target `1080x1920`, rowBytes `4320`, BGRA8888, software.

The accounting-on diagnostic matrix passed 8/8 processes and 24 pass rows for
masks `0,32,8192,8224`, prefetch off/on, one round, and three ordered passes.
The accounting-off performance matrix passed 24/24 processes and 72 pass rows
over three rounds. Per-pass evidence is in
`.agent/benchmarks/image-raster-policy-03/m4-reuse-rgb565-target-color.md`
and the two result ZIPs recorded there.

The target-color diagnostic totals for masks 8192/8224 were
`1014/1014/0/0` for attempts/fallbacks/hits/materializations, with zero
converted bytes. The largest scoped target-key set was `204` sources and
`202` full/no-destination/intrinsic/acquisition keys. Target pending
replacements and shared-slot transitions/pending replacements were zero. The
performance pairwise output had 30 consistent-direction rows and 42
inconclusive-variance rows; only the four plan-approved comparisons were
emitted, so no timing-only causality or default recommendation follows.

## Useful Evidence and Examples

See
`.agent/benchmarks/image-raster-policy-03/m1-write-pixels-accounting.md`
for all 20 M1 raw rows and medians,
`.agent/benchmarks/image-raster-policy-03/m2-variant-audit.md` for M2
calculations,
`.agent/benchmarks/image-raster-policy-03/m4-reuse-rgb565-target-color.md`
for M4 per-pass and pairwise results, and
`.agent/evidence/image-raster-policy-03-audit-and-reuse.md` for hashes,
historical preservation, and the review boundary.

## Limitations, Remaining Work, and Open Questions

M2 is complete, but its one-round workload produced no second observation and
no target-key acquisition; therefore it cannot establish reuse or key
fragmentation beyond the observed eligible physical keys. M3 is structural
evidence only: its workload still did not produce benchmark cache hits, while
the native tests prove the approved reuse cases. M4 reuse/RGB565 measurements
are complete as observational evidence, but remain subject to `STOP / REVIEW
4`; no performance promotion follows from timing alone.
The old target dimensions are preserved as historical evidence but must not be
combined with corrected results.

## Possible Article Angles

The benchmark demonstrates why diagnostic accounting must be separable from
active-work timing and why packed diagnostic transports can create false
hardware conclusions.

## Suggested Narrative

Lead with the corrected physical target evidence, then show the interleaved
20-process design, the stable median writePixels benefit, and the unresolved
tail behavior. Explain the mask-4 intrinsic opacity proof before describing
metadata-enabled mask 32799. Then use M4 to show how BGRA8888 target
classification separates target-color fallback evidence from RGB565
source-storage comparisons, with warm-pass reuse reported as lifecycle data
rather than inferred from timing.

## Claims Requiring Human Review

M1 through M3 are closed at their recorded boundaries and M4 is ready at
`STOP / REVIEW 4`. RGB565 quality/default-mask changes, target-color
performance promotion, shared-slot changes, and cross-platform
generalization still require explicit reviewer approval.
