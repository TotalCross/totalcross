<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image native backing plan 03 corrective checkpoint

Plan 3 milestones 1 and 2 remain complete at implementation checkpoint
`04a7bfa0a`. The corrective milestone requested for the image-native
backing handoff is complete, and execution is fully stopped before milestone
3.

Five focused commits preserve the existing history:

- `94c719c1d fix(sdk): release raster backing when locking image`
- `25678a445 fix(sdk,vm): preserve retryable backing snapshot failures`
- `34ab98ec5 fix(sdk): normalize image backing storage dimensions`
- `a3eddc41f refactor(vm): split Skia image geometry execution`
- `e5c8c35b8 fix(vm): compile split Skia backing sources`

The fixes release raster ownership after `lockChanges()`, retain native
backings, preserve retryable raster/native snapshot failures, normalize
physical storage dimensions, validate detached source metadata, and isolate
Skia geometry execution from backing storage.

The focused SDK test and distribution gate passed. The first macOS arm64
native gate configure passed but the build found a private helper collision
introduced by the split; the follow-up VM fix was validated by an incremental
`tcvm`/Launcher build. The deployed materialization, crop/frame, and
geometry smokes then passed. Together they cover snapshot retry, multi-frame
backing dimensions, native geometry, and the 500x500 to approximately 89x89
regression.

The evidence index records commands, logs, artifacts, the initial gate
failure, and its resolution. Android, iOS, Linux, Windows, the full platform
matrix, broad legacy compatibility smokes, plan 4, and plan 5 remain deferred.

The rotate/save milestone is now complete at `87212e7ff`. It adds the focused
SDK PNG-barrier regression and a deployed macOS arm64 fixture that compares
direct native draw pixels with the PNG round trip after deferred rotation and
scaling. The milestone gate and fixture passed; exact logs and the staging
artifact path are recorded in the evidence index and state file. Plan 3 is
closed, and the next later resume point is plan 4; no plan-4 implementation
was started here.
