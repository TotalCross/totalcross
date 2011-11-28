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

#import <UIKit/UIKit.h>
#import <AudioToolbox/AudioToolbox.h>

#define Class __Class
#include "tcvm.h"
#undef Class


void vmVibrate(int32 ms)
{
   AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
}
