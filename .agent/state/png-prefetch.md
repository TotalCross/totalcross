<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# PNG prefetch Part 2 state

- Active plan: `.agent/plans/png-prefetch-part-2.md`.
- Branch: `feat/png-prefetch`.
- Immutable base: `86d470c64b7dfc0ab6ac24314f32f0d630ff1990`.
- Active milestone: Finalization — editorial report and final repository checks.
- Last functional commit: `f8308ac31` (`fix(bench): guard windows manifest metadata order`; signed).
- Previous plan checkpoint: `1dee6189369f` (Milestone 2; signed).
- Last logical commit: `28aaf3357e2adfebf794f1467e5bf17c4654b468`
  (`docs(benchmark): record png prefetch results`; signed).
- Active paths: Part 2 plan, state, evidence, editorial report, and the
  untracked Windows ZIP under `/tmp/png-prefetch-part2.Kd03lw/`.
- Next action: create `.agent/reports/png-prefetch-editorial.md`, reconcile the
  plan outcomes/state, run final size/header/diff/signature checks, and commit
  the closeout. Do not run the Windows package on macOS.
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
- Completed Part 2 Milestones 4A/4B: packaged only macOS-arm64 from the current
  SDK JAR and fresh runtime, ran the exact six-process matrix once, and passed
  all readiness, lifecycle, thread, and semaphore checks. Every row reported
  663/663/0/0 for dataset `af39fea695191a27`; the former cold-scroll decode and
  materialization counters fell from 3 each to 0. Frame outliers changed in
  both directions, with one run per configuration. Evidence is in
  `.agent/evidence/png-prefetch.md` and `.agent/evidence/png-prefetch-macos.csv`;
  raw output and log are under `/tmp/png-prefetch-part2.Kd03lw/`. The signed
  evidence checkpoint is `28aaf3357e2adfebf794f1467e5bf17c4654b468`.
- Completed Part 2 Milestone 4C: created a current-source Windows x64 package
  with SDK source attestation `ca7d77d88880ac5c6c666bd2b67721c2bead7063`,
  verified the ZIP and six-process manifest, and statically checked the
  PowerShell no-external-tool runner. ZIP SHA-256 is
  `fb04343d5c6ea17bfa520ed80771cdd4e52e1871e515cdf26003d0745713c6dd`; it is
  intentionally untracked under `/tmp/png-prefetch-part2.Kd03lw/`. Windows
  execution and measurements remain unperformed by plan.
- Pre-Part-2 branch-validation correction: initialize `manifestHash` before
  metadata use, defer `RUNNING` until manifest and image payload validation,
  and preserve `FAILED` metadata/ZIP generation on validation errors. The
  README now documents SDK-runtime source ancestry, and focused static checks
  guard the ordering. Validation is indexed in `.agent/evidence/png-prefetch.md`.
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
- Resume: read this file, then Finalization in the Part 2 plan. Do not rerun
  the completed Part 1 build/smoke gates or M4A matrix.
