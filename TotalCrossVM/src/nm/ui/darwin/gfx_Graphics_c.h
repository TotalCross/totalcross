/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



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
