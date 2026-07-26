<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda
SPDX-License-Identifier: LGPL-2.1-only
-->

# Reduce Android rotation rendering latency without destabilizing the graphics lifecycle

This ExecPlan follows `AGENTS.md` and the ExecPlan standard defined in `.agent/PLANS.md`. It is a living document. Keep `Progress`, `Surprises & Discoveries`, `Decision Log`, `Outcomes & Retrospective`, and `Revision Note` current as work proceeds.
All implementation work belongs on `fix/419-sluggish-interface-rendering-when-rotating-screen`. A remote comparison performed on 2026-07-25 confirmed that this branch and `master` were identical at commit `b7c25d7762aa326bf0c3a9bd384c173efad006da`. Recheck this relationship before each milestone because `master` may advance while the plan is active.
The associated issue is `TotalCross/totalcross#419`, titled `Sluggish interface rendering when rotating screen`. It is open, labeled `bug`, and assigned to milestone 7.3.0. The issue reports visible rendering delay on Android, especially while application code performs processing. The problem was not noticeable on Windows and had not been tested on iOS when the issue was filed.

## Purpose / Big Picture

After this change, rotating an Android device must produce one correct final layout and a useful first frame without a perceptible pause, even when the TotalCross event thread is under moderate reproducible load.
The fix must remove only work proven to be redundant. It must not introduce a black, green, white, stale-size, partially laid-out, or frozen frame; lose textures or image contents; skip the final resize; break keyboard shifting or safe-area handling; or change minimize, restore, screen-off, screen-on, and auxiliary-Activity behavior.
A developer must be able to demonstrate the result on the same device and workload by comparing structured trace summaries before and after the change. The comparison must include time from the first accepted rotation callback to the first correct presented frame, resize tasks scheduled and executed, `nativeInitSize`, EGL recreation, `initSkia`, `screenChange`, full repaint, and first swap counts.
The implementation must advance from low-risk, reversible changes to structural graphics-lifecycle changes. Instrumentation, a reproducible harness, exact duplicate removal, and stale-task coalescing come before changes to repaint ownership or EGL/Skia ownership.

## Working Set and Resume Protocol

When checked into the repository, keep the active plan at:

    .agent/plans/419-sluggish-interface-rendering-when-rotating-screen.md

Use these supporting files only when they simplify resumption:
- `.agent/state/419-sluggish-interface-rendering-when-rotating-screen.md` is rewritten rather than appended. It is the first normal read and records the active milestone and slice, last logical commit, active paths, next concrete action, focused validation completed, deferred validation and reason, active decisions, blockers, deliberately out-of-scope files, device used, and a resume command.
- `.agent/evidence/419-sluggish-interface-rendering-when-rotating-screen.jsonl` is append-only. Each compact entry records timestamp, revision, milestone and slice, device and Android version, workload, command or manual procedure, status, p50 and p95 when applicable, counter summary, artifact paths, and limitation.
- `.agent/archive/419-sluggish-interface-rendering-when-rotating-screen-history.md` stores completed milestone detail, rejected alternatives whose rationale remains useful, and retired revision notes. It is not read by default.
- `.agent/reports/419-sluggish-interface-rendering-when-rotating-screen-editorial.md` is the factual handoff updated at important milestone boundaries and final completion, not after every test run.
Do not copy the same logs, videos, benchmark tables, or conclusions into all supporting files. Keep raw `logcat`, traces, videos, and generated summaries outside the active plan and reference them by path.
On resume, read the state file first. If it does not exist, read `Progress`, the active milestone under `Plan of Work`, `Decision Log`, `Validation and Acceptance`, and `Risks and Open Questions`. Do not reread the complete plan, `AGENTS.md`, `.agent/PLANS.md`, old logs, videos, or the evidence index unless the next action requires them.
Run these commands from the repository root before editing:

    git fetch origin
    git switch fix/419-sluggish-interface-rendering-when-rotating-screen
    git pull --ff-only
    git rev-parse HEAD
    git merge-base --is-ancestor origin/master HEAD
    git diff --stat
    git status --short -- \
      TotalCrossVM/android/app/src/main/java/totalcross/Launcher4A.java \
      TotalCrossVM/android/app/src/main/java/totalcross/android/Loader.java \
      TotalCrossVM/src/event/android/event_c.h \
      TotalCrossVM/src/nm/ui/android/gfx_Graphics_c.h \
      TotalCrossVM/src/nm/ui/GraphicsPrimitives_c.h \
      TotalCrossSDK/src/main/java/totalcross/ui \
      scripts \
      .agent

Do not use `git reset --hard`, `git checkout --`, force push, or broad cleanup to resolve divergence. Preserve unrelated local changes and record any baseline change that invalidates a hypothesis.

### Token-efficient execution policy

The executor must operate in token-efficient mode, especially during tests:
1. Read only files and line ranges needed for the active slice. Use `rg` and scoped `git diff` instead of repeatedly dumping complete source files.
2. Save verbose output to files. Return only the command, exit status, concise summary, relevant error lines, and artifact path.
3. Parse rotation traces with a small deterministic summarizer. Review aggregate output and anomalous generations first; open raw logs only when the summary cannot explain a failure.
4. Do not paste complete `logcat`, Gradle, CMake, Ninja, or Android build output into the conversation or plan.
5. Use three rotations for an ordinary implementation slice, ten rotations for a milestone checkpoint, and twenty rotations per scenario only for the baseline and final acceptance. Escalate earlier only when variability makes the smaller sample inconclusive.
6. Run one focused device and one workload during a slice. Run the broader device, Android-version, keyboard, and lifecycle matrix only at milestone boundaries that affect those contracts.
7. Do not rerun device performance tests after comment-only, formatting-only, plan-only, or test-description changes. Use `git diff --check`.
8. Preserve one baseline artifact set. Do not regenerate the same baseline after every code edit.
9. Do not extract or inspect every video frame. Record the video path and a concise manual observation. Inspect individual frames only when trace data and visual behavior disagree.
10. Use Perfetto, `simpleperf`, or broad system traces only when structured
    rotation tracing cannot identify the remaining delay. They are escalation
    tools, not default validation.

## Progress

