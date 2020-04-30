// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifndef GFX_EX_H
#define GFX_EX_H

#define SETPIXEL32(r,g,b) (((r) << 16) | ((g) << 8) | (b))           // 00RRGGBB
#define SETPIXEL565(r,g,b) ((((r) >> 3) << 11) | (((g) >> 2) << 5) | (((b) >> 3))) // bits RRRRRGGGGGGBBBBB

#ifndef HEADLESS
#include <directfb.h>
#endif

typedef struct
{
#ifndef HEADLESS
   IDirectFB *dfb;
   IDirectFBSurface *primary;
   IDirectFBDisplayLayer *layer;
   IDirectFBEventBuffer *events;
#endif
} TScreenSurfaceEx, *ScreenSurfaceEx;

#endif
