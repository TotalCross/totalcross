<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization phase 3 state

Updated: 2026-09-07T03:20:00-03:00
Branch: `perf/image-opt-phase3-formats`
Phase-2 parent SHA: `4d3177801a29752bc3e7b17754400001fef6270f`
Plan: `.agent/plans/exec-plan-image-opt-phase3-formats.md`

## Active milestone

Milestone 8 — corrective closure and final validation complete. Compact
backing implementation, isolated format matrices, promotion matrix, both
combined matrices, correctness matrix, and 200-sample RSS gates are complete;
focused validation, evidence cleanup, and report handoff are complete.

## Last completed slice

The harness/fixture/true-base adapter slice is committed through rebased
checkpoint `b206daacd473a9c8a4af53b3775a97ccf71ad0d4`.
The deterministic adapter digest is
`9a0bc2a348b197f597a11f10b2ca9d5787ab7b0f2937c70a643e4fc6f09018ae`.
The corrected final-workload overlay used for the authoritative true-base RSS
recheck has digest
`edd9a79ebb30d081681b141a245a8525788b74df4a55712113ecf7a9c2335d7d`.
RGB565 S1 is captured under `.agent/benchmarks/image-opt-phase3-formats/rgb565/s1/`
on exact pre-rebase runtime `86bfeafe388ce866236c3ae58eecb144664895e2`.
Compact backing implementation is rebased checkpoint
`5dbadf3bb7ea44ee14472ba25ab2b27b2fb3a2b9`; final benchmark-control corrections
are `9443ca28e55c3f69859602097b0db8a815e6cd6e`,
`2f3d692d4ba2c015c46422dc2e1a6ca7e6d30ed7`, and
`91b277c10ef5ad7ed622d556a01c6a56ff4ec910`. The corrective runtime commit is
`a212f76e64c5415af8d6a3843e87ce82030db94e` and the correctness matrix commit is
`4e52067003b899566de67f01a9f50000326cbd98`.

## Active paths

- `.agent/plans/exec-plan-image-opt-phase3-formats.md`
- `.agent/state/image-opt-phase3-formats.md`
- `.agent/evidence/image-opt-phase3-formats.jsonl`
- `.agent/archive/image-opt-phase3-formats-history.md`
- `.agent/reports/image-opt-phase3-formats-editorial.md`
- `.agent/benchmarks/image-opt-phase3-formats/`

## S1/S2/S3 status

- Exact pre-item S1 runtime SHA (historical pre-rebase evidence):
  `86bfeafe388ce866236c3ae58eecb144664895e2`.
- Exact true-base dylib SHA-256: `32926d24c475ca3b6f04134ce4d6556c37d926862d6877d122cdec517213c4ca`.
- Final Phase-3 HEAD: the tip of `perf/image-opt-phase3-formats` after the
  documentation-only history correction.
- Final dylib SHA-256: `2864d0ee3ace6d52729bcaccad727902088327caa2bafe0769e66cbc2c0a9caa`.
- Harness digest: `9a0bc2a348b197f597a11f10b2ca9d5787ab7b0f2937c70a643e4fc6f09018ae`.
- Rebased implementation checkpoint: `5dbadf3bb7ea44ee14472ba25ab2b27b2fb3a2b9`.
- RGB565: exact S1 plus 60-sample S2/S3 complete; S2 median 63 ms vs 62 ms,
  S3 selects RGB565 and uses 1,048,576 compact bytes.
- GRAY8: exact-base S1 and matched final-harness S1 preserved; S2/S3 complete,
  S3 selects GRAY8, uses one-byte storage, and has zero temporary RGBA decode
  bytes.
- ARGB4444: exact-base S1 and matched final-harness S1 preserved; S2/S3
  complete, S3 model error is 0 and black/white composite max error is 16.
- Promotion matrix: matched-control S1/S2/S3 complete; S3 selects all three
  compact formats and promotes each exactly once per repetition.
- Combined matrices: exact-base and matched-control S1s are preserved; final
  60-sample matrices after the row-conversion correction report disabled-stack
  S1/S2/S3 medians 263/263/184 ms and full-stack medians 160/161/162 ms.
  Both S3 matrices use `RGB565|RGB565|GRAY8|GRAY8|ARGB4444`, zero compact-source
  promotions, and zero temporary RGBA decode bytes. Full-stack S3 improved from
  the previous 169 ms median to 162 ms.

## Corrective 200-sample RSS gate

- ARGB4444: 127088 -> 139456 KiB (+9.7%); required sample-100/150 `vmmap -summary`
  and `ps` captures are under `argb4444/rss-200/diagnostics/`. S2 peak physical
  footprint was 81.3M vs S1 84.4M; the signal is recorded as unconfirmed
  allocator-residency variation.
- Promotion: 153216 -> 153744 KiB (+0.3%).
- Combined-disabled: 147904 -> 151504 KiB (+2.4%).
- Combined-enabled full stack: 157568 -> 152496 KiB (-3.2%).

The authoritative ARGB4444 three-pair recheck is under
`argb4444/rss-200-corrective-3pairs-matched-harness/` and used order
S1->S2, S2->S1, S1->S2. S2 peak-RSS deltas were -0.7%, +7.1%, and -4.3%;
matched S2 peak physical-footprint deltas were -4.9%, -2.6%, and +2.0%.
Checkpoint physical/private writable residency changed with run order. The
frozen rule rejects a reproducible disabled-path regression; runtime source was
not changed.

## Validation

Native rebuild, SDK distribution, compact smoke, decode and promotion
failure/retry checks, exact RGB565/GRAY8 parity, all isolated 60-sample
matrices, promotion, both combined matrices, and the initial corrective RSS
gate passed. The authoritative three-pair ARGB4444 recheck is complete and
rejects the anomaly under the frozen rule without a runtime change. Focused SDK
tests, final header validation, generated-log cleanup, and report size checks
also passed before the history correction. These measurements and their
benchmark artifacts are historical pre-rebase evidence; they were not rerun
or relabeled as measurements on the rebased SHAs. The exact detached-base
CMake configure was deferred after the pinned qrcodegen asset returned HTTP 404;
the native source tree is byte-equivalent to the current branch and the existing
Release dylib was used as the exact Phase-2-compatible runtime.

## Deferred validation

Android, iOS, Windows, Linux, and GPU remain outside this phase contract.

## Decisions still active

- Compact formats are internal, opt-in, and disabled by default.
- Precedence is GRAY8 > RGB565 > ARGB4444 > RGBA8888.
- Compact formats are source-only; mutable/full-precision barriers promote
  transactionally to RGBA8888.
- The Phase-2 true-base adapter must be committed before runtime changes.
- Unrelated generated/untracked files remain untouched.

## Blockers and deliberate out-of-scope files

No Phase-3 blocker. Existing unrelated untracked paths are deliberately out of
scope: `TotalCrossVM/deps/wince-deps/` and `TotalCrossVM/xcode/generated/`.

## Next exact command

No further Phase-3 validation command is pending. Phase 4 starts from the final
HEAD of this branch; do not begin Phase 4 here.
