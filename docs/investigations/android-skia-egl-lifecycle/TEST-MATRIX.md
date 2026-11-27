<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Test matrix

Statuses distinguish historical evidence from this campaign. Current-baseline
cases without recorded assertions remain not run.

## Milestone 1 — API 30 baseline

| App / case | Permission and lifecycle | Expected assertions | Status |
| --- | --- | --- | --- |
| Original customer app | Denied, then return from Settings | Startup, actual denied state, expected error, app completion or native crash | Permission screen opened; trace variant recorded expected `Operation not permitted`; no graphics crash in denied phase |
| Original customer app | Grant while Settings is open | Permission granted, create/delete markers or equivalent, app completion, no native crash | Reproduced SIGSEGV after return; exact APK logcat contains fatal signal in `initSkia` during `nativeSurfaceChanged` |
| Original customer app | Permission pregranted | Create/delete succeeds and app remains functional | Trace control created/deleted `/sdcard/asd`; same process remained alive; no native crash |
| Diagnostic variant | Denied and grant-in-place | Markers, permission state, completion, native stack | Denied operation reported expected error; grant-in-place created/deleted directory, then reproduced matching native stack |
| Diagnostic variant | Background/foreground and surface recreation | Surface recreation, visible rendering, process completion | Settings return exercised surface callback and reproduced crash; manual rotation and standalone background/foreground not run |

All six exact/trace phases ran on API 30 after a disposable test-package overlay
added the two same-name Material assets missing from the Android resource path.
The native crash is a reproduced failure, not a passing validation. In the
exact phase the harness summary reported the same PID while logcat captured a
fatal SIGSEGV for that PID; use the native log as the process outcome. Full
assertions and artifact provenance are in evidence/M1-api30-reproduction.md.

## Historical I03 results

| API | Historical result on official 7.3.0 | Current-master result |
| --- | --- | --- |
| 29 | No native graphics crash; legacy permission flow did not grant required write access | Not run |
| 30 | Native graphics crash in denied and grant-in-place flows | Reproduced on baseline in grant-in-place return; see M1 evidence |
| 35 | Native graphics crash in denied and grant-in-place flows | Not run |
| 36 | Native graphics crash in denied and grant-in-place flows | Not run |

## Final local emulator matrix

The user-requested final campaign covers ARM64 APIs 29, 30, 35, and 36. It
starts only after production implementation and explicit authorization to
continue. Record AVD image, ABI, renderer, SDK/runtime/APK identity, scenario,
permission state, visual assertion, completion, and crash result for each
completed case. Do not interpret API 29 legacy permission behavior as a
graphics failure.
