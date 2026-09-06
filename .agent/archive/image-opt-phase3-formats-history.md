<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization phase 3 history

Milestone detail moves here when a Phase-3 milestone closes. Immutable
benchmark artifacts and compact evidence records remain under the paths named
by the active plan.

## Bootstrap

- Branch: `perf/image-opt-phase3-formats`
- Exact Phase-2 parent: `86bfeafe388ce866236c3ae58eecb144664895e2`
- No runtime or benchmark changes in the bootstrap slice.

## Compact backing completion

- Implementation checkpoint: `fb5718cb2`.
- Harness corrections: `94519f0b8`, `bbd364b32`, `119ab421c`.
- RGB565, GRAY8, ARGB4444, promotion, and both combined matrices completed
  with 60-sample S1/S2/S3 evidence under
  `.agent/benchmarks/image-opt-phase3-formats/`.
- Exact Phase-2 S1 artifacts and matched final-harness controls are both kept
  where a timing-floor correction changed the isolated workload.
- Final compact formats remain opt-in and disabled by default.
