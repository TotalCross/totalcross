// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifdef HEADLESS
#ifndef __GPIOD_LIB__
#define __GPIOD_LIB__

#include "xtypes.h"

typedef void * (*_gpiod_chip_open_by_numberProc) (unsigned int num);
extern _gpiod_chip_open_by_numberProc _gpiod_chip_open_by_number;

typedef void * (*_gpiod_chip_get_lineProc) (void * chip, unsigned int offset);
extern _gpiod_chip_get_lineProc _gpiod_chip_get_line;

typedef int (*_gpiod_line_request_outputProc) (void * line, const char *consumer, int default_val);
extern _gpiod_line_request_outputProc _gpiod_line_request_output;

typedef int (*_gpiod_line_set_valueProc) (void * line, int value);
extern _gpiod_line_set_valueProc _gpiod_line_set_value;

typedef int (*_gpiod_line_request_inputProc) (void * line, const char *consumer);
extern _gpiod_line_request_inputProc _gpiod_line_request_input;

typedef int (*_gpiod_line_get_valueProc) (void * line);
extern _gpiod_line_get_valueProc _gpiod_line_get_value;

bool initGpiod();
void closeGpiod();


#endif
#endif // ifdef HEADLESS