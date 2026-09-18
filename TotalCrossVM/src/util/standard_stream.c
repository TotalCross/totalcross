// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcvm.h"
#include "legacy_debug_console.h"
#include "standard_stream.h"

#if defined(WINCE) || defined(WIN32)
#include "win/standard_stream_c.h"
#elif defined(ANDROID)
#include "android/standard_stream_c.h"
#elif defined(darwin)
#include "darwin/standard_stream_c.h"
#else
#include "posix/standard_stream_c.h"
#endif

#if defined(WINCE) || defined(WIN32)
static CRITICAL_SECTION standardStreamMutex;
#elif defined(POSIX) || defined(ANDROID)
static pthread_mutex_t standardStreamMutex;
#endif

static bool standardStreamInitialized;
static bool standardStreamClosed[2];

static bool validStandardStream(int32 stream)
{
   return stream == STANDARD_STREAM_OUT || stream == STANDARD_STREAM_ERR;
}

/* bool */ int32 standardStreamInit()
{
   int result;

   if (standardStreamInitialized)
      return true;
#if defined(WINCE) || defined(WIN32)
   InitializeCriticalSection(&standardStreamMutex);
#elif defined(POSIX) || defined(ANDROID)
   {
      pthread_mutexattr_t attributes;
      if (pthread_mutexattr_init(&attributes) != 0)
         return false;
#if !defined(PTHREAD_MUTEX_RECURSIVE)
#define PTHREAD_MUTEX_RECURSIVE PTHREAD_MUTEX_RECURSIVE_NP
#endif
      result = pthread_mutexattr_settype(&attributes, PTHREAD_MUTEX_RECURSIVE);
      if (result == 0)
         result = pthread_mutex_init(&standardStreamMutex, &attributes);
      pthread_mutexattr_destroy(&attributes);
      if (result != 0)
         return false;
   }
#else
#error "Standard stream synchronization is not implemented for this platform"
#endif
   if (!standardPlatformInit())
   {
#if defined(WINCE) || defined(WIN32)
      DeleteCriticalSection(&standardStreamMutex);
#else
      pthread_mutex_destroy(&standardStreamMutex);
#endif
      return false;
   }
   standardStreamClosed[STANDARD_STREAM_OUT] = false;
   standardStreamClosed[STANDARD_STREAM_ERR] = false;
   standardStreamInitialized = true;
   return true;
}

void standardStreamDestroy()
{
   if (!standardStreamInitialized)
      return;
#if defined(WINCE) || defined(WIN32)
   EnterCriticalSection(&standardStreamMutex);
#else
   pthread_mutex_lock(&standardStreamMutex);
#endif
   standardPlatformDestroy();
#if defined(WINCE) || defined(WIN32)
   LeaveCriticalSection(&standardStreamMutex);
   DeleteCriticalSection(&standardStreamMutex);
#else
   pthread_mutex_unlock(&standardStreamMutex);
   pthread_mutex_destroy(&standardStreamMutex);
#endif
   standardStreamInitialized = false;
}

/* bool */ int32 standardStreamWrite(int32 stream, const uint8 *bytes, int32 length)
{
   bool platformResult;
   bool legacyResult;
   if (!standardStreamInitialized || !validStandardStream(stream) || length < 0 || (length > 0 && !bytes))
      return false;
#if defined(WINCE) || defined(WIN32)
   EnterCriticalSection(&standardStreamMutex);
#else
   pthread_mutex_lock(&standardStreamMutex);
#endif
   platformResult = !standardStreamClosed[stream] && standardPlatformWrite(stream, bytes, length);
   legacyResult = !standardStreamClosed[stream] && legacyDebugConsoleWrite(bytes, length, false);
#if defined(WINCE) || defined(WIN32)
   LeaveCriticalSection(&standardStreamMutex);
#else
   pthread_mutex_unlock(&standardStreamMutex);
#endif
   return platformResult && legacyResult;
}

/* bool */ int32 standardStreamFlush(int32 stream, /* bool */ int32 durable)
{
   bool platformResult;
   bool legacyResult;
   if (!standardStreamInitialized || !validStandardStream(stream))
      return false;
#if defined(WINCE) || defined(WIN32)
   EnterCriticalSection(&standardStreamMutex);
#else
   pthread_mutex_lock(&standardStreamMutex);
#endif
   platformResult = !standardStreamClosed[stream] && standardPlatformFlush(stream, durable);
   legacyResult = !standardStreamClosed[stream] && legacyDebugConsoleFlush(durable);
#if defined(WINCE) || defined(WIN32)
   LeaveCriticalSection(&standardStreamMutex);
#else
   pthread_mutex_unlock(&standardStreamMutex);
#endif
   return platformResult && legacyResult;
}

/* bool */ int32 standardStreamClose(int32 stream)
{
   bool platformResult;
   bool legacyResult;
   if (!standardStreamInitialized || !validStandardStream(stream))
      return false;
#if defined(WINCE) || defined(WIN32)
   EnterCriticalSection(&standardStreamMutex);
#else
   pthread_mutex_lock(&standardStreamMutex);
#endif
   if (standardStreamClosed[stream])
      platformResult = legacyResult = true;
   else
   {
      platformResult = standardPlatformClose(stream);
      legacyResult = legacyDebugConsoleFlush(false);
      standardStreamClosed[stream] = true;
   }
#if defined(WINCE) || defined(WIN32)
   LeaveCriticalSection(&standardStreamMutex);
#else
   pthread_mutex_unlock(&standardStreamMutex);
#endif
   return platformResult && legacyResult;
}
