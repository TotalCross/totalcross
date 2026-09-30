<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Build the generic runtime diagnostics foundation

This ExecPlan follows `.agent/PLANS.md` and `AGENTS.md`.

## Purpose / Big Picture

Add a small immutable observation API and private metric machinery that can
collect Java-owned and native-owned counters, gauges, and timers through one
snapshot model. The native side supports single and batched reads plus selective
reset. Optional instrumentation is compiled out by default and each optional
runtime group gates collection before metric-only work. The API observes runtime
activity only; it does not select runtime policy or configuration.

This is a foundation change independent of any unmerged RuntimeConfiguration
API. It does not add Image, Rendering, Prefetch, or Scheduling metric catalogs,
tracing, or event timelines. Only bounded synthetic foundation observations are
used to exercise collection paths.

## Working Set and Resume Protocol

This task deliberately uses a single plan file. The attached instructions forbid
`.agent/state`, raw logs, result archives, packages, and benchmark artifacts.
Update this plan at logical checkpoints and use `git log` plus focused tests to
resume. Keep build output outside the repository in `/tmp` and report its path.

Relevant stable interfaces are `TotalCrossVM/src/nm/NativeMethods.txt`,
`TotalCrossVM/src/nm/NativeMethods.h`, and
`TotalCrossVM/src/init/nativeProcAddressesTC.c`; these define native signatures,
prototypes, and registration. `TC_ENABLE_SEMAPHORE_TEST_DIAGNOSTICS` in
`TotalCrossVM/CMakeLists.txt` and the matching guarded source/registration are
the existing pattern for excluding optional native diagnostics. Read the exact
sections only when changing or validating those boundaries.

## Progress

- [x] (2026-09-30) Updated `origin/master` and created isolated branch
  `feat/runtime-diagnostics` at `7d50c12c0675fbef13e74c54e59b704c04c08050`.
- [ ] Commit this plan first, then implement the internal catalog, snapshot,
  delta/reset semantics, and focused Java tests.
- [ ] Add and validate the guarded generic native bridge, including optional
  build-on/runtime-group-off and normal build-off configurations.
- [ ] Finish boundary review, focused validation, SDK distribution build, macOS
  native validation, and commit `.agent/reports/runtime-diagnostics.md`.
- [ ] Push the branch and open the requested PR against `master`; do not merge.

## Current Architecture and Scope

The SDK stores Java source under `TotalCrossSDK/src/main/java`, unit tests under
`TotalCrossSDK/src/test/java`, and converted-runtime smoke apps under
`TotalCrossSDK/src/smokeTest/java`. Native declarations are described in
`TotalCrossVM/src/nm/NativeMethods.txt`; the matching prototype and registration
files are maintained in the same VM tree. CMake controls VM-wide compile-time
options and defines them on `tcvm`.

Private descriptors own each metric's stable internal ID, domain, kind, owner,
and optional group. Java values are read in Java. Native values cross one guarded
bridge; a snapshot batches requested native IDs. Public declarations must not
expose numeric IDs, native protocol values, string metric keys, reset masks, or
registry/provider details. Domain and kind names may describe returned
observations, but no feature-specific public metric catalog is added here.

Use the governing classifications `PRODUCTION_SAFE`, `RUNTIME_OPTIONAL`,
`COMPILE_TIME_ONLY`, `TEST_ONLY`, and `DROP` when deciding whether future
instrumentation belongs in this foundation. Keep only the minimum synthetic
foundation metrics needed by tests. Do not migrate historical feature metrics or
test hooks.

## Plan of Work

### Milestone 1 — private metadata and snapshot semantics

Define the internal domain/kind/owner/group model and a private descriptor table
with non-public stable IDs. Add the small public `RuntimeDiagnostics` and
`RuntimeDiagnosticSnapshot` surface, keeping provider plumbing and reset protocol
internal. The snapshot is immutable and carries enough internal set/epoch
metadata to reject incompatible deltas without exposing those values.

Implement cumulative counter and timer deltas. Treat live gauges as absolute
observations and leave them untouched by reset. Peak gauges are absolute by
default. A selective reset affects only cumulative metrics in selected groups,
advances compatibility epoch state, and is intended for quiescent test or
benchmark boundaries. Normal production snapshots and deltas do not require a
global reset.

Add focused tests for metadata, Java-owned reads, mixed snapshots once the bridge
lands, immutability, valid counter/timer deltas, gauge behavior, incompatible
sets/epochs, selective reset, and live gauges remaining unchanged.

### Milestone 2 — guarded native batch bridge and cost gates

Add internal native `readMetric(metricId)`, `readMetrics(metricIds, values)`,
and `resetMetrics(groupMask)` operations with private protocol IDs. Preserve
Java-owned collection without native calls. Batch native reads per snapshot.
Provide narrow synthetic Java/native foundation metrics for bridge tests; do not
use or preserve historical benchmark IDs as the protocol.

