<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Image Compact Storage — Implementation Report

## Summary

COMPACT is an opt-in runtime storage profile for immutable encoded image
sources. A supported native Skia runtime stores eligible sources in RGB565,
GRAY8, or premultiplied ARGB4444. Public pixel behavior remains ARGB/RGBA;
mutable destinations use RGBA8888 and compact sources promote before mutation.

The macOS ARM64 smoke verified source selection, byte accounting, observer and
drawing parity, retryable decode and promotion failures, and P2/P3 lifecycle
behavior. It reported zero full-frame RGBA decode staging for the supported
fixtures. The measurements below describe the 8x6 deterministic fixtures and
are not application-wide memory or throughput results.

The compact smoke entry point now contains only scenario orchestration and
result reporting. Fixture loading, independent pixel models, and storage
accounting live in `ImageCompactStorageSmokeSupport`; compact-source P3 cases
live in `ImageCompactStorageP3SmokeSupport`. A separate STANDARD-profile smoke
decodes color JPEG, color PNG, and alpha PNG and checks each backing is
RGBA8888 with zero compact decodes or compact bytes.
The STANDARD smoke also reads all three pixel arrays. JPEG parity uses the
compact smoke's 24-channel maximum-error bound; opaque PNG parity uses its
9-channel bound; alpha PNG pixels and black/white compositing match the
full-precision fixture model within one channel value. Observed maximum
channel errors were 19 for JPEG, 0 for opaque PNG, and 0 for alpha PNG; alpha
compositing error was 0.

## Runtime policy

`ImageRuntimeConfigurationStartup` resolves the requested profile after the
graphics backend is finalized. It enables COMPACT only when the native Skia
capability probe can create Gray8, RGB565, and ARGB4444 raster surfaces. If
those formats are unavailable, JavaSE/simulator paths, and non-Skia runtimes
resolve to STANDARD with the reason `native compact backing is unavailable`.
Applications still select only STANDARD or COMPACT; internal pixel formats
are not public configuration.

The package bridge used by startup is internal and excluded from public SDK
artifacts. Native decoders read the resolved policy, so format selection does
not repeat platform checks.

## Internal formats and precedence

Selection uses encoded structure and does not scan decoded pixels. Gray
structure takes precedence over general opaque color; alpha and tRNS take
precedence over both.

| Encoded source structure | Internal format | Bytes per pixel |
| --- | --- | ---: |
| Opaque grayscale JPEG or PNG | GRAY8 | 1 |
| Opaque color JPEG or PNG | RGB565 | 2 |
| PNG with alpha or tRNS | ARGB4444, premultiplied | 2 |
| Unsupported or unsafe compact decode | RGBA8888 fallback | 4 |

JPEG component metadata identifies grayscale versus color; JPEG is opaque.
PNG color type, alpha, and tRNS metadata determine eligibility. A tRNS source
uses ARGB4444 even when its decoded pixels happen to be opaque. Opacity-analysis
cache success does not change the chosen format.

## Direct compact decode

Supported JPEG and PNG rows are written into one final Skia-backed candidate.
The candidate remains decoder-owned until row decoding and snapshot completion
succeed; only then is it installed in the Image and shared with the encoded
source. Failures release the unpublished candidate and leave decode state
retryable. Unsupported paths use RGBA8888.

The candidate-failure hook runs after allocation for both PNG and JPEG. The
smoke verified one release for each injected failure and successful compact
materialization through the decoder fallback. The PNG loader also treats a
stored callback error as failure even if libpng finishes delivering rows from
the current input buffer.

## Observer behavior

`getPixels`, bounded and row reads, PNG encoding, equality/hash, and drawing
observe compact pixels without changing canonical storage. Native
`getPixels()` data remains detached. Conversion readback uses row-sized RGBA
output. Draw can sample compact Skia storage directly; target-color or physical
conversion uses derived variants and does not replace the source backing.

The smoke checked repeated reads, row channel order, PNG encoding, equality and
hash, drawing onto initialized targets, and unchanged compact formats after
those operations. RGB and alpha draw checks had maximum channel errors of 1 and
0 respectively.

## Transactional promotion

Before a pixel-writing operation, a compact source promotes to RGBA8888. The
native backing publishes the writable surface only after allocation and copy
succeed. Failure preserves compact format and pixels for retry. A successful
promotion advances mutation generation, preserves opacity state, and clears
P3 variant state. Subsequent mutation stays RGBA8888.

The smoke injected one promotion failure and verified that format, pixels,
generation, opacity, and variant state remained unchanged. Retry promoted a
96-byte ARGB4444 backing to 192-byte RGBA8888, preserved pixel readback, and
cleared variants. Deferred mutators and `getGraphics()` also promoted before
writing.

## P2/P3 integration

P2 backing ownership remains authoritative for mutation generation and opacity.
Mutation of an image sharing an encoded-source backing detaches the mutable
image; the cached source sibling stays compact. Failed promotion does not
advance generation or change opacity. Successful promotion increments
generation once.

P3 physical identity, exact keys, target-color conversion, and one-slot,
one-pending variant ownership remain unchanged. Promotion invalidates both
cached and pending variants. Derived render targets remain RGBA8888/BGRA8888;
an incompatible compact physical identity uses the existing draw fallback.
The compact-source smoke separately exercised physical-variant pending,
materialization, and reuse while retaining the RGB565 source; target-color
variant creation and reuse with the same source retention; and an incompatible
GRAY8-to-RGB565 physical path that fell back with matching output and no source
promotion. Both target-color and incompatible-path cases retained the original
source backing identity, mutation generation, opacity state, and compact
format; their pipeline roots also retained identity and compact format across
P3 work. Promotion also cleared established cached and pending P3 state. The
raster-core and deferred-mutation smokes passed their identity, cache, mutation,
and target-color checks.

