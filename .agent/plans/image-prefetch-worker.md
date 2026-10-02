<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Semaphore-driven image prefetch worker

This ExecPlan follows `.agent/PLANS.md` and `AGENTS.md`.

## Purpose / Big Picture

Add an internal scheduler mode that runs eligible image preparation on one
process-wide worker awakened by the existing `java.util.concurrent.Semaphore`.
The default remains `LEGACY_PER_ENTRY_THREAD`. Callers continue to use the P8
`ScrollContainer.prepareForDisplay` API and see the same discovery, decode,
adoption, readiness, callback, stale-result, and retry behavior.

## Working Set and Resume Protocol

This plan is the durable design record. The final factual handoff is
`.agent/reports/image-prefetch-worker.md`; read it when reviewing completed
validation and compatibility claims. No state, evidence, checkpoint, or history
files are used. Resume by checking the current branch and focused diff, then the
active milestone below.

## Progress

- [x] Create this plan as the first feature commit.
- [ ] Add the internal policy value and a test-only policy fixture.
- [ ] Route activated work through the legacy or Semaphore execution path.
- [ ] Prove serialized lifecycle, failure recovery, idle shutdown, and P8 behavior.
- [ ] Run the requested SDK and macOS validation, write the report, and open the PR.

## Current Architecture and Scope

`TotalCrossSDK/src/main/java/totalcross/ui/image/ImagePreparationScheduler.java`
owns one static FIFO, pending registry, bounded ready metadata list, and active
request. `activateNextLocked()` marks exactly one request active. The current
`start(Work)` starts one new Java thread per activated request. That thread
performs detached preparation, marks the request `WAITING_ADOPTION`, and posts
`finishOnUi` through `MainWindow.runOnMainThread`. Only UI completion clears
`active`, invokes callbacks, and activates the next FIFO entry.

Each immutable `ImagePreparationRequest` captures the effective
`ImageRuntimePolicy`; request equivalence already includes that policy by
identity. `ImageRuntimePolicy` already has a typed `PrefetchWorkerPolicy` whose
only value is `LEGACY_PER_ENTRY_THREAD`. Generic Semaphore support is already
present in `Semaphore4D` and its native mapping; P9 must consume it without
changing its supported API or native ABI.

P9 changes only how an already-active `Work` is executed and awakened. Keep the
single process-wide `QUEUE`, `PENDING`, `READY`, and `active`, with limits 128
pending and 16 ready metadata entries. Do not alter request discovery,
identity, deduplication, decode eligibility, ownership, adoption, diagnostics,
or callback semantics. PNG preparation remains outside this feature.

## Plan of Work

### Milestone 1 — policy boundary

Add exactly `SEMAPHORE_PROCESS_WORKER` to the internal enum, retaining
`LEGACY_PER_ENTRY_THREAD` as the default and in the runtime report. Provide a
test-only way to install the internal mode and reset it after each test; keep
that fixture out of deployable artifacts. Scheduler dispatch must consult the
captured request policy, not a mutable global selector.

Acceptance: policy/default tests prove both enum values, unchanged default
description, and no application-facing selector or artifact leak.

### Milestone 2 — scheduler worker and lifecycle

Keep activation and FIFO decisions under the scheduler lock. Legacy work keeps
the per-entry thread. Semaphore-mode work lazily creates one process worker and
releases one zero-initialized wake semaphore permit for each newly activated
work item. The worker acquires uninterruptibly, reads only the active
Semaphore-mode work under the lock, performs the existing detached preparation,
posts existing UI adoption, then blocks again. It never drains the FIFO itself
or starts a second decode while adoption is pending.

Track an internal `NOT_STARTED`, `RUNNING_OR_BLOCKED`, or `SHUTDOWN` lifecycle.
Start and publication happen under the scheduler lock. Start failure clears
partial state and routes the active request to the existing transient-failure
terminal path so a later explicit request may retry. Test shutdown requires an
idle scheduler, sets shutdown, releases the semaphore, and waits for worker exit
without polling, sleeps, interruption, or deprecated thread controls. Reset
must allow a later test to start a fresh worker.

Acceptance: deterministic tests prove lazy creation, one process worker over
several FIFO requests, a blocked idle worker, one active request through UI
adoption, exact wake behavior, start-failure retry, and clean shutdown/reset.

### Milestone 3 — P8 integration regressions

