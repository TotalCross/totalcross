// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include<unistd.h>

static bool privateInitDebug()
{
   return true;
}

static void privateDestroyDebug()
{
   legacyDebugConsoleDestroy("===============\n");
}

static bool privateDebug(char* str)
{
    bool err = true;
#if __APPLE__ && !defined darwin
   /* Desktop macOS historically writes Vm.debug messages to stdout only. */
   printf(str);
   printf("\n");
#else
   if (strEq(str,ERASE_DEBUG_STR))
      legacyDebugConsoleErase();
   else
   {
      err = legacyDebugConsoleDebugLine(str, "\n", true);
      if (!legacyDebugConsoleIsOpen())
         err = true;
      if (legacyDebugConsoleIsOpen())
      {
         printf(str);
         printf("\n");
      }
   }
#endif
    return err;
}

// Alert

#if defined(darwin)
void privateAlert(CharP str);
#else
static void privateAlert(CharP str)
{
   privateDebug(str);
}
#endif
