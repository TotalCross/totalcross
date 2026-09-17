<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Standard streams V1 state

- Active milestone/slice: Milestone 4 — completed handoff.
- Branch: `feat/standard-streams-v1`.
- Base SHA: `5917a4aa3e20123a1ff02c1a5b0cedd9640c0c6b`.
- Last implementation commit: `a6edf33dc` (`test(runtime): cover native
  standard stream routing`); preceding fix is `67bcc1076`, shared-writer
  refactor is `a9e973480`, and the M2 router commit is `8e644678a`.
- Final handoff commit: `535e3f14a` (`docs(agent): finalize standard streams
  v1 handoff`).
- Active paths: the completed standard-stream Java/native bridge, five platform
  sink headers, shared `DebugConsole.txt` writer, smoke fixture/runner, and
  `.agent/{plan,state,evidence,archive,reports}` handoff artifacts.
- Next concrete action: no further V1 implementation work; retain the
  recorded platform and benchmark deferrals for future CI or V2 planning.
- Focused validation completed: final focused SDK tests passed at
  `/tmp/tc-standard-streams-v1-final-test.log`; clean SDK `dist` passed with
  agent log `TotalCrossSDK/agent-logs/20260917-200150-clean-agent.log`; the
  permitted macOS native configure/build passed with logs
  `/tmp/tc-standard-streams-v1-final-macos-cmake.log` and
  `/tmp/tc-standard-streams-v1-final-native-build.log`; native smoke passed
  with agent log
  `TotalCrossSDK/agent-logs/20260917-200254-runStandardStreamsSmokeMacOS-agent.log`.
  The smoke summary captured separate stdout/stderr and a shared post-erase
  legacy file. Header, diff, static-scope, and file-size checks pass.
- Deferred validation: Windows, Linux, Android, and iOS builds remain forbidden
  by the active plan. Full image benchmarks remain out of scope.
- Blockers/discoveries: the Milestone 0 and Milestone 1 commit-message checks
  reported body-format deviations; those commits remain unamended. Running the
  native-method generator exposed pre-existing unrelated generated-file drift,
  so only the three focused `VmStandardOutputStream` symbols were retained.
  The first M3 smoke attempt exposed an unavailable deployed
  `java.nio.charset.StandardCharsets`; the supported UTF-8 path fixed it and
  the rerun passed. Existing unrelated untracked files and benchmark artifacts
  remain untouched.
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
