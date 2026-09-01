<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Separate Window backends from platform services and restore Windows SDL input

This ExecPlan follows `.agent/PLANS.md`, `AGENTS.md`, and
`.agents/skills/logical-commits/SKILL.md`.

This is plan 2 of 2. Execute it only after
`01-event-backend-sdl-text-input.md` is complete.

## Purpose / Big Picture

Refactor `Window.c` so windowing backend and OS services are independent, then
remove the Windows SDL keyboard regression caused by coupling SIP visibility to
SDL text-input state.

Final observable behavior:

- SDL `Window_c.h` owns SDL window operations only.
- Native `Window_c.h` files own native window operations only.
- SIP, safe area, and orientation are selected through `TC_OS_*` platform
  services, independent of `TC_WINDOWING_*`.
- Windows SDL = SDL window backend + native Windows SIP/TabTip services.
- Windows Native = native Win32 backend + the same Windows services.
- macOS SDL = SDL backend + explicit macOS service no-ops; no Linux fallthrough.
- `Window.setSIP(SIP_HIDE, ...)` never disables `SDL_TEXTINPUT`.
- public TotalCross Window APIs remain unchanged.

Do not redesign rendering, Skia, pixel formats, DPI, command-line parsing,
fullscreen policy, or native hotkeys.

## Execution Contract

Before editing:

1. Read `.agent/state/event-backend-sdl-text-input.md` and record the completed
   plan-1 commit. Do not reread plan 1 in full.
2. Read this plan once, then resume from this plan's state file.
3. Preserve unrelated changes. Never reset, clean, rebase, amend, force
   checkout, or rewrite history.
4. Use path-scoped reads/searches and concise `/tmp` logs.
5. Follow `logical-commits` for every commit. Do not push.

Build restrictions:

- local builds only for macOS VM/Launcher and `TotalCrossSDK`;
- builds only at related milestone ends;
- no local Windows/Linux/Android/iOS/WinCE builds;
- native smoke only at milestone ends/final closure;
- Windows smoke only from an already-built current artifact.

Every new file must remain <= 20 KB and approximately <= 600 lines.
Do not refactor existing large files merely to reduce size.

## Working Set and Resume Protocol

Use plan key `window-backend-platform-services`.

Maintain:

- `.agent/state/window-backend-platform-services.md` — rewritten after every
  logical commit and read first on resume.
- `.agent/evidence/window-backend-platform-services.md` — concise append-only
  milestone evidence.
- `.agent/reports/window-backend-platform-services-editorial.md` — factual
  milestone/final handoff.

State records prerequisite commit, active milestone/slice, last commit, active
paths, focused validation, blockers, deferred validation, unrelated files left
alone, and one next action.

Resume with:

    sed -n '1,180p' .agent/state/window-backend-platform-services.md

## Progress

- [x] Milestone 1: introduce explicit Window backend/platform-service contracts.
- [x] Milestone 2: migrate platform services and eliminate SDL/SIP coupling.
- [ ] Milestone 3: validate all supported contracts and close; local validation
  passes, but final-HEAD CI has no available check-run yet.

## Current Architecture and Defect

Use normalized macros from `TotalCrossVM/src/tcvm/tc_platform.h`:

    TC_OS_WINDOWS
    TC_OS_WINCE
    TC_OS_MACOS
    TC_OS_IOS
    TC_OS_ANDROID
    TC_OS_LINUX

Windowing is independently selected with:

    TC_WINDOWING_SDL
    TC_WINDOWING_NATIVE

Legacy macros may remain inside low-level platform code where required by SDK or
compiler APIs, but must not remain the primary shared dispatch in `Window.c`.

Known failure to remove:

1. Windows SDL includes `nm/ui/sdl/Window_c.h`.
2. that file implements SIP state with `SDL_IsTextInputActive()`.
3. `SIP_HIDE` calls `SDL_StopTextInput()`.
4. TotalCross legitimately hides SIP during normal UI lifecycle.
5. physical keyboard text then stops producing `SDL_TEXTINPUT`.

macOS desktop does not define the legacy `darwin` macro in its normal CMake
path, so current `Window.c` falls through to `linux/Window_c.h`. Its SIP native
method effectively no-ops. Replace this accident with explicit macOS selection.

