<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Async Display Preparation — Implementation Report

## Summary

Q adds an explicit `ScrollContainer` operation that discovers visible JPEG
images, prepares detached image state on a single active worker, and adopts a
valid result on the UI thread. The resulting variant enters the existing P3
materialized fallback slot and is reused by normal drawing. Preparation is
explicit-only; ordinary scrolling does not start it. P1's
`automaticPreparation` remains disabled.

## Public API

The sole new application-facing operation is:

```java
ScrollContainer.prepareForDisplay(Runnable callback)
```

The optional callback runs on the UI thread after the latest batch settles.
Application artifacts exclude the internal preparation bridge and supporting
request, scheduler, result, and batch types. `ImageDrawingBridge` retains its
existing API surface.

## Discovery model

Discovery starts at the scrolling bag and intersects the ScrollContainer
viewport with clipping ancestors, clipping containers, and child bounds. It
visits visible, positive-sized controls that intersect the effective clip.
`ImageControl` contributes its active image, including temporary hardware
scaling state, and its background image when that image participates in normal
painting. The destination scale comes from the same `Graphics` instance used by
drawing.

Discovery runs synchronously on the UI thread. Unsupported formats and roots,
including PNG, settle as `NOT_PREFETCHABLE` without worker decode.

## Request identity and generations

The immutable request captures target Image, encoded source, pipeline,
decode-policy, and effective runtime-policy identities; exact destination-scale
bits; physical dimensions; decode denominator; frame; pixel and logical
dimensions; and readiness kind. It also captures source decoded generation and
target backing mutation generation as separate values. Path and serialized
pipeline data are never identity. Runtime diagnostics are not request identity.
Batch generation controls callback delivery only.

Adoption independently checks both generations. A changed target backing makes
the result stale. If synchronous decoding advanced the source generation,
adoption preserves the newer compatible live backing and installs the detached
variant only when the request still proves it semantically valid.

## Readiness model

The internal readiness states are `DRAW_READY` and `COPY_READY`. Normal
`ImageControl` display preparation requests only `DRAW_READY`; readiness stays
separate from failure state. `COPY_READY` is reserved for a result that is safe
for existing paths requiring independently adoptable materialized backing.

## Detached preparation

Worker preparation owns copies of captured encoded bytes and native encoded
storage, where present, and uses a fresh pipeline copy. Its materialized
fallback, pending state, draw plans, source decode state, and native derived
state are independent from live state. It uses the captured P5 decode policy
and existing JPEG decoder. Worker code does not resolve or mutate the live
Image, source, or pipeline.

## Global FIFO and scheduler

One process-global FIFO holds at most 128 pending identities, including the
active request. At most one request is active through worker preparation, UI
handoff, adoption, and terminal state. Equivalent pending requests share work;
each interested batch still settles independently. Overflow is transient and
can be retried by a later explicit call.

At most 16 ready metadata records are retained. They keep identity and
generation information without retaining the detached worker prototype or a
prepared Image variant. A later request is ready only when the live P3 pipeline
or compatible source backing proves readiness. Otherwise the metadata is
removed and the request is scheduled normally.

## UI adoption

Worker completion posts through `MainWindow.runOnMainThread`. The UI thread
revalidates target, source, pipeline, decode policy, scale, dimensions, frame,
runtime policy, and target backing mutation generation before publishing any
state. With an unchanged source generation, it transfers the detached decoded
backing and installs the exact prepared result into the existing P3 fallback
slot. Explicit preparation can install directly without the usual synchronous
second-use admission.

If a newer compatible live source backing already exists, adoption never
replaces it. It accepts a live DRAW_READY result first, or installs the
detached exact variant against the current source generation when compatibility
is proven. Otherwise it settles stale. The next FIFO entry starts only after
terminal adoption and completion accounting.

## Failure and stale semantics

Deterministic source failures are cached only after a current request passes
UI-thread validation. A stale deterministic failure does not poison the live
source. Allocation, worker-start, retryable native, queue overflow, and
adoption allocation failures remain transient. No partial backing or variant
is published. A later explicit preparation or synchronous draw may retry; P8
adds no automatic retries.

Every candidate settles once. New batches suppress older callbacks without
cancelling useful work or invalidating otherwise current requests. Empty
batches complete, callbacks run once on the UI thread, and callback exceptions
do not stall the FIFO. Batch state and scheduler locks are released before
callbacks, so callbacks may start another batch without running decode inline.

