<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# PNG prefetch Part 1 state

- Active plan: `.agent/plans/png-prefetch-part-1.md`.
- Branch: `feat/png-prefetch`.
- Immutable base: `86d470c64b7dfc0ab6ac24314f32f0d630ff1990`.
- Active milestone: 2 — benchmark expectations, manifest content counts, and
  the Python-free Windows PowerShell runner.
- Last functional commit: `efc679d88a8f09999304224bd5642f4122d6d505`.
- Last plan commit: `4549dadd24d1c808908c89f2aa8908a71bf4f441` (signed).
- Active paths: benchmark app, Python runner/tests, package script, benchmark
  README, new Windows PowerShell runner, and this plan/state/evidence trio.
- Next action: inspect hard-coded 663/660/0/3 expectations and package manifest
  construction, then implement the 663/663/0/0 and 660-JPEG/3-PNG contracts.
- Base validation: activation checkout equaled the immutable base; its object
  exists and is an ancestor of `feat/semaphore-v1`; `feat/png-prefetch` was
  created at that base.
- Completed validation: `cd TotalCrossSDK && ./gradlew-agent test --tests
  totalcross.ui.image.ImagePreparationTest` passed all 26 tests; focused
  copyright validation checked 5 files with no changes; scoped diff check
  passed.
- Milestone 1 commit: signed `feat(image): add static png prefetch`; its commit
  message and committed headers validated. Test runs and logs are indexed in
  `.agent/evidence/png-prefetch.md`.
- Deferred validation: SDK distribution and deployed macOS native smoke remain
  deferred to Milestone 3.
- Commit-message validation found the initial plan commit body exceeded 80
  characters because the shell preserved literal `\n` text. The plan forbids
  rewriting history, so preserve that signed commit and validate all later
  messages before committing.
- Active decisions: static PNG only, denominator 1, Java-result candidate;
  JPEG semantics and production `legacy` strategy stay unchanged; no
  `TotalCrossVM` changes.
- Blockers: none. The planned prior state file was absent, so resumption began
  from the plan's activation section.
- Preserve unrelated local paths, including existing `.agent/artifacts/`,
  `.agent/benchmarks/`, `TotalCrossVM/xcode/generated/`, and
  `scripts/__pycache__/`; leave the existing `totalcross.code-workspace` change
  untouched.
- Resume: read this file, then Milestone 2 in the Part 1 plan; inspect scoped
  status and continue at the next action above.
