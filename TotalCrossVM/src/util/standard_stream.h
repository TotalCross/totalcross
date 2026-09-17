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

bool standardStreamInit();
void standardStreamDestroy();
bool standardStreamWrite(int32 stream, const uint8 *bytes, int32 length);
bool standardStreamFlush(int32 stream, bool durable);
bool standardStreamClose(int32 stream);

#ifdef __cplusplus
}
#endif

#endif
