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

// $Id: media_Sound_c.h,v 1.16 2011-01-04 13:31:18 guich Exp $

#include <SoundMgr.h>

#define defaultSoundVolume (sndMaxAmp * 3/4)

static void soundSetEnabled(bool b)
{
   UInt16 volume;

   if (b)
      volume = soundSettings.volumeState ? soundSettings.volume : defaultSoundVolume;
   else
      volume = 0;
   PrefSetPreference(prefSysSoundVolume, volume);
}

static void soundBeep(void)
{
   SndPlaySystemSound(sndInfo); // play beep
}

bool soundTone(int32 freq, uint16 duration)
{
   SndCommandType sct;
   sct.cmd = sndCmdFreqDurationAmp;
   sct.param1 = freq;
   sct.param2 = duration;
   sct.param3 = PrefGetPreference(prefSysSoundVolume); // default volume

   return (SndDoCmd(null, &sct, false) == errNone); // play tone
}
