/*
 * Copyright (c) 2007-2016, Cameron Rich
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 * * Neither the name of the axTLS project nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * @file os_port.c
 *
 * OS specific functions.
 */
#if !defined(_WIN32_WCE)
#include <time.h>
#endif
#include <stdlib.h>
#include <stdio.h>
#if !defined(PALMOS) && !defined(_WIN32_WCE)
#include <errno.h>
#endif
#include <stdarg.h>
#include "os_port.h"

#define abort() tcabort("AXTLS",__FILE__,__LINE__) // TOTALCROSS

#ifdef WIN32
/**
 * gettimeofday() not in Win32
 */
EXP_FUNC void STDCALL gettimeofday(struct timeval* t, void* timezone)
{
#if defined(_WIN32_WCE)
    t->tv_sec = time(NULL);
    t->tv_usec = 0;                         /* 1sec precision only */
#else
    struct _timeb timebuffer;
    _ftime(&timebuffer);
    t->tv_sec = (long)timebuffer.time;
    t->tv_usec = 1000 * timebuffer.millitm; /* 1ms precision */
#endif
}

/**
 * strcasecmp() not in Win32
 */
EXP_FUNC int STDCALL strcasecmp(const char *s1, const char *s2)
{
    while (tolower(*s1) == tolower(*s2++))
    {
        if (*s1++ == '\0')
        {
            return 0;
        }
    }

    return *(unsigned char *)s1 - *(unsigned char *)(s2 - 1);
}

#endif

//+fdie@20090325 support certificate expiration dates beyond 2050
EXP_FUNC time_h *getNowUTC(time_h *t)
{
#if defined(WIN32)
   SYSTEMTIME tLocalTime;
   GetSystemTime(&tLocalTime);
   mk_time_h(t, tLocalTime.wYear, tLocalTime.wMonth, tLocalTime.wDay, tLocalTime.wHour, tLocalTime.wMinute, tLocalTime.wSecond, tLocalTime.wMilliseconds);
#else
   struct timeval tv;
   struct tm *tm;
   gettimeofday(&tv, NULL);
   tm = gmtime(&tv.tv_sec);
   mk_time_h(t, tm->tm_year+1900, tm->tm_mon+1, tm->tm_mday, tm->tm_hour, tm->tm_min, tm->tm_sec, tv.tv_usec);
#endif
   return t;
}

EXP_FUNC const char *asc_time_h(const time_h *t, char *buffer)
{
   int hour, day, month, year, min, sec;
   uint32_t v;

   v = t->high;
   hour = v % 24;
   v = (v - hour) / 24;
   day = v % 31;
   v = (v - day) / 31;
   month = v % 12;
   year = (v - month) / 12;
   v = t->low / 1000000;
   min = v / 60;
   sec = v % 60;
   sprintf(buffer, "%04d/%02d/%02d %02d:%02d:%02d", year, month, day, hour, min, sec);

   return buffer;
}
//-fdie@20090325

#if !defined _INC_TIME_INL
char* ctime(const time_t* timer)
{
	static char ascbuf[ASC_BUFF_SIZE];
	return asc_time_h((time_h*) timer, ascbuf);
}
#endif

#undef open
#undef fopen

#if !defined (TOTALCROSS_INTEGRATION) //flsobral@tc114_36: no longer used by totalcross.
#if !defined(PALMOS)

#undef malloc
#undef realloc
#undef calloc

static const char * out_of_mem_str = "out of memory";

/*
 * Some functions that call display some error trace and then call abort().
 * This just makes life much easier on embedded systems, since we're
 * suffering major trauma...
 */
EXP_FUNC void * STDCALL ax_malloc(size_t s)
{
    void *x;

    if ((x = malloc(s)) == NULL)
        exit_now(out_of_mem_str);

    return x;
}

EXP_FUNC void * STDCALL ax_realloc(void *y, size_t s)
{
    void *x;

    if ((x = realloc(y, s)) == NULL)
        exit_now(out_of_mem_str);

    return x;
}

#if defined (WINCE) && _WIN32_WCE < 300
EXP_FUNC void * STDCALL ax_calloc(size_t n, size_t s)
{
    void *x;

    if ((x = malloc(n*s)) == NULL)
        exit_now(out_of_mem_str);

    return x;
}
#else
EXP_FUNC void * STDCALL ax_calloc(size_t n, size_t s)
{
    void *x;

    if ((x = calloc(n, s)) == NULL)
        exit_now(out_of_mem_str);

    return x;
}
#endif

#endif //!defined(PALMOS)
#endif

#if !defined(TOTALCROSS_INTEGRATION)

static const char * file_open_str = "Could not open file \"%s\"";

EXP_FUNC FILE * STDCALL ax_fopen(const char *pathname, const char *type)
{
    FILE *f;

    if ((f = fopen(pathname, type)) == NULL)
        exit_now(file_open_str, pathname);

    return  f;
}

EXP_FUNC int STDCALL ax_open(const char *pathname, int flags)
{
    int x;

    if ((x = open(pathname, flags)) < 0)
        exit_now(file_open_str, pathname);

    return x;
}

/**
 * This is a call which will deliberately exit an application, but will
 * display some information before dying.
 */
void exit_now(const char *format, ...)
{
#if !defined(PALMOS)
    va_list argp;
    va_start(argp, format);
    vfprintf(stderr, format, argp);
    va_end(argp);
#endif
    abort();
}

#endif

#if defined(_WIN32_WCE)

static const __int64 sec_in100ns = (__int64)10000000;

static __int64 fileTime2int64(FILETIME f)
{
   __int64 t;

   t = f.dwHighDateTime;
   t <<= 32;
   t |= f.dwLowDateTime;
   return t;
}

static FILETIME yearToFileTime(WORD year)
{
   SYSTEMTIME s = {0};
   FILETIME f;

   s.wYear      = year;
   s.wMonth     = 1;
   s.wDayOfWeek = 1;
   s.wDay       = 1;

   SystemTimeToFileTime(&s, &f);
   return f;
}

static time_t fileTime2time_t(const FILETIME* f)
{
   FILETIME f1601, f1970;
   __int64 t, offset;

   f1601 = yearToFileTime(1601);
   f1970 = yearToFileTime(1970);

   offset = fileTime2int64(f1970) - fileTime2int64(f1601);
   t = fileTime2int64(*f);
   t -= offset;

   return (time_t)(t / sec_in100ns);
}

time_t time(time_t *timer)
{
   SYSTEMTIME s;
   FILETIME   f;
   static time_t t;

   if (timer == NULL) timer = &t;

   GetSystemTime( &s );
   SystemTimeToFileTime(&s, &f);
   *timer = fileTime2time_t(&f);

   return *timer;
}

static int m_to_d[12] = {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};

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

int _isatty(int fd)
{
   return (fd==(int)_fileno(stdin) || fd==(int)_fileno(stdout)|| fd==(int)_fileno(stderr)) ? 1 : 0;
}

#endif
