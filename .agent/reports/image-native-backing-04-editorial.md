<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image native backing Plan 4 handoff

Plan 4 is complete through `59cda45a6`.

## Delivered architecture

Deployed Skia images execute fade, alpha, touch-up, APPLY_COLOR, APPLY_COLOR2,
CHANGE_COLORS, and SET_TRANSPARENT_COLOR without using `Image.pixels`. Exact
operations read unpremultiplied RGBA bytes in native temporary storage and
replace the immutable Skia backing. Result-producing operations install a new
native backing; in-place operations replace the existing backing.

APPLY_COLOR2 analyzes the complete native frame strip in pipeline order, using
the existing brightness weighting, strict tie behavior, zero-channel handling,
and optional `0xAA` alpha control. Exact color-key operations preserve ARGB and
legacy `-1` semantics.

Mixed pipelines group consecutive geometry nodes into one native geometry plan.
Exact color/source-analysis operations act as native barriers, and no deployed
path falls back to a Java full-image raster.

## Proof and commits

Implementation checkpoints are `7b70f068c`, `c16f68a3b`, `dcd4e026d`,
`a1635eb26`, `7f03f2915`, `706c3d1c0`, and `59cda45a6`. Focused differential
tests cover boundary channels, transparent pixels, multi-frame behavior, ARGB
key matching, color-key behavior, and operation ordering.

The final Plan 4 gate passed on macOS arm64: native `tcvm`/Launcher build,
focused `ImageDeferredColorMutationTest`, SDK `dist -x test`, and the native
color-filter smoke. The smoke passed exact mutations, mixed pipeline ordering,
direct draw, and PNG save/readback. Full logs and fixture output are indexed in
`.agent/evidence/image-native-backing-04.jsonl`.

Android, iOS, Linux, Windows, and the full platform matrix were not run under
the repository's explicit roadmap build budget. Pixel-array retirement remains
Plan 5 work.
