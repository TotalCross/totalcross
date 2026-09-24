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
