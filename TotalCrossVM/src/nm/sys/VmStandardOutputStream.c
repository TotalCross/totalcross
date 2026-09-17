// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcvm.h"
#include "NativeMethods.h"
#include "../../util/standard_stream.h"

static bool validStandardStreamChannel(int32 stream)
{
   return stream == STANDARD_STREAM_OUT || stream == STANDARD_STREAM_ERR;
}

TC_API void tsVSOS_standardWrite_iBii(NMParams p)
{
   int32 stream = p->i32[0];
   TCObject bytes = p->obj[0];
   int32 offset = p->i32[1];
   int32 length = p->i32[2];

   if (!validStandardStreamChannel(stream))
      throwException(p->currentContext, IllegalArgumentException, "Unknown standard stream.");
   else if (!bytes)
      throwNullArgumentException(p->currentContext, "bytes");
   else if (checkArrayRange(p->currentContext, bytes, offset, length))
      p->retI = standardStreamWrite(stream, (uint8 *)ARRAYOBJ_START(bytes) + offset, length);
}

TC_API void tsVSOS_standardFlush_ib(NMParams p)
{
   int32 stream = p->i32[0];
   if (!validStandardStreamChannel(stream))
      throwException(p->currentContext, IllegalArgumentException, "Unknown standard stream.");
   else
      p->retI = standardStreamFlush(stream, p->i32[1] != 0);
}

TC_API void tsVSOS_standardClose_i(NMParams p)
{
   int32 stream = p->i32[0];
   if (!validStandardStreamChannel(stream))
      throwException(p->currentContext, IllegalArgumentException, "Unknown standard stream.");
   else
      p->retI = standardStreamClose(stream);
}
