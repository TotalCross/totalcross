<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image-scroll prefetch state

## Active milestone

Milestone 2: compare the existing legacy and persistent polling strategies with
a persistent Semaphore-backed `ImagePreparation` worker. The default remains
legacy. User request and acceptance criteria are tracked in
`.agent/plans/prefetch-thread-diagnostics-execplan.md` under
“Milestone 2 extension — Semaphore comparison”.

## Active paths

- `TotalCrossSDK/src/main/java/totalcross/ui/image/ImagePreparation.java`
- `TotalCrossSDK/src/test/java/totalcross/ui/image/ImagePreparationTest.java`
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageScrollRealWorkloadBenchmarkApp.java`
- `scripts/run-image-scroll-distributed-benchmark.py`
- `scripts/test-image-scroll-distributed-benchmark.py`
- `scripts/package-image-scroll-benchmark.sh`
- `scripts/README-image-benchmarks.md`
- `.agent/plans/prefetch-thread-diagnostics-execplan.md`
- `.agent/evidence/image-scroll-prefetch.md`
- `.agent/reports/image-scroll-prefetch-editorial.md`

## Required comparison

Masks `6` and `38` across exactly:

- `legacy`, sleep 0 ms
- `worker-poll`, sleep 1 ms
- `worker-semaphore`, sleep 0 ms

The semaphore is a coalesced wake signal under `LOCK`; the worker keeps one
active decode and waits for the existing UI adoption/finish lifecycle. Record
release/acquire/wake counts, outstanding wakes, host process wall time, and the
existing phase and thread/poll metrics. Verify all 663 workload images,
serialization, no lost/duplicate preparation, no deadlock, and zero outstanding
wakes after shutdown.

## Next concrete action

Run the SDK distribution and macOS native build, then package and execute the
six-process macOS 2x3 matrix. Continue with Windows package preparation through
the established Windows native workflow.

## Validation and artifacts

- Current branch/HEAD verified: `feat/semaphore-v1` /
  `205f6ba127a966d645d3025069cc403b2033f287` before task changes.
- SDK source/test slice committed as signed `9913a6847` (`perf(sdk): add
  semaphore prefetch worker`); the focused `ImagePreparationTest` Gradle suite
  passed (20 tests).
- Runner integration and package metadata now specify the exact three-strategy
  matrix. `python3 scripts/test-image-scroll-distributed-benchmark.py` passed
  with all six rows and desktop argument-payload checks.
- `git verify-commit HEAD` and the focused commit-message validation passed.
- Pre-existing `totalcross.code-workspace` edit is unrelated and must remain
  untouched.
- The 663-JPEG corpus is available at
  `/Users/flsobral/Downloads/win32/win32/imag`.
- Generated benchmark runs, packages, logs, and build outputs stay under an
  ignored/untracked artifact directory and are never staged.
- Windows measurement status: pending until a trusted Windows execution uses
  the same package and corpus.

## Resume protocol

Read this state first. Inspect only the active paths above and the active
milestone section in the ExecPlan. Append compact validation/evidence results
here after each logical checkpoint; use the evidence index for artifact paths.
