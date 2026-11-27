<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Investigate Android Skia and EGL surface lifecycle

This ExecPlan follows AGENTS.md and .agent/PLANS.md.

## Purpose / Big Picture

Establish whether current master still reproduces the Android native graphics
crash tracked by TotalCross issue 563, prove the resource-lifetime cause with
local ARM64 emulator evidence, and make a narrow Android-only correction if
the evidence supports it.

## Working Set and Resume Protocol

Read CHECKPOINT.md first when resuming. FINDINGS.md contains historical and
current observations; DECISIONS.md contains decisions that constrain future
work; TEST-MATRIX.md tracks cases; evidence/index.md points to compact logs,
provenance, and results. Diagnostic-only patches belong in diagnostic-patches.
FINAL-REPORT.md is completed only at final consolidation.

## Progress

- [x] (2026-11-27) Establish the current master baseline and inspect existing
  investigation evidence. See CHECKPOINT.md and FINDINGS.md.
- [ ] Complete Milestone 1 with a local API 30 baseline reproduction or a
  documented blocker, then commit and push the evidence.
- [ ] Milestone 2: instrument only the relevant Android EGL and Skia lifecycle.
- [ ] Milestone 3: causally validate the lifecycle ordering before production
  changes; stop for review before implementation.
- [ ] Milestone 4: implement the smallest justified Android correction and
  focused regression coverage; stop for review.
- [ ] Milestone 5: run the authorized local ARM64 emulator matrix; stop for
  review.
- [ ] Milestone 6: reconcile evidence, finish FINAL-REPORT.md, and verify the
  remote branch.

## Current Architecture and Scope

Android nativeSurfaceChanged reaches androidApplySurfaceChange in
TotalCrossVM/src/nm/ui/backend/graphics/gles/gfx_Graphics_c.h. On a replacement
native window, the current code destroys EGL state and releases the old native
window before initializing a replacement EGL context. initGLES makes the new
context current and calls initSkia; initSkia first releases the prior Skia
screen surface. This matches the historical 7.3.0 source sequence, but source
ordering and a crash stack do not prove causality.

Scope is Android Skia/EGL surface recreation only. Permission recovery,
SQLite, other platforms, graphics optimization, physical devices, and remote
runtime tests are excluded by the user request.

## Plan of Work

1. Preparation and baseline reproduction: use current origin/master, the
   existing customer and diagnostic applications, and installed ARM64 API 30.
   Record actual permission state, application completion, rendering, and
   native crash evidence.
2. Lifecycle instrumentation: correlate surface callbacks, EGL identities,
   Skia resources, and owning threads with ordered event identifiers.
3. Causal validation: compare equivalent baseline and diagnostic teardown
   ordering runs. Do not change production behavior before the evidence gate.
4. Production correction: preserve the graphics thread and window ownership
   contracts; release valid-context resources before EGL teardown and handle
   lost contexts using supported Skia APIs.
5. Functional validation: run the specified local ARM64 emulator matrix and
   focused graphics regression application.
6. Final review: audit scope, commits, evidence, tests, and remote HEAD.

Every milestone ends with a commit and remote synchronization, followed by a
pause for explicit user authorization to continue.

## Surprises & Discoveries

- The requested local and remote branch already exists at the same SHA as
  origin/master. The isolated worktree is clean, so it is being reused without
  moving or rewriting the branch.
- Current master declares SDK version 7.2.2. The historical crash evidence
  used the official 7.3.0 runtime; this campaign must build from the current
  source baseline.
- The current Android EGL and Skia lifecycle source is unchanged from the
  inspected 7.3.0 revision. The only difference in the GLES backend header is
  an iOS-only helper.
- The installed API 30 ARM64 system image boots with host GPU acceleration
  when requested on the emulator command line, although its AVD config has
  GPU acceleration disabled.

## Decision Log

- Reuse the existing clean branch fix/android-skia-egl-surface-lifecycle at
  origin/master SHA 52a947b5295321d7350350bd618c55fb26304c00. Recreating or
  moving an already published branch would risk overwriting existing work.
- Build runtime artifacts from this source baseline. Historical 7.3.0 APKs
  are evidence only and cannot establish current master behavior.
- Runtime tests remain local, hardware-accelerated ARM64 emulators only.

## Validation and Acceptance

Milestone 1 requires a current-source SDK and ARM64 runtime, API 30 customer
and diagnostic application runs, permission and completion assertions, and
captured native logs. Later validation stays local and follows the user's
specified emulator matrix. Do not run CI, physical-device, non-Android, or
performance tests.

## Risks and Open Questions

- The old harness builds against the pinned 7.3.0 distribution. Determine the
  smallest adaptation needed to use the current-source SDK without changing
  the customer Java operations.
- The primary EGL/Skia lifetime hypothesis remains unproven.
- Existing API 30 AVD metadata reports a missing image despite the installed
  image being present; emulator boot and renderer were verified directly.

## Idempotence and Recovery

All source changes and commits stay in the isolated worktree. The main checkout
contains unrelated untracked files and must remain untouched. Keep APKs,
emulator state, dependency caches, and build output out of Git. If an emulator
or build fails, preserve logs and classify the result before retrying.

## Outcomes & Retrospective

Milestone 1 is in progress. No current-master runtime result or production
change is recorded yet.

## Revision Note

Initial plan. Future updates are limited to milestone boundaries and material
changes in technical direction.
