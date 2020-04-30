// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only


#include "gpiodLib.h"

void * gpiod_chip_open_by_number(unsigned int num) {
    if (_gpiod_chip_open_by_number!= NULL) {
        return _gpiod_chip_open_by_number(num);
    }
    return NULL;
}

void * gpiod_chip_get_line(void * chip, unsigned int offset) {
    if (_gpiod_chip_get_line != NULL) {
        return _gpiod_chip_get_line(chip, offset);
    }
    return NULL;
}

int32 gpiod_line_request_output(void * line, const char *consumer, int default_val) {
    if (_gpiod_line_request_output != NULL) {
        return _gpiod_line_request_output(line, consumer, default_val);
    }
    return -1;
}


int32 gpiod_line_set_value(void * line, int value) {
    if (_gpiod_line_set_value != NULL) {
        return _gpiod_line_set_value(line, value);
    }
    return -1;
}

int32 gpiod_line_request_input(void * line, const char *consumer) {
    if (_gpiod_line_request_input != NULL) {
        return _gpiod_line_request_input(line, consumer);
    }
    return -1;
}

int32 gpiod_line_get_value(void * line) {
    if (_gpiod_line_get_value != NULL) {
        return _gpiod_line_get_value(line);
    }
    return -1;
}