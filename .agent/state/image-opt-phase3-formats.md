<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization phase 3 state

Updated: 2026-09-05T23:30:00-03:00
Branch: `perf/image-opt-phase3-formats`
Phase-2 parent SHA: `86bfeafe388ce866236c3ae58eecb144664895e2`
Plan: `.agent/plans/exec-plan-image-opt-phase3-formats.md`

## Active milestone

Milestone 8 — final validation and handoff complete. Compact backing
implementation, isolated format matrices, promotion matrix, both combined
matrices, corrective matched-control baselines, focused tests, copyright
validation, and final compact smoke are complete.

## Last completed slice

The harness/fixture/true-base adapter slice is committed through `d8184712b`.
The deterministic adapter digest is
`9a0bc2a348b197f597a11f10b2ca9d5787ab7b0f2937c70a643e4fc6f09018ae`.
RGB565 S1 is captured under `.agent/benchmarks/image-opt-phase3-formats/rgb565/s1/`
on exact runtime `86bfeafe388ce866236c3ae58eecb144664895e2`. Compact backing
implementation is `fb5718cb2`; final benchmark-control corrections are
`94519f0b8`, `bbd364b32`, and `119ab421c`.

## Active paths

- `.agent/plans/exec-plan-image-opt-phase3-formats.md`
- `.agent/state/image-opt-phase3-formats.md`
- `.agent/evidence/image-opt-phase3-formats.jsonl`
- `.agent/archive/image-opt-phase3-formats-history.md`
- `.agent/reports/image-opt-phase3-formats-editorial.md`
- `.agent/benchmarks/image-opt-phase3-formats/`

## S1/S2/S3 status

- Exact pre-item S1 runtime SHA: `86bfeafe388ce866236c3ae58eecb144664895e2`.
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
- Combined matrices: exact-base and matched-control S1s are preserved; both
  disabled-stack and full-stack S1/S2/S3 matrices pass with zero compact-source
  promotions. Full-stack S2 median is 160 ms vs matched S1 160 ms.

## Validation

SDK distribution, focused `totalcross.ui.image.*` tests, Release software-Skia
native build, compact smoke, decode and
promotion failure/retry checks, exact write-pixels parity, all isolated 60-sample
matrices, promotion, and both combined matrices passed. The exact detached-base
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

Commit the final evidence/state/report/plan handoff as
`docs(image): complete compact backing phase`; Phase 4 starts from that HEAD.
