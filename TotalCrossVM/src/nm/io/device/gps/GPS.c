// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h" 
 
#if defined (WINCE)
 #include "win/GPS_c.h"
#elif defined (ANDROID)
 #include "android/GPS_c.h"
#elif defined (darwin)
 #include "darwin/GPS_c.h"
#endif

//////////////////////////////////////////////////////////////////////////
TC_API void tidgGPS_startGPS(NMParams p) // totalcross/io/device/gps/GPS native private boolean startGPS() throws totalcross.io.IOException;
{
#if defined(WINCE) || defined(ANDROID) || defined(darwin)
   Err err;

#ifdef ANDROID   
   if ((err = nativeStartGPS(p->obj[0])) != NO_ERROR)
#else
   if ((err = nativeStartGPS()) != NO_ERROR)
#endif
   {
      if (err == 2) {
         throwException(p->currentContext, GPSDisabledException, "GPS is disabled");
      }
#ifdef ANDROID
      else if (err == 1) {
         throwException(p->currentContext, IOException, "No environment");
      }
#endif
      else {
         throwExceptionWithCode(p->currentContext, IOException, err);
      }
   }
   p->retI = (err == NO_ERROR);
#elif defined (WP8)
   p->retI = nativeStartGPSCPP();
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidgGPS_updateLocation(NMParams p) // totalcross/io/device/gps/GPS native private int updateLocation();
{
   int32 flags = 0;
#if defined(WINCE) || defined(ANDROID) || defined(darwin)
   Err err;

   if ((err = nativeUpdateLocation(p->currentContext, p->obj[0], &flags)) > 0)
   {
      if (err == 2)
         throwException(p->currentContext, GPSDisabledException, "GPS is disabled");
      else
         throwExceptionWithCode(p->currentContext, IOException, err);
   }
   p->retI = flags;
#elif defined (WP8)
   p->retI = nativeUpdateLocationCPP(p->currentContext, p->obj[0]);
#endif 
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidgGPS_stopGPS(NMParams p) // totalcross/io/device/gps/GPS native private void stopGPS();
{
#if defined(WINCE) || defined(ANDROID) || defined(darwin)
   nativeStopGPS();
#elif defined (WP8)
   nativeStopGPSCPP();
#endif
}

#ifdef ENABLE_TEST_SUITE
//#include "GPS_test.h"
#endif
