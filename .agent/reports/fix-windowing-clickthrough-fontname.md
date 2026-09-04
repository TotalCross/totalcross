<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Report: desktop windowing and font fixes

## Result

The branch history was consolidated into one initial plan, six focused
implementation commits, and one final report. The functional tree is
equivalent to the completed implementation before reconstruction. The only
source-tree differences are the deliberate replacement of intermediate
planning files with the consolidated plan and this report.

## Objectives and delivered changes

Desktop startup now has one explicit resolution path from native startup
options through environment loading, shared configuration resolution, and SDL
window creation. It covers fullscreen defaults and overrides, `/scr`,
`TC_WIDTH`, `TC_HEIGHT`, partial dimensions, centering, sizing, source
precedence, and removal of startup options before application arguments are
delivered. The SDL click-through hint is isolated in its own change.

The font pipeline keeps Skia and legacy PalmFont loading separate. Skia TTF
lookup is deterministic, malformed font input is rejected safely, fallback is
preserved, and the typeface registry owns stable dynamic indices. Bold state
is propagated through measurement and rendering, and stale bold state is not
retained between calls. The final Java `Font` documentation describes the
renderer-specific behavior and no longer carries the obsolete warning.

Shared native structures use fixed-width boolean fields where they cross C and
C++. Internal C/C++/Objective-C boolean parameters and results use `int32`
with normalization at the implementation boundary. Compile-time layout
assertions and mixed-language runtime probes cover the changed contracts.
Compatibility-sensitive public native-library, scanner, synchronization, and
`TC_API` boolean ABIs were intentionally left unchanged.

## Problems found and decisions

The original branch mixed implementation commits with replacement fixes and
multiple plans, states, and editorial reports. The intermediate planning
files were removed from the new branch history. Startup-related ABI changes
were folded into the startup commit, the SDL hint remained isolated, font
work was grouped by loading, registry, and bold behavior, and general ABI
hardening was kept in its own final implementation commit.

The final ABI changes were limited to proven internal boundaries. Boolean
fields and signatures confined to one language were retained. External
callback contracts were not changed because their compatibility surface could
not be proven internal.

## Validation

All validation below ran on macOS arm64 against the reconstructed tree:

- CMake configuration and Ninja build of `tcvm`, WindowStartup tests,
  integration test, ABI probes, and Skia fixture: passed.
- `window_startup_native_test`: passed.
- C and C++ WindowStartup ABI probes: passed.
- C and C++ GraphicsPrimitives layout probes and bridge probe for every
  boolean combination: passed.
- Explicit `window_startup_integration_test`: passed.
  - `/scr -1,-1,1024,768`: `1024x768`.
  - `/scr -2,-2,800,600`: `800x600`, centered.
  - `/scr` precedence over environment: `1024x768`.
  - Both environment dimensions: `900x700`.
  - Width-only environment: `900x558`.
  - Height-only environment: `864x700`.
  - Application arguments contained `App.tcz /cmd appArg`; `/scr` was
    absent.
- Skia fixture with a real Arial TTF: registry, plain/bold style, and surface
  copy assertions passed.
- SDK distribution build using `gradlew-agent dist -x test`: passed.
- Copyright-header validation and `git diff --check`: passed.

The full iOS application archive was not part of this local acceptance run;
that flow requires external CocoaPods and platform application dependencies.
The native macOS build and all focused cross-language fixtures completed.

## Worktree and recovery

No push or force-push was performed. The original branch pointer was backed up
locally before reconstruction. Generated build output, dependency checkouts,
logs, screenshots, and unrelated pre-existing untracked work remain outside
the new history and are not part of the deliverable.

## Final outcome

RESULTADO: PASSOU
