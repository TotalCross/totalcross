// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "Window.h"
#include "WindowSafeArea.h"
#include "GraphicsPrimitives.h"

#if TC_WINDOWING_SDL
 #include "sdl/Window_c.h"
#elif TC_WINDOWING_NATIVE
 #if TC_OS_WINDOWS || TC_OS_WINCE
  #include "win/Window_c.h"
 #elif TC_OS_LINUX
  #include "linux/Window_c.h"
 #elif TC_OS_ANDROID
  #include "android/Window_c.h"
 #elif TC_OS_IOS
  #include "darwin/Window_c.h"
 #else
  #error Unsupported native Window backend
 #endif
#else
 #error No Window backend selected
#endif

int32 windowBackendSetSize(int32 width, int32 height)
{
   return windowBackendSetSizeImpl(width, height);
}

#if TC_OS_WINDOWS || TC_OS_WINCE
 #include "win/WindowServices_c.h"
#elif TC_OS_MACOS
 #include "macos/WindowServices_c.h"
#elif TC_OS_LINUX
 #include "linux/WindowServices_c.h"
#elif TC_OS_ANDROID
 #include "android/WindowServices_c.h"
#elif TC_OS_IOS
 #include "darwin/WindowServices_c.h"
#else
 #error Unsupported Window platform services
#endif

//////////////////////////////////////////////////////////////////////////
TC_API void tuW_isSipShown(NMParams p) // totalcross/ui/Window native public static boolean isSipShown();
{     
   p->retI = windowPlatformIsSIPShown();
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuW_setSIP_icb(NMParams p) // totalcross/ui/Window native public static void setSIP(int sipOption, totalcross.ui.Control control, boolean secret);
{
   int32 sipOption = p->i32[0];

   if (sipOption < SIP_HIDE || sipOption > SIP_SHOW)
      throwIllegalArgumentExceptionI(p->currentContext, "sipOption", sipOption);
   else
      windowPlatformSetSIP(
         p->currentContext,
         sipOption,
         p->obj[0] /*control*/,
         p->i32[1] /*numeric*/
      );
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuW_pumpEvents(NMParams p) // totalcross/ui/Window native public static void pumpEvents();
{
   pumpEvents(p->currentContext);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuW_setDeviceTitle_s(NMParams p) // totalcross/ui/Window native public static void setDeviceTitle(String title);
{
   windowBackendSetDeviceTitle(p->obj[0]); // guich@tc113_32: changed 1 to 0
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuW_setOrientation_i(NMParams p) // totalcross/ui/Window native public static void setOrientation(int orientation);
{
   windowPlatformSetOrientation(p->i32[0]);
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
   if (!safeAreaInsetsInitialized && safeAreaInsets != null && p->currentContext->thrownException == null) {
      int32 top = 0, left = 0, bottom = 0, right = 0;
      windowPlatformGetSafeAreaInsets(
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
   p->retO = (*safeAreaInsets);
}

#ifdef ENABLE_TEST_SUITE
#include "Window_test.h"
#endif
