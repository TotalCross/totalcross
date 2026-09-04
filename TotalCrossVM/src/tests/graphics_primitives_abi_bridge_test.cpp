// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "../tcvm/tc_platform.h"
#include "../nm/ui/GraphicsPrimitives.h"
#include "graphics_primitives_abi_asserts.h"

#include <cstdio>
#include <cstring>

extern "C" {
int32 graphicsPrimitivesCReadConfiguration(const TScreenConfiguration *configuration,
   uint8 expectedSurfaceReady, uint8 expectedNativeSurfaceChanged);
void graphicsPrimitivesCWriteConfiguration(TScreenConfiguration *configuration,
   uint8 surfaceReady, uint8 nativeSurfaceChanged);
int32 graphicsPrimitivesCReadSurface(const TScreenSurface *surface, uint8 expectedSurfaceReady);
void graphicsPrimitivesCWriteSurface(TScreenSurface *surface, uint8 surfaceReady);
}

static int checkConfigurationBridge(uint8 surfaceReady, uint8 nativeSurfaceChanged)
{
   TScreenConfiguration configuration;
   std::memset(&configuration, 0, sizeof(configuration));
   configuration.surfaceReady = surfaceReady;
   configuration.nativeSurfaceChanged = nativeSurfaceChanged;
   if (!graphicsPrimitivesCReadConfiguration(&configuration, surfaceReady, nativeSurfaceChanged)) {
      return 0;
   }

   std::memset(&configuration, 0, sizeof(configuration));
   graphicsPrimitivesCWriteConfiguration(&configuration, surfaceReady, nativeSurfaceChanged);
   return configuration.surfaceReady == surfaceReady &&
      configuration.nativeSurfaceChanged == nativeSurfaceChanged;
}

static int checkSurfaceBridge(uint8 surfaceReady)
{
   TScreenSurface surface;
   std::memset(&surface, 0, sizeof(surface));
   surface.surfaceReady = surfaceReady;
   if (!graphicsPrimitivesCReadSurface(&surface, surfaceReady)) {
      return 0;
   }

   std::memset(&surface, 0, sizeof(surface));
   graphicsPrimitivesCWriteSurface(&surface, surfaceReady);
   return surface.surfaceReady == surfaceReady;
}

int main()
{
   for (uint8 surfaceReady = 0; surfaceReady <= 1; ++surfaceReady) {
      for (uint8 nativeSurfaceChanged = 0; nativeSurfaceChanged <= 1; ++nativeSurfaceChanged) {
         if (!checkConfigurationBridge(surfaceReady, nativeSurfaceChanged)) {
            std::fprintf(stderr, "configuration bridge failed for %u,%u\n",
               surfaceReady, nativeSurfaceChanged);
            return 1;
         }
      }
      if (!checkSurfaceBridge(surfaceReady)) {
         std::fprintf(stderr, "surface bridge failed for %u\n", surfaceReady);
         return 1;
      }
   }

   std::puts("GraphicsPrimitives C/C++ bridge checks passed for all boolean combinations");
   return 0;
}
