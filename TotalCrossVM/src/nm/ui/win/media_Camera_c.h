// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#if _WIN32_WCE >= 300

#include "win/aygshellLib.h"

typedef struct
{
   PSHCAMERACAPTURE pshcc;
   SHCameraCaptureProc funcSHCameraCapture;
   bool done;
   HRESULT ret;
} CameraThreadParams;

static DWORD WINAPI privateThreadFunc(VoidP argP)
{
   CameraThreadParams *p = (CameraThreadParams*)argP;
   p->ret = p->funcSHCameraCapture(p->pshcc);
   p->done = true;
   return (DWORD)0;
}

HRESULT SHCameraCapture(PSHCAMERACAPTURE pshcc, bool useThread)
{
   int32 id;

   if (_SHCameraCapture == NULL)
      return -2;
   else
   {
      CameraThreadParams* p = (CameraThreadParams*)xmalloc(sizeof(CameraThreadParams));
      if (p == NULL)
         return -3;
      else
      {
         HRESULT ret;
         p->funcSHCameraCapture = _SHCameraCapture;
         p->pshcc = pshcc;
         if (useThread)
            CreateThread(NULL, 0, privateThreadFunc, p, 0, &id); // guich@tc122_1: must be in a thread
         else
            privateThreadFunc(p);
         while (!p->done)
            Sleep(100);
         ret = p->ret;
         xfree(p);
         return ret;
      }
   }
}

static TCHAR* getString(TCHAR* buf, TCObject str)
{
   return str ? JCharP2TCHARPBuf(String_charsStart(str), String_charsLen(str), buf) : null;
}

/*[HKEY_LOCAL_MACHINE\Software\Microsoft\Pictures\Camera\OEM\PictureResolution] 
    "OptionNum"=dword:8 

    [HKEY_LOCAL_MACHINE\Software\Microsoft\Pictures\Camera\OEM\PictureResolution\1] 
    "ItemString"="88x72" 
    "Width"=dword:58 
    "Height"=dword:48 
    "HighQualityFileSize"=dword:9e6 
    "NormalQualityFileSize"=dword:420 
    "LowQualityFileSize"=dword:1c4 
*/
char* pictureResolutionValues[] = 
{
   "480x640","1E0","280","9DE00","4DE00","7800",
   "1024x1280","400","500","ABC00","4BC00","7000",
   0
};
char* pictureResolutionNames[] = {"ItemString", "Width", "Height", "HighQualityFileSize", "NormalQualityFileSize", "LowQualityFileSize", 0};