Exercise the Semaphore policy through the existing request and adoption paths.
Preserve equivalent-request deduplication, UI-thread callback delivery,
callback-exception progress, and callback reentrancy. Verify valid ready work,
already-decoded work, prefetch-ineligible input, and cached deterministic
failure do not start or wake the worker. Run stale target/pipeline/scale,
synchronous decode race, deterministic failure, transient retry, and queue-bound
coverage in both modes where applicable. PNG must remain
`NOT_PREFETCHABLE`.

Add a focused non-brittle source-structure guard against sleep/polling in the
Semaphore work-discovery path. Do not add sleep modes, poll counters, wake
metrics, diagnostics IDs, or production forcing controls. Keep diagnostics
domain order and ID ranges unchanged.

Acceptance: focused tests prove P8 outcomes and the absence of scheduler
polling, and test fixtures do not leak into application SDK artifacts.

### Milestone 4 — deployed compatibility and final gate

Add a concise macOS smoke that internally forces the Semaphore policy and checks
multiple JPEG requests, one serialized worker, UI callbacks, FIFO/adoption,
draw reuse, captured-source behavior, stale/retry, idle blocking, and shutdown.
At the end of the relevant milestones, run the requested generic Semaphore,
Q/P8, P5, P4 COMPACT/STANDARD and directly affected P6/P7 smokes. At the final
SDK gate, run focused diagnostics-OFF and diagnostics-ON tests, then the
specified broad SDK tasks. At the final native gate, build macOS ARM64 `tcvm`
and `Launcher` Release and run applicable deployed smokes. Do not locally build
Windows, Android, Linux, WinCE, or iOS.

Before push, fetch `origin`, rebase if its master advanced, rerun affected
checks, verify six logical commits and clean status, and run
`git diff --check origin/master...HEAD`. Open the requested PR against `master`
and require a fresh GitHub Merge Flow with all enabled lanes passing. Never
merge the PR.

Acceptance: all applicable focused and final-gate checks pass, the PR targets
`master`, CI reflects the final head, and no default-policy or performance claim
is made.

## Decision Log

- Decision: retain the legacy worker as the production default and add the
  Semaphore mode as an internal alternative.
  Rationale: P9 is a scheduler-execution experiment; existing policy and P8
  semantics remain the compatibility boundary. Date: 2026-10-01.
- Decision: keep activation, pending ownership, and terminal adoption in the
  existing scheduler; let the process worker execute only the active request.
  Rationale: this preserves one active request through UI adoption and the
  established FIFO order. Date: 2026-10-01.
- Decision: use an isolated worktree for the clean feature branch.
  Rationale: the original checkout contains unrelated local edits and artifacts;
  resetting it would destroy user data. Date: 2026-10-01.

## Validation and Acceptance

Use the smallest check that proves each implementation slice, then complete the
explicit final gate. Focused SDK tests run with diagnostics both disabled and
enabled. Final SDK tasks are `test`, `test -PruntimeDiagnostics=true`,
`artifactContentTest`, `compileSmokeTestJava`, and `dist -x test`. Run the
copyright header validator for changed files and `git diff --check` after each
logical change. At the final native milestone, build macOS ARM64 `tcvm` and
`Launcher` Release and run directly relevant deployed smokes. The requested
GitHub Merge Flow is required for completion. Do not run unrelated platform
builds or performance benchmarks.

## Risks and Open Questions

- The worker depends on the already-integrated JavaSE and native Semaphore
  paths; a real defect there would be a separate prerequisite and is not a
  reason to reconstruct historical generic Semaphore changes.
- UI adoption requires a live `MainWindow`; preserve the current abandon/release
  behavior when none exists.
- Deployed macOS smokes require the repository's native runtime and launcher
  artifacts. Their availability must be checked at the final native milestone.
- GitHub Merge Flow availability and status checks depend on repository CI and
  permissions; report the exact result if external status prevents completion.

## Idempotence and Recovery

All implementation and commits occur on `feat/image-prefetch-worker` in its
own worktree. The original checkout's edits, generated artifacts, and logs are
out of scope and must remain untouched. The feature branch starts from
`origin/master`; do not continue or replay historical Semaphore/P8 branches.
Build outputs and logs stay untracked/ignored. Tests must shut down the
process worker in `finally` and reset policy/scheduler fixtures even after
failure. If master advances before push, rebase the feature branch and rerun
affected checks; do not merge master into it.

## Outcomes & Retrospective

Pending implementation and final validation. The completion summary belongs in
`.agent/reports/image-prefetch-worker.md`.
