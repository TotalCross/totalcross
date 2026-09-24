<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Implement prefetch thread diagnostics and worker comparison

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`, using the ExecPlan
policy referenced from `TotalCross/totalcross-depot-tools/.agent/PLANS.md`.

## Purpose / Big Picture

Implement the next image-prefetch experiment in `TotalCross/totalcross` so one
operator command runs the complete comparison:

    python3 run-benchmark.py --phase prefetch-thread-diagnostics

On a clean bundle it must run self-test automatically, then exactly six measured
processes:

    legacy per-entry thread   x mask 6
    legacy per-entry thread   x mask 38
    single worker, sleep 1ms  x mask 6
    single worker, sleep 1ms  x mask 38
    single worker, sleep 2ms  x mask 6
    single worker, sleep 2ms  x mask 38

Instrument thread creation/startup, decode-stage work, worker-to-UI dispatch,
adoption, `finishPreparation`, completion bookkeeping, entry lifetime, and worker
polling. Preserve existing JPEG and geometry diagnostics.

The final result ZIP must contain only `results/` and optional bundle-root
`DebugConsole.txt`. It must not contain corpus, executables, TCZs, manifest,
runner scripts, or other bundle inputs.

Default production behavior remains the current per-entry-thread implementation.
The worker is benchmark/test-only. Do not add `wait/notify`, `Semaphore`,
`LockSupport`, native condition variables, or parallel decode workers.

## Working Set and Resume Protocol

Target repository: `TotalCross/totalcross`.

Immutable planning base:

    feat/standard-streams-v1
    643293db5c09a2ccccb99147c66af796d4a0c5f9

Create branch:

    perf/prefetch-thread-diagnostics

Do not rebase onto a later branch; stop if the exact base is unavailable.

Save this plan verbatim as:

    .agent/plans/prefetch-thread-diagnostics.md

No separate state/evidence/history file is required initially. On resume, read
this plan's `Progress`, active milestone, and `Decision Log`, then run:

    git log --oneline 643293db5c09a2ccccb99147c66af796d4a0c5f9..HEAD

Inspect only active paths; do not reconstruct prior investigation. At completion
create and commit:

    .agent/reports/prefetch-thread-diagnostics-editorial.md

Commit every other durable artifact intentionally created. New files must stay
below 20 KB / ~600 lines; do not refactor existing files for size.

## Progress

- [ ] Activate branch and commit this plan artifact.
- [ ] Milestone 1: instrument the existing legacy thread lifecycle.
- [ ] Milestone 2: add the configurable serialized single worker.
- [ ] Milestone 3: add six-process phase, automatic self-test, aggregation, and
      narrow ZIP contract.
- [ ] Milestone 4: run allowed SDK/macOS validation and finalize artifacts.

## Current Architecture and Scope

`TotalCrossSDK/src/main/java/totalcross/ui/image/ImagePreparation.java` already
serializes work through `pending` and one `activeEntry`. A not-yet-decoded entry
currently gets a new Java `Thread`; that thread decodes, posts adoption to the UI
thread, the UI path adopts and calls `finishPreparation`, `finish()` clears
`activeEntry`, then `scheduleNext()` creates the next thread. There is no decode
parallelism despite repeated thread creation.

TotalCross does not expose `Object.wait/notify`; `Lock` is mutex-only.
`Vm.sleep(int)` reaches the runtime sleep primitive and clamps zero to at least
1 ms. This experiment deliberately uses polling and does not add a new runtime
synchronization API.

The benchmark application is:

    TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/
      ImageScrollRealWorkloadBenchmarkApp.java

The runner/test/package paths are:

    scripts/run-image-scroll-distributed-benchmark.py
    scripts/test-image-scroll-distributed-benchmark.py
    scripts/package-image-scroll-benchmark.sh

Preserve existing `prefetch-diagnostics` with masks `0,4,6,38,32799`. Add the
new thread experiment as a separate profile/phase.

Change the current `CLEAN_START` rejection to automatic self-test, while
preserving fatal partial/invalid handling. Narrow `write_zip()` from whole-bundle
archiving to the result contract below.

## Measurement Contract

All new clocks/counters in `ImagePreparation` must be active only when
`Image.diagnosticAccountingEnabledForTest()` is true. Add no unconditional
`System.nanoTime()` calls to the normal accounting-off legacy path.

Use `long` counts/times and reset everything through `resetAccountingForTest()`.
Expose package-private getters. Record:

- `preparationEntryCount`: entries activated as `activeEntry`.
- `preparationEntryTotalNs`: activation through completion bookkeeping, before
  next scheduling/poll observation.
- `threadCreateCount`: successfully constructed preparation `Thread` objects.
- `threadObjectCreateNs`: time constructing those objects.
- `threadStartCount`: successful `Thread.start()` returns.
- `threadStartCallNs`: time inside `Thread.start()`.
- `threadStartLatencyNs`: immediately before `start()` through first Runnable
  instruction. It overlaps `threadStartCallNs`; never add both.
- `decodeEntryCount`: entries that execute decode.
- `decodeWorkerNs`: `decode()` entry through candidate creation and pre-dispatch
  state transition. It includes native/JPEG work and overlaps `jpegDecodeNs`.
- `uiDispatchCount`: decode-to-UI adoption dispatches.
- `uiDispatchWaitNs`: immediately before `postUi()` through first instruction of
  the posted adoption Runnable.
- `adoptNs`: adoption Runnable start through stale check and Java/native
  candidate adoption, excluding `finishPreparation()`.
- `finishPreparationNs`: time only in `image.finishPreparation(...)`, including
  already-decoded adoption.
- `finishBookkeepingNs`: `finish()` state/callback/accounting work, stopping the
  timer before triggering the next legacy schedule.
- `workerPollCount`: worker polls that find no runnable entry and sleep.
- `workerSleepRequestedNs`: polls multiplied by configured sleep.
- `workerIdleElapsedNs`: actual elapsed time around `Vm.sleep()`.

Expose metadata:

    prefetchThreadMode = legacy | worker
    prefetchWorkerSleepMs = 0 for legacy, positive for worker

Overlapping metrics are attribution only; existing JPEG/geometry invariants
remain authoritative.

## Threading Contract

Add internal/test-only configuration in `ImagePreparation`, not a public SDK flag
and not `ImageOptimizationSettings`.

Modes:

    legacy  (default)
    worker

Worker sleep is a configurable positive integer in milliseconds; this diagnostic
uses only 1 and 2.

The benchmark app accepts internal arguments:

    --prefetch-thread-mode=legacy|worker
    --prefetch-worker-sleep-ms=<positive integer>

Legacy normalizes effective sleep to 0. Worker rejects zero/negative sleep.

Worker semantics are fixed:

1. Keep at most one `activeEntry`.
2. Claim work only when `activeEntry == null`.
3. Preserve current queued UI path for already-decoded entries.
4. Otherwise the one persistent worker calls the existing decode path.
5. After posting adoption, claim nothing until UI completion clears
   `activeEntry`.
6. Worker mode detects newly runnable work only by polling with
   `Vm.sleep(configuredMs)`.
7. Legacy mode keeps immediate `scheduleNext()` behavior.
8. Create the worker lazily once per process and keep it alive while the process
   runs; never create a worker pool.

Java SE tests must not hang. Add a package-private worker shutdown/reset hook and
use it in test `finally` blocks to restore legacy mode.

## Plan of Work

### Activation

From a worktree with unrelated changes untouched:

    git fetch origin feat/standard-streams-v1
    git switch --detach 643293db5c09a2ccccb99147c66af796d4a0c5f9
    git switch -c perf/prefetch-thread-diagnostics

If the branch already exists, require the planning SHA to be its ancestor with
`git merge-base --is-ancestor`; never reset/overwrite a divergent branch.

Commit this plan first after header/whitespace validation. Suggested commit:

    docs(plan): define prefetch thread diagnostic plan

Do not push unless explicitly requested.

### Milestone 1 — Instrument legacy lifecycle

Edit only:

    TotalCrossSDK/src/main/java/totalcross/ui/image/ImagePreparation.java
    TotalCrossSDK/src/test/java/totalcross/ui/image/ImagePreparationTest.java

Implement the measurement contract without scheduling changes. Centralize
measured Thread construction/start so legacy and worker use identical accounting.
Test reset, accounting-off zeros, serialization, decoded-entry thread counts, and
non-negative phases; do not assert timing thresholds.

Only at milestone end run the allowed SDK validation:

    cd TotalCrossSDK
    ./gradlew-agent test --tests totalcross.ui.image.ImagePreparationTest

Then from repository root run focused header validation and `git diff --check`.
Commit the slice following `.agents/skills/logical-commits/SKILL.md`.
Suggested commit:

    test(sdk): account prefetch thread lifecycle

Do not run native builds/smokes here.

### Milestone 2 — Add serialized persistent worker

Continue in the same two SDK files. Add the mode/sleep configuration, one lazy
persistent worker, `Vm.sleep(workerSleepMs)` polling, polling metrics, and the
Java SE shutdown/reset hook.

Test default legacy, one worker for multiple decodes, one active entry maximum,
serialization, already-decoded behavior, 1/2 ms acceptance, invalid sleep,
failure/retry, and shutdown/reset.

Only at milestone end rerun:

    cd TotalCrossSDK
    ./gradlew-agent test --tests totalcross.ui.image.ImagePreparationTest

Run header/diff checks and commit. Suggested commit:

    perf(sdk): add serialized prefetch worker

Do not add a native event/condition primitive.

### Milestone 3 — Integrate one-command diagnostic and artifacts

Edit:

    TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/
      ImageScrollRealWorkloadBenchmarkApp.java
    scripts/run-image-scroll-distributed-benchmark.py
    scripts/test-image-scroll-distributed-benchmark.py
    scripts/package-image-scroll-benchmark.sh

Do not create a new benchmark app.

In the app, parse/configure thread arguments before prefetch, capture the new
metrics, and write strategy metadata plus metrics into per-run `summary.json`,
`counters.json`/`prefetchPhases`, and concise console details. Make new diagnostic
run directories include strategy/sleep without changing existing profile paths.

In the runner define:

    PREFETCH_THREAD_DIAGNOSTIC_MASKS = (6, 38)

Configurations are exactly:

    (legacy, 0)
    (worker, 1)
    (worker, 2)

The new profile has `prefetch=on`, `accounting=on`, `rounds=1`, `passes=1`,
`expected_processes=6`, and `workload_images=663`.

Use dedicated/narrowly extended planning and aggregation; do not reshape all
existing matrices. Run keys include strategy, sleep, mask, and run.

Generate:

    results/prefetch-thread-diagnostics-suite-plan.tsv
    results/prefetch-thread-diagnostics.csv
    results/prefetch-thread-diagnostics-summary.json

CSV: one row per process with strategy, sleep, mask, prefetch wall time, existing
JPEG/geometry fields, and all new preparation metrics.

JSON summary: six rows plus per-mask legacy-vs-worker-1ms/2ms
`prefetchElapsedNs` deltas, descriptive only with no performance threshold.

Validate exactly six rows, two masks per strategy, legacy sleep 0, worker sleeps
1/2, accounting/prefetch on, non-negative metrics, per-entry legacy thread
creation, one worker thread in worker mode when decoded work exists, and all
existing JPEG/geometry invariants.

Automatic self-test behavior:

- Have preflight retain/return result state.
- `CLEAN_START`: run `self_test()` before any requested non-decode phase.
- `self-test`: still exits after self-test.
- `full`: keep self-test-first behavior.
- `VALID_RESUME`: reuse valid self-test.
- `PARTIAL_INVALID`: fail without deletion/overwrite.

ZIP behavior:

- Keep generated ZIP under `results/` unless current tests require otherwise.
- Add every regular file under `results/` except benchmark-result ZIPs, with
  `results/...` archive paths.
- Add bundle-root `DebugConsole.txt` as `DebugConsole.txt` when present.
- Missing `DebugConsole.txt` is non-fatal and logged concisely.
- Include no other bundle-root file.

Update package manifest metadata for the new six-process phase. Keep existing
31-process full suite and five-process `prefetch-diagnostics` unchanged.

Extend runner tests for the exact matrix, strategy argument transport, unique run
paths, aggregation/deltas, clean-start auto self-test, valid resume, invalid
resume rejection, ZIP with/without DebugConsole, exclusion of bundle inputs, and
unchanged existing profile metadata.

At milestone end run:

    python3 scripts/test-image-scroll-distributed-benchmark.py
    python3 -m py_compile \
      scripts/run-image-scroll-distributed-benchmark.py \
      scripts/test-image-scroll-distributed-benchmark.py
    bash -n scripts/package-image-scroll-benchmark.sh
    python3 scripts/validate-copyright-headers.sh --files \
      TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageScrollRealWorkloadBenchmarkApp.java \
      scripts/run-image-scroll-distributed-benchmark.py \
      scripts/test-image-scroll-distributed-benchmark.py \
      scripts/package-image-scroll-benchmark.sh
    git diff --check

Commit the functional benchmark slice. Suggested commit:

    test(benchmark): add prefetch thread diagnostics

If self-test/ZIP changes are independently substantial, use one additional
logical commit instead of inflating the first:

    test(benchmark): simplify benchmark result handoff

### Milestone 4 — Allowed integration validation and finalization

This is the broad integration checkpoint. Builds are permitted only for SDK and
macOS. Do not build Android, Windows, Linux, iOS, or another target.

At milestone end build SDK with `cd TotalCrossSDK && ./gradlew-agent dist -x test`.
On a macOS host only, build native `tcvm` in an isolated directory:

    cd ..
    cmake -S TotalCrossVM -B build-prefetch-thread-diagnostics \
      -DCMAKE_BUILD_TYPE=Release -G Ninja
    ninja -C build-prefetch-thread-diagnostics tcvm

If host is not macOS, do not substitute another native platform; record macOS
build/smoke as deferred for host mismatch.

Native smoke/benchmark is allowed only now (and once more only if required by the
final gate). If a real 663-image corpus is available via valid
`TC_IMAGE_CORPUS`, package only `macos-arm64` using the repository-supported
package script and the just-built matching SDK/runtime, then execute from the
bundle:

    python3 run-benchmark.py --phase prefetch-thread-diagnostics

Do not use `--all` and do not build Windows in this plan.

The native run must show automatic self-test on a fresh bundle, six completed
processes, six CSV rows, separate legacy/1ms/2ms results for masks 6/38, one
worker thread in worker runs, preserved JPEG/geometry validation, and a final ZIP
containing only `results/` plus optional `DebugConsole.txt`.

If `TC_IMAGE_CORPUS` is unavailable, do not synthesize a corpus or claim the real
benchmark ran. Record only that native diagnostic as deferred; focused tests and
allowed builds may still complete the implementation.

Create under 20 KB / ~600 lines:

    .agent/reports/prefetch-thread-diagnostics-editorial.md

Commit the report and final plan checkpoint together. Suggested commit:

    docs(benchmark): record prefetch diagnostic outcome

Do not commit build output, temporary bundles/results/ZIPs, caches, or logs.
Commit every durable source/test/plan/report/fixture artifact created for the task.

## Commit Policy

Follow `.agents/skills/logical-commits/SKILL.md` exactly: inspect scoped paths,
run the smallest allowed validation, validate headers, stage only task paths, run
`git diff --check --cached`, review staged diff/stat, use English scoped
Conventional Commit messages with bodies for non-trivial changes, and run the
skill's commit-message check. Do not amend/rewrite history, disable configured
signing, mix unrelated cleanup, or push without explicit request.

## Validation and Acceptance

Complete only when:

- default preparation still uses legacy per-entry threads;
- accounting-off legacy path adds no new timing reads;
- legacy and worker paths remain serialized;
- worker uses exactly one persistent thread plus `Vm.sleep(N)` polling;
- Java SE tests cannot hang on an orphaned worker;
- one phase command runs exactly six processes;
- clean results auto self-test, valid self-test resumes, partial-invalid fails;
- existing full-suite and `prefetch-diagnostics` counts remain unchanged;
- new metrics appear in per-run JSON and consolidated CSV/JSON;
- existing JPEG/geometry validation still passes;
- result ZIP scope is only `results/` plus optional `DebugConsole.txt`;
- focused Python and `ImagePreparationTest` validations pass;
- SDK build passes at its allowed milestone boundary;
- macOS `tcvm` build passes when host is macOS;
- real macOS diagnostic passes when corpus is available, or is explicitly
  deferred only for missing corpus/host;
- every durable task artifact is committed and generated validation output is
  not committed.

No performance threshold is an acceptance criterion.

## Risks and Open Questions

There are no unresolved architecture choices for the implementing agent.

Fixed mitigations:

- Instrumentation perturbation: gate every new clock read behind diagnostic
  accounting and compare all strategies with identical instrumentation.
- Overlapping startup metrics: document overlap; never sum start call and start
  latency into one remainder.
- `decodeWorkerNs` overlaps JPEG decode: keep `jpegDecodeNs` authoritative.
- Polling adds latency: measure both 1 ms and 2 ms plus actual idle elapsed time.
- Persistent Java SE worker can block JVM exit: explicit shutdown/reset in
  `finally`.
- Generic run keys could overwrite results: strategy/sleep are mandatory in new
  diagnostic keys.
- Auto self-test could mask damage: run automatically only from `CLEAN_START`.
- ZIP could include itself: explicitly exclude all benchmark result ZIP names.

## Idempotence and Recovery

Never delete/reset unrelated work. Re-running focused tests, builds in the
dedicated build directory, packaging, or a fresh diagnostic bundle is safe.

Never resume a bundle whose results are `PARTIAL_INVALID`; preserve it and use a
fresh bundle. Worker configuration is process-local and defaults to legacy.
Tests always stop/reset worker state in `finally`.

If validation fails, keep the slice uncommitted until fixed. Do not use
destructive Git rollback. Inspect only relevant changed paths and short log tails.

## Surprises & Discoveries

- `activeEntry` already serializes prefetch; repeated Java threads do not provide
  parallel decode.
- The 663-image workload can therefore pay thread lifecycle/scheduling cost many
  times in one serial run.
- TotalCross lacks Java `wait/notify`; this first worker experiment uses
  `Vm.sleep` polling instead of broadening runtime/API scope.
- Existing geometry diagnostics already localize geometry materialization; this
  plan measures coordinator overhead around them instead of reopening Skia.

## Decision Log

- Base exactly on `643293db5c09a2ccccb99147c66af796d4a0c5f9` and create
  `perf/prefetch-thread-diagnostics`.
- Keep legacy scheduling as default; worker is benchmark/test-only.
- Use one serialized persistent worker, never a multi-worker pool.
- Use configurable `Vm.sleep`; diagnostic values are exactly 1 ms and 2 ms.
- Do not add native signaling/concurrency APIs in this plan.
- Compare only masks 6 and 38 for this thread experiment.
- Expose one user command that internally runs all six configurations.
- Auto-run self-test only from `CLEAN_START`.
- Archive only `results/` plus optional `DebugConsole.txt`.
- Build only SDK/macOS at stated milestone ends; native smoke only at related
  milestone end/final gate.
- Follow `logical-commits`; do not push without explicit request.

Date for all initial decisions: 2026-09-23.

## Outcomes & Retrospective

Not yet implemented. At completion replace this with factual commits delivered,
behavior/results produced, validations actually run, measured macOS data if any,
and exact deferred items/reasons. Do not turn estimates into measured claims.

## Revision Note

Initial plan, 2026-09-23: prefetch thread diagnostic and result handoff.
