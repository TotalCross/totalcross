<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Static PNG asynchronous preparation

## Purpose

Allow eligible PNG images discovered by `ScrollContainer.prepareForDisplay` to
use the existing asynchronous image preparation lifecycle. Decode at
denominator 1, adopt on the UI thread, and reuse the prepared state during
ordinary drawing.

## Scope / Non-goals

This feature extends format eligibility and detached-result ownership. It
reuses the existing image pipeline, request identity, scheduler, FIFO,
deduplication, worker policies, UI adoption, and diagnostics.

It does not add a second scheduler or worker, a PNG-specific cache or runtime
policy, public API, native ABI, diagnostics domain or ID, or a performance
threshold. Multi-frame PNG remains outside asynchronous preparation.

## Current architecture

`Image.captureDisplayPreparationRequest` captures an encoded image source,
pipeline, decode policy, dimensions, scale, mutation generation, frame,
runtime policy, and batch generation. `Image.prepareDetachedForDisplay` copies
the encoded source and resolves the captured pipeline away from live UI state.
`Image.adoptPreparedForDisplay` validates that request before publishing the
decoded source backing and materialized variant.

`ImagePreparationScheduler` owns one process-wide FIFO shared by
`LEGACY_PER_ENTRY_THREAD` and `SEMAPHORE_PROCESS_WORKER`. A request remains
active through terminal UI adoption. The detached result owns unadopted source
and variant candidates until adoption decides which objects belong to live
state.

## Static PNG eligibility

An encoded JPEG remains eligible under the existing rules. An encoded PNG is
eligible only when its encoded frame count is exactly one. Both formats must
have a supported frame layout and valid, positive destination dimensions and
scale. Other formats and zero-width frame layouts remain ineligible.

A multi-frame PNG is not prefetched and continues through ordinary synchronous
drawing. Asynchronous preparation does not add animation or APNG semantics.

## Decode model

PNG preparation uses `decodeDenominator = 1` and the existing full PNG decode
path. It does not use JPEG tiered or targeted decoding. The established PNG
direct/zero-copy behavior remains governed by the existing runtime policy.

## Request identity

Keep the existing request identity and invalidation checks for the live target,
encoded source, pipeline, decode policy, destination scale, backing mutation
generation, frame, dimensions, runtime policy, and display batch. Do not add a
format-specific identity key.

## Scheduler / worker integration

PNG and JPEG share the same FIFO, deduplication, terminal states, and UI
adoption. Preserve the P9 process Semaphore worker when that runtime policy is
selected. Preserve the legacy per-entry worker as the production default. Keep
at most one request active through UI adoption, and invoke callbacks on the UI
thread after terminal cleanup and queue bookkeeping.

## Ownership and candidate cleanup

The detached `PreparedImageResult` owns candidates until terminal adoption.
After adoption, retain source and variant backings that current live state
references; release other native backings exactly once. Deduplicate identical
backing objects and release distinct decoded-source and transformed-variant
backings separately. Java raster backing requires no explicit release.

Cleanup is idempotent and runs before the scheduler activates the next FIFO
entry or invokes completion callbacks. If UI adoption is unavailable, release
all detached native candidates. Drop the detached encoded-source bag and byte
reference after its decode result is no longer needed.

## Stale / failure semantics

Pipeline, scale, frame, dimensions, source generation, runtime policy, or
backing mutation can make captured work stale. A synchronous source decode
that wins a generation race remains installed; a valid exact variant may be
retained for the current request without replacing that source backing.

Cache a deterministic decode failure only when the request is still current at
UI adoption. A stale deterministic failure must not poison the live source.
Transient decode and worker-start failures remain retryable through a later
explicit preparation. Synchronous drawing remains available for ineligible or
failed preparation.

## Diagnostics

Keep the existing format-agnostic `PREFETCH` diagnostics domain, ordinal, and
metric ranges. Do not add a PNG-specific metric or diagnostic ID. PNG full
decode must not increment targeted JPEG decode accounting.

## Compatibility

- **P4 storage:** Preserve existing STANDARD and COMPACT backing selection and
  decode behavior.
- **P5 JPEG:** Preserve lazy JPEG factories and existing decode behavior.
- **P6/P7 raster:** Preserve fast-path, warm-path, and raster reuse behavior.
- **P8/P9 preparation:** Preserve JPEG preparation, request identity, FIFO,
  deduplication, worker lifecycle, callback ordering, and retry semantics.
- **Worker modes:** Exercise PNG with both the default legacy worker and the
  Semaphore process worker.

## Validation plan

Use focused JavaSE tests for static eligibility, denominator selection,
multi-frame and unsupported-format rejection, full-decode accounting,
immediate draw reuse, deduplication, mixed-format FIFO, worker modes, stale
invalidation, source-generation races, failure and retry semantics, and
candidate ownership.

Run relevant SDK tests with runtime diagnostics disabled and enabled, artifact
boundary validation, smoke-source compilation, and the SDK distribution
build. Run a deployed macOS ARM64 smoke through
`ScrollContainer.prepareForDisplay` for true-color and indexed PNG, UI
adoption, backing validity, draw reuse, stale release, and both worker modes.
Run P4/P5/P6/P7/P8/P9 compatibility smokes and a macOS ARM64 Release build of
`tcvm` and `Launcher`. Finish with copyright-header validation and
`git diff --check`.

Local builds are limited to the SDK and macOS native VM. Cross-platform builds
are validated by the repository Merge Flow.

## Decision log

- Admit PNG only when encoded inspection reports one frame. Denominator 1 uses
  the supported full decoder and avoids treating animated PNG as a still.
- Put cleanup ownership on the detached result. It has the exact candidates
  produced by preparation and can distinguish retained live state from stale
  resources after UI adoption.
- Reuse the existing scheduler and runtime policies. Format support should not
  create a separate lifecycle or compatibility surface.

## Risks / limitations

- Full-resolution denominator-1 PNG decode can use more temporary memory than
  JPEG tiered decoding; P10 defines no performance threshold.
- A transformed pipeline may own a native variant distinct from its decoded
  source backing, so cleanup must handle both identities.
- Compact storage and direct/zero-copy PNG decoding must continue to follow
  the active runtime policy.

## Deferred work / P12 boundary

The external image corpus, benchmark runners and matrix, and final performance
comparison belong to P12. P10 is a correctness change and does not establish a
performance claim.
