// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TCSDL_H
#define TCSDL_H

#ifdef __cplusplus
extern "C" {
#endif

    #include "tcvm.h"
    #if __APPLE__
    #include "SDL.h"
    #else
    #include "SDL2/SDL.h"
    #endif
    #include "GraphicsPrimitives.h"

    bool TCSDL_Init(ScreenSurface screen, const char* title, bool fullScreen);
    bool TCSDL_SetPixelFormatRequest(const char* value);
    bool TCSDL_SetFullscreen(bool fullscreen);
    bool TCSDL_QueryWindowMetrics(ScreenSurface screen, TScreenConfiguration* configuration);
    bool TCSDL_CreateBackBuffer(ScreenSurface screen);
    void TCSDL_DestroyBackBuffer(ScreenSurface screen);
    void TCSDL_DestroyWindow(ScreenSurface screen);
    void TCSDL_UpdateTexture(int w, int h, int pitch,void *pixels);
    void TCSDL_Present();
    void TCSDL_GetWindowSize(ScreenSurface screen, int32* width, int32* height);

#ifdef __cplusplus
}
#endif

#endif
