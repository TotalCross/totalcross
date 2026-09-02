<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image native backing plan 03 state

- Starting HEAD: `6cc978a0c`.
- Prior implementation checkpoint: `04a7bfa0a`; prior artifact
  checkpoints: `772806c85`, `033ccf5fb`, and `db1f0932a`.
- Corrective milestone commits:
  `94c719c1d`, `25678a445`, `34ab98ec5`, `a3eddc41f`, and
  `e5c8c35b8`. No prior commit was amended, rewritten, squashed, or
  rebased.
- Rotate/save milestone checkpoint: `87212e7ff`.
- Plan 3 is complete through milestones 1, 2, and 3. The roadmap boundary is
  now recorded and plan 4 has not been initiated.
- Corrective commit titles passed the repository format check. The local
  body-line wrapping check flagged existing unwrapped body lines; those
  commits remain unchanged to honor the no-history-rewrite instruction.
- Do not restart the corrective milestone or plan 3. A later roadmap resume
  may begin plan 4 after reviewing its own state and acceptance criteria.
- Corrective slice delivered:
  - `Image.lockChanges()` releases raster backing wrappers after clearing
    legacy pixel arrays while retaining native backing ownership.
  - Raster and native snapshot failures are classified as checked retryable
    materialization failures or invalid released-state failures, with an
    explicit native status bridge and deterministic native allocation hook.
  - Backing dimensions now describe physical storage, including full
    multi-frame strips; detached-source validation checks storage width and
    height while allowing zero visible frame width with a nonempty strip.
  - Skia geometry execution is isolated in
    `TotalCrossVM/src/nm/ui/skia/skia_image_geometry.cpp`; storage remains
    owned by `skia_image_backing.cpp` through the private internal header.
- Focused tests passed for backing ownership, invalid native snapshot state,
  storage dimensions, frame/crop/transform/lazy behavior, graphics scale, and
  native bridge registration.
- The corrective SDK test/dist gate passed:
  `/tmp/image-native-backing-03-corrective-gate-tests.log` and
  `/tmp/image-native-backing-03-corrective-gate-dist.log`.
- The first corrective macOS arm64 gate configure passed, while its native
  build initially failed on the new private-header helper collision. The
  follow-up commit `e5c8c35b8` fixed that compile issue; the incremental
  `tcvm`/Launcher build then passed at
  `/tmp/image-native-backing-03-corrective-split-fix-build.log`.
- Post-fix native smoke coverage passed:
  `ImageNativeMaterializationSmokeApp` covers retryable native snapshot
  allocation and the 500x500 to approximately 89x89 regression;
  `ImageDeferredCropFrameSmokeApp` covers multi-frame backing dimensions;
  `ImageNativeGeometrySmokeApp` covers native geometry execution. The
  staging artifact root is
  `/tmp/image-native-backing-03-corrective-smokes.ne2QXN`.
- Milestone 3 validation passed: focused SDK tests at
  `/tmp/image-native-backing-03-m3-sdk-tests.log`, smoke compilation at
  `/tmp/image-native-backing-03-m3-smoke-compile.log`, SDK distribution at
  `/tmp/image-native-backing-03-m3-dist.log`, macOS arm64 configure at
  `/tmp/image-native-backing-03-m3-cmake.log`, and native build at
  `/tmp/image-native-backing-03-m3-native-build.log`.
- The deployed rotate/save fixture passed with result
  `fixture=ImageNativeRotateSaveSmokeApp,pass=true`. Deployment, linkage, and
  run records are `/tmp/image-native-backing-03-m3-rotate-save-deploy.log`,
  `/tmp/image-native-backing-03-m3-rotate-save-linkage.log`, and
  `/tmp/image-native-backing-03-m3-rotate-save-run.log`; staged artifacts are
  under `/var/folders/k8/02b7wfkd7fn32vtm3t5mwxwr0000gn/T/image-native-backing-03-m3-smoke.XXXXXX.lPVdxX3KTK`.
- Active paths are the image backing SDK classes/tests/smokes, Skia backing
  sources and CMake source list, native bridge registration files, and the
  state/evidence/editorial files named below. Preserve unrelated local
  worktree files.
- Evidence is append-only in
  `.agent/evidence/image-native-backing-03.jsonl`; read it selectively for
  historical validation details. The editorial handoff is
  `.agent/reports/image-native-backing-03-editorial.md`.
- Deferred by roadmap scope: Android, iOS, Linux, Windows, full platform
  matrix, broad legacy lazy/ABI smokes, plan 4 color migration, and plan 5
  retirement. Plan 4 is the next later resume point.
- Resume command:
  `sed -n '1,220p' .agent/state/image-native-backing-03.md`.
