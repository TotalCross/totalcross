// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifndef MEDIA_SOUND_H
#define MEDIA_SOUND_H

#include "tcvm.h"
#include "win/aygshellLib.h"

#ifdef WINCE

#include<Mmsystem.h>

#ifndef WAVE_MAPPER
   #define WAVE_MAPPER     ((DWORD)(-1))
#endif

/*** WAVE_FORMAT_MIDI structures from wfmtmidi.h ***
 *
 * WAVEFORMAT_MIDI
 * WAVEFORMAT_MIDI_MESSAGE
 * WAVEFORMAT_MIDI_EXTRASIZE
 *
 * Pocket PC Platforms: Pocket PC 2002 and later
 * OS Versions: Windows CE 3.0 and later
 * Header: wfmtmidi.h
 *
 ***************************************************/
#ifndef WAVE_FORMAT_MIDI
   #define WAVE_FORMAT_MIDI 0x3000

   typedef struct _WAVEFORMAT_MIDI
   {
     WAVEFORMATEX wfx;
     UINT32 USecPerQuarterNote;
     UINT32 TicksPerQuarterNote;
   } WAVEFORMAT_MIDI, *LPWAVEFORMAT_MIDI;

   typedef struct _WAVEFORMAT_MIDI_MESSAGE
   {
     UINT32 DeltaTicks;
     DWORD MidiMsg;
   } WAVEFORMAT_MIDI_MESSAGE;

   #define WAVEFORMAT_MIDI_EXTRASIZE (sizeof(WAVEFORMAT_MIDI)-sizeof(WAVEFORMATEX))
#endif



extern bool SndGetSoundWM5(SND_EVENT sndEvent, SNDFILEINFO *soundFileInfo);
extern bool SndSetSoundWM5(SND_EVENT sndEvent, SNDFILEINFO *soundFileInfo);

#endif // #ifdef WINCE

#endif // #ifndef MEDIA_SOUND_H
