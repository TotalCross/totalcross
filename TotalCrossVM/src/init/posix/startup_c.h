/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: startup_c.h,v 1.14 2011-01-04 13:31:07 guich Exp $

#if defined(linux)
#include <unistd.h>

static void getWorkingDir()
{
   // get the path to the vm
   TCHARP2CharPBuf("/usr/lib/totalcross", vmPath);

   char procname[FILENAME_MAX];
   int len = readlink("/proc/self/exe", procname, FILENAME_MAX - 1);
   if (len <= 0) // I guess we're not running on the right version of unix
	  return;
   procname[len] = '\0';

   TCHARP2CharPBuf(procname, appPath);
   char *sl = xstrrchr(appPath, '/'); // strip the file name from the path
   if (sl)
      *sl++ = 0;
   else
   {
      sl = appPath;
      TCHARP2CharPBuf(".", appPath);
   }

   TCHARP2CharPBuf(sl, exeName);
}
#elif defined(__SYMBIAN32__)
static void getWorkingDir()
{
   // get the path to the vm
   TCHARP2CharPBuf("/system/apps/TotalCross", vmPath);
   TCHARP2CharPBuf(".", appPath);
   TCHARP2CharPBuf("unknown", exeName);
}
#endif

#define waitUntilStarted()

bool wokeUp()
{
   return false;
}

static void registerWake(bool set)
{
   UNUSED(set);
}

static void setFullScreen()
{
}
