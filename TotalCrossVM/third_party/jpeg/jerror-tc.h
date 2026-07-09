// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef JERROR_TC_H
#define JERROR_TC_H

#include "tcvm.h"
#include "jpeglib.h"

typedef struct
{
   struct jpeg_error_mgr pub;
   Heap heap;
} TCJpegErrorManager;

struct jpeg_error_mgr *tc_jpeg_std_error(TCJpegErrorManager *err, Heap heap);

#endif
