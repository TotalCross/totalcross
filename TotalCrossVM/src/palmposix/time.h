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



#ifndef __TIME_H__
#define __TIME_H__

#ifdef __arm__
#include <PalmOSARM.h>
#else
#include <PalmOS.h>
#endif

typedef UInt32 time_t;
#define __time_t_defined
/*
 * time_t type: This is the data type used to represent simple time. Sometimes, it also represents an elapsed time.
 * When interpreted as a calendar time value, it represents the number of seconds elapsed since 00:00:00 on January 1, 1970,
 * Coordinated Universal Time. (This calendar time is sometimes referred to as the epoch.)
 */

struct tm
{
  int   tm_sec;
  int   tm_min;
  int   tm_hour;
  int   tm_mday;
  int   tm_mon;
  int   tm_year;
  int   tm_wday;
  int   tm_yday;
  int   tm_isdst;
  int   tm_gmtoff;
};

struct timeval
{
  long tv_sec;
  long tv_usec;
};

struct timezone
{
  int tz_minuteswest;
  int tz_dsttime;
};

extern time_t time (time_t *timer);
extern time_t mktime (struct tm *tp);
extern int gettimeofday(struct timeval *p, struct timezone *z);

extern struct tm *localtime (const time_t *timer);
extern struct tm *gmtime (const time_t *clock);

extern char *asctime(struct tm *tp);
extern char *ctime (const time_t *timer);

#endif //__TIME_H__
