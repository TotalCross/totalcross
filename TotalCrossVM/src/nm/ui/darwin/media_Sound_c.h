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

// $Id: media_Sound_c.h,v 1.8 2011-01-04 13:31:07 guich Exp $

#if defined (darwin)
 #ifdef __cplusplus
  extern "C" {
 #endif

 void iphone_soundBeep(void);

 #ifdef __cplusplus
  };
 #endif
#endif // darwin


static void soundSetEnabled(int b)
{
}

static void soundBeep(void)
{
#if defined (darwin)
   iphone_soundBeep();
#endif   
}

bool soundTone(int freq, unsigned short duration)
{
}
