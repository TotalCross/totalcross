<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# P7 Scroll Raster Reuse Report

## Summary

P7 adds conservative vertical framebuffer reuse for `ScrollContainer`. It
moves the already-painted viewport area and repaints the newly exposed strip
only when surface, transform, damage, and backend checks all pass. Any
uncertainty or failure uses full repaint. The typed policy is false by default;
there is no public enable switch.

Reuse is vertical-only and limited to software raster surfaces. The native
move is bounded, stride-aware, overlap-safe, and operates in place on the
existing 32-bit framebuffer. Pending damage that cannot be preserved, overlays
that cross the viewport, transparent scrollbars, unsupported transforms, and
failed moves select full repaint. A failed move or strip repaint triggers
immediate recovery while retaining the applied scroll position.

## Architecture

`ScrollContainer` keeps its existing scrollbar, bag-position, and flick
semantics. After an applied vertical delta, `ScrollRasterReuse` validates the
surface and computes the physical viewport, preserved source/destination, and
exposed strip. The successful move is followed by normal active-window
painting clipped to that strip. Horizontal scrolling follows the existing
repaint path.

Native reuse above content scale one remains disabled. The Java simulator path
uses overlap-safe row copies. Native backends without the supported legacy
software raster primitive return unavailable and repaint fully.

RENDERING counters are recorded through `RuntimeDiagnosticsFeatureBridge` in
the optional diagnostics implementation. Metric slots and IDs remain internal;
`RuntimeDiagnostics` exposes only support status, domain enablement, and
snapshots. Diagnostics do not affect eligibility, and the default-off path
does not record an attempt or fallback or inspect the framebuffer.

## Validation

| Check | Result |
| --- | --- |
| Combined P6/P7 raster, copyRect, cache, diagnostics, converter, and artifact-boundary tests with diagnostics disabled | Passed |
| The same combined tests with `runtimeDiagnostics=true` | Passed |
| `artifactContentTest` | Passed |
| `dist -x test` | Passed |
| macOS ARM64 P6 fast-path and warm-path smokes | Passed: cached-final and plan-aware copies, full/partial clip pixel parity, and direct physical copy |
| macOS ARM64 diagnostics-enabled P6 fast-path smoke | Passed: IMAGE diagnostic delta was 4 for the clipped direct copy |
| macOS ARM64 P7 default/Skia smoke | Passed: default-off repaint, both scroll directions, near-viewport parity, and conservative scale fallback matched full repaint |
| macOS ARM64 P7 legacy software smoke | Passed: framebuffer parity and direct positive/negative native move checks; scale fallback matched full repaint |
| macOS ARM64 native Release builds | Passed: Skia, legacy software, and Skia with runtime diagnostics enabled |
| Copyright-header validation and `git diff --check` | Passed |

No performance benchmark was run. The scroll smokes establish framebuffer
correctness, not throughput or frame-rate improvement.

## Limitations

Native scrolling at content scale greater than one is conservatively
unsupported. Transparent scrollbars and overlays that cross the viewport select
full repaint. No performance benefit is claimed. P7 adds no GPU/compositor
reuse, Image decoding change, P6 source-subrect implementation, or threading.

## Relationship to P6

P6 copies a source subrectangle from Image backing; P7 relocates pixels already
present in the destination framebuffer. The implementations remain separate
despite sharing Graphics and native raster code. Combined regressions cover
both paths and their independent fallback behavior. P7 adds no dependency on
P6's source-subrectangle APIs.
