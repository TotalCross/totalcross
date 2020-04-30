// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"

#if _WIN32_WCE >= 300
 #include "win/media_Camera_c.h"
#elif defined(ANDROID)
 #include "android/media_Camera_c.h"
#elif defined(darwin)
 #include "darwin/media_Camera_c.h"
#endif

//#define TOGGLE_CLICK_BENCH

#ifdef TOGGLE_CLICK_BENCH
#define CLICK_BENCH(x) x
#else
#define CLICK_BENCH(x)
#endif

//////////////////////////////////////////////////////////////////////////
void createTempFileName(char* dest, char* ext)
{
   IntBuf intBuf;
   xstrcpy(dest, getAppPath());
   xstrcat(dest, "/");
   xstrcat(dest, getApplicationIdStr());
   xstrcat(dest, int2str(getTimeStamp(), intBuf));
   xstrcat(dest, ext);
}
TC_API void tumC_nativeClick(NMParams p) // totalcross/ui/media/Camera native private String nativeClick();
{
#if defined(ANDROID) || defined(darwin) || defined(WP8) || (defined(WINCE) && _WIN32_WCE >= 300)
   cameraClick(p);
#else
   UNUSED(p);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tumC_nativeFinalize(NMParams p) // totalcross/ui/media/Camera native private void nativeFinalize();
{
   UNUSED(p)
}
//////////////////////////////////////////////////////////////////////////
TC_API void tumC_getNativeResolutions(NMParams p) // totalcross/ui/media/Camera static native private String getNativeResolutions();
{
#if defined(ANDROID)
   setObjectLock(p->retO = Camera_getNativeResolutions(p->currentContext), UNLOCKED);
#else
   p->retO = null;
#endif
}
