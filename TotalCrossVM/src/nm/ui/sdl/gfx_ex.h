// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef GFX_EX_H
#define GFX_EX_H

#define SETPIXEL32(r,g,b) (((r) << 16) | ((g) << 8) | (b))
#define SETPIXEL565(r,g,b) ((((r) >> 3) << 11) | (((g) >> 2) << 5) | ((b) >> 3))

#if __APPLE__
#include "SDL.h"
#else
#include "SDL2/SDL.h"
#endif

typedef struct TScreenSurfaceEx
{
   SDL_Window *window;
   SDL_Renderer *renderer;
   SDL_Texture *texture;
   SDL_Surface *surface;
} *ScreenSurfaceEx, TScreenSurfaceEx;

#endif
