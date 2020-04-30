// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "RadioDevice.h"

#if defined (WIN32) || defined (WINCE)
 #include "win/RadioDevice_c.h"
#elif defined (ANDROID)
 #include "android/RadioDevice_c.h"
#else
// #include "posix/RadioDevice_c.h"
#endif

//////////////////////////////////////////////////////////////////////////
TC_API void tidRD_isSupported_i(NMParams p) // totalcross/io/device/RadioDevice native public static boolean isSupported(int type) throws IllegalArgumentException;
{
   int32 type = p->i32[0];
   
   if (type < WIFI || type > BLUETOOTH)
      throwIllegalArgumentException(p->currentContext, null);
   else
#if defined (WINCE)
      p->retI = RdIsSupported(type);
#elif defined (ANDROID)
      p->retI = RdIsSupported(p->currentContext, type);
#elif defined WP8
      p->retI = true;
#else
   p->retI = false;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidRD_getState_i(NMParams p) // totalcross/io/device/RadioDevice native public static int getState(int type) throws IllegalArgumentException;
{
   int32 type = p->i32[0];
   
   if (type < WIFI || type > BLUETOOTH)
      throwIllegalArgumentException(p->currentContext, null);
   else
   {
#if defined (WINCE)      
      p->retI = RdGetState(type);
#elif defined (ANDROID)
      p->retI = RdGetState(p->currentContext, type);
#elif defined (WP8)
      p->retI = rdGetStateCPP(type);
#else
      p->retI = RADIO_STATE_DISABLED;
#endif      
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidRD_setState_ii(NMParams p) // totalcross/io/device/RadioDevice native public static void setState(int type, int state) throws IllegalArgumentException;
{
   int32 type = p->i32[0];
   int32 state = p->i32[1];

   if (state < RADIO_STATE_DISABLED || state > BLUETOOTH_STATE_DISCOVERABLE || (type != BLUETOOTH && state > RADIO_STATE_ENABLED))
      throwIllegalArgumentException(p->currentContext, null);
   if (type < WIFI || type > BLUETOOTH)
      throwIllegalArgumentException(p->currentContext, null);

#if defined (WINCE)
   RdSetState(type, state);
#elif defined (ANDROID)
   RdSetState(p->currentContext, type, state);
#endif
}

#ifdef ENABLE_TEST_SUITE
//#include "RadioDevice_test.h"
#endif
