<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Phase 2 raster extension state

Updated: 2026-09-07T15:08:22-03:00
Branch: `perf/image-opt-phase2-raster`
Frozen Phase 1 base: `a8a9480bd61aa510de423569af494d8dde69e8f2`
Starting Phase 2 HEAD: `a225d10165b8b60c4bf7bf2f95f5bf3b395f3a92`
Plan: `.agent/plans/exec-plan-image-opt-phase2-raster-extension-01.md`

## Active slice

Milestone 0 bootstrap is complete. The branch is at the expected Phase 2 tip,
and the frozen Phase 1 base is an ancestor. No extension runtime changes have
been made. Historical Phase 2 evidence remains unchanged.

## Last completed slice

Bootstrap verified branch ancestry and recorded the pre-extension Phase 2
state. The continuation skeletons were created in this commit.

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

No build is required for the documentation-only bootstrap milestone.

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

Create the physical-identity, target-color, and physical-variant benchmark
workloads before implementing controls 13-15, then commit only those sources.

## Resume command

```sh
sed -n '1,220p' .agent/state/image-opt-phase2-raster-extension.md
git rev-parse HEAD
git status --short -- .agent/plans .agent/state .agent/evidence .agent/archive .agent/reports .agent/benchmarks/image-opt-phase2-raster-extension TotalCrossSDK/src/smokeTest/java/totalcross/ui/image TotalCrossSDK/src/test/java/totalcross/ui/image TotalCrossVM/src/nm/ui/skia
```
