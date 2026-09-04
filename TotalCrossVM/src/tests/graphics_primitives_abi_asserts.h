// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef GRAPHICS_PRIMITIVES_ABI_ASSERTS_H
#define GRAPHICS_PRIMITIVES_ABI_ASSERTS_H

#include <stddef.h>

#ifdef __cplusplus
#define TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(condition, message) static_assert(condition, message)
#define TC_GRAPHICS_PRIMITIVES_ABI_ALIGNOF(type) alignof(type)
#else
#define TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(condition, message) _Static_assert(condition, message)
#define TC_GRAPHICS_PRIMITIVES_ABI_ALIGNOF(type) _Alignof(type)
#endif

TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(sizeof(TScreenConfiguration) == 48,
   "TScreenConfiguration size changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(TC_GRAPHICS_PRIMITIVES_ABI_ALIGNOF(TScreenConfiguration) == 8,
   "TScreenConfiguration alignment changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenConfiguration, width) == 0,
   "TScreenConfiguration.width offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenConfiguration, height) == 4,
   "TScreenConfiguration.height offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenConfiguration, hRes) == 8,
   "TScreenConfiguration.hRes offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenConfiguration, vRes) == 12,
   "TScreenConfiguration.vRes offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenConfiguration, contentScale) == 16,
   "TScreenConfiguration.contentScale offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenConfiguration, fontScale) == 24,
   "TScreenConfiguration.fontScale offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenConfiguration, deviceFontHeight) == 32,
   "TScreenConfiguration.deviceFontHeight offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenConfiguration, generation) == 36,
   "TScreenConfiguration.generation offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenConfiguration, surfaceReady) == 40,
   "TScreenConfiguration.surfaceReady offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenConfiguration, nativeSurfaceChanged) == 41,
   "TScreenConfiguration.nativeSurfaceChanged offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(sizeof(((TScreenConfiguration *)0)->surfaceReady) == 1,
   "TScreenConfiguration.surfaceReady size changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(sizeof(((TScreenConfiguration *)0)->nativeSurfaceChanged) == 1,
   "TScreenConfiguration.nativeSurfaceChanged size changed");

TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(sizeof(TScreenSurface) == 104,
   "TScreenSurface size changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(TC_GRAPHICS_PRIMITIVES_ABI_ALIGNOF(TScreenSurface) == 8,
   "TScreenSurface alignment changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenSurface, pixels) == 0,
   "TScreenSurface.pixels offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenSurface, mainWindowPixels) == 8,
   "TScreenSurface.mainWindowPixels offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenSurface, pitch) == 16,
   "TScreenSurface.pitch offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenSurface, bpp) == 20,
   "TScreenSurface.bpp offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenSurface, screenX) == 24,
   "TScreenSurface.screenX offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenSurface, screenY) == 28,
   "TScreenSurface.screenY offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenSurface, screenW) == 32,
   "TScreenSurface.screenW offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenSurface, screenH) == 36,
   "TScreenSurface.screenH offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenSurface, minScreenW) == 40,
   "TScreenSurface.minScreenW offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenSurface, minScreenH) == 44,
   "TScreenSurface.minScreenH offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenSurface, hRes) == 48,
   "TScreenSurface.hRes offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenSurface, vRes) == 52,
   "TScreenSurface.vRes offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenSurface, contentScale) == 56,
   "TScreenSurface.contentScale offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenSurface, fontScale) == 64,
   "TScreenSurface.fontScale offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenSurface, deviceFontHeight) == 72,
   "TScreenSurface.deviceFontHeight offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenSurface, surfaceGeneration) == 76,
   "TScreenSurface.surfaceGeneration offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenSurface, pendingChangeFlags) == 80,
   "TScreenSurface.pendingChangeFlags offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenSurface, surfaceReady) == 84,
   "TScreenSurface.surfaceReady offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(sizeof(((TScreenSurface *)0)->surfaceReady) == 1,
   "TScreenSurface.surfaceReady size changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenSurface, extension) == 88,
   "TScreenSurface.extension offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenSurface, shiftY) == 96,
   "TScreenSurface.shiftY offset changed");
TC_GRAPHICS_PRIMITIVES_ABI_ASSERT(offsetof(TScreenSurface, pixelformat) == 100,
   "TScreenSurface.pixelformat offset changed");

#undef TC_GRAPHICS_PRIMITIVES_ABI_ASSERT
#undef TC_GRAPHICS_PRIMITIVES_ABI_ALIGNOF

#endif
