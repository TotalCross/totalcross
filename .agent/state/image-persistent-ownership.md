<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Active state: persistent image ownership

- Active milestone: commit baseline reproducer tests.
- Branch/worktree: `fix/image-persistent-ownership-scaling` at
  `/Users/flsobral/.codex/worktrees/fix-image-persistent-ownership-scaling`.
- Base, initial HEAD, and merge-base:
  `6b915883dd202f6db891990f6c3af26fb645ecc7` (`origin/master`).
- Last commit: base only; baseline red tests are ready to stage and commit.
- Active paths: `TotalCrossSDK/src/main/java/totalcross/ui/image/`,
  `TotalCrossSDK/src/main/java/totalcross/ui/{Container,ScrollContainer,ImageControl,Window}.java`,
  focused tests in `TotalCrossSDK/src/test/java/`, and the ExecPlan records.
- Next action: run focused copyright validation, inspect the scoped diff, stage
  only the reproducer, counters, plan, state, and evidence, check the staged
  diff and commit message, then create the baseline test commit.
- Focused validation completed: baseline command
  `./gradlew-agent test --tests totalcross.ui.image.ImagePersistentOwnershipScalingTest`
  compiled and ran all three tests; all failed as intended with deterministic
  counts recorded in `.agent/evidence/image-persistent-ownership.jsonl`.
- Deferred: production implementation checks pending; nonlocal CI tests require
  a remote runner and cannot be triggered without publication.
- Active decisions: retain detached-construction ownership; explicit subtree
  suspension is separate from ancestor/window presence; no Image layout or
  `ImageDrawingFeatureBridge` public surface changes.
- Blockers: none.
- Preserve: 144 pre-existing untracked items in the original worktree; no files
  from that worktree are in scope.
- Resume: read this file, then the active milestone and exact test paths in
  `.agent/plans/image-persistent-ownership-execplan.md`.
