<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Scroll raster reuse

## Objective and scope

Reuse pixels already present in the main-window software raster when a
`ScrollContainer` moves vertically. Move only the retained viewport area and
repaint the newly exposed strip. Keep the existing full repaint as the result
for disabled policy, unsupported surfaces, conflicting damage, and every
uncertain or failed operation.

The implementation is vertical-only and software-raster-only. It does not add
GPU/compositor reuse, Image source-subrect drawing, asynchronous work, or a
public policy switch.

## Architecture

`ScrollContainer.internalScrollContent` preserves the existing scrollbar,
bag-position, and flick updates. It considers raster reuse only after an
applied vertical delta. The typed `ScrollRasterReuse` policy is read first; its
production default is false. When disabled, the method returns to the legacy
repaint path without diagnostics or reuse-state changes.

`ScrollRasterReuse` performs deterministic eligibility and rectangle planning.
It converts the clipped client viewport and scroll delta to physical pixels,
checks arithmetic and framebuffer bounds, and computes source, destination,
and exposed-strip rectangles. The native operation is a bounded,
stride-aware, overlap-safe move within the existing 32-bit raster surface.
The Java simulator uses row-wise `System.arraycopy` with overlap-safe order.

After a successful move, the normal active-window painting path is clipped to
the exposed strip. Existing overlays and damage that cannot be composed safely
select full repaint. If movement or strip painting fails after the logical
scroll has applied, the active windows are repainted immediately. The logical
scroll position is retained.

RENDERING observations use internal slots through
`RuntimeDiagnosticsFeatureBridge`; public diagnostics exposes only support
status, domain enablement, and snapshots. Collection is gated by the rendering
domain and does not affect eligibility.

## Eligibility and fallback rules

Reuse requires all of the following: enabled policy, raster backend, a
non-zero vertical delta smaller than the viewport, an integral supported
content scale, a visible bounded client viewport, valid 32-bit framebuffer
pixels and stable stride, no pending or in-progress conflicting paint, and a
working bounded move operation. Any failed check follows the legacy full
repaint path.

Native reuse at content scale greater than one is conservatively disabled.
Overlays crossing the viewport and transparent scrollbars also select full
repaint. These restrictions favor exact output over partial reuse.

## Verification design

Cover the pure planner's sign, clipping, overlap order, scale conversion,
overflow, and rejection cases. Compare both scroll directions, repeated moves,
near-viewport deltas, and forced move failure against full repaint. Verify that
the disabled policy leaves rendering counters and internal reuse state
untouched, and that the move-failure hook does not change eligibility or fail
an ineligible move.

Run focused SDK tests with diagnostics disabled and enabled, then
`artifactContentTest`, `dist -x test`, and the native macOS ARM64 correctness
smokes for the default and legacy software configurations. The latter also
checks full-repaint parity for conservative scale fallback and direct native
move behavior where available.

## Limitations

The deployed native path is limited to scale one. Scale two and higher remain
unsupported until native logical-to-physical painting is proven equivalent.
The default remains off. No performance benefit is claimed without a benchmark
that exercises this path.

## P6 integration gate

Before merge, integrate the final P6 change, rebase P7 on that result, confirm
the Image source-subrect path remains independent from framebuffer movement,
run the combined P6/P7 regression suite and applicable native smokes, and pass
a new Merge Flow. Keep P7 unmerged until that gate is complete.
