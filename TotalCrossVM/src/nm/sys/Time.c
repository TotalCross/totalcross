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

// $Id: Time.c,v 1.28 2011-01-04 13:31:08 guich Exp $

#include "tcvm.h"

//////////////////////////////////////////////////////////////////////////
TC_API void tsT_update(NMParams p) // totalcross/sys/Time native void update();
{
   Object time = p->obj[0];
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
