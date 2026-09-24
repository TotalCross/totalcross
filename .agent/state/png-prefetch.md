<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# PNG prefetch Part 1 state

- Active plan: `.agent/plans/png-prefetch-part-1.md`.
- Branch: `feat/png-prefetch`.
- Immutable base: `86d470c64b7dfc0ab6ac24314f32f0d630ff1990`.
- Active milestone: activation and initial plan commit.
- Last functional commit: immutable base; no Part 1 implementation yet.
- Active paths: both PNG prefetch plans, this state file, and
  `.agent/evidence/png-prefetch.md`.
- Next action: review, validate, and sign the initial plan/state/evidence
  commit; then inspect the active SDK paths for Milestone 1.
- Completed validation: current `HEAD` equals the immutable base; its object
  exists and is an ancestor of `feat/semaphore-v1`; `feat/png-prefetch` was
  created at that base.
- Deferred validation: SDK tests, SDK distribution, and macOS native smoke are
  deferred until their plan milestones.
- Active decisions: static PNG only, denominator 1, Java-result candidate;
  JPEG semantics and production `legacy` strategy stay unchanged; no
  `TotalCrossVM` changes.
- Blockers: none. The planned prior state file was absent, so resumption began
  from the plan's activation section.
- Preserve unrelated local paths, including existing `.agent/artifacts/`,
  `.agent/benchmarks/`, `TotalCrossVM/xcode/generated/`, and
  `scripts/__pycache__/`.
- Resume: read this file, then the active milestone in the Part 1 plan; inspect
  scoped status and continue at the next action above.
