<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Standard streams V1 state

- Active milestone/slice: Milestone 7 — closed check-error correction complete.
- Branch: `feat/standard-streams-v1`.
- Base SHA: `5917a4aa3e20123a1ff02c1a5b0cedd9640c0c6b`.
- Last implementation commit: `566aed163` (`fix(sdk): preserve closed print
  stream error state`); its focused test update is in the same commit, and
  preceding closed-flush commits are `b662c3939` and `cd5d5a0d8`.
- Earlier correction documentation closure is `37d4c0ec8` (`docs(agent):
  finalize closed flush handoff`); the closed-check-error documentation
  closure is pending.
- Active paths: corrected Java stream semantics, shared legacy writer/backend
  semantics, artifact boundaries, bounded plan/reference artifacts, smoke
  fixture/runner, and final handoff files.
- Next concrete action: update the bounded correction records, commit the
  documentation closure, and leave task-scoped paths clean.
- Focused validation completed: corrected focused SDK tests passed with agent
  log `TotalCrossSDK/agent-logs/20260917-203241-test-agent.log`; the dedicated
  artifact-boundary task passed with agent log
  `TotalCrossSDK/agent-logs/20260917-203435-artifactContentTest-agent.log`;
  clean SDK `dist` passed with agent log
  `TotalCrossSDK/agent-logs/20260917-203302-clean-agent.log`; the permitted
  macOS native configure/build passed using
  `/tmp/tc-standard-streams-v1-final-corrected` with logs
  `/tmp/tc-standard-streams-v1-corrected-macos-cmake.log` and
  `/tmp/tc-standard-streams-v1-corrected-macos-build.log`; corrected native
  smoke passed with agent log
  `TotalCrossSDK/agent-logs/20260917-203407-runStandardStreamsSmokeMacOS-agent.log`.
  Header, diff, static, bounded-size, and commit-message audits passed at
  closure; the two historical M0/M1 message deviations remain documented.
- Closed-flush focused rerun passed with agent log
  `TotalCrossSDK/agent-logs/20260917-210927-test-agent.log`; no native rebuild
  was needed because the correction changed only SDK Java/test files.
- Closed-check-error focused rerun passed with agent log
  `TotalCrossSDK/agent-logs/20260917-213211-test-agent.log`; the earlier
  post-close assertion failure is recorded in evidence and corrected without
  native changes.
- Final functional/documentation HEAD before this state-only reconciliation is
  `37d4c0ec8`; no native or packaging validation is required for this SDK-only
  change.
- Deferred validation: Windows, Linux, Android, and iOS builds remain forbidden
  by the active plan. Full image benchmarks remain out of scope.
- Blockers/discoveries: the Milestone 0 and Milestone 1 commit-message checks
  reported body-format deviations; those commits remain unamended. Running the
  native-method generator exposed pre-existing unrelated generated-file drift,
  so only the three focused `VmStandardOutputStream` symbols were retained.
  The first M3 smoke attempt exposed an unavailable deployed
  `java.nio.charset.StandardCharsets`; the supported UTF-8 path fixed it and
  the rerun passed. The review correction restored desktop macOS legacy
  stdout-only behavior, added Windows durable explicit flush, consolidated
  the oversized plan into bounded artifacts, and corrected closed-flush
  trouble reporting. Existing unrelated untracked
  files and benchmark artifacts remain untouched.
- The supplied plan was already 39,677 bytes and 1,287 lines, above the
  plan's soft size guideline; it was consolidated rather than discarded. The
  current active plan is 12,422 bytes and 271 lines, and its technical
  reference is 6,569 bytes and 146 lines.
- Active decisions: standard-stream close is logical and never closes the
  shared legacy file; auto-flush is logical while explicit `flush()` is
  durable; `Vm.debug` remains a separate debug command path.
- Deliberate out-of-scope local changes: `IOSDateFixture.tcz`,
  `ImageScrollRealWorkloadBenchmarkApp.log`, `TotalCrossSDK/etc/launchers/`,
  `scripts/__pycache__/`, unrelated `.agent/benchmarks/` artifacts, and the
  `image-optimization-mask-*.md` plans.
- Resume command: `git switch feat/standard-streams-v1 && sed -n '1,220p'
  .agent/state/standard-streams-v1.md`.
