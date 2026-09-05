<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization phase 2 history

The phase-2 branch starts at phase-1 commit
`9545c18207fab74d81340b24825c5a82ddbda7fd`.

Milestone commits:

- `1d37de065` — add the raster optimization ExecPlan.
- `8b586d48a` — add all phase-2 benchmark workloads and runner arguments.
- `7e3346a9a` — record zero-copy S1; `f661e24ec`, `0b4680c13`, and
  `af1d6df65` implement and harden zero-copy decode; `f4f1a6ad9` records
  final S2/S3 results.
- `74137c24b` records opacity S1; `f6a4e1227` implements cached opacity
  metadata and records S2/S3; `0df4bafa3` records the finalized metadata
  benchmark results.
- `8f52cfdf9` records opaque-writePixels S1; `eb192e6fe` and `c590ec290`
  implement and parity-check the guarded fast path and record S2/S3.
- `e7376296b` records readback/color S1; `da324f3d8` implements bounded
  scratch and direct color materialization; `ee7b90051` batches the native
  row bridge; `d2108ad9e` records final readback/color S2/S3.

The final validation and handoff documentation is the next checkpoint. The
bootstrap/workload commits contain the literal `\\n` body sequences noted in
state; they are preserved because history rewriting is prohibited.
