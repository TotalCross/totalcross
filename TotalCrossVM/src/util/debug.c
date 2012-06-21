/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

//#define DEBUG_TO_ADB_ONLY // uncomment this to force all output to go to ADB

#ifdef DEBUG_TO_ADB_ONLY
#pragma warn ============================ DEBUGGING TO ADB ==========================
#endif

#include "tcvm.h"

#if defined(WINCE) || defined(WIN32)
 #include "win/debug_c.h"
#elif defined(PALMOS)
 #include "palm/debug_c.h"
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

bool initDebug()
{
#ifndef PALMOS
   debugstr = malloc(16384); // don't use xmalloc!
#else
   debugstr = malloc(4096);
#endif
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

/* Displays the given char ptr in stdout (or somewhere else). */
TC_API bool debug(const char *s, ...)
{
   va_list args;
   if (debugstr == null) // guich@tc120_3: check disableDebug
      return false;

   va_start(args, s);

   vsprintf(debugstr, s, args);
   va_end(args);
   return debugStr(debugstr);
}

bool debugStr(char *s)
{
   if (tcSettings.disableDebug && *tcSettings.disableDebug) // guich@tc120_3
      return false;
   return privateDebug(s);
}

TC_API bool trace(char *s) // used to trace function calls. also prints the memory available
{
   if (debugstr == null)
      debugStr(s);
   else
   {
      xstrprintf(debugstr, "#%s (%d)", s, getFreeMemory(false));
      debugStr(debugstr);
   }        
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
      if (debugstr) // allow debugging past end of destroyAll
      {
         va_list args;
         va_start(args, s);
         vsprintf(debugstr, s, args);
         va_end(args);
         privateAlert(debugstr);   
#if defined(ANDROID) || defined(WINCE)
         if (mainClass != null) // guich@tc123_
            repaintActiveWindows(mainContext);
#endif     
         return true;
      }
      else 
      {
         privateAlert(s);
         return true;
      }
   }
   return false;
}
