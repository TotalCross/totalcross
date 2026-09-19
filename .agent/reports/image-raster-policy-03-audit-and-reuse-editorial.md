<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Editorial handoff — raster policy audit and reuse

## Editorial Summary

Milestone 1 correction and the M2 evidence-only identity audit are complete.
The corrected bridge reports the real software target `1080x1920`, rowBytes
`4320`, BGRA8888, alpha `2`, `kN32=4`, and software backend. M2 passed the
four-process audit profile and found no target-key acquisition, no variant
hits/stores, no cross-kind replacement, and no canvas/save-state rejection.
`STOP / REVIEW 1` is closed and `STOP / REVIEW 2` is ready; M3 remains
unauthorized. The review-correction rerun also confirmed that the app's
automatic scroll completes: 189 frames per process, endpoint reached, and no
timeout.

## Original Plan versus Actual Outcome

The four-milestone plan remains intact. M1 was rerun after correcting the
target metric transport. M2 added accounting-gated identity and eligibility
diagnostics and ran only its prescribed four-process profile. No default mask,
delayed target-color materialization, cache identity, or shared raster-variant
slot changed. M3–M4 remain gated.

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
active cache key. `PARTIAL_INTERSECTION` is not emitted because no real
condition was classified.

## Decisions and Trade-offs

Keep intrinsic JPEG opacity independent of bit 1. Use mask `4` for the
no-bit-1 proof. Mask `32799` includes `RASTER_OPACITY_METADATA` and is
metadata-enabled, not bit-1-disabled. Accounting off retains external timing
but makes native diagnostics unavailable. Delayed materialization, defaults,
and the single raster-variant slot remain unchanged.

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
`build/image-raster-policy-03-package-m2-review/image-scroll-benchmark-macos-arm64`.
The diagnostic implementation commit is `ca71cbe0b`.
The corpus hash is `588a7e0f4019424a`, SDK JAR hash is
`4c1d1a70069dae3c8cc5e11b77eb55d69c300793a896726dfb27bf3b8538e711`, bundle
hash is `e2b97f8d8f7d125f8937d2339007661f6653aa7f654eb4d9aa55787b632c3f7f`,
runtime hash is
`ead97b59f0a3839e549f76a0afdf770be070ceee70f831145bbbc4f00483254f`, and
result ZIP hash is
`b94034944425e7a0e83b307062c36e48cc960407fbe7dba7505419b4d0515433`.

The four masks passed. Aggregate target attempts/fallbacks/hits/materializations
were `6/6/0/0`; physical-variant lookups/misses/hits/stores/evictions were
`6/6/0/0/0`; and identity attempts/hits/fallbacks were `6/0/6`. Target and
identity rejects were all mapping geometry. Physical full keys versus keys
without surface size were `2/2 = 1.00`. Target full-key ratios are undefined
because no target key reached acquisition. Save-state rejects were `0/6 = 0%`;
all shared-slot and pending cross-kind transitions were zero.

The measured zero-hit causes are eligibility and delayed observation: target
color and identity were rejected by the `root-to-device` mapping subreason,
while physical variants were observed once without a second observation. The
run does not support destination-position fragmentation, surface-dimension
fragmentation, cross-kind eviction, or canvas/save-state rejection as causes
in this workload. Target fallbacks equal target attempts in the applicable
runs, and all three feature-specific save-count bucket vectors are zero.

## Useful Evidence and Examples

See
`.agent/benchmarks/image-raster-policy-03/m1-write-pixels-accounting.md`
for all 20 M1 raw rows and medians,
`.agent/benchmarks/image-raster-policy-03/m2-variant-audit.md` for M2
calculations, and
`.agent/evidence/image-raster-policy-03-audit-and-reuse.md` for hashes,
historical preservation, and the review boundary.

## Limitations, Remaining Work, and Open Questions

M2 is complete, but its one-round workload produced no second observation and
no target-key acquisition; therefore it cannot establish reuse or key
fragmentation beyond the observed eligible physical keys. M3 approved
structural changes and M4 reuse/RGB565 measurements remain after review. No
performance promotion or structural correction follows from M1 or M2 alone.
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
metadata-enabled mask 32799.

## Claims Requiring Human Review

M1 review is closed and M2 is ready at `STOP / REVIEW 2`. Any cache-identity,
eligibility, materialization, default-mask, shared-slot, RGB565, or
cross-platform correction requires explicit reviewer approval of M3/M4.
