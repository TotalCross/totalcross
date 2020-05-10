// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifndef GFX_GRAPHICS_H
#define GFX_GRAPHICS_H

#include "GraphicsPrimitives.h"
#include "settings.h"

#ifdef __cplusplus
extern "C" {
#endif

void privateFullscreen             (bool on);
void privateScreenChange           (int32 w, int32 h);
bool graphicsStartup               (ScreenSurface screen, int16 appTczAttr);
bool graphicsCreateScreenSurface   (ScreenSurface screen);
void graphicsUpdateScreen          (Context currentContext, ScreenSurface screen);
void graphicsDestroy               (ScreenSurface screen, bool isScreenChange);
  
#ifdef __cplusplus
};
#endif

#endif
