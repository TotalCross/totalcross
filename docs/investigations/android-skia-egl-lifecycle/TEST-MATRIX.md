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

## Milestone 4 — first API 30 checkpoint (superseded)

| Case | Result |
| --- | --- |
| ARM64 release build with diagnostics/test hooks | PASS, build3; 86 tasks, 13 executed |
| EGL cleanup policy | PASS, 5 direct C test cases |
| Exact app denied phase | PARTIAL; Settings opened with process alive, then existing ADB Back stall |
| Marker app surface replacement | PASS ordering once; old-context cleanup ended before EGL teardown and replacement Skia init |
| Permission filesystem assertions | INCOMPLETE; create/delete/error markers not established for all three states |
| Rotation/background/foreground | PARTIAL PASS; callbacks and responsiveness observed, no fatal signal |
| Lost-context / replacement-init-failure injection | NOT RUN; test hooks were not reached |
| Distinctive rendering/pixel assertion | NOT PASS; screenshot was blank white |

This table records the first M4 interval; the continuation below supersedes
these statuses where it contains newer evidence.

## Milestone 4 — resumed API 30 validation, earlier checkpoint (superseded)

| Case | Result |
| --- | --- |
| Diagnostics/test-enabled ARM64 Release build | PASS; 86 tasks, 13 executed; build log in `/tmp/codex-android-skia-egl-m4-recovery-build.log` |
| Default-off ARM64 Release build | PASS; 86 tasks, 13 executed; no diagnostic/test options enabled |
| Cleanup policy C test | PASS; valid/current, non-current rebind, lost, unavailable/bind failure, binding verification failure |
| Actual old EGL context destruction | PASS; both unbind and destroy succeeded, abandon cleanup completed, replacement EGL and Skia initialized |
| Failed replacement EGL initialization | PASS after readiness guard; failed attempt kept process alive and later callback restored EGL/Skia |
| Partial Skia surface initialization failure | PASS with test-only null-surface injection; cleanup completed and later surface callback restored graphics |
| Repeated empty cleanup | PASS; second cleanup begin/end completed without crash |
| Five consecutive EGL replacements | PASS; force hook plus five natural rotation changes; ordered cleanup/destroy/create/init evidence each iteration |
| Existing/shared and post-recreation images | PASS in diagnostics build; exact PNG pixel counts after every replacement |
| Default-off startup/background/foreground/termination | PARTIAL; process started, returned from Home, and terminated; captured resumed screen did not retain the full expected content layout |
| Original customer grant-in-place filesystem operation | NOT_RUN in resumed interval; M3 candidate passed but is not permanent-build evidence |
| Original customer denied-return filesystem operation | NOT_RUN in resumed interval; M3 candidate passed but is not permanent-build evidence |
| Original customer pregranted filesystem operation | NOT_RUN in resumed interval; M3 candidate passed but is not permanent-build evidence |
| Original customer and marker-only app on resumed production library | NOT_RUN; the application-level graphics probe used the resumed library |
| Corrected customer app native-crash check | NOT_RUN for permission scenarios; graphics-probe recreation and exceptional-path checks had no fatal signal after the guard |
| TCUI missing resources and ADB Back timeout | OPEN independent findings; neither was corrected |

This earlier M4 checkpoint is superseded by the final resumed cases below.
Milestone 5 and the API 29/30/35/36 matrix have not started.

## Milestone 4 — final resumed API 30 validation

