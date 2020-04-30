// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
#include "SDL2/SDL.h"
#include "../Window.h"

static int32 sip;
static bool windowGetSIP()
{
   return sip != SIP_HIDE;
}

static void windowSetSIP(int32 sipOption)
{
   sip = sipOption;
   if(sip == SIP_HIDE) {
      SDL_StopTextInput();
   }
   else {
      SDL_StartTextInput();
   }
}

static void windowSetDeviceTitle(TCObject titleObj)
{
   UNUSED(titleObj)
}
