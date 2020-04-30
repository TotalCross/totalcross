// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifndef MEDIA_CLIP_H
#define MEDIA_CLIP_H

#include "tcvm.h"

enum
{
   UNREALIZED  = 0,
   REALIZED    = 1,
   PREFETCHED  = 2,
   STARTED     = 3,
   CLOSED      = 4,
};


enum
{
   mediaStarted = 0,
   mediaPaused = 1,
   mediaFinished = 2,
};

#pragma pack(2)  // make sure structure members are aligned at 2 bytes

typedef struct
{
   uint32 ChunkID;                  // 0 - 4
   uint32 ChunkSize;                // 4 - 8
   uint32 Format;                   // 8 - 12

   uint32 Subchunk1ID;              // 12 - 16
   uint32 Subchunk1Size;            // 16 - 20
   uint16 AudioFormat;              // 20 - 22
   uint16 NumChannels;              // 22 - 24
   uint32 SampleRate;               // 24 - 28
   uint32 ByteRate;                 // 28 - 32
   uint16 BlockAlign;               // 32 - 34
   uint16 BitsPerSample;            // 34 - 36

   uint16 ExtraParamSize;           // 36 - 38

   uint32 Subchunk2ID;              // 38 - 42
   uint32 Subchunk2Size;            // 42 - 46
} TWaveHeader, *WaveHeader;

#pragma pack()  // restore structure member alignment to default

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

#ifndef _SOUNDFILE_H_
   typedef enum tagSND_SOUNDTYPE
   {
       SND_SOUNDTYPE_ON,             // If sound is currently Vibrate or None this will restore the
                                     // sound to the previous value. This is only valid for the SND_EVENT_ALL
                                     // SND_EVENT.
       SND_SOUNDTYPE_FILE,           // Soundfile will be specified by SNDFILEINFO.szPathName
       SND_SOUNDTYPE_VIBRATE,        // Sound is vibration.  SNDFILEINFO.szPathName is ignored.
       SND_SOUNDTYPE_NONE,           // No sound (silence).  SNDFILEINFO.szPathName is ignored.
       SND_SOUNDTYPE_DISABLE,        // Only applies to SND_EVENTs for KNOWNCALLER and ROAMING
                                     // Sound will be disabled and behavior will revert back to default.
       SND_SOUNDTYPE_LAST = SND_SOUNDTYPE_DISABLE
   } SND_SOUNDTYPE;

   typedef struct tagSNDFILEINFO
   {
       TCHAR szPathName[MAX_PATH];
       TCHAR szDisplayName[MAX_PATH];
       SND_SOUNDTYPE sstType;
   } SNDFILEINFO;

   typedef enum tagSND_EVENT
   {
       SND_EVENT_ALL,                       // This is a special sound event that applies to all sounds
                                            // on a system wide scale. Currently only supported on Pocket PC, the
                                            // only valid values for SNDFILEINFO.sstType are SND_SOUNDTYPE_ON,
                                            // SND_SOUNDTYPE_VIBRATE, and SND_SOUNDTYPE_NONE. Invoking SndSetSound
                                            // with SND_EVENT_ALL and a SNDFILEINFO struct set to one of the above
                                            // values will be equivalent to the user using the Pocket PC Volume Bubble
                                            // on the home screen. If the volume bubble is currently open the change will
                                            // be reflected in the bubble right away. If the bubble is not open the change
                                            // will take effect immediately and will be visible the next time the user
                                            // opens the bubble.
       SND_EVENT_RINGTONELINE1,             // The ringtone for line 1 of the phone
       SND_EVENT_RINGTONELINE2,             // The ringtone for line 2 of the phone
       SND_EVENT_KNOWNCALLERRINGTONELINE1,  // The ringtone to play for a caller in the contact list who has no contact-specific ring tone.
                                            // This event does not support the "vibrate" and "none" sound types
                                            // Currently applies to both lines without distinction
       SND_EVENT_ROAMINGRINGTONELINE1,      // The ringtone to play while roaming.  This has higher priority than other ring tones.
                                            // This event does not support the "vibrate" and "none" sound types
                                            // Currently applies to both lines without distinction

       SND_EVENT_LAST = SND_EVENT_ROAMINGRINGTONELINE1
   } SND_EVENT;
#endif // #ifndef _SOUNDFILE_H_

extern bool SndGetSoundWM5(SND_EVENT sndEvent, SNDFILEINFO *soundFileInfo);
extern bool SndSetSoundWM5(SND_EVENT sndEvent, SNDFILEINFO *soundFileInfo);

#endif // #ifdef WINCE

#endif // #ifndef MEDIA_CLIP_H
