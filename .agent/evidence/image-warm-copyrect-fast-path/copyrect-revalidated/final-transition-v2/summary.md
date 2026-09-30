<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Final cached-raster transition revalidation

The final implementation stores the bounded physical variant only when no
final raster is available. A successful equivalent final materialization now
evicts that resident physical variant from the same backing slot. The focused
smoke records `resident=true`, `equivalent_evictions=1`,
`noLongerResident=true`, `cachedFinal=true`, and
`non_equivalent_evictions=0`, with pixel-hash parity.

The customer matrix contains 663 JPEGs at 480x720 and 540x960 across all four
target-color / physical-variant profiles and cold/warm/warm2 passes. All 24
records passed. Warm targeted/full JPEG decodes, physical-variant stores, and
physical-variant bytes were zero. Warm frame-time p95 was 9 ms in every
profile and resolution in this final run. The native runtime SHA-256 was
`32cce8727e61f1f6f1bc06301e62fdd466e507d3a439c513e3b8e00fd251e832`.

Focused smoke logs are under `focused/`; matrix logs and `results.csv` are
authoritative for this final implementation. Build logs are intentionally not
part of the evidence set.
