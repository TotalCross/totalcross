<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Phase 2 raster extension state

Updated: 2026-09-07T16:59:07-03:00
Branch: `perf/image-opt-phase2-raster`
Frozen Phase 1 base: `a8a9480bd61aa510de423569af494d8dde69e8f2`
Starting Phase 2 HEAD: `a225d10165b8b60c4bf7bf2f95f5bf3b395f3a92`
Plan: `.agent/plans/exec-plan-image-opt-phase2-raster-extension-02.md`

## Active slice

Extension 01 is complete through the physical identity folding and handoff
milestones. Its final handoff HEAD is
`de5ad089e68e39a1224a87247aad026e6d31baab`; the ID 15 production runtime is
`31c3d0fa40d955806df18e1dad1ba79fa301a606`. The test-raster factory
registration was corrected in `5cbe01782`, `f5be3f190`, and `07912a0f2`.
Historical Phase 2 evidence remains unchanged.

## Last completed slice

ID 15 was implemented after the workloads and validated with exact-output
identity, nearest, non-identity, alpha/save, dynamic-hwScale, crop, and frame
smokes. SDK tests/dist and the macOS software-Skia Release build passed. The
identity S2/S3 comparison used 60 samples because both CVs stayed below 5%:
S2 median 5483 ms with zero identity counters and 114080 KiB peak RSS; S3
median 1060 ms with 1024 hits and 1024 avoided resamples per batch and 115728
KiB peak RSS. Both scenarios produced pixel hash `000000D600000165`.

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
- Milestone 2 SDK test/dist and macOS Release software-Skia build;
- physical identity smooth/nearest, non-identity, alpha/save, dynamic-hwScale,
  crop, frame, geometry, and writePixels smokes;
- identity S2/S3 workloads with exact output parity and counter assertions;
- focused image correctness smokes, including adaptive JPEG tiers and retry,
  APPLY_COLOR2 parity, opacity invalidation, writePixels parity, and readback
  parity;
- integrated S1/S2/S3 workloads with stable pixel, PNG, and color hashes;
- extension 02 S1 target-color RGBA control: 60 samples, median 5477 ms,
  P95 5493 ms, CV 0.22%, peak RSS 115296 KiB, hash `000000D600000165`;
- extension 02 S1 target-color BGRA/RGB565/translucent probes: one sample each,
  all passed with hashes `000000D600000165`, `0000AD0400003765`, and
  `0000F55E00000165`;
- extension 02 S1 physical-variant repeat: 60 samples, median 1326 ms,
  P95 1328 ms, CV 0.13%, peak RSS 114160 KiB, hash `000000D600000165`;
- extension 02 S1 physical-variant first/mutation/size probes: one sample each,
  all passed with hash `000000D600000165`.

The authoritative measurements are in
`.agent/benchmarks/image-opt-phase2-raster-extension/rebaseline-60/`,
`rebaseline-200/s3/`, `identity-s2-60/`, and `identity-s3-60/`. S1 uses frozen runtime
`a8a9480bd61aa510de423569af494d8dde69e8f2` with harness source revision
`25a43c0d55bd8550fef3e992e212da9fd2d57d15` and adapter digest
`69383c1689963b04e1a94e2a0403a024a6c249105683924fab5b8572c496edf9`.
Rebaseline S2/S3 use production revision
`25a43c0d55bd8550fef3e992e212da9fd2d57d15`; identity S2/S3 use
`31c3d0fa40d955806df18e1dad1ba79fa301a606`. Extension 02 S1 uses production
and harness revision `07912a0f2d5d48705a5b7caa65f82a71ed7d3b57`; its additive
captures are under `target-color-s1-60/`, `target-color-s1-1/`,
`physical-variant-s1-60/`, and `physical-variant-s1-1/`.

## Deferred validation

Android, iOS, Windows, Linux, and GPU validation are outside this execution by
plan policy. The commit-message checker flagged overlong body lines on three
earlier extension-01 commits; history was preserved without amendment. The
extension-02 factory correction commit passed the checker.

## Decisions still active

- Preserve all historical Phase 2 benchmark artifacts; new evidence is additive.
- Keep optimization controls opt-in/default-disabled.
- Use the committed true-base adapter and exact production/harness SHAs.
- Do not modify unrelated local or generated files.

## Blockers and deliberate out-of-scope files

No blockers. Existing unrelated untracked files under `.agent`, `scripts`,
`TotalCrossVM/deps`, and `TotalCrossVM/xcode` remain deliberately untouched.

## Next concrete action

Implement ID 13 target-color conversion in the one-slot derived-raster
scaffold, keeping the recorded S1 captures immutable.

## Resume command

```sh
sed -n '1,220p' .agent/state/image-opt-phase2-raster-extension.md
git rev-parse HEAD
git status --short -- .agent/plans .agent/state .agent/evidence .agent/archive .agent/reports .agent/benchmarks/image-opt-phase2-raster-extension TotalCrossSDK/src/smokeTest/java/totalcross/ui/image TotalCrossSDK/src/test/java/totalcross/ui/image TotalCrossVM/src/nm/ui/skia
```
