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

## Corrective closeout

The post-review corrective slice preserved the historical benchmark
directories and added a separate corrected matrix under
`.agent/benchmarks/image-opt-phase2-raster/corrections/`.

- `926b5387b` and `aff5f5ede` add and fix direct APPLY_COLOR2 alpha parity for
  `0xAAxxxxxx` inputs.
- `6b5562b4a` and `4572797fc` cover and fix native-backing mutation
  invalidation, including mutable Graphics aliases and generation changes.
- `f3123be0c` and `74c4d5501` cover and fix zero-copy allocation-failure
  cleanup, retry behavior, and stable process-global decode masks.
- `d17ae57db`, `f7dc80637`, `a58a6b4c4`, and `374381d3f` extend the shared
  conservative opaque-copy path through trivial draw plans and native mutable
  targets, with the failure hook registered in the native table.
- `4a0ec8495`, `f162c981c`, `7cfa7521f`, `4af79bc0c`, and `b15aca032` batch
  the benchmark work, move full hashing outside timing, enforce the 30 ms
  floor, and recapture the final matrix.
- `736ac7c45` records the corrected decode, opacity, opaque-draw, and
  readback/color CSVs, summaries, and reports. The final gate passed the
  focused Image tests, SDK distribution, Release software-Skia native build,
  smoke-test compilation, and direct color, opacity, zero-copy, and draw-plan
  smokes.
- `b68f6cd28` adds the integrated decode/draw/readback/encode/color workload;
  `aba3c7d61` records individual matrices whose S1 is the true Phase-1 base,
  not a final-runtime disabled-feature proxy; `d044e13bd` records the
  integrated S1/S2/S3 matrix and its counter/parity report.

The integrated workload measured 877/878/56 ms median for S1/S2/S3, with
881/884/62 ms P95 and 122,688/131,296/140,352 KiB peak RSS. Its S3 counters
proved 120 zero-copy decodes, 60 source and 60 decode opacity proofs, 61,440
writePixels hits, 92,160 row reads, and 60 direct color materializations. All
three output hashes matched exactly. The final documentation checkpoint is
the Phase-3 base; Android, iOS, Windows, Linux, and GPU remain out of scope.
