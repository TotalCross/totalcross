<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Lazy explicit JPEG factories state

- Branch: `perf/image-jpeg-factories-lazy`
- Frozen Phase-3 base: `224682b15a244201718701ad24be8f9d54b3fa71`
- Phase-3 runtime candidate retained for handoff: `d2f8195b9faf828f4ee04154c93a93e1136c5bd4`
- Active milestone: lazy explicit JPEG factory closeout
- Last runtime commit: `4ce4c74d2d6310ff0eb603546c81da28ebf1bc9f`
- Current docs/test branch tip before closeout docs: `ff5d3ac2cf188a65ae5de919e844c97b6853c664`
- Active paths: `TotalCrossSDK/src/main/java/totalcross/ui/image/Image.java`,
  `ImagePipeline.java`, new `ImageDecodePolicy.java`, focused JPEG tests,
  native image registration/bridge files, existing deployed image smokes.
- Deliberately out of scope: Phase-4 lifecycle/cache-budget work, generated
  native dependency directories, unrelated dirty-worktree files.
- Completed: exact-base branch bootstrap; immutable policy/pipeline resolver;
  native private decode bridges and retired public eager registrations; focused
  Java tests; deployed macOS smoke and benchmark; Android build/package check;
  headers and diff validation.
- Focused validation: `totalcross.ui.image.*` tests passed; SDK
  `dist -x test` passed; Release macOS software-Skia CMake/Ninja passed;
  legacy lazy smoke and focused JPEG factory smoke passed; 60-sample factory
  benchmark passed; Android `:tcvm:externalNativeBuildRelease` and
  `:app:assembleStandardRelease` passed.
- Benchmark result: eager reference was about 8–9 ms, factory-only 0–1 ms
  with zero decode/materialization, first draw about 5 ms with one decode and
  one materialization, and repeated draws about 5–6 ms with one reusable
  decode/materialization per sample.
- Deferred validation/blockers: Android APK installation was blocked by the
  connected device policy (`INSTALL_FAILED_USER_RESTRICTED`); no GitHub matrix
  run exists for this unpublished branch/candidate SHA. The existing Phase-3
  GitHub/Android results remain historical evidence for the retained Phase-3
  candidate and are not relabeled as this milestone's result.
- Final decision: `NO-GO` to begin Phase 4 until the final runtime candidate
  receives the cross-platform matrix and an installable Android GPU smoke.
- Next action: commit closeout state/evidence/editorial updates, report the
  final docs tip separately from the runtime SHA, and stop.
- Resume command: `sed -n '1,220p' .agent/state/image-jpeg-factories-lazy.md`
