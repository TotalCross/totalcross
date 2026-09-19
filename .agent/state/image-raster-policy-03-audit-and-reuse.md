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
- [x] M2 — identity and eligibility audit passed; STOP / REVIEW 2 is ready.
- [ ] M3–M4 — not started; each remains gated by recorded review approval.

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
- M2 is diagnostic-only; structural corrections require explicit review.
- Do not infer causality from M1 timing alone.
- M2 observed no target-key acquisition, no physical-variant hit/store, no
  cross-kind replacement, and no canvas/save-state rejection. Zero-hit causes
  remain measured eligibility and delayed observation for this workload.

## Approved M3 changes

None. M3 is not authorized until M2 review.

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

## M2 audit gate

Passed: macOS ARM64 native rebuild, SDK distribution, package, self-test, and
the exact four-process `raster-policy-audit` matrix for masks `8192`, `16384`,
`32768`, and `57344`, with prefetch and accounting enabled. The package is
`build/image-raster-policy-03-package-m2/image-scroll-benchmark-macos-arm64`.
The corpus hash is `588a7e0f4019424a`; SDK JAR hash is
`4c1d1a70069dae3c8cc5e11b77eb55d69c300793a896726dfb27bf3b8538e711`; bundle
hash is `a9fe55d035fd99fe768b801c6f619e1b37741ab3999f8bfc91960edc5a370120`;
result ZIP hash is
`95e4d7c97bb95a12b7ee911a350572bc967f25d6001a7464834e2f1d01df9971`.

Aggregate target attempts/fallbacks/hits/materializations are `6/6/0/0`;
physical-variant lookups/misses/hits/stores/evictions are `6/6/0/0/0`;
identity attempts/hits/fallbacks are `6/0/6`. All target and identity
rejections were mapping geometry. Save-state rejects and all shared-slot and
pending cross-kind replacements are zero. Physical full/no-surface-size keys
are `2/2`; target key acquisition was `0/0` because eligibility failed first.

## Next action

STOP / REVIEW 2. Await explicit reviewer recording of exact approved M3
structural changes. M3 is not authorized and must not start from this state.

## Resume command

Read this state, the M1/M2 summaries, and the active plan gate. Before the next
commit, validate headers, stage only explicit task paths, run cached
whitespace checks, inspect the staged diff, and validate the commit message.
