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

#include <tcvm.h>
#include "..\barcode.h"

#define UNEXPORTED_METHOD
#include "bbappapi.h"

static alertFunc TC_alert;
extern Context currentContext;
extern HINSTANCE apidll;

typedef HBBCAMERA (__stdcall *ProcBBCameraOpen)(HWND);
typedef DWORD (__stdcall *ProcBBCameraClose)(HBBCAMERA);
typedef DWORD (__stdcall *ProcBBCameraPreviewStart)(HBBCAMERA, BBCameraParameter*);
typedef DWORD (__stdcall *ProcBBCameraPreviewStop)(HBBCAMERA);
typedef DWORD (__stdcall *ProcBBCameraCapture)(HBBCAMERA, BBCameraParameter*);
typedef DWORD (__stdcall *ProcBBCameraPreviewZoom)(HBBCAMERA, BBCameraParameter*);

ProcBBCameraOpen         _BBCameraOpen;
ProcBBCameraClose        _BBCameraClose;
ProcBBCameraPreviewStart _BBCameraPreviewStart;
ProcBBCameraPreviewStop  _BBCameraPreviewStop;
ProcBBCameraCapture      _BBCameraCapture;
ProcBBCameraPreviewZoom  _BBCameraPreviewZoom;

static BBCameraParameter cam;
static HBBCAMERA handle;
static HWND mainHWnd;

bool CameraLibOpen(OpenParams params)
{
   TC_alert = (alertFunc)params->getProcAddress(null, "alert");
   mainHWnd = params->mainHWnd;

   _BBCameraOpen       	   = (ProcBBCameraOpen        ) GetProcAddress(apidll, TEXT("BBCameraOpen"));
   _BBCameraClose      	   = (ProcBBCameraClose       ) GetProcAddress(apidll, TEXT("BBCameraClose"));
   _BBCameraPreviewStart   = (ProcBBCameraPreviewStart) GetProcAddress(apidll, TEXT("BBCameraPreviewStart"));
   _BBCameraPreviewStop	   = (ProcBBCameraPreviewStop ) GetProcAddress(apidll, TEXT("BBCameraPreviewStop"));
   _BBCameraCapture    	   = (ProcBBCameraCapture     ) GetProcAddress(apidll, TEXT("BBCameraCapture"));
   _BBCameraPreviewZoom	   = (ProcBBCameraPreviewZoom ) GetProcAddress(apidll, TEXT("BBCameraPreviewZoom"));

   return _BBCameraOpen && _BBCameraClose && _BBCameraPreviewStart && 
           _BBCameraPreviewStop && _BBCameraCapture && _BBCameraPreviewZoom;
}

void CameraLibClose()
{
   if (handle != 0)
   {
      _BBCameraPreviewStop(handle);
      _BBCameraClose(handle);
      handle = 0;
   }
}

#define CAM_fileName(o)        FIELD_OBJ(o, OBJ_CLASS(o), 0)
#define CAM_photoQuality(o)    FIELD_I32(o, 0)
#define CAM_photoWidth(o)      FIELD_I32(o, 1)
#define CAM_photoHeight(o)     FIELD_I32(o, 2)
#define CAM_strobe(o)          FIELD_I32(o, 3)
#define CAM_x(o)               FIELD_I32(o, 4)
#define CAM_y(o)               FIELD_I32(o, 5)
#define CAM_width(o)           FIELD_I32(o, 6)
#define CAM_height(o)          FIELD_I32(o, 7)
#define CAM_errorCode(o)       FIELD_I32(o, 8)

static TCHAR* getString(TCHAR* buf, TCObject str)
{
   JCharP c;
   int32 len;
   TCHAR* s = buf;
   if (!str)
      return NULL;
   c = String_charsStart(str);
   len = String_charsLen(str);
   for (; --len >= 0; c++)
      *s++ = *c == '/' ? '\\' : *c;
   *s = 0;
   return buf;
}

#ifdef __cplusplus
extern "C" {
#endif

//////////////////////////////////////////////////////////////////////////
SCAN_API void pC_click(NMParams p) // pidion/Camera native public boolean click();
{
   TCObject obj = p->obj[0];
   int32 ok = 0;
   if (handle)
   {
      DWORD ret;
      TCHAR fileName[255];
      cam.capture_file_name = getString(fileName, CAM_fileName(obj)); 
      cam.capture_strobe_on = 0;
      cam.capture_width = CAM_photoWidth(obj);
      cam.capture_height = CAM_photoHeight(obj);
      ret = _BBCameraCapture(handle, &cam);
      CAM_errorCode(obj) = ret;
      ok = ret == 0;
   }
   p->retI = ok;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void pC_setPreview_b(NMParams p) // pidion/Camera native public void setPreview(boolean on);
{
   TCObject obj = p->obj[0];
   int32 on = p->i32[0];
   DWORD ret;
   if (on && handle == 0)
   {
      handle = _BBCameraOpen((HWND)mainHWnd);
      ret = GetLastError();
      if (ret != 0)
         TC_alert("Error on BBCameraOpen: %d. Please reboot the pda.",ret);
      else
      {
         xmemzero(&cam, sizeof(cam));
         cam.preview_width = CAM_width(obj)>>1<<1;
         cam.preview_height = CAM_height(obj)>>1<<1;
         cam.preview_x = CAM_x(obj);
         cam.preview_y = CAM_y(obj);
         cam.brightness = cam.saturation = cam.contrast = 255;
         cam.capture_strobe_on = CAM_strobe(obj);
         cam.fr_mode = rate_auto;
         cam.ImageQuality = CAM_photoQuality(obj);
         ret = _BBCameraPreviewStart(handle, &cam);
         if (ret != 0)
            TC_alert("Error on BBCameraPreview : %d",ret);
      }
      if (ret != 0)
      {
         CAM_errorCode(obj) = ret;
         handle = 0;
      }
   }
   else
   if (!on && handle != 0)
      CameraLibClose();
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void pC_setZoom_i(NMParams p) // pidion/Camera native public void setZoom(int value);
{
   if (handle) 
   {
      cam.preview_zoom = p->i32[0];
      _BBCameraPreviewZoom(handle, &cam);
   }
}
#ifdef __cplusplus
}
#endif
