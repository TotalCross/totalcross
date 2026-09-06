<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization phase 3 state

Updated: 2026-09-05T21:25:00-03:00
Branch: `perf/image-opt-phase3-formats`
Phase-2 parent SHA: `86bfeafe388ce866236c3ae58eecb144664895e2`
Plan: `.agent/plans/exec-plan-image-opt-phase3-formats.md`

## Active milestone

Milestone 2 — generic infrastructure and RGB565. The harness is frozen and
the exact pre-RGB565 S1 is captured; runtime implementation has not started.

## Last completed slice

The harness/fixture/true-base adapter slice is committed through `d8184712b`.
The deterministic adapter digest is
`9a0bc2a348b197f597a11f10b2ca9d5787ab7b0f2937c70a643e4fc6f09018ae`.
RGB565 S1 is captured under `.agent/benchmarks/image-opt-phase3-formats/rgb565/s1/`
on exact runtime `86bfeafe388ce866236c3ae58eecb144664895e2`. The runtime remains
unchanged from Phase 2.

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
- Implementation SHA: not created.
- RGB565: S1 captured; implementation/S2/S3 not started.
- GRAY8: S1/S2/S3 not started.
- ARGB4444: S1/S2/S3 not started.
- Promotion matrix: S1/S2/S3 not started.
- Combined matrices: S1/S2/S3 not started.

## Validation

SDK distribution, smoke compilation, Release software-Skia native build,
true-base deployment, fallback smoke, and seven three-sample workload
dry-runs passed. RGB565 S1 completed with 60 samples, stable hashes, 62–64 ms
elapsed samples, and peak RSS 139520 KiB. The exact detached-base CMake
configure was deferred after the pinned qrcodegen asset returned HTTP 404; the
native source tree is byte-equivalent to the current branch and the existing
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

Inspect the existing PNG/JPEG decode ownership and Skia backing APIs, then add
generic format metadata/accounting and transactional promotion without making
any compact format selectable yet.

```sh
rg -n "Image.*Benchmark|runImage|IMAGE_OPT|DECODE_ZERO_COPY|RASTER_" \
  TotalCrossSDK TotalCrossVM scripts .agent/benchmarks/image-opt-phase2-raster
```
