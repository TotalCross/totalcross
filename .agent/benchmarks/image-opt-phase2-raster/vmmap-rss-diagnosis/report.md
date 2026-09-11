<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Integrated raster RSS `vmmap` diagnosis

## Scope and exact inputs

This is a diagnostic-only capture. No runtime, benchmark source, benchmark
runner, ExecPlan, state, or acceptance decision was changed.

Both processes ran `ImageRasterCombinedBenchmarkApp` with 200 samples and the
same committed harness. The app performs three warmup batches before sample 1.
The processes were launched directly so they remained alive while `vmmap`
attached; the workload itself and its arguments were unchanged.

| Scenario | Runtime source | Runtime dylib SHA-256 | Arguments |
| --- | --- | --- | --- |
| S1 | `9545c18207fab74d81340b24825c5a82ddbda7fd` | `2b7a6e3e854d11dd2d08c7e7095860febb5faf33084f6ca710a10c1632cfac7b` | `--scenario=pre --samples=200` |
| S2 | final branch HEAD `8a7716258476fd74e87d1b5dfd35998250bda83d` | `32926d24c475ca3b6f04134ce4d6556c37d926862d6877d122cdec517213c4ca` | `--scenario=post-disabled --samples=200` |

S1 used the committed true-base adapter
`.agent/benchmarks/image-opt-phase2-raster/true-base-harness/prepare-image-opt-phase2-true-base.sh`
with digest
`cbfada1ba5f95c27005c473d1f7f128e644adf8f8496d8573bf8beeb1dc08a19`.
Both builds used Release software Skia:

```text
cmake -S TotalCrossVM -B <build> -G Ninja \
  -DCMAKE_BUILD_TYPE=Release \
  -DTC_GRAPHICS_SOFTWARE=ON -DTC_RENDERER_SKIA=ON -DTC_WINDOWING_SDL=ON
```

For each process, `vmmap -summary <pid>` and `ps -o rss= -p <pid>` were
captured immediately after the log reported `sample=100` and again after
`sample=150`. The exact summary files and capture RSS records are adjacent to
this report. No full `vmmap <pid>` dump was needed because the summary
categories explained the live and high-water footprint.

## Capture results

| Point | S1 RSS | S2 RSS | S1 physical footprint | S2 physical footprint |
| --- | ---: | ---: | ---: | ---: |
| sample 100 | 101,984 KiB | 100,576 KiB (-1.38%) | 75.0 MiB | 68.1 MiB (-6.9 MiB) |
| sample 150 | 100,096 KiB | 97,520 KiB (-2.57%) | 78.8 MiB | 65.5 MiB (-13.3 MiB) |

The `vmmap` lifetime high-water field was 82.9 MiB for S1 and 86.9 MiB for
S2. This is a transient high-water difference; it does not match the live
RSS/physical-footprint ordering at either controlled capture point.

## Region comparison at sample 100

Values below are `vmmap` resident/dirty values, S2 minus S1.

| Category | S1 | S2 | S2 - S1 | Interpretation |
| --- | ---: | ---: | ---: | --- |
| `MALLOC_SMALL` resident | 20.4 MiB | 20.1 MiB | -0.3 MiB | no S2 private-resident growth |
| `MALLOC_SMALL` dirty | 18.6 MiB | 10.5 MiB | -8.1 MiB | S1 had more dirty allocator pages |
| `MALLOC_SMALL (empty)` resident | 8,336 KiB | 7,408 KiB | -928 KiB | no S2 retained increase |
| `MALLOC_SMALL (empty)` dirty | 1,344 KiB | 4,128 KiB | +2,784 KiB | allocator fragmentation/state, not a leak proof |
| `MALLOC metadata` resident | 432 KiB | 400 KiB | -32 KiB | effectively equal |
| `__TEXT` resident | 264.8 MiB | 332.3 MiB | +67.5 MiB | read-only executable/library pages |
| `__DATA_CONST` resident | 15.8 MiB | 16.7 MiB | +0.9 MiB | small read-only/data-const variation |
| `__DATA` resident | 6,843 KiB | 7,063 KiB | +220 KiB | small |
| `__DATA_DIRTY` resident | 1,654 KiB | 1,637 KiB | -17 KiB | no S2 private dirty growth |
| mapped file resident | 13.5 MiB | 13.5 MiB | 0 | unchanged |
| `IOSurface` resident | 14.8 MiB | 14.8 MiB | 0 | unchanged |
| `IOAccelerator (graphics)` resident | 8,320 KiB | 8,320 KiB | 0 | unchanged |
| `VM_ALLOCATE`, stacks, shared memory | unchanged | unchanged | ~0 | not explanatory |

The large resident `__TEXT` difference is in the read-only portion of loaded
libraries: the summary reports 274.0 MiB for S1 and 342.9 MiB for S2. It is
not private writable allocation and does not account for a higher S2 physical
footprint; the S2 physical footprint is lower at the capture point. Mapped
files, IOSurface, graphics allocations, stacks, and `VM_ALLOCATE` do not
explain the earlier RSS delta.

## Growth check and conclusion

Between samples 100 and 150, S1 physical footprint moved from 75.0 to 78.8
MiB while S2 moved from 68.1 to 65.5 MiB. S1 RSS fell 1,888 KiB and S2 RSS
fell 3,056 KiB. The S2 private writable categories do not grow progressively;
there is no `vmmap` evidence of a S2 leak or retained private-memory increase.

This controlled `vmmap` run does not reproduce the earlier 6–7 MiB S2 RSS
excess. The prior peak-RSS difference is therefore consistent with
run-to-run/high-water sampling variance and executable/library page residency,
not a demonstrated disabled-raster allocation. The diagnostic result does not
change the Phase-2 acceptance decision and does not justify a runtime fix.
