// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"

//////////////////////////////////////////////////////////////////////////
TC_API void tsT_update(NMParams p) // totalcross/sys/Time native void update();
{
   TCObject time = p->obj[0];
   int32 year,month,day,hour,minute,second,millis;
   
   getDateTime(&year,&month,&day,&hour,&minute,&second,&millis);
   
   Time_year(time)   = year;
   Time_month(time)  = month;
   Time_day(time)    = day;
   Time_hour(time)   = hour;
   Time_minute(time) = minute;
   Time_second(time) = second;
   Time_millis(time) = millis;
}

#ifdef ENABLE_TEST_SUITE
#include "Time_test.h"
#endif
