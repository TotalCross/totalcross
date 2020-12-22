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
    #if __APPLE__
    #include "SDL.h"
    #else
    #include "SDL2/SDL.h"
    #endif
    #include "GraphicsPrimitives.h"

#include <sys/time.h>

#define PROFILE_CPUTIME_SYSTIME 1

long getMicrotime(char* name);
void profileStop(char* name);

#define ReportMeasurment(t1, t2) {\
    static int callNum {};\
    static double cpuTime {};\
    callNum++;\
    double elps = (double)(t2-t1)/CLOCKS_PER_SEC;\
    cpuTime += elps;\
    printf("%-25s\t%-.20f\n",__FUNCTION__,cpuTime/callNum);\
}

#ifdef PROFILE_WALLTIME_CHRONO
    #define PROFILE_START getMicrotime((char*) __func__);
    #define PROFILE_STOP profileStop((char*) __func__);
#elif  PROFILE_CPUTIME_SYSTIME
    #define PROFILE_START auto start_profile {clock()};
    #define PROFILE_STOP {auto stop_profile {clock()};\
    ReportMeasurment(start_profile,stop_profile);}
#else
    #define PROFILE_START
    #define PROFILE_STOP
#endif

    bool TCSDL_Init(ScreenSurface screen, const char* title, bool fullScreen);
    void TCSDL_UpdateTexture(int w, int h, int pitch,void *pixels);
    void TCSDL_Present();
    void TCSDL_Destroy(ScreenSurface screen);
    void TCSDL_GetWindowSize(ScreenSurface screen, int32* width, int32* height);

#ifdef __cplusplus
}
#endif

#endif
