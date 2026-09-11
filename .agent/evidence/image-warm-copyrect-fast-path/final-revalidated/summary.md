<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Final revalidation summary

Code revision: `0366e909f`.

- Fresh-process startup smoke: `drawMask=32799`, `decodeMask=32799`,
  `expectedMask=32799`, all pass. This is bits 0, 1, 2, 3, 4, and 15.
- Fresh-process customer matrix: 663 JPEGs, 480x720 and 540x960, all four
  target-color/physical-variant profiles, three passes per profile; all 24
  pass records succeeded.
- Warm matrix p95: 9 ms for every profile at both resolutions. Warm targeted
  and full JPEG decodes were zero in every profile.
- Variant-enabled warm lanes recorded `write_pixels_hits=4509` at 480x720 and
  `5898` at 540x960. Variant cold materialization bytes were `268180848` and
  `339890928`, respectively; warm materialization bytes were zero.
- Target-color output is present in every record; this macOS raster target
  selected no conversion, so `target_color_converted_bytes=0` throughout.
- Rejection diagnostics identify mapping geometry as dominant: 4509 rejects
  per 480x720 pass and 5898 per 540x960 pass. Backing rejects were zero.
- Warm micro full/partial hashes matched across materialized and independent
  deferred draw/copy lanes. The identity-direct lane recorded
  `physical_identity_hits=2560`, `write_pixels_hits=2560`, and zero generic or
  smooth-resample draws.
- Physical-variant smoke recorded one warm hit, one write-pixels hit, zero
  generic/smooth draws, and passed alpha, hardware-scale, rotation, and
  combined fallback guards.

The matrix runtime SHA-256 was
`efef5fb8b062df88054daa7c2e4aeeff98b1b1dd1c2ea49e00aad48ce188a61a`.
The matrix was run on macOS only; Android, Linux, Windows, and iOS builds were
intentionally deferred per the execution plan.