Add a default-off CMake option for optional diagnostics. With it off, optional
native storage, update helpers, bridge implementation, and registration are
absent. With it on, runtime group gates are checked before clocks, allocation,
metric-only arithmetic, synchronization, registry lookup, or native crossing.
Disabled snapshot groups return before arrays or native calls. Keep all IDs,
group masks, reset operations, and synthetic update hooks internal or test-only.

Add focused checks for batched native reads, native-owned and mixed snapshots,
disabled-group early exit, compile-time-off registration/storage absence, and
compile-time-on with the runtime group disabled. Snapshot reads must not alter
runtime behavior.

### Milestone 3 — API boundary, review, and delivery

Run focused SDK tests and artifact-boundary validation for any public classes.
Build the SDK distribution and the native macOS VM at the end of their related
milestones only. Do not build Android, Windows, Linux, WinCE, or iOS. Audit the
diff for accidental policy changes, per-pixel diagnostics branches, public
protocol values, oversize new files, and unrelated edits.

Write the required final factual handoff to
`.agent/reports/runtime-diagnostics.md`. Keep each newly created file at or
below 20 KB and about 600 lines. Commit coherent slices, wrap commit bodies at
80 columns, and commit all task-created artifacts. Then push
`feat/runtime-diagnostics` and open a PR titled `feat(runtime): add generic
runtime diagnostics foundation` against `master`; leave it unmerged.

## Decision Log

- Decision: Start from the fetched latest `origin/master` in a new worktree.
  Rationale: The shared checkout contains many unrelated untracked local files;
  an isolated worktree preserves them while allowing a clean branch.
  Date: 2026-09-30.
- Decision: Keep feature catalogs, tests hooks, fault injection, and event
  timelines out of this PR; use only generic synthetic observations.
  Rationale: The attached task requires an independently useful mechanism
  without depending on the separate RuntimeConfiguration work or importing
  feature-specific policy into diagnostics.
  Date: 2026-09-30.
- Decision: Keep IDs, metric keys, masks, provider plumbing, and reset protocol
  private; batch native reads and read Java metrics directly in Java.
  Rationale: The public boundary should remain small and the bridge should not
  become a stable protocol or add needless native crossings.
  Date: 2026-09-30.

## Validation and Acceptance

Use the smallest checks that prove each slice. At functional completion, run
focused tests for every behavior listed in the attachment: metadata, Java and
native ownership, mixed reads, immutable snapshots, compatible/incompatible
deltas, gauge semantics, selective reset, live gauge preservation, early
runtime-group exit, compile-time OFF exclusion, compile-time ON with a disabled
runtime group, and native batching. Run artifact-boundary validation if public
classes change, `git diff --check origin/master...HEAD`, and the SDK distribution
build plus native macOS validation for the bridge. Keep full tool output outside
the repository; do not add test logs to commits.

The attachment explicitly excludes Android, Windows, Linux, WinCE, and iOS
builds. Do not run them. Defer other platform builds because this foundation's
required native validation is macOS and the change must remain isolated from
platform behavior. No benchmark is needed because no production hot path or
metric catalog is being migrated.

## Risks and Open Questions

- The generated native signature/prototype artifacts must agree with the
  compile-time registration boundary; verify default builds do not advertise a
  callable optional bridge at runtime.
- The public snapshot must remain useful without exposing keys or numeric IDs.
  Resolve this with a typed, immutable observation view and internal descriptors,
  without adding a stable feature catalog.
- Reset and snapshot races must not corrupt live gauges or silently validate
  deltas across different metric sets or epochs.
- CMake/macOS dependencies may be unavailable in the isolated worktree; if so,
  record the exact blocked command and reason and complete all independent checks.

## Idempotence and Recovery

All changes are in `/Users/flsobral/repos/totalcross-runtime-diagnostics` on
`feat/runtime-diagnostics`. The original shared checkout and its untracked
files are outside the task worktree and must remain untouched. If the worktree
is interrupted, read this plan, inspect the latest commit and scoped diff, then
continue from the earliest unchecked milestone. Never reset or clean the shared
checkout, delete generated caches, or stage paths outside this worktree.

Use separate `/tmp` build directories for default and diagnostics-enabled
macOS configurations. A failed build may be retried from the same directory
unless CMake reports stale configuration; do not run `clean` by default.

## Outcomes & Retrospective

Pending implementation. At completion, summarize delivered public classes,
compile/runtime gates, native bridge shape, snapshot/reset behavior, validation,
and explicitly deferred feature-specific metrics here and in the editorial
report.

## Revision Note

- Initial plan, authored before source changes, to satisfy the required first
  commit and bound implementation/validation to the attached task.
