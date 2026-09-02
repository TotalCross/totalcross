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
- The corrective milestone is complete and this goal is fully stopped.
- Corrective commit titles passed the repository format check. The local
  body-line wrapping check flagged existing unwrapped body lines; those
  commits remain unchanged to honor the no-history-rewrite instruction.
- Plan 3 remains open before milestone 3. Resume later at the rotate/save
  milestone gate; do not restart the corrective milestone or begin plan 4.
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
- Active paths are the image backing SDK classes/tests/smokes, Skia backing
  sources and CMake source list, native bridge registration files, and the
  state/evidence/editorial files named below. Preserve unrelated local
  worktree files.
- Evidence is append-only in
  `.agent/evidence/image-native-backing-03.jsonl`; read it selectively for
  historical validation details. The editorial handoff is
  `.agent/reports/image-native-backing-03-editorial.md`.
- Deferred by roadmap scope: Android, iOS, Linux, Windows, full platform
  matrix, broad legacy lazy/ABI smokes, plan 3 milestone 3, plan 4 color
  migration, and plan 5 retirement.
- Resume command:
  `sed -n '1,220p' .agent/state/image-native-backing-03.md`.
