<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization phase 2 state

Updated: 2026-09-05T00:00:00-03:00
Branch: `perf/image-opt-phase2-raster`
Base SHA: `9545c18207fab74d81340b24825c5a82ddbda7fd`
Plan: `.agent/plans/exec-plan-image-opt-phase2-raster.md`

## Active slice

Milestone 0 is in progress. The branch was created from the final phase-1
branch HEAD. Tracking files are being created before benchmark implementation.

## Next concrete action

Create and commit the four phase-2 benchmark workloads, then build the SDK and
macOS software-Skia runtime and capture the zero-copy S1 baseline.

## Active paths

- `.agent/plans/exec-plan-image-opt-phase2-raster.md`
- `.agent/state/image-opt-phase2-raster.md`
- `.agent/evidence/image-opt-phase2-raster.jsonl`
- `.agent/archive/image-opt-phase2-raster-history.md`
- `.agent/reports/image-opt-phase2-raster-editorial.md`
- `.agent/design/image-optimization-benchmark-protocol.md`
- `.agent/benchmarks/image-opt-phase2-raster/`

## Validation

No phase-2 validation has run yet. Branch ancestry and the exact phase-1 base
SHA were checked before creating this branch.

## Deferred validation

SDK/native builds, benchmarks, and platform validation remain deferred until the
benchmark workload commit. Android, iOS, Windows, Linux, and GPU validation are
outside this phase.

## Decisions still active

- All phase-2 optimization toggles remain disabled unless a scenario enables
  exactly one target feature.
- Old paths remain available for S1/S2/S3 comparisons.
- Only SDK and macOS software-Skia builds are in scope.
- Unrelated local changes and generated artifacts remain unstaged.

## Blockers and deliberate out-of-scope files

There are no blockers. The repository contains unrelated untracked files under
the SDK, VM, scripts, and `.agent`; none are part of this phase-2 slice.

## Resume command

```sh
sed -n '1,220p' .agent/state/image-opt-phase2-raster.md
sed -n '179,470p' .agent/plans/exec-plan-image-opt-phase2-raster.md
git log -1 --oneline
```