Plan 1 already owns SDL text-input lifecycle at the event/window boundary. This
plan must ensure Window/SIP code never controls that lifecycle.

## Target Architecture

Use exactly this responsibility split:

    TC_WINDOWING_* -> how the Window is implemented
    TC_OS_*        -> which native platform services exist

Window backend owns device/window title.
Platform services own SIP, safe area, and orientation.
SDL event subsystem owns SDL text-input lifecycle and keyboard translation.

Do not rename existing `darwin` directories to `ios`; select them with
`TC_OS_IOS`.

### Backend interface

Keep these backend paths:

    TotalCrossVM/src/nm/ui/sdl/Window_c.h
    TotalCrossVM/src/nm/ui/win/Window_c.h
    TotalCrossVM/src/nm/ui/linux/Window_c.h
    TotalCrossVM/src/nm/ui/android/Window_c.h
    TotalCrossVM/src/nm/ui/darwin/Window_c.h

Every selected backend exposes:

    static void windowBackendSetDeviceTitle(TCObject titleObj);

Required behavior:

- SDL: retain UTF-16 -> UTF-8 conversion and call `SDL_SetWindowTitle`; use this
  on Windows SDL, macOS SDL, and Linux SDL.
- native Windows/WinCE: preserve `SetWindowText` behavior.
- native Android: preserve current JNI title behavior.
- native Linux: preserve current effective title behavior, including no-op.
- native iOS: preserve current effective no-op unless a real implementation is
  already present.

Backend `Window_c.h` files must not own SIP, safe area, orientation, or SDL
text-input lifecycle.

### Platform-service interface

Create exactly:

    TotalCrossVM/src/nm/ui/win/WindowServices_c.h
    TotalCrossVM/src/nm/ui/linux/WindowServices_c.h
    TotalCrossVM/src/nm/ui/macos/WindowServices_c.h
    TotalCrossVM/src/nm/ui/android/WindowServices_c.h
    TotalCrossVM/src/nm/ui/darwin/WindowServices_c.h

`win/WindowServices_c.h` serves Windows desktop and WinCE.

Every adapter exposes identical static functions:

    static bool windowPlatformIsSIPShown(void);

    static void windowPlatformSetSIP(
        Context currentContext,
        int32 sipOption,
        TCObject control,
        bool numeric);

    static void windowPlatformSetOrientation(int32 orientation);

    static void windowPlatformGetSafeAreaInsets(
        int32 *top,
        int32 *left,
        int32 *bottom,
        int32 *right);

Use `UNUSED` for unused parameters. Do not vary signatures by OS.

### Required platform behavior

Windows desktop:

- move existing Win32 SIP/TabTip code from `win/Window_c.h`;
- preserve current `isShown` state semantics;
- `SIP_HIDE` preserves current touch-keyboard close behavior;
- show/top/bottom preserve current TabTip launch behavior;
- orientation no-op; safe area zeros;
- no SDL includes or text-input API calls.

WinCE:

- move existing SIP/IME code into `win/WindowServices_c.h`;
- use `TC_OS_WINCE` for TotalCross dispatch;
- retain `_WIN32_WCE`/SDK guards only where required by WinCE APIs;
- preserve virtual-keyboard/IME behavior;
- orientation no-op; safe area zeros unless already explicitly implemented.

Android:

- move current JNI set/get SIP, orientation, and safe-area functions from
  `android/Window_c.h` into `android/WindowServices_c.h`;
- preserve Java method names, JNI signatures, arguments, and cleanup exactly.

I/O/S (`darwin` directory):

- move SIP and safe-area declarations/adapters from `darwin/Window_c.h` into
  `darwin/WindowServices_c.h`;
- inspect callers of corresponding Objective-C symbols once;
- if Window is the only caller, rename implementations to `windowPlatform*`;
- otherwise keep existing symbols and use thin standardized wrappers;
- do not duplicate logic;
- orientation no-op unless this exact API is already implemented.

macOS:

    windowPlatformIsSIPShown() -> false
    windowPlatformSetSIP(...) -> no-op
    windowPlatformSetOrientation(...) -> no-op
    windowPlatformGetSafeAreaInsets(...) -> zeros

Do not use UIKit/iOS services on macOS.

Linux:

- explicit no-op services unless an already-supported DirectFB service exists;
- default SIP false/set no-op/orientation no-op/safe area zeros;
- never use SDL text-input APIs in Window services.

