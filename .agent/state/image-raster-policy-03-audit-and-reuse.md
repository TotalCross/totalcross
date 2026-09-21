<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# State — raster policy audit and reuse

## Active slice

- Bootstrap, Milestone 1, and the M2 evidence-only audit are complete.
- M1 correction rerun passed at implementation HEAD `31bdef4bf`.
- Required branch: `perf/image-decode-distributed-benchmark`.
- Plan 2 remains an ancestor of the execution HEAD.
- The compact plan is committed as `5d4b3a300`.

Read this state first on resume, then the active plan section and only the
source/evidence paths needed for the next action.

## Working set

- Plan: `.agent/plans/image-raster-policy-03-audit-and-reuse.md`
- Evidence: `.agent/evidence/image-raster-policy-03-audit-and-reuse.md`
- Editorial report:
  `.agent/reports/image-raster-policy-03-audit-and-reuse-editorial.md`
- M1 summary:
  `.agent/benchmarks/image-raster-policy-03/m1-write-pixels-accounting.md`

## Progress

- [x] (2026-09-18) Verify branch, ancestry, and preserve unrelated files.
- [x] (2026-09-18) Create bootstrap state, evidence, and editorial handoff.
- [x] (2026-09-19) Implement M1 correctness and benchmark hygiene.
- [x] (2026-09-19) Correct the native target-metric transport and rerun M1.
- [x] (2026-09-19) STOP / REVIEW 1 — technically approved and closed.
- [x] M2 — identity and eligibility audit passed; review correction rerun passed;
  STOP / REVIEW 2 is ready.
- [x] M3 — approved structural corrections and validation passed;
  STOP / REVIEW 3 is ready.
- [x] (2026-09-19) Correct the M3 lifecycle registry and scaled source mapping;
  rerun the exact ten-process gate with target-color acquisition required.
- [x] (2026-09-21) Bound integral-coordinate tolerance to local float ULPs
  with a `1/1024` cap; focused numeric tests and the exact M3 gate passed.
- [ ] M4 — not started; remains gated by recorded M3 review approval.

## M1 correction gate

Passed: SDK distribution; macOS ARM64 `tcvm`, `Launcher`, and
`skia_surface_test`; native surface test; package; self-test; six accounting-
on smokes; and the exact 20-process, five-round A/B matrix.

The package is
`build/image-raster-policy-03-package-fixed/image-scroll-benchmark-macos-arm64`.
The corpus has 663 files and hash `588a7e0f4019424a`. SDK compile/deploy JAR
SHA-256 is
`4c1d1a70069dae3c8cc5e11b77eb55d69c300793a896726dfb27bf3b8538e711`.
The result ZIP SHA-256 is
`9f2d6ee03838b30814f1b76830b1fa14b80dc30929e5b02d4f58263f134c5264`.

The active software target is physically `1080x1920`, with effective
pitch/rowBytes `4320`, color type `6` (`BGRA8888`), alpha `2`,
`kN32=4`, and backend `software`. The former `56x896` was packed-field
truncation, not a physical target; it is preserved only as historical evidence.

The five-round median work P50 delta for `32795 -> 32799` is -18.66% with
accounting on and -18.54% with accounting off. Work P95 is +1.83% on and
+10.20% off; off-mode work P99 is `26478916 -> 28147333`, a delta of
`+1668417` (`+6.30%`). Tails increase in both modes. These are observations,
not a causal promotion claim.

Mask `4` is the bit-1-disabled intrinsic JPEG proof. Its off smoke recorded
175 intrinsic-known results and 2 fallback scans overall. Mask `32799`
includes `RASTER_OPACITY_METADATA` (bit 1); its off smoke recorded 175
intrinsic-known, 177 metadata-known, and 0 fallback scans. Do not describe
`32799` as bit 1 disabled.

Accounting-off retains work/paint timing and marks diagnostics unavailable.
M2 diagnostic work, the historical full profile, and the standalone decode
benchmark remain deferred.

