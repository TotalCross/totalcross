<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image native backing plan 04 state

- Status: stopped at the Milestone 2 boundary; Milestone 2 is complete and
  Milestone 3 has not started.
- Plan 3 remains complete and unchanged through `9d14d42b5`.
- Milestone 1 (fade, alpha, and touch-up) is complete through
  `c16f68a3b`. Checkpoint A is `7b70f068c`; checkpoint B is `c16f68a3b`.
- Native color operations use exact unpremultiplied RGBA passes over Skia
  backing. In-place APPLY_FADE preserves the selected frame scope; result
  operations replace the destination backing without populating `Image.pixels`.
- Milestone 1 validation passed: macOS arm64 configure/build, focused SDK
  color/transform tests, `dist -x test`, smoke compilation, and the native
  color-filter smoke. See append-only evidence in
  `.agent/evidence/image-native-backing-04.jsonl` for commands and logs.
- Milestone 2 (APPLY_COLOR and APPLY_COLOR2) is complete through
  `a1635eb26`. Checkpoint C is `dcd4e026d`; checkpoint D is `a1635eb26`.
- APPLY_COLOR now uses the exact Java multiplier mapping with clamping, alpha
  preservation, and byte-identical transparent pixels. APPLY_COLOR2 performs
  native full-strip opaque-pixel analysis with the existing brightness
  weighting, strict tie behavior, zero-channel replacement, and optional
  `0xAA` alpha control before applying the exact mapping.
- Milestone 2 validation passed: macOS arm64 native build, focused SDK
  differential tests, and native decode/apply/save/readback smoke. The SDK
  `dist -x test` result from the M1 gate remains applicable because M2 changed
  no SDK production sources; the M2 native and test changes were rebuilt and
  retested. See append-only evidence in
  `.agent/evidence/image-native-backing-04.jsonl` for commands and logs.
- Execution is intentionally stopped at this milestone boundary per the
  current task. Milestone 3 exact color-key passes and composition work remain
  pending for a later resume.
- Plan 4 working set: Image color mutation methods, ImagePipeline, native
  geometry/materialization, Skia backing/readback, and focused color tests.
- Android, iOS, Linux, Windows, and the full platform matrix remain outside
  the roadmap build budget.
- Resume command:
  `sed -n '1,220p' .agent/state/image-native-backing-04.md`.
