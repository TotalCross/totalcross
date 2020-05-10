// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only



#if defined (darwin)
 #ifdef __cplusplus
  extern "C" {
 #endif

 void iphone_soundPlay(CharP filename);
 void iphone_soundBeep();

 #ifdef __cplusplus
  };
 #endif
#endif // darwin


static void soundPlay(CharP filename)
{
   iphone_soundPlay(filename);
}

static void soundSetEnabled(int b)
{
}

/*static void soundBeep(void)
{
   iphone_soundBeep();
}*/

bool soundTone(int freq, unsigned short duration)
{
   return false;
}
