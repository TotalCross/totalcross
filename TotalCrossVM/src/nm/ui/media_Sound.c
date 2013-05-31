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



#include "tcvm.h"

#if defined WINCE || defined WIN32
 #include "win/media_Sound_c.h"
#elif defined ANDROID
 #include "android/media_Sound_c.h"
#elif defined darwin
 #include "darwin/media_Sound_c.h"
#else
 #include "linux/media_Sound_c.h"
#endif

//////////////////////////////////////////////////////////////////////////
TC_API void tumS_beep(NMParams p) // totalcross/ui/media/Sound native public static void beep();
{
   UNUSED(p);
   if (soundSettings.isSoundEnabled)
      soundBeep();
}
//////////////////////////////////////////////////////////////////////////
TC_API void tumS_tone_ii(NMParams p) // totalcross/ui/media/Sound native public static void tone(int freq, int duration);
{
   if (soundSettings.isSoundEnabled)
      soundTone(p->i32[0], p->i32[1]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tumS_setEnabled_b(NMParams p) // totalcross/ui/media/Sound native public static void setEnabled(boolean on);
{
   bool enableSound = (bool) p->i32[0];

   if (enableSound != soundSettings.isSoundEnabled)
   {
      soundSetEnabled(enableSound);
      soundSettings.isSoundEnabled = enableSound;
   }
}

#ifdef ENABLE_TEST_SUITE
#include "media_Sound_test.h"
#endif
