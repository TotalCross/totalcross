// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#if HAVE_CONFIG_H
#include "config.h"
#endif

#include <dlfcn.h>
#include <string.h>
#include <stdio.h>
#include "xtypes.h"

#if __APPLE__
#define CURRENT_PATH "@executable_path"
#define PARENT_PATH "@executable_path/.."
#define LIB_EXTENSION "dylib"
#else
#define CURRENT_PATH "."
#define PARENT_PATH ".."
#define LIB_EXTENSION "so"
#endif

char *args = "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";

typedef int (*ExecuteProgramProc)(char* args);

typedef void *Handle;

static Handle tryOpen(const char *prefix, const char* libname)
{
   char path[MAX_PATHNAME];

   if (prefix) {
      snprintf(path, MAX_PATHNAME, "%s/%s.%s", prefix, libname, LIB_EXTENSION);
   }
   else {
      snprintf(path, MAX_PATHNAME, "%s.%s", libname, LIB_EXTENSION);
   }
   return dlopen(path, RTLD_LAZY);
}

static int executeProgram(char* cmdline)
{
   int ret = 0;
   ExecuteProgramProc fExecuteProgram = NULL;
   Handle tcvm;

   tcvm = tryOpen(CURRENT_PATH, "libtcvm");              // load in current folder - otherwise, we'll not be able to debug

   if (!tcvm) {
      printf("%s\n", dlerror());
      tcvm = tryOpen(PARENT_PATH, "libtcvm");            // load in parent folder
   }
#if __APPLE__
   if (!tcvm) {
      printf("%s\n", dlerror());
      tcvm = tryOpen("@rpath", "libtcvm");               // LC_RPATH pointing to a predefined path, like /usr/local/lib/totalcross
   }
   if (!tcvm) {
      printf("%s\n", dlerror());
      tcvm = tryOpen(NULL, "libtcvm");                   // search in paths from loader
   }
#endif
   if (!tcvm) {
      printf("%s\n", dlerror());
      tcvm = tryOpen("/usr/lib/totalcross", "libtcvm");  // load in most common absolute path
   }
   if (!tcvm) {
      printf("%s\n", dlerror());
      return 10000;
   }
   fExecuteProgram = (ExecuteProgramProc)dlsym(tcvm, TEXT("executeProgram"));
   if (!fExecuteProgram)
      return 10001;

   ret = fExecuteProgram(cmdline); // call the function now

   dlclose(tcvm); // free the library
   return ret;
}

int main(int argc, const char *argv[])
{
   char cmdline[512];
   xmemzero(cmdline,sizeof(cmdline));
   int argvIndex = 0;
   if (argv)
   {
      if (argc > 1 && 
         xstrlen(argv[0]) >= 8 &&
         memcmp(argv[0] + xstrlen(argv[0]) - 8, "Launcher", 8) == 0) {
         argvIndex = 1;
      }
      xstrcpy(cmdline, argv[argvIndex++]);
      xstrcat(cmdline, ".tcz");
   }
   if (argc > argvIndex || args[0] != '1') // if there's a commandline passed by the system or one passed by the user
   {
      xstrcat(cmdline, " /cmd ");
      if (args[0] != '1')
         xstrcat(cmdline, args);
      const char **p = argv + argvIndex;
      int n = argc;
      while (n-- > argvIndex)
      {
         xstrcat(cmdline, " ");
         xstrcat(cmdline, *p++);
      }
   }
   return executeProgram(cmdline); // in tcvm\startup.c
}
