<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image native backing plan 02 state

- Starting HEAD: `1f81dc6242d1fe593c88aa342f8e78f5ea813380`
- Active milestone: complete — plan 2 closed.
- Active slice: hand off native generated/decode materialization and barrier
  behavior to plan 3.
- Scoped paths: `TotalCrossSDK/src/main/java/totalcross/ui/image/`,
  `TotalCrossSDK/src/main/java/totalcross/ui/gfx/Graphics.java`,
  `TotalCrossVM/src/nm/ui/`, `TotalCrossVM/third_party/png/`,
  `TotalCrossVM/third_party/jpeg/`, `TotalCrossVM/src/nm/instancefields.h`,
  `.agent/state/image-native-backing-02.md`, and
  `.agent/evidence/image-native-backing-02.jsonl`.
- Previous plan handoff: plan 1 closed with additive backing contracts and
  opaque Skia ownership.
- Implementation commit: `4f5c58da8` — moved generated/decoded Images, draw/readback,
  snapshots, and deferred scale materialization to native backing on Skia.
- Next action: resume with
  `.agent/plans/exec-plan-image-native-backing-03-geometry.md`; plan 3 was not
  started in this turn.
- Validation policy: use only the plan's SDK and macOS arm64 milestone gates;
  keep full logs under `/tmp` and append compact results to evidence.
- Focused validation: copyright headers, staged diff check, focused SDK image
  tests, SDK distribution, smoke compilation, macOS arm64 `tcvm`/`Launcher`,
  issue-417 generated-image smoke, and native materialization smoke passed.
- Deferred validation: Android, iOS, Linux, Windows, and the full platform
  matrix were not run because the roadmap permits only SDK and macOS arm64
  gates.
- Commit-message check: one overlong body line was reported for `4f5c58da8`;
  history was not rewritten.
- Deliberately out of scope: unrelated dirty worktree files and plans 3–5.
- Resume command: `sed -n '1,180p' .agent/state/image-native-backing-02.md`.
