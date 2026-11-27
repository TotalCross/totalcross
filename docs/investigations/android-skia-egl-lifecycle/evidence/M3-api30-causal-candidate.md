<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Milestone 3 — controlled EGL/Skia cleanup candidate

## Experiment

Compared the retained M2 diagnostic APK/logs at
`7b86b52b00a1933453581db575af76f774a42c05` with an isolated diagnostic
candidate. The candidate added one compile-time-only operation: on the existing
graphics/event thread, when an initialized native window is being replaced,
call `destroySkiaScreen()` while the old EGL context is still current, then
continue the existing EGL destruction, native-window release, and replacement
EGL/Skia initialization sequence.

The candidate was preserved in
`diagnostic-patches/M3-pre-egl-skia-cleanup-candidate.patch.gz`. Candidate-only
source edits were removed after the run; no production implementation is in
the source tree. The candidate retained M2 logging, generation handling,
callback ownership, native-window reference handling, and replacement context
initialization. No synchronization, delay, global invalidation, or unrelated
graphics change was introduced.

## Results

The M2 trace grant-in-place run created and deleted `/sdcard/asd`, then crashed
after the old EGL context was destroyed and the old Skia surface reset under
the replacement context. The M2 exact-app denied-return log also records a
SIGSEGV during surface recreation. These retained results were reused; no M2
baseline bundle or APK was rebuilt.

For the candidate, the first focused grant-in-place phase and five additional
repetitions all passed. Each run applied the permission while Settings was
open, created and deleted `/sdcard/asd`, retained its process, logged
`skia.surface.reset.end` before `egl.destroy.begin` with the old EGL context
current, and logged replacement `skia.init.end` and Java surface acceptance.
None contained the original SIGSEGV.

The candidate denied-return phase reported the expected `Operation not
permitted`, retained its process, completed old-context Skia cleanup before
EGL destruction, and initialized the replacement surface without a native
crash. The pregranted control created/deleted the directory and stayed alive.

One rotation/background/foreground control observed `ROTATION_90`, Home pause,
foreground resume, subsequent surface callbacks, and multiple successful Skia
initializations. During the recreated-surface path, old-context Skia cleanup
ended before EGL teardown. The process remained alive and focused on the app;
no native fatal signal was found. Rotation settings were restored afterward.
The captured post-grant screen shows the live white customer test surface;
see `M3-api30-candidate-rendered.png`.

This controlled change establishes the cause for the reproduced API 30 paths:
moving only old screen Skia cleanup before EGL teardown removed the crash while
preserving the rest of the surface replacement path. Evidence is limited to
the local API 30 ARM64 emulator and these exercised flows; this is not the
production implementation or the final platform matrix.

## Skia context loss contract

The dependency manifest pins Skia version `158dc9d7`, release
`skia-158dc9d7-r8`. Its public `GrDirectContext` header documents two distinct
cleanup states:

- `releaseResourcesAndAbandonContext()` is for a still-valid underlying 3D
  context. It releases allocated resources before returning and then assumes
  the native context may no longer be valid. It must run before EGL destroys
  that context.
- `abandonContext()` is for a lost or unavailable underlying context. Skia
  marks its GPU resources abandoned so context/resource destructors will not
  call the backend 3D API. Unflushed content may be lost; subsequent calls are
  no-ops or fail.

`SkSurface::recordingContext()` exposes the associated recording context, and
the pinned headers provide `GrAsDirectContext()` for the direct context created
by `GrDirectContext::MakeGL`. The narrow production contract is therefore:
release the old screen Skia surface and its GPU work on the owning graphics
thread while its old EGL context is current, before destroying the EGL context
or releasing the old window. If that EGL context is already lost/unavailable,
abandon its `GrDirectContext` before dropping screen-surface/resource wrappers;
do not let their destructors issue GL work against a replacement context.
This contract does not add a broader graphics context-loss redesign.

## Independent findings and artifacts

The missing `floatingFrg.png` and `outlined_edit.png` Android resources remain
an independent test-input defect; the existing disposable TCUI overlay was
reused and not changed in source. The M2 all-phases harness ADB Back timeout
also remains independent; M3 used focused phases and did not modify that
harness.

Candidate AAB SHA-256:
`863aec3bcc5db31258919b1577cdae21b40f374a44def769c2e9b41864056e54`.
Candidate disposable SDK package SHA-256:
`e43b629ddd060f0b50dae747d296ee133a8040a62a189eff87c7675d12ea61d2`.
The candidate APK/source hashes and focused logcats remain outside Git under
`/var/folders/k8/02b7wfkd7fn32vtm3t5mwxwr0000gn/T/codex-android-skia-egl-m1-9h0ksa2a/results-m3-candidate`.
The compact ordered sequence and five-repetition summary are in
`M3-api30-candidate-sequence.log`.
