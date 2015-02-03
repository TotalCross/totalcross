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



#include "tcvm.h"

void privateExit(int32 code);

//////////////////////////////////////////////////////////////////////////
TC_API void tuMW_restore(NMParams p) // totalcross/ui/MainWindow native public final void restore();
{
#if defined WP8
#elif defined ANDROID
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
#if defined WP8
#elif defined ANDROID
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
   keepRunning = false;
}
//////////////////////////////////////////////////////////////////////////
void setTimerInterval(int32 t)
{
   nextTimerTick = getTimeStamp() + t;
}

TC_API void tuMW_setTimerInterval_i(NMParams p) // totalcross/ui/MainWindow native void setTimerInterval(int n);
{
   setTimerInterval(p->i32[0]);
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
