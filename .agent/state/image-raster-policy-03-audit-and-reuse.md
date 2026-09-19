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

New commits `feea98e56`, `31bdef4bf`, and `5d4b3a300` are signed and
message-line compliant. History was not rewritten.

The M2 correction commits `ca71cbe0b` and `5149d7450` are signed with
Conventional titles, but their body lines exceed 80 characters. They were not
amended because this execution explicitly preserves history.

The source-scoping commit `f4dab5e23` is also signed and preserved without
amend; its body line is 83 characters.

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

Passed: signed implementation commit `0129316af` applied exactly the three
approved changes: canonical intrinsic target-color identity, removal of
physical target surface dimensions from the real key, and conservative
rectangular saved-clip eligibility. Delayed materialization, default masks,
the single shared slot, and fallback behavior remain unchanged.

Native macOS ARM64 build and `skia_surface_test` passed. The native tests cover
target position changes, compatible surface sizes, saved rectangular clips,
outside-clip protection, translated positive-scale matrices, and fallback for
non-rectangular clips, rotation, and skew.

The final bundle is
`build/image-raster-policy-03-package-m3-native/image-scroll-benchmark-macos-arm64`.
It passed self-test and the exact `raster-structural-smoke` profile: masks
`0`, `8192`, `16384`, `32768`, and `57344`; prefetch on; accounting on; two
rounds; ten processes. Each process completed 189 frames and reached scroll
endpoint `39091`; no timeout occurred. Target metrics remain physical
`1080x1920`, rowBytes `4320`, color type `6` (`BGRA8888`), alpha `2`, and
software backend.

Aggregate structural counters are target attempts/fallbacks/hits/materializations
`12/12/0/0`, all target mapping rejects `root-to-device`; physical lookups,
misses, hits, and stores `12/12/0/0`, with `12` full and no-surface-size keys;
and identity attempts/hits/fallbacks `12/0/12`, all identity mapping rejects
`root-to-device`. The runner verified disabled paths stayed at zero and
writePixels accounting remained consistent. This is structural evidence, not
a performance-promotion gate.

Bundle SHA-256 is
`70fe022ebb57991f0aa69d53e3c894994ce9ea49bb501605635a17b3eaf879f`;
runtime SHA-256 is
`a7fd330a1fc983d4a0e63e6a53a91edc766995b6cf35e06f6f4218448ad838d3`;
result ZIP SHA-256 is
`da92c7308be840f83da66ca0b361abac55e4eef4a82583255419307fb76666ca`.
Detailed rows are in
`.agent/benchmarks/image-raster-policy-03/m3-structural-validation.md`.

## Next action

STOP / REVIEW 3. M4 remains gated and has not started.

## Resume command

Read this state, the M1/M2 summaries, and the active plan gate. Before the next
commit, validate headers, stage only explicit task paths, run cached
whitespace checks, inspect the staged diff, and validate the commit message.
