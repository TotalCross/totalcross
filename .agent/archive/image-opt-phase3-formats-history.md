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

## Corrective closure checkpoint

- Runtime row-conversion correction: `37746781b`.
- Correctness and combined assertion checkpoint: `6fcb50a37`.
- The implementation was largely delivered in one runtime slice before the
  per-format S1 captures; exact-base and matched-control evidence remain
  separate.
- 200-sample matched RSS pairs were ARGB4444 `127088 -> 139456` KiB,
  promotion `153216 -> 153744`, combined-disabled `147904 -> 151504`, and
  full-stack combined-enabled `157568 -> 152496`. Required ARGB4444 checkpoint
  `vmmap -summary`/`ps` evidence is preserved; its lower S2 peak physical
  footprint makes the raw RSS signal unconfirmed.
- Final post-optimization combined medians/P95/peak RSS are disabled-stack
  `263/264/145424`, `263/264/154352`, `184/186/154304`, and full-stack
  `160/161/146656`, `161/164/152256`, `162/163/139360` for S1/S2/S3.

## ARGB4444 RSS anomaly closure

- The first corrective S1/S2 pair was rejected as mismatched because the
  detached S1 bundle still contained the shorter pre-correction workload.
- A corrected S1 overlay used the final benchmark workload/support with the
  conservative Phase-2 hook shim and the exact true-base dylib. Its overlay
  digest is `edd9a79ebb30d081681b141a245a8525788b74df4a55712113ecf7a9c2335d7d`.
- Three valid alternating 200-sample pairs produced S2 peak-RSS deltas
  `-0.7%`, `+7.1%`, and `-4.3%`; S2 peak physical-footprint deltas were
  `-4.9%`, `-2.6%`, and `+2.0%`. `vmmap -summary`/`ps` checkpoints showed
  order-dependent allocator/page residency, not a reproducible disabled-path
  regression. Runtime source was unchanged.