## Diagnostics/accounting

Accounting is test-gated and does not add public diagnostic IDs or domains. The
8x6 macOS ARM64 smoke reads actual Skia rowBytes and backing sizes and reports
live/peak retained bytes by format, direct compact decode counts/bytes,
compact readbacks, tracked row scratch, full-frame RGBA decode temporary bytes,
and promotion attempts/results/bytes.

The test counters sum `rowBytes * height` for live backing records. They are a
per-handle backing estimate; handles can share immutable Skia storage, so these
totals are not process RSS or unique physical allocation totals. Row scratch
counts decoder output-row staging used by the exercised paths.

## Compatibility

Public Image and profile APIs do not expose storage formats. Mutable images,
derived destinations, and unsupported decodes remain RGBA8888. Existing
JavaSE/simulator behavior falls back to STANDARD when native compact capability
is absent. The package-private capability bridge is excluded from public
artifacts, and artifact-boundary validation passed.

## Validation

- `./gradlew-agent test -PruntimeDiagnostics=true --tests ...` passed the
  focused Image backing, raster-core, lazy materialization, deferred-transform,
  decode-policy, deferred-mutation, lazy-JPEG, async-preparation, preparation
  scheduler, scrolling preparation, runtime-configuration, and diagnostics
  test classes.
- `./gradlew-agent artifactContentTest dist -x test` passed with diagnostics
  disabled.
- `cmake -S TotalCrossVM -B build-p4-macos-native -DTC_ENABLE_RUNTIME_DIAGNOSTICS=ON -DCMAKE_BUILD_TYPE=Release -G Ninja` and
  `ninja -C build-p4-macos-native` passed for macOS ARM64 Release, including
  `tcvm`, `Launcher`, and `skia_surface_test` with the Q/P8 diagnostics bridge
  enabled.
- `./build-p4-macos-native/skia_surface_test` passed.
- `runRuntimeConfigurationImageCompactStorageMacOSSmoke` and
  `runRuntimeConfigurationImageStandardStorageMacOSSmoke` passed. The compact
  smoke included all policy, format, observer, quality, promotion,
  failure/retry, accounting, and compact-source P3 assertions. The STANDARD
  smoke passed its JPEG/PNG/alpha-PNG RGBA8888 checks and zero compact-decode
  assertions.
- `runRuntimeDiagnosticsSmokeMacOS`, `runFlickPacingDiagnosticsSmokeMacOS`,
  and `runImageAsyncPreparationMacOS` passed with diagnostics enabled.
- Runtime policy, raster-core, lazy-JPEG, and deferred-color-mutation macOS
  smokes passed.
- Copyright-header validation passed for the changed first-party files, and
  `git diff --check` passed.

Android, Windows, Linux, WinCE, and iOS were not built locally. Cross-platform
compatibility is left to the repository's enabled CI jobs.

## Memory/performance evidence

Actual backing sizes for the 8x6 fixtures:

| Format | rowBytes | Bytes per pixel | Backing bytes |
| --- | ---: | ---: | ---: |
| RGBA8888 | 32 | 4 | 192 |
| RGB565 | 16 | 2 | 96 |
| GRAY8 | 8 | 1 | 48 |
| ARGB4444 | 16 | 2 | 96 |

In this smoke run, live/peak retained bytes were RGBA8888
1152/1152, RGB565 672/768, GRAY8 240/288, and ARGB4444 480/576. Seven
deterministic fixture instances plus three mutation-source instances produced
10 compact decodes: four RGB565 (384 bytes), three GRAY8 (144 bytes), and three
ARGB4444 (288 bytes). The smoke counted 87 compact readbacks, 32 bytes of peak
decoder output-row scratch, and 0 bytes of full-frame RGBA decode temporaries.

Promotion accounting was four attempts, three successes, one injected failure,
and 576 bytes of promoted RGBA storage. One 96-to-192-byte promotion measured
0 ms at the smoke timer's millisecond resolution; this is only a small storage
and correctness check, not a timing claim.

The RGB565 readback exactly matched the independent rounded-pack/bit-replicate
reference (maximum error 0). RGB PNG source error was at most 4, with channel
RMSE 2.13. JPEG source error was at most 21; grayscale JPEG error was at most
8 and grayscale PNG was exact. The independent premultiplied ARGB4444 model
had maximum black/white composite error 0; comparison to full-precision source
compositing had maximum error 16. Transparent hidden RGB read back as zero,
and alpha drawing had maximum error 0.

## Known limitations

- Native compact allocation and quality were exercised locally on macOS ARM64
  RASTER with the pinned Skia dependency; other platforms rely on enabled CI.
- Only the deterministic 8x6 fixture family was used for current memory and
  quality evidence. It does not establish behavior for every image profile or
  workload.
- Retained-byte sums count backing records and can count shared immutable
  storage more than once. They do not measure process RSS or transient Skia
  allocator overhead.
- ARGB4444 is intentionally lossy; the reported bounds are specific to these
  alpha-gradient and tRNS fixtures.
- No FPS or application-level throughput improvement is claimed.
