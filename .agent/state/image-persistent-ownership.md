<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Active state: persistent image ownership

- Active milestone: final acceptance and nonlocal CI preparation.
- Branch/worktree: `fix/image-persistent-ownership-scaling` at
  `/Users/flsobral/.codex/worktrees/fix-image-persistent-ownership-scaling`.
- Base, initial HEAD, and merge-base:
  `6b915883dd202f6db891990f6c3af26fb645ecc7` (`origin/master`).
- Last commit: `207292bc4` `perf(image): index persistent drawing ownership`.
- Active paths: `TotalCrossSDK/src/main/java/totalcross/ui/image/`,
  `TotalCrossSDK/src/main/java/totalcross/ui/{Container,ScrollContainer,ImageControl,Window}.java`,
  focused tests in `TotalCrossSDK/src/test/java/`, and the ExecPlan records.
- Next action: validate headers and diff, commit bounded maintenance, then
  inspect canonical CI and performance-lab workflows and prepare focused
  functional smoke coverage for nonlocal targets.
- Focused validation completed: baseline command
  `./gradlew-agent test --tests totalcross.ui.image.ImagePersistentOwnershipScalingTest`
  compiled and ran all three tests; all failed as intended with deterministic
  counts recorded in `.agent/evidence/image-persistent-ownership.jsonl`.
  Commit message and changed-file copyright validators passed before and after
  commit; `git diff --cached --check` passed.
- Deferred: production implementation checks pending; nonlocal CI tests require
  a remote runner and cannot be triggered without publication.
- Lifecycle validation passed with 10 JavaSE tests: all 6
  `ImagePersistentRasterAdmissionTest` tests and 4 selected lifecycle regressions
  passed. The detailed run is recorded in the evidence index.
- Lifecycle commit message and copyright validators passed after commit.
- Indexed registry validation passed: 16 focused JavaSE tests and deployed API
  validation; measured 14,855 / 47,596 / 97,070 registration inspections for
  500 / 1,510 / 3,020 pairs. Commit message and copyright validators passed.
- Bounded maintenance validation passed: 48 focused JavaSE tests (2 skipped),
  including 200 repaint samples per configuration, plus the artifact-content
  task (15 artifact-boundary tests). The task graph includes deployed API
  validation. Populated repaint p50 was 140,458 ns disabled and 141,792 ns
  enabled per 64 entries; paired median delta was 1,417 ns against a much wider
  timing spread. Empty registries produced zero inspections; populated enabled
  maintenance inspected 6,296 relationships total, within 32 per invocation.
  The bounded repaint hook is retained. Logs and raw samples are listed in the
  evidence index; generated reports remain under ignored build output.
- The two Image drawing paths no longer run registry maintenance. Mutation
  operations retain bounded sweeps; Window calls the narrow bridge every 64
  outermost repaint entries, skipping nested entries and empty registries.
- Artifact-boundary tests confirm the new bridge is included in runtime and
  aggregate artifacts but excluded from `totalcross-api`.
- Active decisions: retain detached-construction ownership; explicit subtree
  suspension is separate from ancestor/window presence; no Image layout or
  `ImageDrawingFeatureBridge` public surface changes.
- Deferred: header/diff validation and stage-four commit; GitHub Actions
  functional smokes for Android, Windows, and Linux remain pending until a
  publication trigger is available. No remote publication is authorized.
- Blockers: none.
- Preserve: 144 pre-existing untracked items in the original worktree; no files
  from that worktree are in scope.
- Resume: read this file, then the active milestone and exact test paths in
  `.agent/plans/image-persistent-ownership-execplan.md`.
