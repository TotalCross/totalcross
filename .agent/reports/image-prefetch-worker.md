<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image prefetch worker

## Summary

The image preparation scheduler has an internal Semaphore-driven execution
mode for eligible JPEG requests. It reuses the existing P8 request discovery,
deduplication, FIFO ownership, detached preparation, UI adoption, and callback
path.

## Policy/default

`LEGACY_PER_ENTRY_THREAD` remains the runtime default. The alternate
`SEMAPHORE_PROCESS_WORKER` policy is captured in each immutable request policy
and is installable only by test and smoke support. No application-facing
selector or setter was added.

## Scheduler architecture

The scheduler continues to own one process-wide pending registry, FIFO, bounded
ready metadata list, and active request. Limits remain 128 pending requests and
16 ready metadata entries. Only `start(Work)` changes dispatch behavior:
legacy requests keep their per-entry thread; Semaphore-policy requests share
one lazily created process worker.

The worker reads only the scheduler's active request. It does not dequeue or
choose arbitrary work. Request policy identity remains part of request
equivalence.

## Worker lifecycle

The lifecycle is `NOT_STARTED`, `RUNNING_OR_BLOCKED`, or `SHUTDOWN`. Startup
publishes one worker and its wake Semaphore while holding the scheduler lock.
The worker remains the same thread across requests and blocks between them.
Test shutdown requires an idle scheduler, marks shutdown, releases the wake
Semaphore, and waits for the worker's completion signal. JavaSE test support
joins the exited thread before resetting scheduler state.

## Semaphore wake semantics

The process wake Semaphore starts with zero permits. A newly activated
Semaphore-policy request releases one permit. The worker waits with
`acquireUninterruptibly()` and begins preparation only after a permit arrives.
There is no polling or busy-wait work discovery, timer wake, or
`Thread.interrupt()`-based normal lifecycle.

Already-ready, deduplicated, already-decoded, and not-prefetchable requests do
not create or unnecessarily wake the worker.

## FIFO/adoption preservation

The scheduler remains responsible for activating work and keeps `active` set
through detached preparation and UI adoption. It removes the active request
and activates the next FIFO entry only in the existing UI completion path.
Callbacks still run on the UI thread, callback reentrancy queues behind the
current adoption, and a callback exception does not stall the next request.

## Ownership/threading

| Work | Owner/thread |
| --- | --- |
| Discovery, request identity, FIFO, active state, and ready metadata | Existing scheduler on the UI/requesting thread, under its lock where required |
| Detached JPEG preparation | One process Semaphore worker in the alternate policy; one per-entry worker in the default policy |
| Target mutation and prepared-result adoption | Existing UI completion callback |
| Completion callbacks | UI thread, after terminal adoption |
| Test policy forcing and worker observation | Test/smoke-only support; excluded from application artifacts |

P8's public `ScrollContainer.prepareForDisplay` API is unchanged.

## Failure/retry behavior

Worker creation or signaling failure follows the existing transient terminal
path. Partial worker startup state is cleared so an explicit later request can
retry. Stale requests remain rejected at adoption; deterministic decode
failures remain cached; transient failures remain retryable. Captured encoded
source ownership is released through the existing result lifecycle.

## Diagnostics

No P9 diagnostics domain, metric, or ID range was added. The diagnostics domain
order remains `RUNTIME / IMAGE / SCHEDULING / PREFETCH / RENDERING`; existing
metric ranges are unchanged. The runtime policy report continues to show
`LEGACY_PER_ENTRY_THREAD` by default. Diagnostics-disabled and
diagnostics-enabled SDK test suites both pass.

## Compatibility

- Default execution remains `LEGACY_PER_ENTRY_THREAD`; no performance or
  default-switch claim is made.
- The Semaphore worker is an internal execution policy with no
  application-facing way to select it.
- PNG remains outside prefetch and is deferred to P10.
- No generic Semaphore implementation, native registration, CMake source, or
  native ABI changed.
- The P8 public API, request deduplication, FIFO ordering, callbacks, stale
  handling, retry behavior, pending limit, and ready limit remain intact.

## Validation

SDK validation passed:

- Focused scheduler, async preparation, runtime startup policy, and Semaphore
  converter tests.
- Full `./gradlew-agent test --console=plain`.
- Full `./gradlew-agent test -PruntimeDiagnostics=true --console=plain`.
- `./gradlew-agent artifactContentTest --console=plain`.
- `./gradlew-agent compileSmokeTestJava --console=plain`.
- `./gradlew-agent dist -x test --console=plain`.
- Changed-file copyright header validation and `git diff --check`.

macOS ARM64 Release validation passed:

- `tcvm` and `Launcher` built for ARM64 with CMake Release configuration.
- Deployed Semaphore correctness, stress, and wake-latency regression smokes.
- P9 worker, Q/P8 async preparation, P5 lazy JPEG, P4 COMPACT and STANDARD,
  P6 fast/warm path, and P7 reuse regression smokes.
- The P9 smoke uses test-only policy forcing through the existing P8
  `ScrollContainer.prepareForDisplay` entry point. It passed ten consecutive
  direct launches and reported one worker across requests, UI callbacks,
  serialized FIFO/adoption, deduplication, draw reuse, captured-source
  behavior, stale/transient retry, idle blocking, and clean shutdown.

The Semaphore wake-latency smoke is used only as a correctness regression
signal, not as performance evidence. Its deployed diagnostic method requires
the existing test-only `TC_ENABLE_SEMAPHORE_TEST_DIAGNOSTICS` native build
option.

## Known limitations

- Windows, Android, Linux, WinCE, and iOS were not built locally; their enabled
  CI lanes are part of the PR gate.
- The P7 smoke's overall correctness marker passes, while its optional
  `nativeReuse` and `nativePrimitive` markers remain false in this environment;
  no native primitive reuse claim is made.
- Local macOS configuration used the current QRCodeGen and SQLite release tags
  from the pinned depot-tools checkout because the checked-in defaults did not
  resolve to available artifacts. No dependency pins were changed.

## Deferred work

Keep the legacy worker as default until a separate decision establishes a
measured reason to switch. PNG preparation remains P10. Cross-platform build
and deployed validation is delegated to GitHub Merge Flow; no performance
benchmark is part of P9.
