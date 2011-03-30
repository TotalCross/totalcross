/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: time.c,v 1.7 2011-01-04 13:31:15 guich Exp $

#if HAVE_CONFIG_H
#include "config.h"
#endif

#ifdef __arm__
#include <PalmOSARM.h>
#else
#include <PalmOS.h>
#endif

#include "time.h"
#include "stdio.h"
#include <assert.h>
#include <string.h>

static int m_to_d[12] = {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};
static UInt32 secs_before_1970;

/*
 * Compute once the number of seconds between 1904 (PalmOS epoch) and 1970 (Unix epoch)
 */
static void compute_secs_before_1970()
{
   DateTimeType since1970;
   memset(&since1970, 0, sizeof(since1970));
   since1970.day = 1;
   since1970.month = 1;
   since1970.year = 1970;
   secs_before_1970 = TimDateTimeToSeconds(&since1970);
}

time_t time (time_t *_timer)
{
   UInt32 secs;

   if (secs_before_1970 == 0)
      compute_secs_before_1970();

   secs = TimGetSeconds() - secs_before_1970;
   if (_timer != NULL) *_timer = secs;
   return secs;
}

static time_t mkgmtime(struct tm *t)
{
   short month, year;
   time_t result;

   month = t->tm_mon;
   year = t->tm_year + month / 12 + 1900;
   month %= 12;
   if (month < 0)
   {
      year -= 1;
      month += 12;
   }
   result = (year - 1970) * 365 + (year - 1969) / 4 + m_to_d[month];
   result = (year - 1970) * 365 + m_to_d[month];
   if (month <= 1)
      year -= 1;
   result += (year - 1968) / 4;
   result -= (year - 1900) / 100;
   result += (year - 1600) / 400;
   result += t->tm_mday;
   result -= 1;
   result *= 24;
   result += t->tm_hour;
   result *= 60;
   result += t->tm_min;
   result *= 60;
   result += t->tm_sec;
   return result;
}

time_t mktime (struct tm *tp)
{
   /*
    * This field describes the time zone that was used to compute this broken-down time value,
    * including any adjustment for daylight saving; it is the number of seconds that you must
    * add to UTC to get local time.
    */
   return mkgmtime(tp) - tp->tm_gmtoff;
}

int gettimeofday(struct timeval *p, struct timezone *z)
{
   if (p != NULL)
   {
      if (secs_before_1970 == 0)
         compute_secs_before_1970();
      p->tv_sec = TimGetSeconds() - secs_before_1970;
      p->tv_usec = 0;
   }
   if (z != NULL)
   {
      // timezone: deprecated
      z->tz_minuteswest = PrefGetPreference(prefTimeZone);
      z->tz_dsttime = PrefGetPreference(prefDaylightSavingAdjustment);
   }
   return 0;
}

struct tm *localtime (const time_t *_timer)
{
   static struct tm _tm;
   time_t t;
   DateTimeType dt;

   t = (_timer == NULL) ? time(NULL) : *_timer;

   if (secs_before_1970 == 0)
      compute_secs_before_1970();

   t += secs_before_1970;
   TimSecondsToDateTime (t, &dt);

   _tm.tm_sec = dt.second;
   _tm.tm_min = dt.minute;
   _tm.tm_hour = dt.hour;
   _tm.tm_mday = dt.day;
   _tm.tm_mon = dt.month;
   _tm.tm_year = dt.year - 1900;
   _tm.tm_wday = dt.weekDay;
   _tm.tm_yday = m_to_d[dt.month] + dt.day;
   _tm.tm_gmtoff = PrefGetPreference(prefTimeZone) * 60;
   _tm.tm_isdst = PrefGetPreference(prefTimeZone) * 60;

   return &_tm;
}

struct tm *gmtime (const time_t *clock)
{
   struct tm *tm;
   Int16 timeZone = PrefGetPreference(prefTimeZone);
   Int16 daylightSavingAdjustment = PrefGetPreference(prefDaylightSavingAdjustment);
   UInt32 utcTime = TimTimeZoneToUTC(TimGetSeconds(), timeZone, daylightSavingAdjustment);
   if (secs_before_1970 == 0)
      compute_secs_before_1970();
   utcTime -= secs_before_1970;
   tm = localtime (&utcTime);
   tm->tm_gmtoff = 0;
   clock = clock;
   return tm;
}

char *asctime(struct tm *tp)
{
   static char result[3+1+3+3+1+2+1+2+1+2+1+4+2+1];
   static const char *day_name[7]  = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
   static const char *mon_name[12] = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

   assert(tp->tm_wday < 7 && tp->tm_mon < 12 && tp->tm_mday <= 31 && tp->tm_hour <= 23 && tp->tm_min <= 59
            && tp->tm_sec <= 59 && tp->tm_year < 8100);

   sprintf (result, "%s %s%3d %02d:%02d:%02d %d\n", day_name[tp->tm_wday], mon_name[tp->tm_mon],
               tp->tm_mday, tp->tm_hour, tp->tm_min, tp->tm_sec, 1900 + tp->tm_year);

   return result;
}

char *ctime (const time_t *timer)
{
   return asctime(localtime(timer));
}
