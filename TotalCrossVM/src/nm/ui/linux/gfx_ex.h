// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef GFX_EX_H
#define GFX_EX_H

#define SETPIXEL32(r,g,b) (((r) << 16) | ((g) << 8) | (b))           // 00RRGGBB
#define SETPIXEL565(r,g,b) ((((r) >> 3) << 11) | (((g) >> 2) << 5) | (((b) >> 3))) // bits RRRRRGGGGGGBBBBB

#if TC_WINDOWING_SDL
#if __APPLE__
#include "SDL.h"
#else
#include "SDL2/SDL.h"
#endif
#else
#include <directfb.h>
#endif

typedef struct TScreenSurfaceEx
{
#if TC_WINDOWING_SDL
   SDL_Window *window;
   SDL_Renderer *renderer;
   SDL_Texture *texture;
   SDL_Surface* surface;
#else
   IDirectFB *dfb;
   IDirectFBSurface *primary;
   IDirectFBDisplayLayer *layer;
   IDirectFBEventBuffer *events;
#endif
} *ScreenSurfaceEx, TScreenSurfaceEx;

#endif
