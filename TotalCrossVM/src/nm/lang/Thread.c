// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"
#if TC_OS_DESKTOP
#include <stdlib.h>
#include <string.h>

#if !TC_OS_WINDOWS
#include <sched.h>
#endif

static bool parseNativeThreadYieldMode(const char *value)
{
   return value != NULL && strcmp(value, "native") == 0;
}

static bool isNativeThreadYieldMode(void)
{
   static int32 mode = -1;
   if (mode < 0)
      mode = parseNativeThreadYieldMode(getenv("TC_THREAD_YIELD_MODE"));
   return mode != 0;
}
#endif

//////////////////////////////////////////////////////////////////////////
TC_API void jlT_yield(NMParams p) // java/lang/Thread native public static void yield();
{
   UNUSED(p)
#if TC_OS_DESKTOP
   if (isNativeThreadYieldMode())
   {
#if TC_OS_WINDOWS
      if (!SwitchToThread())
         Sleep(0);
#else
      sched_yield();
#endif
      return;
   }
#endif
   Sleep(1);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlT_start(NMParams p) // java/lang/Thread native public void start();
{
   threadCreateJava(p->currentContext, p->obj[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlT_currentThread(NMParams p) // java/lang/Thread native public static java.lang.Thread currentThread();
{
   p->retO = p->currentContext->threadObj; // guich@tc122_6
}

#ifdef ENABLE_TEST_SUITE
#include "Thread_test.h"
#endif
