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

// $Id: startup_c.h,v 1.3 2011-01-04 13:31:20 guich Exp $

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
