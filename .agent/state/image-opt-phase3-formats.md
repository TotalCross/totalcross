<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization phase 3 state

Updated: 2026-09-05T00:00:00-03:00
Branch: `perf/image-opt-phase3-formats`
Phase-2 parent SHA: `86bfeafe388ce866236c3ae58eecb144664895e2`
Plan: `.agent/plans/exec-plan-image-opt-phase3-formats.md`

## Active milestone

Milestone 0 — bootstrap and freeze contract. The branch is based on the exact
accepted Phase-2 HEAD. No Phase-3 runtime implementation, benchmark, or build
has run yet.

## Last completed slice

The Phase-3 plan and resumable support-file skeletons are ready for the
benchmark-freeze slice. The runtime remains unchanged from Phase 2.

## Active paths

- `.agent/plans/exec-plan-image-opt-phase3-formats.md`
- `.agent/state/image-opt-phase3-formats.md`
- `.agent/evidence/image-opt-phase3-formats.jsonl`
- `.agent/archive/image-opt-phase3-formats-history.md`
- `.agent/reports/image-opt-phase3-formats-editorial.md`
- `.agent/benchmarks/image-opt-phase3-formats/`

## S1/S2/S3 status

- Exact pre-item S1 runtime SHA: not applicable before the harness freeze.
- Harness digest: not created.
- Implementation SHA: not created.
- RGB565: S1/S2/S3 not started.
- GRAY8: S1/S2/S3 not started.
- ARGB4444: S1/S2/S3 not started.
- Promotion matrix: S1/S2/S3 not started.
- Combined matrices: S1/S2/S3 not started.

## Validation

No build, smoke, or benchmark has run for Phase 3. The branch/bootstrap
checkpoint is documentation-only.

## Deferred validation

SDK/macOS software-Skia build and all native smokes begin only after the full
harness, fixtures, correctness oracles, counters, deployment tasks, and
true-base adapter are committed. Android, iOS, Windows, Linux, and GPU remain
outside this phase contract.

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

Inspect the Phase-2 benchmark harness and create all Phase-3 fixture/workload,
oracle, counter, deployment, and true-base adapter files before runtime edits.

```sh
rg -n "Image.*Benchmark|runImage|IMAGE_OPT|DECODE_ZERO_COPY|RASTER_" \
  TotalCrossSDK TotalCrossVM scripts .agent/benchmarks/image-opt-phase2-raster
```

