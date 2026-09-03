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
- Historical Plan 5 gate passed at `b7337e93e`; it is superseded as current
  validation by the final gate at code revision `ee0fbe23a`:
  CMake configure/build logs are `/tmp/image-native-backing-frame-layout-final-arm64-cmake.log`
  and `/tmp/image-native-backing-frame-layout-final-arm64-build.log`; focused
  SDK tests are in `/tmp/image-native-backing-frame-layout-final-focused-tests.log`;
  SDK `dist` is in `TotalCrossSDK/agent-logs/20260903-000517-dist-full.log`
  with compact summary `TotalCrossSDK/agent-logs/20260903-000517-dist-agent.log`;
  frame-state smoke is `/tmp/image-native-backing-frame-layout-final-frame-state-smoke.log`;
  geometry and materialization smokes are
  `/tmp/image-native-backing-frame-layout-final-geometry-run.log` and
  `/tmp/image-native-backing-frame-layout-final-materialization-run.log`; final
  image-backing smoke is `/tmp/image-native-backing-frame-layout-final-lazy-smoke.log`.
  All required fields passed, including `frameLayoutScaledResidual=true`,
  `frameLayoutResidualRoundTrip=true`, `backingReadbackCount=2`, and
  `overallPass=true`.
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
