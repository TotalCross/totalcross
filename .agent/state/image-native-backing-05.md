<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image native backing plan 05 state

- Status: ready; Plan 5 has not started.
- Starting revision: `59cda45a6` (`feat/image-native-backing`).
- Plans 1 through 4 are complete; Plan 4 handoff is in
  `.agent/reports/image-native-backing-04-editorial.md`.
- Plan 5 scope: retire `Image.pixels` and `pixelsOfAllFrames`, route readback
  and encoding through explicit backings, and perform the final ABI/lifecycle
  compatibility gate.
- First milestone: migrate bounded pixel reads, PNG/JPG/PDB encoding,
  equality/hash, `getPixels`, and native frame access. Do not edit Image object
  slots until the focused inventory is recorded.
- Second milestone: atomically move the native object slot to `Image.backing`,
  retain the reserved legacy slot, remove `Image_pixels` and
  `Image_pixelsOfAllFrames`, and update every native consumer in one ABI commit.
- Third milestone: run the final macOS arm64 SDK/native gate and one compact
  native smoke for decode, generated surfaces, geometry, color, readback,
  encoding, cache reuse, and detached `getPixels` snapshots.
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
- Resume command:
  `sed -n '1,220p' .agent/state/image-native-backing-05.md`.
