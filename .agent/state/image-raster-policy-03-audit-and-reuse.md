<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# State — raster policy audit and reuse

## Active slice

- Bootstrap is complete; Milestone 1 is next.
- Required branch: `perf/image-decode-distributed-benchmark`.
- Starting HEAD: `57d0532409c2163ac390d2f0affd8a0787049b07`.
- Plan 2 is complete at the prior implementation/documentation revisions
  recorded in `.agent/state/image-write-pixels-policy-02-device-copy.md`.

## Working set

- Plan: `.agent/plans/image-raster-policy-03-audit-and-reuse.md`
- State: this file; rewrite on resume.
- Evidence: `.agent/evidence/image-raster-policy-03-audit-and-reuse.md`
- Editorial report:
  `.agent/reports/image-raster-policy-03-audit-and-reuse-editorial.md`
- Milestone summaries: `.agent/benchmarks/image-raster-policy-03/`

Bootstrap sources were read once: the Plan 2 state, evidence, and benchmark
summary. Read this state first on resume; read the active plan sections and
only the active source paths next.

## Progress

- [x] (2026-09-18) Verify branch, ancestry, and preserve unrelated local files.
- [x] (2026-09-18) Create bootstrap state, evidence, and editorial handoff.
- [ ] Milestone 1: correctness and benchmark hygiene.
- [ ] STOP / REVIEW 1.
- [ ] Milestones 2–4, each gated by explicit recorded review approval.

## Validation and deferrals

- Bootstrap checks: branch/ancestry, focused header validation, whitespace,
  and plan syntax/size inspection.
- No native build or benchmark has run for this plan.
- Expensive validation is deferred to the Milestone 1 gate.

## Decisions still active

- Do not change default optimization masks.
- Preserve delayed target-color materialization and the single raster-variant
  slot through all four milestones.
- Milestone 2 is diagnostic-only; structural corrections require reviewer
  approval recorded below.

## Approved M3 changes

None. Milestone 3 is not authorized until Milestone 2 review.

## Deliberate local files

Preserve the unrelated benchmark logs/directories, earlier optimization plans,
generated SDK fixtures/launchers, caches, and partial artifacts listed by the
bootstrap status check. Do not stage them.

## Next action

Inspect the Milestone 1 active source paths, implement the strengthened clip
proof first, then the JPEG opacity, accounting, and target-format contracts.

## Resume command

Read this state, then the Milestone 1 sections of the active plan and the
listed primary implementation paths. Before the next commit, validate headers,
stage only explicit task paths, run cached whitespace checks, and validate the
commit message.
