<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization phase 3 state

Updated: 2026-09-06T00:55:41-03:00
Branch: `perf/image-opt-phase3-formats`
Phase-2 parent SHA: `86bfeafe388ce866236c3ae58eecb144664895e2`
Plan: `.agent/plans/exec-plan-image-opt-phase3-formats.md`

## Active milestone

Milestone 8 — corrective closure and final validation complete. Compact
backing implementation, isolated format matrices, promotion matrix, both
combined matrices, correctness matrix, and 200-sample RSS gates are complete;
focused validation, evidence cleanup, and report handoff are complete.

## Last completed slice

The harness/fixture/true-base adapter slice is committed through `d8184712b`.
The deterministic adapter digest is
`9a0bc2a348b197f597a11f10b2ca9d5787ab7b0f2937c70a643e4fc6f09018ae`.
RGB565 S1 is captured under `.agent/benchmarks/image-opt-phase3-formats/rgb565/s1/`
on exact runtime `86bfeafe388ce866236c3ae58eecb144664895e2`. Compact backing
implementation is `fb5718cb2`; final benchmark-control corrections are
`94519f0b8`, `bbd364b32`, and `119ab421c`. The corrective runtime commit is
`37746781b` and the correctness matrix commit is `6fcb50a37`.

## Active paths

- `.agent/plans/exec-plan-image-opt-phase3-formats.md`
- `.agent/state/image-opt-phase3-formats.md`
- `.agent/evidence/image-opt-phase3-formats.jsonl`
- `.agent/archive/image-opt-phase3-formats-history.md`
- `.agent/reports/image-opt-phase3-formats-editorial.md`
- `.agent/benchmarks/image-opt-phase3-formats/`

## S1/S2/S3 status

- Exact pre-item S1 runtime SHA: `86bfeafe388ce866236c3ae58eecb144664895e2`.
- Exact true-base dylib SHA-256: `32926d24c475ca3b6f04134ce4d6556c37d926862d6877d122cdec517213c4ca`.
- Final runtime SHA: `6fcb50a37651597b11388fec611a599576e7841b`.
- Final dylib SHA-256: `2864d0ee3ace6d52729bcaccad727902088327caa2bafe0769e66cbc2c0a9caa`.
- Harness digest: `9a0bc2a348b197f597a11f10b2ca9d5787ab7b0f2937c70a643e4fc6f09018ae`.
- Implementation SHA: `fb5718cb2`.
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

## Validation

Native rebuild, SDK distribution, compact smoke, decode and promotion
failure/retry checks, exact RGB565/GRAY8 parity, all isolated 60-sample
matrices, promotion, both combined matrices, and the corrective RSS gates
passed. Focused SDK tests, final header validation, generated-log cleanup, and
report size checks also passed. The exact detached-base
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

Run the focused SDK tests, SDK distribution, Release software-Skia smoke and
header/diff validation, remove only generated benchmark `*.log` artifacts, then
commit the final evidence/state/report/plan handoff. Phase 4 starts from that
final HEAD; do not begin Phase 4 here.
