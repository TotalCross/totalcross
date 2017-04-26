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
   if ((err = nativeStartGPS(p->obj[0])) > 0)
#else
   if ((err = nativeStartGPS()) > 0)
#endif
   {
      if (err == 2)
         throwException(p->currentContext, GPSDisabledException, "GPS is disabled");
      else
#ifdef ANDROID
      throwException(p->currentContext, IOException, err == 1 ? "No environment" : "Unknown error");
#else         
      throwExceptionWithCode(p->currentContext, IOException, err);
#endif      
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