- [x] (2026-07-16T21:17:06Z) Issue #419 was filed describing sluggish Android rendering during rotation, especially while processing is active.
- [x] (2026-07-21T23:00:00-03:00) Static analysis identified that `surfaceChanged()` schedules resize work on the TotalCross event thread.
- [x] (2026-07-21T23:00:00-03:00) Static analysis identified a possible duplicate Skia initialization path through `nativeInitSize()` and `screenChange()`.
- [x] (2026-07-21T23:00:00-03:00) Static analysis identified an immediate `repaintActiveWindows()` call in `screenChange()` in addition to the posted screen-change event.
- [x] (2026-07-25T21:00:00-03:00) The target branch was confirmed identical to `master` at commit `b7c25d7762aa326bf0c3a9bd384c173efad006da`.
- [x] (2026-07-25T21:00:00-03:00) Current-source inspection reconfirmed the Java, JNI/EGL, event, screen-surface, and repaint paths described below.
- [x] (2026-07-25T21:00:00-03:00) This plan was rewritten in English using the `totalcross-depot-tools/.agent/PLANS.md` structure and an explicit token-efficient test policy.
- [x] (2026-07-25T22:30:00-03:00) Added disabled-by-default Java/JNI/EGL/Skia rotation tracing with generation association, stage counters, first-swap summaries, and sentinel operations left outside the trace-generation path.
- [x] (2026-07-25T22:30:00-03:00) Added `scripts/diagnostics/summarize-rotation-trace.py`; a synthetic complete generation produced JSON with p50/p95, stage counts, and no missing-stage warnings.
- [x] (2026-07-25T22:30:00-03:00) `TotalCrossVM/android/./gradlew :app:assembleStandardDebug --console=plain --warning-mode=none` passed after one instrumentation-only pointer fix; full output is in `/tmp/tc-rotation-m1-build.log`.
- [x] (2026-07-25T19:05:00-03:00) With explicit authorization, uninstalled the stale `totalcross.android` package from emulator `emulator-5554` and installed the generated test APK through the SDK deploy flow.
- [x] (2026-07-25T19:25:00-03:00) Captured the reproducible idle and deterministic event-thread-load baselines on Android 14 using the temporary `RotationTest` application, 24 alternating orientation requests per workload, structured traces, and `screenrecord` evidence.
- [x] Milestone 1: complete the low-noise tracing and baseline checkpoint; no optimization milestone was started.
- [x] (2026-07-25T19:28:00-03:00) Added a package-private request coordinator and focused JVM coverage for identity, dimensions, orientation, interactive state, keyboard category, lifecycle category, and newest-request preservation.
- [x] (2026-07-25T19:34:00-03:00) Generated the updated Standard Release AAB, deployed `RotationTest` through the SDK template, and completed the focused Android smoke run with 16 idle and 10 event-thread-load rotations; all accepted callbacks completed and no stale task was observed.
- [x] Milestone 2: drop only exact duplicate rotation requests while preserving lifecycle and surface identity changes.
- [x] (2026-07-25T19:39:00-03:00) Extended accepted requests into immutable generation snapshots, rejected non-positive sentinel dimensions from resize coordination, and guarded the event-thread runnable with generation and `Surface.isValid()` checks before JNI.
- [x] (2026-07-25T19:43:00-03:00) The rapid-load probe discarded three stale generations before `native_init_size_entered`; the closure runs accepted ten callbacks per workload and completed the newest stable generation with final dimensions `1080x2220`.
- [x] Milestone 3: coalesce obsolete queued generations before JNI while preserving the newest accepted resize.
- [x] (2026-07-25T19:56:00-03:00) Measured the current rotation path: closed-keyboard runs requested one SIP hide and one close event per completed generation, while native immediate repaint produced two swaps per generation.
- [x] (2026-07-25T20:03:00-03:00) Removed redundant rotation SIP calls when the captured request has a closed keyboard, preserving one hide/close pair for a request captured with the keyboard visible.
- [x] (2026-07-25T20:10:00-03:00) A/B evidence showed that suppressing the native immediate repaint leaves one Java-handler repaint/swap and correct screen-recorded output; the temporary switch was removed and the final Android build/smoke passed with one swap per generation.
- [x] Milestone 4: remove measured SIP and repaint duplication in independently committed slices.
- [x] (2026-07-25T20:20:00-03:00) Milestone 5 gate review found no duplicate or dominant EGL/Skia recreation: M1 and M4 summaries reported at most one `init_skia` per generation and no `window_changes`; no ownership refactor was justified.
- [x] Milestone 5: review EGL/Skia ownership evidence; defer ownership changes because the entry condition was not met.
- [x] (2026-07-25T20:55:00-03:00) Final M6 AAB/deploy flow passed; 20 idle and 20 fixed-load rotations completed on Android 14, with OpenGL screen-recording evidence and no crash/ANR/ClassNotFoundException.
- [x] (2026-07-25T21:05:00-03:00) Keyboard, burst, minimize/restore, screen off/on, auxiliary-Activity return, gesture navigation, and three-button navigation cases were exercised; temporary A/B switches were absent and the old `totalcross.android` package was removed.
- [x] Milestone 6: validate lifecycle and resource recovery, consolidate evidence, and close the plan on the available device.
- [x] (2026-07-25T21:25:00-03:00) Added and executed the requested two-Edit lower-screen keyboard test in the temporary diagnostic app; `Window.shiftY=1804` was computed, but the OpenGL frame left the lower Edit covered by the visible keyboard.
- [x] (2026-07-25T21:35:00-03:00) Regression-tested the same two-Edit app against the pre-plan base `b7c25d776`, M4 SIP `c72ca44a9`, and M4 repaint `5a773cebc`; all three computed `shiftY=1804` and left the lower Edit covered with the keyboard visible, so no shift regression was introduced by an ExecPlan milestone.
- [x] (2026-07-25T21:45:00-03:00) Repeated the two-Edit test with the cached TotalCross SDK 7.2.2 AAB and deploy tool; after compiling the temporary app with Java 8 bytecode, it reproduced `shiftY=1804` with the lower Edit covered by the visible keyboard.
- [ ] M6 follow-up: make the native OpenGL repaint honor the computed `Window.shiftY` for the lower Edit; blocked pending an explicitly authorized runtime fix.

## Current Architecture and Scope

`TotalCrossVM/android/app/src/main/java/totalcross/Launcher4A.java` is the Android `SurfaceView` host. Its `surfaceChanged()` callback detects orientation and size changes, sends keyboard sentinel calls to `nativeInitSize`, stores `lastSurface`, `lastScreenW`, and `lastScreenH`, and invokes `sendScreenChangeEvent()`.
`sendScreenChangeEvent()` schedules a runnable on the TotalCross event thread. The runnable reads the mutable global `lastSurface` and dimensions rather than an immutable callback snapshot. It calls `nativeInitSize`, recalculates display metrics and font height, marks the full dirty rectangle, hides the SIP, posts `SCREEN_CHANGED`, and sends a close-SIP event. Multiple queued runnables can therefore process the same final global state repeatedly.
`TotalCrossVM/src/nm/ui/android/gfx_Graphics_c.h` implements `Java_totalcross_Launcher4A_nativeInitSize`. When the underlying `ANativeWindow` changes, it calls `destroyEGL()` and `initGLES()`. `initGLES()` creates the EGL display, surface, and context, sets the viewport, clears buffers, and calls `initSkia()`.
The same file implements `graphicsCreateScreenSurface()`, which updates pitch and pixel markers and calls `initSkia()` when Skia is enabled. This makes a second Skia initialization possible during the same logical rotation.
`TotalCrossVM/src/event/android/event_c.h` handles `SCREEN_CHANGED`. It updates the global screen dimensions and calls `screenChange()` when startup is complete.
`TotalCrossVM/src/nm/ui/GraphicsPrimitives_c.h` implements `screenChange()`. It updates `Settings` dimensions, marks the whole screen dirty, updates the platform projection, destroys and recreates the screen surface when dimensions changed, posts `SK_SCREEN_CHANGE`, and immediately calls `repaintActiveWindows()`.
The currently observed flow is:

    Android surfaceChanged, possibly more than once
      -> update mutable lastSurface and lastScreenW/H
      -> queue runnable on TotalCross event thread
      -> runnable calls nativeInitSize with current global state
           -> possibly destroy EGL
           -> init GLES
           -> init Skia
      -> post SCREEN_CHANGED
      -> native event handler calls screenChange
           -> update Settings and mark full dirty
           -> graphicsDestroy
           -> graphicsCreateScreenSurface
           -> possibly init Skia again
           -> post SK_SCREEN_CHANGE
           -> repaintActiveWindows immediately
      -> application screen-resize handling and later repaint
      -> first correct swap

