<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image native backing plan 03 state

- Starting HEAD: `6cc978a0c`
- Milestone checkpoint commit: `04a7bfa0a` — plan 3 milestones 1 and 2
  implementation and focused fixtures.
- Active milestone: milestone 2 complete; plan 3 remains open at milestone 3.
- Active slice: resume later with rotate/save validation and any remaining
  geometry materialization barriers. The current execution is intentionally
  stopped at this milestone.
- Scoped paths: `TotalCrossSDK/src/main/java/totalcross/ui/image/`,
  `TotalCrossSDK/src/main/java/totalcross/ui/gfx/Graphics.java`,
  `TotalCrossSDK/src/test/`, `TotalCrossSDK/src/smokeTest/`,
  `TotalCrossVM/src/nm/ui/skia/`, `TotalCrossVM/src/nm/ui/`, and
  `.agent/state/image-native-backing-03.md`,
  `.agent/evidence/image-native-backing-03.jsonl`, and
  `.agent/reports/image-native-backing-03-editorial.md`.
- Previous plan handoff: plan 2 made generated and decoded deployed Skia
  Images native-backed and added backing-aware materialization barriers.
- Implemented SDK paths: `ImageGeometryPlan.java`, `ImagePipeline.java`,
  `ImageDrawingBridge.java`, `NativeImageBacking.java`, `Image.java`,
  `Graphics.java`, `build.gradle`, and the focused geometry/crop smoke
  fixtures. Temporary diagnostic output has been removed.
- Implemented VM paths: `TotalCrossVM/src/nm/instancefields.h`,
  `TotalCrossVM/src/nm/ui/skia/skia.h`,
  `TotalCrossVM/src/nm/ui/skia/skia_image_backing.cpp`,
  `TotalCrossVM/src/nm/ui/GraphicsPrimitivesSkia_c.h`,
  `TotalCrossVM/src/nm/ui/gfx_Graphics.c`,
  `TotalCrossVM/src/nm/ui/image_NativeImageBacking.c`,
  `TotalCrossVM/src/nm/NativeMethods.txt`,
  `TotalCrossVM/src/nm/NativeMethods.h`,
  `TotalCrossVM/src/nm/NativeMethodsPrototypes.txt`, and
  `TotalCrossVM/src/nm/nativeProcAddressesTC.c`.
- Checkpoint results: final macOS arm64 configure/build, focused SDK tests, SDK
  distribution, smoke compilation, copyright validation, direct geometry
  smoke, crop/frame differential smoke, and native materialization smoke all
  passed. The direct geometry fixture passes nearest, crop, smooth, chained
  scale-then-crop, rotation, and frame-selection checks. The crop/frame fixture
  passes encoded multi-frame metadata, fractional scales, alpha masks,
  hardware scales, targeted JPEG behavior, and fallback behavior. The native
  materialization fixture passes the 500x500 to 89x89 ImageControl regression.
- Deferred broad smoke results: the legacy lazy materialization smoke fails at
  `draw/pixel_barriers` because deployed `getPixels()` now returns detached
  snapshots and the fixture still requires array identity; the ABI smoke exits
  139 after reaching legacy mutable-array/color-native assumptions. These are
  recorded as deferred plan-4 compatibility work, not milestone-2 acceptance
  failures.
- Next action on resume: read this state first, then run the plan-3 milestone-3
  rotate/save gate. Do not restart milestone 2 or execute plan 4 from this
  checkpoint.
- Known caveat: color/mutation nodes remain on the old resolver and broad native
  color ABI behavior is intentionally outside this geometry milestone.
- Validation policy: use only the plan's SDK and macOS arm64 milestone gates;
  keep full logs under `/tmp` and append compact results to evidence.
- Deliberately out of scope: plan 4 color migration, plan 5 retirement, and
  unrelated dirty worktree files.
- Deferred validation: Android, iOS, Linux, Windows, and the full platform
  matrix remain skipped because the roadmap authorizes only SDK and macOS
  arm64 validation for this plan.
- Evidence and logs: `.agent/evidence/image-native-backing-03.jsonl` records
  the completed checks. Full logs are in `/tmp`, including
  `/tmp/image-native-backing-03-m1-static.log` and
  `/tmp/image-native-backing-03-stop-static.log`,
  `/tmp/image-native-backing-03-m1-cmake.log`,
  `/tmp/image-native-backing-03-m1-native-build.log`,
  `/tmp/image-native-backing-03-m1-test.log`,
  `/tmp/image-native-backing-03-m1-sdk-tests.log`,
  `/tmp/image-native-backing-03-m1-dist.log`,
  `/tmp/image-native-backing-03-native-generator.log`,
  `/tmp/image-native-backing-03-m1-smoke-deploy.log`, and
  `/tmp/image-native-backing-03-m1-smoke-run.log`, plus the current milestone-2
  logs `/tmp/image-native-backing-03-m2-cmake.log`,
  `/tmp/image-native-backing-03-m2-native-build.log`,
  `/tmp/image-native-backing-03-m2-sdk-tests.log`,
  `/tmp/image-native-backing-03-m2-dist.log`,
  `/tmp/image-native-backing-03-m2-compile-smoke.log`,
  `/tmp/image-native-backing-03-m2-geometry-smoke-f.log`,
  `/tmp/image-native-backing-03-m2-crop-frame-smoke-debug3.log`, and
  `/tmp/image-native-backing-03-m2-lazy-smoke.log`. Final checkpoint logs are
  `/tmp/image-native-backing-03-m2-cmake-final.log`,
  `/tmp/image-native-backing-03-m2-native-build-final.log`,
  `/tmp/image-native-backing-03-m2-sdk-tests-final.log`,
  `/tmp/image-native-backing-03-m2-dist-final.log`,
  `/tmp/image-native-backing-03-m2-compile-smoke-final.log`,
  `/tmp/image-native-backing-03-m2-headers-final2.log`,
  `/tmp/image-native-backing-03-m2-crop-frame-smoke-conditional.log`,
  `/tmp/image-native-backing-03-m2-geometry-run-clean.log`, and
  `/tmp/image-native-backing-03-m2-materialization-run-final2.log`.
  Direct geometry staging is `/tmp/image-native-backing-03-m2-geometry-final2.HJzwPF`;
  materialization staging is
  `/tmp/image-native-backing-03-m2-materialization-final2.eWLckz`.
- Resume command: `sed -n '1,220p' .agent/state/image-native-backing-03.md`.
