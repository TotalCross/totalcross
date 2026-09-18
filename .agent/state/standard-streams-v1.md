<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Standard streams V1 state

- Active milestone/slice: Milestone 8 — iOS standard-stream bool ABI correction
  complete.
- Branch: `feat/standard-streams-v1`.
- Base SHA: `5917a4aa3e20123a1ff02c1a5b0cedd9640c0c6b`.
- Last implementation commit: `b7ef55ce5` (`fix(ios): avoid bool ABI conflict
  in standard streams`); it aligns both public signatures and `durable` in the
  header and implementation after CI failure `35296909437`.
- The prior check-error documentation closure is `dfd8141a3`; the final
  reconciliation records implementation commit `b7ef55ce5`.
- Active paths: corrected Java stream semantics, shared legacy writer/backend
  semantics, artifact boundaries, bounded plan/reference artifacts, smoke
  fixture/runner, and final handoff files.
- Next concrete action: none; leave task-scoped paths clean and retain the
  recorded platform/V2 deferrals.
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
  `TotalCrossSDK/agent-logs/20260917-213211-test-agent.log`; the completion
  audit found the wrapped-`PrintStream` query edge case, and the final focused
  rerun passed with `TotalCrossSDK/agent-logs/20260917-220422-test-agent.log`.
- Final implementation HEAD before this artifact reconciliation is
  `b7ef55ce5`; the permitted macOS native regression configure/build passed
  with logs `/tmp/tc-standard-streams-v1-ios-bool-fix-cmake.log` and
  `/tmp/tc-standard-streams-v1-ios-bool-fix-build.log`.
- Deferred validation: Windows, Linux, Android, and iOS local builds remain
  forbidden by the active plan; iOS CI failure `35296909437` is addressed by
  the source-level ABI correction and macOS regression build. Full image
  benchmarks remain out of scope.
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
  files and benchmark artifacts remain untouched. iOS CI failure `35296909437`
  confirmed that Apple-header `bool` changes could split the standard-stream
  declarations from definitions; the public signatures now use commented
  `int32` consistently without changing runtime values.
- The supplied plan was already 39,677 bytes and 1,287 lines, above the
  plan's soft size guideline; it was consolidated rather than discarded. The
  current active plan is 12,991 bytes and 279 lines, and its technical
  reference is 6,874 bytes and 152 lines.
- Active decisions: standard-stream close is logical and never closes the
  shared legacy file; auto-flush is logical while explicit `flush()` is
  durable; `Vm.debug` remains a separate debug command path.
- Deliberate out-of-scope local changes: `IOSDateFixture.tcz`,
  `ImageScrollRealWorkloadBenchmarkApp.log`, `TotalCrossSDK/etc/launchers/`,
  `scripts/__pycache__/`, unrelated `.agent/benchmarks/` artifacts, and the
  `image-optimization-mask-*.md` plans.
- Resume command: `git switch feat/standard-streams-v1 && sed -n '1,220p'
  .agent/state/standard-streams-v1.md`.