The event thread also executes application work. Processing performed there can delay resize runnables and amplify a queue of redundant rotation work.
The sentinel widths `-999`, `-998`, and `-997` passed to `nativeInitSize` have keyboard and lifecycle meanings. They are not ordinary resize requests and must not be deduplicated or coalesced by the resize-generation logic without separate proof.
Initially in-scope files are:
- `TotalCrossVM/android/app/src/main/java/totalcross/Launcher4A.java`;
- focused Android unit tests under `TotalCrossVM/android/app/src/test/java/totalcross`;
- focused device tests under `TotalCrossVM/android/app/src/androidTest/java/totalcross` only if the module already supports them without broad test-infrastructure work;
- `TotalCrossVM/src/event/android/event_c.h`;
- `TotalCrossVM/src/nm/ui/android/gfx_Graphics_c.h`;
- `TotalCrossVM/src/nm/ui/GraphicsPrimitives_c.h`;
- a compact trace summarizer under `scripts/diagnostics` if retained;
- the minimal TotalCross diagnostic application or existing sample used for device reproduction;
- `.agent` supporting files for this plan.
Broad event-thread redesign, non-Android rendering refactors, unrelated keyboard behavior, and generalized graphics-resource architecture are out of scope unless required by measured evidence.

## Plan of Work

### Milestone 1 — Establish a low-noise baseline without changing behavior

The goal is to make every rotation generation observable before optimizing it. At completion, tracing disabled must preserve current behavior and tracing enabled must produce one compact lifecycle summary per accepted rotation.
Add a monotonically increasing rotation generation at the Java boundary and trace these stages with a monotonic clock:

    surface callback accepted
    resize runnable scheduled
    resize runnable started
    nativeInitSize entered and returned
    ANativeWindow changed
    destroyEGL
    initGLES
    initSkia
    SCREEN_CHANGED handled
    screenChange entered and returned
    graphicsCreateScreenSurface
    repaintActiveWindows
    first eglSwapBuffers after the generation

Do not log every frame. Use structured lines such as:

    ROTATION_TRACE generation=12 stage=initSkia width=1080 height=2400 dt_ms=8

Emit one final summary line for each completed generation. Gate all tracing behind a private debug property, build flag, or constant that is false in normal release behavior.
Create or retain a small summarizer that converts raw trace lines into compact CSV or JSON with one row per generation and aggregate p50, p95, counts, stale tasks, and missing-stage warnings. The executor must inspect this summary before opening raw logs.
Use the same diagnostic application and two workloads:
1. idle application;
2. deterministic moderate work on the TotalCross event thread.
A worker-thread load may be recorded as a control, but it is not a substitute for the reported event-thread scenario.
For visual evidence, use Android screen recording. `adb exec-out screencap -p` may produce a white or blank image when the visible content is rendered through OpenGL, so a blank `screencap` is not evidence that the application rendered a blank frame. `adb shell screenrecord` captures the OpenGL output and is the preferred visual artifact:

    adb shell rm -f /sdcard/tc-rotation-baseline.mp4
    adb shell screenrecord --bit-rate 4000000 --time-limit 30 \
      /sdcard/tc-rotation-baseline.mp4
    adb pull /sdcard/tc-rotation-baseline.mp4 \
      /tmp/tc-rotation-baseline.mp4

Rotate the device while `screenrecord` is active. Do not stream the binary into the agent context or extract every frame by default. Record the path, duration, device, workload, and concise visual observations.
Capture `logcat` to a file and filter only trace summaries for normal review:

    adb logcat -c
    adb logcat -v monotonic > /tmp/tc-rotation-baseline.log

Run twenty alternating rotations for each baseline workload. Record p50, p95, stage counts, black/green/white/stale frames observed in the video, and whether the final dimensions match the stable orientation.
Normal validation is level 1 plus a focused device observation. No functional optimization belongs in this milestone.
Suggested logical commit:

    test(graphics,android): trace rotation rendering stages

### Milestone 2 — Test coordination rules and drop exact duplicates

The goal is to remove only callbacks that are provably equivalent, without changing EGL, Skia, repaint, SIP, or final resize behavior.
Extract the smallest package-private Java coordinator needed to test accepted resize state. A request is an exact duplicate only when all relevant values match:

    Surface identity or stable surface token
    width and height
    orientation
    Activity interactive state
    keyboard-transition category
    lifecycle category

Do not use width and height alone. A replacement surface with the same dimensions may require EGL recreation.
Add focused JVM tests for first request, exact duplicate, same size with a new surface, orientation change, keyboard sentinel separation, Activity resume, and preservation of the final accepted request.
Before queuing work, drop an exact duplicate only when no relevant lifecycle transition occurred after the previous accepted request. Increment a trace counter but avoid per-drop verbose output.
During ordinary slices, run the focused unit test and only three device rotations. At milestone closure, run ten idle and ten event-thread-load rotations. Compare scheduled-runnable count with baseline while requiring the same number of correct final generations.
Normal validation is level 2, escalating to a focused device smoke test because the logic guards a native surface path.
Suggested logical commit:

    fix(graphics,android): drop duplicate rotation callbacks

### Milestone 3 — Coalesce obsolete queued generations before JNI

The goal is to prevent queued intermediate work from reaching JNI/EGL while guaranteeing that the newest accepted generation always executes.
When a resize is accepted, capture an immutable snapshot containing the generation, surface token, actual `Surface`, dimensions, orientation, and relevant keyboard/lifecycle category. At runnable start, compare the captured generation with the latest accepted resize generation. If it is obsolete, exit before `nativeInitSize`, metrics work, SIP work, or event posting.
Never apply this rule to `-999`, `-998`, or `-997` sentinel operations. Keep lifecycle and keyboard signaling on explicitly separate paths.
Recheck that the captured surface is valid before JNI. A stale task must never destroy or replace resources belonging to a newer generation.
Add tests for rapid portrait-landscape-portrait acceptance, an old task starting after a new surface is accepted, final-task preservation, surface invalidation, and sentinel isolation.
Use the trace summarizer to verify:
- obsolete tasks exit before JNI;
- the newest generation always reaches `SCREEN_CHANGED`;
- final VM dimensions match the stable orientation;
- queue backlog and p95 do not regress.
Run three rotations during each code slice and ten per workload at milestone closure. Use a twenty-rotation run only if the ten-run result is too variable.
Normal validation is level 3, a focused integration test and device smoke test.
Suggested logical commit:

    fix(graphics,android): coalesce stale rotation work

