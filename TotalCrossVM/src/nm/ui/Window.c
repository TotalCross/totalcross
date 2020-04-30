// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



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
TC_API void tuW_isSipShown(NMParams p) // totalcross/ui/Window native public static boolean isSipShown();
{     
   int32 ret = 0;
#if defined (WINCE) && _WIN32_WCE >= 300
   if (*tcSettings.virtualKeyboardPtr)
      ret = windowGetSIP();
#elif defined (WP8)
      ret = privateWindowGetSIP();
#elif defined(darwin)
   ret = windowGetSIP();
#elif defined (ANDROID)
   ret = windowGetSIP();
#elif defined (WIN32) && !defined(WINCE) // for windows 8 and up tablet devices
   ret = windowGetSIP();
#endif   
   p->retI = ret;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuW_setSIP_icb(NMParams p) // totalcross/ui/Window native public static void setSIP(int sipOption, totalcross.ui.Control control, boolean secret);
{
   int32 sipOption = p->i32[0];

   if (sipOption < SIP_HIDE || sipOption > SIP_SHOW)
      throwIllegalArgumentExceptionI(p->currentContext, "sipOption", sipOption);
   else
#if defined (WINCE) && _WIN32_WCE >= 300
   if (*tcSettings.virtualKeyboardPtr)
      windowSetSIP(sipOption, p->i32[1]);
#elif defined (WP8)
      privateWindowSetSIP(sipOption != SIP_HIDE);
#elif defined(darwin)
   windowSetSIP(p->currentContext, sipOption, p->obj[0] /*control*/, p->i32[1] /*numeric*/);
#elif defined (ANDROID)
   windowSetSIP(sipOption, p->i32[1] /*numeric*/);
#elif defined (WIN32) && !defined(WINCE) // for windows 8 and up tablet devices
   windowSetSIP(sipOption, p->i32[1]);
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
