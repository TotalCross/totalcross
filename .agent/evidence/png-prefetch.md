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
