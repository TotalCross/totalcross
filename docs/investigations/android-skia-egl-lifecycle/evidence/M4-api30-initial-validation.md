<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Milestone 4 — API 30 initial validation

The ARM64 release build passed with default-off lifecycle diagnostics and
test-only hooks enabled (86 tasks; 13 executed, 73 up-to-date). Full log:
`/tmp/codex-android-skia-egl-m4-build3.log`. A later experimental test-only
render helper failed compilation because it referenced diagnostic identities
before declaration; that helper was reverted. Build3 corresponds to the
committed source state.

The focused C cleanup-policy test passed five cases: valid/current, valid but
non-current with rebind, invalid context, failed rebind, and failed binding
verification. It was compiled with C99, `-Wall -Wextra -Werror` and run
directly; see `/tmp/android_skia_egl_lifecycle_test`.

The existing API 30 ARM64 emulator and disposable TCUI overlay were reused.
The exact app's denied phase reached Android Settings with its process alive,
then the existing ADB Back step stalled. The harness was not modified.

A focused marker-app run observed one same-process surface replacement. Native
events show valid/current Skia cleanup ending before EGL teardown, followed by
replacement Skia initialization. Rotation/background/foreground callbacks
remained responsive and no fatal signal appeared. Compact/full logs and APKs
remain outside Git under `/tmp/codex-android-skia-egl-m4/`.

Validation remains partial. Filesystem markers for all permission states,
repeated actual EGL replacement, test-injected destruction of a real EGL
context, failed replacement initialization and recovery, and distinctive
post-recreation pixel assertions were not completed. The captured screenshot
is a blank white surface and is not rendering proof. Do not begin Milestone 5
until the outstanding focused M4 checks are completed and reviewed.