## Window.c Dispatch Contract

Refactor `TotalCrossVM/src/nm/ui/Window.c` into two independent selections.

Backend:

    #if TC_WINDOWING_SDL
      #include "sdl/Window_c.h"
    #elif TC_WINDOWING_NATIVE
      #if TC_OS_WINDOWS || TC_OS_WINCE
        #include "win/Window_c.h"
      #elif TC_OS_LINUX
        #include "linux/Window_c.h"
      #elif TC_OS_ANDROID
        #include "android/Window_c.h"
      #elif TC_OS_IOS
        #include "darwin/Window_c.h"
      #else
        #error Unsupported native Window backend
      #endif
    #else
      #error No Window backend selected
    #endif

Native macOS remains unsupported. Never fall through to Linux.

Platform services:

    #if TC_OS_WINDOWS || TC_OS_WINCE
      #include "win/WindowServices_c.h"
    #elif TC_OS_MACOS
      #include "macos/WindowServices_c.h"
    #elif TC_OS_LINUX
      #include "linux/WindowServices_c.h"
    #elif TC_OS_ANDROID
      #include "android/WindowServices_c.h"
    #elif TC_OS_IOS
      #include "darwin/WindowServices_c.h"
    #else
      #error Unsupported Window platform services
    #endif

After adapter selection, native methods do not branch by OS:

    tuW_isSipShown()
        -> windowPlatformIsSIPShown()

    tuW_setSIP_icb()
        -> validate sipOption once
        -> windowPlatformSetSIP(currentContext, sipOption, control, numeric)

    tuW_setDeviceTitle_s()
        -> windowBackendSetDeviceTitle(title)

    tuW_setOrientation_i()
        -> windowPlatformSetOrientation(orientation)

    tuW_getSafeAreaInsets()
        -> windowPlatformGetSafeAreaInsets(...) when initialization is needed

Preserve public Java/native API names.
Do not change unrelated safe-area scaling semantics or uncomment currently
inactive assignments as part of this refactor.

## SDL Ownership Prohibition

After migration, no Window backend/service except the plan-1 SDL event lifecycle
may own production calls to:

    SDL_StartTextInput
    SDL_StopTextInput
    SDL_IsTextInputActive

Specifically:

- remove SIP functions from `sdl/Window_c.h`;
- remove SDL SIP/text-input calls from `linux/Window_c.h`;
- no `WindowServices_c.h` may include or call SDL text-input APIs.

Do not add `WM_CHAR` fallback, printable `SDL_KEYDOWN` fallback, or a second
event pump.

## Plan of Work

### Milestone 1 — Introduce explicit contracts

Goal: `Window.c` composes one backend and one OS service adapter with normalized
macros.

Inspect only:

- `TotalCrossVM/src/nm/ui/Window.c`;
- platform `Window_c.h` files;
- `darwin/Window_c.m` around SIP/safe-area symbols only;
- `tc_platform.h`;
- build files only if needed for include/source reachability.

Implement:

1. create all five `WindowServices_c.h` files;
2. introduce the standardized service signatures;
3. rename backend title function to `windowBackendSetDeviceTitle`;
4. implement the exact backend/service dispatch above;
5. make Window native methods call standardized adapters;
6. preserve behavior while moving code; do not redesign services.

Keep the tree internally coherent at each commit. Interface plus dispatch may be
one commit if separating them would leave unresolved functions.

Preferred commit:

    refactor(window): separate backend and platform services

Focused tests must prove:

- backend selection uses `TC_WINDOWING_*`;
- service selection uses `TC_OS_*`;
- SDL precedes native OS backend selection;
- macOS has an explicit service branch;
- native macOS fails rather than falling to Linux;
- all service adapters expose identical signatures;
- `/darwin/` is selected through `TC_OS_IOS`.

Before commit:

- header validation;
- focused source-contract test;
- `git diff --check --cached`;
- staged diff review;
- logical commit with required English body;
- commit-message validation.

At milestone end only, run permitted macOS build:

    cmake -S TotalCrossVM -B build-macos-window-event -G Ninja \
      -DCMAKE_BUILD_TYPE=Release \
      -DTC_WINDOWING_SDL=ON \
      -DTC_WINDOWING_NATIVE=OFF \
      -DTC_RENDERER_SKIA=ON \
      -DTC_RENDERER_LEGACY=OFF \
      -DTC_GRAPHICS_SOFTWARE=ON \
      -DTC_GRAPHICS_GLES=OFF

    cmake --build build-macos-window-event --target tcvm Launcher

