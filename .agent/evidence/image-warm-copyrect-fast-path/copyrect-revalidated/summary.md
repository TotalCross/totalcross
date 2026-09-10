<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Cached-final copyRect revalidation

The final transition implementation passed the focused macOS copyRect smoke
and the fresh-process customer matrix. Authoritative final evidence is in
`final-transition-v2/`; the native runtime SHA-256 was
`32cce8727e61f1f6f1bc06301e62fdd466e507d3a439c513e3b8e00fd251e832`.

- Focused copyRect smoke: full and partial hashes passed; the first fallback
  performed one native geometry materialization and one draw-plan creation;
  the cached repeat created no plan, performed no materialization, and stored
  no physical variant. The variant-first transition recorded one resident
  physical variant, one equivalent-final eviction, no later physical hit,
  cached-final copyRect reuse, and retained a scale-mismatched variant.
  Destination-scale and encoded-source-generation invalidation both
  rematerialized correctly. The drawImage control case continued to execute
  its draw plan.
- Customer matrix: 663 JPEGs, 480x720 and 540x960, all four target-color /
  physical-variant profiles, and cold/warm/warm2 fresh processes; all 24
  records passed. Warm JPEG decodes were zero and warm physical-variant stores
  and bytes were zero because cached final rasters served copyRect directly.
- Warm frame-time p95 was 9 ms in every profile and resolution in the final
  run. Cold backing live/peak bytes remained stable at 940232584 for 480x720
  and 1019597208 for 540x960.
- Feature-14 variant creation remains covered separately when no final raster
  exists: the mutation smoke recorded two lookups, one materialization, and
  `physical_variant_bytes=160000`, with passing pixel parity.

Matrix logs and `results.csv` in `final-transition-v2/` are the authoritative
fresh-process records. Ordinary Gradle and native build logs are intentionally
not included.
