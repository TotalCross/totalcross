// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "../tcvm/tc_platform.h"
#include "../nm/ui/GraphicsPrimitives.h"
#include "graphics_primitives_abi_asserts.h"

int32 graphicsPrimitivesCReadConfiguration(const TScreenConfiguration *configuration,
   uint8 expectedSurfaceReady, uint8 expectedNativeSurfaceChanged)
{
   return configuration->surfaceReady == expectedSurfaceReady &&
      configuration->nativeSurfaceChanged == expectedNativeSurfaceChanged;
}

void graphicsPrimitivesCWriteConfiguration(TScreenConfiguration *configuration,
   uint8 surfaceReady, uint8 nativeSurfaceChanged)
{
   configuration->surfaceReady = surfaceReady;
   configuration->nativeSurfaceChanged = nativeSurfaceChanged;
}

int32 graphicsPrimitivesCReadSurface(const TScreenSurface *surface, uint8 expectedSurfaceReady)
{
   return surface->surfaceReady == expectedSurfaceReady;
}

void graphicsPrimitivesCWriteSurface(TScreenSurface *surface, uint8 surfaceReady)
{
   surface->surfaceReady = surfaceReady;
}
