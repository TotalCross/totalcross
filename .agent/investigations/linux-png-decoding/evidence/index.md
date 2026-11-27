<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Evidence Index

Closure summary: [`FINAL-REPORT.md`](../FINAL-REPORT.md). The issue texts are
local drafts only; neither GitHub issue was modified.

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

`TotalCrossSDK/src/test/resources/native-startup/png-regression/fixtures/` is
self-contained. Its `SHA256SUMS` file covers every binary input.
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

### Run and Build Identity

- Actions run: [46061400453](https://github.com/TotalCross/totalcross/actions/runs/46061400453),
  push head `94917ff6d51f7dd442aacbec838717e5aef48fa4`; conclusion: success.
- Source selector job `140716278474` passed. Linux x86_64 runtime job
  `140717307446` passed. Canonical Linux build job `140716670628` and SDK build
  job `140716489405` passed.
- Both `linux-amd64/build-provenance.json` and the SDK package's provenance
  record source commit `6beef491f28c63aa364d3661dd5c37ccff01fb0d`, tree
  `9ddcd0275dad076260e3a60169ab1da870351252`, `dirty: false`, and build ID
  `b044e6dca052ae68307006e83fbfb7672364c21f5cff393b889029a552548354`.
- Runtime artifact identity: `libtcvm.so` SHA-256
  `04b248226d68630ab67764371d12e12a9e533e9a100af3c0d6620b0928236a3f`;
  Linux `Launcher` SHA-256
  `603440e2aa1cf9a651631d34aae8973d04216ad5e2e7b1326888d86ce67e5b37`;
  SDK jar SHA-256
  `970876eb5223c494b5b4a5465fb51a1be9fab4e19cab742b53d4f59301a52f5a`.
- Runtime source VM subtree is
  `15096565350eadd0c059df66d339f41b7ab71e93`; generated native-bridge tree
  digest is `bc30e18437913444ee09baa1a53a0f4710e36a442774d60539e919e2d62b0762`.
- The exact original I06 probe source is from PL commit
  `d084949d106f9606a7639d851ad66b97734df1ec`; the ownership fixture hash is
  listed above and the workflow verified the embedded array byte-for-byte.

| Artifact | ID | SHA-256 digest | Size (bytes) |
| --- | ---: | --- | ---: |
| Linux runtime evidence | `15228767611` | `2e61ef84ff7374613a96f91491e7cc6dc6bc65630117666b3d3ebf3ad0082882` | 40,367,408 |
| Linux build | `15228663178` | `87cfabc9fd06d69a58d7e756519c526f860ba76a16c4e44bc733b672913122f9` | 7,612,645 |
| SDK build | `15229440316` | `2bde7cbda4bbab3e4756ce0f7c8768bdc1d5fb01f36dacabcdb18704be12aa27` | 65,460,408 |
| Generated native bridges | `15228393349` | `db6f063fd5ebfee6b29b2e3a7223aef72505a0762f13d8e1873ea01d284ef7dc` | 24,840 |
| Prepared native dependencies | `15228477966` | `2a71e2235a04129aa28f7ea66e5f59e3e8ff5d2cff634ecc0090b45ea2b9a94c` | 79,957,924 |
| Linux helper contract | `15228907454` | `6e7571c74ce4b9c0ebe8f828fdb2b316d149f2eab3bddeebf123158d104fa7fc` | 865 |

### Startup and PNG Results

- No startup shim was used. Both diagnostic steps unset `LD_PRELOAD`; I02's
  result records `runtimeShim: null`.
- The PR #568 native startup matrix passed all five cases (normal, short,
  bounded-long, symlink, and relative path): each exited 0, emitted
  `TC_STARTUP_COMPLETE`, and passed the downstream JPEG control.
- I02 exited 0 in 0.138 seconds and emitted its completion marker with all
  nine cases. Exact output:

```text
TC_PNG case=png-file,construct=true,decode=true,width=36,height=36,pixelCount=1296,pixel0=0,expectedBehavior=decode,exception=none
TC_PNG case=png-bytes,construct=true,decode=true,width=36,height=36,pixelCount=1296,pixel0=0,expectedBehavior=decode,exception=none
TC_PNG case=tcz-png,construct=true,decode=true,width=2,height=2,pixelCount=4,pixel0=80ff0000,expectedBehavior=decode,exception=none
TC_PNG case=alpha-png,construct=true,decode=true,width=2,height=2,pixelCount=4,pixel0=80ff0000,expectedBehavior=decode,exception=none
TC_PNG case=indexed-png,construct=false,decode=false,width=-1,height=-1,pixelCount=-1,pixel0=none,expectedBehavior=decode,exception=totalcross.ui.image.ImageException:Invalid or unsupported encoded image
TC_PNG case=jpeg-file,construct=true,decode=true,width=512,height=512,pixelCount=262144,pixel0=ff9e683a,expectedBehavior=decode,exception=none
TC_PNG case=jpeg-bytes,construct=true,decode=true,width=512,height=512,pixelCount=262144,pixel0=ff9e683a,expectedBehavior=decode,exception=none
TC_PNG case=invalid-file,construct=false,decode=false,width=-1,height=-1,pixelCount=-1,pixel0=none,expectedBehavior=reject,exception=totalcross.ui.image.ImageException:Invalid or unsupported encoded image
TC_PNG case=invalid-bytes,construct=false,decode=false,width=-1,height=-1,pixelCount=-1,pixel0=none,expectedBehavior=reject,exception=totalcross.ui.image.ImageException:Invalid or unsupported encoded image
```

