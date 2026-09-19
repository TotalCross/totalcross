<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Editorial handoff — raster policy audit and reuse

## Editorial Summary

Milestone 1 correction is complete and passes the SDK/native/package,
self-test, six-smoke, and exact 20-process A/B gates. The corrected bridge
reports the real software target `1080x1920`, rowBytes `4320`, BGRA8888,
alpha `2`, `kN32=4`, and software backend. Median active work improves
18.66% with accounting on and 18.54% off; tails rise and remain a review
topic. Execution stops at `STOP / REVIEW 1`.

## Original Plan versus Actual Outcome

The four-milestone plan remains intact. M1 was rerun after correcting the
target metric transport. No default mask, delayed target-color materialization,
cache identity, or shared raster-variant slot changed. M2–M4 remain gated and
were not started.

## What Changed

The benchmark-only target bridge now transports scalar width, height, effective
software pitch/rowBytes, alpha type, Skia color type, `kN32` type, stable
color classification, and backend through a short NativeImageBacking native
name. Structural validation checks minimum rowBytes for each color class.
The old unsafe Image probe and packed dimension word were removed.

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
and -18.54% off. Work P95 is +1.83% on and +10.20% off; P99/MAX also rise.
Mask `4/off` recorded 175 intrinsic-known results and 2 fallback scans
overall. Mask `32799/off` recorded 175 intrinsic-known, 177
metadata-known, and 0 fallback scans.

## Useful Evidence and Examples

See
`.agent/benchmarks/image-raster-policy-03/m1-write-pixels-accounting.md`
for all 20 raw rows and medians, and
`.agent/evidence/image-raster-policy-03-audit-and-reuse.md` for hashes,
historical preservation, and the review boundary.

## Limitations, Remaining Work, and Open Questions

M2 identity/eligibility diagnostics, M3 approved structural changes, and M4
reuse/RGB565 measurements remain. No performance promotion or structural
correction follows from M1 alone. The old target dimensions are preserved as
historical evidence but must not be combined with corrected results.

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

Review is required before M2. Any cache-identity, eligibility, materialization,
default-mask, shared-slot, RGB565, or cross-platform conclusion requires the
later gates in the active plan.