## Decisions still active

- Do not change default optimization masks.
- Preserve delayed target-color materialization and the single raster-variant
  slot through all four milestones.
- M2 remains diagnostic-only; M3 applied only the explicitly approved
  structural corrections.
- Do not infer causality from M1 timing alone.
- M2 observed no target-key acquisition, no physical-variant hit/store, no
  cross-kind replacement, and no canvas/save-state rejection. Zero-hit causes
  remain measured eligibility and delayed observation for this workload.

## Approved M3 changes

The user's explicit instruction to start M3 authorizes the three candidate
changes listed by the plan:

1. Use canonical intrinsic target-color identity for the real target-color
   variant key: source generations and target color type only.
2. Remove physical target surface dimensions from the real variant identity
   only after the implementation and structural tests prove independence.
3. Replace blanket `saveCount == 1` rejection with a conservative proof that
   the canvas state and clip are rectangular and safe for the fast path.

Delayed materialization, default masks, the single shared slot, and fallback
behavior remain fixed. Any candidate that cannot meet the proof remains a
fallback and is not relaxed.

## Historical commit-message findings

Post-commit validation found historical lines longer than 80 characters in all
of these Plan 3 commits. They are recorded, not amended or rewritten:

- `82f4af600`: line length 266;
- `ed09cf657`: line length 141;
- `135050818`: line length 102;
- `1f5bdbbea`: line lengths 90 and 122;
- `f02f9dcff`: line length 273;
- `1fb0b1d35`: line length 86.
- `0129316af`: line lengths 114 and 87;
- `11c7349a1`: line length 94.

New commits `feea98e56`, `31bdef4bf`, `5d4b3a300`, `0528eb62b`, and
`367fc887c` are signed and
message-line compliant. History was not rewritten.

The M2 correction commits `ca71cbe0b` and `5149d7450` are signed with
Conventional titles, but their body lines exceed 80 characters. They were not
amended because this execution explicitly preserves history.

The source-scoping commit `f4dab5e23` is also signed and preserved without
amend; its body line is 83 characters.

The signed M3 follow-up commits `922ce2921` and `3c7864115` are preserved.
Their bodies contain historical lines longer than 80 characters because the
commit-message audit was run after creation and history is not rewritten:
`922ce2921` has lines 223 and 323; `3c7864115` has lines 126 and 178.

The signed ULP-boundary correction commit `404424e3c` is preserved. Its first
body line was found to exceed 80 characters by the post-commit audit; it was
not amended or rewritten. The exception is recorded here alongside the prior
historical findings.

## M2 audit gate

Passed: macOS ARM64 native rebuild, SDK distribution, package, self-test, and
the exact four-process `raster-policy-audit` matrix for masks `8192`, `16384`,
`32768`, and `57344`, with prefetch and accounting enabled. The source-scoping
implementation commit is `f4dab5e23`. The package is
`build/image-raster-policy-03-package-m2-scoped/image-scroll-benchmark-macos-arm64`.
The corpus hash is `588a7e0f4019424a`; SDK JAR hash is
`4c1d1a70069dae3c8cc5e11b77eb55d69c300793a896726dfb27bf3b8538e711`; bundle
hash is `a261d3f6f60a2b4074bb8c6f6979d201b03cc2aa443145d7c979a28381663b89`;
runtime hash is `7730aff3bf03272396bdd67d4f752d70e00ec9993851fa1685c2109de5182845`;
result ZIP hash is
`72f5602092befc2aae0d4937dc77939ecc9db3587ed3fac24d45a88cdca332eb`.

