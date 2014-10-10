/*********************************************************************************
 *  TotalCross Virtual Machine, version 1                                        *
 *  Copyright (C) 2007-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *********************************************************************************/


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
