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

## Milestone 2 — ordered lifecycle diagnostics

| App / case | Permission and lifecycle | Expected assertions | Status |
| --- | --- | --- | --- |
| Exact app | Grant in Settings, return to app | Callback thread, EGL/Skia identities, crash stack | Reproduced SIGSEGV during `destroySkiaScreen`; EGL context replaced while old Skia resources remained |
| Trace app | Grant in Settings, return to app | Create/delete markers, identities, native completion | `/sdcard/asd` create/delete succeeded before SIGSEGV; reset-end event absent |
| Trace app control | Permission pregranted | Create/delete markers, matching EGL/Skia context, process alive | Created/deleted `/sdcard/asd`; same process remained alive and reset completed with its EGL context current |
| Full harness | All six exact/trace phases | Complete phase matrix | Incomplete: ADB timed out sending Back after exact phases; focused trace phases above were run directly |

Compact sequence, crash stack, build identity, and full-log location are in
evidence/M2-api30-lifecycle-diagnostics.md and
evidence/M2-api30-lifecycle-sequence.log.

## Milestone 3 — controlled API 30 candidate

| App / case | Permission and lifecycle | Expected assertions | Status |
| --- | --- | --- | --- |
| Trace candidate | Grant in Settings, return in place | Cleanup under old current EGL context before teardown; replacement init; file operation; no crash | PASS, initial run and five additional repetitions; create/delete succeeded |
| Trace candidate | Return without granting | Denial state/error; ordered cleanup; replacement init; no crash | PASS; expected `Operation not permitted` |
| Trace candidate | Permission pregranted | File operation; no prompt; app remains functional | PASS; create/delete succeeded |
| Customer candidate | Rotation, background, foreground | Surface recreation, replacement init, app responsiveness, no crash | PASS; one control, ROTATION_90 and Home/resume observed |
| Visual check | After grant-in-place surface recreation | Visible customer content | PASS; screenshot preserved in `evidence/M3-api30-candidate-rendered.png` |

All candidate cases used the existing API 30 ARM64 emulator, diagnostic
instrumentation, customer reproducer, and disposable TCUI overlay. The M2
instrumented baseline evidence was reused without rebuilding. Ordered events
and repeat results are in `evidence/M3-api30-candidate-sequence.log`; the
candidate patch and provenance are indexed in `evidence/index.md`. Results
confirm causality for these paths, not the full Android API matrix.

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
