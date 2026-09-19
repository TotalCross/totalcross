<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# M3 — structural validation

Implementation HEAD: `367fc887c`.

The historical M3 implementation commit `0129316af` remains unchanged and
preserved the approved corrections: canonical intrinsic target-color identity,
physical identity without target surface dimensions, and conservative saved
rectangular-clip handling. Follow-up commits `0528eb62b` and `367fc887c`
corrected logical-to-physical mapping and strengthened the structural gate.
Defaults, delayed materialization, the single shared raster-variant slot, and
fallback behavior remain unchanged.

## Native validation

The macOS ARM64 Release build passed for `tcvm`, `Launcher`, and
`skia_surface_test`. The native test passed with final-pixel assertions for
target-color reuse after a position change and physical-variant reuse across
compatible surface sizes. It also covers a real 2× logical-to-physical draw,
rectangular clip protection and outside pixels, `saveLayer` fallback,
non-rectangular clip, rotation, skew, perspective, and invalid mapping cases.

## Bundle gate

Final bundle:
`build/image-raster-policy-03-package-m3-scaled2/image-scroll-benchmark-macos-arm64`

The bundle passed self-test and the exact `raster-structural-smoke` profile:

| field | value |
|---|---|
| masks | `0,8192,16384,32768,57344` |
| prefetch | `on` |
| accounting | `on` |
| rounds/processes | `2 / 10` |
| process results | `10/10 PASS` |
| frames per process | `189` |
| scroll | `0 -> 39091`, endpoint reached |
| corpus | `663` JPEGs, hash `588a7e0f4019424a` |
| target | `1080x1920`, rowBytes `4320`, BGRA8888, software |

No process timed out. Every pass record reached `scroll_end=39091` and every
summary reported `overallPass=true`. Disabled paths stayed at zero and
writePixels attempts equaled hits plus fallbacks.

The strengthened runner gate also proved that enabled target-color and
physical-identity candidates were not rejected 100% at `RootToDevice`:

- target-color: attempts/fallbacks/hits/materializations `12/12/0/0`,
  `RootToDevice=0`, later `SourceMapping=12`;
- physical identity: attempts/hits/fallbacks `12/0/12`,
  `RootToDevice=0`, later `SourceMapping=12`;
- physical variant: lookups/misses/hits/stores `12/12/0/0`, with `12` full
  keys and `12` no-surface-size keys, and no `RootToDevice` rejects.

The real workload therefore traversed the scaled mapping proof and reached
later source-mapping fallback behavior. Target-color materialization and
physical-variant stores remained zero because this profile did not produce the
required later observation; that is documented structural evidence, not a
performance conclusion.

## Artifacts and hashes

- SDK compile/deploy JAR SHA-256:
  `4c1d1a70069dae3c8cc5e11b77eb55d69c300793a896726dfb27bf3b8538e711`;
- SDK package ZIP SHA-256:
  `d1bbd5301db88a2eea4362732c09ed320311e53d6adc0b4d5c45c68f209e46d7`;
- runtime SHA-256:
  `33f50a0cc910947fe2b102c4e1c65aed37ca4d830734ad2c9daedd965bf3c2e0`;
- Launcher SHA-256:
  `ef6f924f3beda71e6615badf94dd93d5dd167a332250aa22e6dc3855cbcf384a`;
- bundle ZIP SHA-256:
  `86b7239434a6771c21a1d1460a7d84d3af9cddfd78854b18b46de3867328740f`;
- result ZIP SHA-256:
  `2622321e0a1178fc368a11e359c5085c17f38e0b44b430e2680fe94f787fafd4`.

Logs are outside the repository:

- `/tmp/image-raster-m3-scaled-native-build.log`;
- `/tmp/image-raster-m3-scaled-native-test.log`;
- `/tmp/image-raster-m3-scaled2-package.log`;
- `/tmp/image-raster-m3-scaled2-self-test.log`;
- `/tmp/image-raster-m3-scaled2-structural-smoke.log`.

`STOP / REVIEW 3` is ready. M4 reuse and RGB565/target-color measurements
remain gated and were not started.
