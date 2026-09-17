// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef STANDARD_STREAM_WIN_C_H
#define STANDARD_STREAM_WIN_C_H

static bool standardPlatformInit()
{
   return true;
}

static bool standardPlatformWrite(int32 stream, const uint8 *bytes, int32 length)
{
   HANDLE handle = GetStdHandle(stream == STANDARD_STREAM_OUT ? STD_OUTPUT_HANDLE : STD_ERROR_HANDLE);
   if (handle == NULL || handle == INVALID_HANDLE_VALUE)
      return true;

   while (length > 0)
   {
      DWORD chunk = (DWORD)min32(length, 0x7fffffff);
      DWORD written = 0;
      if (!WriteFile(handle, bytes, chunk, &written, NULL) || written == 0)
         return false;
      bytes += written;
      length -= (int32)written;
   }
   return true;
}

static bool standardPlatformFlush(int32 stream, bool durable)
{
   UNUSED(stream);
   UNUSED(durable);
   return true;
}

static bool standardPlatformClose(int32 stream)
{
   UNUSED(stream);
   return true;
}

static void standardPlatformDestroy()
{
}

#endif
