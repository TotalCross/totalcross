// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
#if __APPLE__
#include "SDL.h"
#else
#include "SDL2/SDL.h"
#endif
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
