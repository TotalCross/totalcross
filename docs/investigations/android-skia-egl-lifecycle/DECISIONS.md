<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Decisions

## Reuse the existing target branch

The requested local and remote branch already exists, is clean, and equals
origin/master at 52a947b5295321d7350350bd618c55fb26304c00. Reuse that isolated
worktree rather than recreate, move, or overwrite a published branch.

## Current-source runtime artifacts are required

The old 7.3.0 package is historical evidence only. Build the test SDK and
Android ARM64 runtime from the recorded current-master baseline.

## Keep the causal gate before production changes

The suspected EGL-before-Skia destruction ordering is not proven by static
source or the old stack. Milestone 2 instrumentation and Milestone 3 controlled
comparison must establish causality before any production correction.

M2 diagnostics show `surface.reset()` destroying the old GPU surface and
flushing queued work while the replacement EGL context is current. M3 changed
only the cleanup ordering in an isolated candidate: old screen Skia cleanup
ran while the old EGL context remained current, before existing EGL teardown
and replacement initialization. Grant-in-place passed six times total, and
denied-return, pregranted, and rotation/background/foreground controls passed.
Old-context cleanup completed before EGL destruction, and replacement Skia
initialization completed. This confirms the cause for the exercised API 30
paths. Pause for explicit review before any production change.

For the pinned Skia `158dc9d7` APIs, use
`releaseResourcesAndAbandonContext()` only while the native context is still
valid, before destroying it. If already lost or unavailable, use
`abandonContext()` before dropping the old GPU surface/resource wrappers so
their destructors cannot call the replacement backend. Keep lifecycle cleanup
on the existing graphics-owning thread; M3 does not authorize broader context
loss redesign.

## Apply the local emulator restriction

All runtime execution is local on hardware-accelerated ARM64 emulators. No
physical device, GitHub Actions runtime, remote emulator, or other platform
test is in scope.

## Use a disposable UI-resource overlay for the baseline run

The current Android resource path lacks floatingFrg.png and outlined_edit.png,
which are eagerly loaded before the customer flow. For M1 only, use the
same-name Material assets in a temporary TCUI archive so the unchanged
customer app can reach the permission transition. Keep the overlay out of
production source and treat the result as qualified by this test input.
