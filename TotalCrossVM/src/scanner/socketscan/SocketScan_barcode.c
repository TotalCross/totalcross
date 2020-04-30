// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



//----------------------------------------------------------------------------
// Symbol Scanner Support
//
// This file includes all the native functions required to support the
// SuperWaba Scanner class.
//
// This dll starts up a thread to monitor the scanner, and uses
// SCAN_ReadLabelEvent to be notified of scans.
//
// It will also stop scanning for a period of time when the unit is
// powered up - see readme-ce.txt for more information.
//
// The power-up Event name is taken from registry setting
// HKCU\Software\SuperWaba\Scanner\pwrEvent, and defaults to the definition
// POWER_ON_EVENT if the registry key does not exist.
//----------------------------------------------------------------------------


#include "barcode.h"

// now Scanner specific includes
typedef int SCAN_RESULT;
#include "ScanAPI.h"

// Scanner variables
#define WM_INSERTION    WM_USER + 1 // the message we want on device insertions
#define WM_REMOVAL		WM_USER + 2 // the message we want on device removals
#define WM_SCANNERDATA	WM_USER + 3 // the message we want when data is available
#define WM_CHS_STATUS	WM_USER + 4 // the message we want when CHS status changes

typedef SCAN_RESULT (__stdcall *ScanInitProc)(HWND hWnd, UINT wmInsertion, UINT wmRemoval);
typedef SCAN_RESULT (__stdcall *ScanOpenDeviceProc)(HANDLE hScanner);
typedef SCAN_RESULT (__stdcall *ScanCloseDeviceProc)(HANDLE hScanner);
typedef SCAN_RESULT (__stdcall *ScanGetDevInfoProc)(HANDLE hScanner, LPSCANDEVINFO lpScanDevInfo);
typedef SCAN_RESULT (__stdcall *ScanRequestDataEventsProc)(HANDLE hScanner, HWND hWnd, UINT wmScannerData);
typedef SCAN_RESULT (__stdcall *ScanTriggerProc)(HANDLE hScanner);
typedef SCAN_RESULT (__stdcall *ScanGetDataProc)(HANDLE hScanner, TCHAR * lpBuff, LPINT BufSize);
//SCANAPI_API SCAN_RESULT ScanGetStatus(HANDLE hScanner);
typedef SCAN_RESULT (__stdcall *ScanDeinitProc)(void);
//SCANAPI_API SCAN_RESULT ScanErrToText(SCAN_RESULT Err, LPTSTR lpBuff, LPINT BufSize);
//SCANAPI_API SCAN_RESULT WINAPI ScanEnableDisableSymbology (HANDLE hScanner, INT nSymID, BOOL flag);

HMODULE socketScanDll = null;
ScanInitProc               ScanInitP;
ScanOpenDeviceProc         ScanOpenDeviceP;
ScanCloseDeviceProc        ScanCloseDeviceP;
ScanGetDevInfoProc         ScanGetDevInfoP;
ScanRequestDataEventsProc  ScanRequestDataEventsP;
ScanTriggerProc            ScanTriggerP;
ScanGetDataProc            ScanGetDataP;
ScanDeinitProc             ScanDeinitP;

HANDLE hScanner = INVALID_HANDLE_VALUE;
int32 ScannerDataSize = 0;
TCHAR ScannerData[256];
Method onEventMethod;

SCAN_API int32 LibOpen(OpenParams params)
{
   TCClass scannerClass;

   scannerClass = loadClass(params->currentContext, "totalcross.io.device.scanner.Scanner", false);
   onEventMethod = getMethod(scannerClass, false, "_onEvent", 1, J_INT);

   if ((socketScanDll = LoadLibrary(TEXT("ScanApi.dll"))) == null)
      return 0;

   ScanInitP               = (ScanInitProc)              GetProcAddress(socketScanDll, TEXT("ScanInit"));
   ScanOpenDeviceP         = (ScanOpenDeviceProc)        GetProcAddress(socketScanDll, TEXT("ScanOpenDevice"));
   ScanCloseDeviceP        = (ScanCloseDeviceProc)       GetProcAddress(socketScanDll, TEXT("ScanCloseDevice"));
   ScanGetDevInfoP         = (ScanGetDevInfoProc)        GetProcAddress(socketScanDll, TEXT("ScanGetDevInfo"));
   ScanRequestDataEventsP  = (ScanRequestDataEventsProc) GetProcAddress(socketScanDll, TEXT("ScanRequestDataEvents"));
   ScanTriggerP            = (ScanTriggerProc)           GetProcAddress(socketScanDll, TEXT("ScanTrigger"));
   ScanGetDataP            = (ScanGetDataProc)           GetProcAddress(socketScanDll, TEXT("ScanGetData"));
   ScanDeinitP             = (ScanDeinitProc)            GetProcAddress(socketScanDll, TEXT("ScanDeinit"));

   if (!ScanInitP    || !ScanOpenDeviceP  || !ScanTriggerP  || !ScanGetDevInfoP        || 
       !ScanDeinitP  || !ScanCloseDeviceP || !ScanGetDataP  || !ScanRequestDataEventsP )
   {
      FreeLibrary(socketScanDll);
      return 0;
   }

   return 1;
}