### Milestone 4 — Remove measured SIP and repaint duplication

This milestone contains two separately commit-able slices. Do not combine them unless each is independently proven.
First, measure SIP work during rotation. Separate ordinary resize state from keyboard state. When the keyboard is already closed and no control expects a close transition, avoid redundant `setSIP(SIP_HIDE, false)` and `sendCloseSIPEvent()` calls. Preserve exactly one required transition when the keyboard was open. Test keyboard closed, keyboard open before rotation, open after rotation, Back dismissal, gesture navigation, and three-button navigation.
Second, use trace counts to prove whether more than one full repaint is useful. Add a private experiment switch rather than deleting a repaint immediately. Compare these candidates one at a time:
1. keep `screenChange()` repaint unchanged;
2. suppress the immediate repaint and rely on repaint after `SK_SCREEN_CHANGE`;
3. defer the full repaint until resize/layout handling completes;
4. retain immediate repaint only when no Java screen-change handler will run.
Select the path that produces one correct full repaint after final layout. Applications overriding resize handling must still repaint correctly. Remove the temporary switch after the decision is supported by evidence.
For each slice, use focused tests and three rotations. At each slice closure, run ten idle and ten loaded rotations plus the relevant keyboard cases. Record only aggregate counters and anomalous generation identifiers.
Normal validation is level 3. Escalate to a module build at milestone closure.
Suggested logical commits:

    fix(runtime,android): avoid redundant rotation sip events
    perf(graphics,android): remove duplicate rotation repaint

### Milestone 5 — Establish one EGL and Skia recreation owner when justified

Do not start this milestone unless Milestone 1 tracing proves more than one Skia initialization per normal surface generation or shows recreation as a dominant part of the delay.
Document current ownership separately for `ANativeWindow`, EGL display, EGL surface, EGL context, viewport, Skia screen surface, projection, screen dimensions, and GPU-resource invalidation.
Choose one architecture based on thread requirements and measured behavior:
- Option A: `nativeInitSize()` owns `ANativeWindow`, EGL, and Skia recreation; `screenChange()` updates VM dimensions, layout state, events, and dirty state without recreating the already initialized Android screen surface.
- Option B: `nativeInitSize()` records and references the new native window; the TotalCross event/graphics path recreates EGL and Skia once on the owning thread before rendering resumes.
Do not implement both. Record why the selected owner is safe for the EGL calls and existing render thread.
Associate native graphics state with a monotonically increasing surface generation. An old generation must never destroy EGL or Skia resources created for a newer generation. Add debug assertions or counters requiring no more than one normal `initSkia` per surface generation, except an explicitly logged recovery attempt.
Keep this change in an isolated commit. If it regresses device behavior, revert this milestone while preserving the safer improvements from Milestones 1–4.
Run focused lifecycle tests, ten idle rotations, ten loaded rotations, keyboard cases, minimize/restore, screen-off/on, and return from an auxiliary Activity. Run twenty rotations only for the closing comparison.
Normal validation reaches level 5, a physical-device smoke test, because this slice changes native graphics ownership.
Suggested logical commit:

    perf(graphics,android): unify rotation surface recreation

### Milestone 6 — Validate recovery, consolidate evidence, and close

The goal is to prove the final behavior and remove temporary experiment code without broadening the fix.
If context-loss measurement shows eager reconstruction of non-visible resources is still a material delay, classify resources as context-dependent, CPU-resident, or lazily recreatable. Preserve CPU pixels and invalidate GPU handles, recreating visible resources on demand. Do not add this optimization without measured need.
Remove temporary A/B switches, excessive stage logs, and test-only behavior. A compact disabled-by-default rotation trace may remain for future diagnosis.
Run the final matrix on the primary device and at least one additional Android device when available:
- twenty idle rotations;
- twenty event-thread-load rotations;
- portrait-landscape-portrait bursts;
- keyboard closed;
- keyboard open before rotation;
- keyboard opened after rotation;
- Back dismissal;
- minimize and restore;
- screen off and on;
- return from camera or another auxiliary Activity;
- gesture navigation and three-button navigation when available;
- Android 13 and Android 15 or newer when available;
- at least one Samsung device when available.
Use `screenrecord` for OpenGL visual evidence. Do not use a white result from `adb exec-out screencap -p` as failure evidence. If a still image is required, extract a specific frame from the recorded MP4 only after the video confirms that the frame is meaningful.
Compare the final summary with the preserved baseline on the same device and workload. Update the editorial report and state whether iOS and Windows were built, tested, or deliberately not exercised.
Normal validation reaches level 5. A full distribution build is level 6 and may be deferred to the 7.3.0 milestone close unless shared graphics code changed in a way that requires it. A clean full build is level 7 and requires evidence of stale artifacts or an explicit request.

## Surprises & Discoveries

