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

static void getWorkingDir()
{
   TCHARP2CharPBuf("/Applications/TotalCross.app", vmPath);
}

bool wokeUp()
{
   return false;
}

static void registerWake(bool set)
{
   UNUSED(set);
}

extern void privateFullscreen(bool on);

static void setFullScreen()
{
   privateFullscreen(true);
}
