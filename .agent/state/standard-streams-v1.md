<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Standard streams V1 state

- Active milestone/slice: Milestone 5 — corrected final handoff reconciliation.
- Branch: `feat/standard-streams-v1`.
- Base SHA: `5917a4aa3e20123a1ff02c1a5b0cedd9640c0c6b`.
- Last implementation commit: `a0011d954` (`test(sdk): cover corrected stream
  semantics`); preceding artifact fix is `e36203795`, runtime fix is
  `cfeb4f51c`, and plan consolidation is `4d0ded3db`.
- Earlier handoff commits remain `535e3f14a`, `39dcb0b41`, and `c3cba7e9b`;
  corrected final documentation is pending.
- Active paths: corrected Java stream semantics, shared legacy writer/backend
  semantics, artifact boundaries, bounded plan/reference artifacts, smoke
  fixture/runner, and final handoff files.
- Next concrete action: commit the corrected plan/evidence/archive/report;
  then record the actual final HEAD and leave task-scoped paths clean.
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
  Headers, diff, static, and bounded-size checks remain required at closure.
- Deferred validation: Windows, Linux, Android, and iOS builds remain forbidden
  by the active plan. Full image benchmarks remain out of scope.
- Blockers/discoveries: the Milestone 0 and Milestone 1 commit-message checks
  reported body-format deviations; those commits remain unamended. Running the
  native-method generator exposed pre-existing unrelated generated-file drift,
  so only the three focused `VmStandardOutputStream` symbols were retained.
  The first M3 smoke attempt exposed an unavailable deployed
  `java.nio.charset.StandardCharsets`; the supported UTF-8 path fixed it and
  the rerun passed. The review correction restored desktop macOS legacy
  stdout-only behavior, added Windows durable explicit flush, and consolidated
  the oversized plan into bounded artifacts. Existing unrelated untracked
  files and benchmark artifacts remain untouched.
- The supplied plan was already 39,677 bytes and 1,287 lines, above the
  plan's soft size guideline; it was preserved as user-provided input. The
  current reconciled file is 40,760 bytes and 1,303 lines.
- Active decisions: standard-stream close is logical and never closes the
  shared legacy file; auto-flush is logical while explicit `flush()` is
  durable; `Vm.debug` remains a separate debug command path.
- Deliberate out-of-scope local changes: `IOSDateFixture.tcz`,
  `ImageScrollRealWorkloadBenchmarkApp.log`, `TotalCrossSDK/etc/launchers/`,
  `scripts/__pycache__/`, unrelated `.agent/benchmarks/` artifacts, and the
  `image-optimization-mask-*.md` plans.
- Resume command: `git switch feat/standard-streams-v1 && sed -n '1,220p'
  .agent/state/standard-streams-v1.md`.
