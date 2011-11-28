/*********************************************************************************
 *  TotalCross Virtual Machine, version 1                                        *
 *  Copyright (C) 2007-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *********************************************************************************/


#import <Foundation/Foundation.h>
#import <AudioToolbox/AudioToolbox.h>
#include "xtypes.h"

void iphone_soundBeep()
{
   AudioServicesPlayAlertSound(kSystemSoundID_Vibrate);
}
