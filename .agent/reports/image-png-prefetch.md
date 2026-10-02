<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# V/P10 — Static PNG async preparation

## Summary

Static PNG images use the existing `ScrollContainer.prepareForDisplay`
asynchronous image preparation lifecycle. Decode and UI adoption use the
existing image source, pipeline, scheduler, FIFO, and runtime worker policies.
The change adds no public SDK API, native ABI, runtime policy option,
diagnostics domain, or diagnostics ID.

## Eligibility

Encoded JPEG remains eligible under existing rules. Encoded PNG is eligible
only when it has exactly one frame, a supported frame layout, and valid
positive destination dimensions and scale. Multi-frame PNG remains
`NOT_PREFETCHABLE` and uses ordinary synchronous drawing. Other unsupported
formats remain outside asynchronous preparation.

## Decode model

PNG preparation uses denominator 1 and the existing full PNG decoder. It does
not use JPEG tiered decoding or increment targeted JPEG decode accounting.
Prepared drawing reuses the published state without decoding the source again.

## Request identity

Preparation retains the existing identity and invalidation checks for target,
encoded source, pipeline, decode policy, destination scale, backing mutation
generation, frame, dimensions, runtime policy, and display batch. No PNG-
specific cache key was added.

## Scheduler/worker integration

PNG shares the P8 FIFO, deduplication, terminal-state handling, and UI
adoption, with the P9 process Semaphore worker when selected. The legacy
per-entry worker remains the production default. Mixed JPEG/PNG FIFO and
duplicate callback tests pass; active work stays at or below one through
adoption. Deployed PNG smokes pass under both Semaphore and legacy modes.

## Ownership and cleanup

`PreparedImageResult.releaseUnretainedCandidates` is idempotent. After terminal
adoption decides ownership, it retains source and variant backings published
into current live state and releases other native candidates. It deduplicates
identical objects and releases distinct source and transformed variant
backings separately. Java raster backing needs no explicit release.

Cleanup also releases all detached native candidates when UI adoption is
unavailable. The detached encoded-source bag and byte reference are dropped
before the scheduler continues the FIFO or invokes callbacks. Deployed stale
result accounting observed two native candidates created, two released, and
the live backing count unchanged at three. Successful prepared backings
remained valid and were reused by drawing.

## UI adoption

Adoption occurs on the UI thread and verifies the captured target, source,
pipeline, scale, mutation generation, dimensions, frame, and runtime policy. A
source-generation race preserves the exact ready variant while leaving the
newer synchronous source backing intact. Cleanup completes before the next
FIFO activation and completion callbacks.

## Failure/stale semantics

Pipeline, scale, and backing mutation invalidate captured PNG work under the
existing stale rules. A stale deterministic decode failure does not poison the
live source. A current corrupt PNG caches its deterministic failure at UI
adoption; transient decode failure and worker-start failure remain retryable
on a later explicit preparation. Discarded candidates are released once,
while source-race READY candidates remain available.

## Diagnostics

The existing format-agnostic `PREFETCH` diagnostics are unchanged. No domain,
ordinal, metric range, or production metric ID was added. Full SDK tests pass
with runtime diagnostics disabled and enabled.

## Compact-storage compatibility

P10 does not change runtime storage policy or PNG backing selection. Existing
P4 STANDARD and COMPACT macOS smoke apps pass. P10 preparation also passes on
the default STANDARD runtime.

## JPEG compatibility

JPEG eligibility, denominator selection, and targeted decode accounting are
unchanged. P5 lazy JPEG, P8 asynchronous preparation, and P9 Semaphore worker
macOS smokes pass. JavaSE tests cover retry, stale work, deduplication, FIFO
order, and UI callback behavior.

## Validation

- Focused JavaSE lifecycle tests pass for `ImageAsyncPreparationTest` and
  `ImagePreparationSchedulerTest`.
- The full JavaSE suite passes with diagnostics off: 547 tests, 0 failures,
  0 errors, 23 skipped. With diagnostics on: 547 tests, 0 failures, 0 errors,
  2 skipped.
- `artifactContentTest`, `compileSmokeTestJava`, and `dist -x test` pass.
- Deployed P10 Semaphore and legacy PNG smokes both report
  `overallPass=true`, asynchronous UI callbacks, denominator 1, valid source
  backing, draw reuse, full-decode-only accounting, and stale candidate
  release. Semaphore mode starts one process worker; legacy mode creates
  none. Across the three PNG fixtures, accounting is three full decodes and
  zero targeted decodes. Stale cleanup releases two native candidates and
  restores the live backing count to its baseline of three.
- P8 JPEG asynchronous preparation, P9 Semaphore worker, P5 lazy JPEG, P4
  STANDARD and COMPACT, P6 fast/warm raster paths, and P7 raster core and
  presentation-state smokes pass.
- macOS ARM64 Release `tcvm` and `Launcher` build passes. The QRCodeGen asset
  was unavailable for this target, so the existing QRCode backend was used;
  no dependency pin was changed.
- Copyright-header validation and `git diff --check` pass.

## Known limitations

Multi-frame PNG is not prepared asynchronously. PNG decode remains a full
denominator-1 decode, with no P10-specific memory or performance threshold.
No external corpus or performance comparison was run.

## Deferred work

The 663-image corpus, benchmark runners and matrix, and performance comparison
belong to P12. Cross-platform builds are validated by the repository Merge
Flow.
