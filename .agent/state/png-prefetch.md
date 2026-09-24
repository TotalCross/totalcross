<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# PNG prefetch Part 1 state

- Active plan: `.agent/plans/png-prefetch-part-1.md`.
- Branch: `feat/png-prefetch`.
- Immutable base: `86d470c64b7dfc0ab6ac24314f32f0d630ff1990`.
- Active milestone: 3 — SDK distribution and deployed macOS PNG smoke.
- Last functional commit: `e391aaf11dff` (`feat(benchmark): support png prefetch diagnostics`; signed).
- Last plan commit: `0ef3606c4b57` (Milestone 1 checkpoint; signed).
- Active paths: SDK build outputs/logs and the existing macOS native runtime and
  ImagePreparation smoke flow; update this plan/state/evidence trio at the
  Milestone 3 checkpoint.
- Next action: run `cd TotalCrossSDK && ./gradlew-agent dist -x test` once,
  saving verbose output under a task-specific log.
- Base validation: activation checkout equaled the immutable base; its object
  exists and is an ancestor of `feat/semaphore-v1`; `feat/png-prefetch` was
  created at that base.
- Completed Milestone 1: signed `feat(image): add static png prefetch`; 26
  focused SDK tests passed. Logs are indexed in `.agent/evidence/png-prefetch.md`.
- Completed Milestone 2: signed `feat(benchmark): support png prefetch
  diagnostics`; Python compilation, focused benchmark tests, Bash syntax,
  headers, and staged whitespace checks passed. Benchmark test log:
  `/tmp/png-prefetch-m2-benchmark-tests.log`.
- Deferred validation: SDK distribution and deployed macOS native smoke are
  active in Milestone 3. PowerShell execution was unavailable because this
  macOS host has no PowerShell runtime; package tests validate its matrix,
  required calls, forbidden dependencies, and size.
- Commit-message validation found the initial plan commit body exceeded 80
  characters because the shell preserved literal `\n` text. The plan forbids
  rewriting history, so preserve that signed commit and validate all later
  messages before committing.
- Active decisions: static PNG only, denominator 1, Java-result candidate;
  JPEG semantics and production `legacy` strategy stay unchanged; benchmark
  package counts image content by signature; no `TotalCrossVM` changes.
- Blockers: none. The planned prior state file was absent, so resumption began
  from the plan's activation section.
- Preserve unrelated local paths, including existing `.agent/artifacts/`,
  `.agent/benchmarks/`, `TotalCrossVM/xcode/generated/`, and
  `scripts/__pycache__/`; leave the existing `totalcross.code-workspace` change
  untouched.
- Resume: read this file, then Milestone 3 in the Part 1 plan; inspect the SDK
  build and existing macOS smoke procedure before proceeding past the next
  action above.
