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

#define ANDROID_BPP 32 

#define SETPIXEL32(r,g,b) (((b) << 16) | ((g) << 8) | (r) | 0xFF000000)           // 00RRGGBB
#define SETPIXEL565(r,g,b) ((((r) >> 3) << 11) | (((g) >> 2) << 5) | (((b) >> 3))) // bits RRRRRGGGGGGBBBBB
#define SETPIXEL565_(dest,p) do {*dest++ = ((p>>16)&0xF800) | ((p>>13)&0x7E0) | ((p>>11)&0x1F );} while(0);

typedef struct
{
   
} TScreenSurfaceEx, *ScreenSurfaceEx;

#define NO_GRAPHICS_LOCK_NEEDED
#define graphicsLock(screenSurface, on) true


#endif
