/*********************************************************************************
 *  TotalCross Virtual Machine, version 1                                        *
 *  Copyright (C) 2007-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *********************************************************************************/


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
