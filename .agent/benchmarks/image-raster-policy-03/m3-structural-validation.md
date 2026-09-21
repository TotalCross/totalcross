<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# M3 — structural validation

Implementation HEAD: `404424e3c`.

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

## Final corrective rerun

Implementation HEAD: `3c7864115`, with native correction `922ce2921`.
The lifecycle registry is cleared before canvas replacement, backing release,
screen shutdown, and color-mutation surface replacement. The native tests
also assert final pixels for a real 2x smooth target, fractional target-color
clipping, and inconsistent fractional identity fallback.

The final macOS ARM64 build passed for `tcvm`, `Launcher`, and
`skia_surface_test`. The final bundle is
`build/image-raster-policy-03-package-m3-final/image-scroll-benchmark-macos-arm64`.
Self-test passed with 663 JPEGs and corpus hash `588a7e0f4019424a`. The exact
matrix passed 10/10 processes for masks `0,8192,16384,32768,57344`, prefetch
on, accounting on, and two rounds. Each process completed 189 frames and
reached the automatic-scroll endpoint; no timeout occurred.

The final aggregate counters are:

| counter | value |
|---|---|
| target color attempts/fallbacks/hits/materializations/acquisition sources | `12/12/0/0/12` |
| target mapping RootToDevice/SourceMapping/VisibleMapping | `0/0/0` |
| physical identity attempts/hits/fallbacks | `12/0/12` |
| physical identity mapping RootToDevice/SourceMapping/VisibleMapping | `0/0/12` |
| physical variant lookups/misses/hits/stores | `12/12/0/0` |
| disabled paths | zero |
| writePixels accounting | consistent |

The target remained physical `1080x1920`, rowBytes `4320`, BGRA8888, alpha 2,
software. Runtime SHA-256 is
`62318f5b09172aa2518c1bd9dc2d0013c7fdff548af71cc85d95b8145d584ee2`;
Launcher SHA-256 is
`ef6f924f3beda71e6615badf94dd93d5dd167a332250aa22e6dc3855cbcf384a`;
bundle ZIP SHA-256 is
`6ee02bc362dcb7e8578044eab2b0834b0271c70ed73b1fac6330f251d87fcef5`;
result ZIP SHA-256 is
`8067a4a6b4b55a49afc49956a127b7b6cd7b65905b97376d52c8f893a1c80886`.

This remains structural evidence only. M4 reuse and RGB565/target-color
measurements remain gated and were not started.

## Final ULP-boundary correction

Implementation HEAD: `404424e3c`.
`integerDoubleValue()` now uses the local float spacing from adjacent
`nextafterf` values, capped at `1/1024` pixel. Native focused tests accept
`999.999971 -> 1000` and `1000.000029 -> 1000`, and reject `1000.01`,
`1000000.1`, and `1000000.4`.

The macOS ARM64 build passed for `tcvm`, `Launcher`, and
`skia_surface_test`. The final package directory is
`build/image-raster-policy-03-package-m3-final/image-scroll-benchmark-macos-arm64`.
Self-test passed with 663 JPEGs and corpus hash `588a7e0f4019424a`. The exact
structural matrix passed 10/10 processes for masks
`0,8192,16384,32768,57344`, prefetch on, accounting on, and two rounds.
Every process completed 189 frames and reached `scroll_end=39091`.

Final aggregate counters remain:

| counter | value |
|---|---|
| target color attempts/fallbacks/hits/materializations/acquisition sources | `12/12/0/0/12` |
| target mapping RootToDevice/SourceMapping/VisibleMapping | `0/0/0` |
| physical identity attempts/hits/fallbacks | `12/0/12` |
| physical identity mapping RootToDevice/SourceMapping/VisibleMapping | `0/0/12` |
| physical variant lookups/misses/hits/stores | `12/12/0/0` |
| disabled paths | zero |
| writePixels accounting | consistent |

Runtime SHA-256 is
`6b5a5d7590b6fdb862a0c1e213c277ba47508d7a4bca557ed9c24c518fe8b84a`;
result ZIP SHA-256 is
`41515e8d90d518c1bfaa7c4073aedb55e886b960043f70349e089dd7cd6d431f`;
bundle ZIP SHA-256 is
`f722781d007cd3c3dcb3c0e3848e5d7013b43fade85ec7ccde8b3d24f3e62f16`.
Logs: `/tmp/image-raster-m3-ulp-build.log`,
`/tmp/image-raster-m3-ulp-native-test.log`,
`/tmp/image-raster-m3-ulp-self-test.log`, and
`/tmp/image-raster-m3-ulp-structural-matrix.log`.

All M3 acceptance criteria pass. M4 remains gated and was not started.
