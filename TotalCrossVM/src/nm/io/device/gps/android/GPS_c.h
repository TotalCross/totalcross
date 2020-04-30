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

#include <errno.h>

#define GPSFUNC_START   7
#define GPSFUNC_STOP    8
#define GPSFUNC_GETDATA 9

static Err nativeStartGPS(TCObject gpsObject)
{
   JNIEnv *env = getJNIEnv();
   jstring jgpsData;
   if (!env)
      return 1;
   
   jmethodID method = (*env)->GetStaticMethodID(env, applicationClass, "requestLocationPermission", "()I");
   jint result = (*env)->CallStaticIntMethod(env, applicationClass, method);
   if (result <= 0) {
       return EACCES;
   }
   
   jgpsData = (*env)->CallStaticObjectMethod(env, applicationClass, jgpsFunc, GPSFUNC_START, GPS_precision(gpsObject)); // guich@tc125_1
   if (jgpsData != null) (*env)->DeleteLocalRef(env, jgpsData);
   return jgpsData == null ? 2 : NO_ERROR;
}

static void nativeStopGPS()
{
   JNIEnv *env = getJNIEnv();
   if (env)
   {
      jobject ret = (*env)->CallStaticObjectMethod(env, applicationClass, jgpsFunc, GPSFUNC_STOP, -1);
      if (ret != null) (*env)->DeleteLocalRef(env, ret); // guich@tc125_1
   }
}

static void splitStr(char* what, char sep, char** into)
{
   int32 i = 0;
   into[i++] = what;
   for (; *what; what++)
      if (*what == sep)
      {
         *what = 0;
         if (what[1] != 0) // not end of string?
            into[i++] = what+1;
      }
}

static Err nativeUpdateLocation(Context currentContext, TCObject gpsObject, int32* flags)
{
   TCObject lastFix = GPS_lastFix(gpsObject);
   JNIEnv *env = getJNIEnv();
   jstring jgpsData;         
   char gpsData[255];
   char *split[7];
   int32 yy,mm,dd,HH,MM,SS,gpsPrecision;
   if (!env)
      return 1;

   jgpsData = (*env)->CallStaticObjectMethod(env, applicationClass, jgpsFunc, GPSFUNC_GETDATA, -1);
   if (jgpsData == null) // no provider?
      return NO_ERROR;
   jstring2CharP(jgpsData, gpsData);
   (*env)->DeleteLocalRef(env, jgpsData); // guich@tc125_1
   if (gpsData[0] == '*') // low signal?
      return NO_ERROR;
   // lat;lon;fix(yy/mm/ss hh:mm:ss);sat;vel;dir;
   splitStr(gpsData, ';', split);
   
   *flags |= 3;
   GPS_latitude(gpsObject) = str2double(split[0],null);
   GPS_longitude(gpsObject) = str2double(split[1],null);
   if (*split[3])
   {
      *flags |= 16;
      GPS_satellites(gpsObject) = str2int(split[3],null);
   }
   if (*split[4])
   {
      *flags |= 8;
      GPS_velocity(gpsObject) = str2double(split[4],null);
   }
   if (*split[5])
   {
      *flags |= 4;
      GPS_direction(gpsObject) = str2double(split[5],null);
   }
   if (*split[6]) // guich@tc126_66
   {
      *flags |= 32;
      GPS_pdop(gpsObject) = str2double(split[6],null);
   }
   
   sscanf(split[2],"%d/%d/%d %d:%d:%d",&yy,&mm,&dd,&HH,&MM,&SS);
   Time_year(lastFix)   = yy;
   Time_month(lastFix)  = mm;
   Time_day(lastFix)    = dd;
   Time_hour(lastFix)   = HH;
   Time_minute(lastFix) = MM;
   Time_second(lastFix) = SS;
   Time_millis(lastFix) = 0;

   return NO_ERROR;
}
