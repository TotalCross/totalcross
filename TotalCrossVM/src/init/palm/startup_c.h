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



#define waitUntilStarted()
#define getWorkingDir()

bool wokeUp()
{
   static int32 last;
   int32 cur = getTimeStamp();
   UInt32 v=0;
   if ((cur-last) < 500) // don't poll too much
      return false;
   last = cur;

   if (FtrGet('PWER', 1, &v) == errNone) // WOKEUP state?
   {
      FtrUnregister('PWER',1);
      return true;
   }
   return false;
}

static void registerWake(bool reg)
{
   UNUSED(reg)
}

static void setFullScreen()
{
}
