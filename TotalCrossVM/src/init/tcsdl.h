// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TCSDL_H
#define TCSDL_H

#ifdef __cplusplus
extern "C" {
#endif

    #include "tcvm.h"
    #include "SDL2/SDL.h"
    #include "GraphicsPrimitives.h"

    extern SDL_Window *window;
    extern SDL_Surface *surfaceSDL;

    int initSDL(ScreenSurface screen);
    void updateScreenSDL(int w, int h, int pitch,void *pixels);
    void presentSDL();
    int pixelFormatSDL(int pixelFormat);

#ifdef __cplusplus
}
#endif

#endif
