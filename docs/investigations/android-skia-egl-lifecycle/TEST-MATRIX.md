<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Test matrix

Statuses distinguish historical evidence from this campaign. Current-master
cases remain not run until a result and its assertions are recorded.

## Milestone 1 — API 30 baseline

| App / case | Permission and lifecycle | Expected assertions | Status |
| --- | --- | --- | --- |
| Original customer app | Denied, then return from Settings | Startup, actual denied state, expected error, app completion or native crash | Blocked before prompt by missing floatingFrg.png |
| Original customer app | Grant while Settings is open | Permission granted, create/delete markers or equivalent, app completion, no native crash | Blocked before prompt by missing floatingFrg.png |
| Original customer app | Permission pregranted | Create/delete succeeds and app remains functional | Blocked before customer operation by missing floatingFrg.png |
| Diagnostic variant | Denied and grant-in-place | Markers, permission state, completion, native stack | Blocked before customer operation by missing floatingFrg.png |
| Diagnostic variant | Background/foreground and surface recreation | Surface recreation, visible rendering, process completion | Not run; startup blocker prevents valid scenario |

Six API 30 phases were attempted. The app remained on an unhandled resource
error dialog; process liveness is not a pass. No graphics conclusion is drawn.

## Historical I03 results

| API | Historical result on official 7.3.0 | Current-master result |
| --- | --- | --- |
| 29 | No native graphics crash; legacy permission flow did not grant required write access | Not run |
| 30 | Native graphics crash in denied and grant-in-place flows | Not run |
| 35 | Native graphics crash in denied and grant-in-place flows | Not run |
| 36 | Native graphics crash in denied and grant-in-place flows | Not run |

## Final local emulator matrix

The user-requested final campaign covers ARM64 APIs 29, 30, 35, and 36. It
starts only after production implementation and explicit authorization to
continue. Record AVD image, ABI, renderer, SDK/runtime/APK identity, scenario,
permission state, visual assertion, completion, and crash result for each
completed case. Do not interpret API 29 legacy permission behavior as a
graphics failure.
