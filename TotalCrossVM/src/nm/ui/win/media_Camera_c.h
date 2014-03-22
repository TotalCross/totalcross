/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/
static void cameraClick(NMParams p)
{
    HRESULT hResult;
    SHCAMERACAPTURE shcc;
    TCObject obj = p->obj[0];
    TCHAR initialDir[255];
    TCHAR defFN[255];
    TCHAR title[255];
    TCHAR deviceId[MAX_PATH];
    int32 len;
    bool useThread = true;

    initialDir[0] = defFN[0] = 0;

    // guich@tc114_11: added support for Hand Held Dolphin
    len = sizeof(deviceId);
    SystemParametersInfo(SPI_GETOEMINFO, MAX_PATH, deviceId, 0); 
    if (wcsstr(deviceId,TEXT("Intermec")) != null) // intermec devices don't like capturing on a thread.
       useThread = false;
    if (wcsstr(deviceId,TEXT("Motorola")) != null)
       changeArcSoftAPI();
    if (wcsstr(deviceId,TEXT("Hand Held")) != null)
    {
       TCHAR* ret;
       bool lowRes = Camera_resolutionWidth(obj) != 640 || Camera_resolutionHeight(obj) != 480;
       getString(initialDir, Camera_initialDir(obj));
       getString(defFN, Camera_defaultFileName(obj));
       if (dolphinDll == null && (dolphinDll = loadLibrary("Dolphin")) == null)
          throwException(p->currentContext, IOException, "Error: cannot find Dolphin.dll");
       else
       {
          if (showDolphinCamera == null)
             showDolphinCamera = (procshowDolphinCamera) GetProcAddress(dolphinDll, TEXT("showDolphinCamera"));
          ret = showDolphinCamera(initialDir[0] ? initialDir : null, defFN[0] ? defFN : null, lowRes);
          p->retO = ret ? createStringObjectFromTCHAR(p->currentContext, ret, tcslen(ret)) : null;
       }
       return;
    }

    if (Camera_captureMode(obj) == 0) // photo?
       updatePicturesRegistry(Camera_resolutionWidth(obj),Camera_resolutionHeight(obj));

    // Set the SHCAMERACAPTURE structure.
    ZeroMemory(&shcc, sizeof(shcc));
    shcc.cbSize             = sizeof(shcc);
    shcc.hwndOwner          = mainHWnd;
    shcc.VideoTypes         = Camera_videoType(obj);
    shcc.Mode               = Camera_captureMode(obj);
    shcc.pszInitialDir      = getString(initialDir, Camera_initialDir(obj)); 
    shcc.pszDefaultFileName = getString(defFN, Camera_defaultFileName(obj));
    shcc.pszTitle           = getString(title, Camera_title(obj));
    shcc.StillQuality       = Camera_stillQuality(obj);
    shcc.nResolutionWidth   = Camera_resolutionWidth(obj);
    shcc.nResolutionHeight  = Camera_resolutionHeight(obj);
    shcc.nVideoTimeLimit    = Camera_videoTimeLimit(obj);

    // Display the Camera Capture dialog.

    hResult = SHCameraCapture(&shcc, useThread);
    if (hResult != S_OK && hResult != 1)
       throwException(p->currentContext, IOException, "Error when using the camera: %d (0x%X)", (int)hResult, (int)hResult);
    else
       p->retO = hResult == 1 ? null : createStringObjectFromTCHAR(p->currentContext, shcc.szFile, tcslen(shcc.szFile));
    if (wcsstr(deviceId,TEXT("Motorola")) != null)
       changeArcSoftAPI();
}

