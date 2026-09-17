// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcvm.h"
#include "legacy_debug_console.h"

#include <stdio.h>
#include <string.h>

#if defined(WINCE) || defined(WIN32)
static CRITICAL_SECTION legacyDebugConsoleMutex;
#else
#include <unistd.h>
static pthread_mutex_t legacyDebugConsoleMutex;
#endif

static FILE *legacyDebugFile;
static char legacyDebugPath[MAX_PATHNAME];
static bool legacyDebugConsoleInitialized;

static void lockLegacyDebugConsole()
{
#if defined(WINCE) || defined(WIN32)
   EnterCriticalSection(&legacyDebugConsoleMutex);
#else
   pthread_mutex_lock(&legacyDebugConsoleMutex);
#endif
}

static void unlockLegacyDebugConsole()
{
#if defined(WINCE) || defined(WIN32)
   LeaveCriticalSection(&legacyDebugConsoleMutex);
#else
   pthread_mutex_unlock(&legacyDebugConsoleMutex);
#endif
}

static void closeLegacyDebugFileLocked()
{
   if (legacyDebugFile)
      fclose(legacyDebugFile);
   legacyDebugFile = null;
}

static bool openLegacyDebugFileLocked()
{
   if (!legacyDebugFile)
   {
#if defined(WINCE) || defined(WIN32)
      xstrprintf(legacyDebugPath, "%s\\DebugConsole.txt", appPath);
#else
      xstrprintf(legacyDebugPath, "%s/DebugConsole.txt", appPath);
#endif
      legacyDebugFile = fopen(legacyDebugPath, "ab+");
   }
   return legacyDebugFile != null;
}

static bool flushLegacyDebugFileLocked(bool durable)
{
   if (!legacyDebugFile || fflush(legacyDebugFile) != 0)
      return false;
#if !defined(WINCE) && !defined(WIN32)
   if (durable && fsync(fileno(legacyDebugFile)) != 0)
      return false;
#else
   UNUSED(durable);
#endif
   return true;
}

static bool deleteLegacyDebugFileLocked()
{
#if defined(WINCE) || defined(WIN32)
   TCHAR path[MAX_PATHNAME];
   CharP2TCHARPBuf(legacyDebugPath, path);
   return DeleteFile(path) != 0;
#else
   return remove(legacyDebugPath) == 0;
#endif
}

bool legacyDebugConsoleInit()
{
   int result = 0;

   if (legacyDebugConsoleInitialized)
      return true;
#if defined(WINCE) || defined(WIN32)
   InitializeCriticalSection(&legacyDebugConsoleMutex);
#else
   {
      pthread_mutexattr_t attributes;
      if (pthread_mutexattr_init(&attributes) != 0)
         return false;
#if !defined(PTHREAD_MUTEX_RECURSIVE)
#define PTHREAD_MUTEX_RECURSIVE PTHREAD_MUTEX_RECURSIVE_NP
#endif
      result = pthread_mutexattr_settype(&attributes, PTHREAD_MUTEX_RECURSIVE);
      if (result == 0)
         result = pthread_mutex_init(&legacyDebugConsoleMutex, &attributes);
      pthread_mutexattr_destroy(&attributes);
      if (result != 0)
         return false;
   }
#endif
   legacyDebugConsoleInitialized = true;
   return true;
}

void legacyDebugConsoleClose()
{
   if (!legacyDebugConsoleInitialized)
      return;
   lockLegacyDebugConsole();
   closeLegacyDebugFileLocked();
   unlockLegacyDebugConsole();
}

bool legacyDebugConsoleIsOpen()
{
   bool open;
   if (!legacyDebugConsoleInitialized)
      return false;
   lockLegacyDebugConsole();
   open = legacyDebugFile != null;
   unlockLegacyDebugConsole();
   return open;
}

bool legacyDebugConsoleWrite(const uint8 *bytes, int32 length, bool durable)
{
   bool result;
   if (length < 0 || (length > 0 && !bytes))
      return false;
   if (length == 0)
      return true;
   if (!legacyDebugConsoleInitialized)
      return false;
   lockLegacyDebugConsole();
   result = openLegacyDebugFileLocked()
      && fwrite(bytes, 1, (size_t)length, legacyDebugFile) == (size_t)length;
   if (result && durable)
      result = flushLegacyDebugFileLocked(true);
   unlockLegacyDebugConsole();
   return result;
}

bool legacyDebugConsoleFlush(bool durable)
{
   bool result;
   if (!legacyDebugConsoleInitialized)
      return false;
   lockLegacyDebugConsole();
   result = legacyDebugFile == null || flushLegacyDebugFileLocked(durable);
   unlockLegacyDebugConsole();
   return result;
}

bool legacyDebugConsoleDebugLine(const char *line, const char *lineEnding, bool durable)
{
   bool result;
   if (!line || !lineEnding || !legacyDebugConsoleInitialized)
      return false;
   lockLegacyDebugConsole();
   result = openLegacyDebugFileLocked()
      && fputs(line, legacyDebugFile) >= 0
      && fputs(lineEnding, legacyDebugFile) >= 0;
   if (result && durable)
      result = flushLegacyDebugFileLocked(true);
   unlockLegacyDebugConsole();
   return result;
}

bool legacyDebugConsoleErase()
{
   if (!legacyDebugConsoleInitialized)
      return false;
   lockLegacyDebugConsole();
   if (openLegacyDebugFileLocked())
   {
      closeLegacyDebugFileLocked();
      (void)deleteLegacyDebugFileLocked();
   }
   unlockLegacyDebugConsole();
   return true;
}

void legacyDebugConsoleDestroy(const char *separator)
{
   if (!legacyDebugConsoleInitialized)
      return;
   lockLegacyDebugConsole();
   if (legacyDebugFile)
   {
      if (separator)
         fputs(separator, legacyDebugFile);
      closeLegacyDebugFileLocked();
   }
   unlockLegacyDebugConsole();
}

void legacyDebugConsoleShutdown()
{
   if (!legacyDebugConsoleInitialized)
      return;
   legacyDebugConsoleClose();
#if defined(WINCE) || defined(WIN32)
   DeleteCriticalSection(&legacyDebugConsoleMutex);
#else
   pthread_mutex_destroy(&legacyDebugConsoleMutex);
#endif
   legacyDebugConsoleInitialized = false;
}