static void updatePicturesRegistry(int32 resW, int32 resH) // guich@tc111_10
{
   HKEY handle=(HKEY)0;
   DWORD dvalue,size,err;
   TCHAR keyname[128];
   TCHAR keyvalue[128];
   char option1[16];
   char **values, **names;
   int i,found=-1,ww,hh;
   // first, check if there's already a resolution like that
   if (resW > resH) // resolution must be in protrait
   {
      int t = resW;
      resW = resH;
      resH = t;
   }
   for (i = 1; found==-1; i++)
   {
      size = 4;
      wsprintf(keyname, TEXT("Software\\Microsoft\\Pictures\\Camera\\OEM\\PictureResolution\\%d"), i);
      err = RegOpenKeyEx(HKEY_LOCAL_MACHINE, keyname, 0, KEY_READ, &handle);
      if (err)
         if (i == 1) // no keys at all?
            return;
         else
            break;
      size = 4;
      // guich@tc126_24: query width and height directly
      ww = hh = -1;
      err = RegQueryValueEx(handle,TEXT("Width"),null,null,(uint8*)&ww,&size);
      err = RegQueryValueEx(handle,TEXT("Height"),null,null,(uint8*)&hh,&size);
      if (ww == resW && hh == resH)
         found = i;
      RegCloseKey(handle);
   }
   if (found == -1) // if key was not found, add a new entry with that resolution
   {
      // increase OptionNum count
      RegOpenKeyEx(HKEY_LOCAL_MACHINE, TEXT("Software\\Microsoft\\Pictures\\Camera\\OEM\\PictureResolution"), 0, KEY_READ|KEY_WRITE, &handle);
      size = 4; 
      RegQueryValueEx(handle,TEXT("OptionNum"),null,null,(uint8*)&dvalue,&size);
      dvalue++;
      RegSetValueEx(handle, TEXT("OptionNum"), null, REG_DWORD, (uint8*)&dvalue,4);
      RegCloseKey(handle);

      // now find the value at our table
      xstrprintf(option1, "%dx%d", resW, resH);
      for (values = pictureResolutionValues; *values; values += 6)
         if (xstrcmp(*values, option1) == 0) // found such resolution?
         {
            // create the new registry key
            size = 4;
            found = i;
            wsprintf(keyname, TEXT("Software\\Microsoft\\Pictures\\Camera\\OEM\\PictureResolution\\%d"), found);
            err = RegCreateKeyEx(HKEY_LOCAL_MACHINE, keyname, 0, 0, 0, KEY_WRITE, 0, &handle, &size);
            if (err != 0)
               return;
            names = pictureResolutionNames;
            // set the ItemString (the only SZ)
            err = RegSetValueEx(handle,CharP2TCHARPBuf(*names++, keyname), 0, REG_SZ, (uint8*)CharP2TCHARPBuf(*values, keyvalue),xstrlen(*values)*2+2); values++;
            for (; *names; values++, names++)
            {
               radix2int(*values, 16, &dvalue); // convert from hex to decimal
               RegSetValueEx(handle, CharP2TCHARPBuf(*names, keyname), 0, REG_DWORD, (uint8*)&dvalue,4);
            }
            RegCloseKey(handle);
            break;
         }
   }
   if (found != -1 && RegOpenKeyEx(HKEY_CURRENT_USER, TEXT("\\Software\\Microsoft\\Pictures\\Camera\\USER"), 0, KEY_WRITE, &handle) == 0) // update the currently selected resolution
   {
      dvalue = found-1; 
      RegSetValueEx(handle, TEXT("Resolution"), null, REG_DWORD, (uint8*)&dvalue,4);
      RegCloseKey(handle);
   }
}

// on Motorola ES400
// HKEY_LOCAL_MACHINE\System\Pictures\Camera\OEM
// OEMCAMERACAPTUREDLL  =  \\windows\\oemcameracapture.dll  ->  <delete>
// CameraApp            =  \\windows\\camera.exe            ->  \\windows\\pimg.exe
static void changeArcSoftAPI()
{
   HKEY handle=(HKEY)0;
   DWORD err,size;
   TCHAR buf[255];

   err = RegOpenKeyEx(HKEY_LOCAL_MACHINE, TEXT("System\\Pictures\\Camera\\OEM"), 0, KEY_ALL_ACCESS, &handle);
   if (err)
      return;
   size = sizeof(buf);
   buf[0] = 0;
   RegQueryValueEx(handle,TEXT("OEMCAMERACAPTUREDLL"),null,null,(uint8*)buf,&size);
   if (wcsicmp(buf,TEXT("\\windows\\oemcameracapture.dll")) == 0)
      RegSetValueEx(handle, TEXT("OEMCAMERACAPTUREDLL"), 0, REG_SZ, (uint8*)TEXT("\\windows\\_oemcameracapture.dll"),62);
   else
   if (wcsicmp(buf,TEXT("\\windows\\_oemcameracapture.dll")) == 0)
      RegSetValueEx(handle, TEXT("OEMCAMERACAPTUREDLL"), 0, REG_SZ, (uint8*)TEXT("\\windows\\oemcameracapture.dll"),60);

   size = sizeof(buf);
   buf[0] = 0;
   RegQueryValueEx(handle,TEXT("CameraApp"),null,null,(uint8*)buf,&size);
   if (wcsicmp(buf,TEXT("\\windows\\camera.exe")) == 0)
      RegSetValueEx(handle, TEXT("CameraApp"), 0, REG_SZ, (uint8*)TEXT("\\windows\\pimg.exe"),36);
   else
   if (wcsicmp(buf,TEXT("\\windows\\pimg.exe")) == 0)
      RegSetValueEx(handle, TEXT("CameraApp"), 0, REG_SZ, (uint8*)TEXT("\\windows\\camera.exe"),40);

   RegCloseKey(handle);
}

//////////////////////////////////////////////////////////////////////////
HINSTANCE dolphinDll;
typedef TCHAR* (__stdcall *procshowDolphinCamera)(TCHAR* path, TCHAR* file, bool lowRes);
procshowDolphinCamera showDolphinCamera;

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

#endif
