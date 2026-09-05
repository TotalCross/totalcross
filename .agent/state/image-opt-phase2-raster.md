<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization phase 2 state

Updated: 2026-09-05T14:12:00-03:00
Branch: `perf/image-opt-phase2-raster`
Base SHA: `9545c18207fab74d81340b24825c5a82ddbda7fd`
Plan: `.agent/plans/exec-plan-image-opt-phase2-raster.md`

## Active slice

Milestone 3 is complete. The branch was created from
the final phase-1 branch HEAD, the four benchmark workloads and
argument-capable runner are committed, zero-copy S1/S2/S3 was captured for PNG
and JPEG, and the opt-in PNG/JPEG direct decode path is implemented with
semantic retry/parity coverage. Opacity metadata is implemented with
source/decode proofs, cached backing state, and conservative invalidation.
Opacity S1 covers all four required fixture kinds at `f4f1a6ad9`; S2/S3 is
recorded at `f6a4e1227`.

## Next concrete action

Implement `RASTER_OPAQUE_WRITE_PIXELS`, preserving the opacity gate and
recording write attempt/hit/fallback counters. Capture its S1 before changing
the runtime write path, then validate the enabled path before moving to row
readback and direct color materialization.

## Active paths

- `.agent/plans/exec-plan-image-opt-phase2-raster.md`
- `.agent/state/image-opt-phase2-raster.md`
- `.agent/evidence/image-opt-phase2-raster.jsonl`
- `.agent/archive/image-opt-phase2-raster-history.md`
- `.agent/reports/image-opt-phase2-raster-editorial.md`
- `.agent/design/image-optimization-benchmark-protocol.md`
- `.agent/benchmarks/image-opt-phase2-raster/`

## Validation

SDK `dist -x test`, smoke-test Java compilation, Release macOS software-Skia
CMake/Ninja, native materialization/JPEG smokes, zero-copy parity/retry smoke,
benchmark deployment, and the shared 60-sample RSS-aware runner passed. PNG S1
median/P95 was 4/4 ms with 139264 KiB peak RSS; JPEG S1 median/P95 was 20/21 ms
with 162512 KiB peak RSS. S2/S3 showed no disabled timing regression; enabled
JPEG median was 16 ms and copied decode bytes fell to zero. Full raw samples
and compact summaries are under `.agent/benchmarks/image-opt-phase2-raster/zero-copy/`.

## Deferred validation

WritePixels S1/S2/S3, row-readback/color materialization, focused Image unit
tests, and final SDK/native validation remain deferred. Android, iOS, Windows,
Linux, and GPU validation are outside this phase.

## Decisions still active

- All phase-2 optimization toggles remain disabled unless a scenario enables
  exactly one target feature.
- Old paths remain available for S1/S2/S3 comparisons.
- Only SDK and macOS software-Skia builds are in scope.
- Unrelated local changes and generated artifacts remain unstaged.
- The shared benchmark runner accepts repeated extra workload arguments so one
  RSS regime covers PNG/JPEG and operation variants.

## Blockers and deliberate out-of-scope files

There are no runtime blockers. The repository contains unrelated untracked
files under the SDK, VM, scripts, and `.agent`; none are part of this phase-2
slice. The bootstrap and workload commits contain literal `\\n` sequences in
their bodies because of shell quoting; they were preserved without amendment
per the no-history-rewrite rule, and all later commit messages are validated
with literal newlines.

## Resume command

```sh
sed -n '1,220p' .agent/state/image-opt-phase2-raster.md
sed -n '179,470p' .agent/plans/exec-plan-image-opt-phase2-raster.md
git log -1 --oneline
```
