<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Proposed Issue: Rejecting Valid Indexed PNG With Palette Transparency

Draft for a separate issue only. It has not been posted, and this branch does
not investigate or correct the behavior.

## Reproduction

Use `TotalCrossSDK/src/test/resources/native-startup/png-regression/fixtures/indexed-transparent-2x2.png`.
The exact fixture is 107 bytes with SHA-256
`0823539a40acde890b3a769c7c48490e5bf083da51c78898fdbea67c8119c10e`.
The file's chunk CRCs are valid.

On clean master `6beef491f28c63aa364d3661dd5c37ccff01fb0d`, hosted Linux run
`46061400453` recorded:

```text
TC_PNG case=indexed-png,construct=false,decode=false,width=-1,height=-1,pixelCount=-1,pixel0=none,expectedBehavior=decode,exception=totalcross.ui.image.ImageException:Invalid or unsupported encoded image
```

The I02 process completed normally without `LD_PRELOAD`. Its indexed case
failed while constructing `Image(File)`, before dimensions or pixels could be
read. There is no observed runtime pixel array for this case.

## Validated Structure and Reference Pixels

This is a 2×2, 8-bit indexed PNG (PNG color type 3):

| Chunk | Length | Contents |
| --- | ---: | --- |
| IHDR | 13 | 2×2, bit depth 8, color type 3 |
| PLTE | 9 | RGB entries: red, green, blue |
| tRNS | 3 | Per-entry alpha: 255, 0, 128 |
| IDAT | 14 | Inflates to 6 scanline bytes (filter byte plus two indices per row) |
| IEND | 0 | Valid end marker |

The decoded palette indices are `[[0, 1], [2, 0]]`. A host reference decoder
reports these expected RGBA pixels:

| Coordinate | Palette index | Expected RGBA |
| --- | ---: | --- |
| (0, 0) | 0 | (255, 0, 0, 255) opaque red |
| (1, 0) | 1 | (0, 255, 0, 0) transparent green |
| (0, 1) | 2 | (0, 0, 255, 128) half-transparent blue |
| (1, 1) | 0 | (255, 0, 0, 255) opaque red |

These are fixture reference values, not TotalCross-observed pixels.

## Relevant Boundary and Unknowns

`Image(Stream)` routes through `EncodedImageSource.fromStream` during
construction. The deployed capture method is replaced by native code; the Java
fallback uses `ImageEncodedStructure.inspect` to inspect the encoded structure
without inflating image data. The current source-level fallback permits indexed
PNG with a palette and transparency chunk, while the deployed I02 case rejects
the fixture at construction. This establishes a constructor/capture boundary
before pixel materialization, but does not identify the exact native rejecting
check or root cause.

The behavior is separate from the non-reproduced historical RGBA ADLER32 and
I06 drawing failures. Keep the indexed fixture as a future regression input if
the issue is accepted; do not fold it into their fix without new evidence and
review.

## Reproduction Instructions

Open [hosted run 46061400453](https://github.com/TotalCross/totalcross/actions/runs/46061400453)
and inspect the Linux runtime artifact `15228767611` as described in
`PROPOSED-ISSUE-564-UPDATE.md`. The captured output line above is from
`png-baseline/unmodified/result.json`. The fixture, SHA-256 list, PNG chunk
CRCs, decoded scanline length, indices, palette, and transparency values are
retained under
`TotalCrossSDK/src/test/resources/native-startup/png-regression/fixtures/`.
