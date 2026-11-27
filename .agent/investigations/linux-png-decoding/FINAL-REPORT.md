<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Linux PNG Investigation Closure

## Outcome

The historical Linux ADLER32 decode failure and original I06 PNG drawing
failure are both **NOT_REPRODUCED** on clean master
`6beef491f28c63aa364d3661dd5c37ccff01fb0d`. Their root causes remain
undetermined. The hosted baseline ran without `LD_PRELOAD` in Actions run
`46061400453`.

The original I06 PNG drawing and Linux ownership validation are **PASS** with
the exact original 74-byte PNG fixture and no startup shim. The run completed
all seven drawing/application stages, produced exact red `0xff0000`, completed
two ownership cycles with 1,510 controls per cycle, and reported no ownership
mismatches.

Indexed palette-transparency PNG construction remains a separate reproducible
rejection. It is documented in a separate issue proposal and was not diagnosed
or changed in this branch.

## Evidence and Reproduction

The detailed baseline revisions, historical run identities, and artifact
digests are in [`evidence/index.md`](evidence/index.md). The self-contained
proposed update for issue #564 is
[`PROPOSED-ISSUE-564-UPDATE.md`](PROPOSED-ISSUE-564-UPDATE.md); the separate
indexed-PNG issue proposal is
[`PROPOSED-INDEXED-PNG-ISSUE.md`](PROPOSED-INDEXED-PNG-ISSUE.md). Neither was
published.

Reproduce the baseline from hosted run `46061400453` and Linux runtime artifact
`15228767611`, as documented in the issue #564 proposal. The reusable
repository-owned Linux cases are in
`TotalCrossSDK/src/test/resources/native-startup/png-regression/` and are
invoked by `.github/workflows/native-startup-regression.yml` through the
existing Linux runtime job.

The permanent probe covers:

- The historical 36×36 RGBA fixture through `Image(File)` and `Image(byte[])`,
  asserting dimensions, 1,296 pixels, and transparent-black pixel zero.
- The original 2×2 opaque-red I06 PNG, asserting dimensions, repeated drawing
  raster reuse, exact red output, and owner cleanup after detaching the control.

All original/control fixtures are retained in one test-resource directory and
verified against its `SHA256SUMS` file. No production image code changed.

## Closure Regression Run

- Actions run [46071207197](https://github.com/TotalCross/totalcross/actions/runs/46071207197)
  passed. Tested branch revision
  `394dbd7754bcd62bf4aa0cc56dca3ddec7753c88`, tree
  `5cb1a33249203bda172d2391a0a93cd782ee4d4a`, `dirty: false`.
- Runtime evidence artifact ID `15233640195`, SHA-256
  `6767a4f2faa89b8d72a7e8f160cdb87e12c98fa8a34cdf2f1bb9515ac1f086ee`.
  Linux build artifact ID `15232314511`, SHA-256
  `b5be7fe36948715d39c111d4e581adf1015bceb8cbeea1243bd8d2e5d15d7e57`.
  SDK build artifact ID `15233092536`, SHA-256
  `da2f00bfb37d5d15e56c80a020bcd0d97e81747a0f99a6af3f47a283a8da36fe`.
- Regression result: **PASS**, exit 0, no shim. All three markers passed:
  RGBA via file, RGBA via bytes, and original PNG drawing with exact red pixel
  `16711680` and zero owners after detaching its control. The existing five-case
  startup path matrix also passed. Other platforms were skipped.

## Validation and Scope

Local validation passed for Java 8 compilation, jar creation and deployment
against the canonical SDK/Linux artifacts, Python syntax, all seven fixture
checksums, copyright headers, Ruby YAML parsing, and `git diff --check`. The
hosted Linux workflow passed the deployed PNG and startup assertions. No local
native runtime was executed. Windows, macOS, Android, sanitizer, and benchmark
work was skipped because it is outside this closure and unnecessary for these
Linux-only assertions.

No decoder or dependency changes, symbol-isolation experiments, allocator
changes, historical bisecting, indexed-PNG diagnosis, GitHub issue edits, or PR
creation were performed.

## Commits

- Regression integration: `394dbd7754bcd62bf4aa0cc56dca3ddec7753c88`.
- The completed report, proposals, matrix, evidence index, and checkpoint are
  recorded in the closing documentation commit that follows the integration
  commit.
