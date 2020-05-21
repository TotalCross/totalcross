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
    extern Uint32 *pixels;
    extern int pitch;
    int TCSDL_Init(ScreenSurface screen);
    void TCSDL_UpdateTexture(int w, int h, int pitch,void *pixels);
    void TCSDL_Present();
    int TCSDL_PixelFormat();

#ifdef __cplusplus
}
#endif

#endif
