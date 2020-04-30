// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda. 
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

int iphone_gpsStart();
void iphone_gpsStop();
int iphone_gpsUpdateLocation(bool *flags, int *date, int *time, int* sat, double *veloc, double* pdop, double* dir, double* lat, double* lon);

static Err nativeStartGPS()
{
   return iphone_gpsStart();
}

static void nativeStopGPS()
{
   iphone_gpsStop();
}

static Err nativeUpdateLocation(Context currentContext, TCObject gpsObject, int32* flags)
{
   int date=0, time=0, sat=0;
   double veloc=0, pdop=0, dir=0, lat=0, lon=0;
   TCObject lastFix = GPS_lastFix(gpsObject);
   int ret = iphone_gpsUpdateLocation(flags, &date, &time, &sat, &veloc, &pdop, &dir, &lat, &lon);

   if (ret == 0 && *flags != 0)
   {
      GPS_latitude(gpsObject) = lat;
      GPS_longitude(gpsObject) = lon;
      GPS_direction(gpsObject) = dir;
      GPS_pdop(gpsObject) = pdop;
      GPS_satellites(gpsObject) = sat;
      GPS_velocity(gpsObject) = veloc;
      Time_day(lastFix)    = date % 100; date /= 100;
      Time_month(lastFix)  = date % 100; date /= 100;
      Time_year(lastFix)   = date;
      Time_second(lastFix) = time % 100; time /= 100;
      Time_minute(lastFix) = time % 100; time /= 100;
      Time_hour(lastFix)   = time;
      Time_millis(lastFix) = 0;
   }  
   return ret;
}