| Case | Result |
| --- | --- |
| Reused default-off ARM64 Release runtime and compatible artifacts | PASS; embedded runtime SHA matches `c86831b5…`, test TCUI matches the recorded overlay, customer APK signatures verify |
| Exact customer / grant in place | PASS; Settings returned to same PID, app-op `allow`, no native crash |
| Exact customer / denied return | PASS; returned to same PID, app-op `ignore`, no native crash |
| Exact customer / pregranted | PASS; app-op `allow` before launch, no Settings screen, same live process, no native crash |
| Marker app / grant in place | PASS; same process, `/sdcard/asd` created and deleted, app-op `allow`, no native crash |
| Marker app / denied return | PASS; same process, expected `Operation not permitted`, app-op `ignore`, no native crash |
| Marker app / pregranted | PASS; `/sdcard/asd` created and deleted, app-op `allow`, no Settings screen, no native crash |
| Default-off initial probe rendering | PASS; exact expected red/yellow, blue/white, green/magenta RGB counts; all `debug.tcvm.*` hooks off |
| Default-off portrait after orientation round-trip | PASS; exact portrait counts match the initial capture; read-only Java lifecycle INFO logging was enabled |
| Default-off newly created image and continued interaction | PASS; cyan/black image pixels present; retained/shared image counts unchanged; same process responsive |
| Logging-suppressed pixel repetition | NOT_RUN; logger property set to `SUPPRESS` after capture, no rerun after 28-minute experiment cutoff |
| EGL replacement in default-off run | NOT_RUN as an EGL replacement assertion; surface generations 1–4 and callbacks are logged, but no EGL destroy/create events |
| Shared `initSkia()` return-contract review | PASS; all three direct callers check the result. Non-Android software initialization failure now propagates; compile affected non-Android backends in final PR review |
| API 29/30/35/36 matrix and Milestone 5 | NOT_RUN; require explicit approval |
| Missing TCUI resources and full-harness ADB Back timeout | OPEN independent findings; not investigated or corrected |

See `evidence/M4-api30-default-off-closeout.md` for artifact identities and
scenario evidence, `evidence/M4-api30-default-off-pixel-assertions.json` for
exact screenshot assertions, and the four `evidence/M4-api30-defaultoff-*.png`
captures. The accepted diagnostic-enabled five-replacement sequence remains
in `evidence/M4-api30-five-replacements.json`.

## Milestone 5 — API 36 authorized slice

| Case | Expected assertions | Status |
| --- | --- | --- |
| ARM64 API 36 environment and artifact provenance | Hardware-accelerated emulator; default-off APKs embed the reused release runtime | PASS |
| Exact customer, grant in place | Settings return, app-op `allow`, same live PID, no native crash | PASS |
| Exact customer, denied return | Settings return, app-op `ignore`, same live PID, no native crash | PASS |
| Marker, grant in place | App-op `allow`; create/delete `/sdcard/asd`; same process | PASS |
| Marker, denied return | App-op `ignore`; expected error 13; no directory; same process | PASS |
| Marker, pregranted | App-op `allow` before launch; create/delete `/sdcard/asd` | PASS |
| Retained/shared image rendering | Exact red/yellow, blue, green, and magenta pixels after transitions | PASS |
| Rotation and Home/foreground | Same process returns responsive; exact image counts retained | PASS |
| Settings return and new image | Retained/shared counts remain; new cyan image and black interior asserted | PASS |
| Default-off EGL replacement event assertion | Logging suppressed in default-off run; replacement itself not recorded | NOT_RUN |
| Equivalent diagnostic Settings-return replacement | Cleanup end precedes EGL destroy; new EGL current precedes Skia init end | PASS |
| Native crash scan | No `Fatal signal`, `SIGSEGV`, `SIGABRT`, `FATAL EXCEPTION`, or `Abort message` matches | PASS |
| API 35 and API 29 | Await explicit authorization; no tests started | NOT_RUN |

The portrait orientation round-trip consistently shifts all image bounds down
65 pixels while preserving their exact counts; post-transition captures use
matching shifted sample coordinates. Artifact identities, permission states,
filesystem assertions, hashes, pixel results, screenshots, and the diagnostic
event excerpt are in `evidence/M5-api36-validation.md`,
`evidence/M5-api36-scenario-results.json`, and
`evidence/M5-api36-pixel-assertions.json`.
