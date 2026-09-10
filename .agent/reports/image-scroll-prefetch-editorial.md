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

The final matrix passed all 16 fresh processes and 48 pass records using the
663-JPEG corpus. Enabled prefetch accounted for every request with 660 ready,
zero failures, and three not-prefetchable images. Successful prefetched images
did not perform first-use targeted JPEG decodes during cold scrolling.

Warm p95 stayed at 9 ms or below except for one 10 ms sample at 540x960 with
variant caching enabled; this is within the plan's allowed benchmark noise and
does not exceed the 10% regression gate. Prefetch intentionally retains its
detached backing memory: final live/peak usage was approximately 666--677 MB.

## Review notes

- Worker decode is detached from UI adoption; completion is marshalled to the
  UI thread and stale batches are rejected by active-batch identity.
- The benchmark starts on a subsequent UI timer tick so repaint accounting is
  not suppressed while runner callbacks are being drained.
- The batch completion check does not re-read a mutable content scale after
  discovery; the captured scale remains the request key, while active-batch
  identity protects against completing an obsolete batch.
- No eviction policy was added, as `PREFETCH_ALL` is explicitly an
  all-descendants, memory-first profile.

## Validation

- Focused SDK tests for image preparation, deferred graphics, traversal, and
  ScrollContainer insets: passed.
- Release SDK distribution, benchmark compile/deploy, and macOS `tcvm` build:
  passed.
- ImagePreparation macOS native smoke: passed with detached adoption, UI
  completion, timer responsiveness, deferred draw plan, and one targeted
  decode before first draw.
- Final real-workload matrix: passed; raw logs and CSV are in
  `.agent/benchmarks/image-scroll-prefetch/final/`.
