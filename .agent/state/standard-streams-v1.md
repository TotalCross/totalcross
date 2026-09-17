<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Standard streams V1 state

- Active milestone/slice: Milestone 3 — share the legacy DebugConsole writer.
- Branch: `feat/standard-streams-v1`.
- Base SHA: `5917a4aa3e20123a1ff02c1a5b0cedd9640c0c6b`.
- Last logical commit: `8e644678a` (`feat(runtime): add standard stream router`);
  prior SDK commit is `b8eefe31c` and the plan commit is `ed56aff81`.
- Active paths: `TotalCrossVM/src/util/debug.c`, platform debug backends,
  `TotalCrossVM/src/util/legacy_debug_console.{h,c}`, standard-stream router
  integration, native smoke fixture/runner, and this state/evidence files.
- Next concrete action: inventory each platform debug backend's legacy writer
  behavior, extract the shared DebugConsole writer, then add the M3 smoke test.
- Focused validation completed: Milestone 1 focused SDK test passed at
  `/tmp/tc-standard-streams-v1-m1-test.log`; Milestone 2 focused SDK tests
  passed at `/tmp/tc-standard-streams-v1-m2-test.log`; the macOS native
  configure/build passed using `/tmp/tc-standard-streams-v1-m2-macos-20260917`
  with logs `/tmp/tc-standard-streams-v1-m2-cmake.log` and
  `/tmp/tc-standard-streams-v1-m2-native-build.log`; headers and diff checks
  pass.
- Deferred validation: Windows, Linux, Android, and iOS builds remain forbidden
  by the active plan. Full image benchmarks remain out of scope.
- Blockers/discoveries: the Milestone 0 and Milestone 1 commit-message checks
  reported body-format deviations; those commits remain unamended. Running the
  native-method generator exposed pre-existing unrelated generated-file drift,
  so only the three focused VmStandardOutputStream symbols were retained.
  Existing unrelated untracked files and benchmark artifacts remain untouched.
- Deliberate out-of-scope local changes: `IOSDateFixture.tcz`,
  `ImageScrollRealWorkloadBenchmarkApp.log`, `TotalCrossSDK/etc/launchers/`,
  `scripts/__pycache__/`, unrelated `.agent/benchmarks/` artifacts, and the
  `image-optimization-mask-*.md` plans.
- Resume command: `git switch feat/standard-streams-v1 && sed -n '1,220p' .agent/state/standard-streams-v1.md`.
