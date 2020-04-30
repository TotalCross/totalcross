// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

//#define DEBUG_TO_ADB_ONLY // uncomment this to force all output to go to ADB

#include "tcvm.h"

#if defined(WINCE) || defined(WIN32)
 #include "win/debug_c.h"
#elif defined(ANDROID)
 #include "android/debug_c.h"
#else
 #include "posix/debug_c.h"
#endif

#if defined(ANDROID) || defined(WINCE)
void repaintActiveWindows(Context currentContext);
#endif

///////////////////////////////////////////////////////////////////////////
//                                Debug                                  //
///////////////////////////////////////////////////////////////////////////
static char debugstrSmall[64]; // used during startup and exit, when debugstr is not valid, for SHORT MESSAGES!

bool initDebug()
{
   debugstr = malloc(16384); // don't use xmalloc!
   return debugstr != null && privateInitDebug();
}

void destroyDebug()
{
#ifdef TRACK_USED_OPCODES
   int i;
   int8* usedOpcodes = usedOpcodes;
   debug("===========\nUsed opcodes:");
   for (i =0; i <=255 ; i++)
      if (usedOpcodes[i] > 0)
         debug("%3d",i);
#endif
   privateDestroyDebug();
   free(debugstr);
   debugstr = null;
}

void iphoneDebug(CharP s);
/* Displays the given char ptr in stdout (or somewhere else). */
TC_API bool debug(const char *s, ...)
{
   va_list args;
   char* buf = debugstr ? debugstr : debugstrSmall;
   if (debugstr == null) // guich@tc120_3: check disableDebug
   {
#ifdef ANDROID   
      __android_log_print(ANDROID_LOG_INFO, "TotalCross", s, "");
#endif         
      return false;  
   }

   va_start(args, s);

   vsprintf(buf, s, args);
   va_end(args);
   return debugStr(buf);
}

bool debugStr(char *s)
{
#ifdef ANDROID   
   if (s && !strEq(s,ALTERNATIVE_DEBUG)) // is the user asking to change the mode?
      __android_log_write(ANDROID_LOG_INFO, "TotalCross", s);
#elif defined darwin
   iphoneDebug(s);
#endif
   if (tcSettings.disableDebug && *tcSettings.disableDebug) // guich@tc120_3
      return false;
   return privateDebug(s);
}

TC_API bool trace(char *s) // used to trace function calls. also prints the memory available
{
   char* buf = debugstr ? debugstr : debugstrSmall;
   xstrprintf(buf, "#%s (%d)", s, getFreeMemory(false));
   debugStr(buf);
   return true;
}

void deleteDebugFile()
{
   debug(ERASE_DEBUG_STR);
}

TC_API bool alert(char *s, ...)
{
   if (s)
   {
      char* buf = debugstr ? debugstr : debugstrSmall;
      va_list args;
      va_start(args, s);
      vsprintf(buf, s, args);
      va_end(args);
      privateAlert(buf);   
#if defined(ANDROID) || defined(WINCE)
      if (debugstr && mainClass != null) // guich@tc123_
         repaintActiveWindows(mainContext);
#endif     
      return true;
   }
   return false;
}

TC_API void tcabort(char* msg, char* file, int32 line)
{
   debug("@@@ ABORT %s REQUESTED AT %s (%d)", msg, file, line);
}