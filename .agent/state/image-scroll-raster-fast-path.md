<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image Scroll Raster Fast Path — Active State

## Current slice

Milestone 2: add a non-materializing lookup for the existing final materialized raster and test scale/generation/cache validity before draw-plan construction.

## Active paths

- `TotalCrossSDK/src/main/java/totalcross/ui/gfx/Graphics.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/image/Image.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/image/ImagePipeline.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/image/ImageDrawingBridge.java` (keep exactly two public static methods)
- `TotalCrossSDK/src/main/java/totalcross/ui/image/ImageRasterFeatureBridge.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/image/ImageDrawingFeatureBridge.java` if a separate cache bridge is needed
- `TotalCrossSDK/src/test/java/tc/tools/ArtifactBoundariesTest.java`
- `TotalCrossSDK/src/test/java/totalcross/ui/gfx/GraphicsDeferredImageTest.java`
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageScrollRasterFastPathSmokeApp.java`
- `TotalCrossVM/src/nm/ui/gfx_Graphics.c`
- `TotalCrossVM/src/nm/ui/GraphicsPrimitivesSkia_c.h`
- `TotalCrossVM/src/nm/ui/skia/skia_image_geometry.cpp`

## Next action

Implement the cache probe through a separate narrow bridge if it cannot fit the unchanged `ImageDrawingBridge` surface. Confirm exact scale, source decode generation, current source/pipeline state, and cached Image validity; verify a miss reaches native plan copy without creating a materialized final raster.

## Focused validation completed

- Milestone 1 committed plan-aware Image-source copying with explicit source and destination rectangles. Focused SDK, distribution, macOS native build, and native smoke passed; see `.agent/evidence/image-scroll-raster-fast-path.md`.
- The plan-copy smoke records attempt/handled/fallback/status counters through the internal raster bridge.

## Deferred validation

- Cached-final hit/invalidation tests, artifact-boundary checks for any new bridge, diagnostics-on validation, warm measurement, optional corpus workload, P2/P3 regression smokes, and final PR checks are deferred to their milestones.

## Resume command

From the repository root, read this state file first, then the active sections in `.agent/plans/image-scroll-raster-fast-path.md`.
