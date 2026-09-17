<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Standard streams V1 state

- Active milestone/slice: Milestone 2 — add the Java/native bridge and router.
- Branch: `feat/standard-streams-v1`.
- Base SHA: `5917a4aa3e20123a1ff02c1a5b0cedd9640c0c6b`.
- Last logical commit: pending Milestone 1 SDK core commit; prior plan commit is
  `ed56aff81` (`docs(agent): add standard streams v1 plan`).
- Active paths: `TotalCrossSDK/src/main/java/totalcross/sys/VmStandardOutputStream.java`,
  `TotalCrossSDK/src/main/java/jdkcompat/lang/System4D.java`, native method
  metadata/registration, `TotalCrossVM/src/`, and this state/evidence files.
- Next concrete action: inspect native registration generation and existing
  platform debug backends, then add the standard-stream bridge/router family.
- Focused validation completed: Milestone 1 focused SDK test passed at
  `/tmp/tc-standard-streams-v1-m1-test.log`; headers and scoped diff checks pass.
- Deferred validation: Milestone 2’s single permitted macOS native build remains
  deferred until the bridge/router family is complete; Windows, Linux, Android,
  and iOS builds remain forbidden by the active plan.
- Blockers/discoveries: `PrintStream4D` was previously unused by `System4D`,
  which still imports JDK `PrintStream`; integration is intentionally deferred
  to the planned bridge milestone. The Milestone 0 commit-message checker
  reported one overlong body line; that commit remains unamended. Existing
  unrelated untracked files are deliberately out of scope and untouched.
- Deliberate out-of-scope local changes: `IOSDateFixture.tcz`,
  `ImageScrollRealWorkloadBenchmarkApp.log`, `TotalCrossSDK/etc/launchers/`,
  `scripts/__pycache__/`.
- Resume command: `git switch feat/standard-streams-v1 && sed -n '1,220p' .agent/state/standard-streams-v1.md`.