- Observation: queued Java resize runnables read mutable `lastSurface`, `lastScreenW`, and `lastScreenH` rather than a callback-local snapshot. Evidence: `sendScreenChangeEvent()` accesses those fields when the runnable executes.
- Observation: a new `ANativeWindow` can trigger `destroyEGL()`, `initGLES()`, and `initSkia()` inside `nativeInitSize()`. Evidence: the current Android graphics implementation performs this sequence when `lastWindow != window`.
- Observation: `screenChange()` can then call `graphicsDestroy()` and `createScreenSurface()`, whose Android Skia path calls `initSkia()` again. Evidence: the current screen-change and screen-surface implementations contain both paths.
- Observation: `screenChange()` posts `SK_SCREEN_CHANGE` and also invokes `repaintActiveWindows()` immediately. Evidence: both actions occur in the same current function.
- Observation: the resize runnable hides SIP and sends a close-SIP event for ordinary screen changes. Evidence: these calls are currently unconditional inside the runnable after native size initialization.
- Observation: `-999`, `-998`, and `-997` are non-resize signals with keyboard and lifecycle semantics. Evidence: `nativeInitSize()` branches on these values before ordinary surface handling.
- Observation: `adb exec-out screencap -p` may produce a white or blank image for OpenGL-rendered content. Evidence: this is an environment limitation supplied for this issue; `adb shell screenrecord` captures the OpenGL drawing and must be preferred.
- Observation: duplicate work remains a hypothesis until measured per generation. Evidence: static call paths prove the calls exist, but do not alone prove how often each executes during a real rotation on the affected device.
- Observation: the available emulator initially rejected the instrumented debug APK because the installed `totalcross.android` package had a different shared UID. Evidence: `adb install -r` returned `INSTALL_FAILED_UID_CHANGED`; after explicit authorization, `adb uninstall totalcross.android` resolved the blocker without affecting repository files.
- Observation: changing `settings put system user_rotation` did not reliably invoke the Android surface callback, while `wm user-rotation lock` produced the expected callbacks. Evidence: only the latter generated accepted surface generations during the controlled probes.
- Observation: the first screen-change generation association matched callbacks by dimensions and misattributed queued generations when dimensions repeated. Evidence: the trace showed a later Java generation receiving an earlier screen-change event; FIFO association and callback-before-event ordering corrected the attribution.
- Observation: the showcase sample could not provide a stable deploy smoke app because its generated lambda class was unavailable at runtime. Evidence: deployment succeeded but launch aborted with `ClassNotFoundException`; a temporary minimal `RotationTest` app was used outside the repository instead.
- Observation: the load baseline produced one accepted callback without `graphics_create_screen_surface` and `init_skia`, while still reaching a first frame. Evidence: generation 28 in `/tmp/tc-rotation-m1-baseline-load-summary.log`; this is retained as a measured lifecycle variation, not treated as an optimization target in Milestone 1.
- Observation: the emulator absorbed some one-second `wm user-rotation` requests while a previous transition was still settling. Evidence: the first ten-command Milestone 2 smoke produced six accepted callbacks; a slower run produced 16 idle callbacks and the required ten load callbacks. Device counts are therefore reported from accepted trace generations, not command count.
- Observation: exact duplicate suppression is most directly proven by the coordinator's seven-case unit suite; ordinary orientation changes replace the surface or dimensions often enough that the device smoke primarily verifies preservation and no regression. Evidence: `RotationRequestCoordinatorTest` covers the exact duplicate and all relevant distinguishing fields, while the smoke traces report zero stale tasks.
- Observation: under rapid event-thread load, accepted intermediate generations can become obsolete before their runnable starts. Evidence: `/tmp/tc-rotation-m3-rapid.log` contains three `resize_runnable_stale` stages and no native-init stage for those generations; the newest generation completes normally.
- Observation: the ten-rotation closure intentionally reports fewer completed intermediate generations when they are superseded: idle had 10 accepted callbacks, 6 completed, and 7 stale tasks; load had 10 accepted callbacks, 8 completed, and 5 stale tasks. Evidence: `/tmp/tc-rotation-m3-idle.log` and `/tmp/tc-rotation-m3-load.log`; both final stable generations reached `SCREEN_CHANGED` and first swap at `1080x2220`.
- Observation: before the SIP slice, closed-keyboard rotation traces contained 10 `sip_hide_requested` and 10 `sip_close_event_requested` entries for the ten-rotation workloads; after the slice, both counts were zero for closed-keyboard runs and exactly one each when the keyboard was visible at request capture. Evidence: `/tmp/tc-rotation-m4-idle-closed.log`, `/tmp/tc-rotation-m4-load-closed.log`, and `/tmp/tc-rotation-m4-sip-keyboard-open.log`.
- Observation: one automated Back key event did not immediately clear `mInputShown` on the emulator; a second explicit Back event did. The keyboard was then hidden and rotation settings were restored. Evidence: `/tmp/tc-rotation-m4-sip-keyboard-after-back.log` and the subsequent `mInputShown=false` check; this is retained as an emulator-input caveat.
- Observation: the native immediate repaint caused two swaps per generation, while the A/B candidate caused one and preserved the rendered frame. Evidence: baseline `/tmp/tc-rotation-m4-idle-closed.log` and `/tmp/tc-rotation-m4-load-closed.log` report `swaps=2`; A/B `/tmp/tc-rotation-m4-ab-idle.log` and `/tmp/tc-rotation-m4-ab-load.log` report `swaps=1`; screen recording `/tmp/tc-rotation-m4-ab-load.mp4` shows the diagnostic content after rotation.
- Observation: the Milestone 5 entry condition was not met. M1 baseline summaries had 24 generations with `init_skia=1`, 4 startup generations with `init_skia=0`, and no `window_changes` in both idle and load; M4 final summaries had 10 rotation generations with `init_skia=1`, one startup generation with `init_skia=0`, and no `window_changes` in both workloads. Evidence: `/tmp/tc-rotation-m1-baseline-idle-summary.log`, `/tmp/tc-rotation-m1-baseline-load-summary.log`, `/tmp/tc-rotation-m4-final-idle.log`, and `/tmp/tc-rotation-m4-final-load.log`.
- Observation: Android deployment did not propagate `/c load` into `MainWindow.getCommandLine()` for this single-APK path; the final load workload therefore used a temporary outside-repository `RotationTest` variant with deterministic load enabled at compile time. Evidence: the first `/c load` recording displayed the idle label; the corrected `/tmp/tc-rotation-m6-final-load-fixed.mp4` displays `Android rotation trace load`.
- Observation: `monkey` did not reliably bring the app back after minimize on this emulator, while explicit `am start -n totalcross.apprtst/.Loader` restored it successfully. Evidence: `/tmp/tc-rotation-m6-lifecycle-explicit.log` and the explicit foreground checks.
- Observation: the keyboard-open-before-rotation case hid the IME after five rotations; the after-rotation case opened the IME but repeated Back events did not dismiss it in the final attempt. The IME was cleared with `ime hide` before lifecycle testing, and the emulator input caveat remains. Evidence: `/tmp/tc-rotation-m6-keyboard-before.log` and `/tmp/tc-rotation-m6-keyboard-after.log`.
- Observation: the final trace still reports one stale non-final task in each 20-rotation workload, while all 20 accepted rotation callbacks completed and had zero missing-stage warnings. Evidence: `/tmp/tc-rotation-m6-final-idle-summary.json` and `/tmp/tc-rotation-m6-final-load-fixed-summary.json`.
- Observation: the requested two-Edit test computes `Window.shiftY=1804` and `shiftH=184`, but the Android OpenGL frame does not translate the VM content: the lower Edit remains covered after `mInputShown=true`. Evidence: `/tmp/tc-rotation-m6-two-edits-shift-ime.mp4`, `/tmp/tc-rotation-m6-two-edits-shift-ime-frame.png`, and `/tmp/tc-rotation-m6-two-edits-observe-frame.png`.
Move resolved discoveries that no longer influence future work to the history file at milestone boundaries.

## Decision Log

