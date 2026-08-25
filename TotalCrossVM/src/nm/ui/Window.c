// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "Window.h"
#include "WindowSafeArea.h"
#include "GraphicsPrimitives.h"

#if defined (WINCE) || defined (WIN32)
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
//////////////////////////////////////////////////////////////////////////
TCObject* safeAreaInsets;
static bool safeAreaInsetsInitialized;
static Method updateSafeAreaInsetsMethod;

static int32 windowPhysicalToLogical(int32 value)
{
   double scale = screen.contentScale > 0 ? screen.contentScale : 1;
   double logical = value / scale;
   return logical >= 0 ? (int32)(logical + 0.5) : (int32)(logical - 0.5);
}

void windowUpdateSafeAreaInsetsPhysical(
   Context currentContext,
   int32 top,
   int32 left,
   int32 bottom,
   int32 right)
{
   if (currentContext == null)
      return;

   if (updateSafeAreaInsetsMethod == null)
   {
      TCClass windowClass = loadClass(currentContext, "totalcross.ui.Window", true);
      if (windowClass == null || currentContext->thrownException != null)
         return;
      updateSafeAreaInsetsMethod = getMethod(
         windowClass,
         false,
         "_updateSafeAreaInsets",
         4,
         J_INT,
         J_INT,
         J_INT,
         J_INT);
   }

   if (updateSafeAreaInsetsMethod != null)
   {
      safeAreaInsetsInitialized = true;
      executeMethod(
         currentContext,
         updateSafeAreaInsetsMethod,
         0, //windowPhysicalToLogical(top),
         0, //windowPhysicalToLogical(left),
         0, //windowPhysicalToLogical(bottom),
         0  //windowPhysicalToLogical(right)
      );
   }
}

TC_API void tuW_getSafeAreaInsets(NMParams p) // totalcross/ui/Window public static Insets getSafeAreaInsets();
{
   if (safeAreaInsets == null) {
      safeAreaInsets = getStaticFieldObject(p->currentContext, loadClass(p->currentContext, "totalcross.ui.Window", true), "safeAreaInsets");
   }
#if defined darwin || defined ANDROID
   if (!safeAreaInsetsInitialized && safeAreaInsets != null && p->currentContext->thrownException == null) {
      int32 top = 0, left = 0, bottom = 0, right = 0;
      windowGetSafeAreaInsets(
         &top,
         &left,
         &bottom,
         &right
      );
      FIELD_I32(*safeAreaInsets, 0) = 0; //windowPhysicalToLogical(top);
      FIELD_I32(*safeAreaInsets, 1) = 0; //windowPhysicalToLogical(left);
      FIELD_I32(*safeAreaInsets, 2) = 0; //windowPhysicalToLogical(bottom);
      FIELD_I32(*safeAreaInsets, 3) = 0; //windowPhysicalToLogical(right);
      safeAreaInsetsInitialized = true;
   }
#endif
   p->retO = (*safeAreaInsets);
}

#ifdef ENABLE_TEST_SUITE
#include "Window_test.h"
#endif