Reuse the plan-1 build directory when compatible.

### Milestone 2 — Move services and fix Windows SDL input

Goal: Window files obey ownership boundaries and SIP cannot disable SDL input.

Move implementations exactly as specified above.

Final backend shape:

- `sdl/Window_c.h`: SDL title/window concern only;
- `win/Window_c.h`: native title/window concern only;
- `linux/Window_c.h`: native backend concern only, no SDL text input;
- `android/Window_c.h`: native backend/title concern only;
- `darwin/Window_c.h`: native iOS backend concern only.

Enforce searches:

- no SIP implementation in SDL backend;
- no SDL text-input APIs in any Window service;
- no SDL text-input APIs in Linux Window backend;
- plan-1 event lifecycle remains production owner of start/stop.

Preferred commit:

    refactor(window): move native platform services

If Windows SIP extraction is cleaner and independently coherent, use:

    fix(window,windows): decouple SIP from SDL input

Do not split by file count; use logical behavior boundaries.

Focused test strategy:

Prefer a small `scripts/test-window-event-backend-contracts.py`, or extend the
existing SDL desktop contract test if it stays focused and within size limits.
Do not create a new framework.

The test must fail if:

- Windows SIP calls SDL text-input APIs;
- macOS falls through to Linux;
- shared `Window.c` returns to legacy OS dispatch;
- adapter signatures diverge;
- SDL backend regains platform-service responsibilities.

At milestone end only:

- rebuild permitted macOS VM/Launcher;
- run macOS keyboard smoke if a current runnable sample exists;
- if a current prebuilt Windows artifact exists, run Windows smoke without a
  local Windows build.

Windows smoke:

1. focus an editable control;
2. type ASCII text;
3. type a non-ASCII character if practical;
4. execute a normal path that hides SIP;
5. type again and verify text still arrives;
6. arrows, Backspace, Enter, Tab, Escape;
7. Ctrl+C/V/X and existing SDL control shortcuts;
8. representative special/function key;
9. verify no duplicate printable characters.

If unavailable, record exactly:

    Windows source/build contract validated; interactive keyboard runtime deferred.

### Milestone 3 — Final validation and closure

Search production occurrences of:

    SDL_StartTextInput
    SDL_StopTextInput
    SDL_IsTextInputActive

Ownership must match plan 1; Window/SIP code must own none of them.

Search shared dispatchers `Event.c`, `specialkeys.c`, and `Window.c` for legacy
primary dispatch macros. Legacy macros may remain only in low-level platform
implementation details justified by SDK/compiler needs.

For every new file:

    wc -c -l <file>

Require <= 20 KB and approximately <= 600 lines.

Run:

- focused contract/native tests;
- header validation for touched/new files;
- `git diff --check` for completed range and worktree;
- commit-message validation for commits created by this plan.

Final allowed builds only now:

    cmake --build build-macos-window-event --target tcvm Launcher

    cd TotalCrossSDK
    ./gradlew-agent dist
    cd ..

Redirect verbose output to `/tmp` logs and record concise results.

Final native smoke may run only if useful after final code changes.
Inspect final-revision CI if available for Windows SDL, Windows Native + Legacy,
macOS, Linux, Android, iOS, and SDK. Do not push solely to obtain CI.

Compile CI is not Windows interactive keyboard proof.

Update state, evidence, Progress, Outcomes, and editorial report.
If closure docs need a final commit:

    docs(plan): close window service refactor

Do not mix functional code into a documentation-only commit.

## Validation and Acceptance

Complete only when:

