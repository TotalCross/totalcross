<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Evidence Index

## Baseline and Source Revisions

- Selected `TotalCross/totalcross` master: `6beef491f28c63aa364d3661dd5c37ccff01fb0d`.
- PR #568 merge: `6beef491f28c63aa364d3661dd5c37ccff01fb0d`; ancestor check
  passed. Its Linux startup workflow is
  `.github/workflows/native-startup-regression.yml`.
- I02 report:
  [I02-linux-png.md](https://github.com/TotalCross/totalcross-performance-lab/blob/investigation/totalcross-7.3.0-functional-validation/investigations/7.3.0-functional/reports/I02-linux-png.md).
- I06 report:
  [I06-linux-ownership.md](https://github.com/TotalCross/totalcross-performance-lab/blob/investigation/totalcross-7.3.0-functional-validation/investigations/7.3.0-functional/reports/I06-linux-ownership.md).
- Existing I02 run: `45949849457`, harness commit
  `bfd2eee51be51e56b6c78ca4e27b6c927a4b8c1b`; its 49,881-byte result artifact
  is `15176521522` (SHA-256
  `d46e614e164b96a0511a7eb4609825563cf1239301afba0b1337ce9395828488`).
- Existing I06 PNG failure run: `45978158503`, probe commit
  `d084949d106f9606a7639d851ad66b97734df1ec`; original-PNG run artifacts are
  `15187598928` and `15188152941`.
- Existing I06 JPEG-control run: `45989644203`, harness commit
  `65925255376e15820e3bdfa04ca3d090020f6d0e`; Linux result artifact
  `15193167516` (SHA-256
  `d13ecea7447310f6f8af596e93d963940a4482dc57757213d7447290d4ffb849`).
- The baseline workflow reuses the pinned harness commit
  `6ce6c8fa0b2bbd8fdb6ac81a423ab332f9342483`; its I02 source and fixture paths
  match the corresponding paths from run commit `bfd2eee...` byte-for-byte.

## Retained Fixtures

`fixtures/` is self-contained. The `SHA256SUMS` file covers every binary input.
`manifest.json` records PNG dimensions, bit depth, color type, chunks and CRCs,
inflated scanline lengths, sample pixels, alpha, and indexed palette
transparency. Inputs were read with Pillow 13.0.0 as a host-only reference
decoder; no TotalCross native runtime ran locally.

| Fixture | SHA-256 | Identity |
| --- | --- | --- |
| `historical-rgba-36x36.png` | `958903b3f880fbf626fa8fbba8f69856a989f5930531984be9585aac08a5aba6` | 136-byte RGBA8, 36×36 |
| `ownership-red-2x2.png` | `d28c139bba718d60154960eaca75535a5cc5b59fe2153e998a29251e8e5f4667` | Original 74-byte opaque red RGBA8 PNG extracted from the I06 source array |
| `indexed-transparent-2x2.png` | `0823539a40acde890b3a769c7c48490e5bf083da51c78898fdbea67c8119c10e` | Indexed8, PLTE 3 entries, tRNS 3 values |
| `alpha-2x2.png` | `79938a708543f85c5c04bd15747a1c0314c7cbfca0ae0be09f69b4e4f5b6df3e` | RGBA8 alpha values 128, 255, 0, 255 |
| `tcz-2x2.png` | same as `alpha-2x2.png` | Existing TCZ PNG fixture |
| `known-good-512x512.jpg` | `df0035534c5c12d024763dce5f515023ad7e16139f4278b360291b2f0261fb51` | JPEG positive control |
| `invalid.bin` | `71933a2f8897a278956c0cae49de3f2a1d9a2e7ea5b6f7daabc2f7e83a4e3131` | Invalid encoded-image control |

## Historical Runtime Evidence

- I02's unmodified frozen-master run exited 134 at startup and produced no PNG
  cases. Its conditional shim run exited 0 and completed nine cases: historical
  RGBA file and bytes passed, TCZ and alpha PNG passed, indexed PNG construction
  failed, JPEG file and bytes passed, and invalid inputs were rejected.
- I06's original source embeds a 2×2 PNG in
  `tests/functional-validation/totalcross/ui/image/ImageOwnershipLifecycleProbe.java`
  at `d084949d106f9606a7639d851ad66b97734df1ec` (source SHA-256
  `fdecc98a576b23a5c9e68d64ed2eff2b68899b47fb54597e121711fa69bba117`). Its
  stage-3 sequence constructs the PNG, attaches an ImageControl, resolves the
  drawing raster twice, draws twice to an Image graphics context, checks the
  red output pixel, and writes a PNG. The app exits on a native failure before
  proceeding to the ownership stages.
- Full outputs remain available in the cited Actions artifacts, but the branch
  retains fixture bytes, identities, probe revisions, and reproduction steps so
  conclusions do not depend on artifact retention.

## Current-Master Hosted Run

Pending. Record the Actions run ID, job ID, tested source revision, artifact IDs
and digests, startup markers, process status, each PNG result line, and I06
stage/failure markers here after completion.
