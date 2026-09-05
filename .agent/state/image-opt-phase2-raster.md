<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization phase 2 state

Updated: 2026-09-05T19:34:00-03:00
Branch: `perf/image-opt-phase2-raster`
Base SHA: `9545c18207fab74d81340b24825c5a82ddbda7fd`
Phase-3 base: final HEAD of `perf/image-opt-phase2-raster`
Phase-3 bootstrap checkpoint SHA: `aacff9d53`
Plan: `.agent/plans/exec-plan-image-opt-phase2-raster.md`

## Active slice

The phase implementation, corrective closeout, and reproducibility closeout
are complete. Historical S1/S2/S3 artifacts remain untouched; corrected
60-sample artifacts remain under `benchmarks/.../corrections/`, and the new
200-sample gate is under `benchmarks/.../closeout-200/`. The integrated PNG is
materialized before scenario configuration and has identical full-content
hashes in S1/S2/S3. The exact true-base adapter is versioned under
`benchmarks/.../true-base-harness/`.

## Last completed slice

The row-readback/direct-color S1 baseline is captured at `8f52cfdf9` in
`readback-color/`. The true-base individual matrix is recorded by `aba3c7d61`
and the integrated 60-sample matrix by `d044e13bd`. Reproducibility is closed
by `a769c7ca6` and `f66561b99`; the 200-sample evidence is committed by
`aacff9d53`. The canonical integrated S3 row proves all five optimizations
were exercised.

## Final validation

The true-base Release software-Skia build, base SDK distribution and smoke
compilation, true-base deployments, final benchmark deployment, and all
requested 200-sample runs passed. Final integrated output and input fixture
hashes are equal across S1/S2/S3. A repeated final-runtime `pre` versus
`post-disabled` control differs by 0.95% RSS with identical medians, so the
integrated +5.26% canonical S2/S1 RSS shift is recorded as cross-runtime
revision behavior, not demonstrated disabled-feature overhead. The final
focused image tests, SDK distribution, native incremental build, and related
native smokes also passed. No runtime fix was justified.

## Active paths

- `.agent/plans/exec-plan-image-opt-phase2-raster.md`
- `.agent/state/image-opt-phase2-raster.md`
- `.agent/evidence/image-opt-phase2-raster.jsonl`
- `.agent/archive/image-opt-phase2-raster-history.md`
- `.agent/reports/image-opt-phase2-raster-editorial.md`
- `.agent/design/image-optimization-benchmark-protocol.md`
- `.agent/benchmarks/image-opt-phase2-raster/`

## Validation

Release macOS software-Skia CMake/Ninja, base SDK distribution and smoke
compilation, true-base benchmark deployment, final-runtime benchmark
deployment, the shared 200-sample RSS-aware runner, focused Image tests, the
final SDK distribution, and the related native Image smokes passed. The
closeout report records medians, P95, RSS, input/output hashes, repeat
controls, and the adapter digest. Historical reports and corrected reports
remain preserved.

## Deferred validation

Android, iOS, Windows, Linux, and GPU validation are outside this phase.

## Decisions still active

- All phase-2 optimization toggles remain disabled unless a scenario enables
  exactly one target feature.
- Old paths remain available for S1/S2/S3 comparisons.
- Only SDK and macOS software-Skia builds are in scope.
- Unrelated local changes and generated artifacts remain unstaged.
- The shared benchmark runner accepts repeated extra workload arguments so one
  RSS regime covers PNG/JPEG and operation variants.
- The 200-sample integrated S2/S1 RSS difference is retained as a measured
  cross-runtime limitation; final-runtime `pre`/`post-disabled` controls do
  not support a disabled-only runtime fix.

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
