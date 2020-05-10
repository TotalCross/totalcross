// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TCSDL_H
#define TCSDL_H
#ifdef __cplusplus
extern "C" {
#endif
#include "SDL2/SDL.h"
#include "GraphicsPrimitives.h"

extern SDL_Surface *sdlsurface;
extern SDL_Window *window;
extern SDL_Surface* surface2;
int initSDL(ScreenSurface screen);
void updateSDLScreen(int w, int h, void *pixels);
int ColorFormatSDL2Skia (int pixelFormat);
bool sdlPresent();
#ifdef __cplusplus
}
#endif

#endif
