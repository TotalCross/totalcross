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
bool getSDCardPath(char* buf, int32 idx);
void playSound(CharP filename)
{
   char fullpath[MAX_PATHNAME];
   fullpath[0] = 0;
   if (strEqn(filename,"device/",7))
   {
      xstrcpy(fullpath, appPath);
      xstrcat(fullpath, filename + 6); // keep slash
   }
#ifdef ANDROID
   else
   if (strEqn(filename,"/sdcard",7))
   {
      if (getSDCardPath(fullpath, filename[7]-'0'))
         xstrcat(fullpath, filename + 7);
   }
#endif   
   else xstrcpy(fullpath,filename);
   soundPlay(fullpath);
}

TC_API void tumS_play_s(NMParams p) // totalcross/ui/media/Sound native public static void play(String filename);
{
   TCObject o = p->obj[0];
   if (o == null)
      throwNullArgumentException(p->currentContext, "filename");
   else
   {
      CharP filename = String2CharP(o);
      if (filename)
         playSound(filename);
      xfree(filename);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tumS_beep(NMParams p) // totalcross/ui/media/Sound native public static void beep();
{
   UNUSED(p);
   if (soundSettings.isSoundEnabled)
#if defined(darwin) || defined(ANDROID)
      playSound("device/chime.mp3");
#else           
      soundBeep();
#endif      
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
//////////////////////////////////////////////////////////////////////////
TC_API void tumS_toText_s(NMParams p) // totalcross/ui/media/Sound native public static String toText(String params);
{
#if defined(ANDROID)
   soundToText(p);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tumS_fromText_s(NMParams p) // totalcross/ui/media/Sound native public static void fromText(String text);
{
#if defined(ANDROID)
   soundFromText(p);
#endif
}

#ifdef ENABLE_TEST_SUITE
#include "media_Sound_test.h"
#endif
