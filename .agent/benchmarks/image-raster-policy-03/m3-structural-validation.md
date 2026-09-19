<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# M3 — structural validation

Implementation commit: `0129316af` (`fix(vm,benchmark): apply approved raster
reuse corrections`). The implementation changed only the approved M3
families:

- the real target-color key now contains source generation, source decode
  generation, target color type, and kind;
- the real physical key no longer contains target surface width or height;
- saved canvas states are accepted only when the current Skia clip is a
  non-empty rectangle; non-rectangular clips remain on the fallback path.

Delayed materialization, default masks, the single shared raster-variant slot,
and fallback behavior were preserved.

## Native validation

The macOS ARM64 native build and `skia_surface_test` passed. The M3 test covers
target position changes, compatible surface sizes, saved rectangular clips,
outside-clip protection, translated positive-scale matrices, and conservative
fallback for non-rectangular clips, rotation, and skew.

## Bundle gate

Final bundle:
`build/image-raster-policy-03-package-m3-native/image-scroll-benchmark-macos-arm64`

The bundle passed self-test and the exact `raster-structural-smoke` profile:

| field | value |
|---|---|
| masks | `0,8192,16384,32768,57344` |
| prefetch | `on` |
| accounting | `on` |
| rounds/processes | `2 / 10` |
| process results | `10/10 PASS` |
| frames per process | `189` |
| scroll endpoint | `39091` |
| corpus | `663` JPEGs, hash `588a7e0f4019424a` |
| target | `1080x1920`, rowBytes `4320`, BGRA8888, software |

No process timed out. The runner verified that disabled paths remained at zero,
enabled paths produced attempts or measured rejection reasons, and writePixels
attempts equaled hits plus fallbacks. All benchmark output reported
`overallPass=true`.

Aggregate counters over the ten processes:

- target-color: attempts/fallbacks/hits/materializations `12/12/0/0`; all
  twelve rejects were `root-to-device` mapping rejects;
- physical variant: lookups/misses/hits/stores `12/12/0/0`; twelve full keys
  and twelve no-surface-size keys were observed;
- physical identity: attempts/hits/fallbacks `12/0/12`; all twelve rejects
  were `root-to-device` mapping rejects;
- writePixels attempts/hits/fallbacks `0/0/0`.

The workload still does not reach target-key acquisition or a second physical
observation, so zero benchmark hits are documented eligibility/delayed-
observation outcomes rather than a performance conclusion. The native tests
are the direct evidence for key reuse and rectangular saved-clip behavior.

## Artifacts and hashes

- SDK compile/deploy JAR SHA-256:
  `4c1d1a70069dae3c8cc5e11b77eb55d69c300793a896726dfb27bf3b8538e711`;
- runtime SHA-256:
  `a7fd330a1fc983d4a0e63e6a53a91edc766995b6cf35e06f6f4218448ad838d3`;
- bundle ZIP SHA-256:
  `70fe022ebb57991f0aa69d53e3c894994ce9ea49bb501605635a17b3eaf879f`;
- result ZIP SHA-256:
  `da92c7308be840f83da66ca0b361abac55e4eef4a82583255419307fb76666ca`.

Logs are outside the repository:

- `/tmp/image-raster-policy-03-m3-native-build.log`;
- `/tmp/image-raster-policy-03-m3-native-test.log`;
- `/tmp/image-raster-policy-03-m3-native-package.log`;
- `/tmp/image-raster-policy-03-m3-native-self-test.log`;
- `/tmp/image-raster-policy-03-m3-native-structural-smoke.log`.

`STOP / REVIEW 3` is ready. M4 reuse and RGB565/target-color measurements
remain gated and were not started.
