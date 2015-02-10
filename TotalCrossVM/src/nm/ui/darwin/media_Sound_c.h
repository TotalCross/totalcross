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
