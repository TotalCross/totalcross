<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Async Display Preparation

## Context

`ScrollContainer` can explicitly prepare the images currently visible in its
scrolling content before they are drawn. Discovery and result adoption run on
the UI thread. Supported JPEG decoding and pipeline materialization run against
detached state on one disposable worker at a time. A successful result enters
the existing `ImagePipeline` materialized fallback slot, so ordinary drawing
uses the normal image path.

Image pixels and drawing state are protected by `ImageBacking` mutation
generations. Encoded JPEG sources retain their own decoded generation and
decode policy. Deferred image operations use the one-slot P3 materialized
fallback cache, separate draw-plan cache, and native raster variants.
Preparation must preserve these ownership and cache boundaries.

## Objectives

- Move explicit, eligible JPEG display preparation off the UI thread.
- Keep discovery, validation, adoption, and callbacks on the UI thread.
- Reuse the prepared result through the existing P2/P3/P5 drawing state.
- Bound deduplication and ensure one active request through terminal adoption.
- Leave ordinary scrolling and image drawing unchanged when preparation is not
  requested.

## Public API

The only new application operation is:

```java
ScrollContainer.prepareForDisplay(Runnable callback)
```

The callback is optional. It runs on the UI thread once every candidate in the
latest batch has settled. A newer call suppresses an older callback without
cancelling useful work. Automatic preparation remains disabled by default and
is not activated by Q.

`ImagePreparationFeatureBridge` is an unsupported internal bridge used only to
cross from `totalcross.ui` into `totalcross.ui.image`. It and the scheduler,
request, result, and batch implementation types are excluded from
application-facing SDK artifacts.

## Discovery model

Discovery starts at the `ScrollContainer` scrolling bag and uses the current
client viewport. It intersects that viewport with clipping ancestors, clipping
container bounds, and each control's bounds. Only visible, positive-sized
controls that intersect the effective clip are visited. Package-private hooks
on `Control`, `Container`, and `ImageControl` keep hierarchy traversal out of
the image implementation.

`ImageControl` contributes the image used by its active paint path, including
its temporary hardware-scaled image when active. It includes `imgBack` when
that background participates in normal painting. The destination scale comes
from the same `Graphics` instance used by drawing. Unsupported roots and
formats, including PNG, settle as `NOT_PREFETCHABLE` without worker decode.

Discovery is synchronous. It does not add scrolling, paint, or other automatic
triggers, and `ScrollContainer` does not depend directly on image internals.

## Request identity

Each immutable request captures:

- target `Image`, captured `EncodedImageSource`, and immutable
  `ImagePipeline` object identities;
- the pipeline's `ImageDecodePolicy` object identity;
- exact destination-scale bits and requested physical width and height;
- decode denominator, current frame, pixel dimensions, and logical dimensions;
- readiness kind and immutable effective `ImageRuntimePolicy` identity;
- source `decodedGeneration()` and target backing mutation generation as
  independent values.

Paths, filenames, and serialized pipeline descriptions are never identity.
Batch generation is callback bookkeeping only; it does not affect request
equivalence or make valid work stale.

## Detached preparation

The worker deep-copies the captured encoded bytes and owns a native encoded-bag
copy where applicable. It copies the immutable pipeline structure into fresh
materialization fallback, pending, draw-plan, and native-derived state. Decode
uses the captured P5 policy and existing decoder; it does not introduce a new
JPEG decoder.

Worker code never resolves or mutates the live image, live source decode state,
or live pipeline caches. It produces an owned decoded backing, an exact-scale
materialized variant, or a typed deterministic or transient failure.

## Scheduler and deduplication

One process-global FIFO has at most 128 pending identities, including the
active request, and at most 16 useful-ready metadata records. Equivalent
pending requests share work while each interested batch settles separately.
Queue overflow is transient and can be retried by a later explicit call.

Only the FIFO head gets a disposable worker. The active request remains active
through worker preparation, UI handoff, adoption, and terminal state. Queue
locks do not cover decode, backing allocation, hierarchy access, adoption,
resource release, or callbacks.

Ready records contain identity and generation metadata only. They do not retain
the detached prototype or a prepared variant. A later equivalent request is
ready only if the current P3 pipeline or reusable source backing proves
readiness. Otherwise its metadata record is removed and work is scheduled
normally. The live pipeline's one materialized fallback slot remains
authoritative.

## UI adoption

The worker posts adoption with `MainWindow.runOnMainThread`. Adoption verifies
target, source, pipeline, decode-policy, scale, dimensions, frame, runtime
policy, and target backing mutation generation before changing live state.
Source decoded generation is checked independently.

