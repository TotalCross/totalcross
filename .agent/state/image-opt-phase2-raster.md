<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization phase 2 state

Updated: 2026-09-05T13:33:58-03:00
Branch: `perf/image-opt-phase2-raster`
Base SHA: `9545c18207fab74d81340b24825c5a82ddbda7fd`
Plan: `.agent/plans/exec-plan-image-opt-phase2-raster.md`

## Active slice

Milestone 1 is complete. The branch was created from the final phase-1 branch
HEAD, the four benchmark workloads and argument-capable runner are committed,
and zero-copy S1 has been captured for PNG and JPEG.

## Next concrete action

Implement `DECODE_ZERO_COPY` behind its disabled default, preserving the
current copied path and exactly-once ownership behavior. Then build the SDK and
macOS software-Skia runtime, run the focused decode/native-materialization
smokes, and capture S2/S3.

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
CMake/Ninja, benchmark deployment, and the shared 60-sample RSS-aware runner
passed. PNG S1 median/P95 was 4/4 ms with 139264 KiB peak RSS; JPEG S1
median/P95 was 20/21 ms with 162512 KiB peak RSS. Full raw samples and compact
summaries are under `.agent/benchmarks/image-opt-phase2-raster/zero-copy/`.

## Deferred validation

S2/S3 implementation results, focused Image tests, native Image smokes, and
final SDK/native validation remain deferred. Android, iOS, Windows, Linux, and
GPU validation are outside this phase.

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