1. `Window.c` selects backend with `TC_WINDOWING_*` and services with `TC_OS_*`.
2. Window native methods have no legacy per-OS primary dispatch.
3. SDL `Window_c.h` has no SIP/safe-area/orientation implementation.
4. Windows SDL uses Windows SIP/TabTip services.
5. Windows Native uses the same Windows service implementation.
6. macOS SDL explicitly uses SDL backend + macOS services.
7. macOS cannot fall through to Linux.
8. Linux Window backend has no SDL text-input lifecycle calls.
9. Android JNI service behavior is preserved.
10. iOS SIP/safe-area behavior is preserved and selected only by `TC_OS_IOS`.
11. WinCE SIP/IME behavior is preserved.
12. `Window.setSIP(SIP_HIDE, ...)` cannot disable SDL text events.
13. SDL text input remains owned by the plan-1 event lifecycle.
14. public Window API signatures remain unchanged.
15. all new files satisfy 20 KB / approximately 600 lines.
16. permitted macOS build passes.
17. final SDK distribution build passes.
18. no prohibited local build is executed.

## Decision Log

- Decision: backend and platform service selection are independent.
  Rationale: SDL windowing must not replace native OS services.
  Date: 2026-09-01.

- Decision: SIP, safe area, and orientation are platform services; title is a
  window-backend operation.
  Rationale: service ownership follows the facility that implements it.
  Date: 2026-09-01.

- Decision: macOS gets an explicit no-op platform-service adapter.
  Rationale: preserve current effective behavior without Linux fallthrough.
  Date: 2026-09-01.

- Decision: keep `darwin` directory names but select them with `TC_OS_IOS`.
  Rationale: directory rename is unrelated churn.
  Date: 2026-09-01.

- Decision: preserve existing Windows TabTip semantics.
  Rationale: fix responsibility/input regression without redesigning SIP.
  Date: 2026-09-01.

- Decision: Window/SIP code never controls SDL text-input lifecycle.
  Rationale: virtual keyboard visibility and physical text event activation are
  separate concepts.
  Date: 2026-09-01.

## Runtime Diagnostic Procedure

No architecture decisions are left to the agent.

If Windows input still fails after this refactor and runtime is available,
temporarily log only:

- `SDL_WINDOWEVENT_FOCUS_GAINED/LOST`;
- `SDL_KEYDOWN`;
- `SDL_TEXTINPUT`;
- `SDL_GetKeyboardFocus()`;
- `SDL_IsTextInputActive()`.

Decision procedure:

- neither keydown nor text -> investigate SDL focus/message delivery;
- keydown present, text absent -> investigate SDL text-input/IME lifecycle;
- both present, UI unchanged -> inspect TotalCross translation/posting;
- do not change mappings before raw SDL arrival is proven;
- do not add `WM_CHAR` fallback or a second event pump.

Remove diagnostics before commit.

## Idempotence and Recovery

After every commit rewrite state with prerequisite plan-1 commit, current commit,
paths, focused validation, milestone, deferrals, and exact next action.

On interruption, read state first, inspect only active paths, preserve unrelated
changes, rerun focused tests safely, and reuse the macOS build directory.
Never perform destructive cleanup.

If a milestone-end macOS build fails, stop, fix only that slice in a focused
follow-up commit, and rerun the milestone build before proceeding.

## Outcomes & Retrospective

The Window dispatcher now composes an independently selected backend and
platform-service adapter. Windows SDL and Windows Native share the extracted
Windows SIP/TabTip implementation, so `SIP_HIDE` no longer calls SDL text-input
APIs. SDL text-input lifecycle ownership remains exclusively in the Plan 1 SDL
event adapter. macOS SDL has an explicit no-op service adapter, and native
macOS no longer falls through to Linux. Android JNI, iOS SIP/safe-area, and
WinCE behavior remain behind standardized service signatures.

Focused contracts, header validation, whitespace checks, new-file size checks,
the permitted macOS Release build, and the final SDK distribution build passed.
Windows, Linux, Android, iOS, and WinCE builds were intentionally deferred by
the execution contract. No interactive Windows artifact or runnable macOS
keyboard sample was available, so interactive keyboard smoke remains deferred.

The functional change is `ab85fa051`, with closure records in `ed78a0d10` and
the final fixes in `ffdab187f` and `183fc28bb`. The rewritten and new commit
messages pass the repository format check. GitHub has no check-run for final
HEAD `183fc28bb` and reports that the SHA is unknown, so the plan remains open
until its full CI matrix is green; no push was performed.

At closure summarize only final backend/service composition, Windows SDL
SIP/input result, macOS explicit dispatch, native compatibility, validations
actually run, Windows interactive proof or explicit deferral, and final commit
range.

## Revision Note

Initial plan written 2026-09-01 as the second sequential stage of the Window/event
responsibility refactor.
