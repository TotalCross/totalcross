<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Pre-image optimization master 01 correctness handoff

Plan 1 is complete on `fix/pre-image-optimization-master` at final handoff
HEAD `221c259ab`; the implementation commit is `061a1dde9`.

BASE_SHA is `1898014784b2fba5716cc033e49520740b05f0dd`. The repaired invariant
is: targeted JPEG physical dimensions remain decoder output, logical dimensions
remain encoded-source logical dimensions, and `contentScale` equals
`1.0 / decodedDenominator()` for denominators 1, 2, 4, and 8. Fresh targeted
decode and cached backing reuse now expose equivalent metadata.

Regression coverage includes denominator 2/4/8, plain and hardware-alias
smooth scaling, smooth plus alpha, crop plus smooth, chained smooth scaling,
cached reuse, direct draw, materialization/readback, odd dimensions and
boundary transitions, plus explicit full-decode checks for nearest and rotate
scale. The independent visual reference forces a separate full JPEG decode
before applying the same smooth transformation. Explicit JPEG factories remain
eager.

Validation passed: focused `totalcross.ui.image.*` SDK tests, SDK distribution
without tests, fresh Release macOS software-Skia CMake/Ninja build, exact-dylib
`runImageJpegPinchSmokeMacOS`, focused copyright headers, `git diff --check`,
and commit-message validation. The runtime artifact used by the smoke was
`build-preopt-macos/libtcvm.dylib` with SHA-256
`eaf8367f8f91499ec93be98474224d0c7aedd9f1d0775abd3e2eeca282e87a85`.

Plan 2 must start from exact `PLAN1_HEAD=221c259ab` and remains responsible for
native `USE_NATIVE_SWAP` benchmarking and the
remaining optimization work. Android, iOS, Windows, Linux, packaging, and
unrelated local `.agent` artifacts were intentionally left out of scope.
