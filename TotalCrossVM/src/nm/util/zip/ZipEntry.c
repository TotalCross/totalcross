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

#define TICKSPERSEC        10000000
#define SECSPERDAY         86400

/* 1601 to 1970 is 369 years plus 89 leap days */
#define SECS_1601_TO_1970  ((369 * 365 + 89) * (ULONGLONG) SECSPERDAY)
#define TICKS_1601_TO_1970 (SECS_1601_TO_1970 * TICKSPERSEC)

//////////////////////////////////////////////////////////////////////////
TC_API void tuzZE_setTime_l(NMParams p) // totalcross/util/zip/ZipEntry native public void setTime(long time);
{
   TCObject zipEntryObj = p->obj[0];
   int64 time = p->i64[0];
   
#if defined (WIN32) || defined (WINCE)   
   LARGE_INTEGER fileTimeUTC;
   FILETIME fileTimeLocal;
   SYSTEMTIME systemTime;
   Err err;

   ULONGLONG secs = (time / 1000) * (ULONGLONG) TICKSPERSEC + TICKS_1601_TO_1970;
   fileTimeUTC.u.LowPart  = (DWORD) secs;
   fileTimeUTC.u.HighPart = (DWORD)(secs >> 32);

#ifdef WP8
   fileTimeLocal = *(FILETIME*)&fileTimeUTC;
#else
   err = FileTimeToLocalFileTime((FILETIME*) &fileTimeUTC, &fileTimeLocal);
#endif
   err = FileTimeToSystemTime(&fileTimeLocal, &systemTime);

   ZipEntry_time(zipEntryObj) = (systemTime.wYear - 1980) << 25
                                 | systemTime.wMonth << 21
                                 | systemTime.wDay << 16
                                 | systemTime.wHour << 11
                                 | systemTime.wMinute << 5
                                 | systemTime.wSecond >> 1;
#elif defined (POSIX) || defined (ANDROID) || defined (darwin)
   time_t fileTimeLocal = (time_t)(time / 1000); // milliseconds -> seconds
   struct tm* systemTime;

   systemTime = localtime(&fileTimeLocal);
   ZipEntry_time(zipEntryObj) = (systemTime->tm_year - 80) << 25
                                 | (systemTime->tm_mon + 1) << 21
                                 | systemTime->tm_mday << 16
                                 | systemTime->tm_hour << 11
                                 | systemTime->tm_min << 5
                                 | systemTime->tm_sec >> 1;
#endif   
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuzZE_getTime(NMParams p) // totalcross/util/zip/ZipEntry native public long getTime();
{
   TCObject zipEntryObj = p->obj[0];
   int32 dostime = ZipEntry_time(zipEntryObj);

   if (dostime == -1)
      p->retL = -1;
   else
   {
#if defined (WIN32) || defined (WINCE)      
      SYSTEMTIME systemTime = {0};
      FILETIME fileTimeLocal;
      LARGE_INTEGER fileTimeUTC;
      ULONGLONG tmp;
      Err err;

      systemTime.wYear = (((dostime >> 25) & 0x7f) + 1980);
      systemTime.wMonth = (((dostime >> 21) & 0x0f));
      systemTime.wDay = ((dostime >> 16) & 0x1f);
      systemTime.wHour = ((dostime >> 11) & 0x1f);
      systemTime.wMinute = ((dostime >> 5) & 0x3f);
      systemTime.wSecond = ((dostime << 1) & 0x3e);
      
      err = SystemTimeToFileTime(&systemTime, &fileTimeLocal);
#ifdef WP8
      fileTimeUTC = *(LARGE_INTEGER*)&fileTimeLocal;
#else
      err = LocalFileTimeToFileTime(&fileTimeLocal, (FILETIME*) &fileTimeUTC);
#endif

      tmp = ((ULONGLONG) fileTimeUTC.u.HighPart << 32) | fileTimeUTC.u.LowPart;
      tmp = tmp / TICKSPERSEC - SECS_1601_TO_1970;
      if (tmp > 0xffffffff)
         p->retL = -1;
      else
         p->retL = tmp * 1000;
#elif defined (POSIX) || defined (ANDROID) || defined (darwin)
      struct tm systemTime;
      
      systemTime.tm_year = (((dostime >> 25) & 0x7f) + 80);
      systemTime.tm_mon = (((dostime >> 21) & 0x0f)) - 1;
      systemTime.tm_mday = ((dostime >> 16) & 0x1f);
      systemTime.tm_hour = ((dostime >> 11) & 0x1f);
      systemTime.tm_min = ((dostime >> 5) & 0x3f);
      systemTime.tm_sec = ((dostime << 1) & 0x3e);
      
      p->retL = ((int64) mktime(&systemTime)) * 1000; // seconds -> milliseconds
#endif
   }
}

#ifdef ENABLE_TEST_SUITE
//#include "ZipEntry_test.h"
#endif
