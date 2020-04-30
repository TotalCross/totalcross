// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"
#include "gpiodLib.h"
#include <dlfcn.h>
#include <glob.h>

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

           glob("/usr/lib/libgpiod.so*", GLOB_DOOFFS, NULL, &globbuf);
           for (i = globbuf.gl_pathc - 1 ; i >= 0 ; i--) {
            gpiodLib = dlopen(globbuf.gl_pathv[i], RTLD_LAZY);
            if (gpiodLib != null) {
                break;
            }
         }
         globfree(&globbuf);
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
