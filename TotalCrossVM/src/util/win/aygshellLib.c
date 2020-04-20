/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  Copyright (C) 2012-2020 TotalCross Global Mobile Platform Ltda.   
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 2.1    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-2.1.txt                                     *
 *                                                                               *
 *********************************************************************************/

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
