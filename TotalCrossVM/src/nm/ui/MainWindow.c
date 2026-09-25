// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcvm.h"
#include <stdlib.h>
#include <string.h>

void privateExit(int32 code);

//////////////////////////////////////////////////////////////////////////
TC_API void tuMW_restore(NMParams p) // totalcross/ui/MainWindow native public final void restore();
{
#if defined ANDROID
   #define SOFT_UNEXIT 0x40000001
   privateExit(SOFT_UNEXIT);
#elif defined WIN32 // guich@tc122_49
   ShowWindow(mainHWnd, SW_RESTORE); 
   SetForegroundWindow(mainHWnd);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuMW_minimize(NMParams p) // totalcross/ui/MainWindow native public final void minimize();
{
#if defined ANDROID
   #define SOFT_EXIT 0x40000000
   privateExit(SOFT_EXIT);
#elif defined WIN32 // guich@tc122_49
   ShowWindow(mainHWnd, SW_MINIMIZE);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuMW_exit_i(NMParams p) // totalcross/ui/MainWindow native public final void exit(int exitCode);
{
   exitCode = p->i32[0];
   printf("tuMW_exit_i\n");    
   keepRunning = false;
}
static int32 parseTimerDeadlineMode(const char *value);
static int64 calculateAbsoluteTimerDeadlineNs(int64 nowNs,
   int64 pendingDeadlineNs, int64 previousDeadlineNs,
   int32 previousIntervalMs, int32 intervalMs);

//////////////////////////////////////////////////////////////////////////
void setTimerInterval(int32 t)
{
   nextTimerTick = getTimeStamp() + t;
   if (isAbsoluteTimerDeadlineMode())
   {
      int64 pendingDeadlineNs = nextTimerDeadlineNs;
      int32 previousIntervalMs = absoluteTimerIntervalMs;
      int64 nowNs = getNanoTime();

      if (t == 0)
      {
         nextTimerDeadlineNs = 0;
         lastFiredTimerDeadlineNs = 0;
         absoluteTimerIntervalMs = 0;
      }
      else
      {
         nextTimerDeadlineNs = calculateAbsoluteTimerDeadlineNs(
            nowNs, pendingDeadlineNs, lastFiredTimerDeadlineNs,
            previousIntervalMs, t);
         absoluteTimerIntervalMs = t;
      }
   }
}

static int32 parseTimerDeadlineMode(const char *value)
{
   return value != NULL && strcmp(value, "absolute") == 0;
}

bool isAbsoluteTimerDeadlineMode(void)
{
   static int32 mode = -1;
   if (mode < 0)
      mode = parseTimerDeadlineMode(getenv("TC_TIMER_DEADLINE_MODE"));
   return mode != 0;
}

static int64 calculateAbsoluteTimerDeadlineNs(int64 nowNs,
   int64 pendingDeadlineNs, int64 previousDeadlineNs,
   int32 previousIntervalMs, int32 intervalMs)
{
   int64 intervalNs;
   int64 deadlineNs;

   if (intervalMs == 0)
      return 0;

   intervalNs = (int64)intervalMs * 1000000;
   if (intervalMs != previousIntervalMs || intervalMs < 0)
      return nowNs + intervalNs;

   if (previousDeadlineNs != 0 && intervalNs > 0)
   {
      deadlineNs = previousDeadlineNs + intervalNs;
      if (deadlineNs <= nowNs)
         deadlineNs += ((nowNs - deadlineNs) / intervalNs + 1) * intervalNs;
   }
   else
      deadlineNs = nowNs + intervalNs;

   if (pendingDeadlineNs != 0 && pendingDeadlineNs < deadlineNs)
      return pendingDeadlineNs;
   return deadlineNs;
}

TC_API void tuMW_setTimerInterval_i(NMParams p) // totalcross/ui/MainWindow native void setTimerInterval(int n);
{
   setTimerInterval((p->i32[0] < *tcSettings.minimalUpdateInterval) ? p->i32[0] : *tcSettings.minimalUpdateInterval);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuMW_getCommandLine(NMParams p) // totalcross/ui/MainWindow native public static String getCommandLine();
{
   p->retO = createStringObjectFromCharP(p->currentContext, commandLine,-1);
   if (p->retO)
      setObjectLock(p->retO, UNLOCKED);
}

#ifdef ENABLE_TEST_SUITE
#include "MainWindow_test.h"
#endif
