<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image native backing plan 05 state

- Status: complete; Plan 5 and the full five-plan image-native-backing sequence are complete, including the FRAME_LAYOUT compatibility correction at `ee0fbe23a`.
- Starting revision: `15c7e813a` (`docs(plans): close native image color plan four`).
- Plans 1 through 4 are complete; Plan 4 handoff is in
  `.agent/reports/image-native-backing-04-editorial.md`.
- Plan 5 scope: retire `Image.pixels` and `pixelsOfAllFrames`, route readback
  and encoding through explicit backings, and perform the final ABI/lifecycle
  compatibility gate.
- Milestone 1 completed through `8eff5efba` and `e37d15d04`: bounded backing
  readback, rows, PNG/JPG/PDB input, equality/hash, `getPixels`, and native
  frame access.
- Milestone 2 completed atomically in `7639733d6`: slot 0 is `Image.backing`,
  slot 1 is reserved, old Image pixel macros are gone, and all native consumers
  use explicit backing macros.
- Milestone 3 completed in `b7337e93e`: the compact deployed smoke covers the
  final ten scenarios plus the test-only allocation/readback accounting hook.
- Corrective FRAME_LAYOUT milestone completed in `ee0fbe23a`: destination-scale
  dimensions now match JavaSE/native semantics, pure layouts reuse the complete
  native strip, and transformed non-divisible strips preserve residual pixels
  through native readback and PNG round-trip. The zero-width layout regression
  remains covered.
- Structural geometry split completed in `4d9ea4dd7`: geometry compilation and
  direct drawing remain in `skia_image_geometry.cpp`; materialization-specific
  sizing, surface allocation, prefix rendering, and backing registration now
  live in `skia_image_geometry_materialize.cpp`, with a narrow private header.
  Resulting sizes are 16,314 bytes/365 lines, 5,964 bytes/125 lines, and
  1,040 bytes/33 lines for the implementation, materializer, and header.
- Active paths: `TotalCrossSDK/src/main/java/totalcross/ui/image/Image.java`,
  `ImageBacking.java`, `NativeImageBacking.java`, `RasterImageBacking.java`,
  `ImageSource` implementations, `TotalCrossVM/src/nm/instancefields.h`,
  `TotalCrossVM/src/nm/ui/ImagePrimitives_c.h`, and the Skia backing files.
- Required references: read `.agent/plans/exec-plan-image-native-backing-05-retirement.md`
  for the milestone acceptance criteria, and `.agent/plans/image-native-backing-roadmap.md`
  for the fixed architecture.
- Validation budget: SDK and macOS arm64 only. Do not run Android, iOS, Linux,
  or Windows builds/smokes. Keep full logs in task-specific paths and index
  only concise outcomes.
- Deliberately preserved local files: unrelated converter-test edits and
  generated/downloaded SDK/VM artifacts remain outside the plan commits.
- Focused inventory recorded on 2026-09-03 with:
  `rg -n 'Image_pixels|Image_pixelsOfAllFrames|\.pixels\b|pixelsOfAllFrames'
  TotalCrossSDK/src/main/java/totalcross/ui/image TotalCrossVM/src/nm/ui
  TotalCrossVM/src/nm/instancefields.h`.
  Output: `/tmp/image-native-backing-05-inventory.txt` (121 hits). The final
  rerun is `/tmp/image-native-backing-05-m2-inventory-final.txt` (57 legitimate
  backing/source references, zero legacy Image macros/usages).
- M1 focused tests, native builds, SDK distributions, and smoke evidence remain
  indexed in the earlier records below. M2 focused Java tests passed in
  `/tmp/image-native-backing-05-m1-java-tests.log` and
  `/tmp/image-native-backing-05-m2-sdk-abi-tests.log`; the incremental native
  build passed in `/tmp/image-native-backing-05-m2-native-build-rerun.log`.
- Historical Plan 5 gate passed at `b7337e93e`; the FRAME_LAYOUT correction
  gate passed at `ee0fbe23a`. The current structural-split gate passed at code
  revision `4d9ea4dd7`: CMake configure/build logs are
  `/tmp/image-native-backing-geometry-split-final-arm64-cmake.log` and
  `/tmp/image-native-backing-geometry-split-final-arm64-build.log`; frame-state
  smoke is `/tmp/image-native-backing-geometry-split-final-frame-state-smoke.log`;
  geometry and materialization smokes are
  `/tmp/image-native-backing-geometry-split-final-geometry-run.log` and
  `/tmp/image-native-backing-geometry-split-final-materialization-run.log`; final
  image-backing smoke is `/tmp/image-native-backing-geometry-split-final-lazy-smoke.log`.
  All required fields passed, including multi-row `applyFade`, truncated and
  zero-width frame layouts, destination-scale 2px output, residual-strip and
  PNG round-trip checks, `backingReadbackCount=2`, and `overallPass=true`.
- The structural-only commit reused the already-built unchanged SDK/Java smoke
  artifacts and injected the newly built arm64 dylib into each deployed smoke.
- The commit-message check accepted the title but reported one body line over
  80 characters; history was preserved without amend/rewrite as required.
- Static final checks found no `Image_pixels` or `Image_pixelsOfAllFrames`
  native macro/use. `Image.java` retains only legitimate backing/source API
  references and no `int[] pixels` field. Headers and staged diffs passed.
- No Android, iOS, Linux, or Windows builds/smokes were run under the fixed
  validation policy. Unrelated converter-test edits and generated/downloaded
  SDK/VM artifacts remain outside the plan commits.
- Editorial completion report: `.agent/reports/image-native-backing-05-editorial.md`.
- Resume/audit command:
  `sed -n '1,220p' .agent/state/image-native-backing-05.md`.
