<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Stabilize headless preview startup

This ExecPlan follows `.agent/PLANS.md` and `AGENTS.md`.

## Purpose / Big Picture

Make SDK preview sessions start without constructing AWT top-level windows,
surface the original application initialization failure instead of a later
`EventLoop(null)` error, and preserve fresh static window state across two
sequential preview sessions. A developer can verify the result through the
focused `LauncherPreviewLifecycleTest` in normal and forced-headless test JVMs.

## Progress

- [x] 2026-08-12: Read repository policy, inspected the preview lifecycle, and
  ran the pre-fix lifecycle test in normal and nominally forced-headless modes.
  Both passed because the Gradle test task does not explicitly forward the
  command-line AWT property to its forked worker.
- [x] 2026-08-12: Forwarded the explicit AWT property to the test worker. The
  pre-fix forced-headless run then reproduced `NullPointerException: target`
  after a swallowed `HeadlessException` from `AlertBox.<init>`.
- [x] 2026-08-12: Added fail-fast propagation and confirmed the same run exposed
  `Failed to initialize preview MainWindow: ...` with `HeadlessException` as
  its cause before changing alert construction.
- [x] 2026-08-12: Implemented alert ownership, initialization propagation, and the post-init
  MainWindow invariant.
- [x] 2026-08-12: Added deterministic headless and failing-constructor coverage without
  weakening sequential lifecycle assertions.
- [x] 2026-08-12: Normal, forced-headless, and related preview selectors passed;
  the six changed files passed focused header and whitespace validation.
- [x] 2026-08-12: Created and inspected the requested cohesive commit without
  pushing.

## Current Architecture and Scope

`tc.simulator.Launcher.startPreviewFrames` creates a facade `Launcher`; its
private `start(true)` resets `MainWindow` preview state, creates the internal
simulator `Launcher`, calls `RuntimeState.init()`, and then calls `startApp()`.

`RuntimeState.init()` currently constructs `StreamTypes.AlertBox` before the
requested `MainWindow`. `AlertBox` extends `java.awt.Frame`, so construction is
invalid in a headless JVM. The legacy outer `catch (Exception)` prints and
returns, allowing `RuntimeState.startApp()` to construct `EventLoop` with a null
target. `EventLoop` correctly enforces its non-null target with
`Objects.requireNonNull` and is not in scope for weakening.

`StreamBridge.alert` assumes an `AlertBox` exists after startup. It must retain
desktop alert UI when available and report to the console when no alert UI is
owned by preview or headless operation.

Scope is limited to the SDK simulator lifecycle, its Gradle test-property
plumbing, and focused JUnit coverage. No VM, platform, packaging, publishing,
or unrelated simulator refactor is included.

## Plan of Work

First, explicitly forward `java.awt.headless` from the Gradle invocation to the
forked test worker and rerun the existing forced-headless command before runtime
changes. This distinguishes a real reproduction from the earlier daemon-only
property setting and records the first visible pre-fix failure.

Second, make `AlertBox` creation depend on both non-preview ownership and a
graphical environment. Put the decision behind a small package-visible pure
helper so the headless branch is deterministic in JUnit without changing the
global AWT cache. Make `StreamBridge.alert` use console error reporting whenever
the UI object is absent, including after application startup.

Third, preserve legacy non-preview exception handling but wrap preview
initialization failures in an `IllegalStateException` that names the requested
MainWindow and retains the original throwable as its cause. Add an explicit
check immediately after `launcher.init()` and before `launcher.startApp()` so a
null preview `MainWindow` cannot reach `EventLoop` even when there was no caught
initialization exception.

Fourth, extend `LauncherPreviewLifecycleTest` with a deterministic alert-decision
and real preview-startup test, plus a deliberately failing MainWindow
constructor. Keep the existing two-session test and its real frame, dimensions,
window identity, clean stop, and state-isolation assertions intact. Prove the
failing path has the original constructor exception in its cause chain and no
event loop.

Finally, run the user-requested normal, forced-headless, and related preview
selectors. Run `git diff --check` and the focused copyright validator for every
changed first-party file, inspect the scoped diff, and create one cohesive
`fix(sdk): support headless preview startup` commit without pushing.

## Decision Log

- Decision: Keep `EventLoop` unchanged and enforce validity before construction.
  Rationale: A null application target is invalid; the visible NPE is a useful
  invariant, not the underlying lifecycle defect.
  Date: 2026-08-12.

- Decision: Use a pure alert-creation decision plus actual forced-headless test
  JVM validation, rather than toggling `java.awt.headless` within JUnit.
  Rationale: AWT caches headless state, so in-process property mutation can be
  order-dependent and contaminate unrelated tests.
  Date: 2026-08-12.

- Decision: Preserve the legacy print/exit behavior outside preview mode.
  Rationale: The requested compatibility boundary is preview fail-fast behavior;
  changing desktop or applet failure semantics would be unrelated risk.
  Date: 2026-08-12.

## Validation and Acceptance

This is Level 2 functional-commit validation. Acceptance requires:

- the exact lifecycle selector passes normally and with
  `-Djava.awt.headless=true`;
- forced headless is actually visible to the test JVM;
- preview owns no `AlertBox`, creates the requested `MainWindow`, emits the
  requested frame, and stops cleanly;
- the second session uses a distinct application/window and emits its own frame;
- a failing constructor produces an initialization exception naming the class,
  retains the original cause, and starts no event loop;
- all related preview selectors pass;
- changed-file whitespace and copyright validation pass.

Full SDK distribution, Android, iOS, VM, packaging, publishing, and deployment
validation are deliberately excluded by the user and do not exercise this
focused lifecycle change.

## Risks and Open Questions

The initial forced-headless Gradle command passed locally before property
forwarding. The isolated rerun after only test-worker forwarding reproduced the
CI path and confirmed the unconditional AWT `Frame` construction as root cause.

Constructor failures can leave partially created static `MainWindow` state.
Normal preview startup already resets this state before each session; failure
test cleanup remains defensive and must not become required for a subsequent
valid startup.

## Idempotence and Recovery

All test commands use `--rerun-tasks` and are safe to repeat. Full logs go under
`/tmp/headless-preview-*.log`. Existing untracked `.agent` artifacts and SDK
generated/local files are user-owned and remain untouched. Scoped Git inspection
will keep them out of the requested commit.

## Outcomes & Retrospective

Preview startup no longer constructs `AlertBox`, and headless non-preview
initialization also avoids that AWT top-level window. Missing alert UI falls
back to stderr. Preview initialization failures name the requested MainWindow
and retain their original cause, while an explicit post-init invariant prevents
a null MainWindow from reaching `EventLoop`.

The lifecycle suite now has three focused tests: the original two-session state
isolation test, a deterministic alert-decision and real frame/stop test, and a
failing-constructor test proving cause retention and absence of an event loop.
Normal and forced-headless lifecycle runs passed, as did the related five-test
preview selector. No broader builds were run because the user explicitly
excluded them and they do not add evidence for this simulator-only change.
