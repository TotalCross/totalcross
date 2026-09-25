<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# PNG prefetch Part 1 state

- Active plan: `.agent/plans/png-prefetch-part-2.md`.
- Branch: `feat/png-prefetch`.
- Immutable base: `86d470c64b7dfc0ab6ac24314f32f0d630ff1990`.
- Active milestone: 4A — fresh macOS six-process benchmark.
- Last functional commit: `689e27daf126` (`test(image): cover deployed png prefetch`; signed).
- Previous plan checkpoint: `1dee6189369f` (Milestone 2; signed).
- Active paths: fresh macOS benchmark package/run scripts, the existing
  663-image corpus, and Part 2 evidence/report files.
- Next action: run the Part 2 fresh macOS six-process benchmark after this
  Part 1 checkpoint is committed.
- Base validation: activation checkout equaled the immutable base; its object
  exists and is an ancestor of `feat/semaphore-v1`; `feat/png-prefetch` was
  created at that base.
- Completed Milestone 1: signed `feat(image): add static png prefetch`; 26
  focused SDK tests passed. Logs are indexed in `.agent/evidence/png-prefetch.md`.
- Completed Milestone 2: signed `feat(benchmark): support png prefetch
  diagnostics`; Python compilation, focused benchmark tests, Bash syntax,
  headers, and staged whitespace checks passed. Benchmark test log:
  `/tmp/png-prefetch-m2-benchmark-tests.log`.
- Completed Milestone 3: signed `test(image): cover deployed png prefetch`
  (`689e27daf126`); SDK distribution, Release `tcvm`/`Launcher` build, and
  ordinary/indexed deployed macOS PNG smokes passed. Details and artifact
  identity are indexed in `.agent/evidence/png-prefetch.md`.
- Deferred validation: PowerShell execution was unavailable because this
  macOS host has no PowerShell runtime; package tests validate its matrix,
  required calls, forbidden dependencies, and size. Windows/Linux/Android/iOS
  builds remain outside Part 1 scope.
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
- Resume: read this file, then Milestone 4A in the Part 2 plan. Do not rerun the
  completed Part 1 build/smoke gates unless implementation changes.
