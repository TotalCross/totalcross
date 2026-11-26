<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Persistent image ownership: implementation handoff

## Branches and commits

The TotalCross implementation is on `fix/image-persistent-ownership-scaling`
in `/Users/flsobral/.codex/worktrees/fix-image-persistent-ownership-scaling`.
Its base and merge-base are `6b915883dd202f6db891990f6c3af26fb645ecc7`
(`origin/master`). The implementation and CI commit is
`ed026ea40c6e256335f288b7e5e39c75da857638`; a final documentation closeout
commit records this report and the reconciled plan state.

Logical commits:

| Commit | Subject |
|---|---|
| `051e9d2146ba454d76a2ca1eb7e9ee890d28ccfe` | `test(image): reproduce persistent ownership scaling leaks` |
| `4cbd85fb6c7b9bd1dbff2cb07dafaebab88361e4` | `fix(image): suspend owners in detached subtrees` |
| `207292bc46b1e6b47c0f9f738f6a5e8c357453a5` | `perf(image): index persistent drawing ownership` |
| `b874f5fb0940ac00b7dca61be455db8f9beabbef` | `perf(image): bound persistent owner maintenance` |
| `ed026ea40c6e256335f288b7e5e39c75da857638` | `ci(image): run persistent ownership target smokes` |

The companion performance-lab branch is `fix/image-persistent-ownership-smoke`
in `/Users/flsobral/.codex/worktrees/totalcross-performance-lab-image-ownership`.
It is based at `fe23e009fa2fcdbf8c36746cea58b9946a138858`; HEAD is
`bae23107b57390525a90669086e9fb47efaaa440`.

| Commit | Subject |
|---|---|
| `acb9bd5c4f89783304ce59d9eb020350260d629e` | `test(image): add native ownership lifecycle smoke` |
| `b922a23f9bdbb60302de54ee5632e9fce60f222b` | `fix(image): set smoke runtime home` |
| `bae23107b57390525a90669086e9fb47efaaa440` | `fix(image): read Windows smoke console output` |

## Implementation

The SDK tracks explicit suspension of detached Container subtrees and
reconciles descendant `ImageControl` image slots on structural changes. This
preserves ownership during detached construction, active reparenting, popup
and visibility changes, while releasing associations promptly when an
attached subtree is removed.

`PersistentImageOwnershipRegistry` now uses weak identity-hash indexes for
owners and images, collision-safe identity checks, aggregate per-image counts,
and bounded stale-relation cleanup. Pipeline transfers use aggregate counts.
Image resolution and draw-cache lookup no longer sweep the registry. A narrow
maintenance bridge runs at most once per 64 outermost repaint entries and
skips nested repaint calls and empty registries. The new bridge remains outside
the public `totalcross-api` artifact.

The baseline and indexed registration inspections were:

| Owner-image pairs | Baseline inspections | Indexed inspections |
|---:|---:|---:|
| 500 | 249,500 | 14,855 |
| 1,510 | 2,278,590 | 47,596 |
| 3,020 | 9,117,380 | 97,070 |

The baseline draw/cache paths inspected all 1,510 relationships twice. The
indexed version records zero global owner scans per image resolution/draw.

## Repaint comparison

The benchmark used 20 warmup pairs, 200 samples per configuration, 64 repaint
entries per sample, and 1,510 populated owners. Values are nanoseconds.

| Registry | Maintenance | p50 | p95 | Inspections |
|---|---|---:|---:|---:|
| Empty | Disabled | 176,250 | 246,084 | 0 |
| Empty | Enabled | 165,292 | 258,125 | 0 |
| Populated | Disabled | 140,458 | 187,417 | 0 |
| Populated | Enabled | 141,792 | 195,459 | 6,296 |

For paired populated samples, median delta was 1,417 ns and p95 delta was
50,416 ns; 108 of 200 pairs were slower with maintenance enabled. The hook was
retained because its median delta was small relative to the paired spread.
Every maintenance invocation is capped at 32 stale-relation inspections.

## Changed files

TotalCross repo, 18 tracked files:

