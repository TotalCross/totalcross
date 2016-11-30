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



#include "Window.h"

#if defined (WP8)
#elif defined (WINCE) || defined (WIN32)
 #include "win/Window_c.h"
#elif defined (darwin)
 #include "darwin/Window_c.h"
#elif defined(ANDROID)
 #include "android/Window_c.h"
#else
 #include "linux/Window_c.h"
#endif

//////////////////////////////////////////////////////////////////////////
TC_API void tuW_setSIP_icb(NMParams p) // totalcross/ui/Window native public static void setSIP(int sipOption, totalcross.ui.Control control, boolean secret);
{
   int32 sipOption = p->i32[0];

   if (sipOption < SIP_HIDE || sipOption > SIP_DISABLE_NUMERICPAD)
      throwIllegalArgumentExceptionI(p->currentContext, "sipOption", sipOption);
   else
#if defined (WINCE) && _WIN32_WCE >= 300
   if (*tcSettings.virtualKeyboardPtr || sipOption == SIP_ENABLE_NUMERICPAD || sipOption == SIP_DISABLE_NUMERICPAD)
      windowSetSIP(sipOption);
#elif defined (WP8)
      privateWindowSetSIP(sipOption != SIP_HIDE);
#elif defined(darwin)
   windowSetSIP(p->currentContext, sipOption, p->obj[0] /*control*/, p->i32[1] /*secret*/);
#elif defined (ANDROID)
   windowSetSIP(sipOption);
#elif defined (WIN32) && !defined(WINCE) // for windows 8 and up tablet devices
   windowSetSIP(sipOption);
#else
   ;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuW_pumpEvents(NMParams p) // totalcross/ui/Window native public static void pumpEvents();
{
   pumpEvents(p->currentContext);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuW_setDeviceTitle_s(NMParams p) // totalcross/ui/Window native public static void setDeviceTitle(String title);
{
   UNUSED(p);
#ifndef darwin
   windowSetDeviceTitle(p->obj[0]); // guich@tc113_32: changed 1 to 0
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuW_setOrientation_i(NMParams p) // totalcross/ui/Window native public static void setOrientation(int orientation);
{
#ifdef ANDROID
   windowSetOrientation(p->i32[0]);
#endif
}

#ifdef ENABLE_TEST_SUITE
#include "Window_test.h"
#endif
