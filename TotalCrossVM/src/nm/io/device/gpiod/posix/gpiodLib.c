// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"
#include "gpiodLib.h"
#include <dlfcn.h>
#include <glob.h>

#if !defined APPLE && !defined ANDROID && defined linux && defined __arm__ && !defined __aarch64__
// Avoid dependency on glibc 2.27
__asm__(".symver glob,glob@GLIBC_2.4");
#endif

static void* gpiodLib = NULL;

_gpiod_chip_open_by_numberProc _gpiod_chip_open_by_number;
_gpiod_chip_get_lineProc _gpiod_chip_get_line;
_gpiod_line_request_outputProc _gpiod_line_request_output;
_gpiod_line_set_valueProc _gpiod_line_set_value;
_gpiod_line_request_inputProc _gpiod_line_request_input;
_gpiod_line_get_valueProc _gpiod_line_get_value;

bool initGpiod() 
{
#ifdef HEADLESS
    gpiodLib = dlopen("libgpiod.so", RTLD_LAZY);
    
    if (gpiodLib == null) {
        glob_t globbuf;
        int i;

        if (glob("/usr/lib/libgpiod.so*", GLOB_DOOFFS, NULL, &globbuf) == 0) {
            for (i = globbuf.gl_pathc - 1 ; i >= 0 ; i--) {
                gpiodLib = dlopen(globbuf.gl_pathv[i], RTLD_LAZY);
                if (gpiodLib != null) {
                    break;
                }
            }
            globfree(&globbuf);
        }
    }

    if (gpiodLib != null) {
        _gpiod_chip_open_by_number = (_gpiod_chip_open_by_numberProc) dlsym(gpiodLib, TEXT("gpiod_chip_open_by_number"));
        _gpiod_chip_get_line = (_gpiod_chip_get_lineProc) dlsym(gpiodLib, TEXT("gpiod_chip_get_line"));
        _gpiod_line_request_output = (_gpiod_line_request_outputProc) dlsym(gpiodLib, TEXT("gpiod_line_request_output"));
        _gpiod_line_set_value = (_gpiod_line_set_valueProc) dlsym(gpiodLib, TEXT("gpiod_line_set_value"));
        _gpiod_line_request_input = (_gpiod_line_request_inputProc) dlsym(gpiodLib, TEXT("gpiod_line_request_input"));
        _gpiod_line_get_value = (_gpiod_line_get_valueProc) dlsym(gpiodLib, TEXT("gpiod_line_get_value"));
    }
#endif
    return true;
}

void closeGpiod()
{
#ifdef HEADLESS
    if (gpiodLib != null)
    {
        dlclose(gpiodLib);
        gpiodLib = null;
    }
#endif
}
