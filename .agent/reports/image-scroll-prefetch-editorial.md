<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image scroll prefetch editorial report

## Scope

The `perf/image-scroll-prefetch` branch adds asynchronous image display
preparation, package-private descendant traversal, and a `ScrollContainer`
`PREFETCH_ALL` batch API. The customer workload now measures both disabled and
enabled prefetch profiles without changing the four feature-13/14 profiles.

## Result

The branch has two explicitly distinguished benchmark generations. The earlier
`final/` implementation removed first-use JPEG decode but left roughly 645--648
final image/native-geometry materializations in the cold scroll. That was
insufficient for the copyRect contract.

The corrected implementation uses `COPY_READY` for the default
`ImageControl`: worker decode is adopted on the UI thread, the final raster is
materialized and cached before scrolling, and the intermediate decoded source
backing is released only when no cached final variant still references it. The
corrected `final-fixed/` matrix passed all 16 fresh processes and 48 records
using the exact 663-JPEG corpus.

Enabled prefetch accounted for every request with 660 ready, zero failures,
and three not-prefetchable images. Cold scroll had zero targeted JPEG decodes,
three full decodes for the non-prefetchable cases, at most three final raster
materializations, and at most three native geometry materializations. Warm
targeted/full decodes and materializations were zero.

In the final run, cold p95 was at most 6 ms, warm all-prefetch p95 at most 5
ms, disabled warm p95 at most 9 ms, and cold frames at or above 34 ms at most
one. Prefetch elapsed time was 11.5--14.2 seconds; live/peak backing memory at
prefetch completion was 282--357 MB, and after cold/warm scrolling it was
296--370 MB. Target-color converted and physical-variant bytes were zero in
the all-prefetch records.

## Review notes

- Worker decode is detached from UI adoption; completion is marshalled to the
  UI thread and stale batches are rejected by active-batch identity.
- The benchmark starts on a subsequent UI timer tick so repaint accounting is
  not suppressed while runner callbacks are being drained; the cold pass also
  explicitly resets the scrollbar to its declared minimum.
- The batch completion check does not re-read a mutable content scale after
  discovery; the captured scale remains the request key, while active-batch
  identity protects against completing an obsolete batch.
- No general eviction policy was added. Releasing a decoded source after its
  COPY_READY final raster is cached is an intermediate-lifecycle cleanup, not
  an LRU or memory-pressure policy.

## Validation

- Focused SDK tests for image preparation, deferred graphics, traversal, and
  ScrollContainer insets, including COPY_READY pixel parity, deduplication,
  terminal cleanup, retry, and non-prefetchable fallback: passed.
- Release SDK distribution, benchmark compile/deploy, and macOS `tcvm` build:
  passed.
- ImagePreparation macOS native smoke: passed with injected adoption failure,
  retry, detached adoption, UI completion, timer responsiveness, captured
  optimization mask, deferred pipeline, and no extra decode during copyRect.
- Corrected final real-workload matrix: passed; raw logs, CSV, and summary are
  in `.agent/benchmarks/image-scroll-prefetch/final-fixed/`.
