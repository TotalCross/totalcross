// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef STANDARD_STREAM_POSIX_C_H
#define STANDARD_STREAM_POSIX_C_H

#include <errno.h>
#include <unistd.h>

static bool standardPlatformInit()
{
   return true;
}

static bool standardPlatformWrite(int32 stream, const uint8 *bytes, int32 length)
{
   int fd = stream == STANDARD_STREAM_OUT ? STDOUT_FILENO : STDERR_FILENO;
   while (length > 0)
   {
      ssize_t written = write(fd, bytes, (size_t)length);
      if (written > 0)
      {
         bytes += written;
         length -= (int32)written;
      }
      else if (written < 0 && errno == EINTR)
         continue;
      else
         return false;
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
