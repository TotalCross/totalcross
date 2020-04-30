// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#define SETPIXEL32(r,g,b) (((r) << 16) | ((g) << 8) | (b))           // 00RRGGBB
#define SETPIXEL565(r,g,b) ((((r) >> 3) << 11) | (((g) >> 2) << 5) | (((b) >> 3))) // bits RRRRRGGGGGGBBBBB
#define SETPIXEL565_(dest,p) do {uint32 temp = p & 0xF800F800; *dest++ = ( (temp>>16) | ((p>>13)&0x7E0) | (temp>>11) );} while(0);

typedef struct
{
   HBITMAP hbmp;
   HDC dc;
} TScreenSurfaceEx, *ScreenSurfaceEx;

#define NO_GRAPHICS_LOCK_NEEDED
#define graphicsLock(screenSurface, on) true
