<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Phase 2 raster extension state

Updated: 2026-09-07T15:39:18-03:00
Branch: `perf/image-opt-phase2-raster`
Frozen Phase 1 base: `a8a9480bd61aa510de423569af494d8dde69e8f2`
Starting Phase 2 HEAD: `a225d10165b8b60c4bf7bf2f95f5bf3b395f3a92`
Plan: `.agent/plans/exec-plan-image-opt-phase2-raster-extension-01.md`

## Active slice

Milestone 1 rebaseline is complete. The branch has benchmark workloads for
physical identity, target color, and physical variants, but no ID 13-15
runtime changes. Historical Phase 2 evidence remains unchanged.

## Last completed slice

The workloads were committed before implementation. SDK tests and distribution
build passed, the macOS software-Skia Release build passed, the true-base
adapter captured frozen Phase 1 S1, and the rebased Phase 2 S2/S3 controls
passed exact-output and focused correctness checks. S3 was escalated from 60 to
200 samples because its initial CV was 5.56%; the 200-sample CV was 4.22%.

## Active paths

- `.agent/plans/exec-plan-image-opt-phase2-raster-extension-01.md`
- `.agent/plans/exec-plan-image-opt-phase2-raster-extension-02.md`
- `.agent/plans/exec-plan-image-opt-phase2-raster-extension-03.md`
- `.agent/state/image-opt-phase2-raster-extension.md`
- `.agent/evidence/image-opt-phase2-raster-extension.jsonl`
- `.agent/archive/image-opt-phase2-raster-extension-history.md`
- `.agent/reports/image-opt-phase2-raster-extension-editorial.md`
- `.agent/benchmarks/image-opt-phase2-raster-extension/`
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/`
- `TotalCrossSDK/src/test/java/totalcross/ui/image/`
- `TotalCrossVM/src/nm/ui/skia/`

## Validation

Bootstrap checks passed:

- `git rev-parse HEAD` matched the expected Phase 2 tip;
- `git merge-base --is-ancestor a8a9480... HEAD` passed.

Milestone 1 checks passed:

- `./gradlew-agent test --tests 'totalcross.ui.image.*' --no-daemon
  --console=plain`;
- `./gradlew-agent dist -x test --no-daemon --console=plain`;
- macOS software-Skia CMake/Ninja Release build;
- focused image correctness smokes, including adaptive JPEG tiers and retry,
  APPLY_COLOR2 parity, opacity invalidation, writePixels parity, and readback
  parity;
- integrated S1/S2/S3 workloads with stable pixel, PNG, and color hashes.

The authoritative measurements are in
`.agent/benchmarks/image-opt-phase2-raster-extension/rebaseline-60/` and
`rebaseline-200/s3/`. S1 uses frozen runtime
`a8a9480bd61aa510de423569af494d8dde69e8f2` with harness source revision
`25a43c0d55bd8550fef3e992e212da9fd2d57d15` and adapter digest
`69383c1689963b04e1a94e2a0403a024a6c249105683924fab5b8572c496edf9`.
S2/S3 use production revision
`25a43c0d55bd8550fef3e992e212da9fd2d57d15`.

## Deferred validation

SDK and macOS Release software-Skia builds are deferred to the end of
Milestone 1. Android, iOS, Windows, Linux, and GPU validation are outside this
execution by plan policy.

## Decisions still active

- Preserve all historical Phase 2 benchmark artifacts; new evidence is additive.
- Keep optimization controls opt-in/default-disabled.
- Use the committed true-base adapter and exact production/harness SHAs.
- Do not modify unrelated local or generated files.

## Blockers and deliberate out-of-scope files

No blockers. Existing unrelated untracked files under `.agent`, `scripts`,
`TotalCrossVM/deps`, and `TotalCrossVM/xcode` remain deliberately untouched.

## Next concrete action

Implement `RasterPhysicalPlan` and ID 15 exactly within the software-raster
draw path, then run the focused identity smokes and S2/S3 comparison.

## Resume command

```sh
sed -n '1,220p' .agent/state/image-opt-phase2-raster-extension.md
git rev-parse HEAD
git status --short -- .agent/plans .agent/state .agent/evidence .agent/archive .agent/reports .agent/benchmarks/image-opt-phase2-raster-extension TotalCrossSDK/src/smokeTest/java/totalcross/ui/image TotalCrossSDK/src/test/java/totalcross/ui/image TotalCrossVM/src/nm/ui/skia
```
