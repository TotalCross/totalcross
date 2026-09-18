<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Editorial handoff — image-scroll diagnostics macOS instrumentation

This report is completed at the end of Plan 1. It summarizes delivered
diagnostic contracts, exact commits, validation, limitations, and discoveries
without claiming optimization results.

## Initial scope

Instrument writePixels fallback classification and native JPEG decode timing
for the existing macOS image-scroll benchmark. No optimization policy changes
are in scope.

## Delivered

- Gated native writePixels rejection and device one-to-one opportunity
  counters, preserving attempts/hits/fallbacks.
- Gated native JPEG timing with actual `1/1`, `1/2`, `1/4`, `1/8`, and other
  denominator buckets, requested-mode counts, and failure counts.
- Prefetch/scroll phase separation, per-frame JPEG count/ns columns, detailed
  `counters.json`, compact distributed `summary.csv` fields, and explicit
  attempt-based feature statuses.

## Validation and discoveries

Milestone 1 SDK tests/distribution and macOS ARM64 `tcvm`/`Launcher` builds
passed. Milestone 2 static checks, SDK/native rebuild, macOS-only package,
bundle self-test, four prescribed smokes, and mask-4 `off`/`on` smokes passed.
Mask 4 reported nonzero writePixels attempts, zero hits, and
`ATTEMPTED_NO_HIT`; JPEG denominator and frame-sum invariants passed.

The first packaged smoke found the VM's 32-character native resolver limit for
long diagnostic getter names, including a candidate-name truncation collision.
Commit `f803d4a4d` corrected the private bridge identifiers and the complete
gate was rerun successfully. No optimization policy or rendering behavior was
changed. The next action is Plan 2:
`.agent/plans/image-scroll-diagnostics-macos-02-benchmark.md`.
