// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef STANDARD_STREAM_DARWIN_C_H
#define STANDARD_STREAM_DARWIN_C_H

#include <CoreFoundation/CoreFoundation.h>
#include <os/log.h>
#include <string.h>

#define STANDARD_STREAM_LINE_CAPACITY 4096

static char standardDarwinSubsystem[128] = "org.totalcross.runtime";
static char standardDarwinLines[2][STANDARD_STREAM_LINE_CAPACITY];
static int32 standardDarwinLineLengths[2];
static os_log_t standardDarwinLogs[2];

static void standardDarwinEmit(int32 stream)
{
   os_log_type_t type = stream == STANDARD_STREAM_OUT ? OS_LOG_TYPE_INFO : OS_LOG_TYPE_ERROR;
   standardDarwinLines[stream][standardDarwinLineLengths[stream]] = 0;
   if (standardDarwinLogs[stream])
      os_log_with_type(standardDarwinLogs[stream], type, "%{public}s", standardDarwinLines[stream]);
   standardDarwinLineLengths[stream] = 0;
}

static bool standardPlatformInit()
{
   CFBundleRef bundle = CFBundleGetMainBundle();
   CFStringRef identifier = bundle ? CFBundleGetIdentifier(bundle) : NULL;
   if (identifier)
      CFStringGetCString(identifier, standardDarwinSubsystem, sizeof(standardDarwinSubsystem), kCFStringEncodingUTF8);
   standardDarwinLogs[STANDARD_STREAM_OUT] = os_log_create(standardDarwinSubsystem, "System.out");
   standardDarwinLogs[STANDARD_STREAM_ERR] = os_log_create(standardDarwinSubsystem, "System.err");
   standardDarwinLineLengths[STANDARD_STREAM_OUT] = 0;
   standardDarwinLineLengths[STANDARD_STREAM_ERR] = 0;
   return true;
}

static bool standardPlatformWrite(int32 stream, const uint8 *bytes, int32 length)
{
   while (length-- > 0)
   {
      uint8 byte = *bytes++;
      if (byte == '\n')
         standardDarwinEmit(stream);
      else
      {
         if (standardDarwinLineLengths[stream] == STANDARD_STREAM_LINE_CAPACITY - 1)
            standardDarwinEmit(stream);
         standardDarwinLines[stream][standardDarwinLineLengths[stream]++] = (char)byte;
      }
   }
   return standardDarwinLogs[stream] != NULL;
}

static bool standardPlatformFlush(int32 stream, bool durable)
{
   UNUSED(durable);
   if (standardDarwinLineLengths[stream] > 0)
      standardDarwinEmit(stream);
   return standardDarwinLogs[stream] != NULL;
}

static bool standardPlatformClose(int32 stream)
{
   return standardPlatformFlush(stream, false);
}

static void standardPlatformDestroy()
{
   standardPlatformFlush(STANDARD_STREAM_OUT, false);
   standardPlatformFlush(STANDARD_STREAM_ERR, false);
}

#endif
