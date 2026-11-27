<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Proposed Update to Issue #564

Draft for review only. This text has not been posted to GitHub.

## Current Result

The two historical Linux PNG failures are **NOT_REPRODUCED** on clean master
`6beef491f28c63aa364d3661dd5c37ccff01fb0d`. Their root causes remain
undetermined. No decoder, dependency, symbol, allocator, or historical-bisect
experiment was performed, and no production image code was changed.

The baseline was run without `LD_PRELOAD` in [hosted Actions run
46061400453](https://github.com/TotalCross/totalcross/actions/runs/46061400453).
Both the Linux runtime artifact and SDK artifact identify source commit
`6beef491f28c63aa364d3661dd5c37ccff01fb0d`, tree
`9ddcd0275dad076260e3a60169ab1da870351252`, and `dirty: false`. Their shared
build identity is `b044e6dca052ae68307006e83fbfb7672364c21f5cff393b889029a552548354`.

## Historical Observations

- The 136-byte RGBA8 fixture
  (`historical-rgba-36x36.png`, SHA-256
  `958903b3f880fbf626fa8fbba8f69856a989f5930531984be9585aac08a5aba6`)
  historically failed through both `Image(File)` and `Image(byte[])` with
  `IDAT: ADLER32 checksum mismatch`. The failure was recorded at source
  `61034a7562e7fa43df076dcc38bcd48128605395`.
- I02 run `45949849457` used harness commit
  `bfd2eee51be51e56b6c78ca4e27b6c927a4b8c1b` with frozen runtime source
  `6b915883dd202f6db891990f6c3af26fb645ecc7`. Its unshimmed app aborted at
  startup with exit 134 before reaching PNG cases. A test-only startup shim
  allowed the original RGBA file and byte-array cases to pass, but that result
  did not explain the historical checksum failure.
- The original I06 input is the 74-byte opaque-red PNG
  (`ownership-red-2x2.png`, SHA-256
  `d28c139bba718d60154960eaca75535a5cc5b59fe2153e998a29251e8e5f4667`). The
  original probe source is from performance-lab commit
  `d084949d106f9606a7639d851ad66b97734df1ec` (source SHA-256
  `fdecc98a576b23a5c9e68d64ed2eff2b68899b47fb54597e121711fa69bba117`). The
  historical I06 failure reached native drawing of this PNG before the
  ownership assertions switched to a JPEG control.
- Original I06 PNG failure run `45978158503` used probe commit
  `d084949d106f9606a7639d851ad66b97734df1ec`; its retained artifacts are
  `15187598928` and `15188152941`. Later I06 run `45989644203` used harness
  commit `65925255376e15820e3bdfa04ca3d090020f6d0e`; its Linux output artifact
  is `15193167516` (SHA-256
  `d13ecea7447310f6f8af596e93d963940a4482dc57757213d7447290d4ffb849`).
- Original I02 output artifact `15176521522` (SHA-256
  `d46e614e164b96a0511a7eb4609825563cf1239301afba0b1337ce9395828488`)
  retains the unshimmed startup abort and the separate conditional-shim result.

## Current Unshimmed Linux Results

- The 36×36 RGBA PNG constructed and decoded through both file/stream and byte
  array paths: 1,296 pixels, no exception. `pixel0=0` matches the fixture's
  transparent-black top-left sample.
- The original I06 drawing sequence completed all seven stages. It drew twice
  into a 4×4 image, returned exact red `drawPixelRgb=16711680`, completed two
  screen cycles and 1,510 controls per cycle, and reported zero ownership
  mismatches. The I06 Linux ownership validation is **PASS**, without a startup
  shim and with the original PNG fixture.
- Normal Linux startup/path controls passed 5/5. Each exited 0 and emitted
  `TC_STARTUP_COMPLETE` and its downstream JPEG control marker.
- The indexed palette-transparency PNG is still rejected during construction.
  That independent result is captured in a separate proposed issue and is not
  treated as a cause of either failure above.

## Permanent Regression Integration

The repository now keeps the original RGBA and I06 PNG fixtures under
`TotalCrossSDK/src/test/resources/native-startup/png-regression/fixtures/` and
runs focused deployed assertions through the existing Linux startup workflow.
Hosted run [46071207197](https://github.com/TotalCross/totalcross/actions/runs/46071207197)
passed on branch revision `394dbd7754bcd62bf4aa0cc56dca3ddec7753c88`, source
tree `5cb1a33249203bda172d2391a0a93cd782ee4d4a`, `dirty: false`. It used no
startup shim. Its exact results were:

```text
TC_LINUX_PNG case=historical-rgba-file,status=PASS,width=36,height=36,pixelCount=1296,pixel0=0
TC_LINUX_PNG case=historical-rgba-bytes,status=PASS,width=36,height=36,pixelCount=1296,pixel0=0
TC_LINUX_PNG case=original-ownership-drawing,status=PASS,width=2,height=2,drawPixelRgb=16711680,ownerCountAfterDetach=0
TC_LINUX_PNG_RESULT complete=true
```

The run's runtime artifact is ID `15233640195`, SHA-256
`6767a4f2faa89b8d72a7e8f160cdb87e12c98fa8a34cdf2f1bb9515ac1f086ee`.
The canonical Linux build artifact is ID `15232314511`, SHA-256
`b5be7fe36948715d39c111d4e581adf1015bceb8cbeea1243bd8d2e5d15d7e57`; the SDK
build artifact is ID `15233092536`, SHA-256
`da2f00bfb37d5d15e56c80a020bcd0d97e81747a0f99a6af3f47a283a8da36fe`. Both
builds report the same clean commit and source tree. The regression source SHA-256
is `602d6fc9368e3bd643ef4078ae5db15733bdd6d07561f1311e06fc28c84223dc`.

## Revisions and Artifact Identity

| Item | Identity |
| --- | --- |
| Baseline run head | `94917ff6d51f7dd442aacbec838717e5aef48fa4` |
| Linux runtime source | `6beef491f28c63aa364d3661dd5c37ccff01fb0d`, tree `9ddcd0275dad076260e3a60169ab1da870351252` |
| Permanent regression source | `394dbd7754bcd62bf4aa0cc56dca3ddec7753c88`, tree `5cb1a33249203bda172d2391a0a93cd782ee4d4a` |
| Runtime VM subtree | `15096565350eadd0c059df66d339f41b7ab71e93` |
| `libtcvm.so` SHA-256 | `04b248226d68630ab67764371d12e12a9e533e9a100af3c0d6620b0928236a3f` |
| Linux `Launcher` SHA-256 | `603440e2aa1cf9a651631d34aae8973d04216ad5e2e7b1326888d86ce67e5b37` |
| SDK jar SHA-256 | `970876eb5223c494b5b4a5465fb51a1be9fab4e19cab742b53d4f59301a52f5a` |
| Generated native bridge tree SHA-256 | `bc30e18437913444ee09baa1a53a0f4710e36a442774d60539e919e2d62b0762` |
| Linux runtime evidence artifact | ID `15228767611`, SHA-256 `2e61ef84ff7374613a96f91491e7cc6dc6bc65630117666b3d3ebf3ad0082882` |
| Linux build artifact | ID `15228663178`, SHA-256 `87cfabc9fd06d69a58d7e756519c526f860ba76a16c4e44bc733b672913122f9` |
| SDK build artifact | ID `15229440316`, SHA-256 `2bde7cbda4bbab3e4756ce0f7c8768bdc1d5fb01f36dacabcdb18704be12aa27` |
| Native bridges artifact | ID `15228393349`, SHA-256 `db6f063fd5ebfee6b29b2e3a7223aef72505a0762f13d8e1873ea01d284ef7dc` |
| Prepared native dependencies artifact | ID `15228477966`, SHA-256 `2a71e2235a04129aa28f7ea66e5f59e3e8ff5d2cff634ecc0090b45ea2b9a94c` |
| Historical I02 results artifact | ID `15176521522`, SHA-256 `d46e614e164b96a0511a7eb4609825563cf1239301afba0b1337ce9395828488` |
| Historical I06 control artifact | ID `15193167516`, SHA-256 `d13ecea7447310f6f8af596e93d963940a4482dc57757213d7447290d4ffb849` |

## Reproduction and Inspection

The retained output is the `native-startup-linux-runtime-94917ff6d51f7dd442aacbec838717e5aef48fa4`
artifact from run `46061400453`. Download and inspect it with:

```sh
gh api repos/TotalCross/totalcross/actions/artifacts/15228767611/zip > linux-runtime.zip
unzip -q linux-runtime.zip -d linux-runtime
jq -r '.observed.pngCases[]' linux-runtime/png-baseline/unmodified/result.json
jq . linux-runtime/png-baseline/I06/original-png/classification.json
jq . linux-runtime/png-baseline/I06/original-png/result.json
```

The permanent regression is in run `46071207197`, runtime artifact
`15233640195`. Download it and inspect the exact three focused cases with:

```sh
gh run download 46071207197 --repo TotalCross/totalcross \
  --name native-startup-linux-runtime-394dbd7754bcd62bf4aa0cc56dca3ddec7753c88 \
  --dir linux-runtime-regression
jq .pngRegression linux-runtime-regression/summary.json
```

For a fresh run, the retained workflow is `.github/workflows/native-startup-regression.yml`.
It checks out the canonical SDK/Linux artifacts, unsets `LD_PRELOAD`, verifies
the exact fixture hashes, runs both historical PNG scenarios, and uploads the
runtime logs and result JSON. The repository-owned reusable probe is
`TotalCrossSDK/src/test/resources/native-startup/png-regression/LinuxPngRegressionProbe.java`.

Original report references: [I02 Linux PNG report](https://github.com/TotalCross/totalcross-performance-lab/blob/investigation/totalcross-7.3.0-functional-validation/investigations/7.3.0-functional/reports/I02-linux-png.md)
and [I06 Linux ownership report](https://github.com/TotalCross/totalcross-performance-lab/blob/investigation/totalcross-7.3.0-functional-validation/investigations/7.3.0-functional/reports/I06-linux-ownership.md).

## Remaining Uncertainty

Neither historical failure reproduces on the tested clean master revision.
There is no supported causal conclusion or evidence that PR #568 fixed either
PNG symptom. The indexed-PNG constructor rejection is independent and has not
been diagnosed or changed in this branch.