- Decision: perform all work on `fix/419-sluggish-interface-rendering-when-rotating-screen`. Rationale: the branch is dedicated to issue #419 and was identical to `master` at the recorded baseline. Date: 2026-07-25.
- Decision: instrument before optimizing. Rationale: this path crosses Android callbacks, the TotalCross event thread, JNI, EGL, Skia, layout, repaint, and keyboard behavior; static analysis alone cannot identify the dominant runtime cost. Date: 2026-07-25.
- Decision: exact duplicate removal and stale-generation coalescing precede repaint or EGL ownership changes. Rationale: they can eliminate queued work without redefining the graphics lifecycle. Date: 2026-07-25.
- Decision: preserve the newest accepted resize and keep sentinel operations outside resize coalescing. Rationale: dropping the final generation or keyboard/lifecycle signals can leave stale dimensions, broken IME state, or invalid graphics resources. Date: 2026-07-25.
- Decision: use `adb shell screenrecord` as the primary visual evidence for OpenGL rendering. Rationale: `adb exec-out screencap -p` may return a white image even when the OpenGL frame is correct. Date: 2026-07-25.
- Decision: review compact summaries before raw logs and use progressive sample sizes. Rationale: this reduces execution-token use and repeated test cost while preserving full artifacts for investigation. Date: 2026-07-25.
- Decision: gate runtime tracing with the Android log tag `TotalCrossRotation` at DEBUG. Rationale: `Log.isLoggable` is false by default, can be enabled for a controlled run with `adb shell setprop log.tag.TotalCrossRotation DEBUG`, and avoids a release-only source toggle. Date: 2026-07-25.
- Decision: keep the baseline instrumentation checkpoint independently commit-able even though device capture is blocked. Rationale: the implementation and build proof are complete, while deleting or bypassing the installed package would be destructive and would not prove behavior on the existing app state. Date: 2026-07-25.
- Decision: use a generated Standard Release AAB as the deploy template, replacing `TotalCrossSDK/dist/vm/android/TotalCross.aab` only for the controlled deploy and restoring the prior artifact afterward. Rationale: this follows the requested SDK deployment flow while keeping generated repository artifacts out of the logical commit. Date: 2026-07-25.
- Decision: associate queued screen-change generations FIFO and enqueue the native association before posting `SCREEN_CHANGED`. Rationale: repeated dimensions are not a sufficient identity and the Java callback order is the available lifecycle ordering signal. Date: 2026-07-25.
- Decision: use a temporary minimal `RotationTest` application outside the repository for baseline capture. Rationale: the existing showcase deploy was not runnable after packaging, while the temporary app provides idle and deterministic event-thread workloads without adding product test code in this milestone. Date: 2026-07-25.
- Decision: gate queue admission through a package-private coordinator before incrementing the rotation generation. Rationale: exact duplicates must not create trace generations or event-thread work, while a new `Surface`, orientation, keyboard state, interactive state, or lifecycle transition must remain observable. Date: 2026-07-25.
- Decision: retain the existing mutable resize runnable for Milestone 2. Rationale: immutable snapshotting and stale-generation cancellation are explicitly deferred to Milestone 3, keeping duplicate admission independently reviewable. Date: 2026-07-25.
- Decision: compare the immutable request generation at runnable start, before trace JNI registration, `nativeInitSize`, metrics work, SIP work, or event posting. Rationale: an obsolete task must not touch native graphics state or enqueue a stale screen event. Date: 2026-07-25.
- Decision: revalidate the captured `Surface` immediately before JNI and exit when it is null or invalid. Rationale: a valid snapshot can become unusable while queued, and stale work must not destroy resources belonging to a newer surface. Date: 2026-07-25.
- Decision: make the captured keyboard category control rotation SIP work. Rationale: a closed keyboard has no close transition to preserve, while a request captured with the keyboard visible still needs exactly one hide and close event. Date: 2026-07-25.
- Decision: remove the Android native immediate repaint and retain the Java `SK_SCREEN_CHANGE` handler repaint. Rationale: A/B traces reduced swaps from two to one per generation, and `screenrecord` showed correct final OpenGL output; non-Android behavior remains unchanged. Date: 2026-07-25.
- Decision: retain the Java `SK_SCREEN_CHANGE` handler as the single useful full-repaint owner on Android. Rationale: the A/B run showed that the native immediate repaint was redundant and that the Java-handler repaint alone produced one correct swap per generation. Date: 2026-07-25.
- Decision: defer an EGL/Skia ownership refactor. Rationale: the recorded normal rotations show no more than one `init_skia` per generation and no native-window change requiring EGL recreation, so changing ownership would add risk without measured duplicate work to remove. Date: 2026-07-25.
- Decision: retain only the disabled-by-default compact rotation trace after M6. Rationale: the A/B switch and temporary load/keyboard variants are outside the repository and removed from the final source path, while the trace remains useful for future diagnosis without normal release logging. Date: 2026-07-25.
- Decision: stop at the two-Edit shift failure without changing runtime code. Rationale: the test proves the VM computes the shift but the native OpenGL frame does not honor it; fixing that ownership/rendering path is a separate implementation task beyond the requested validation. Date: 2026-07-25.
- Decision: classify the lower-Edit shift failure as pre-existing relative to this ExecPlan. Rationale: the identical temporary app and SDK Java classes reproduced the same failure with the pre-plan base AAB and both M4 checkpoints; the plan's changed paths do not contain the shift-state implementation. Date: 2026-07-25.

## Validation and Acceptance

Follow the validation escalation in `AGENTS.md` and stop at the first sufficient level:
1. static or diff check;
2. focused unit test;
3. focused integration test;
4. module build;
5. device smoke test;
6. full distribution build;
7. clean full distribution build.
Use quiet builds and save output:

    cd TotalCrossVM/android
    ./gradlew :app:testDebugUnitTest --console=plain \
      > /tmp/tc-rotation-unit.log 2>&1
    status=$?
    tail -60 /tmp/tc-rotation-unit.log
    exit $status

Adapt the task to the actual variant and test class. Do not run every Android task when one focused test proves the slice. Fetch native dependencies only when missing. Do not run `clean` by default.
For device logs, save raw output and review summaries:

    adb logcat -c
    adb logcat -v monotonic > /tmp/tc-rotation.log

    grep 'ROTATION_TRACE_SUMMARY' /tmp/tc-rotation.log \
      > /tmp/tc-rotation-summary.log

The final implementation is accepted when:
1. Twenty consecutive rotations in each final workload complete without crash, freeze, black, green, white, stale-size, or partially laid-out final frames.
2. Final VM dimensions, layout dimensions, native surface dimensions, and stable orientation agree.
3. The newest accepted resize generation is never discarded.
4. Obsolete queued resize work exits before JNI.
5. Exact duplicates do not enter the heavy pipeline.
6. Keyboard behavior remains correct before and after rotation.
7. Minimize/restore, screen-off/on, and auxiliary-Activity return remain correct.
8. Normal rotation performs no more than one EGL/Skia initialization per surface generation when Milestone 5 is implemented.
9. Each stable generation produces one useful full repaint, except explicitly logged recovery paths.
10. The camera or screen-recording evidence shows the OpenGL output correctly.
11. p50 or p95 time to the first correct frame improves by at least 25% in the
    reproducible reported workload, or the issue owner accepts a documented
    structural improvement that removes the visible delay despite a different
    measured percentage.
12. No persistent memory increase or startup regression is introduced.
13. Focused tests, the required Android build, and the physical-device smoke
    test pass.

## Risks and Open Questions

