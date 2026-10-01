<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image Scroll Raster Fast Path — Active State

## Current slice

Milestone 3: factor P3's exact physical identity proof and use it for a conservative direct software-raster copy path.

## Active paths

- `TotalCrossVM/src/nm/ui/skia/skia_image_geometry.cpp`
- `TotalCrossVM/src/nm/ui/skia/skia_image_geometry.h`
- `TotalCrossVM/src/nm/ui/gfx_Graphics.c`
- `TotalCrossVM/src/nm/ui/GraphicsPrimitivesSkia_c.h`
- `TotalCrossSDK/src/main/java/totalcross/ui/gfx/Graphics.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/image/ImageDrawPlan.java`
- relevant image geometry and graphics native tests under `TotalCrossVM/src/nm/ui/skia`

## Next action

Inspect P3's current physical-identity predicate and the destination surface/write APIs. Factor a shared exact mapping result without widening P3 eligibility, then use it only when source and destination representations prove a direct copy is safe.

## Focused validation completed

- Milestone 1 committed plan-aware Image-source copying with explicit source and destination rectangles. Focused SDK, distribution, macOS native build, and native smoke passed; see `.agent/evidence/image-scroll-raster-fast-path.md`.
- Milestone 2 committed an exact-key non-materializing cache probe. Focused image/artifact tests, SDK distribution, header validation, and native smoke passed. The smoke observed five probe misses and three handled plan copies while deferred sources remained unmaterialized.

## Deferred validation

- P3 shared-proof/direct-copy implementation and tests, diagnostics-on validation, warm measurement, optional corpus workload, complete artifact/integration validation, and final PR checks remain deferred.

## Resume command

From the repository root, read this state file first, then the active sections in `.agent/plans/image-scroll-raster-fast-path.md`.
