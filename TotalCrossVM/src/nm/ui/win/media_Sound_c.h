// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "media_Sound.h"

#define kFSPDWORD             4
#define kFSPPBYTE             (PBYTE) 4
#define kFSPPDWORD            (PDWORD) 4
#define kFSPSetTone           1
BOOL WaveBeeperHoneywell(unsigned frequency, unsigned duration)
{
	DWORD rc=0;

	HANDLE gFrontSpeakerDriverHandle = CreateFile(L"FSP1:", GENERIC_READ | GENERIC_WRITE, FILE_SHARE_READ | FILE_SHARE_WRITE, NULL, OPEN_EXISTING, 0, 0);
	if(gFrontSpeakerDriverHandle!=INVALID_HANDLE_VALUE)
	{
		rc= DeviceIoControl(gFrontSpeakerDriverHandle , kFSPSetTone, kFSPPBYTE, (DWORD) frequency, kFSPPBYTE, (DWORD) duration, kFSPPDWORD,  NULL);
		CloseHandle(gFrontSpeakerDriverHandle);
	}	
	return rc;
}

static void soundPlay(CharP filename)
{
#ifdef WP8
   nativeSoundPlayCPP(filename);
#elif !defined(WINCE) // not sure if this works on wince
   char mcidata[129]; // Data is returned by some MCI requests
   int32  mcidatalen=sizeof(mcidata)-1;
   char mcicmd[129];
   int32 rc;
   int32 millis;
   char* c;
   while ((c = xstrchr(filename, '/')) != 0) // replace slashes
	   *c = '\\';

   sprintf(mcicmd,"open \"%s\" alias MEDIAFILE",filename);
   rc = mciSendString(mcicmd,mcidata,mcidatalen,NULL);
   if (rc != 0) 
   {
	  mciGetErrorString(rc,mcidata,mcidatalen);
	  debug("Error when loading media: %s (%d)",mcidata, rc);
      return;
   }
   mciSendString("status MEDIAFILE length",mcidata,mcidatalen,NULL);
   millis = atoi(mcidata);
   rc = mciSendString("play MEDIAFILE",NULL,0,NULL);
   if (rc == 0)
      Sleep(millis);
   mciSendString("stop MEDIAFILE",NULL,0,NULL);
   mciSendString("close MEDIAFILE",NULL,0,NULL);
#elif defined(WINCE)
   TCHAR fn[MAX_PATHNAME];
   char* c;
   while ((c = xstrchr(filename, '/')) != 0) // replace slashes
	   *c = '\\';
   CharP2TCHARPBuf(filename, fn);
   PlaySound(fn, NULL, SND_ASYNC | SND_FILENAME);
#endif
}


/*****   soundBeep   *****
 *
 * MessageBeep
 *
 * Parameters
 * uType
 *    [in] Specifies the sound type, as identified by an
 *    entry in the [sounds] section of the registry.
 *    It is one of the following values:
 *    Value                Description
 *    0xFFFFFFFF           SystemDefault
 *    MB_ICONASTERISK      SystemAsterisk
 *    MB_ICONEXCLAMATION   SystemExclamation
 *    MB_ICONHAND          SystemHand
 *    MB_ICONQUESTION      SystemQuestion
 *    MB_OK                SystemDefault
 *
 * OS Versions: Windows CE 1.0 and later.
 * Header: Winbase.h.
 * Link Library: Msgbeep.lib.
 *
 ********************************/

static void soundBeep()
{
#if defined (WINCE)
   MessageBeep(MB_OK);
#else
   Beep(800, 200);
#endif
}

/*****   soundSetEnabled   *****
 *
 * WAVE_MAPPER
 *    Constant that always points to the default wave device on the system.
 *
 * WAVEOUTCAPS
 *
 * dwSupport
 *    Specifies the optional functionality supported by the device.
 *    The following table shows the possible values:
 *    Value                   Description
 *    WAVECAPS_LRVOLUME       Supports separate left and right volume control.
 *    WAVECAPS_PITCH          Supports pitch control.
 *    WAVECAPS_PLAYBACKRATE   Supports playback rate control.
 *    WAVECAPS_SYNC           Specifies that the driver is synchronous and blocks while playing a buffer.
 *    WAVECAPS_VOLUME         Supports volume control.
 *    WAVECAPS_SAMPLEACCURATE Returns sample-accurate position information.
 *
 * waveOutGetNumDevs
 * waveOutGetDevCaps
 * waveOutSetVolume
 *
 *
 * OS Versions: Windows CE 2.0 and later.
 * Header: Mmsystem.h.
 * Link Library: Coredll.lib.
 *
 ************************************/
