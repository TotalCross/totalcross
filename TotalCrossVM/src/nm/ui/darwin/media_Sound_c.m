// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda. 
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#import <Foundation/Foundation.h>
#import <AudioToolbox/AudioToolbox.h>
#include "xtypes.h"

static SystemSoundID sound;
static char lastSound[MAX_PATHNAME];
void iphone_soundPlay(CharP filename)
{
   if (!strEq(filename,lastSound))
   {
      if (sound) AudioServicesDisposeSystemSoundID(sound);
      xstrcpy(lastSound, filename);
      NSString* string = [NSString stringWithFormat:@"%s", filename];
      NSURL *wav = [NSURL fileURLWithPath:string];
      AudioServicesCreateSystemSoundID((__bridge CFURLRef)wav, &sound);
   }
   AudioServicesPlaySystemSound(sound);
}


void iphone_soundBeep()
{
   AudioServicesPlayAlertSound(kSystemSoundID_Vibrate);
}
