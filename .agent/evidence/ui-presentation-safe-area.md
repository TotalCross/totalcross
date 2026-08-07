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
| 1 | `ContainerClippingTest` | PASS; 1 test |
| 1 | `ClippedContainerTest` | PASS; 2 tests |
| 1 | focused header/static checks | PASS |
| 2 | `PresentationHostTest`, `SafeAreaLayoutTest` | PASS; 8 tests |
| 2 | focused header/static/new-file size checks | PASS |

Milestone 1 commits are `536a7984c` (explicit default clipping) and
`c6e2f90bc` (visibility search and sentinel correctness). The initial two test
runs failed only because their fixtures lacked deterministic launcher/screen
initialization; the corrected fixtures passed without production-code changes.

Milestone 2 commit `cd5082a1d` proves a safe viewport of
`20,10,260,600`, bottom outside origin `0,600,260,600`, unchanged owner/z-stack,
dynamic relayout to `24,12,252,596`, retained content identity, and clean
idempotent dismissal. The first run exposed the parent-bounds requirement and
the corrected deferred layout passed.

Verbose logs for later Gradle and smoke commands are stored under
`artifacts/ui-presentation-safe-area/logs/`.