static void soundSetEnabled(bool enableSound)
{
#if defined (WINCE)
   WAVEOUTCAPS tWaveoutCaps;
   SNDFILEINFO soundFileInfo;

   soundFileInfo.sstType = (enableSound ? SND_SOUNDTYPE_ON : SND_SOUNDTYPE_NONE);
   if (SndSetSoundWM5(SND_EVENT_ALL, &soundFileInfo))
      return;

   if (waveOutGetNumDevs() < 1)
      return;
   if (waveOutGetDevCaps(WAVE_MAPPER, &tWaveoutCaps, sizeof(tWaveoutCaps)) != MMSYSERR_NOERROR)
      return;
   if (!(tWaveoutCaps.dwSupport & WAVECAPS_VOLUME))
      return;

   if (enableSound)
      waveOutSetVolume((HWAVEOUT) WAVE_MAPPER, 0xFFFF);
   else
      waveOutSetVolume((HWAVEOUT) WAVE_MAPPER, 0x0000);
   //XXX how todo this in WP8?
#elif !defined WP8
   int32 numDevices, NumControls, DeviceID;
   int32 ControlID;
   HMIXER hMixer = null;
   MIXERLINE Line;
   MIXERCONTROLDETAILS Details;
   MIXERCONTROLDETAILS_UNSIGNED Value;

   //Get # of devices
   numDevices = mixerGetNumDevs();

   for(DeviceID = 0; DeviceID < numDevices; DeviceID++)
   {
      //Open the device
      mixerOpen(&hMixer, DeviceID, 0, 0, MIXER_OBJECTF_MIXER);

      Line.cbStruct = sizeof(MIXERLINE);
      Line.dwComponentType = MIXERLINE_COMPONENTTYPE_DST_SPEAKERS;
      mixerGetLineInfo((HMIXEROBJ) hMixer, &Line, MIXER_OBJECTF_HMIXER | MIXER_GETLINEINFOF_COMPONENTTYPE);

      NumControls = Line.cControls;
      for(ControlID = 1; ControlID <= NumControls ; ControlID++) //IDs are NOT zero-based
      {
         Details.cbStruct = sizeof(MIXERCONTROLDETAILS);
         Details.cbDetails = sizeof(MIXERCONTROLDETAILS_UNSIGNED);
         Details.paDetails = &Value;

         mixerGetControlDetails((HMIXEROBJ) hMixer, &Details, MIXER_OBJECTF_HMIXER | MIXER_GETCONTROLDETAILSF_VALUE);
         Details.dwControlID = 1; //Master Volume
         Details.cChannels = 1;
         Details.cMultipleItems = 0;
         Value.dwValue = enableSound ? 0 : ((MIXERCONTROLDETAILS_UNSIGNED*)Details.paDetails)->dwValue;

         mixerSetControlDetails((HMIXEROBJ) hMixer, &Details, MIXER_OBJECTF_HMIXER | MIXER_GETCONTROLDETAILSF_VALUE);
      }
      mixerClose(hMixer);
   }
#endif
}

/*****   soundTone   *****
 *
 * WAVEHDR
 * waveOutOpen
 * waveOutPrepareHeader
 * waveOutWrite
 * waveOutReset
 * waveOutUnprepareHeader
 * waveOutClose
 *
 * OS Versions: Windows CE 2.0 and later.
 * Header: Mmsystem.h.
 * Link Library: Coredll.lib.
 *
 * Beep
 *
 * Win32
 *
 *****************************
 *
 * CreateEvent
 * ResetEvent
 *
 * OS Versions: Windows CE 1.0 and later.
 * Header: Winbase.h.
 * Link Library: Coredll.lib, Nk.lib.
 *
 *****************************
 *
 * WaitForSingleObject
 *
 * OS Versions: Windows CE 1.0 and later.
 * Header: Winbase.h.
 * Link Library: Coredll.lib.
 *
 *****************************/

