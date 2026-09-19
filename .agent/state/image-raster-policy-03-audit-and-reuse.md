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
- [x] (2026-09-19) Milestone 1: correctness and benchmark hygiene.
- [ ] STOP / REVIEW 1 — awaiting explicit reviewer approval.
- [ ] Milestones 2–4, each gated by explicit recorded review approval.

## Validation and deferrals

- Bootstrap checks: branch/ancestry, focused header validation, whitespace,
  and plan syntax/size inspection.
- Milestone 1 native gate passed: SDK distribution, macOS ARM64 `tcvm`,
  `Launcher`, `skia_surface_test`, bundle packaging, and bundle self-test.
- Six accounting-on smokes passed, followed by the 20-process accounting A/B
  matrix. The required corpus self-test hash was `588a7e0f4019424a`.
- Detailed samples and aggregate deltas are in
  `.agent/benchmarks/image-raster-policy-03/m1-write-pixels-accounting.md`.
- Deferred: all Milestone 2+ work and the historical full profile.

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

Stop for review. Do not begin Milestone 2 until the reviewer explicitly
approves continuation and that approval is recorded below.

## Milestone 1 outcome

- Strong clipped writePixels proof passed in `skia_surface_test`; outside
  sentinel pixels remained unchanged.
- JPEG opacity is published intrinsically on successful native publication;
  diagnostic smoke evidence shows intrinsic opacity with bit 1 disabled and
  no scan in the prefetch-off candidate path.
- Accounting-off retains external work/paint timing and writes unavailable
  diagnostics markers without requiring counter fields.
- Native target metrics are captured once from the active Skia target through
  the established NativeImageBacking bridge. The original Image bridge was
  unsafe in the deployed VM and was not used for final environment reporting.
- The five-round A/B median work P50 delta for `32795 -> 32799` was -19.52%
  with accounting on and -19.16% with accounting off. P95/tail deltas are
  reported without a causal claim.

## Review record

Pending explicit reviewer approval for Milestone 2.

Commit-message validation notes: commits `82f4af600`, `ed09cf657`,
`f02f9dcff`, and `1fb0b1d35` contain historical body-line formatting defects
detected by post-commit checks. History was not rewritten, per the execution
constraints; the defects are recorded here for reviewer visibility.

## Resume command

Read this state, then the Milestone 1 sections of the active plan and the
listed primary implementation paths. Before the next commit, validate headers,
stage only explicit task paths, run cached whitespace checks, and validate the
commit message.
