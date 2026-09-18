// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef STANDARD_STREAM_H
#define STANDARD_STREAM_H

#include "xtypes.h"

#ifdef __cplusplus
extern "C" {
#endif

enum {
   STANDARD_STREAM_OUT = 0,
   STANDARD_STREAM_ERR = 1
};

/* bool */ int32 standardStreamInit();
void standardStreamDestroy();
/* bool */ int32 standardStreamWrite(int32 stream, const uint8 *bytes, int32 length);
/* bool */ int32 standardStreamFlush(int32 stream, /* bool */ int32 durable);
/* bool */ int32 standardStreamClose(int32 stream);

#ifdef __cplusplus
}
#endif

#endif
