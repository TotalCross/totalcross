// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include <time.h>
#include <sys/time.h>
#include <errno.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/wait.h>

static void vmSetTime(TCObject time)
{
   struct tm tm;
   struct timeval tv;
   struct timezone tz;

   tm.tm_year     = Time_year(time);
   tm.tm_mon      = Time_month(time);
   tm.tm_mday     = Time_day(time);
   tm.tm_hour     = Time_hour(time);
   tm.tm_min      = Time_minute(time);
   tm.tm_sec      = Time_second(time);
   // tm.tm_yday = 0;  // irrelevant
   // tm.tm_wday = 0;  // irrelevant
   tm.tm_isdst = -1;   // unavailable
   gettimeofday(&tv, &tz);  // fill-up tz (buggy for Linux)
   tv.tv_sec = mktime(&tm);
   tv.tv_usec = Time_millis(time) * 1000;
   settimeofday(&tv, &tz);
}

void rebootDevice()
{
//   ExitWindowsEx(EWX_REBOOT,0);
}

#ifndef darwin // implemented in Vm_c.m
static int32 vmExec(TCHARP szCommand, TCHARP szArgs, int32 launchCode, bool wait)
{
   int32 ret = -1;

   int32 pid = fork();
   if (pid == 0)
   {
      // this is the child
      char *argv[4];

      argv[0] = "sh";
      argv[1] = "-c";
      argv[2] = szCommand ? szCommand : szArgs;
      argv[3] = 0;
      execv("/bin/sh", argv);
#ifndef darwin
      exit(127); // exit the fork
#else
      keepRunning = false;      
#endif      
   }
   if (wait)
   {
      // the parent waits... the child termination
      while (waitpid(pid, &ret, 0) == -1)
         if (errno != EINTR) ret = -1;
   }
   
   return ret;
}
#endif

void vmSetAutoOff(bool enable)
{
//   if ((!enable && oldAutoOffValue == 0) || (enable && oldAutoOffValue != 0))
//      oldAutoOffValue = (int32)SysSetAutoOffTime(oldAutoOffValue);
}

//////////// START OF KEY INTERCEPTION FUNCTIONS

static void vmShowKeyCodes(bool show)
{
   // must register all keys in all ranges so they can be displayed to the user
   UNUSED(show)
}

static void vmInterceptSpecialKeys(int32* keys, int32 len)
{
   if (interceptedSpecialKeys != null)
      freeArray(interceptedSpecialKeys);
   if (len == 0)
      interceptedSpecialKeys = null;
   else
   {
      int32 *dk;
      dk = interceptedSpecialKeys = newPtrArrayOf(Int32, len, null);
      if (interceptedSpecialKeys != null)
      {
         // map the TotalCross keys into the device-specific keys
         for (; len-- > 0; keys++, dk++)
            *dk = keyPortable2Device(*keys);
      }
   }
}
//////////// END OF KEY INTERCEPTION FUNCTIONS

#ifdef darwin
void vmClipboardCopy(JCharP string, int32 sLen); // in mainview.m
unsigned short* ios_ClipboardPaste();
TCObject vmClipboardPaste(Context currentContext)
{
   unsigned short* chars = ios_ClipboardPaste();
   return !chars ? null : createStringObjectFromJCharP(currentContext, chars,-1);
}
#else
static void vmClipboardCopy(JCharP string, int32 sLen) // JCharP
{
   //dfb_clipboard_set()
}

static TCObject vmClipboardPaste(Context currentContext)
{
   //dfb_clipboard_get()
   return createStringObjectFromTCHAR(currentContext, "", 0);
}
#endif
static bool vmIsKeyDown(int32 key)
{
   return 0;
}

static int32 vmGetRemainingBattery()
{
   return 100;
}


#if 0
static void *loadLib(UtfString lname, int libPrefix/*, int syspath*/) // TODO support client native libs in the app path
{
   void * h;

   TCHAR path[MAX_PATH];

   // append the user path and ".dll/.so" to the library name
   int l = (*vmGlobals->ascii2unicode)(path, vmGlobals->appPath, MAX_PATH);

   // try to prefix with "lib" on Linux
   int l2 = libPrefix ? (*vmGlobals->ascii2unicode)(path+l, "lib", MAX_PATH) : 0;
   int l3 = (*vmGlobals->ascii2unicode)(path+l+l2, lname.str, MAX_PATH);
   (*vmGlobals->ascii2unicode)(path+l+l2+l3, DLL_EXT, MAX_PATH);

   // first search in the user path
   _LOG_INFO((LOG_CAT_NLIB, "LoadLibrary(%s)", path));
   h = LoadLibrary(path);
   if (h == NULL) // not found in user path?
   {
      _LOG_INFO((LOG_CAT_NLIB, "LoadLibrary2(%s)", path+l));
      h = LoadLibrary(path+l); // skip the user path
      if (h == NULL)
      {
         _LOG_ERROR((LOG_CAT_NLIB, "LoadLibrary(%s) failed", lname.str));
      }
   }
   return h;
}
#endif


static bool vmTurnScreenOn(bool on)
{
   return false;
}

#ifndef darwin // implemented in Vm_c.m
void vmVibrate(int32 ms)
{
}
#endif