## Diagnostics

The opt-in `PREFETCH` domain records aggregate discovered, enqueued,
deduplicated, ready, stale, not-prefetchable, deterministic-failure, and
transient-failure counters, plus waiting-depth and active-count gauges. Feature
code uses the generic `RuntimeDiagnosticsFeatureBridge` with private slots;
private IDs occupy `0x5001` through `0x500A`. No path or per-request data is
recorded. The domain is appended after existing domains; the current order is
`RUNTIME`, `IMAGE`, `SCHEDULING`, `PREFETCH`. The diagnostics-off implementation
has no feature-owned storage or synchronization. Metrics do not affect request
eligibility or FIFO order.

## Stacked-branch integration

P8 was originally stacked on P5. After P5 merged, this branch was rebased onto
the latest `master` without replaying P5 commits. The current base includes P11's
`SCHEDULING` diagnostics; Q preserves that domain and its ordinal, then appends
`PREFETCH`. PR #479 remains based on `master` and is open; this work does not
merge it.

## Validation

Focused P8 lifecycle, discovery, identity, readiness, FIFO, worker/adoption,
stale, failure, diagnostics, and converter tests passed with diagnostics off and
with `-PruntimeDiagnostics=true`. The same runs covered P11 scheduling
diagnostics, container clipping and traversal, P2 backing/raster-core, P3
materialization/draw state, P5 lazy JPEG and decode-policy regressions, and P1
runtime-configuration tests. `artifactContentTest`, `dist -x test`, and
`compileSmokeTestJava` passed with diagnostics off. Artifact-boundary tests
confirm that application code cannot import the internal Q bridge or supporting
types.

The macOS ARM64 Release `tcvm` and `Launcher` build passed. Deployed macOS
smokes passed for Q async preparation, P5 lazy JPEG capture and decode, P2
raster-core behavior, P3 native materialization, and P3 draw presentation
state. The Q smoke confirmed captured-path deletion, stale batch and request
handling, transient retry, deterministic failure caching, UI-thread adoption,
and subsequent draw reuse (`decodeBefore=0`, `decodePrepared=1`,
`decodeAfterDraw=1`).

Local Android, Windows, Linux, WinCE, and iOS builds were not run. The fresh
CI matrix built Android, Windows, Linux, macOS, and iOS; WinCE has no explicit
job. Deployed smoke coverage is macOS ARM64 only. No frame-rate claim is made;
validation covers lifecycle correctness and reuse through the established
drawing path.

## Compatibility

### P2/P3/P5 integration

P2 remains authoritative for backing ownership, mutation generation,
detach-on-mutation, opacity metadata, transactional publication, and release.
P3 retains one Java materialized fallback slot, normal synchronous second-use
admission, its independent two-entry draw-plan cache, source-generation checks,
and native one-slot raster variants. Ready metadata cannot resurrect an
evicted variant. P5 captured encoded sources and decode policies provide the
worker input contract.

### Compact-storage compatibility

Preparation preserves the current source backing and uses the existing decoder
and backing contracts; it adds no compact conversion path or hidden promotion.
When compact storage is active, its policy remains authoritative. Any
policy-specific promotion or fallback should be covered by compact-storage
integration tests.

### Draw-path compatibility

Prepared output is installed in P3's normal fallback slot and remains reusable
by ordinary drawing. Q adds no alternate draw API, copy cache, framebuffer
reuse, or change to direct-copy ordering. A compatible scroll raster fast path
can reuse the established prepared image state through the normal draw path.

## Known limitations

Preparation currently accepts captured JPEG sources only. Work beyond the
128-entry pending limit settles transient and requires a later explicit call to
retry. A later scale can evict a prepared result from P3's single fallback
slot; ready metadata then defers to current source and pipeline readiness.
Native deployed smoke coverage is macOS ARM64; other platforms rely on their
fresh CI workflows.

## Deferred work

P9 owns replacing the isolated legacy scheduler with the Semaphore-driven
process worker. P10 owns PNG prefetch. Q adds no automatic preparation, PNG
async success path, Semaphore worker, permanent worker, parallel decode,
polling, framebuffer scroll reuse, or frame-pacing changes. No general thread
pool was added.
