<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# PNG prefetch evidence index

Append concise validation records here. Full logs and generated artifacts stay
outside Git; record only stable paths and relevant result summaries.

| Timestamp (UTC) | Revision | Slice | Validation | Result | Artifact / limitation |
| --- | --- | --- | --- | --- | --- |
| 2026-09-24 | `86d470c64b7dfc0ab6ac24314f32f0d630ff1990` | Activation | `git cat-file -e` and `git merge-base --is-ancestor` | Passed; immutable base present and current branch descended from it | Created `feat/png-prefetch` at the immutable base; no validation log needed |
| 2026-09-24 23:07:50 | `4549dadd24d1c808908c89f2aa8908a71bf4f441` | Plan activation | Signed `docs(plan): define png prefetch execution`; focused commit-message check | Commit created; message check failed because literal `\n` made a body line exceed 80 characters | Preserved per no-rewrite policy; subsequent messages will be checked before commit |
| 2026-09-24 23:11:54 | `efc679d88a8f09999304224bd5642f4122d6d505` | Milestone 1 | `cd TotalCrossSDK && ./gradlew-agent test --tests totalcross.ui.image.ImagePreparationTest`; focused headers; scoped diff check | Passed: 26 tests, 0 failures/errors/skips; headers checked 5 files with 0 changes; whitespace clean | Final full/agent logs: `TotalCrossSDK/agent-logs/20260924-201154-test-full.log` and `TotalCrossSDK/agent-logs/20260924-201154-test-agent.log`; earlier attempts exposed only incorrect test assumptions and were corrected |
| 2026-09-24 23:36:42 | `e391aaf11dff` | Milestone 2 | Focused benchmark tests; `py_compile`; `bash -n`; header and staged whitespace checks | Passed; benchmark matrix and contract tests passed; Windows runner static contract and size checks passed | Test log `/tmp/png-prefetch-m2-benchmark-tests.log`; PowerShell runtime unavailable on macOS, so the `.ps1` was not executed |
| 2026-09-24 23:51:59 | `689e27daf126b4145c63e2c0718befc1ce0d10ad` | Milestone 3 / Part 1 handoff | `cd TotalCrossSDK && ./gradlew-agent dist -x test`; Release CMake configure and `ninja ... tcvm Launcher`; deployed ImagePreparation smoke with `tiny.png` and `indexed.png`; focused headers, whitespace, and no-`TotalCrossVM`-delta checks | Passed: SDK distribution, native build, and both deployed PNG smokes; source dimensions 36×36 and 182×26; READY backing valid, immediate reuse added no decode, discarded candidate released | SDK artifact `TotalCrossSDK/dist/totalcross-sdk.jar`; runtime `/tmp/png-prefetch-m3-macos-native/libtcvm.dylib`, SHA-256 `ac48fc121de338951824d645f5c7d5e37090e33d6cc6a085bf9d4553906895e0`; logs `/tmp/png-prefetch-m3-sdk-dist.log`, `/tmp/png-prefetch-m3-cmake-configure.log`, `/tmp/png-prefetch-m3-native-build.log`, `/tmp/png-prefetch-m3-smoke-ordinary.log`, `/tmp/png-prefetch-m3-smoke-indexed.log` |
| 2026-09-25 00:09:46 | `f8308ac31` | Pre-Part-2 runner/provenance correction | `python3 scripts/test-image-scroll-distributed-benchmark.py`; requested `py_compile`; `bash -n scripts/package-image-scroll-benchmark.sh`; focused headers; `git diff --check`; signed commit/message checks | Passed: benchmark suite and static Windows runner/provenance checks; exact six-run matrix retained | PowerShell runtime unavailable for execution; SDK distribution and macOS smokes were not rerun for this scripts/docs-only change |
