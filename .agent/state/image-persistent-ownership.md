<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Active state: persistent image ownership

- Active milestone: complete; local acceptance and nonlocal CI preparation are
  recorded.
- Branch/worktree: `fix/image-persistent-ownership-scaling` at
  `/Users/flsobral/.codex/worktrees/fix-image-persistent-ownership-scaling`.
- Base, initial HEAD, and merge-base:
  `6b915883dd202f6db891990f6c3af26fb645ecc7` (`origin/master`).
- Last commit: `docs(image): record ownership acceptance status`.
- Active paths: `TotalCrossSDK/src/main/java/totalcross/ui/image/`,
  `TotalCrossSDK/src/main/java/totalcross/ui/{Container,ScrollContainer,ImageControl,Window}.java`,
  focused tests in `TotalCrossSDK/src/test/java/`, and the ExecPlan records.
- Next action: none. Final handoff is recorded in
  `.agent/reports/image-persistent-ownership-editorial.md`.
- Focused validation completed: baseline command
  `./gradlew-agent test --tests totalcross.ui.image.ImagePersistentOwnershipScalingTest`
  compiled and ran all three tests; all failed as intended with deterministic
  counts recorded in `.agent/evidence/image-persistent-ownership.jsonl`.
  Commit message and changed-file copyright validators passed before and after
  commit; `git diff --cached --check` passed.
- Nonlocal CI preparation: the reusable workflow now scopes the ownership
  slice, consumes same-run SDK/target artifacts, and runs Linux, Windows, and
  Android functional smokes from performance-lab commit
  `bae23107b57390525a90669086e9fb47efaaa440`. The scope selector produced
  `image-ownership` and `image_ownership_changed=true` for this branch.
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
- Workflow YAML parsing, focused copyright validation, staged diff check,
  commit-message validation, and post-commit checks passed for the CI slice.
- Android uses an API 35 x86_64 emulator with ARM ABI translation for the
  arm64-v8a-only APK. The target jobs remain PENDING until the branches are
  published; no publication was performed.
- The optional native macOS runtime smoke was not run. JavaSE simulator tests,
  deployed API validation, and artifact-content validation passed. A standalone
  JavaSE launcher attempt exceeded its 60-second limit and is not counted as a
  passing check.
- Implementation and CI commit: `ed026ea40c6e256335f288b7e5e39c75da857638`;
  merge-base remains `6b915883dd202f6db891990f6c3af26fb645ecc7`.
- Companion smoke app branch: `fix/image-persistent-ownership-smoke`, base
  `fe23e009fa2fcdbf8c36746cea58b9946a138858`, HEAD
  `bae23107b57390525a90669086e9fb47efaaa440`.
- Blockers: none.
- Preserve: 144 pre-existing untracked items in the original worktree; no files
  from that worktree are in scope.
- Resume: read this file, then the active milestone and exact test paths in
  `.agent/plans/image-persistent-ownership-execplan.md`.
