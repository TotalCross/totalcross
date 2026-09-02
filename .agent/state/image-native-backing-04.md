<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image native backing plan 04 state

- Status: in progress.
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
- Active milestone: Milestone 2, APPLY_COLOR and APPLY_COLOR2. The next slice
  must add exact multiplier mapping first, then full-strip source analysis and
  tie behavior for APPLY_COLOR2.
- Plan 4 working set: Image color mutation methods, ImagePipeline, native
  geometry/materialization, Skia backing/readback, and focused color tests.
- Android, iOS, Linux, Windows, and the full platform matrix remain outside
  the roadmap build budget.
- Resume command:
  `sed -n '1,220p' .agent/state/image-native-backing-04.md`.