The I02 RGBA top-left pixel value `0` matches the fixture's transparent-black
reference sample. The indexed image is still rejected during construction; keep
that result separate from the RGBA and drawing findings.

### Original I06 Drawing Sequence

- Classification: `NOT_REPRODUCED`; tool exit code 0, startup marker present,
  stage 3 entered, no failure marker, and all seven stages completed.
- `drawPixelRgb` was `16711680` (`0xff0000`). The original PNG decoded and
  rendered, and the complete run reported 1,510 controls and distinct images
  per cycle, two screen cycles, zero ownership mismatches, and `status: PASS`.
- I06 prepared the SDK and Linux VM from the same clean master commit/tree and
  recorded the SDK jar, launcher, VM library, and native-bridge identities
  above. The run used no startup shim.
- The canonical workflow also ran its existing macOS ARM64 build and deployed
  startup smoke job `140717307484`; it passed. It was an existing workflow
  side-effect and is not used to classify the Linux findings.

## Closure Regression Run

- Actions run: [46071207197](https://github.com/TotalCross/totalcross/actions/runs/46071207197),
  branch head `394dbd7754bcd62bf4aa0cc56dca3ddec7753c88`; conclusion: success.
- Source selector job `140748310981`, canonical Linux build job
  `140748755381`, SDK build job `140748491226`, and Linux runtime job
  `140749268210` passed. The helper, Windows, macOS, Android, iOS, and disabled
  Linux ARM jobs did not run. No cross-platform build ran.
- Linux runtime and SDK provenance both identify clean source commit
  `394dbd7754bcd62bf4aa0cc56dca3ddec7753c88`, tree
  `5cb1a33249203bda172d2391a0a93cd782ee4d4a`, `dirty: false`.
- Runtime artifact `native-startup-linux-runtime-394dbd7754bcd62bf4aa0cc56dca3ddec7753c88`:
  ID `15233640195`, SHA-256
  `6767a4f2faa89b8d72a7e8f160cdb87e12c98fa8a34cdf2f1bb9515ac1f086ee`,
  30,120,574 bytes.
- Linux build artifact ID `15232314511`, SHA-256
  `b5be7fe36948715d39c111d4e581adf1015bceb8cbeea1243bd8d2e5d15d7e57`,
  7,612,635 bytes. SDK build artifact ID `15233092536`, SHA-256
  `da2f00bfb37d5d15e56c80a020bcd0d97e81747a0f99a6af3f47a283a8da36fe`,
  65,461,238 bytes.
- Generated native bridges artifact ID `15232359174`, SHA-256
  `292a630ce62a8a170df26be4a6e3ae22cf25a2cdf69c04df629eab10ed95c5b0`;
  prepared native dependencies artifact ID `15232688322`, SHA-256
  `1592c26ab10197ee334bf390f0c90d05df8e3e4f877d62567a2fa35de49b9d39`.
- Runtime `libtcvm.so` SHA-256 is
  `27e42a2987e8a81b62ad2a7bb123a1670e9f9e1b2d3a36d3a8d5d3dbeb6c68ea`;
  Linux `Launcher` SHA-256 is
  `f638b0ff270906a6c703dc84983f27d173083f7c22bd48296ac8016efc2a3a09`.
- `pngRegression.status` is `PASS`, process exit 0, `runtimeShim: null`. Its
  Java source SHA-256 is
  `602d6fc9368e3bd643ef4078ae5db15733bdd6d07561f1311e06fc28c84223dc`.
  Exact markers show both 36×36 RGBA paths passed with 1,296 pixels and
  `pixel0=0`; original PNG drawing passed with 2×2 dimensions, exact red
  `16711680`, and owner count 0 after detach. The generated render PNG digest
  is `961193a64a23a06ebe62da397919e4e04d752c4f1a6fa31c4e7170fd70d365b5`.
- All seven fixture hashes were verified. The original RGBA and original I06
  fixture digests match the baseline record above.

Reproduce the permanent cases by downloading runtime artifact `15233640195`
from run `46071207197` and reading `summary.json` and
`png-regression/result.json`. The focused runtime log contains the three
`TC_LINUX_PNG` pass markers and `TC_LINUX_PNG_RESULT complete=true`.
