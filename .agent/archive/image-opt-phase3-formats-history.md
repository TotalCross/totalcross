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
- Initial Phase-2 parent (historical bootstrap):
  `86bfeafe388ce866236c3ae58eecb144664895e2`
- No runtime or benchmark changes in the bootstrap slice.

## Exact-base rebase handoff

- Old Phase-3 HEAD: `9d5c6133318f97baeb88d382cb7837c3f585f122`.
- Frozen Phase-2 parent verified at
  `origin/perf/image-opt-phase2-raster`:
  `6d1c95f77fcb9c74d19b4e9393dba7c82cd37aee`.
- Rebasing produced Phase-3 implementation HEAD
  `15ab7d72e28f860f67106796bdd4cd7329075e56`.
- All 21 Phase-3 commits were replayed in their original order. Conflicts in
  build registration and compact-backing Java/native integration retained the
  finalized Phase-2 raster architecture and genuine compact-format changes.
- Focused SDK image tests, SDK distribution, Release macOS software-Skia
  build, and compact-format smoke passed after the rebase. Benchmarks remain
  historical and were not rerun.

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

## Milestone 9B final integration

- The authoritative final-stack candidate is `d2f8195b9faf828f4ee04154c93a93e1136c5bd4`,
  built from frozen Phase-2 base `6d1c95f77fcb9c74d19b4e9393dba7c82cd37aee`.
  The last 9A runtime correction was `8b455f2ad`; no 9B runtime fix was needed.
  Final adapter digest: `f2292c73893fb1568c3ba5e56803f4fb0b37d3ff5b7f193f9a992a3e91472f42`.
- Matrix A isolated and Matrix B full-stack authoritative artifacts are under
  `.agent/benchmarks/image-opt-phase3-formats/milestone9/`. Matrix A medians
  were `266/264/184` ms with S2 RSS delta `+0.81%`; Matrix B medians were
  `162/159/163` ms with first S2 RSS delta `+9.65%`. The required Matrix B
  200-sample rerun reduced the elapsed delta to `-0.62%` while RSS remained
  `+8.60%`; matched `vmmap`/`ps` checkpoints classified the signal as
  unconfirmed allocator/residency variation, with no runtime change.
- Existing PR workflow run
  `34190415679` passed at the exact candidate SHA across SDK, macOS ARM64,
  Android, iOS, Windows, Windows native/legacy, Linux amd64, and Linux ARM64;
  Linux arm32 cross remained intentionally skipped.
- Physical Android validation passed on Xiaomi `2312DRA50G`, Android 13,
  Adreno 710 / OpenGL ES 3.2 `V@0615.73`. All five compact formats were
  selected, direct decode was `5` / `2097152` bytes, quality was
  `max=32/rmse=8.056096554759305`, screen draw completed, and raster,
  promotion, and temporary-RGBA counters were zero. The diagnostic package
  was removed afterward.
- Hosted Windows/Linux S1/S2/S3 performance remains `NOT AVAILABLE`; no
  benchmark CI workflow was added. Phase 4 starts from the final docs tip,
  while runtime provenance remains the candidate SHA above.
