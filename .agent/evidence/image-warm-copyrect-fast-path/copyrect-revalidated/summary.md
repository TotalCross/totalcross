<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Cached-final copyRect revalidation

Revision `efe0f3b24` passed the focused macOS copyRect smoke and
the fresh-process customer matrix. The native runtime SHA-256 was
`efef5fb8b062df88054daa7c2e4aeeff98b1b1dd1c2ea49e00aad48ce188a61a`.

- Focused copyRect smoke: full and partial hashes passed; the first fallback
  performed one native geometry materialization and one draw-plan creation;
  the cached repeat created no plan, performed no materialization, and stored
  no physical variant. Destination-scale and encoded-source-generation
  invalidation both rematerialized correctly. The drawImage control case
  continued to execute its draw plan.
- Customer matrix: 663 JPEGs, 480x720 and 540x960, all four target-color /
  physical-variant profiles, and cold/warm/warm2 fresh processes; all 24
  records passed. Warm JPEG decodes were zero and warm physical-variant stores
  and bytes were zero because cached final rasters served copyRect directly.
- Warm frame-time p95 was 9 ms in every 480x720 profile and 9–13 ms at
  540x960. Cold backing live/peak bytes were 940232584 at 480x720 and
  1019597208 at 540x960; the values remained stable across warm passes.
- Feature-14 variant creation remains covered separately when no final raster
  exists: the mutation smoke recorded two lookups, one materialization, and
  `physical_variant_bytes=160000`, with passing pixel parity.

Matrix logs and `results.csv` in this directory are the authoritative fresh
process records. Ordinary Gradle and native build logs are intentionally not
included.
