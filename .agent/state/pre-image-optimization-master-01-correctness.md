<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Pre-image optimization master 01 correctness state

- Active milestone/slice: Milestone 1 bootstrap complete; inspect targeted JPEG
  metadata boundaries and existing regressions.
- Branch: `fix/pre-image-optimization-master`.
- BASE_SHA: `1898014784b2fba5716cc033e49520740b05f0dd`.
- Last logical commit: none; bootstrap files are staged for the first commit.
- Active paths: `TotalCrossSDK/src/main/java/totalcross/ui/image/`; focused
  image tests and smoke registration; targeted native JPEG/image paths named by
  the plan.
- Next exact action: inspect `ImageDecodeRequirement.choose()`, encoded-root
  materialization, cached encoded-source reconstruction, decoded source fields,
  targeted native decode, and root `contentScale` use before editing.
- Focused validation completed: branch base and scoped worktree inspection;
  no build run per Milestone 0.
- Deferred validation: SDK tests/dist and macOS native build/smoke until the
  implementation and regression matrix are complete.
- Decisions still active: preserve adaptive smooth denominators; repair scale
  metadata at fresh and cached materialization boundaries; keep nearest,
  rotate-scale, and explicit JPEG factories eager/full-decode.
- Blocker: none.
- Deliberate out-of-scope local files: unrelated untracked `.agent/evidence/`,
  `.agent/plans/`, `.agent/reports/`, and `.agent/state/` artifacts listed by
  scoped status; do not stage them.
- Resume command: read this file, then inspect the plan's Milestone 1 contract
  and the active source paths narrowly.