Aggregate target attempts/fallbacks/hits/materializations are `6/6/0/0`;
physical-variant lookups/misses/hits/stores/evictions are `6/6/0/0/0`;
identity attempts/hits/fallbacks are `6/0/6`. Target requests and unique
sources are `6/6`, with zero target acquisition sources and zero target keys
because eligibility failed first. Physical lookups/misses/unique sources/full/
no-surface-size keys are `6/6/6/6/6`; source-scoping corrected the prior
collapsed physical key count without changing cache behavior. All target and
identity rejections remain mapping geometry, specifically `root-to-device`;
physical variants had no mapping rejection. Save-count buckets for
target-color, physical-variant, and identity are all `[0,0,0,0,0,0]`. All
shared-slot and pending cross-kind replacements are zero.
`PARTIAL_INTERSECTION` is not emitted because no real classified condition
exists. Each process completed 189 frames and reached the automatic-scroll
endpoint in about 3.01 seconds; no timeout occurred.

## M3 structural gate

Passed at implementation HEAD `367fc887c`, preserving historical signed
implementation commit `0129316af` and validation commit `11c7349a1`. The
follow-up native correction `0528eb62b` proves finite positive axis-aligned
logical-to-physical scales, content-scale compatibility, integral source and
visible mappings, and safe clips. Unknown rectangular states, `saveLayer` or
equivalent compositing, non-rectangular clips, rotation, skew, perspective,
and inconsistent mappings remain fallback cases. Defaults, delayed
materialization, the single shared slot, and fallback behavior remain fixed.

The macOS ARM64 Release build passed for `tcvm`, `Launcher`, and
`skia_surface_test`. The native tests assert final pixels for target-color
position reuse and physical reuse across compatible surfaces, plus real 2×
mapping, clip/outside pixels, `saveLayer`, and invalid-transform fallbacks.

The final bundle is
`build/image-raster-policy-03-package-m3-scaled2/image-scroll-benchmark-macos-arm64`.
It passed self-test and the exact `raster-structural-smoke` profile: masks
`0`, `8192`, `16384`, `32768`, and `57344`; prefetch on; accounting on; two
rounds; ten processes. All 10/10 processes completed 189 frames, reached
`scroll_end=39091`, reported `overallPass=true`, and did not time out. Target
metrics remain physical `1080x1920`, rowBytes `4320`, color type `6`
(`BGRA8888`), alpha `2`, and software backend.

Aggregate target attempts/fallbacks/hits/materializations are `12/12/0/0`,
with `RootToDevice=0` and later `SourceMapping=12`. Identity
attempts/hits/fallbacks are `12/0/12`, also with `RootToDevice=0` and
`SourceMapping=12`. Physical lookups/misses/hits/stores are `12/12/0/0`,
with `12` full and `12` no-surface-size keys and no root-to-device rejects.
Disabled paths stayed at zero and writePixels accounting remained consistent.
The zero materializations/stores are delayed-observation outcomes, not a
performance conclusion.

SDK JAR, SDK ZIP, runtime, Launcher, bundle ZIP, and result ZIP hashes are
recorded in `.agent/benchmarks/image-raster-policy-03/m3-structural-validation.md`.

## M3 final corrective gate

The final implementation validation used signed commits `922ce2921` and
`3c7864115`. The native change clears the rectangular-clip registry before
bitmap replacement, backing release, screen shutdown, and color-mutation
surface replacement. Native tests prove that a reused bitmap/canvas address
does not inherit an old clip authorization.

`buildRasterPhysicalPlan()` now computes source boundaries from the double
geometry transform rather than an inverted float matrix. Four bounded float
ULPs are accepted only around an integral boundary. Target-color conversion
may use a fractional visible source edge only after full-source and bounds
proofs, preserving the original smooth sampling; identity folding and
inconsistent mappings remain fallback cases.

The final package is
`build/image-raster-policy-03-package-m3-final/image-scroll-benchmark-macos-arm64`.
Self-test passed with 663 JPEGs and corpus hash `588a7e0f4019424a`. The exact
structural matrix passed 10/10 processes: masks `0,8192,16384,32768,57344`,
prefetch on, accounting on, two rounds, 189 frames per process, and automatic
scroll endpoint reached. Aggregate target-color attempts/fallbacks/hits/
materializations/acquisition sources are `12/12/0/0/12`; target mapping
RootToDevice/SourceMapping/VisibleMapping are `0/0/0`. Identity attempts/
hits/fallbacks are `12/0/12`, with RootToDevice/SourceMapping/VisibleMapping
`0/0/12`, proving fractional clipped mappings still fall back. Physical
lookups/misses/hits/stores are `12/12/0/0`. Disabled paths remain zero and
writePixels accounting is consistent.

