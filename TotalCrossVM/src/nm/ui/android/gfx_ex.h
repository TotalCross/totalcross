// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TC_ANDROID_GFX_EX_H
#define TC_ANDROID_GFX_EX_H

#include <android/native_window.h>
#include <EGL/egl.h>

#define ANDROID_BPP 32

#define SETPIXEL32(r,g,b) (((b) << 16) | ((g) << 8) | (r) | 0xFF000000)           // 00RRGGBB
#define SETPIXEL565(r,g,b) ((((r) >> 3) << 11) | (((g) >> 2) << 5) | (((b) >> 3))) // bits RRRRRGGGGGGBBBBB
#define SETPIXEL565_(dest,p) do {*dest++ = ((p>>16)&0xF800) | ((p>>13)&0x7E0) | ((p>>11)&0x1F );} while(0);

typedef struct TScreenSurfaceEx
{
   ANativeWindow *window;
   EGLDisplay display;
   EGLSurface surface;
   EGLContext context;
   int graphicsInitialized;
} TScreenSurfaceEx, *ScreenSurfaceEx;

#define NO_GRAPHICS_LOCK_NEEDED
#define graphicsLock(screenSurface, on) true

#endif
