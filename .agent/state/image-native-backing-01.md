<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image native backing plan 01 state

- Starting HEAD: `d955b6d5b9de7ca27f889b55ca1ea295499fa6d3`
- Active milestone: complete — plan 1 closed.
- Active slice: hand off additive Java/Skia backing foundations to plan 2.
- Scoped paths: `TotalCrossSDK/src/main/java/totalcross/ui/image/`,
  `TotalCrossVM/src/nm/ui/`, `TotalCrossVM/src/nm/instancefields.h`,
  `.agent/state/image-native-backing-01.md`, and
  `.agent/evidence/image-native-backing-01.jsonl`.
- Last commit: `1f81dc624` — registered native backing field access. Native
  implementation commit: `481698aff`; previous Java foundation commits:
  `635a30eb3`, `e82957614`.
- Next action: resume with
  `.agent/plans/exec-plan-image-native-backing-02-materialization.md` after
  reviewing this plan's handoff.
- Focused validation: copyright headers passed for the foundation files;
  scoped and staged `git diff --check` passed; focused SDK image/backing tests
  passed; SDK `dist -x test` passed. The local commit-message validator
  reported one overlong body line for each foundation commit; history was not
  rewritten per repository policy.
- Validation completed: macOS arm64 `tcvm`/`Launcher` build passed;
  deterministic native backing smoke passed after that build; focused SDK
  backing/ABI tests and `dist -x test` passed.
- Deferred validation: Android, iOS, Linux, and Windows builds were not run
  because the roadmap and plan scope permit only SDK and macOS arm64 gates.
- Deliberately out of scope: existing unrelated untracked files in
  `.agent/plans/`, `scripts/`, and `tests/`.
- Resume command: `sed -n '1,180p'
  .agent/state/image-native-backing-01.md`.