When the source generation is unchanged, adoption transfers the detached
decoded backing through the current source contract and installs the exact
prepared variant in P3's existing one-slot fallback cache. Explicit
preparation may admit this exact result without the ordinary second-observation
step; later synchronous materialization keeps normal P3 admission behavior.

If synchronous drawing advanced the source generation, adoption first accepts
an already-ready live result. Otherwise it may associate the detached exact
variant with the current generation only when the newer reusable backing is
compatible with the captured request. It never replaces a newer source backing.
If that compatibility cannot be proved, the result is stale.

The active FIFO slot is released only after adoption reaches a terminal state.
The next request starts after callbacks have been accounted for and outside the
scheduler lock.

## Failure and stale semantics

Changing the target backing generation, source or pipeline identity, policy,
scale, dimensions, or frame makes the result stale. A stale result cannot
mutate live image state. Stale deterministic failures are not cached.

Deterministic decode failures are cached on the live encoded source only after
the request has passed UI-thread validity checks. Allocation, worker-start,
retryable native, queue-overflow, and adoption allocation failures are
transient and remain retryable. Partial backing or variant state is never
published.

Every candidate settles once as ready, deduplicated/ready, not-prefetchable,
stale, deterministic failure, or transient failure. Empty batches complete.
Only the latest callback runs, exactly once and on the UI thread. Callback
exceptions do not keep the FIFO active or prevent later requests from starting.

## Diagnostics

The opt-in `PREFETCH` domain uses the generic
`RuntimeDiagnosticsFeatureBridge`. Feature-private slots map to private IDs
`0x5001` through `0x500A`: discovered, enqueued, deduplicated, ready, stale,
not-prefetchable, deterministic failure, transient failure, waiting queue
depth, and active count. No path, image identity, pipeline data, or metric ID
is exposed to applications.

`PREFETCH` is appended after all domains present in the runtime diagnostics
snapshot. Existing domain ordinals and the P11 scheduling domain, when present,
remain unchanged. Disabled or diagnostics-off builds add no feature-owned
metric storage or synchronization and do not affect scheduling or results.

## Compatibility constraints

- P2 backing ownership, mutation generations, opacity metadata, release
  behavior, and transactional publication remain authoritative.
- P3 retains one Java materialized fallback slot, its normal second-use
  admission for synchronous materialization, its two-entry draw-plan cache,
  source-generation checks, and native one-slot raster variants.
- P5 captured encoded sources and decode policies remain the input contract.
- Compact source backing remains authoritative when compact storage is enabled;
  read-only display preparation does not force promotion.
- Prepared variants remain reusable by normal drawing and any compatible direct
  copy path; Q adds no parallel draw or framebuffer cache.
- `ImageDrawingBridge` retains its existing surface. No image internals or
  public diagnostics bridge are added to application artifacts.

## Milestones

1. Add clipped hierarchy discovery and the single explicit API.
2. Capture request identity, detach JPEG source and pipeline state, and adopt
   validated results through the existing P2/P3/P5 contracts.
3. Add bounded FIFO deduplication, batch completion semantics, retryable
   transient outcomes, and aggregate opt-in diagnostics.
4. Add focused SDK lifecycle, regression, artifact-boundary, and compile-surface
   coverage, followed by the supported macOS native and deployed smoke checks.

## Validation

Run focused SDK tests for discovery, request identity, FIFO limits and
deduplication, adoption races, failure retry, callback semantics, runtime
diagnostics, and directly affected image and scroll regressions with diagnostics
disabled and enabled. Validate artifact contents and compile surfaces, then run
`dist -x test` and smoke-source compilation.

At the native milestone, build only macOS ARM64 Release `tcvm` and `Launcher`.
Run deployed preparation and directly affected P2/P3/P5 smokes. If compact
storage or scroll raster reuse is present in the integrated base, include a
focused compatibility smoke for that behavior. CI covers other platforms.

Use `git diff --check` and the focused copyright-header validator on changed
first-party files. Do not require broad benchmarks or claim a specific frame
rate improvement; correctness and reuse through the established draw path are
the acceptance criteria.

## Risks and tradeoffs

- Native encoded-bag copying must own its storage and preserve native allocation
  and error semantics.
- Synchronous drawing can advance source generation during worker preparation;
  adoption must preserve a newer compatible source and reject uncertain cases.
- P3 intentionally retains only one materialized fallback. A later scale may
  evict a previously prepared variant, so ready metadata must always defer to
  current pipeline/source readiness.
- UI handoff and callbacks depend on the normal event loop; callers must not
  block that loop while waiting for completion.

## Out of scope

Automatic prefetch, asynchronous PNG decoding, Semaphore-based permanent
workers, thread pools, parallel decoding, polling, framebuffer scroll reuse,
and frame-pacing changes belong to other work.
