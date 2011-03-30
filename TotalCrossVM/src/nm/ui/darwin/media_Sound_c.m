/*********************************************************************************
 *  TotalCross Virtual Machine, version 1                                        *
 *  Copyright (C) 2007-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *********************************************************************************/
// $Id: media_Sound_c.m,v 1.7 2011-01-04 13:31:07 guich Exp $

#ifdef darwin9
 #import <Foundation/Foundation.h>
 #import <AudioToolbox/AudioToolbox.h>
 #include "xtypes.h"
#endif

void iphone_soundBeep()
{
#ifdef darwin9 // only on 2.x or greater
   AudioServicesPlayAlertSound(kSystemSoundID_Vibrate);
#endif
}