static int status(char*tit, int n)
{
   if (n != 0 && n != 8)
      return 0;
   return 1;
}

//----------------------------------------------------------------------------
// Called by the VM to let the native libraries handle particular events.
// Must return SWErrEventHandled or SWErrEventNotHandled.
//
// Handles scanning events and scan errors.
//----------------------------------------------------------------------------

SCAN_API bool HandleEvent(VoidP eventP)
{
   WinEvent* we = (WinEvent*) eventP;
   HANDLE receivedHandle = (HANDLE) we->lParam;	// The scanner handle is passed in         

   switch (we->msg)
   {
      case WM_INSERTION:
      {
         SCANDEVINFO ScanDevInfo = {0};
         ScanDevInfo.StructSize = sizeof(SCANDEVINFO);
         ScanOpenDeviceP(receivedHandle); // == 0
         ScanGetDevInfoP(receivedHandle, &ScanDevInfo);
         ScanRequestDataEventsP(receivedHandle, we->hWnd, WM_SCANNERDATA);
      } break;
      case WM_REMOVAL:
      {
         ScanCloseDeviceP(receivedHandle);
      } break;
      case WM_SCANNERDATA:
      {
         ScannerDataSize = we->wParam;
         status("ondata", ScanGetDataP(receivedHandle, (TCHARP) &ScannerData, &ScannerDataSize));
         executeMethod(we->currentContext, onEventMethod, 1); // 1 - SCANNED event number
      } break;
      default:
         return false;
   }

   hScanner = receivedHandle;
   return true;
}

SCAN_API void LibClose()
{
   if (hScanner != INVALID_HANDLE_VALUE)
   {
      ScanCloseDeviceP(hScanner);
      ScanDeinitP();
   }
   if (socketScanDll != null)
      FreeLibrary(socketScanDll);
}

//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_scannerActivate(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean scannerActivate();
{
   Err err = 0;
   if (hScanner == INVALID_HANDLE_VALUE)
      err = ScanInitP(getMainWindowHandle(), WM_INSERTION, WM_REMOVAL);
   p->retI = err == 0;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_setBarcodeParam_ib(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean setBarcodeParam(int barcodeType, boolean enable);
{
   int32 barcodeType = p->i32[0];
   int32 value = p->i32[1];
   bool ret = true;//0;

   //if (hScanner != INVALID_HANDLE_VALUE)
      //ret = ScanEnableDisableSymbology(hScanner, barcodeType, value) == 0;
   p->retI = ret;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_setParam_iii(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean setParam(int type, int barcodeType, int value);
{
   int32 type = p->i32[0];
   int32 barcodeType = p->i32[1];
   int32 value = p->i32[2];
   bool ret = false;

   if (hScanner != INVALID_HANDLE_VALUE)
      ret = status("setparam", ScanTriggerP(hScanner));
   p->retI = ret;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_setBarcodeLength_iiii(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean setBarcodeLength(int barcodeType, int lengthType, int min, int max);
{
   p->retI = true;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_commitBarcodeParams(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean commitBarcodeParams();
{
   p->retI = true;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_getData(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getData();
{
   int32 len = ScannerDataSize;
   ScannerDataSize = 0;

   p->retO = createStringObjectFromTCHAR(p->currentContext, ScannerData, len);
   setObjectLock(p->retO, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_getScanManagerVersion(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getScanManagerVersion();
{
   p->retO = createStringObjectFromCharP(p->currentContext, "SocketScan", -1);
   setObjectLock(p->retO, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_getScanPortDriverVersion(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getScanPortDriverVersion();
{
   p->retO = createStringObjectFromCharP(p->currentContext, "", -1);
   setObjectLock(p->retO, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_deactivate(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean deactivate();
{
   if (hScanner == INVALID_HANDLE_VALUE)   // if scanner failed to open
      p->retI = false;

   ScanCloseDeviceP(hScanner);
   ScanDeinitP();
   hScanner = INVALID_HANDLE_VALUE;
   p->retI = true;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_setParam_ss(NMParams p) // totalcross/io/device/scanner/Scanner native public static void setParam(String what, String value);
{
}
