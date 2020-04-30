// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#define waitUntilStarted()

bool wokeUp()
{
   return false;
}

static void registerWake(bool set)
{
   UNUSED(set);
}

#if defined (darwin)
void setFullScreen();
void privateGetWorkingDir(CharP vmPath, CharP appPath);

static void getWorkingDir()
{
   privateGetWorkingDir(vmPath, appPath);
}

#else

void setFullScreen()
{
}

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
#endif
#endif
