<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Warm copyRect evidence index

## Cached-final copyRect correction

- `2026-09-10` — revision `efe0f3b24` — PASS — `Graphics.copyRect(Image,
  ...)` now checks the final materialized variant for destination scale and
  source decode generation before creating a draw plan. The focused smoke
  recorded one first fallback materialization and plan, zero physical-variant
  stores, cache-served full and partial copies, correct scale and decode
  generation invalidation, and unchanged drawImage plan execution. Artifacts:
  `image-warm-copyrect-fast-path/copyrect-revalidated/`.
- The fresh-process 663-JPEG matrix passed all 24 cold/warm/warm2 records at
  480x720 and 540x960 across the four target-color/physical-variant profiles.
  Warm JPEG decodes were zero; warm physical-variant stores and bytes were
  zero because the cached final raster served copyRect directly. Warm p95 was
  9 ms at 480x720 and 9–13 ms at 540x960. Cold backing live/peak bytes were
  `940232584` and `1019597208`, respectively.
- Feature-14 physical-variant creation remains covered by the mutation smoke
  when no final materialization is available: one materialization and
  `160000` variant bytes with passing pixel parity. Startup defaults and warm
  full/partial hash lanes also passed. Runtime SHA-256:
  `efef5fb8b062df88054daa7c2e4aeeff98b1b1dd1c2ea49e00aad48ce188a61a`.

## Authoritative revalidation

- `2026-09-09T23:15:00-03:00` — code revision `0366e909f` — PASS — startup
  defaults, direct physical-variant smoke, corrected warm micro, and the
  fresh-process 663-JPEG matrix were rerun after the direct-copy fix. The
  authoritative artifacts are in
  `image-warm-copyrect-fast-path/final-revalidated/`.
- Final matrix: 24 pass records across 480x720/540x960, target-color
  disabled/enabled, and physical-variant disabled/enabled, with three passes
  each. Warm p95 is 9 ms everywhere; warm JPEG decodes are zero everywhere.
  Variant-enabled warm writes are 4509 and 5898 hits at the two resolutions.
- Rejection diagnostics: mapping geometry dominates at 4509 and 5898 rejects
  per pass; backing rejects are zero. Cold physical-variant bytes are
  268180848 and 339890928. `target_color_converted_bytes` is emitted and zero
  for this macOS target.
- Runtime SHA-256:
  `efef5fb8b062df88054daa7c2e4aeeff98b1b1dd1c2ea49e00aad48ce188a61a`.
- Scope: macOS SDK/native validation only; Android, Linux, Windows, and iOS
  were intentionally deferred.

- `2026-09-09T23:22:23Z` — rewrite — remote
  `perf/image-scroll-raster-fast-path` moved from `640e327cd584342e3137260272e733f5b47a39f7`
  to `81bb027e650712af29d7df8ecde6a6caadaf763e` with the required lease.
- `2026-09-09T23:45:00Z` — milestone-0-baseline — revision
  `9e959282c5f6f39d79e4c3c12d96d96c342aef44` — PASS — 663 JPEGs, 2
  resolutions, 4 feature-13/14 profiles, 3 passes each; artifacts:
  `image-warm-copyrect-fast-path/baseline/`.
  Native runtime SHA-256:
  `73a2e6f6b3ceaaf4ad4f55c8312d5d4ca574f5261fc715245f5e9fb9685b68e8`.
- `2026-09-09T23:45:00Z` — milestone-0-warm-micro — revision
  `9e959282c5f6f39d79e4c3c12d96d96c342aef44` — PASS — six hash-checked
  materialized/deferred full and partial cases; artifact:
  `image-warm-copyrect-fast-path/baseline/warm-micro.log`.
- `2026-09-09T23:36:40Z` — milestone-1-default-policy — working tree at
  `770c0b43258509bc850c93f153f19cd0f2bd6208` — PASS — focused
  `ImageOptimizationSettingsTest`; log:
  `/tmp/image-warm-copyrect-m1-settings-test.log`.
- `2026-09-10T00:36:32Z` — milestone-3-final-matrix — revision
  `da2c0ba94` — PASS — corrected 663-JPEG matrix across two resolutions,
  four target/variant profiles, and three passes; runtime SHA-256
  `6d2ae5b79c6c886079190657940345b9d55e0502a0af29777b844aac9e2aeab9`;
  artifacts: `image-warm-copyrect-fast-path/final/`.
- `2026-09-10T00:36:32Z` — milestone-3-final-warm-micro — revision
  `da2c0ba94` — PASS — six hash-checked materialized/deferred full and
  partial cases; artifact:
  `image-warm-copyrect-fast-path/final/warm-micro.log`.
- `2026-09-10T00:36:32Z` — milestone-3-direct-copy-smokes — revision
  `da2c0ba94` — PASS — physical identity, guard, and copyRect fallback/gate
  smoke coverage; artifacts:
  `image-warm-copyrect-fast-path/final/{physical-identity-smoke,physical-guards-smoke,copyrect-smoke}.log`.
