<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization phase 2 state

Updated: 2026-09-05T17:34:00-03:00
Branch: `perf/image-opt-phase2-raster`
Base SHA: `9545c18207fab74d81340b24825c5a82ddbda7fd`
Phase-3 base SHA: `aa870bc98`
Plan: `.agent/plans/exec-plan-image-opt-phase2-raster.md`

## Active slice

The phase implementation and corrective closeout are complete. The branch was
created from the final phase-1 branch HEAD, the four benchmark workloads and
argument-capable runner are committed, and the opt-in PNG/JPEG direct decode
path has semantic retry/parity coverage. The final corrective pass also
verified direct APPLY_COLOR2 alpha parity, native-backing mutation
invalidation, zero-copy allocation-failure cleanup, process-global masks, and
the shared conservative raster-copy eligibility helper used by ordinary draws
and trivial draw plans. The historical S1/S2/S3 artifacts remain untouched;
the corrected batched recapture is under `benchmarks/.../corrections/`. The
individual S1/S2/S3 matrices now use the true Phase-1 runtime for S1; the
integrated matrix exercises all five Phase-2 features together.

## Last completed slice

The row-readback/direct-color S1 baseline is captured at `8f52cfdf9` in
`readback-color/`. The implementation is committed at `da324f3d8` with the
native row bridge follow-up at `ee7b90051`. The true-base individual matrix is
recorded by `aba3c7d61`, and the integrated matrix by `d044e13bd`. All 33
individual scenario files and all three integrated scenarios passed 60 samples
with stable full-output hashes and the 30 ms floor. The integrated S3 counter
row proves all five optimizations were exercised.

## Final validation

Focused Image tests, SDK distribution, final Release software-Skia Ninja
build, smoke-test compilation, the corrective native Image smoke family, the
true-base/final-runtime individual matrices, and the integrated matrix all
passed. The focused corrective smokes covered direct color parity, mutable
opacity invalidation, zero-copy retry and cleanup, and trivial draw-plan
writes. The exact Phase-3 source/evidence base is `aa870bc98`; the subsequent
documentation-only checkpoint does not change runtime or benchmark artifacts.

## Active paths

- `.agent/plans/exec-plan-image-opt-phase2-raster.md`
- `.agent/state/image-opt-phase2-raster.md`
- `.agent/evidence/image-opt-phase2-raster.jsonl`
- `.agent/archive/image-opt-phase2-raster-history.md`
- `.agent/reports/image-opt-phase2-raster-editorial.md`
- `.agent/design/image-optimization-benchmark-protocol.md`
- `.agent/benchmarks/image-opt-phase2-raster/`

## Validation

SDK `dist -x test`, focused `totalcross.ui.image.*` tests, smoke-test Java
compilation, Release macOS software-Skia CMake/Ninja, four corrective native
Image smokes, true-base benchmark deployment, final-runtime benchmark
deployment, and the shared 60-sample RSS-aware runner passed. The corrected reports under
`.agent/benchmarks/image-opt-phase2-raster/corrections/` retain full CSV
samples, stable full-content hashes, proof counters, and the disabled-metadata
fallback scan. The historical reports under the original item directories
remain preserved for comparison.

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
