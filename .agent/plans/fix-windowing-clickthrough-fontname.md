<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Plan: desktop windowing and font fixes

This ExecPlan follows `.agent/PLANS.md` and `AGENTS.md`.

## Purpose / Big Picture

Make desktop startup behavior deterministic and make the SDL desktop window
interact correctly with the pointer while preserving the existing application
contract. At the same time, make Skia and legacy font loading explicit,
deterministic, and testable, including bold text.

The observable result is a desktop window whose initial size, position,
fullscreen state, and environment overrides are resolved before SDL creates
the window, while mouse clicks can pass through when the desktop backend
requests it. Font loading must select the intended renderer path, resolve TTF
files reproducibly, reject invalid inputs safely, maintain stable typeface
indices, and honor bold measurement and rendering.

## Working Set and Resume Protocol

The implementation is concentrated in:

- `TotalCrossVM/src/init/startup.c`, `tcsdl.cpp`, and
  `nm/ui/WindowStartup.*` for startup resolution;
- `TotalCrossVM/src/nm/ui/skia/*`, `font_Font.c`,
  `font_FontMetrics.c`, and PalmFont helpers for font behavior;
- native startup, ABI, integration, and Skia fixtures under
  `TotalCrossVM/src/tests` and `TotalCrossVM/src/nm/ui/skia`;
- `TotalCrossSDK/src/main/java/totalcross/ui/font/Font.java` for the
  public font documentation and renderer-facing contract.

Build output, downloaded dependencies, test logs, and screenshots remain
outside versioned source. On resumption, inspect the current worktree,
the active source paths, and the most recent focused validation log before
expanding the investigation.

## Progress

- [ ] Consolidate desktop startup resolution and its native tests.
- [ ] Isolate SDL click-through configuration.
- [ ] Separate and harden Skia/legacy font loading and bold behavior.
- [ ] Harden shared native ABI boundaries and retain compatibility-sensitive
  public APIs.
- [ ] Run the acceptance matrix and write the final factual report.

## Current Architecture and Scope

Desktop startup is resolved through the native path
`startup.c` → `desktopWindowStartupOptions` →
`windowLoadStartupEnvironment()` →
`windowResolveStartupConfiguration()` → `TCSDL_Init()`. The resolver
combines command-line `/scr`, `TC_WIDTH`, `TC_HEIGHT`, application
attributes, display metrics, fullscreen settings, and platform defaults before
SDL window creation. Command-line startup options must not leak into the
application argument list.

The native UI layer shares C structs and declarations with C++, Objective-C,
and Objective-C++. These boundaries require stable fixed-width data fields and
integer boolean representations where a signature crosses languages. Public
third-party callback ABIs are compatibility-sensitive and remain outside this
change unless their internal-only status is proven.

Font creation has two renderer paths. Skia needs deterministic TTF resolution,
validation, registry ownership, stable indices, and explicit bold propagation.
The legacy PalmFont path must remain separate and preserve its own fallback
rules. The Java-facing `Font` contract documents the distinction.

## Plan of Work

1. Resolve desktop startup behavior in one cohesive implementation. Add
   explicit fullscreen/default handling, command-line and environment
   precedence, sizing, centering, and argument stripping. Keep the
   WindowStartup ABI representation compatible across C and C++ from this
   change.
2. Add the SDL mouse click-through hint as an isolated backend change.
3. Consolidate the font loading work: separate Skia and legacy loading,
   resolve TTF files deterministically, validate inputs, preserve fallback,
   and update the focused font fixtures.
4. Harden the Skia typeface registry with dynamic storage, stable indices,
   invalid-TTF rejection, and registry tests.
5. Complete bold propagation through measurement and drawing, reset state
   correctly, update pixel and metric fixtures, document the final
   `Font` behavior, and remove obsolete warnings.
6. Harden only proven internal C/C++/Objective-C boundaries. Replace shared
   boolean fields with fixed-width values, normalize internal boolean
   parameters and results, and add compile-time and runtime ABI probes.
7. Produce one final report describing the delivered changes, decisions,
   limitations, and evidence.

## Decision Log

- Keep `/scr` as the highest-priority explicit startup size source; use
  environment values independently for width and height when command-line
  sizing is absent or partial.
- Represent shared boolean fields as `uint8` and cross-language boolean
  parameters/results as `int32`, converting at the implementation boundary.
- Preserve public native-library, scanner, synchronization, and other
  exported callback ABIs when their compatibility surface cannot be proven
  internal.
- Keep Skia typeface registration owned by the native registry rather than
  relying on a fixed-size table or renderer-specific lookup side effects.

## Validation and Acceptance

Use focused validation first, then the smallest complete family that proves
the behavior:

- build the native VM and the C/C++ ABI probes on macOS arm64;
- run the existing WindowStartup tests and a real SDL integration harness;
- verify explicit, centered, environment-only, and mixed startup dimensions;
- verify command-line option removal from the application arguments;
- run the Skia surface/typeface fixture with a real TTF, including plain and
  bold measurement/rendering;
- run the relevant SDK build and font-focused checks;
- run copyright-header validation and `git diff --check`.

Record full tool output in temporary logs and report compact outcomes only.
Platform builds requiring unavailable credentials, pods, or external services
must be identified as limitations rather than inferred as passing.

## Risks and Open Questions

- Platform-specific startup defaults can differ between Linux, Windows,
  macOS, and mobile targets; keep platform selection explicit and testable.
- Static archives and language linkage can hide ABI mismatches until a target
  is linked, so both compile-time layout assertions and mixed-language
  runtime probes are required.
- Font files may be missing, malformed, or duplicated across search paths;
  fallback and rejection behavior must remain deterministic.
- Rewriting renderer state can accidentally alter legacy rendering; fixtures
  must cover both renderer paths where available.

## Idempotence and Recovery

All source changes are ordinary commits and can be rebuilt from the merge-base.
Generated build directories, fetched dependencies, logs, and screenshots are
not source inputs and must not be committed. Before any history operation,
preserve the current branch pointer and local untracked work. If a validation
fails, retain its log, repair only the affected slice, and rerun the focused
command.

## Outcomes & Retrospective

Complete this section in the final report rather than during the initial
implementation. It must distinguish implemented behavior from platform work
that could not be executed locally.
