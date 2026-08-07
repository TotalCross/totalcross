<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Safe presentation evidence

## Baseline

- Branch: `feat/logical-ui-scaling2`
- Base commit: `62c9c728cd0570c1e1a8219b42dfd72c6fedd355`
- Planning commit: `9a3b22ae1`
- Prior commits preserved: `d848b627b` safe-area animation layout and
  `62c9c728c` explicit top-bar width.
- `PathAnimation.create(..., preserveOrthogonalPosition)` is present for legacy
  callers; the new presentation foundation will use explicit local coordinates.

## Validation ledger

| Milestone | Command or evidence | Result |
| --- | --- | --- |
| 0 | scoped plan/design `git diff --check` | PASS |
| 0 | copyright validator for plan/design | PASS (2 files) |
| 0 | `wc -lc` for plan/design | PASS; both below 20 KiB and 600 lines |

Verbose logs for later Gradle and smoke commands are stored under
`artifacts/ui-presentation-safe-area/logs/`.
