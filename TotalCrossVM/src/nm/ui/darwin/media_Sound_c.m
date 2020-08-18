// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "xtypes.h"
#import <AudioToolbox/AudioToolbox.h>
#import <Foundation/Foundation.h>

static SystemSoundID sound;
static char lastSound[MAX_PATHNAME];
void
iphone_soundPlay(CharP filename)
{
  if (!strEq(filename, lastSound)) {
    if (sound) {
      AudioServicesDisposeSystemSoundID(sound);
    }
    xstrcpy(lastSound, filename);
    NSString* string = [NSString stringWithFormat:@"%s", filename];
    NSURL* wav = [NSURL fileURLWithPath:string];
    AudioServicesCreateSystemSoundID((__bridge CFURLRef)wav, &sound);
  }
  AudioServicesPlaySystemSound(sound);
}

void
iphone_soundBeep()
{
  AudioServicesPlayAlertSound(kSystemSoundID_Vibrate);
}