- `.agent/evidence/image-persistent-ownership.jsonl`
- `.agent/plans/image-persistent-ownership-execplan.md`
- `.agent/reports/image-persistent-ownership-editorial.md`
- `.agent/state/image-persistent-ownership.md`
- `.github/workflows/build.yml`
- `.github/workflows/merge.yml`
- `TotalCrossSDK/gradle/artifact-boundaries.gradle`
- `TotalCrossSDK/src/main/java/totalcross/ui/Container.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/ImageControl.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/Window.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/image/Image.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/image/ImageDrawingBridge.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/image/ImageOwnershipMaintenanceBridge.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/image/ImagePipeline.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/image/PersistentImageOwnershipRegistry.java`
- `TotalCrossSDK/src/test/java/tc/tools/ArtifactBoundariesTest.java`
- `TotalCrossSDK/src/test/java/totalcross/ui/image/ImageOwnershipMaintenanceBenchmarkTest.java`
- `TotalCrossSDK/src/test/java/totalcross/ui/image/ImagePersistentOwnershipScalingTest.java`

Performance-lab repo, 4 tracked files:

- `tests/image-ownership/README.md`
- `tests/image-ownership/src/totalcross/ui/image/ImageOwnershipLifecycleProbe.java`
- `tests/test_image_ownership_smoke_contract.py`
- `tools/image_ownership_smoke.py`

No generated artifacts are committed. `TotalCrossSDK/IOSDateFixture.tcz`,
created by artifact-content validation, remains untracked and preserved.

## Validation

Passed on the Darwin arm64 host:

- Baseline reproducer intentionally failed all 3 tests on the original code,
  recording the baseline operation counts and 1,510 retained descendant
  owners after ancestor removal.
- Focused subtree lifecycle JavaSE run: 10 tests passed.
- Indexed registry JavaSE run: 16 tests passed; deployed Java API validation
  passed.
- Wider focused JavaSE acceptance run: 48 passed, 0 failed, 2 skipped.
- `./gradlew-agent artifactContentTest --warning-mode=none --console=plain`:
  passed; all 15 artifact-boundary tests passed, with deployed API validation
  included in the task graph.
- `python3 -B -m unittest discover -s tests -p 'test_image_ownership_smoke_contract.py' -v`
  in the performance-lab worktree: 5 tests passed.
- `javac --release 8` compilation of the smoke app against the SDK JAR passed.
- Python source syntax compilation, workflow YAML parsing, changed-file
  copyright validation, commit-message validation, and `git diff --check`
  passed.
- The extracted build-scope selector produced `image-ownership` and
  `image_ownership_changed=true` for the final branch diff. On this Mac the
  system Bash is 3.2 and lacks `mapfile`; the selector was exercised with a
  small Bash 3 compatibility shim.

No Android, Windows, Linux, iOS, or other nonlocal target build or runtime was
run locally. The optional native macOS runtime smoke was not run. A separate
JavaSE launcher experiment exceeded its 60-second limit and is not counted as
a passing check. Detailed test logs and benchmark artifacts are recorded in
`.agent/evidence/image-persistent-ownership.jsonl`.

## Nonlocal CI status and remaining limitations

`.github/workflows/build.yml` contains focused functional smoke jobs for
`linux-amd64`, Windows, and Android. The ownership-related change detector
selects a focused SDK/Linux/Windows/Android build scope. The smoke jobs reuse
same-run SDK and target artifacts and check the source commit/tree, archive
hashes, and pinned performance-lab revision before archiving assertions,
provenance, and runtime logs. `.github/workflows/merge.yml` exposes a manual
`image_ownership_smoke` input that selects the same scope.

The Android job uses an API 35 x86_64 emulator for the ARM64-only runtime
artifact. API 35 ARM ABI translation support is documented in the emulator
runner's [working example](https://github.com/ReactiveCircus/android-emulator-runner/issues/458).

Android, Windows, and Linux functional CI runs are **PENDING** because they
require publication of the authorized branches. No push, PR, merge, or other
remote write was performed. Read-only remote fetch/inspection was used to
confirm the base revisions. The optional native macOS smoke remains unrun.