The main correctness risk is discarding a callback that represents a new native surface with unchanged dimensions. Surface identity must be part of duplicate and generation decisions.
The main lifecycle risk is an obsolete task destroying EGL resources belonging to a newer surface. Generation checks must exist on both Java and native boundaries before ownership changes are accepted.
The main compatibility risk is removing the immediate repaint when an application depends on it. Repaint changes require A/B evidence and testing of custom resize handling.
The main keyboard risk is treating sentinel operations as ordinary resizes or suppressing a real close transition. Keyboard tests are mandatory for slices that touch `surfaceChanged`, `sendScreenChangeEvent`, or `nativeInitSize`.
Performance measurements can be noisy because device thermal state, refresh rate, background processes, and application workload vary. Compare on the same device, build type, orientation sequence, and workload, and record limitations.
Visual recording may alter performance slightly. Use trace timing as the primary metric and video as correctness evidence. Record whether `screenrecord` was active for each benchmark.
The exact diagnostic application path and available Android test tasks must be confirmed before the first implementation commit. Do not create a large test framework if a small existing sample and focused JVM test are sufficient.

## Idempotence and Recovery

Tracing, summary generation, and tests must be repeatable. Use unique artifact paths or overwrite only files created for this plan.
If an experiment regresses rendering, revert only that logical commit and preserve instrumentation, tests, baseline artifacts, and earlier safe fixes. Milestone 5 must remain independently revertible from Milestones 1–4.
When a test temporarily changes device rotation settings, record the original values and restore them in a shell `trap` or immediately after the run. Do not leave a personal device rotation-locked.
Do not delete `.cxx`, Gradle caches, native dependencies, or unrelated generated files merely to obtain a clean build. Remove only a proven stale generated directory when necessary.
Before committing, run:

    git diff --stat
    git diff -- \
      TotalCrossVM/android/app/src/main/java/totalcross/Launcher4A.java \
      TotalCrossVM/android/app/src/test \
      TotalCrossVM/android/app/src/androidTest \
      TotalCrossVM/src/event/android/event_c.h \
      TotalCrossVM/src/nm/ui/android/gfx_Graphics_c.h \
      TotalCrossVM/src/nm/ui/GraphicsPrimitives_c.h \
      scripts/diagnostics \
      .agent
    git diff --check

Local logical commits are allowed after focused validation. Pushes, pull-request creation, issue updates, tags, releases, and artifact publication require explicit user instruction and are not performed automatically by this plan.
Before an authorized push, fetch the remote, confirm the remote branch has not advanced unexpectedly, and push without `--force`. Preserve unrelated local changes.

## Outcomes & Retrospective

Current state: the base Milestone 6 validation is complete, but the requested two-Edit lower-screen follow-up is blocked. The final Standard Release AAB was rebuilt with `:app:bundleStandardRelease`, copied to `TotalCrossSDK/dist/vm/android/TotalCross.aab`, deployed through `tc.Deploy`, and installed as `totalcross.apprtst`. On the Android 14 emulator `sdk_gphone64_arm64` (`emulator-5554`), the final idle run `/tmp/tc-rotation-m6-final-idle.log` completed 20/20 accepted rotation callbacks with p50/p95 first-frame times `157.146/256.095 ms`, one stale non-final task, zero missing-stage warnings, and one useful swap per summary. The fixed-load run `/tmp/tc-rotation-m6-final-load-fixed.log` completed 20/20 with p50/p95 `167.915/211.629 ms`, one stale non-final task, zero missing-stage warnings, and one swap per summary. Compared with preserved M1 baselines (`198.587/255.170 ms` idle and `199.846/424.110 ms` load), load p95 improved by about 50%; idle p50 improved by about 21% and idle p95 remained comparable. `/tmp/tc-rotation-m6-final-load-fixed.mp4` and its extracted frame show the correct OpenGL load content. Keyboard-before-rotation hid the IME after five rotations; keyboard-after-rotation opened it but Back dismissal remained an emulator caveat. Explicit lifecycle checks passed for burst rotations, minimize/restore, screen off/on, return from Settings, gesture navigation, and three-button navigation. The old `totalcross.android` package was removed; only `totalcross.apprtst` remained. The two-Edit test computed `Window.shiftY=1804` with `mInputShown=true`, but `/tmp/tc-rotation-m6-two-edits-shift-ime-frame.png` still shows the lower Edit covered by the keyboard. The compact trace remains disabled by default; no A/B switch or temporary diagnostic source was added to the repository. No additional physical/Android-version/Samsung device was available, and iOS/Windows were deliberately not exercised. The next action is a separately authorized runtime investigation of native application of `Window.shiftY`; no later milestone exists.

### Editorial Summary

The Android rotation path now drops exact duplicates and obsolete queued work, avoids redundant closed-keyboard SIP events, performs one useful repaint, and retains the existing EGL/Skia ownership because runtime evidence did not show duplicate recreation. Final 20-rotation idle/load runs completed without crash or ANR on the available Android 14 emulator. The added two-Edit test exposed a remaining native OpenGL shift-application blocker.

### Original Plan versus Actual Outcome

The planned low-risk sequence was followed through tracing, duplicate suppression, stale-generation coalescing, SIP/repaint measurement, and final lifecycle validation. The conditional EGL/Skia ownership refactor was not implemented because its evidence gate was not met.

### What Changed

`Launcher4A` now uses immutable rotation request snapshots and conditional SIP handling. Android native immediate repaint was removed from `GraphicsPrimitives_c.h`. Disabled-by-default tracing records the final generation summaries, including repaint and first-swap counters.

### Decisions and Trade-offs

The Java `SK_SCREEN_CHANGE` handler owns the single full repaint. EGL and Skia ownership stayed unchanged to avoid an unmeasured native-threading risk. A deterministic temporary load variant was used outside the repository because Android single-APK deployment did not propagate `/c load`.

### Unexpected Problems and Discoveries

The emulator required explicit `am start` for reliable restore after minimize, and Back did not dismiss the keyboard in the final after-rotation attempt. The final traces also retained one stale non-final task per workload while completing every accepted rotation.

### Validation and Measurable Results

The Standard Release AAB build, SDK-template deploy, APK install, focused unit tests, 20 idle rotations, 20 load rotations, lifecycle checks, keyboard checks, two navigation modes, and the two-Edit shift test were exercised. Load p95 improved from `424.110 ms` to `211.629 ms` against the preserved M1 baseline; the two-Edit test failed its visual visibility criterion.

### Useful Evidence and Examples

Final traces: `/tmp/tc-rotation-m6-final-idle-summary.json` and `/tmp/tc-rotation-m6-final-load-fixed-summary.json`. Visual evidence: `/tmp/tc-rotation-m6-final-load-fixed.mp4`. Lifecycle evidence: `/tmp/tc-rotation-m6-lifecycle-explicit.log`. Shift evidence: `/tmp/tc-rotation-m6-two-edits-shift-ime.mp4` and `/tmp/tc-rotation-m6-two-edits-shift-ime-frame.png`.

