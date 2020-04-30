// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifndef GRAPHICSPRIMITIVES_H
#define GRAPHICSPRIMITIVES_H

#include "tcclass.h"

#if defined ANDROID || defined darwin || defined HEADLESS
#include "android/skia.h"
#endif

#if defined(WINCE) || defined(WIN32)
 #include "win/gfx_ex.h"
#elif defined(darwin)
 #include "darwin/gfx_ex.h"
#elif defined(ANDROID)
 #include "android/gfx_ex.h"
#elif defined(linux)
 #ifdef TOTALCROSS
  #include "linux/gfx_ex.h"
 #endif
#endif
#include "xtypes.h"
#ifdef __cplusplus
extern "C"
{
#endif
typedef uint32 Pixel32; // 32 bpp
#ifndef SKIA_H
typedef uint16 Pixel565; // 16 bpp
typedef uint8  PixelPal; // 8 bpp - palettized
#endif
typedef Pixel32 Pixel;

typedef union
{
   struct
   {
      uint8 a,b,g,r;
   };
   Pixel pixel;
} PixelConv;
typedef struct {uint8 b,g,r;} Pixel24;

#define SETPIXEL24(t,f) {t->b = f->b; t->r = f->r; t->g = f->g;}

typedef enum
{
   SURF_IMAGE,
   SURF_CONTROL
} SurfaceType;

typedef struct TScreenSurface // represents a device-dependant surface, there's only ONE per application
{
   uint8* pixels; // pixels in native format
   __unsafe_unretained TCObject mainWindowPixels; // pixels in 888 format, read directly from totalcross.ui.gfx.Graphics class
   int32 pitch; // screen memory pitch size in bytes
   uint32 bpp;
   int32 screenX, screenY, screenW, screenH,minScreenW,minScreenH;
   int32 hRes, vRes;
   void *extension; // platform specific data
   int32 shiftY;
} *ScreenSurface, TScreenSurface;


Pixel makePixelA(int32 a, int32 r, int32 g, int32 b);
Pixel makePixel(int32 r, int32 g, int32 b);
Pixel makePixelARGB(int32 rgb);
Pixel makePixelRGB(int32 rgb);
PixelConv makePixelConvRGB(int32 rgb);


/**
 * The device context points a structure containing platform specific data
 * that have to handled in platform specific code only, that's why we don't
 * define a structure here insofar some platform specific data can't be
 * defined in plain C (such as SymbianOS C++ classes, iPhone objC data structures, ...)
 * Currently this pointer is mirrored in ScreenSurface's extension field but this
 * may change sooner or later.
 */
extern void *deviceCtx;

#define SCREEN_EX(x)        ((ScreenSurfaceEx)((x)->extension))
#define DEVICE_CTX          ((ScreenSurfaceEx)deviceCtx)

#ifdef __cplusplus
}
#endif

#endif