The tested target is physical `1080x1920`, rowBytes `4320`, BGRA8888, alpha 2,
software. Runtime SHA-256 is
`62318f5b09172aa2518c1bd9dc2d0013c7fdff548af71cc85d95b8145d584ee2`;
Launcher SHA-256 remains
`ef6f924f3beda71e6615badf94dd93d5dd167a332250aa22e6dc3855cbcf384a`;
bundle ZIP SHA-256 is
`6ee02bc362dcb7e8578044eab2b0834b0271c70ed73b1fac6330f251d87fcef5`;
result ZIP SHA-256 is
`8067a4a6b4b55a49afc49956a127b7b6cd7b65905b97376d52c8f893a1c80886`.
This remains structural evidence only; delayed materialization and M4 reuse/
RGB565 measurements remain gated.

## M3 final ULP-boundary correction

Implementation HEAD: `404424e3c` (`fix(vm,skia): bound float rounding tolerance`).
`integerDoubleValue()` now derives the local float spacing from both adjacent
`nextafterf` values and caps accepted error at `1/1024` pixel. The focused
native test accepts `999.999971 -> 1000` and `1000.000029 -> 1000`, while
rejecting `1000.01`, `1000000.1`, and `1000000.4`. This prevents large
coordinates from making material subpixel mappings eligible.

Validation passed:

- `ninja -C build/image-scroll-diagnostics-macos tcvm Launcher
  skia_surface_test`;
- `build/image-scroll-diagnostics-macos/skia_surface_test`, including the
  five ULP cases, final-pixel M3 geometry, fractional/inconsistent fallback,
  lifecycle, clip, and copy assertions;
- package self-test with 663 JPEGs and corpus hash `588a7e0f4019424a`;
- exact `raster-structural-smoke` matrix, masks
  `0,8192,16384,32768,57344`, prefetch/accounting on, two rounds, 10/10
  PASS, 189 frames each, automatic-scroll endpoint reached.

The fresh matrix aggregate remains target-color attempts/fallbacks/hits/
materializations/acquisition sources `12/12/0/0/12`, target mapping
RootToDevice/SourceMapping/VisibleMapping `0/0/0`, physical identity
attempts/hits/fallbacks `12/0/12`, identity mapping `0/0/12`, physical
variant lookups/misses/hits/stores `12/12/0/0`, disabled paths zero, and
writePixels accounting consistent. Runtime SHA-256 is
`6b5a5d7590b6fdb862a0c1e213c277ba47508d7a4bca557ed9c24c518fe8b84a`;
result ZIP SHA-256 is
`41515e8d90d518c1bfaa7c4073aedb55e886b960043f70349e089dd7cd6d431f`;
bundle ZIP SHA-256 is
`f722781d007cd3c3dcb3c0e3848e5d7013b43fade85ec7ccde8b3d24f3e62f16`.
Logs: `/tmp/image-raster-m3-ulp-build.log`,
`/tmp/image-raster-m3-ulp-native-test.log`,
`/tmp/image-raster-m3-ulp-self-test.log`, and
`/tmp/image-raster-m3-ulp-structural-matrix.log`.

All M3 acceptance criteria pass at this HEAD. `STOP / REVIEW 3` is ready;
M4 remains gated and was not started.

## Next action

STOP / REVIEW 3. M4 remains gated and has not started.

## Resume command

Read this state, the M1/M2 summaries, and the active plan gate. Before the next
commit, validate headers, stage only explicit task paths, run cached
whitespace checks, inspect the staged diff, and validate the commit message.