### Limitations, Remaining Work, and Open Questions

Evidence covers one Android 14 emulator only. No Samsung, Android 13/15+, physical-device, iOS, Windows, or full distribution validation was performed. The after-rotation keyboard Back behavior needs confirmation on a physical device or a more reliable emulator input sequence. The lower-Edit shift requires a native OpenGL rendering fix or ownership investigation.

### Possible Article Angles

Measured duplicate work across Java, JNI, and graphics layers; generation-aware cancellation; and why a conditional EGL/Skia refactor was deliberately avoided are the strongest technical angles.

### Suggested Narrative

Start with the measured rotation trace, show the two-swap and SIP duplication, explain the low-risk fixes, then close with the final 20-rotation evidence and the explicit decision not to change EGL/Skia ownership without proof.

### Claims Requiring Human Review

Confirm the acceptable interpretation of the emulator keyboard caveat, the one stale non-final task, whether the measured Android 14 improvement is sufficient for issue closure across physical devices, and whether the lower-Edit OpenGL shift failure should become a separate fix.

At each milestone boundary, replace or append a short factual summary containing the behavior delivered, validation performed, aggregate result, evidence path, limitations, and next boundary. Move completed details to the history file when they make the active plan harder to resume.
At completion, the editorial report must include:
- `Editorial Summary`;
- `Original Plan versus Actual Outcome`;
- `What Changed`;
- `Decisions and Trade-offs`;
- `Unexpected Problems and Discoveries`;
- `Validation and Measurable Results`;
- `Useful Evidence and Examples`;
- `Limitations, Remaining Work, and Open Questions`;
- `Possible Article Angles`;
- `Suggested Narrative`;
- `Claims Requiring Human Review`.
The final retrospective must distinguish measured causes from static hypotheses, tested devices from expected compatibility, and delivered optimizations from deferred possibilities.

Milestone 1 continuation note (2026-07-25): the stale emulator package was removed only after explicit user authorization; Gradle generated the Standard Release AAB, it replaced the SDK deploy template for a controlled `tc.Deploy`, and a temporary `RotationTest` APK was installed. Idle and deterministic event-thread-load baselines were captured with 24 alternating rotations each. The trace generation ordering and FIFO screen-change association were corrected, and summary emission now occurs after `screen_change_returned`. No optimization milestone was started.
Milestone 2 continuation note (2026-07-25): exact duplicate admission was isolated in `RotationRequestCoordinator`, with eight JUnit cases and a focused Android smoke run on the deployed test application. The implementation preserves same-size new surfaces, orientation changes, interactive-state changes, keyboard transitions, and lifecycle transitions; immutable resize snapshots and stale-task coalescing remain deferred to Milestone 3.
Milestone 3 continuation note (2026-07-25): accepted requests now carry generation and surface snapshots; stale runnables exit before JNI, while the newest stable generation reaches the native pipeline. Eleven JVM tests, a rapid-load probe, and ten accepted rotations per final workload passed on the Android 14 emulator. No SIP, repaint, EGL/Skia ownership, or later lifecycle validation was performed.
Milestone 4 continuation note (2026-07-25): redundant closed-keyboard SIP work was removed in one commit, and native immediate repaint was removed in a separate commit after an A/B experiment showed one correct swap instead of two. Final idle/load runs completed 10/10 rotations with one swap per generation; keyboard-open and Back cases were exercised with the emulator input caveat recorded above. The Milestone 5 ownership gate was the next boundary.
Milestone 5 continuation note (2026-07-25): the ownership gate was reviewed against preserved M1 and M4 traces. Normal generations showed at most one `init_skia` and no `window_changes`, so the planned EGL/Skia ownership refactor was not justified and no native ownership commit was created. The next boundary was Milestone 6.
Milestone 6 continuation note (2026-07-25): rebuilt and redeployed the final Standard Release AAB, completed 20 idle and 20 fixed-load rotations with one swap per generation and no missing stages, exercised keyboard/lifecycle/navigation cases, reviewed the final OpenGL recording, and removed the stale `totalcross.android` package. The base milestone was closed; broader devices and platform builds remain explicitly unexercised.
Milestone 6 follow-up note (2026-07-25): added a temporary diagnostic app with top and bottom `Edit` controls, executed the keyboard-visible screen-recording test, and observed `Window.shiftY=1804`/`shiftH=184` while the lower control remained covered in OpenGL output. A regression across the pre-plan base (`b7c25d776`), M4 SIP (`c72ca44a9`), and M4 repaint (`5a773cebc`) AABs reproduced the same result, locating the failure before this ExecPlan rather than in a milestone change. The cached SDK 7.2.2 AAB reproduced it as well. No runtime fix was attempted; this follow-up is blocked pending authorization for a native rendering investigation.

## Revision Note

2026-07-25: the previous Portuguese linear plan was rewritten in English using the `TotalCross/totalcross-depot-tools` `.agent/PLANS.md` template. The revision updated the target branch and baseline, added the required resumable-plan sections and supporting-file protocol, preserved the progressive low-risk-first strategy, added explicit token-efficient execution and testing instructions, and documented that `adb exec-out screencap -p` may produce a white OpenGL capture while `adb shell screenrecord` captures the rendered output.
2026-07-25: Milestone 1 was closed after the authorized package replacement, Gradle AAB generation, SDK-template deploy, two 24-rotation workload baselines, and visual screen-recording review. The plan records the corrected FIFO generation association and retains one measured load-path lifecycle variation for later interpretation.
2026-07-25: Milestone 2 was closed after focused coordinator tests, the updated AAB deploy flow, and accepted-generation smoke runs. The next continuation starts at Milestone 3; no stale-generation or later lifecycle validation was performed here.
2026-07-25: Milestone 3 was closed after immutable snapshot checks, rapid-load stale-generation evidence, and ten accepted rotations per workload. The next continuation starts at Milestone 4; no SIP/repaint or later-milestone validation was performed here.
2026-07-25: Milestone 4 was closed after independent SIP and repaint slices, A/B screen-recording evidence, keyboard cases, final ten-rotation idle/load runs, and the required module build. The next continuation starts at Milestone 5; EGL/Skia ownership validation was deliberately not performed here.
2026-07-25: Milestone 5 was closed at its entry gate after preserved traces showed no duplicate Skia initialization or EGL window recreation in normal rotations. No ownership change or Milestone 6 validation was performed.
2026-07-25: Milestone 6 was closed after final AAB/deploy, 20 idle and 20 load rotations, recovery/lifecycle/navigation checks, visual screen-recording review, and editorial consolidation. Broader device and non-Android validation remain outside the available environment.
2026-07-25: The requested two-Edit keyboard-shift follow-up was executed after the M6 closure. VM shift state was computed, but the lower Edit remained covered in the OpenGL frame; checkpoint AAB regression testing and the cached SDK 7.2.2 AAB reproduced the failure before the plan's changes, and no runtime code was changed.