void soundTone(int32 frequency, int32 duration)
{
#if defined (WINCE)
   char buf[128];
   getDeviceId(buf);
   if (xstrstr(buf,"Hand")) // Hand Held Products
      WaveBeeperHoneywell(frequency, duration);
   else
   {
      WAVEFORMAT_MIDI waveMidi;
      HANDLE hEvent;
      HWAVEOUT hWaveOut;
      WAVEFORMAT_MIDI_MESSAGE midiMessage[2];
      WAVEHDR waveHdr;

      if (waveOutGetNumDevs() < 1)
         return;

      // Build a MIDI waveformat header
      xmemzero(&waveMidi, sizeof(waveMidi));

      waveMidi.wfx.wFormatTag = WAVE_FORMAT_MIDI;
      waveMidi.wfx.nChannels = 1;
      waveMidi.wfx.nBlockAlign = sizeof(WAVEFORMAT_MIDI_MESSAGE);
      waveMidi.wfx.cbSize = WAVEFORMAT_MIDI_EXTRASIZE;

      /// These fields adjust the interpretation of DeltaTicks, and thus the rate of playback
      // Set to 1 second. Note driver will default to 500000 if set to 0.
      waveMidi.USecPerQuarterNote = 1000000;
      // Set to 1000 so duration is expressed in milliseconds. Note driver will default to 96 if set to 0
      waveMidi.TicksPerQuarterNote = 1000;
      ///

      hEvent = CreateEvent(null, true, false, null);
      ResetEvent(hEvent);

      // Open the waveout device
      if (waveOutOpen(&hWaveOut, WAVE_MAPPER, (LPWAVEFORMATEX) &waveMidi, (DWORD) hEvent, 0, CALLBACK_EVENT) == MMSYSERR_NOERROR)
      {
         midiMessage[0].DeltaTicks = 1;                           // Note on
         midiMessage[0].MidiMsg = 0x7F0090 | (frequency << 8);    // Wait
         midiMessage[1].DeltaTicks = duration;                    // Note off
         midiMessage[1].MidiMsg = 0x7F0080 | (frequency << 8);

         waveHdr.lpData = (LPSTR) midiMessage;
         waveHdr.dwBufferLength = sizeof(midiMessage);
         waveHdr.dwFlags = 0;

         if (waveOutPrepareHeader(hWaveOut, &waveHdr, sizeof(waveHdr) ) == MMSYSERR_NOERROR)
         {
            // Play the data
            if (waveOutWrite(hWaveOut, &waveHdr, sizeof(waveHdr)) == MMSYSERR_NOERROR)
            {
               // Wait for playback to complete
               if (WaitForSingleObject(hEvent, (uint32)(duration*1.5)) == WAIT_TIMEOUT)
                  waveOutReset(hWaveOut);
            }

            /*
             * This function complements waveOutPrepareHeader. You must call this function before freeing the buffer.
             * After passing a buffer to the device driver with the waveOutWrite function, you must wait until
             * the driver is finished with the buffer before calling waveOutUnprepareHeader.
             * Unpreparing a buffer that has not been prepared has no effect, and the function returns zero.
             */
            waveOutUnprepareHeader(hWaveOut, &waveHdr, sizeof(waveHdr));
         }
         waveOutClose(hWaveOut);
      }
   }
#else
   Beep(frequency, duration);
#endif
}

#if defined (WINCE)
bool SndGetSoundWM5(SND_EVENT sndEvent, SNDFILEINFO *soundFileInfo)
{
   if (!isWindowsMobile || *tcSettings.romVersionPtr < 500 || _SndGetSound == NULL)
      return false;
   
   _SndGetSound(sndEvent, soundFileInfo);
   return true;
}

bool SndSetSoundWM5(SND_EVENT sndEvent, SNDFILEINFO *soundFileInfo)
{
   if (!isWindowsMobile || *tcSettings.romVersionPtr < 500 || _SndSetSound == NULL)
      return false;

   _SndSetSound(sndEvent, soundFileInfo, false);
   return true;
}
#endif
