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
`ImageControl`: one short-lived background `Thread` decodes one detached
candidate, UI-thread adoption/finalization completes it, and only then does
`scheduleNext()` authorize another candidate. There is no persistent worker,
`waitForAdoption`, busy waiting, or additional concurrency primitive. The
TCVM-compatible coordinator uses `totalcross.util.concurrent.Lock`.

Shared `EncodedImageSource` instances use stable content identity for final
materialized variants, while decoded-backing generation remains specific to
draw-plan validity. `COPY_READY` drops source ownership of intermediate
decoded backing without explicitly releasing a backing still referenced by a
sibling plan. The earlier `final-fixed/` matrix is retained as historical
evidence; the authoritative result is `final-definitive-pass/`.

Enabled prefetch accounted for every request with 660 ready, zero failures,
and three not-prefetchable images. Cold scroll had zero targeted JPEG decodes,
three full decodes for the non-prefetchable cases, at most three final raster
materializations, and at most three native geometry materializations. Warm
targeted/full decodes and materializations were zero.

In the authoritative final run, cold p95 was at most 5 ms, warm/warm2 p95 was
at most 5 ms, and cold frames at or above 34 ms were at most 2. All prefetch
runs reported 663 requests, 660 READY, 0 FAILED, and 3 NOT_PREFETCHABLE.
Cold targeted JPEG decodes were zero, cold final/native materializations were
at most 3, and warm/warm2 JPEG decodes and materializations were zero. Both
target-color converted bytes and physical-variant bytes were zero. The
benchmark resets image, native-backing, and preparation accounting after UI
construction and immediately before `prepareForDisplay`.

## Review notes

- Detached decode candidates are marshalled to the UI thread; terminal success
  and failure paths clear the active slot before the next decode starts.
- Already-decoded `COPY_READY` requests use an unconditional MainWindow event
  queue continuation when a window exists, with a direct no-window fallback for
  tests/non-UI contexts. The terminal path queues the next adoption, so no
  inline recursive adoption chain remains during discovery.
- The benchmark starts on a subsequent UI timer tick so repaint accounting is
  not suppressed while runner callbacks are being drained; the cold pass also
  explicitly resets the scrollbar to its declared minimum.
- The batch completion check does not re-read a mutable content scale after
  discovery; the captured scale remains the request key, while active-batch
  identity protects against completing an obsolete batch.
- No general eviction/LRU policy was added. Dropping source ownership after a
  COPY_READY final raster is cached is intermediate-lifecycle cleanup and does
  not explicitly destroy sibling-referenced backing.

## Validation

- Focused SDK tests for image preparation, deferred graphics, traversal, and
  ScrollContainer insets, including COPY_READY pixel parity, deduplication,
  terminal cleanup, retry, and non-prefetchable fallback: passed.
- Release SDK distribution, benchmark compile/deploy, and macOS `tcvm` build:
  passed.
- ImagePreparation macOS native smoke: passed with injected adoption failure,
  retry, detached adoption, UI completion, timer responsiveness, captured
  optimization mask, deferred pipeline, and no extra decode during copyRect.
- TCVM-compatible `Lock` focused validation: passed; shared-source lifecycle
  regressions and existing ScrollContainer/traversal tests passed.
- Already-decoded scheduling regressions passed: request-time materialization is
  deferred, queued continuations iterate one active entry at a time, callbacks
  complete, active entries return to zero, and `COPY_READY` adds no JPEG decode.
- Authoritative final real-workload matrix: passed; the committed CSV and
  summary are in `.agent/benchmarks/image-scroll-prefetch/final-definitive-pass/`.
- Earlier `final/` and `final-fixed/` evidence remains preserved as historical
  and superseded material.
- The 663-JPEG matrix was not rerun for this scheduling-only correction because
  image/decode behavior and benchmark gates were unchanged.
