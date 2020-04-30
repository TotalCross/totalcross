// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



const int32 MAX_DAYS[13] = {0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

TESTCASE(tsT_update) // totalcross/sys/Time native void update();
{
   TNMParams p;
   TCObject time;

   time = createObject(currentContext, "totalcross.sys.Time");
   setObjectLock(time, UNLOCKED);
   ASSERT1_EQUALS(NotNull, time);
   p.obj = &time;
   p.currentContext = currentContext;

   tsT_update(&p);

   ASSERT_BETWEEN(I32,  2000, Time_year(p.obj[0])  ,   2020);
   ASSERT_BETWEEN(I32,     1, Time_month(p.obj[0]) ,     12);
   ASSERT_BETWEEN(I32,     1, Time_day(p.obj[0])   , MAX_DAYS[Time_month(p.obj[0])]);
   ASSERT_BETWEEN(I32,     0, Time_hour(p.obj[0])  ,     23);
   ASSERT_BETWEEN(I32,     0, Time_minute(p.obj[0]),     59);
   ASSERT_BETWEEN(I32,     0, Time_second(p.obj[0]),     59);
   ASSERT_BETWEEN(I32,     0, Time_millis(p.obj[0]),    999);

   finish: ;
}
