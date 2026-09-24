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
TCVM-compatible coordinator uses `totalcross.util.concurrent.Lock`. This
remains the production default path.

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

## Semaphore worker comparison

Milestone 2 adds two opt-in comparison strategies while keeping the production
default on legacy per-entry threads. `worker-poll` uses the existing 1 ms sleep;
`worker-semaphore` uses a persistent serialized worker and coalesced Semaphore
wake notifications. The macOS matrix ran once per configuration on the 663-JPEG
corpus. Times below are seconds except thread-start call time and start latency,
which are milliseconds. Poll values show count / requested sleep seconds /
measured idle seconds. Semaphore values show release / acquire / work wake /
outstanding wake counts.

| Mask | Strategy | Wall (s) | Prep (s) | Decode (s) | UI wait (s) | Finish (s) | Starts / call ms / latency ms | Poll count / requested s / idle s | Semaphore release / acquire / wake / outstanding |
|---:|---|---:|---:|---:|---:|---:|---:|---:|---:|
| 6 | legacy | 19.930 | 14.876 | 1.459 | 0.817 | 11.725 | 642 / 41.457 / 858.508 | 0 / 0.000 / 0.000 | 0 / 0 / 0 / 0 |
| 6 | worker-poll | 18.853 | 14.111 | 1.591 | 0.752 | 11.755 | 1 / 0.028 / 1.357 | 9601 / 9.601 / 12.091 | 0 / 0 / 0 / 0 |
| 6 | worker-semaphore | 18.382 | 14.056 | 1.503 | 0.782 | 11.759 | 1 / 0.025 / 1.163 | 0 / 0.000 / 0.000 | 641 / 641 / 641 / 0 |
| 38 | legacy | 16.817 | 12.575 | 1.364 | 0.850 | 9.496 | 642 / 36.365 / 851.621 | 0 / 0.000 / 0.000 | 0 / 0 / 0 / 0 |
| 38 | worker-poll | 16.453 | 11.744 | 1.382 | 0.819 | 9.533 | 1 / 0.029 / 1.331 | 7906 / 7.906 / 9.951 | 0 / 0 / 0 / 0 |
| 38 | worker-semaphore | 15.956 | 11.669 | 1.370 | 0.767 | 9.520 | 1 / 0.027 / 1.344 | 0 / 0.000 / 0.000 | 641 / 641 / 641 / 0 |

Each process passed with 663 requests, 660 ready, zero failed, and three
not-prefetchable images. There were 642 unique activated preparation entries;
each was decoded, adopted, and finished once. No process deadlocked, and both
Semaphore processes ended with zero outstanding wakes. These are single-run
measurements for comparison, not statistical performance claims. The exact
per-run data is in `prefetch-thread-diagnostics.csv` and
`prefetch-thread-diagnostics-summary.json` under
`.agent/benchmarks/image-scroll-prefetch/worker-semaphore-milestone2/package-macos-final/image-scroll-benchmark-macos-arm64/results/`.

The Windows x64 package is prepared at
`.agent/benchmarks/image-scroll-prefetch/worker-semaphore-milestone2/package-windows-ci36053759681/image-scroll-benchmark-windows-x64.zip`.
It uses the production runtime from trusted workflow run `36053759681` at
`ca7d77d88880ac5c6c666bd2b67721c2bead7063`; the current branch has no
`TotalCrossVM` source changes relative to that runtime source commit. The
current SDK and benchmark application were packaged at source commit
`8945bbff4825a1aa695a9dbdf189e0b5fa74ce8a`. Windows measurements for both masks
and all three strategies remain pending because this exact local revision is
not available to the trusted Windows workflow. No Windows values are inferred.

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
