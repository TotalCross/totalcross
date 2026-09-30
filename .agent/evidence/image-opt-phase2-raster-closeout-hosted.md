<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Phase 2 raster closeout hosted validation

Status: `DEFERRED — no supported hosted Phase 2 S1/S2/S3 workflow is available
on the final branch`

Validation head: `70aa29e8a367343146fd8ade9931a11038ef77d9` on
`perf/image-opt-phase2-raster`.

The existing workflow ID `351888305` is named `native swap benchmark`, but its
workflow file is absent from the final branch and from `master`. The historical
file at `2d926eb56` is a native swap benchmark, not the Phase 2 image S1/S2/S3
workload. The dispatch attempt was:

    gh workflow run 351888305 --ref perf/image-opt-phase2-raster

It returned HTTP 422: `Workflow does not have 'workflow_dispatch' trigger`.
The branch content lookup returned HTTP 404 for
`.github/workflows/native-swap-benchmark.yml`, and
`gh run list --workflow 351888305 --branch perf/image-opt-phase2-raster`
returned no runs.

The only current-head hosted result is the successful Merge flow run
`34170732235`:
<https://github.com/TotalCross/totalcross/actions/runs/34170732235>
with head SHA `70aa29e8a367343146fd8ade9931a11038ef77d9`. It is not benchmark
evidence. Historical native-swap runs `34079967147`, `34079709225`, and
`34079538428` ran on other heads and are not used here.

No hosted Windows/Linux S1/S2/S3 timing, RSS, mismatch, crash, or regression
claim is made. No QEMU or emulated performance evidence was collected.
