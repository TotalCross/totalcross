// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifndef WINDOW_H
#define WINDOW_H

#include "tcvm.h"

enum TCSIP
{
   SIP_HIDE    = 10000, /** Used to hide the virtual keyboard */
   SIP_TOP     = 10001, /** Used to place the keyboard on top of screen */
   SIP_BOTTOM  = 10002, /** Used to place the keyboard on bottom of screen */
   SIP_SHOW    = 10003,  /** Used to show the virtual keyboard, without changing the position */
   SIP_ENABLE_NUMERICPAD = 10004,
   SIP_DISABLE_NUMERICPAD = 10005
};

#endif
