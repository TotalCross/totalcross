<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Standard streams V1 state

- Active milestone/slice: Milestone 0 — branch and resumable artifacts.
- Branch: `feat/standard-streams-v1`.
- Base SHA: `5917a4aa3e20123a1ff02c1a5b0cedd9640c0c6b`.
- Last logical commit: none for this plan; `HEAD` currently equals the base.
- Active paths: `.agent/plans/standard-streams-v1.md`, this state file, and
  `.agent/evidence/standard-streams-v1.md`.
- Next concrete action: validate and commit the planning artifacts, then inspect
  `PrintStream4D` and its existing SDK test conventions for Milestone 1.
- Focused validation completed: remote ref refreshed; branch ancestry confirmed.
- Deferred validation: no SDK or native build in Milestone 0; Windows, Linux,
  Android, and iOS builds remain forbidden by the active plan.
- Blockers/discoveries: the plan references repository-local logical-commits and
  validate-headers skills; both files exist and were read. Existing unrelated
  untracked files are deliberately out of scope and untouched.
- Deliberate out-of-scope local changes: `IOSDateFixture.tcz`,
  `ImageScrollRealWorkloadBenchmarkApp.log`, `TotalCrossSDK/etc/launchers/`,
  `scripts/__pycache__/`.
- Resume command: `git switch feat/standard-streams-v1 && sed -n '1,220p' .agent/state/standard-streams-v1.md`.
