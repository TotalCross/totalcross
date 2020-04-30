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

#include "tcvm.h"
#include "aygshellLib.h"

static HINSTANCE aygshellDll = NULL;

bool initAygshell()
{
#ifdef WINCE
  aygshellDll = LoadLibrary(_T("aygshell.dll"));

  if (aygshellDll != null) {
    _DMProcessConfigXML = (DMProcessConfigXMLProc)GetProcAddress(aygshellDll, _T("DMProcessConfigXML"));
    _Vibrate = (VibrateProc) GetProcAddress(aygshellDll, _T("Vibrate"));
    _VibrateStop = (VibrateStopProc) GetProcAddress(aygshellDll, _T("VibrateStop"));
    _SHSetImeMode = (SHSetImeModeProc) GetProcAddress(aygshellDll, _T("SHSetImeMode"));
    _SHGetImeMode = (SHGetImeModeProc) GetProcAddress(aygshellDll, _T("SHGetImeMode"));
    _SHCameraCapture = (SHCameraCaptureProc)GetProcAddress(aygshellDll, _T("SHCameraCapture"));
    _SndGetSound = (SndGetSoundProc) GetProcAddress(aygshellDll, _T("SndGetSound"));
    _SndSetSound = (SndSetSoundProc)GetProcAddress(aygshellDll, _T("SndSetSound"));
    _SHFullScreen = (SHFullScreenProc)GetProcAddress(aygshellDll, _T("SHFullScreen"));
    _SHDoneButton = (SHDoneButtonProc)GetProcAddress(aygshellDll, _T("SHDoneButton"));
  }
#endif
   return true;
}

void closeAygshell()
{
#ifdef WINCE
   if (aygshellDll != null)
   {
      FreeLibrary(aygshellDll);
      aygshellDll = null;
   }
#endif
}
