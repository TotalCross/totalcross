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

#ifdef WINCE
#ifndef __AYGSHELL_LIB__
#define __AYGSHELL_LIB__

#include <aygshell.h>

//for Smartphones
typedef struct 
{
WORD wDuration;
BYTE bAmplitude;
BYTE bFrequency;
} VIBRATENOTE;

#define VIB_NONE 0
#define VIB_AYG 1
#define VIB_CORE 2
#define VIB_NOTAVAILABLE 3

typedef enum tagSHIME_MODE
{
    SHIME_MODE_NONE                = 0,
    SHIME_MODE_SPELL               = 1,
    SHIME_MODE_SPELL_CAPS          = 2,
    SHIME_MODE_SPELL_CAPS_LOCK     = 3,
    SHIME_MODE_AMBIGUOUS           = 4,
    SHIME_MODE_AMBIGUOUS_CAPS      = 5,
    SHIME_MODE_AMBIGUOUS_CAPS_LOCK = 6,
    SHIME_MODE_NUMBERS             = 7,
    SHIME_MODE_CUSTOM              = 8,
} SHIME_MODE;


typedef HRESULT (__stdcall *DMProcessConfigXMLProc)( LPCWSTR , DWORD , LPWSTR* );
typedef int (__stdcall *VibrateProc)(DWORD cvn, const VIBRATENOTE * rgvn, BOOL fRepeat, DWORD dwTimeout);
typedef int (__stdcall *VibrateStopProc)();
typedef HRESULT (__stdcall *SHSetImeModeProc)(HWND hWnd, SHIME_MODE nMode);
typedef HRESULT (__stdcall *SHGetImeModeProc)(HWND hWnd, SHIME_MODE* pnMode);

static DMProcessConfigXMLProc _DMProcessConfigXML = null;
static VibrateProc _Vibrate = null;
static VibrateStopProc _VibrateStop = null;
static SHSetImeModeProc _SHSetImeMode = null;
static SHGetImeModeProc _SHGetImeMode = null;


//////////////////////////////////////////////////////////////////////////////
//
//Flags for camera capture UI

typedef enum 
{
CAMERACAPTURE_MODE_STILL = 0,
CAMERACAPTURE_MODE_VIDEOONLY,
CAMERACAPTURE_MODE_VIDEOWITHAUDIO,
} CAMERACAPTURE_MODE;

typedef enum 
{
CAMERACAPTURE_STILLQUALITY_DEFAULT = 0,
CAMERACAPTURE_STILLQUALITY_LOW,
CAMERACAPTURE_STILLQUALITY_NORMAL,
CAMERACAPTURE_STILLQUALITY_HIGH,
} CAMERACAPTURE_STILLQUALITY;

typedef enum 
{
CAMERACAPTURE_VIDEOTYPE_ALL = 0xFFFF,
CAMERACAPTURE_VIDEOTYPE_STANDARD = 1,
CAMERACAPTURE_VIDEOTYPE_MESSAGING = 2,
} CAMERACAPTURE_VIDEOTYPES;

typedef struct tagSHCAMERACAPTURE
{
DWORD   cbSize;
HWND   hwndOwner;
TCHAR   szFile[MAX_PATH];
LPCTSTR   pszInitialDir;
LPCTSTR   pszDefaultFileName;
LPCTSTR   pszTitle;
CAMERACAPTURE_STILLQUALITY   StillQuality;
CAMERACAPTURE_VIDEOTYPES   VideoTypes;
DWORD   nResolutionWidth;
DWORD   nResolutionHeight;
DWORD   nVideoTimeLimit;
CAMERACAPTURE_MODE   Mode;
} SHCAMERACAPTURE, *PSHCAMERACAPTURE;


HRESULT SHCameraCapture(PSHCAMERACAPTURE pshcc, bool useThread);
typedef HRESULT (*SHCameraCaptureProc)(PSHCAMERACAPTURE pshcc);
static SHCameraCaptureProc _SHCameraCapture = NULL;

// end camera capture

// sound
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

typedef HRESULT (__stdcall *SndGetSoundProc)( SND_EVENT, SNDFILEINFO* );
static SndGetSoundProc _SndGetSound = NULL;

typedef HRESULT (__stdcall *SndSetSoundProc)( SND_EVENT, const SNDFILEINFO*, BOOL );
static SndSetSoundProc _SndSetSound = NULL;

//

//++++++
//
// SHFullScreen
//
typedef BOOL (__stdcall *SHFullScreenProc)( HWND hwndRequester, DWORD dwState );
static SHFullScreenProc _SHFullScreen = NULL;

//++++++
//
//SHDoneButton
//
typedef BOOL (__stdcall *SHDoneButtonProc)(HWND hwndRequester, DWORD dwState);
static SHDoneButtonProc _SHDoneButton = NULL;

bool initAygshell();
void closeAygshell();

#endif
#endif // ifdef WINCE
