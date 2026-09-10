<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Warm copyRect evidence index

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
