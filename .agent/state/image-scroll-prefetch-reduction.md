<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image-scroll prefetch state

## Completed milestone

Milestone 2 compared legacy and persistent polling with a persistent
Semaphore-backed `ImagePreparation` worker. The default remains legacy. User
request and acceptance criteria are tracked in
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

## Outcome

The source, benchmark runner, and focused validations are complete. All six
macOS configurations passed on the 663-JPEG corpus. The Windows x64 benchmark
package is prepared from the current SDK and a production runtime artifact from
the established workflow. Windows measurements remain pending because the
current local commits are not available to that exact-commit workflow.

## Validation and artifacts

- Current branch/HEAD verified: `feat/semaphore-v1` /
  `205f6ba127a966d645d3025069cc403b2033f287` before task changes.
- SDK source/test slice committed as signed `9913a6847` (`perf(sdk): add
  semaphore prefetch worker`); the focused `ImagePreparationTest` Gradle suite
  passed (20 tests).
- Runner integration and package metadata specify the exact three-strategy
  matrix. Signed commits `fbc53cb26` and `8945bbff4` contain the matrix and
  deduplicated-entry validation fix. The focused runner suite passed.
- `./gradlew-agent dist -x test`, the macOS Release CMake configure/build, and
  the fresh six-process matrix all passed. Each row reported 663 requests,
  660 ready, zero failed, three unsupported, 642 unique preparation entries,
  one decode/adoption/finish per entry, and no deadlock. Semaphore rows ended
  with 641 releases, acquires, and work wakes and zero outstanding wakes.
- Full macOS CSV, summary, package, and logs are indexed in
  `.agent/evidence/image-scroll-prefetch.md`.
- The Windows bundle is prepared at
  `.agent/benchmarks/image-scroll-prefetch/worker-semaphore-milestone2/package-windows-ci36053759681/image-scroll-benchmark-windows-x64.zip`.
  Its production `tcvm.dll` matches workflow run `36053759681`, built at
  `ca7d77d88880ac5c6c666bd2b67721c2bead7063`; `TotalCrossVM` has no source
  delta from that commit to the current HEAD. The local revision is not pushed,
  so the Windows matrix has not run and remains pending.
- Implementation commits `9913a6847`, `fbc53cb26`, and `8945bbff4`, plus
  results/evidence commit `68f72fe96`, passed commit-message and signature
  checks. Focused copyright validation and `git diff --check` passed for the
  plan, state, evidence, and editorial files.
- Pre-existing `totalcross.code-workspace` edit is unrelated and must remain
  untouched.
- The 663-JPEG corpus is available at
  `/Users/flsobral/Downloads/win32/win32/imag`.
- Generated benchmark runs, packages, logs, and build outputs stay under an
  ignored/untracked artifact directory and are never staged.
- Windows run status: pending until a trusted Windows runner can execute this
  package and corpus against the current source revision.

## Resume protocol

Read this state first. Inspect only the paths above and the completed milestone
section in the ExecPlan. If a trusted Windows runner later executes the exact
package, append its results here and in the evidence index and editorial report.
