<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Active state: persistent image ownership

- Active milestone: indexed weak-identity ownership registry.
- Branch/worktree: `fix/image-persistent-ownership-scaling` at
  `/Users/flsobral/.codex/worktrees/fix-image-persistent-ownership-scaling`.
- Base, initial HEAD, and merge-base:
  `6b915883dd202f6db891990f6c3af26fb645ecc7` (`origin/master`).
- Last commit: `4cbd85fb6` `fix(image): suspend owners in detached subtrees`.
- Active paths: `TotalCrossSDK/src/main/java/totalcross/ui/image/`,
  `TotalCrossSDK/src/main/java/totalcross/ui/{Container,ScrollContainer,ImageControl,Window}.java`,
  focused tests in `TotalCrossSDK/src/test/java/`, and the ExecPlan records.
- Next action: implement owner/image identity-hash indexes with weak referents,
  per-image aggregate counts, constant-time indexed unlink, and bulk pipeline
  count transfer; retain test hooks for collisions, lifetime, and counts.
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
- Active decisions: retain detached-construction ownership; explicit subtree
  suspension is separate from ancestor/window presence; no Image layout or
  `ImageDrawingFeatureBridge` public surface changes.
- Blockers: none.
- Preserve: 144 pre-existing untracked items in the original worktree; no files
  from that worktree are in scope.
- Resume: read this file, then the active milestone and exact test paths in
  `.agent/plans/image-persistent-ownership-execplan.md`.
